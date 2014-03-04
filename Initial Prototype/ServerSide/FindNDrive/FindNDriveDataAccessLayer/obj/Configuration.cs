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
    using System.Data.Entity;
    using System.Data.Entity.Migrations;
    using System.Drawing;
    using System.Globalization;
    using System.IO;
    using System.Security.Cryptography;
    using System.Text;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveServices2.ServiceUtils;

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
            
            // Prepare the default profile picture.
            var img = Image.FromFile(@"C:\\CSC3002FYP\\Initial Prototype\\Resources\\default_picture.png");
            byte[] arr;

            using (var ms = new MemoryStream())
            {
                img.Save(ms, System.Drawing.Imaging.ImageFormat.Jpeg);
                arr = ms.ToArray();
            }

            // Initialise the WebSecurity module.
            if (!WebSecurity.Initialized)
            {
                WebSecurity.InitializeDatabaseConnection("FindNDriveConnectionString", "User", "Id", "UserName", true); 
            }

            // Create the administrator role.
            if (!System.Web.Security.Roles.RoleExists("Administrators"))
            {
                System.Web.Security.Roles.CreateRole("Administrators");
            }

            this.AddAdministrator(context, arr);
            this.AddJourneysAndPassengers(context, arr);
            this.AddNotifications(context);
        }

        /// <summary>
        /// The add administrator.
        /// </summary>
        /// <param name="context">
        /// The context.
        /// </param>
        /// <param name="arr">
        /// The arr.
        /// </param>
        private void AddAdministrator(ApplicationContext context, byte[] arr)
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
                UserId = 1,
                ProfilePicture = new ProfilePicture
                {
                    ProfilePictureBytes = arr,
                    ProfilePictureId = 1
                }
            };

            context.User.AddOrUpdate(_ => _.UserId, administrator);

            var session = new Session()
            {
                UserId = administrator.UserId,
                SessionType = SessionTypes.Permanent,
                SessionId = SessionManager.GenerateNewSessionId(administrator.UserId),
                DeviceId = "Test",
                ExpiryDate = DateTime.Now.AddDays(999),
            };

            context.Sessions.AddOrUpdate(_ => _.UserId, session);

            WebSecurity.CreateUserAndAccount("Admin", "password");

            System.Web.Security.Roles.AddUserToRole("Admin", "Administrators");

            context.SaveChanges();
        }

        /// <summary>
        /// The add journeys and passengers.
        /// </summary>
        /// <param name="context">
        /// The context.
        /// </param>
        /// <param name="arr">
        /// The arr.
        /// </param>
        private void AddJourneysAndPassengers(ApplicationContext context, byte[] arr)
        {
            var cal = new GregorianCalendar();

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
                UserId = 2,
                MemberSince = DateTime.Now,
                AverageRating = 0,
                ProfilePicture = new ProfilePicture
                                     {
                                         ProfilePictureBytes = arr,
                                         ProfilePictureId = 2
                                     }
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
                GCMRegistrationID = "0",
                Status = Status.Offline,
                UserId = 3,
                ProfilePicture = new ProfilePicture
                {
                    ProfilePictureBytes = arr,
                    ProfilePictureId = 3
                }
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
                UserId = 4,
                ProfilePicture = new ProfilePicture
                {
                    ProfilePictureBytes = arr,
                    ProfilePictureId = 4
                }
            };

            context.SaveChanges();

            WebSecurity.CreateUserAndAccount(driver.UserName, "p");

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

            for (var i = 0; i < 5; i++)
            {
                context.Journeys.AddOrUpdate(
                    new Journey
                        {
                            AvailableSeats = i + 1,
                            DateAndTimeOfDeparture = DateTime.Now.AddDays(i + 1),
                            GeoAddresses = new List<GeoAddress> { geoAddress1, geoAddress2, geoAddress3 },
                            Description = "Free ride to Dublin!",
                            Fee = 0.00,
                            Driver = driver,
                            SmokersAllowed = false,
                            JourneyStatus = JourneyStatus.OK,
                            CreationDate = DateTime.Now.Subtract(TimeSpan.FromDays(i)),
                            PreferredPaymentMethod = string.Empty,
                            Participants = new Collection<User> { participant1, participant2 }
                        });
            }

            var geoAddress4 = new GeoAddress { AddressLine = "London City Centre", Latitude = 51.5072, Longitude = 0.12755, Order = 4 };
            var geoAddress5 = new GeoAddress { AddressLine = "Manchester City Centre", Latitude = 53.4667, Longitude = -2.247926, Order = 5 };

            for (var i = 0; i < 8; i++)
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
                        PreferredPaymentMethod = string.Empty,
                        Participants = new Collection<User> { participant1, participant2 }
                    });
            }

            context.SaveChanges();
        }

        private void AddNotifications(ApplicationContext context)
        {
            for (var i = 0; i < 100; i++)
            {
                context.Notifications.AddOrUpdate(
                    new Notification()
                        {
                            UserId = 4,
                            ProfilePictureId = 4,
                            NotificationType = NotificationType.App,
                            ReceivedOnDate = DateTime.Now,
                            NotificationMessage =
                                string.Format("This is a test notification no: {0}", i + 1),
                            Delivered = true,
                            TargetObjectId = -1,
                            NotificationTitle = "Test notification",
                            CollapsibleKey = -1,
                            NotificationContentType = NotificationContentType.JourneyRequestSent
                        });
            }
        }
    }
}
