namespace AdminPanel.Models.Users
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Linq;

    using CODE.Framework.Wpf.Mvvm;

    using DataAccessLayer;

    using DomainObjects.Domains;

    public class SearchViewModel : UsersListViewModel
    {
        private readonly List<User> users;

        public SearchViewModel(List<User> users, string windowTitle) : base(users, windowTitle, true)
        {
            this.SearchCustomers = new ViewAction("Search", execute: (a, o) => this.Search());
            this.ResetAll = new ViewAction("Reset", execute: (a, o) => this.Reset());

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
            var findNDriveUnitOfWork = new FindNDriveUnitOfWork(
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

            this.users = findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeChildren().ToList();
        }

        public IViewAction SearchCustomers { get; set; }
        public IViewAction ResetAll { get; set; }

        public void Search()
        {
            var searchUsers = users;

            if (SearchID != null)
            {
                searchUsers = users.Where(_ => _.UserId == Convert.ToInt32(SearchID)).ToList();
            }

            if (SearchUsername != null)
            {
                searchUsers = users.Where(_ => _.UserName.Equals(SearchUsername)).ToList();
            }

            if (SearchFirstName != null)
            {
                searchUsers = users.Where(_ => _.UserName.Equals(SearchFirstName)).ToList();
            }

            if (SearchLastName != null)
            {
                searchUsers = users.Where(_ => _.UserName.Equals(SearchLastName)).ToList();
            }

            if (SearchEmailAddress != null)
            {
                searchUsers = users.Where(_ => _.UserName.Equals(SearchEmailAddress)).ToList();
            }

            this.LoadCustomers(searchUsers);
        }

        public void Reset()
        {
            SearchID = string.Empty;
            SearchUsername = string.Empty;
            SearchFirstName = string.Empty;
            SearchLastName = string.Empty;
            SearchEmailAddress = string.Empty;
            this.LoadCustomers(users);
        }

        public string SearchID { get; set; }
        public string SearchUsername { get; set; }
        public string SearchFirstName { get; set; }
        public string SearchLastName { get; set; }
        public string SearchEmailAddress { get; set; }

    }

    public class UsersListViewModel : ViewModel
    {
        public IViewAction SearchCustomers { get; set; }

        public UsersListViewModel(List<User> users,  string windowTItle, bool loadCustomers = true)
        {
            this.Users = new ObservableCollection<UserInformation>();
            
            this.LoadCustomers(users);
            this.Actions.Add(new CloseCurrentViewAction(this, beginGroup: true));
            this.EditCustomer = new ViewAction("Edit", execute: (a, o) => this.LaunchEdit(o as UserInformation));
        }

        public IViewAction EditCustomer { get; set; }
        public IViewAction ListFriends { get; set; }
        public string WindowTitle { get; set; }
        public ObservableCollection<UserInformation> Users { get; set; }

        public void LoadCustomers(List<User> users)
        {
            this.Users.Clear();
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
