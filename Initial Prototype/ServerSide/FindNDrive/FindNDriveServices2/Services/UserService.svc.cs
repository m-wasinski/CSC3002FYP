using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Security.Cryptography;
using System.Security.Policy;
using System.ServiceModel;
using System.ServiceModel.Activation;
using System.ServiceModel.Channels;
using System.ServiceModel.Configuration;
using System.ServiceModel.Web;
using System.Text;
using System.Threading;
using System.Web;
using System.Web.Security;
using DomainObjects.Constants;
using DomainObjects.DOmains;
using DomainObjects.Domains;
using FindNDriveDataAccessLayer;
using FindNDriveInfrastructureCore;
using FindNDriveInfrastructureDataAccessLayer;
using FindNDriveServices2.Contracts;
using FindNDriveServices2.DTOs;
using FindNDriveServices2.ServiceResponses;
using WebMatrix.WebData;
using Roles = DomainObjects.Constants.Roles;

namespace FindNDriveServices2.Services
{
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

        public UserService(FindNDriveUnitOfWork findNDriveUnitOf)
        {
            _findNDriveUnitOfWork = findNDriveUnitOf;
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

            if (!WebSecurity.Initialized)
            {
                WebSecurity.InitializeDatabaseConnection("FindNDriveConnectionString", "User", "Id", "UserName", true);
            }

            User loggedInUser = null;

            var validatedUser = ValidationHelper.Validate(login);
       
            // Need to change validated user isValid method as extra validation in this class....
            if (login.UserName == null || login.Password == null || !WebSecurity.Login(login.UserName, login.Password))
            {
                validatedUser.ErrorMessages.Add("Invalid Username or Password.");
                validatedUser.IsValid = false;
            }
            else
            {   
                var proxy = _findNDriveUnitOfWork.UserRepository.Find(WebSecurity.GetUserId(login.UserName));
                //Retrieve headers from the incoming request.
                var rememberMe = WebOperationContext.Current.IncomingRequest.Headers["RememberMe"];
                var currentId = WebOperationContext.Current.IncomingRequest.Headers["DeviceID"];

                //generate a new random hash.
                var hashedSessionId = SessionHelper.GenerateNewSessionId();
                var hashedDeviceId = SessionHelper.EncryptValue(currentId);
                //set expiration date for the above token, initialy to 30 minutes.
                var validUntil = DateTime.Now.AddMinutes(30);
                if (WebOperationContext.Current != null)
                {
                    var rememberUser = WebOperationContext.Current.IncomingRequest.Headers["RememberMe"];

                    if (rememberMe != null)
                    {
                        if (rememberUser.Equals("true"))
                        {
                            //make the token expire in two weeks.
                            validUntil = DateTime.Now.AddDays(14);
                        }

                        var currentSession = _findNDriveUnitOfWork.SessionRepository.Find(proxy.Id);
                        
                        

                        var newSession = new Session()
                        {
                            LastKnownId = currentId, 
                            SessionExpirationDate = validUntil, 
                            SessionType = SessionTypes.Permanent,
                            Token = hashedSessionId, 
                            UserId = proxy.Id
                        };

                        if (currentSession != null)
                        {
                            currentSession.Token = hashedSessionId;
                            currentSession.LastKnownId = hashedDeviceId;
                            currentSession.SessionExpirationDate = validUntil;
                            _findNDriveUnitOfWork.SessionRepository.Update(currentSession);
                        }
                        else
                            _findNDriveUnitOfWork.SessionRepository.Add(newSession);

                        _findNDriveUnitOfWork.Commit();
                        WebOperationContext.Current.OutgoingResponse.Headers.Add("SessionId", hashedSessionId);
                    }

                }

                loggedInUser = new User()
                {
                    FirstName = proxy.FirstName,
                    LastName = proxy.LastName,
                    DateOfBirth = proxy.DateOfBirth,
                    EmailAddress = proxy.EmailAddress,
                    Gender = proxy.Gender,
                    Role = Roles.User,
                    UserName = proxy.UserName,
                    Id = proxy.Id
                };
            }

            
            return new ServiceResponse<User>
            {
                Result = loggedInUser,
                ServiceResponseCode = (loggedInUser == null) ? ServiceResponseCode.Failure : ServiceResponseCode.Success,
                ErrorMessages = validatedUser.ErrorMessages
            };
        }

        public ServiceResponse<User> AutoUserLogin()
        {
            var token = WebOperationContext.Current.IncomingRequest.Headers["Token"];
            var currentId = WebOperationContext.Current.IncomingRequest.Headers["DeviceID"];
            var userId = WebOperationContext.Current.IncomingRequest.Headers["UserId"];

            var currentSession = _findNDriveUnitOfWork.SessionRepository.Find(int.Parse(userId));
            var credentialsValid = SessionHelper.ValidateSession(token, currentId, currentSession);

            User loggedInUser = null;

            if (credentialsValid)
            {
                var proxy = _findNDriveUnitOfWork.UserRepository.Find(int.Parse(userId));
                loggedInUser = new User()
                {
                    FirstName = proxy.FirstName,
                    LastName = proxy.LastName,
                    DateOfBirth = proxy.DateOfBirth,
                    EmailAddress = proxy.EmailAddress,
                    Gender = proxy.Gender,
                    Role = Roles.User,
                    UserName = proxy.UserName,
                    Id = proxy.Id
                };
            }

            return new ServiceResponse<User>
            {
                Result = loggedInUser,
                ServiceResponseCode = (loggedInUser == null) ? ServiceResponseCode.Failure : ServiceResponseCode.Success,
                ErrorMessages = new List<string>() { SessionHelper.EncryptValue(currentId) }
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
            if (!WebSecurity.Initialized)
            {
                WebSecurity.InitializeDatabaseConnection("FindNDriveConnectionString", "User", "Id", "UserName", true);
            }

            var validatedRegisterDTO = ValidationHelper.Validate(register);

            if (validatedRegisterDTO.IsValid)
            {
                WebSecurity.CreateUserAndAccount(register.User.UserName, register.Password);
                register.User.Id = WebSecurity.GetUserId(register.User.UserName);

                newUser = new User()
                {
                    FirstName = register.User.FirstName,
                    LastName = register.User.LastName,
                    DateOfBirth = register.User.DateOfBirth,
                    EmailAddress = register.User.EmailAddress,
                    Gender = register.User.Gender,
                    Role = Roles.User,
                    UserName = register.User.UserName,
                    Id = register.User.Id
                };

                _findNDriveUnitOfWork.UserRepository.Add(newUser);

                _findNDriveUnitOfWork.Commit();
            }

            return new ServiceResponse<User>
            {
                Result = newUser,
                ServiceResponseCode = validatedRegisterDTO.IsValid? ServiceResponseCode.Success: ServiceResponseCode.Failure,
                ErrorMessages = validatedRegisterDTO.ErrorMessages
            };
        }

        public ServiceResponse<User> GetUsers()
        {
            throw new NotImplementedException();
        }

        public ServiceResponse<User> TestAuthentication(UserDTO userDTO)
        {
            if (WebOperationContext.Current != null)
            {
                var securityToken = WebOperationContext.Current.IncomingRequest.Headers["SessionId"];
            }

            return new ServiceResponse<User>()
            {
                Result = null,
                ServiceResponseCode = WebSecurity.IsAuthenticated? ServiceResponseCode.Success : ServiceResponseCode.Failure
            };
        }
    }
    
}

