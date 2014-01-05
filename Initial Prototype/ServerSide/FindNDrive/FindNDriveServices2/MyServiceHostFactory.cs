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
    using System.Diagnostics;
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
            return new MyServiceHost(serviceType, baseAddresses);
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
        /// The _service type.
        /// </summary>
        private readonly Type _serviceType;

        /// <summary>
        /// The _user service.
        /// </summary>
        private readonly UserService _userService;

        /// <summary>
        /// The _car share service.
        /// </summary>
        private readonly CarShareService _carShareService;

        /// <summary>
        /// The _request service.
        /// </summary>
        private readonly RequestService _requestService;

        /// <summary>
        /// The _search service.
        /// </summary>
        private readonly SearchService _searchService;

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
        public MyInstanceProvider(Type serviceType)
        {
            this._serviceType = serviceType;
            this._userService = new UserService();
            this._carShareService = new CarShareService();
            this._searchService = new SearchService();
            this._requestService = new RequestService();
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
            var testDbContext = new ApplicationContext();
            var userEntityFrameworkRepository = new EntityFrameworkRepository<User>(testDbContext);
            var carShareEntityFrameworkRepository = new EntityFrameworkRepository<CarShare>(testDbContext);
            var sessionEntityFrameworkRepository = new EntityFrameworkRepository<Session>(testDbContext);
            var carShareRequestEntityFrameWorkRepository = new EntityFrameworkRepository<CarShareRequest>(testDbContext);

            var findNDriveUnitOfWork = new FindNDriveUnitOfWork(
                testDbContext,
                userEntityFrameworkRepository,
                carShareEntityFrameworkRepository,
                sessionEntityFrameworkRepository,
                carShareRequestEntityFrameWorkRepository);
            var sessionManager = new SessionManager(findNDriveUnitOfWork);

            if (_serviceType == _userService.GetType())
            {
                return new UserService(findNDriveUnitOfWork, sessionManager);
            }

            if (_serviceType == _carShareService.GetType())
            {
                return new CarShareService(findNDriveUnitOfWork, sessionManager);
            }

            if (_serviceType == _searchService.GetType())
            {
                return new SearchService(findNDriveUnitOfWork, sessionManager);
            }

            if (_serviceType == _requestService.GetType())
            {
                return new RequestService(findNDriveUnitOfWork, sessionManager);
            }

            return null;
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

        /// <summary>
        /// The add binding parameters.
        /// </summary>
        /// <param name="contractDescription">
        /// The contract description.
        /// </param>
        /// <param name="endpoint">
        /// The endpoint.
        /// </param>
        /// <param name="bindingParameters">
        /// The binding parameters.
        /// </param>
        public void AddBindingParameters(ContractDescription contractDescription, ServiceEndpoint endpoint, BindingParameterCollection bindingParameters)
        {
        }

        /// <summary>
        /// The apply client behavior.
        /// </summary>
        /// <param name="contractDescription">
        /// The contract description.
        /// </param>
        /// <param name="endpoint">
        /// The endpoint.
        /// </param>
        /// <param name="clientRuntime">
        /// The client runtime.
        /// </param>
        public void ApplyClientBehavior(ContractDescription contractDescription, ServiceEndpoint endpoint, ClientRuntime clientRuntime)
        {
            clientRuntime.ClientMessageInspectors.Add(new CustomMessageInspector());
        }

        /// <summary>
        /// The apply dispatch behavior.
        /// </summary>
        /// <param name="contractDescription">
        /// The contract description.
        /// </param>
        /// <param name="endpoint">
        /// The endpoint.
        /// </param>
        /// <param name="dispatchRuntime">
        /// The dispatch runtime.
        /// </param>
        public void ApplyDispatchBehavior(ContractDescription contractDescription, ServiceEndpoint endpoint, DispatchRuntime dispatchRuntime)
        {
            dispatchRuntime.InstanceProvider = this;
        }

        /// <summary>
        /// The validate.
        /// </summary>
        /// <param name="contractDescription">
        /// The contract description.
        /// </param>
        /// <param name="endpoint">
        /// The endpoint.
        /// </param>
        public void Validate(ContractDescription contractDescription, ServiceEndpoint endpoint)
        {
        }

        #endregion
    }

}