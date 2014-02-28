using System;

namespace FindNDriveAdminPanel2.Models.Users
{
    using System.Linq;

    using DomainObjects.Constants;
    using DomainObjects.Domains;
    using CODE.Framework.Wpf.Mvvm;

    using FindNDriveDataAccessLayer;

    using FindNDriveInfrastructureDataAccessLayer;

    public class EditViewModel : ViewModel
    {
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        private User user;

        public EditViewModel()
        {
            Actions.Add(new ViewAction("Show friends", execute: (a, o) => Controller.Action("Users", "List", new { users = this.user.Friends.ToList(), windowTitle = string.Format("{0}'s friends", this.user.UserName) })));

            Actions.Add(new ViewAction("Show journeys", execute: (a, o) => Controller.Action("Users", "List")));

            Actions.Add(new ViewAction("Show requests", execute: (a, o) => Controller.Action("Users", "List")));
          

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
                profilePictureRepository);

            Actions.Add(new ViewAction("Save", execute: (a, o) => Save(), category: "Customer"));
            Actions.Add(new CloseCurrentViewAction(this, beginGroup: true, category: "Customer"));
        }

        public void LoadData(int id)
        {
            this.user = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeAll().First(_ => _.UserId == id);
            var journeys = this.findNDriveUnitOfWork.JourneyRepository.AsQueryable().IncludeAll().ToList();
            var messages = this.findNDriveUnitOfWork.JourneyMessageRepository.AsQueryable().ToList();
            var journeyMessages = this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable().ToList();
            var requests = this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable().ToList();

            Id = user.UserId;
            FirstName = user.FirstName;
            LastName = user.LastName;
            Username = user.UserName;
            Email = user.EmailAddress;
            PhoneNumber = user.PhoneNumber;
            GcmRegistrationId = user.GCMRegistrationID;
            Status = user.Status;
            DateOfBirth = user.DateOfBirth;
            Gender = user.Gender;
            ProfilePicture = user.ProfilePicture.ProfilePictureBytes;

            MemberSince = user.MemberSince;
            LastLogon = user.LastLogon;
            AverageRating = user.AverageRating;
        }

        public void Save()
        {
            var u = this.findNDriveUnitOfWork.UserRepository.Find(Id);
            u.EmailAddress = Email;
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
        public Gender Gender { get; set; }
        public DateTime DateOfBirth { get; set; }

        public DateTime MemberSince { get; set; }
        public DateTime LastLogon { get; set; }
        public double AverageRating { get; set; }
    }
}
