// --------------------------------------------------------------------------------------------------------------------
// <copyright file="UserService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   The user service.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Services
{
    using System.Collections.Generic;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveInfrastructureCore;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    using WebMatrix.WebData;

    using Roles = DomainObjects.Constants.Roles;
    using User = DomainObjects.Domains.User;

    /// <summary>
    /// The user service.
    /// </summary>
    [ServiceBehavior(
           InstanceContextMode = InstanceContextMode.PerCall,
           ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class UserService : IUserService
    {

        /// <summary>
        /// The unit of work, which provides access to the required Repositories, and exposes
        /// a commit method to complete the unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager sessionManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="UserService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        public UserService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, GCMManager gcmManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
        }

        public UserService()
        {
           
        }

        /// <summary>
        /// Logs a user in.
        /// </summary>
        /// <param name="login"></param>
        /// <returns></returns>
        public ServiceResponse<User> ManualUserLogin(LoginDTO login)
        {
            User loggedInUser = null;

            var validatedUser = ValidationHelper.Validate(login);
       
            if (!validatedUser.IsValid || !WebSecurity.Login(login.UserName, login.Password))
            {   
                validatedUser.ErrorMessages.Add("Invalid Username or Password.");
                validatedUser.IsValid = false;
            }
            else
            {
                loggedInUser = this.findNDriveUnitOfWork.UserRepository.Find(WebSecurity.GetUserId(login.UserName));
                this.sessionManager.GenerateNewSession(loggedInUser.UserId);
                loggedInUser.GCMRegistrationID = login.GCMRegistrationID;

                this.findNDriveUnitOfWork.Commit();
            }

            return new ServiceResponse<User>
            {
                Result = loggedInUser,
                ServiceResponseCode = (validatedUser.IsValid) ? ServiceResponseCode.Success : ServiceResponseCode.Unauthorized,
                ErrorMessages = validatedUser.ErrorMessages
            };
        }

        /// <summary>
        /// The auto user login.
        /// </summary>
        /// <param name="sessionDTO">
        /// The session DTO.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> AutoUserLogin()
        {
            User loggedInUser = null;

            if (this.sessionManager.ValidateSession())
            {
                var userId = this.sessionManager.GetUserId();
                
                if (userId != -1)
                {
                    loggedInUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.UserId == userId);
                }
            }

            return new ServiceResponse<User>
            {
                Result = loggedInUser,
                ServiceResponseCode = (loggedInUser == null) ? ServiceResponseCode.Failure : ServiceResponseCode.Success
            };
        }

        /// <summary>
        /// Registers a new user.
        /// </summary>
        /// <param name="register"></param>
        /// <returns></returns>
        public ServiceResponse<User> RegisterUser(RegisterDTO register)
        {
            User newUser = null;

            var validatedRegisterDTO = ValidationHelper.Validate(register);

            //Check if an account with the same username already exists.
            if (this.findNDriveUnitOfWork.UserRepository.AsQueryable().Any(_ => _.UserName.Equals(register.User.UserName)))
            {
                return ResponseBuilder.Failure<User>("Account with this username already exists.");
            }

            //Check if an account with the same username already exists.
            if (this.findNDriveUnitOfWork.UserRepository.AsQueryable().Any(_ => _.EmailAddress.Equals(register.User.EmailAddress)))
            {
                return ResponseBuilder.Failure<User>("Account with this email address already exists.");
            }

            if (validatedRegisterDTO.IsValid)
            {
                WebSecurity.CreateUserAndAccount(register.User.UserName, register.Password);
                register.User.UserId = WebSecurity.GetUserId(register.User.UserName);

                newUser = new User()
                {
                    FirstName = register.User.FirstName,
                    LastName = register.User.LastName,
                    DateOfBirth = register.User.DateOfBirth,
                    EmailAddress = register.User.EmailAddress,
                    Gender = register.User.Gender,
                    Role = Roles.User,
                    UserName = register.User.UserName,
                    UserId = register.User.UserId
                };

                this.sessionManager.GenerateNewSession(newUser.UserId);
                this.findNDriveUnitOfWork.UserRepository.Add(newUser);
                this.findNDriveUnitOfWork.Commit();
            }

            return new ServiceResponse<User>
            {
                Result = newUser,
                ServiceResponseCode =
                    validatedRegisterDTO.IsValid ? ServiceResponseCode.Success : ServiceResponseCode.Failure,
                ErrorMessages = validatedRegisterDTO.ErrorMessages
            };
        }

        /// <summary>
        /// The logout user.
        /// </summary>
        /// <param name="forceInvalidate">
        /// The force invalidate.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<bool> LogoutUser(bool forceInvalidate)
        {
            var success = this.sessionManager.InvalidateSession(forceInvalidate);
 
            return new ServiceResponse<bool>
            {
                Result = success,
                ServiceResponseCode = success ? ServiceResponseCode.Success : ServiceResponseCode.Failure
            };
        }

        /// <summary>
        /// The refresh user.
        /// </summary>
        /// <param name="userId">
        /// The user id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> RefreshUser(int userId)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new User());
            }

            var user = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == userId);

            if (user != null)
            {
                return ResponseBuilder.Success(user);
            }

            return ResponseBuilder.Failure<User>("Invalid user Id");
        }

        /// <summary>
        /// The add travel buddy.
        /// </summary>
        /// <param name="user">
        /// The user.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> AddTravelBuddy(FriendDTO user)
        {
            //TODO: Check if buddy is already in the list. Return error if yes.
            var targetUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == user.TargetUserId);

            var newBuddy = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == user.FriendUserId);

            targetUser.Friends.Add(newBuddy);

            this.findNDriveUnitOfWork.Commit();

            return new ServiceResponse<User>()
                       {
                           Result = targetUser,
                           ServiceResponseCode = ServiceResponseCode.Success
                       };
        }

        public ServiceResponse<List<User>> GetTravelBuddies(int userId)
        {
            var user =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == userId)
                    .Friends.ToList();

            return new ServiceResponse<List<User>>()
                       {
                           Result = user,
                           ServiceResponseCode = ServiceResponseCode.Success
                       };
        }
    }
    
}

