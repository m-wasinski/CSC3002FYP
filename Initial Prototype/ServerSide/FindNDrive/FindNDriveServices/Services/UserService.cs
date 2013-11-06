using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.ServiceModel;
using System.Web.UI;
using DomainObjects;
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

     public ServiceResponse<User> RegisterUser(RegisterDTO register)
     {
         Debug.WriteLine("RegisterUser method called:  " + register.User.FirstName + " " + register.User.LastName + " " + register.User.Gender + " " + register.User.DateOfBirth);

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
         Debug.WriteLine("New user Id:  " + newUser.Id);*/
         

         /*var testDriver = new User
         {
             FirstName = "Aleksandra",
             LastName = "Szczypior",
             EmailAddress = "alex1710@vp.pl",
             Gender = Gender.Female,
         };

         var testCarShare = new CarShare()
         {
             DateOfDeparture = new DateTime(DateTime.Today.Year, DateTime.Today.Month, DateTime.Today.Day),
             DepartureCity = "Belfast",
             Description = "Test Car Share",
             DestinationCity = "Lurgan",
             Driver = testDriver,
             Fee = 0.00,
             AvailableSeats = 4,
             Participants = new List<User>(),
             SmokersAllowed = false,
             WomenOnly = false,
         };*/

         //testDriver.CarShares.Add(testCarShare);
         
         return new ServiceResponse<User>
         {
             Result = _findNDriveUnitOfWork.UserRepository.AsQueryable().First(_ => _.Id == 1),
             ServiceReponseCode = ServiceResponseCode.Success,
             ErrorMessages = new List<string>() {"testerror"}
         };
     }

     public ServiceResponse<User> GetUsers()
     {
         throw new System.NotImplementedException();
     }
    }
}

