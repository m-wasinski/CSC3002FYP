using System;
using System.Data.Entity;
using System.ServiceModel;
using DomainObjects.DOmains;
using DomainObjects.Domains;
using FindNDriveDataAccessLayer;
using FindNDriveInfrastructureCore.Util;
using FindNDriveInfrastructureDataAccessLayer;
using FindNDriveServices2.Contracts;
using FindNDriveServices2.Services;
using Microsoft.Practices.Unity;
using Unity.Wcf;
using WebMatrix.WebData;

namespace FindNDriveServices2
{
    public class WcfServiceFactory : UnityServiceHostFactory
    {
        private readonly string _connectionString;

        /// <summary>
        /// Initializes a new instance of the <see cref="WcfServiceFactory"/> class.
        /// </summary>
        public WcfServiceFactory()
            : this("FindNDriveConnectionString")
        {
        }

        public WcfServiceFactory(string connectionString)
        {
            _connectionString = connectionString;
        }

        public ServiceHost Foo(Type serviceType, Uri baseAddress)
        {
            return base.CreateServiceHost(serviceType, new[] {baseAddress});
        }

        /// <summary>
        ///     Configures the Inversion of Control container for WPF Services. 
        ///     In order for types to be bound at runtime, you should register them to the container in format: TFrom, TTo
        /// </summary>
        /// <param name="container">The IOC container</param>
        protected override void ConfigureContainer(IUnityContainer container)
        {
            InitializeMembership();
            RegisterUnitOfWork(container);
            RegisterServices(container);
        }

        /// <summary>
        /// initializes the membership for the WCF layer.
        /// </summary>
        private void InitializeMembership()
        {
            // If web security has already been initialized then don't do it again, Since this will be run per scenario.
           // if (!WebSecurity.Initialized)
                //WebSecurity.InitializeDatabaseConnection(_connectionString, "User", "Id", "Username", true);
            //WebSecurity.InitializeDatabaseConnection("FindNDriveConnectionString", "User", "Id", "Email", false);
        }

        /// <summary>
        /// Registers the known service implementations with the given container
        /// </summary>
        /// <param name="container">
        /// The container.
        /// </param>
        private void RegisterServices(IUnityContainer container)
        {
            container.RegisterType<IUserService, UserService>();
        }

        /// <summary>
        /// Registers the IRepository and UnitOfWork implementations with the given container
        /// </summary>
        /// <param name="container">
        /// The container.
        /// </param>
        private void RegisterUnitOfWork(IUnityContainer container)
        {
            container.RegisterType<DbContext, ApplicationContext>(new PerResolveLifetimeManager(), new InjectionConstructor(this._connectionString));
            container.RegisterType<IRepository<User>, EntityFrameworkRepository<User>>();
            container.RegisterType<IRepository<CarShare>, EntityFrameworkRepository<CarShare>>();
            container.RegisterType<FindNDriveUnitOfWork, FindNDriveUnitOfWork>();

            // Resolve a function type which can produce a unit of work when GetNewInstance<T>() is called
            container.RegisterType<IFactory<FindNDriveUnitOfWork>, Factory<FindNDriveUnitOfWork>>(
                new InjectionConstructor(new ResolvedParameter<Func<FindNDriveUnitOfWork>>()));
        }
    }
}
