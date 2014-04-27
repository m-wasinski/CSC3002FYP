// --------------------------------------------------------------------------------------------------------------------
// <copyright file="UserService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   The user service.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.Services
{
    using System;
    using System.Data.Entity;
    using System.IO;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;
    using System.ServiceModel.Web;

    using DataAccessLayer;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using global::Services.Contracts;
    using global::Services.DTOs;
    using global::Services.ServiceResponses;
    using global::Services.ServiceUtils;

    using WebMatrix.WebData;

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

        /// <summary>
        /// Performs a standard user login using credentials provided by the user in the logindto object.
        /// </summary>
        /// <param name="login">
        /// The login - loginDTO object containing user's username and password that will be verified by the WebSecurity module.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> ManualUserLogin(LoginDTO login)
        {
            var validatedUser = ValidationHelper.Validate(login);
       
            // Verify user's username and password using the WebSecurity module.
            if (!validatedUser.IsValid || !WebSecurity.Login(login.UserName, login.Password))
            {   
                return ServiceResponseBuilder.Failure<User>("Invalid Username or Password.");
            }

            var userId = WebSecurity.GetUserId(login.UserName);
            var loggedInUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable().Include(_ => _.PrivacySettings).FirstOrDefault(_ => _.UserId == userId);

            if (loggedInUser == null)
            {
                return ServiceResponseBuilder.Failure<User>("User does not exist!");
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
                userToReset.GCMRegistrationID = null;
                userToReset.Status = Status.Offline;
            }

            // Update current user status and save it to the database.
            this.sessionManager.GenerateNewSession(loggedInUser.UserId);
            loggedInUser.GCMRegistrationID = login.GCMRegistrationID;
            loggedInUser.Status = Status.Online;
            loggedInUser.LastLogon = DateTime.Now;

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(loggedInUser);
        }

        /// <summary>
        /// Attempts to perform automatic user login by verifying the session arguments located in supplied HTTP headers.
        /// Session is verified inside session manager.
        /// </summary>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> AutoUserLogin()
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Failure<User>();
            }

            var userId = this.sessionManager.GetUserId();

            if (userId == -1)
            {
                return ServiceResponseBuilder.Failure<User>();
            }

            var loggedInUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable().Include(_ => _.PrivacySettings).FirstOrDefault(_ => _.UserId == userId);

            if (loggedInUser == null)
            {
                return ServiceResponseBuilder.Failure<User>("User with this id does not exist.");
            }

            loggedInUser.Status = Status.Online;
            loggedInUser.LastLogon = DateTime.Now;
            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(loggedInUser);
        }

        /// <summary>
        /// Registers a new user with the system.
        /// </summary>
        /// <param name="register">
        /// The register - contains a user object with the basic information provided by the user to create a new account with the system.
        /// Other information needed is set to default values which the user can then change inside their profile editor.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> RegisterUser(RegisterDTO register)
        {
            var validatedObject = ValidationHelper.Validate(register);

            if (!validatedObject.IsValid)
            {
                return ServiceResponseBuilder.Failure<User>(validatedObject.ErrorMessages);
            }

            // Check if an account with the same username already exists.
            if (WebSecurity.UserExists(register.User.UserName))
            {
                return ServiceResponseBuilder.Failure<User>("Account with this username already exists.");
            }

            // Check if an account with the same username already exists.
            if (
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Any(_ => _.EmailAddress.Equals(register.User.EmailAddress)))
            {
                return ServiceResponseBuilder.Failure<User>("Account with this email address already exists.");
            }

            // Prepare the default profile picture.
            var img = Properties.Resources.default_picture;
            byte[] arr;

            using (var ms = new MemoryStream())
            {
                img.Save(ms, System.Drawing.Imaging.ImageFormat.Png);
                arr = ms.ToArray();
            }

            try
            {
                WebSecurity.CreateUserAndAccount(register.User.UserName, register.Password);
                register.User.UserId = WebSecurity.GetUserId(register.User.UserName);

                // Create the new user.
                var newUser = new User
                {
                    EmailAddress = register.User.EmailAddress,
                    UserName = register.User.UserName,
                    UserId = register.User.UserId,
                    FirstName = string.Empty,
                    LastName = string.Empty,
                    GCMRegistrationID = register.User.GCMRegistrationID,
                    MemberSince = DateTime.Now,
                    AverageRating = 0,
                    LastLogon = DateTime.Now,
                    ProfilePicture = new ProfilePicture
                                         {  
                                             ProfilePictureId = register.User.UserId,
                                             ProfilePictureBytes = arr
                                         },
                                         PrivacySettings = new PrivacySettings
                                                               {
                                                                   DateOfBirthPrivacyLevel = PrivacyLevel.Private,
                                                                   EmailPrivacyLevel = PrivacyLevel.Private,
                                                                   GenderPrivacyLevel = PrivacyLevel.Private,
                                                                   JourneysPrivacyLevel = PrivacyLevel.Everyone,
                                                                   PhoneNumberPrivacyLevel = PrivacyLevel.Private,
                                                                   RatingPrivacyLevel = PrivacyLevel.Everyone
                                                               },
                                                               VotesCount = 0,
                                                               DateOfBirth = null,
                                                               Gender = Gender.NotAvailable,
                                                               PhoneNumber = null,
                                                               Status = Status.Online                                               
                };

                // Check if another user has been logged on on the same device before.
                var userToReset =
                    this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                        .FirstOrDefault(
                            _ =>
                            _.GCMRegistrationID == register.User.GCMRegistrationID);

                // If yes, reset this user's GCM registration ID to 0 to prevent GCM notifications from being sent to the wrong device.
                if (userToReset != null)
                {
                    userToReset.GCMRegistrationID = "0";
                    userToReset.Status = Status.Offline;
                }

                this.findNDriveUnitOfWork.UserRepository.Add(newUser);
                this.findNDriveUnitOfWork.Commit();

                this.sessionManager.GenerateNewSession(newUser.UserId);

                EmailUtils.SendEmail(
                    new[] { newUser.EmailAddress },
                    null,
                    "Welcome to FindNDrive",
                    string.Format("Hello {0}, welcome to FindNDrive. This is just a short message to let you know that your account was created successfully. You're good to go!", newUser.UserName),
                    false);

                return ServiceResponseBuilder.Success(newUser);
            }
            catch (Exception)
            {
                return ServiceResponseBuilder.Failure<User>("Account with this username already exists.");
            }     
        }

        /// <summary>
        /// Logs the current user out.
        /// </summary>
        /// <param name="forceInvalidate">
        /// The force invalidate - tells whether user wants to perform a logout from their permanent session if one was established.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse LogoutUser(bool forceInvalidate)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            this.sessionManager.InvalidateSession(forceInvalidate);
            return ServiceResponseBuilder.Success();
        }

        /// <summary>
        /// Updates information about the current user.
        /// </summary>
        /// <param name="userDTO">
        /// The user dto - contains the new information provided by the user.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> UpdateUser(UserDTO userDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised<User>();
            }

            var user = this.findNDriveUnitOfWork.UserRepository.AsQueryable().FirstOrDefault(_ => _.UserId == userDTO.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<User>("Invalid user id.");
            }

            if (!string.IsNullOrWhiteSpace(userDTO.FirstName) && !string.Equals(user.FirstName, userDTO.FirstName))
            {
                user.FirstName = userDTO.FirstName;
            }

            if (!string.IsNullOrWhiteSpace(userDTO.LastName) && !string.Equals(user.LastName, userDTO.LastName))
            {
                user.LastName = userDTO.LastName;
            }

            if (!string.IsNullOrWhiteSpace(userDTO.EmailAddress) && !string.Equals(user.EmailAddress, userDTO.EmailAddress))
            {
                if (
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Any(_ => _.EmailAddress.Equals(userDTO.EmailAddress)))
                {
                    return ServiceResponseBuilder.Failure<User>("This email address is already in use.");
                }

                user.EmailAddress = userDTO.EmailAddress;

                EmailUtils.SendEmail(
                    new[] { userDTO.EmailAddress },
                    null,
                    "FindNDrive email change confirmation.",
                    string.Format(
                        "Hi {0}, this is just a short message to let you know that your email address was changed successfully.",
                        user.UserName),
                    false);
            }

            if ((userDTO.Gender == Gender.Female || userDTO.Gender == Gender.Male) && userDTO.Gender != user.Gender)
            {
                user.Gender = userDTO.Gender;
            }

            if (userDTO.DateOfBirth != DateTime.MinValue)
            {   
                user.DateOfBirth = userDTO.DateOfBirth;
            }

            if (!string.IsNullOrWhiteSpace(userDTO.PhoneNumber)
                && !string.Equals(user.PhoneNumber, userDTO.PhoneNumber))
            {
                user.PhoneNumber = userDTO.PhoneNumber;
            }
            
            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(user);
        }

        /// <summary>
        /// Retrievers current picture of the user whose id matches the one supplied.
        /// </summary>
        /// <param name="id">unique identifier of the user whose picture is to be retrieved.</param>
        /// <returns></returns>
        public Stream GetUserProfilePicture(int id)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return new MemoryStream(0);
            }

            var user =
                this.findNDriveUnitOfWork.ProfilePicturesRepository.Find(id);

            if (user == null)
            {
                return new MemoryStream(0);
            }

            if (WebOperationContext.Current != null)
            {
                WebOperationContext.Current.OutgoingResponse.ContentType = "image/png";
            }

            return new MemoryStream(user.ProfilePictureBytes);
        }

        /// <summary>
        /// Updates user's profile picture.
        /// </summary>
        /// <param name="profilePictureUpdaterDTO">
        /// The profile picture updater dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse UpdateProfilePicture(ProfilePictureUpdaterDTO profilePictureUpdaterDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            var user =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeChildren()
                    .FirstOrDefault(_ => _.UserId == profilePictureUpdaterDTO.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure("User with this id does not exist.");
            }

            var imageBytes = Convert.FromBase64String(profilePictureUpdaterDTO.Picture);
            user.ProfilePicture.ProfilePictureBytes = imageBytes;

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(true);
        }

        /// <summary>
        /// Updates user's privacy settings.
        /// </summary>
        /// <param name="dto">
        /// The dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> UpdatePrivacySettings(PrivacySettingsUpdaterDTO dto)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised<User>();
            }

            var user =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Include(_ => _.PrivacySettings)
                    .FirstOrDefault(_ => _.UserId == dto.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<User>("User with this id does not exist.");
            }

            user.PrivacySettings.EmailPrivacyLevel = dto.EmailPrivacyLevel;
            user.PrivacySettings.GenderPrivacyLevel = dto.GenderPrivacyLevel;
            user.PrivacySettings.DateOfBirthPrivacyLevel = dto.DateOfBirthPrivacyLevel;
            user.PrivacySettings.PhoneNumberPrivacyLevel = dto.PhoneNumberPrivacyLevel;
            user.PrivacySettings.RatingPrivacyLevel = dto.RatingPrivacyLevel;
            user.PrivacySettings.JourneysPrivacyLevel = dto.JourneysPrivacyLevel;

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(user);
        }

        /// <summary>
        /// Retrieves user by its id.
        /// </summary>
        /// <param name="dto">
        /// The dto - contains the id's of the retrieving user and the user to be retrieved. Id's of both users are required to determine the relationship between them.
        /// This is to ensure that the right level of privacy settings is detected and that only the allowed information is returned from the server.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<User> GetUser(UserRetrieverDTO dto)
        {
            var retrievingUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeChildren()
                    .FirstOrDefault(_ => _.UserId == dto.RetrievingUserId);

            if (retrievingUser == null)
            {
                return ServiceResponseBuilder.Failure<User>("User with this id does not exist.");
            }

            var targetUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Include(_ => _.Friends)
                    .Include(_ => _.PrivacySettings)
                    .FirstOrDefault(_ => _.UserId == dto.TargetUserId);

            if (targetUser == null)
            {
                return ServiceResponseBuilder.Failure<User>("User with this id does not exist.");
            }

            var friend = targetUser.Friends.Select(_ => _.UserId).Contains(retrievingUser.UserId);

            // To protect user's data, we must first analyse this user's privacy settings and return the relevant information based on those.
            return
                ServiceResponseBuilder.Success(
                    new User
                        {
                            UserId = targetUser.UserId,
                            UserName = targetUser.UserName,
                            FirstName = targetUser.FirstName,
                            LastName = targetUser.LastName,
                            LastLogon = targetUser.LastLogon,
                            MemberSince = targetUser.MemberSince,
                            AverageRating =
                                targetUser.PrivacySettings.RatingPrivacyLevel == PrivacyLevel.Everyone
                                    ? targetUser.AverageRating
                                    : targetUser.PrivacySettings.RatingPrivacyLevel == PrivacyLevel.FriendsOnly
                                      && friend
                                          ? targetUser.AverageRating
                                          : -1,
                            EmailAddress =
                                targetUser.PrivacySettings.EmailPrivacyLevel == PrivacyLevel.Everyone
                                    ? targetUser.EmailAddress
                                    : targetUser.PrivacySettings.RatingPrivacyLevel == PrivacyLevel.FriendsOnly
                                      && friend
                                          ? targetUser.EmailAddress
                                          : null,
                            Gender =
                                targetUser.PrivacySettings.GenderPrivacyLevel == PrivacyLevel.Everyone
                                    ? targetUser.Gender
                                    : targetUser.PrivacySettings.GenderPrivacyLevel == PrivacyLevel.FriendsOnly
                                      && friend
                                          ? targetUser.Gender
                                          : Gender.NotAvailable,
                            DateOfBirth =
                                targetUser.PrivacySettings.DateOfBirthPrivacyLevel == PrivacyLevel.Everyone
                                    ? targetUser.DateOfBirth
                                    : targetUser.PrivacySettings.DateOfBirthPrivacyLevel == PrivacyLevel.FriendsOnly
                                      && friend
                                          ? targetUser.DateOfBirth
                                          : null,
                            PhoneNumber =
                                targetUser.PrivacySettings.PhoneNumberPrivacyLevel == PrivacyLevel.Everyone
                                    ? targetUser.PhoneNumber
                                    : targetUser.PrivacySettings.PhoneNumberPrivacyLevel == PrivacyLevel.FriendsOnly
                                      && friend
                                          ? targetUser.PhoneNumber
                                          : null,
                            JourneysVisible =
                                targetUser.PrivacySettings.JourneysPrivacyLevel == PrivacyLevel.Everyone
                                || (targetUser.PrivacySettings.JourneysPrivacyLevel == PrivacyLevel.FriendsOnly
                                    && friend),
                        });
        }

        public ServiceResponse AdminLogin(LoginDTO loginDTO)
        {
            var validatedUser = ValidationHelper.Validate(loginDTO);

            // Verify user's username and password using the WebSecurity module.
            if (!validatedUser.IsValid || !WebSecurity.Login(loginDTO.UserName, loginDTO.Password))
            {
                return ServiceResponseBuilder.Failure("Invalid Username or Password.");
            }

            if (!System.Web.Security.Roles.GetRolesForUser(loginDTO.UserName).Contains("Administrators"))
            {
                return ServiceResponseBuilder.Failure("You are not an administrator.");
            }

            return ServiceResponseBuilder.Success();
        }   
    }
}

