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
    using System.Globalization;

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
                DeviceId = "Test",
                ExpiryDate = DateTime.Now.AddDays(14),
            };

            context.Sessions.AddOrUpdate(_ => _.UserId, session);
            WebSecurity.CreateUserAndAccount("Admin", "password");

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
            Calendar cal = new GregorianCalendar();

            var participant1 = new User
            {
                DateOfBirth = new DateTime(1992, 11, 15),
                EmailAddress = "participant1@domain.com",
                FirstName = "John",
                LastName = "Doe",
                Gender = Gender.Male,
                UserName = "john",
                Role = Roles.User
            };

            context.User.AddOrUpdate(_ => _.UserId, participant1);
            WebSecurity.CreateUserAndAccount(participant1.UserName, "password");

            var session1 = new Session()
            {
                UserId = participant1.UserId,
                SessionType = SessionTypes.Permanent,
                SessionId = SessionManager.GenerateNewSessionId(participant1.UserId),
                DeviceId = "Test",
                ExpiryDate = DateTime.Now.AddDays(14)
            };

            context.Sessions.AddOrUpdate(_ => _.UserId, session1);
            context.SaveChanges();


            var participant2 = new User
            {
                DateOfBirth = new DateTime(1985, 3, 2),
                EmailAddress = "participant2@domain.com",
                FirstName = "Laura",
                LastName = "McDonald",
                Gender = Gender.Female,
                UserName = "laura",
                Role = Roles.User,
            };

            context.User.AddOrUpdate(_ => _.UserId, participant2);
            WebSecurity.CreateUserAndAccount(participant2.UserName, "password");

            var session2 = new Session()
            {
                UserId = participant2.UserId,
                SessionType = SessionTypes.Permanent,
                SessionId = SessionManager.GenerateNewSessionId(participant2.UserId),
                DeviceId = "Test",
                ExpiryDate = DateTime.Now.AddDays(14),
            };

            context.Sessions.AddOrUpdate(_ => _.UserId, session2);
            context.SaveChanges();

            var driver = new User
            {
                DateOfBirth = new DateTime(1970, 5, 4),
                EmailAddress = "driver@domain.com",
                FirstName = "Jessica",
                LastName = "Patterson",
                Gender = Gender.Female,
                UserName = "jessica",
                Role = Roles.User,
            };


            context.User.AddOrUpdate(_ => _.UserId, driver);
            WebSecurity.CreateUserAndAccount(driver.UserName, "password");

            var session3 = new Session()
            {
                UserId = driver.UserId,
                SessionType = SessionTypes.Permanent,
                SessionId = SessionManager.GenerateNewSessionId(driver.UserId),
                DeviceId = "Test",
                ExpiryDate = DateTime.Now.AddDays(14),
            };

            context.Sessions.AddOrUpdate(_ => _.UserId, session3);

            context.SaveChanges();

            var carShare = new CarShare
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 2, 1, 20, 15, 0, cal),
                DepartureCity = "Lurgan",
                DestinationCity = "Dublin",
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                CarShareStatus = CarShareStatus.Upcoming
            };

            context.CarShares.AddOrUpdate(carShare);
            context.SaveChanges();

            var carShare2 = new CarShare
            {
                AvailableSeats = 2,
                DateAndTimeOfDeparture = new DateTime(2013, 11, 15, 15, 10, 0, cal),
                DepartureCity = "Belfast",
                DestinationCity = "Coleraine",
                Description = "Lift to Coleraine",
                Fee = 0.00,
                WomenOnly = false,
                Driver = participant1,
                Participants = new Collection<User>() {participant2},
                SmokersAllowed = false,
                CarShareStatus = CarShareStatus.Upcoming
            };

            context.CarShares.AddOrUpdate(carShare2);
            context.SaveChanges();

            var carShare3 = new CarShare
            {
                AvailableSeats = 1,
                DateAndTimeOfDeparture = new DateTime(2014, 1, 1, 20, 20, 0, cal),
                DepartureCity = "Belfast",
                DestinationCity = "Newcastle",
                Description = "Lift to Newcastle",
                Fee = 0.00,
                WomenOnly = false,
                Driver = participant1,
                SmokersAllowed = false,
                Participants = new Collection<User>() { participant2 },
                CarShareStatus = CarShareStatus.Upcoming
            };

            context.CarShares.AddOrUpdate(carShare3);
            context.SaveChanges();

            var carShare4 = new CarShare
            {
                AvailableSeats = 3,
                DateAndTimeOfDeparture = new DateTime(2014, 12, 1, 17, 30, 0, cal),
                DepartureCity = "Belfast",
                DestinationCity = "Portrush",
                Description = "Free lift to Portrush",
                Fee = 0.00,
                WomenOnly = false,
                Driver = participant1,
                SmokersAllowed = false,
                CarShareStatus = CarShareStatus.Upcoming
            };

            context.CarShares.AddOrUpdate(carShare4);
            context.SaveChanges();

            var carShare5 = new CarShare
            {
                AvailableSeats = 6,
                DateAndTimeOfDeparture = new DateTime(2014, 12, 5, 12, 30, 0, cal),
                DepartureCity = "Belfast",
                DestinationCity = "Dublin",
                Description = "Free lift to Dublin",
                Fee = 0.00,
                WomenOnly = false,
                Driver = participant1,
                SmokersAllowed = false,
                CarShareStatus = CarShareStatus.Upcoming
            };

            context.CarShares.AddOrUpdate(carShare5);
            context.SaveChanges();

            var carShare6 = new CarShare
            {
                AvailableSeats = 8,
                DateAndTimeOfDeparture = new DateTime(2013, 12, 1, 17, 30, 0, cal),
                DepartureCity = "Belfast",
                DestinationCity = "Dublin",
                Description = "Free lift to Dublin",
                Fee = 0.00,
                WomenOnly = false,
                Driver = participant1,
                SmokersAllowed = false,
                CarShareStatus = CarShareStatus.Upcoming
            };

            context.CarShares.AddOrUpdate(carShare6);
            context.SaveChanges();

            var carShare7 = new CarShare
            {
                AvailableSeats = 8,
                DateAndTimeOfDeparture = new DateTime(2013, 1, 1, 17, 30, 0, cal),
                DepartureCity = "Belfast",
                DestinationCity = "Dublin",
                Description = "Free lift to Dublin, women only.",
                Fee = 0.00,
                WomenOnly = true,
                Driver = participant1,
                SmokersAllowed = false,
                CarShareStatus = CarShareStatus.Upcoming
            };

            context.CarShares.AddOrUpdate(carShare7);
            context.SaveChanges();

            var carShare8 = new CarShare
            {
                AvailableSeats = 8,
                DateAndTimeOfDeparture = new DateTime(2014, 1, 1, 17, 30, 0, cal),
                DepartureCity = "London",
                DestinationCity = "Manchester",
                Description = "Free lift to Dublin, smokers allowed.",
                Fee = 0.00,
                WomenOnly = true,
                Driver = participant1,
                SmokersAllowed = true,
                CarShareStatus = CarShareStatus.Upcoming
            };

            context.CarShares.AddOrUpdate(carShare8);
            context.SaveChanges();

            var carShare9 = new CarShare
            {
                AvailableSeats = 8,
                DateAndTimeOfDeparture = new DateTime(2015, 1, 1, 17, 30, 0, cal),
                DepartureCity = "Belfast",
                DestinationCity = "Dublin",
                Description = "Lift to Dublin, fee applies.",
                Fee = 25.40,
                WomenOnly = false,
                Driver = participant1,
                SmokersAllowed = false,
                CarShareStatus = CarShareStatus.Upcoming
            };

            context.CarShares.AddOrUpdate(carShare9);
            context.SaveChanges();

            var carShare10 = new CarShare
            {
                AvailableSeats = 8,
                DateAndTimeOfDeparture = new DateTime(2015, 1, 1, 17, 30, 0, cal),
                DepartureCity = "Newcastle",
                DestinationCity = "Bangor",
                Description = "Lift to Bangor, fee applies.",
                Fee = 10.50,
                WomenOnly = false,
                Driver = participant1,
                SmokersAllowed = true,
                CarShareStatus = CarShareStatus.Upcoming
            };

            context.CarShares.AddOrUpdate(carShare10);
            context.SaveChanges();
        }
    }
}
