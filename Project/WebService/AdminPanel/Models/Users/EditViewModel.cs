namespace AdminPanel.Models.Users
{
    using System;
    using System.Linq;

    using CODE.Framework.Wpf.Mvvm;

    using DataAccessLayer;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    public class EditViewModel : ViewModel
    {
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        private User user;

        public EditViewModel()
        {
            this.Actions.Add(new ViewAction("Show friends", execute: (a, o) => Controller.Action("Users", "List", new { users = this.user.Friends.ToList(), windowTitle = string.Format("{0}'s friends", this.user.UserName) })));

            this.Actions.Add(new ViewAction("Show journeys", execute: (a, o) => Controller.Action("Users", "List")));

            this.Actions.Add(new ViewAction("Show requests", execute: (a, o) => Controller.Action("Users", "List")));
          

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

            this.Actions.Add(new ViewAction("Save", execute: (a, o) => this.Save(), category: "Customer"));
            this.Actions.Add(new CloseCurrentViewAction(this, beginGroup: true, category: "Customer"));
        }

        public void LoadData(int id)
        {
            this.user = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeAll().First(_ => _.UserId == id);
            var journeys = this.findNDriveUnitOfWork.JourneyRepository.AsQueryable().IncludeAll().ToList();
            var messages = this.findNDriveUnitOfWork.JourneyMessageRepository.AsQueryable().ToList();
            var journeyMessages = this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable().ToList();
            var requests = this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable().ToList();

            this.Id = this.user.UserId;
            this.FirstName = this.user.FirstName;
            this.LastName = this.user.LastName;
            this.Username = this.user.UserName;
            this.Email = this.user.EmailAddress;
            this.PhoneNumber = this.user.PhoneNumber;
            this.GcmRegistrationId = this.user.GCMRegistrationID;
            this.Status = this.user.Status;
            this.DateOfBirth = this.user.DateOfBirth;
            this.Gender = this.user.Gender;
            this.ProfilePicture = this.user.ProfilePicture.ProfilePictureBytes;

            this.MemberSince = this.user.MemberSince;
            this.LastLogon = this.user.LastLogon;
            this.AverageRating = this.user.AverageRating;
        }

        public void Save()
        {
            var u = this.findNDriveUnitOfWork.UserRepository.Find(this.Id);
            u.EmailAddress = this.Email;
            this.findNDriveUnitOfWork.Commit();
        }

        public int Id { get; set; }
        public string FirstName { get; set; }
        public string LastName { get; set; }
        public string Username { get; set; }
        public string Email { get; set; }
        public string GcmRegistrationId { get; set; }
        public string PhoneNumber { get; set; }
        public byte[] ProfilePicture { get; set; }
        public Status Status { get; set; }
        public Gender? Gender { get; set; }
        public DateTime? DateOfBirth { get; set; }

        public DateTime MemberSince { get; set; }
        public DateTime LastLogon { get; set; }
        public double? AverageRating { get; set; }
    }
}
