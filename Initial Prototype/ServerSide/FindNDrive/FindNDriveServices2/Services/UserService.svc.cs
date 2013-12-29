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

    using DomainObjects.DOmains;

    using FindNDriveDataAccessLayer;

    using FindNDriveInfrastructureCore;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    using WebMatrix.WebData;

    using Roles = DomainObjects.Constants.Roles;

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
        private readonly FindNDriveUnitOfWork _findNDriveUnitOfWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager _sessionManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="UserService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        public UserService(FindNDriveUnitOfWork findNDriveUnitOfWork ,SessionManager sessionManager)
        {
            this._findNDriveUnitOfWork = findNDriveUnitOfWork;
            this._sessionManager = sessionManager;
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
                var proxy = this._findNDriveUnitOfWork.UserRepository.Find(WebSecurity.GetUserId(login.UserName));
                this._sessionManager.GenerateNewSession(proxy.UserId);
              
                loggedInUser = new User()
                {
                    FirstName = proxy.FirstName,
                    LastName = proxy.LastName,
                    DateOfBirth = proxy.DateOfBirth,
                    EmailAddress = proxy.EmailAddress,
                    Gender = proxy.Gender,
                    Role = Roles.User,
                    UserName = proxy.UserName,
                    UserId = proxy.UserId
                };
            }

            return new ServiceResponse<User>
            {
                Result = loggedInUser,
                ServiceResponseCode = (loggedInUser == null) ? ServiceResponseCode.Failure : ServiceResponseCode.Success,
                ErrorMessages = validatedUser.ErrorMessages
            };
        }

        /// <summary>
        /// The auto user login.
        /// </summary>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> AutoUserLogin()
        {
            User loggedInUser = null;

            if (this._sessionManager.ValidateSession())
            {
                var userId = this._sessionManager.GetUserId();

                if (userId != -1)
                {
                    loggedInUser = this._findNDriveUnitOfWork.UserRepository.Find(userId);  
                }
            }

            return new ServiceResponse<User>
            {
                Result = loggedInUser,
                ServiceResponseCode = (loggedInUser == null) ? ServiceResponseCode.Failure : ServiceResponseCode.Success,
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
            if (this._findNDriveUnitOfWork.UserRepository.AsQueryable().Any(_ => _.UserName.Equals(register.User.UserName)))
            {
                return ResponseBuilder.Failure<User>("Account with this username already exists.");
            }

            //Check if an account with the same username already exists.
            if (this._findNDriveUnitOfWork.UserRepository.AsQueryable().Any(_ => _.EmailAddress.Equals(register.User.EmailAddress)))
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

                this._findNDriveUnitOfWork.UserRepository.Add(newUser);

                this._findNDriveUnitOfWork.Commit();
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
            var success = this._sessionManager.InvalidateSession(forceInvalidate);
 
            return new ServiceResponse<bool>
            {
                Result = success,
                ServiceResponseCode = success ? ServiceResponseCode.Success : ServiceResponseCode.Failure,
                ErrorMessages = null
            };
        }
    }
    
}

