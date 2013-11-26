// --------------------------------------------------------------------------------------------------------------------
// <copyright file="MyServiceHostFactory.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the MyServiceHostFactory type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2
{
    using System;
    using System.ServiceModel;
    using System.ServiceModel.Activation;
    using System.ServiceModel.Channels;
    using System.ServiceModel.Description;
    using System.ServiceModel.Dispatcher;
    using DomainObjects.DOmains;
    using DomainObjects.Domains;
    using FindNDriveDataAccessLayer;
    using FindNDriveInfrastructureDataAccessLayer;
    using FindNDriveServices2.Services;
    using WebMatrix.WebData;

    /// <summary>
    /// The my service host factory.
    /// </summary>
    public class MyServiceHostFactory : ServiceHostFactory
    {
        /// <summary>
        /// The _find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork _findNDriveUnitOfWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager _sessionManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="MyServiceHostFactory"/> class.
        /// </summary>
        public MyServiceHostFactory()
        {
            if (!WebSecurity.Initialized)
                WebSecurity.InitializeDatabaseConnection("FindNDriveConnectionString", "User", "Id", "UserName", true);

            var testDbContext = new ApplicationContext();
            var userEntityFrameworkRepository = new EntityFrameworkRepository<User>(testDbContext);
            var carShareEntityFrameworkRepository = new EntityFrameworkRepository<CarShare>(testDbContext);
            var sessionEntityFrameworkRepository = new EntityFrameworkRepository<Session>(testDbContext);
            _findNDriveUnitOfWork = new FindNDriveUnitOfWork(testDbContext, userEntityFrameworkRepository, carShareEntityFrameworkRepository, sessionEntityFrameworkRepository);
            _sessionManager = new SessionManager(_findNDriveUnitOfWork);
        }

        /// <summary>
        /// The create service host.
        /// </summary>
        /// <param name="serviceType">
        /// The service type.
        /// </param>
        /// <param name="baseAddresses">
        /// The base addresses.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceHost"/>.
        /// </returns>
        protected override ServiceHost CreateServiceHost(Type serviceType, Uri[] baseAddresses)
        {
            return new MyServiceHost(_findNDriveUnitOfWork, _sessionManager, serviceType, baseAddresses);
        }
    }

    /// <summary>
    /// The my instance provider.
    /// </summary>
    public class MyInstanceProvider : IInstanceProvider, IContractBehavior
    {
        /// <summary>
        /// The _find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork _findNDriveUnitOfWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager _sessionManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="MyInstanceProvider"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// </exception>
        public MyInstanceProvider(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager)
        {
            if (findNDriveUnitOfWork == null)
            {
                throw new ArgumentNullException("findNDriveUnitOfWork");
            }
            
            this._findNDriveUnitOfWork = findNDriveUnitOfWork;
            this._sessionManager = sessionManager;
        }

        #region IInstanceProvider Members

        /// <summary>
        /// The get instance.
        /// </summary>
        /// <param name="instanceContext">
        /// The instance context.
        /// </param>
        /// <param name="message">
        /// The message.
        /// </param>
        /// <returns>
        /// The <see cref="object"/>.
        /// </returns>
        public object GetInstance(InstanceContext instanceContext, Message message)
        {
            return this.GetInstance(instanceContext);
        }

        /// <summary>
        /// The get instance.
        /// </summary>
        /// <param name="instanceContext">
        /// The instance context.
        /// </param>
        /// <returns>
        /// The <see cref="object"/>.
        /// </returns>
        public object GetInstance(InstanceContext instanceContext)
        {
            return new UserService(this._findNDriveUnitOfWork, this._sessionManager);
        }

        /// <summary>
        /// The release instance.
        /// </summary>
        /// <param name="instanceContext">
        /// The instance context.
        /// </param>
        /// <param name="instance">
        /// The instance.
        /// </param>
        public void ReleaseInstance(InstanceContext instanceContext, object instance)
        {
        }

        #endregion

        #region IContractBehavior Members

        public void AddBindingParameters(ContractDescription contractDescription, ServiceEndpoint endpoint, BindingParameterCollection bindingParameters)
        {
        }

        public void ApplyClientBehavior(ContractDescription contractDescription, ServiceEndpoint endpoint, ClientRuntime clientRuntime)
        {
            clientRuntime.ClientMessageInspectors.Add(new CustomMessageInspector());
        }

        public void ApplyDispatchBehavior(ContractDescription contractDescription, ServiceEndpoint endpoint, DispatchRuntime dispatchRuntime)
        {
            dispatchRuntime.InstanceProvider = this;
        }

        public void Validate(ContractDescription contractDescription, ServiceEndpoint endpoint)
        {
        }

        #endregion
    }

}