using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data.Entity;
using System.Linq;
using System.Web;
using DomainObjects;

namespace FindNDriveDataAccessLayer
{
    public class ApplicationContext : DbContext
    {
        /**
         * Each DbSet<T> repesents a collection of entitites that can be queried from the database. 
         **/
        public DbSet<User> User { get; set; }

        public ApplicationContext()
            : this("FindNDriveConnectionString")
        {
            Configuration.ProxyCreationEnabled = false;
        }


        public ApplicationContext(string connectionString)
            : base(connectionString)
        {
            Configuration.ProxyCreationEnabled = false;
        }
    }
}