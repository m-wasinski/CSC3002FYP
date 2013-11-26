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
    using DomainObjects.DOmains;

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
        public DbSet<CarShare> CarShares { get; set; }

        /// <summary>
        /// Gets or sets the sessions.
        /// </summary>
        public DbSet<Session> Sessions { get; set; }

        /// <summary>
        /// Initializes a new instance of the <see cref="ApplicationContext"/> class.
        /// </summary>
        public ApplicationContext()
            : this("FindNDriveConnectionString")
        {
            Configuration.ProxyCreationEnabled = false;
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
            modelBuilder.Entity<CarShare>()
                .HasMany(_ => _.Participants)
                .WithMany()
                .Map(map =>
                {
                    map.MapLeftKey("CarShareId");
                    map.MapRightKey("UserId");
                    map.ToTable("CarShare_User");
                });

            modelBuilder.Conventions.Remove<OneToManyCascadeDeleteConvention>();   
        }
    }
}