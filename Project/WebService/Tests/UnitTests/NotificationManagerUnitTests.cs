namespace Tests.UnitTests
{
    using System;
    using System.Collections.ObjectModel;
    using System.Data.Entity;
    using System.Linq;

    using DataAccessLayer;
    using DataAccessLayer.Migrations;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using Microsoft.VisualStudio.TestTools.UnitTesting;

    using Services.ServiceUtils;

    using TestContext = Tests.TestContext;

    [TestClass]
    public class NotificationManagerUnitTests
    {
        private FindNDriveUnitOfWork findNDriveUnitOfWork;

        private User user;

        private NotificationManager notificationManager;

        private SessionManager sessionManager;

        /// <summary>
        /// Initialises all the variables required for the test.
        /// </summary>
        [TestInitialize]
        public void Initialise()
        {
            var dbContext = new TestContext();
            var userRepository = new EntityFrameworkRepository<User>(dbContext);
            var journeyRepository = new EntityFrameworkRepository<Journey>(dbContext);
            var sessionEntityFrameworkRepository = new EntityFrameworkRepository<Session>(dbContext);
            var journeyRequestRepository = new EntityFrameworkRepository<JourneyRequest>(dbContext);
            var chatMessageRepository = new EntityFrameworkRepository<ChatMessage>(dbContext);
            var notificationRepository = new EntityFrameworkRepository<Notification>(dbContext);
            var friendsRequestRepository = new EntityFrameworkRepository<FriendRequest>(dbContext);
            var journeyMessageRepository = new EntityFrameworkRepository<JourneyMessage>(dbContext);
            var geoAddressRepository = new EntityFrameworkRepository<GeoAddress>(dbContext);
            var ratingsRepository = new EntityFrameworkRepository<Rating>(dbContext);
            var profilePictureRepository = new EntityFrameworkRepository<ProfilePicture>(dbContext);
            var journeyTemplateRepository = new EntityFrameworkRepository<JourneyTemplate>(dbContext);
            findNDriveUnitOfWork = new FindNDriveUnitOfWork(
                dbContext,
                userRepository,
                journeyRepository,
                sessionEntityFrameworkRepository,
                journeyRequestRepository,
                chatMessageRepository,
                notificationRepository,
                friendsRequestRepository,
                journeyMessageRepository,
                geoAddressRepository,
                ratingsRepository,
                profilePictureRepository,
                journeyTemplateRepository);

            sessionManager = new SessionManager(findNDriveUnitOfWork);
            notificationManager = new NotificationManager(findNDriveUnitOfWork, sessionManager);

            this.user = TestFactories.GetUser();
            this.findNDriveUnitOfWork.UserRepository.Add(user);
            this.findNDriveUnitOfWork.Commit();
            this.user = this.findNDriveUnitOfWork.UserRepository.AsQueryable().FirstOrDefault(_ => _.UserName.Equals(user.UserName));
        }

        [TestMethod]
        public void TestSendInAppNotification()
        {
            notificationManager.CreateAppNotification(new Collection<User> { user }, "New notificaion", "You have a new notification", -1, -1, NotificationType.App, NotificationContentType.FriendOfferedNewJourney, -1);

            var currentUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Include(_ => _.Notifications)
                    .FirstOrDefault(_ => _.UserId == user.UserId);

            Assert.AreEqual(1, currentUser.Notifications.Count);
            Assert.AreEqual(NotificationType.App, currentUser.Notifications.First().NotificationType);
        }

        [TestMethod]
        public void TestSendDeviceNotification()
        {
            notificationManager.CreateAppNotification(new Collection<User>{user}, "New notificaion", "You have a new notification", -1, -1, NotificationType.Device, NotificationContentType.FriendOfferedNewJourney, -1);

            var currentUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Include(_ => _.Notifications)
                    .FirstOrDefault(_ => _.UserId == user.UserId);

            Assert.AreEqual(1, currentUser.Notifications.Count);
            Assert.AreEqual(NotificationType.Device, currentUser.Notifications.First().NotificationType);
        }

        /// <summary>
        /// Cleans up the database after the test.
        /// </summary>
        [TestCleanup]
        public void TestCleanup()
        {
            var notifications =
                this.findNDriveUnitOfWork.NotificationRepository.AsQueryable().Where(_ => _.UserId == user.UserId).ToList();

            this.findNDriveUnitOfWork.NotificationRepository.RemoveRange(notifications); 
            this.findNDriveUnitOfWork.UserRepository.Remove(user);
            
            this.findNDriveUnitOfWork.Commit();
        }
    }
}
