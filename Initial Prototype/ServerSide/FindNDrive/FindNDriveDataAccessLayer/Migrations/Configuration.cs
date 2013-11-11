using System;
using DomainObjects.Constants;
using DomainObjects.DOmains;
using WebMatrix.WebData;

namespace FindNDriveDataAccessLayer.Migrations
{
    using System.Data.Entity.Migrations;

    internal sealed class Configuration : DbMigrationsConfiguration<ApplicationContext>
    {
        public Configuration()
        {
            AutomaticMigrationsEnabled = true;
        }

        protected override void Seed(ApplicationContext context)
        {
            //  This method will be called after migrating to the latest version.
            if (!WebSecurity.Initialized)
            {
                WebSecurity.InitializeDatabaseConnection("FindNDriveConnectionString", "User", "Id", "UserName", true); 
            }

            AddAdministrator(context);
        }

        private static void AddAdministrator(ApplicationContext context)
        {
            var administrator = new User()
            {
                DateOfBirth = new DateTime(1990, 11, 11),
                EmailAddress = "wasinskimichal@gmail.com",
                FirstName = "Michal",
                LastName = "Wasinski",
                Gender = Gender.Male,
                UserName = "Admin",
                Role = Roles.Administrator,
                Id = 1
            };

            context.User.AddOrUpdate(_ => _.Id, administrator);
          
            WebSecurity.CreateUserAndAccount("Administrator", "AdminPassword1234");
        }
    }
}
