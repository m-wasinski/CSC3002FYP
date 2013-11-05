using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.ServiceModel;
using System.Web.UI;
using DomainObjects;
using FindNDriveDataAccessLayer;
using FindNDriveServices.Contracts;
using FindNDriveServices.DTOs;
using FindNDriveServices.ServiceResponses;
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

     public ServiceResponse<User> RegisterUser(RegisterDTO register)
     {
         Debug.WriteLine("RegisterUser method called:  " + register.User.FirstName);
         throw new NotImplementedException();
         /*var user = new User()
         {
             Age = register.User.Age,
             FirstName = register.User.FirstName,
             LastName = register.User.LastName
         };
         // Register user
         //WebSecurity.CreateUserAndAccount(user.FirstName, user.LastName);
         //var newId = WebSecurity.GetUserId(user.FirstName);
         //user.Id = 2;

         // Add to DB set
         this._findNDriveUnitOfWork.UserRepository.Add(user);
         this._findNDriveUnitOfWork.Commit();
         User newUser = null;
         newUser = this._findNDriveUnitOfWork.UserRepository.Find(user.Id);
         Debug.WriteLine("New user Id:  " + newUser.Id);
         return new ServiceResponse<User>
         {
             Result = newUser,
             ServiceReponseCode = ServiceResponseCode.Success,
             ErrorMessages = null
         };*/
     }

     public ServiceResponse<User> GetUsers()
     {
         throw new System.NotImplementedException();
     }
    }
}

