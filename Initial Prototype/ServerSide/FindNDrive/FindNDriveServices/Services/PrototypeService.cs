using System.Collections.Generic;
using System.ServiceModel;
using DomainObjects;
using FindNDriveServices.Contracts;

namespace FindNDriveServices.Services
{
 [ServiceBehavior(
        InstanceContextMode = InstanceContextMode.Single,
        ConcurrencyMode = ConcurrencyMode.Multiple)]
    public class PrototypeService : IPrototypeService
    {   
        private readonly List<User> _users = new List<User>();
        
        public User GetUser()
        {   
            return new User
            {
                Id = 1,
                FirstName = "Michal",
                LastName = "Wasinski",
                Age = 22
            };
        }

        public void SaveUser(User user)
        {
            _users.Add(user);
        }

        public List<User> GetAllUsers()
        {
            return _users;
        }
    }
}

