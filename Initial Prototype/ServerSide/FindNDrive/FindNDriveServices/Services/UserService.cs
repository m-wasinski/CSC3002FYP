using System.Collections.Generic;
using System.Diagnostics;
using System.ServiceModel;
using DomainObjects;
using FindNDriveServices.Contracts;
using FindNDriveServices.DTOs;
using FindNDriveServices.ServiceResponses;

namespace FindNDriveServices.Services
{
 [ServiceBehavior(
        InstanceContextMode = InstanceContextMode.Single,
        ConcurrencyMode = ConcurrencyMode.Multiple)]
    public class UserService : IUserService
    {

     public ServiceResponse<User> LoginUser(LoginDTO login)
     {     

         Debug.WriteLine("Service Called! " + login.Password);
         ServiceResponse<User> response =  new ServiceResponse<User>
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
         };

         return response;
     }

     public ServiceResponse<User> RegisterUser()
     {
         throw new System.NotImplementedException();
     }

     public ServiceResponse<User> GetUsers()
     {
         throw new System.NotImplementedException();
     }
    }
}

