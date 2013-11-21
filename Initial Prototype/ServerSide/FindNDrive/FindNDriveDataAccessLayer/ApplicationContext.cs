using System.Data.Entity;
using DomainObjects.Domains;
using DomainObjects.DOmains;

namespace FindNDriveDataAccessLayer
{
    public class ApplicationContext : DbContext
    {
        /**
         * Each DbSet<T> repesents a collection of entitites that can be queried from the database. 
         **/
        public DbSet<User> User { get; set; }
        public DbSet<CarShare> CarShares { get; set; }
        public DbSet<Session> Sessions { get; set; }
        public ApplicationContext()
            : this("FindNDriveConnectionString")
        {   
        }


        public ApplicationContext(string connectionString)
            : base(connectionString)
        {
        }

        // Define the model relationships, if C# isn't able to infer them itself
        protected override void OnModelCreating(DbModelBuilder modelBuilder)
        {
        }
    }
}