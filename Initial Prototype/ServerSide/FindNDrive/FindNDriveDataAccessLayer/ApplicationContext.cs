// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ApplicationContext.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the ApplicationContext type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------



namespace FindNDriveDataAccessLayer
{
    using System.Data.Entity;
    using System.Data.Entity.ModelConfiguration.Conventions;

    using DomainObjects.Domains;

    /// <summary>
    /// The application context.
    /// </summary>
    public class ApplicationContext : DbContext
    {
        /**
         * Each DbSet<T> repesents a collection of entitites that can be queried from the database. 
         **/

        /// <summary>
        /// Gets or sets the user.
        /// </summary>
        public DbSet<User> User { get; set; }

        /// <summary>
        /// Gets or sets the car shares.
        /// </summary>
        public DbSet<Journey> Journeys { get; set; }

        /// <summary>
        /// Gets or sets the car share requests.
        /// </summary>
        public DbSet<JourneyRequest> JourneyRequests { get; set; }

        /// <summary>
        /// Gets or sets the sessions.
        /// </summary>
        public DbSet<Session> Sessions { get; set; }

        /// <summary>
        /// Gets or sets the chat messages.
        /// </summary>
        public DbSet<ChatMessage> ChatMessages { get; set; }

        /// <summary>
        /// Gets or sets the chat messages.
        /// </summary>
        public DbSet<Notification> Notifications { get; set; }

        public DbSet<GCMNotification> GCMNotifications { get; set; }

        /// <summary>
        /// Initializes a new instance of the <see cref="ApplicationContext"/> class.
        /// </summary>
        public ApplicationContext()
            : this("FindNDriveConnectionString")
        {
            Configuration.ProxyCreationEnabled = false;
            Configuration.LazyLoadingEnabled = false;
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="ApplicationContext"/> class.
        /// </summary>
        /// <param name="connectionString">
        /// The connection string.
        /// </param>
        public ApplicationContext(string connectionString)
            : base(connectionString)
        {
            Configuration.ProxyCreationEnabled = false;
            Configuration.LazyLoadingEnabled = false;
        }

        // Define the model relationships, if C# isn't able to infer them itself
        /// <summary>
        /// The on model creating.
        /// </summary>
        /// <param name="modelBuilder">
        /// The model builder.
        /// </param>
        protected override void OnModelCreating(DbModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Journey>()
                .HasMany(_ => _.Participants)
                .WithMany()
                .Map(map =>
                {
                    map.MapLeftKey("JourneyId");
                    map.MapRightKey("UserId");
                    map.ToTable("Journey_User");
                });

            modelBuilder.Entity<Journey>()
                .HasMany(_ => _.Messages)
                .WithMany()
                .Map(map =>
                {
                    map.MapLeftKey("JourneyId");
                    map.MapRightKey("JourneyMessageId");
                    map.ToTable("Journey_Message");
                });

            modelBuilder.Entity<Journey>()
                .HasMany(_ => _.Requests)
                .WithMany()
                .Map(map =>
                {
                    map.MapLeftKey("JourneyId");
                    map.MapRightKey("JourneyRequestId");
                    map.ToTable("Journey_Request");
                });

            modelBuilder.Entity<User>()
               .HasMany(_ => _.Friends).WithMany().Map(map =>
                {
                    map.MapLeftKey("UserId");
                    map.ToTable("Friend_Mappings");
                });

            modelBuilder.Conventions.Remove<OneToManyCascadeDeleteConvention>();   
        }
    }
}