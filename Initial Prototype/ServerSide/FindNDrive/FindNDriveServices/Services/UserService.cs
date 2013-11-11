using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.ServiceModel;
using System.Web.UI;
using DomainObjects;
using DomainObjects.Domains;
using DomainObjects.DOmains;
using DomainObjects.Enums;
using FindNDriveDataAccessLayer;
using FindNDriveServices.Contracts;
using FindNDriveServices.DTOs;
using FindNDriveServices.ServiceResponses;
using Newtonsoft.Json;
using WebMatrix.WebData;

namespace FindNDriveServices.Services
{
 [ServiceBehavior(
        InstanceContextMode = InstanceContextMode.Single,
        ConcurrencyMode = ConcurrencyMode.Multiple)]
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
            
     }
     public ServiceResponse<User> LoginUser(LoginDTO login)
     {     
         throw new NotFiniteNumberException();
         Debug.WriteLine("LoginUser method called: " + login.Password);
         /*return new ServiceResponse<User>
         {
             Result = new User
             {
                 Id = 1,
                 FirstName = "Aleksandra",
                 LastName = "Szczypior",
                 Age = 20
             }, 
              ServiceReponseCode = (login.Password == "testpassword") ? ServiceResponseCode.Success : ServiceResponseCode.Failure, 
             ErrorMessages = null
         };*/
     }

     /// <summary>
     /// Registers a new user.
     /// </summary>
     /// <param name="register"></param>
     /// <returns></returns>
     public ServiceResponse<User> RegisterUser(RegisterDTO register)
     {
         //Debug.WriteLine("RegisterUser method called:  " + register.User.FirstName + " " + register.User.LastName + " " + register.User.Gender + " " + register.User.DateOfBirth);

         WebSecurity.CreateUserAndAccount(register.User.EmailAddress, register.Password);
         var newUser = register.User;
         _findNDriveUnitOfWork.UserRepository.Add(newUser);
         _findNDriveUnitOfWork.Commit();

         return new ServiceResponse<User>
         {
             Result = newUser,
             ServiceReponseCode = ServiceResponseCode.Success
         };
     }

     public ServiceResponse<User> GetUsers()
     {
         throw new System.NotImplementedException();
     }
    }
}

