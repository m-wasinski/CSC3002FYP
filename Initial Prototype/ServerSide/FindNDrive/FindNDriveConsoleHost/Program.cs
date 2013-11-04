using System;
using System.Collections.Generic;
using System.Data.Entity;
using System.Linq;
using System.ServiceModel;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using DomainObjects;
using FindNDriveDataAccessLayer;
using FindNDriveInfrastructureDataAccessLayer;
using FindNDriveServices;
using FindNDriveServices.Contracts;
using FindNDriveServices.Services;

namespace FindNDriveConsoleHost
{
    class Program
    {
        private static volatile bool serverRunnning;
       
        //private readonly IObjectContainer _objectContainer;
        static void Main(string[] args)
        {
            ApplicationContext testDbContext = new ApplicationContext();
            EntityFrameworkRepository<User> testEntityFrameworkRepository = new EntityFrameworkRepository<User>(testDbContext);
            FindNDriveUnitOfWork testUnitOfWork = new FindNDriveUnitOfWork(testDbContext, new EntityFrameworkRepository<User>(testDbContext));
           

            UserService testservice = new UserService(testUnitOfWork);

            var host = new ServiceHost(testservice);

            /*var thread = new Thread(
                () =>
                {
                    // var testDataPath = Directory.GetCurrentDirectory();

                    //var baseAddress = new Uri(address);
                    var factory = new WcfServiceFactory("FindNDriveConnectionString");
                    var serviceHost = factory.Foo(typeof(UserService), new Uri("UserServiceEndpoint"));

                    //serviceHost.AddServiceEndpoint(typeof(IUserService), new WSHttpBinding(), baseAddress);

                    serviceHost.Open();

                    serverRunnning = true;

                    var addresses = serviceHost.BaseAddresses;

                    // When Teardown is called we can end this service host
                    while (serverRunnning)
                    {
                        // Black this thread until we are allowed to stop the server running
                        Thread.Sleep(1000);
                    }

                    serviceHost.Close();
                });
            thread.Start();

            // Block until the server is up and running
            while (serverRunnning)
            {
                // Block
                Thread.Sleep(1000);
            }*/

            /*host.AddServiceEndpoint(typeof (IPrototypeService),
                new BasicHttpBinding(), "http://localhost:8080/prototype/basic");

            host.AddServiceEndpoint(typeof(IPrototypeService),
                new WSHttpBinding(), "http://localhost:8080/prototype/ws");

            host.AddServiceEndpoint(typeof(IPrototypeService),
                new NetTcpBinding(), "net.tcp://localhost:8081/evals");*/

            try
            {
                host.Open();
               /* testEntityFrameworkRepository.Add(new User
             {
                 Id = 2,
                 FirstName = "Aleksandra",
                 LastName = "Szczypior",
                 Age = 20
             } );
                testUnitOfWork.Commit();*/
                PrintServiceInfo(host);
                Console.ReadLine();
                host.Close();
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
                host.Abort();
                Console.ReadLine();
            }
        }

        static void PrintServiceInfo(ServiceHost host)
        {
            Console.WriteLine(" {0} is up an running with these endpoints:",
                host.Description.ServiceType);

            foreach (var se in host.Description.Endpoints)
            {
                Console.WriteLine(se.Address);
            }
        }
    }
}
