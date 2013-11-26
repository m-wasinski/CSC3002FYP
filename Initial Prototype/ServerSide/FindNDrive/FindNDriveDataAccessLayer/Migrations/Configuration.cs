// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Configuration.cs" company="">
//   
// </copyright>
// <summary>
//   The configuration.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveDataAccessLayer.Migrations
{
    using System;
    using System.Collections.ObjectModel;
    using System.Data.Entity.Migrations;

    using DomainObjects.Constants;
    using DomainObjects.DOmains;
    using DomainObjects.Domains;

    using FindNDriveServices2;

    using WebMatrix.WebData;

    /// <summary>
    /// The configuration.
    /// </summary>
    internal sealed class Configuration : DbMigrationsConfiguration<ApplicationContext>
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="Configuration"/> class.
        /// </summary>
        public Configuration()
        {
            AutomaticMigrationsEnabled = true;
        }

        /// <summary>
        /// The seed.
        /// </summary>
        /// <param name="context">
        /// The context.
        /// </param>
        protected override void Seed(ApplicationContext context)
        {
            // This method will be called after migrating to the latest version.
            if (!WebSecurity.Initialized)
            {
                WebSecurity.InitializeDatabaseConnection("FindNDriveConnectionString", "User", "Id", "UserName", true); 
            }

            AddAdministrator(context);
            AddCarShareAndParticipants(context);
        }

        /// <summary>
        /// The add administrator.
        /// </summary>
        /// <param name="context">
        /// The context.
        /// </param>
        private static void AddAdministrator(ApplicationContext context)
        {   

            var administrator = new User
            {
                DateOfBirth = new DateTime(1990, 11, 11),
                EmailAddress = "wasinskimichal@gmail.com",
                FirstName = "Michal",
                LastName = "Wasinski",
                Gender = Gender.Male,
                UserName = "Admin",
                Role = Roles.Administrator,
                UserId = 1
            };

            context.User.AddOrUpdate(_ => _.UserId, administrator);

            var session = new Session()
            {
                UserId = administrator.UserId,
                SessionType = SessionTypes.Permanent,
                SessionId = SessionManager.GenerateNewSessionId(administrator.UserId),
                LastKnownId = "Test",
                ExpiresOn = DateTime.Now.AddDays(14),
            };

            context.Sessions.AddOrUpdate(_ => _.UserId, session);
            WebSecurity.CreateUserAndAccount("Administrator", "AdminPassword1234");

            context.SaveChanges();
        }

        /// <summary>
        /// The add car share.
        /// </summary>
        /// <param name="context">
        /// The context.
        /// </param>
        private static void AddCarShareAndParticipants(ApplicationContext context)
        {
            var participant1 = new User
            {
                DateOfBirth = new DateTime(1992, 11, 15),
                EmailAddress = "participant1@domain.com",
                FirstName = "John",
                LastName = "Doe",
                Gender = Gender.Male,
                UserName = "CrazyJohn",
                Role = Roles.User
            };

            context.User.AddOrUpdate(_ => _.UserId, participant1);
            WebSecurity.CreateUserAndAccount(participant1.UserName, "password");
            context.SaveChanges();
            var participant2 = new User
            {
                DateOfBirth = new DateTime(1985, 3, 2),
                EmailAddress = "participant2@domain.com",
                FirstName = "Laura",
                LastName = "McDonald",
                Gender = Gender.Female,
                UserName = "Laura",
                Role = Roles.User,
            };

            context.User.AddOrUpdate(_ => _.UserId, participant2);
            WebSecurity.CreateUserAndAccount(participant2.UserName, "password");
            context.SaveChanges();
            var driver = new User
            {
                DateOfBirth = new DateTime(1970, 5, 4),
                EmailAddress = "driver@domain.com",
                FirstName = "Jessica",
                LastName = "Patterson",
                Gender = Gender.Female,
                UserName = "TheDriver",
                Role = Roles.User,
            };


            context.User.AddOrUpdate(_ => _.UserId, driver);
            WebSecurity.CreateUserAndAccount(driver.UserName, "password");
            context.SaveChanges();
            var carShare = new CarShare
            {
                AvailableSeats = 4,
                DateOfDeparture = DateTime.Now,
                DepartureCity = "Belfast",
                DestinationCity = "Dublin",
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Participants = new Collection<User>{ participant1, participant2 },
                Driver = driver,
                SmokersAllowed = false
            };

            context.CarShares.AddOrUpdate(carShare);
            context.SaveChanges();
        }
    }
}
