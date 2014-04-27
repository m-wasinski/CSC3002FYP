namespace AdminPanel.Models.Home
{
    using System.Linq;

    using CODE.Framework.Wpf.Mvvm;

    using DataAccessLayer;

    using DomainObjects.Domains;

    public class StartViewModel : ViewModel
    {
        public static StartViewModel Current { get; set; }

        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        public StartViewModel()
        {
            Current = this;

            var dbContext = new ApplicationContext();
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

            this.findNDriveUnitOfWork = this.findNDriveUnitOfWork = new FindNDriveUnitOfWork(
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
        }

        public void LoadActions()
        {
            this.Actions.Clear();

            this.Actions.Add(new ViewAction("Users", execute: (a, o) => Controller.Action("Users", "Search", new {users = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeChildren().ToList(), windowTitle = "Users" })) { Significance = ViewActionSignificance.AboveNormal });
        }
    }
}
