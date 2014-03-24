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

            // TODO: The following list of actions is used to populate the application's main navigation area (such as a menu or a home screen)

            this.Actions.Add(new ViewAction("Users", execute: (a, o) => Controller.Action("Users", "Search", new {users = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeAll().ToList(), windowTitle = "Users" })) { Significance = ViewActionSignificance.AboveNormal });

            this.Actions.Add(new ViewAction("Journeys", execute: (a, o) => Controller.Message("Menu Item #3 clicked!")) { Significance = ViewActionSignificance.AboveNormal });

            //Actions.Add(new ViewAction("Menu Item #3", execute: (a, o) => Controller.Message("Menu Item #3 clicked!")));

            /*Actions.Add(new SwitchThemeViewAction("Workplace", "Workplace (Office 2013) Theme", category: "View", categoryAccessKey: 'V', accessKey: 'W'));
            Actions.Add(new SwitchThemeViewAction("Metro", "Metro Theme", category: "View", categoryAccessKey: 'V', accessKey: 'M'));
            Actions.Add(new SwitchThemeViewAction("Battleship", "Windows 95 Theme", category: "View", categoryAccessKey: 'V', accessKey: 'W'));
            Actions.Add(new SwitchThemeViewAction("Vapor", "Vapor Theme", category: "View", categoryAccessKey: 'V', accessKey: 'V'));
            Actions.Add(new SwitchThemeViewAction("Geek", "Geek (Visual Studio) Theme", category: "View", categoryAccessKey: 'V', accessKey: 'G'));*/
        }
    }
}
