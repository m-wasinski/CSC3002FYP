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
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Data.Entity.Migrations;
    using System.Drawing;
    using System.Globalization;
    using System.IO;
    using System.Security.Cryptography;
    using System.Text;

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
            this.AddMessages(context);
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
                GCMRegistrationID = "0",
                Status = Status.Offline,
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
                GCMRegistrationID = "0",
                Status = Status.Offline,
                Role = Roles.User,
                UserId = 2,
                MemberSince = DateTime.Now,
                AverageRating = 0
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
            Image img = Image.FromFile(@"C:\\CSC3002FYP\\user.png");
            byte[] arr;
            using (MemoryStream ms = new MemoryStream())
            {
                img.Save(ms, System.Drawing.Imaging.ImageFormat.Jpeg);
                arr =  ms.ToArray();
            }
            var participant2 = new User
            {
                DateOfBirth = new DateTime(1985, 3, 2),
                EmailAddress = "participant2@domain.com",
                FirstName = "Laura",
                LastName = "McDonald",
                Gender = Gender.Female,
                UserName = "laura",
                GCMRegistrationID = "0",
                Status = Status.Offline,
                Role = Roles.User,
                UserId = 3,
                ProfileImage = arr
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
                EmailAddress = "alex@domain.com",
                FirstName = "Alex",
                LastName = "Johnson",
                GCMRegistrationID = "0",
                Status = Status.Offline,
                Gender = Gender.Female,
                UserName = "alex",
                Role = Roles.User,
                UserId = 4
            };

            context.SaveChanges();

            WebSecurity.CreateUserAndAccount(driver.UserName, "password");

            var encoding = new UTF8Encoding();
            var bytes = encoding.GetBytes("Test1");

            var sha = new SHA1CryptoServiceProvider();
            var hash = sha.ComputeHash(bytes);
            

            var session3 = new Session()
            {
                UserId = driver.UserId,
                SessionType = SessionTypes.Permanent,
                SessionId = "4:Test1",
                DeviceId = Convert.ToBase64String(hash),
                Uuid = "Test1",
                ExpiryDate = DateTime.Now.AddDays(14),
            };

            context.Sessions.AddOrUpdate(_ => _.UserId, session3);
             
            context.SaveChanges();

            var geoAddress1 = new GeoAddress { AddressLine = "Dublin City Centre", Latitude = 53.3478, Longitude = -6.2597, Order = 0};
            var geoAddress2 = new GeoAddress { AddressLine = "Warrenpoint City Centre", Latitude = 54.09900, Longitude = -6.24900, Order = 1 };
            var geoAddress3 = new GeoAddress { AddressLine = "Belfast City Centre", Latitude = 54.5970, Longitude = -5.9300, Order = 2};

            for(int i = 0; i < 1; i++)
            {
                context.Journeys.AddOrUpdate(
                    new Journey
                        {
                            AvailableSeats = 4,
                            DateAndTimeOfDeparture = DateTime.Now.AddDays(i + 1),
                            GeoAddresses = new List<GeoAddress> { geoAddress1, geoAddress3 },
                            Description = "Free ride to Dublin!",
                            Fee = 0.00,
                            Driver = driver,
                            SmokersAllowed = false,
                            JourneyStatus = JourneyStatus.OK,
                            CreationDate = DateTime.Now,
                            PreferredPaymentMethod = string.Empty,
                            Participants = new Collection<User> { participant1, participant2 }
                        });
            }

            return; 

            var geoAddress4 = new GeoAddress { AddressLine = "London City Centre", Latitude = 51.5072, Longitude = 0.12755, Order = 4 };
            var geoAddress5 = new GeoAddress { AddressLine = "Manchester City Centre", Latitude = 53.4667, Longitude = -2.247926, Order = 5 };

            for (int i = 0; i < 20; i++)
            {
                context.Journeys.AddOrUpdate(
                    new Journey
                    {
                        AvailableSeats = 4,
                        DateAndTimeOfDeparture = DateTime.Now.AddDays(i + 1),
                        GeoAddresses = new List<GeoAddress> { geoAddress4, geoAddress5},
                        Description = "London to Manchester",
                        Fee = 0.00,
                        Driver = driver,
                        SmokersAllowed = false,
                        JourneyStatus = JourneyStatus.OK,
                        CreationDate = DateTime.Now,
                        Participants = new Collection<User> { participant1, participant2 }
                    });
            }



            for (int i = 0; i < 20; i++)
            {
                context.Journeys.AddOrUpdate(
                    new Journey
                    {
                        AvailableSeats = 4,
                        DateAndTimeOfDeparture = new DateTime(2014, 2, 1, 20, 15, 0, cal),
                        GeoAddresses = new List<GeoAddress> { geoAddress1, geoAddress2, geoAddress3, geoAddress4, geoAddress5 },
                        Description = "Ultimate trip!",
                        Fee = 0.00,
                        Driver = driver,
                        SmokersAllowed = false,
                        JourneyStatus = JourneyStatus.OK,
                        CreationDate = new DateTime(2014, 1, 1, 20, 15, 0, cal),
                        Participants = new Collection<User> { participant1, participant2 }
                    });
            }

            context.SaveChanges();
        }

        private void AddMessages(ApplicationContext context)
        {
            /*for (int i = 1; i <= 500; i++)
            {
                context.ChatMessages.Add(
                    new ChatMessage()
                        {
                            ChatMessageId = i,
                            Read = false,
                            MessageBody = "Test message: " + i,
                            SenderId = 2,
                            RecipientId = 4,
                            RecipientUserName = "jess",
                            SentOnDate = DateTime.Now,
                            SenderUserName = "john"
                        });
            }

            context.SaveChanges(); */
        }

        /// <summary>
        /// The add notifications.
        /// </summary>
        /// <param name="context">
        /// The context.
        /// </param>
        private void AddNotifications(ApplicationContext context)
        {
            /*for (int i = 0; i < 100; i++)
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

            context.SaveChanges();*/
        }
    }
}
