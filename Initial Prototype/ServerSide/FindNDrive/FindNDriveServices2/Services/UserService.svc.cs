using System;
using System.Diagnostics;
using System.ServiceModel;
using System.ServiceModel.Activation;
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

namespace FindNDriveServices2.Services
{
    [ServiceBehavior(
           InstanceContextMode = InstanceContextMode.Single,
           ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Allowed)]
    
    public class UserService : IUserService
    {

        /// <summary>
        /// The scrum unit of work, which provides access to the required Repositories, and exposes
        /// a commit method to complete the unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork _findNDriveUnitOfWork;

        public UserService(FindNDriveUnitOfWork findNDriveUnitOf)
        {
            _findNDriveUnitOfWork = findNDriveUnitOf;
        }

        public UserService()
        {
            var testDbContext = new ApplicationContext();
            var userEntityFrameworkRepository = new EntityFrameworkRepository<User>(testDbContext);
            //var carShareEntityFrameworkRepository = new EntityFrameworkRepository<CarShare>(testDbContext);
            _findNDriveUnitOfWork = new FindNDriveUnitOfWork(testDbContext, userEntityFrameworkRepository, null);
        }
        /// <summary>
        /// Logs a user in.
        /// </summary>
        /// <param name="login"></param>
        /// <returns></returns>
        public ServiceResponse<User> LoginUser(LoginDTO login)
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
                ServiceReponseCode = (loggedInUser == null) ? ServiceResponseCode.Failure : ServiceResponseCode.Success,
                ErrorMessages = validatedUser.ErrorMessages
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
                ServiceReponseCode = validatedRegisterDTO.IsValid? ServiceResponseCode.Success: ServiceResponseCode.Failure,
                ErrorMessages = validatedRegisterDTO.ErrorMessages
            };
        }

        public ServiceResponse<User> GetUsers()
        {
            throw new NotImplementedException();
        }
    }
    
}

