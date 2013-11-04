using System.Collections.Generic;
using DomainObjects;

namespace FindNDriveDataAccessLayer.Migrations
{
    using System;
    using System.Data.Entity;
    using System.Data.Entity.Migrations;
    using System.Linq;

    internal sealed class Configuration : DbMigrationsConfiguration<FindNDriveDataAccessLayer.ApplicationContext>
    {
        public Configuration()
        {
            AutomaticMigrationsEnabled = true;
        }

        protected override void Seed(ApplicationContext context)
        {
            //  This method will be called after migrating to the latest version.

            context.User.AddOrUpdate(
                _ => _.Id,
                new User
                {
                    Id = 1,
                    FirstName = "Michal",
                    LastName = "Wasinski",
                    Age = 23
                });
        }
    }
}
