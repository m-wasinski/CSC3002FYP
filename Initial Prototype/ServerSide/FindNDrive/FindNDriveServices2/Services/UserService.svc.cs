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
    using System.Data.SqlClient;
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

    using Microsoft.Practices.ObjectBuilder2;

    using Newtonsoft.Json;

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
        /// The notification manager.
        /// </summary>
        private readonly NotificationManager notificationManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="UserService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        /// <param name="notificationManager">
        /// The notification Manager.
        /// </param>
        public UserService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.notificationManager = notificationManager;
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

            if (loggedInUser == null)
            {
                return ResponseBuilder.Failure<User>("User does not exist!");
            }

            // Check if this user is currently logged onto another device, if yes, log them out.
            if (loggedInUser.GCMRegistrationID != null && !loggedInUser.GCMRegistrationID.Equals("0") 
                && loggedInUser.Status == Status.Online && !loggedInUser.GCMRegistrationID.Equals(login.GCMRegistrationID))
            {
                this.notificationManager.SendNotification(
                    new Collection<User> { loggedInUser },
                    "LOGOUT",
                    NotificationType.Logout,
                    "LOGOUT");
            }

            // Check if another user has been logged on on the same device before.
            var userToReset =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .FirstOrDefault(
                        _ =>
                        _.GCMRegistrationID == loggedInUser.GCMRegistrationID
                        && !_.UserName.Equals(loggedInUser.UserName));

            // If yes, reset this user's GCM registration ID to 0 to prevent GCM notifications from being sent to the wrong device.
            if (userToReset != null)
            {
                userToReset.GCMRegistrationID = "0";
                userToReset.Status = Status.Offline;
            }

            this.sessionManager.GenerateNewSession(loggedInUser.UserId);
            loggedInUser.GCMRegistrationID = login.GCMRegistrationID;
            loggedInUser.Status = Status.Online;

            this.findNDriveUnitOfWork.Commit();

            this.notificationManager.SendOfflineGCMNotification(loggedInUser);

            return ResponseBuilder.Success(loggedInUser);
        }

        /// <summary>
        /// The auto user login.
        /// </summary>
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
                    return ResponseBuilder.Failure<User>("MANUAL LOGIN");
                }

                var loggedInUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.UserId == userId);
                if (loggedInUser != null)
                {
                    loggedInUser.Status = Status.Online;
                }

                this.findNDriveUnitOfWork.Commit();
                return ResponseBuilder.Success(loggedInUser);
            }

            return ResponseBuilder.Failure<User>("MANUAL LOGIN");
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
            if (
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Any(_ => _.UserName.Equals(register.User.UserName)))
            {
                return ResponseBuilder.Failure<User>("Account with this username already exists.");
            }
            //Check if an account with the same username already exists.
            if (
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Any(_ => _.EmailAddress.Equals(register.User.EmailAddress)))
            {
                return ResponseBuilder.Failure<User>("Account with this email address already exists.");
            }

            if (!validatedRegisterDTO.IsValid)
            {
                return ResponseBuilder.Failure<User>("Failed to register");
            }

            try
            {
                WebSecurity.CreateUserAndAccount(register.User.UserName, register.Password);
                register.User.UserId = WebSecurity.GetUserId(register.User.UserName);

                var newUser = new User
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
            catch (Exception e)
            {
                return ResponseBuilder.Failure<User>("Account with this username already exists.");
            }     
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

            return user != null ? ResponseBuilder.Success(user) : ResponseBuilder.Unauthorised(new User());
        }
    }
}

