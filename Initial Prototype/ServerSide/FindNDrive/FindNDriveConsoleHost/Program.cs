using System;
using System.Collections.Generic;
using System.Linq;
using System.ServiceModel;
using System.Text;
using System.Threading.Tasks;
using FindNDriveServices.Contracts;
using FindNDriveServices.Services;

namespace FindNDriveConsoleHost
{
    class Program
    {
        static void Main(string[] args)
        {
            var host = new ServiceHost(typeof(UserService));

            /*host.AddServiceEndpoint(typeof (IPrototypeService),
                new BasicHttpBinding(), "http://localhost:8080/prototype/basic");

            host.AddServiceEndpoint(typeof(IPrototypeService),
                new WSHttpBinding(), "http://localhost:8080/prototype/ws");

            host.AddServiceEndpoint(typeof(IPrototypeService),
                new NetTcpBinding(), "net.tcp://localhost:8081/evals");*/

            try
            {
                host.Open();
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
