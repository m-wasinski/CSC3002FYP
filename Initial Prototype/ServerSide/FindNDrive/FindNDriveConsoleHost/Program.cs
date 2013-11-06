using System;
using System.Collections.Generic;
using System.ServiceModel;
using DomainObjects;
using FindNDriveDataAccessLayer;
using FindNDriveInfrastructureDataAccessLayer;
using FindNDriveServices.Services;

namespace FindNDriveConsoleHost
{
    class Program
    {

        static void Main(string[] args)
        {
            ApplicationContext testDbContext = new ApplicationContext();

            EntityFrameworkRepository<User> userEntityFrameworkRepository = new EntityFrameworkRepository<User>(testDbContext);
            EntityFrameworkRepository<CarShare> carShareEntityFrameworkRepository = new EntityFrameworkRepository<CarShare>(testDbContext);

            FindNDriveUnitOfWork testUnitOfWork = new FindNDriveUnitOfWork(testDbContext, userEntityFrameworkRepository, carShareEntityFrameworkRepository);
          

            UserService testservice = new UserService(testUnitOfWork);
            CarShareService carShareService = new CarShareService(testUnitOfWork);
            var host = new ServiceHost(carShareService);

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
            var aleksandra = new User
            {
                FirstName = "Aleksandra",
                LastName = "Szczypior",
                DateOfBirth = new DateTime(1992, 11, 15),
                EmailAddress = "alex1710@vp.pl",
                Gender = Gender.Female,
            };

            try
            {
                host.Open();
                userEntityFrameworkRepository.Add(aleksandra);

                carShareEntityFrameworkRepository.Add(new CarShare()
                {
                    DateOfDeparture = new DateTime(DateTime.Today.Year, DateTime.Today.Month, DateTime.Today.Day),
                    DepartureCity = "Belfast",
                    Description = "Test Car Share",
                    DestinationCity = "Lurgan",
                    Driver = aleksandra,
                    Fee = 0.00,
                    AvailableSeats = 4,
                    Participants = new List<User>(),
                    SmokersAllowed = false,
                    WomenOnly = false,
                });

                testUnitOfWork.Commit();
            
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
