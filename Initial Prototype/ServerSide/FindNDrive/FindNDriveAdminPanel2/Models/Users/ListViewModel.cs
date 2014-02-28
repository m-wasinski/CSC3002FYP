using System;
using System.Linq;

namespace FindNDriveAdminPanel2.Models.Users
{
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Windows.Documents;

    using CODE.Framework.Wpf.Mvvm;

    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveInfrastructureDataAccessLayer;

    public class SearchViewModel : ListViewModel
    {
      
        public SearchViewModel()
            : base(null, "Search Results", false)
        {
            SearchCustomers = new ViewAction("Search", execute: (a, o) => Search());
        }

        public IViewAction SearchCustomers { get; set; }

        public void Search()
        {
            // TODO: Perform actual search here... but for now we just load the fake data
            LoadCustomers(null);
        }

        // Example search terms
        public string SearchTerm1 { get; set; }
        public string SearchTerm2 { get; set; }
        public string SearchTerm3 { get; set; }
        public string SearchTerm4 { get; set; }
        public string SearchTerm5 { get; set; }
    }

    public class ListViewModel : ViewModel
    {
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        public ListViewModel(List<User> users,  string windowTItle, bool loadCustomers = true)
        {
            WindowTitle = windowTItle;
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

            this.findNDriveUnitOfWork = new FindNDriveUnitOfWork(
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
                profilePictureRepository);

            this.Users = new ObservableCollection<UserInformation>();
            
            if (loadCustomers)
            {
                LoadCustomers(users);
            }

            Actions.Add(new CloseCurrentViewAction(this, beginGroup: true));
            EditCustomer = new ViewAction("Edit", execute: (a, o) => LaunchEdit(o as UserInformation));
            ListFriends = new ViewAction("Edit", execute: (a, o) => LaunchEdit(o as UserInformation));
        }

        public IViewAction EditCustomer { get; set; }
        public IViewAction ListFriends { get; set; }
        public string WindowTitle { get; set; }
        public ObservableCollection<UserInformation> Users { get; set; }

        public void LoadCustomers(List<User> users)
        {
            users.ForEach(
                user =>
                this.Users.Add(
                    new UserInformation
                        {
                            FirstName = user.FirstName,
                            LastName = user.LastName,
                            Id = user.UserId,
                            Username = user.UserName,
                            ProfilePicture = user.ProfilePicture.ProfilePictureBytes
                        }));


        }

        public void LaunchEdit(UserInformation user)
        {
            Controller.Action("Users", "Edit", new { id = user.Id });
        }
    }

    public class UserInformation
    {
        public int Id { get; set; }
        public byte[] ProfilePicture { get; set; }
        public string FirstName { get; set; }
        public string LastName { get; set; }
        public string Username { get; set; }
    }
}
