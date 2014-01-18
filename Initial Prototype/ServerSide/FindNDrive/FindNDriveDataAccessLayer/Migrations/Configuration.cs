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
            AddJourneysAndPassengers(context);
            AddNotifications(context);
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
        private static void AddJourneysAndPassengers(ApplicationContext context)
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
            WebSecurity.CreateUserAndAccount(participant1.UserName, "p");

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
            WebSecurity.CreateUserAndAccount(participant2.UserName, "p");

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
                EmailAddress = "jessica@domain.com",
                FirstName = "Jessica",
                LastName = "Patterson",
                Gender = Gender.Female,
                UserName = "jess",
                Role = Roles.User,
                Friends = new Collection<User>{participant1, participant2}
            };

            context.User.AddOrUpdate(_ => _.UserId, driver);
            context.SaveChanges();

            participant1.Friends = new Collection<User>(){driver};
            context.User.AddOrUpdate(participant1);
            context.SaveChanges();

            WebSecurity.CreateUserAndAccount(driver.UserName, "p");

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

            var journey1 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 2, 1, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 1, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey1);
            context.SaveChanges();

            var journey2 = new Journey
            {
                AvailableSeats = 1,
                DateAndTimeOfDeparture = new DateTime(2014, 6, 6, 15, 30, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "London",
                    Latitude = 51.5072,
                    Longitude = -0.1275
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Manchester",
                    Latitude = 53.4667,
                    Longitude = -2.2333
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 2, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey2);
            context.SaveChanges();

            var journey3 = new Journey
            {
                AvailableSeats = 2,
                DateAndTimeOfDeparture = new DateTime(2014, 3, 1, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 3, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey3);
            context.SaveChanges();

            var journey4 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 4, 1, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 4, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey4);
            context.SaveChanges();

            var journey5 = new Journey
            {
                AvailableSeats = 7,
                DateAndTimeOfDeparture = new DateTime(2014, 5, 1, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 5, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey5);
            context.SaveChanges();

            var journey6 = new Journey
            {
                AvailableSeats = 1,
                DateAndTimeOfDeparture = new DateTime(2014, 6, 1, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 6, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey6);
            context.SaveChanges();

            var journey7 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 7, 1, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 7, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey7);
            context.SaveChanges();

            var journey8 = new Journey
            {
                AvailableSeats = 10,
                DateAndTimeOfDeparture = new DateTime(2014, 8, 1, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 8, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey8);
            context.SaveChanges();

            var journey9 = new Journey
            {
                AvailableSeats = 2,
                DateAndTimeOfDeparture = new DateTime(2014, 9, 1, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 9, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey9);
            context.SaveChanges();

            var journey10 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 10, 1, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 10, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey10);
            context.SaveChanges();

            var journey11 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 2, 3, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 11, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey11);
            context.SaveChanges();

            var journey12 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 4, 5, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 12, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey12);
            context.SaveChanges();

            var journey13 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 5, 2, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 13, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey13);
            context.SaveChanges();

            var journey14 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 5, 2, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 14, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey14);
            context.SaveChanges();

            var journey15 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 2, 1, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 15, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey15);
            context.SaveChanges();

            var journey16 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 7, 7, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 16, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey16);
            context.SaveChanges();

            var journey17 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 4, 10, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 17, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey17);
            context.SaveChanges();

            var journey18 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 5, 2, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 18, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey18);
            context.SaveChanges();

            var journey19 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 11, 11, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 19, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey19);
            context.SaveChanges();

            var journey20 = new Journey
            {
                AvailableSeats = 4,
                DateAndTimeOfDeparture = new DateTime(2014, 10, 10, 20, 15, 0, cal),
                DepartureAddress = new GeoAddress()
                {
                    AddressLine = "Belfast",
                    Latitude = 54.5970,
                    Longitude = -5.9300
                },
                DestinationAddress = new GeoAddress()
                {
                    AddressLine = "Dublin",
                    Latitude = 53.3478,
                    Longitude = -6.2597
                },
                Description = "Free ride to Dublin!",
                Fee = 0.00,
                WomenOnly = false,
                Driver = driver,
                SmokersAllowed = false,
                JourneyStatus = JourneyStatus.Upcoming,
                CreationDate = new DateTime(2014, 1, 20, 20, 15, 0, cal),
            };

            context.Journeys.AddOrUpdate(journey20);
            context.SaveChanges(); 
        }

        /// <summary>
        /// The add notifications.
        /// </summary>
        /// <param name="context">
        /// The context.
        /// </param>
        private void AddNotifications(ApplicationContext context)
        {
            for (int i = 0; i < 100; i++)
            {
                context.Notifications.AddOrUpdate(
                    new Notification()
                        {
                            UserId = 4,
                            NotificationBody = "Test Notification no: " + i,
                            Context = NotificationContext.Neutral,
                            Read = false,
                            ReceivedOnDate = DateTime.Now
                        });
            }

            context.SaveChanges();
        }
    }
}
