using System;
using System.ServiceModel;
using System.Threading;
using DomainObjects.Constants;
using DomainObjects.Domains;
using DomainObjects.DOmains;
using FindNDriveDataAccessLayer;
using FindNDriveInfrastructureDataAccessLayer;
using FindNDriveServices2;
using FindNDriveServices2.DTOs;
using FindNDriveServices2.Services;
using WebMatrix.WebData;

namespace FindNDriveConsoleHost
{
    using FindNDriveServices2.Contracts;

    /// <summary>
    /// Used temporarily to create and host my web services.
    /// Endpoints for these services are stored in App.config in FindNDriveConsoleHost project.
    /// </summary>
    class Program
    {
        static void Main(string[] args)
        {
            //Create dbcontext, user entity framework repository, car share entity framework repository and a unit of work.
            var testDbContext = new ApplicationContext();
            var userEntityFrameworkRepository = new EntityFrameworkRepository<User>(testDbContext);
            var carShareEntityFrameworkRepository = new EntityFrameworkRepository<CarShare>(testDbContext);
            var sessionEntityFrameworkRepository = new EntityFrameworkRepository<Session>(testDbContext);
            //var testUnitOfWork = new FindNDriveUnitOfWork(testDbContext, userEntityFrameworkRepository, carShareEntityFrameworkRepository, sessionEntityFrameworkRepository);
            //var sessionManager = new SessionManager(testUnitOfWork);
            //WebSecurity.InitializeDatabaseConnection("FindNDriveConnectionString", "User", "Id", "UserName", true);
            var carShareService = new CarShareService();
            //Spawn these two services in two separate threads to ensure they both run concurrently.
            //var userServiceThread = new Thread(
            //    () =>
            //    {
            //        //var baseAddress = new Uri("https://asus:8050/userservice.svc");
            //        //var factory = new WcfServiceFactory("FindNDriveConnectionString");
            //        //var serviceHost = factory.Foo(typeof(UserService), baseAddress);

            //        //serviceHost.AddServiceEndpoint(typeof(IUserService), new WebHttpBinding(), baseAddress);
                    

            //        var userService = new UserService(testUnitOfWork);
            //        var host = new ServiceHost(userService);

            //        try
            //        {
            //            host.Open();

            //            PrintServiceInfo(host);
            //        }
            //        catch (Exception e)
            //        {
            //            Console.WriteLine(e);
            //            host.Abort();

            //        }
            //    });
            //userServiceThread.Start();

        }

        /// <summary>
        /// Outputs information about servicehost passed in as a parameter.
        /// </summary>
        /// <param name="host"></param>
        static void PrintServiceInfo(ServiceHost host)
        {
            Console.WriteLine(" {0} is now running with the following endpoints:",
                host.Description.ServiceType);

            foreach (var se in host.Description.Endpoints)
            {
                Console.WriteLine(se.Address);
            }
        }
    }
}
