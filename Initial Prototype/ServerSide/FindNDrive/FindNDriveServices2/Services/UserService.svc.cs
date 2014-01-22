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
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DomainObjects.Constants;
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
        /// The gcm manager.
        /// </summary>
        private readonly GCMManager gcmManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="UserService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        /// <param name="gcmManager"></param>
        public UserService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, GCMManager gcmManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.gcmManager = gcmManager;
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
            var validatedUser = ValidationHelper.Validate(login);
       
            if (!validatedUser.IsValid || !WebSecurity.Login(login.UserName, login.Password))
            {   
                return ResponseBuilder.Failure<User>("Invalid Username or Password.");
            }

            var userId = WebSecurity.GetUserId(login.UserName);
            var loggedInUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.UserId == userId);
            if (loggedInUser != null)
            {
                this.sessionManager.GenerateNewSession(loggedInUser.UserId);
                loggedInUser.GCMRegistrationID = login.GCMRegistrationID;
                loggedInUser.Status = Status.Online;

                var gcmNotifications =
                    this.findNDriveUnitOfWork.GCMNotificationsRepository.AsQueryable().Where(_ => _.UserId == userId && !_.Delivered).ToList();

                gcmNotifications.ForEach(
                    delegate(GCMNotification gcmNotification)
                        {
                            this.gcmManager.SendNotification(new Collection<string>{loggedInUser.GCMRegistrationID}, gcmNotification.NotificationType, gcmNotification.NotificationArguments, gcmNotification.ContentTitle, gcmNotification.NotificationMessage);
                            gcmNotification.Delivered = true;
                        });

                this.findNDriveUnitOfWork.Commit();

                return ResponseBuilder.Success(loggedInUser);
            }
            return ResponseBuilder.Failure<User>("User does not exist!");
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
            if (this.sessionManager.ValidateSession())
            {
                var userId = this.sessionManager.GetUserId();

                if (userId == -1)
                {
                    return ResponseBuilder.Failure<User>("Unauthorised");
                }

                var loggedInUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.UserId == userId);
                if (loggedInUser != null)
                {
                    loggedInUser.Status = Status.Online;
                }

                this.findNDriveUnitOfWork.Commit();
                return ResponseBuilder.Success(loggedInUser);
            }

            return ResponseBuilder.Failure<User>("Unauthorised");
        }

        /// <summary>
        /// Registers a new user.
        /// </summary>
        /// <param name="register"></param>
        /// <returns></returns>
        public ServiceResponse<User> RegisterUser(RegisterDTO register)
        {
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

            if (!validatedRegisterDTO.IsValid)
            {
                return ResponseBuilder.Failure<User>("Failed to register");
            }

            WebSecurity.CreateUserAndAccount(register.User.UserName, register.Password);
            register.User.UserId = WebSecurity.GetUserId(register.User.UserName);

            var newUser = new User()
                              {
                                  EmailAddress = register.User.EmailAddress,
                                  Role = Roles.User,
                                  UserName = register.User.UserName,
                                  UserId = register.User.UserId
                              };

            this.sessionManager.GenerateNewSession(newUser.UserId);
            this.findNDriveUnitOfWork.UserRepository.Add(newUser);
            this.findNDriveUnitOfWork.Commit();

            return ResponseBuilder.Success(newUser);
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
            return ResponseBuilder.Success(success);
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
    }
}

