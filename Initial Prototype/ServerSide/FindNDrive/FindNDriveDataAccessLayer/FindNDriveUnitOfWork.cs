// --------------------------------------------------------------------------------------------------------------------
// <copyright file="FindNDriveUnitOfWork.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the FindNDriveUnitOfWork type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveDataAccessLayer
{
    using System;
    using System.Data.Entity;

    using DomainObjects.Domains;

    using FindNDriveInfrastructureDataAccessLayer;

    /// <summary>
    /// The find n drive unit of work.
    /// </summary>
    public class FindNDriveUnitOfWork : IUnitOfWork
    {
        /// <summary>
        /// The _db context.
        /// </summary>
        private readonly DbContext _dbContext;

        /**
        * Each IRepository<T> repesents a repository available in the database. 
        **/

        /// <summary>
        /// Gets or sets the user repository.
        /// </summary>
        public IRepository<User> UserRepository { get; set; }

        /// <summary>
        /// Gets or sets the car share repository.
        /// </summary>
        public IRepository<Journey> JourneyRepository { get; set; }

        /// <summary>
        /// Gets or sets the session repository.
        /// </summary>
        public IRepository<Session> SessionRepository { get; set; }

        /// <summary>
        /// Gets or sets the car share request repository.
        /// </summary>
        public IRepository<JourneyRequest> JourneyRequestRepository { get; set; }

        /// <summary>
        /// Gets or sets the chat message repository.
        /// </summary>
        public IRepository<ChatMessage> ChatMessageRepository { get; set; }

        /// <summary>
        /// Gets or sets the notification repository.
        /// </summary>
        public IRepository<Notification> NotificationRepository { get; set; }

        /// <summary>
        /// Gets or sets the gcm notifications repository.
        /// </summary>
        public IRepository<GCMNotification> GCMNotificationsRepository { get; set; } 

        /// <summary>
        /// Initializes a new instance of the <see cref="FindNDriveUnitOfWork"/> class.
        /// </summary>
        /// <param name="dbContext">
        /// The db context.
        /// </param>
        /// <param name="userRepository">
        /// The user repository.
        /// </param>
        /// <param name="journeyRepository">
        /// The car share repository.
        /// </param>
        /// <param name="sessionRepository">
        /// The session repository.
        /// </param>
        /// <param name="journeyRequestRepository">
        /// The car Share Request Repository.
        /// </param>
        /// <param name="chatMessageRepository">
        /// The chat Message Repository.
        /// </param>
        /// <param name="notificationRepository">
        /// The notification Repository.
        /// </param>
        /// <param name="gcmNotificationsRepository">
        /// The gcm Notifications Repository.
        /// </param>
        public FindNDriveUnitOfWork(
            DbContext dbContext,
            IRepository<User> userRepository,
            IRepository<Journey> journeyRepository,
            IRepository<Session> sessionRepository,
            IRepository<JourneyRequest> journeyRequestRepository,
                                    IRepository<ChatMessage> chatMessageRepository,
                                    IRepository<Notification> notificationRepository,
                                    IRepository<GCMNotification> gcmNotificationsRepository)
        {
            this._dbContext = dbContext;
            this.UserRepository = userRepository;
            this.JourneyRepository = journeyRepository;
            this.SessionRepository = sessionRepository;
            this.JourneyRequestRepository = journeyRequestRepository;
            this.ChatMessageRepository = chatMessageRepository;
            this.NotificationRepository = notificationRepository;
            this.GCMNotificationsRepository = gcmNotificationsRepository;
        }

        /// <summary>
        /// The commit.
        /// </summary>
        public void Commit()
        {
            this._dbContext.SaveChanges();
        }

        /// <summary>
        /// The dispose.
        /// </summary>
        public void Dispose()
        {
            this._dbContext.Dispose();
        }
    }
}