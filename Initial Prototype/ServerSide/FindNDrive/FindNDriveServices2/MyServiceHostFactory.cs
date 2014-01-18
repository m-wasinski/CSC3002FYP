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
    using System.Reflection;
    using System.ServiceModel;
    using System.ServiceModel.Activation;
    using System.ServiceModel.Channels;
    using System.ServiceModel.Description;
    using System.ServiceModel.Dispatcher;

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
        /// The _service type.
        /// </summary>
        private readonly Type serviceType;

        /// <summary>
        /// The _user service.
        /// </summary>
        private readonly UserService userService;

        /// <summary>
        /// The _car share service.
        /// </summary>
        private readonly JourneyService journeyService;

        /// <summary>
        /// The _request service.
        /// </summary>
        private readonly JourneyRequestService requestService;

        /// <summary>
        /// The _search service.
        /// </summary>
        private readonly SearchService searchService;

        /// <summary>
        /// The _notification service.
        /// </summary>
        private readonly NotificationService notificationService;

        /// <summary>
        /// Initializes a new instance of the <see cref="MyInstanceProvider"/> class.
        /// </summary>
        /// <param name="serviceType">
        /// The service Type.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// </exception>
        public MyInstanceProvider(Type serviceType)
        {
            this.serviceType = serviceType;
            this.userService = new UserService();
            this.journeyService = new JourneyService();
            this.searchService = new SearchService();
            this.requestService = new JourneyRequestService();
            this.notificationService = new NotificationService();
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
            var dbContext = new ApplicationContext();
            var userRepository = new EntityFrameworkRepository<User>(dbContext);
            var journeyRepository = new EntityFrameworkRepository<Journey>(dbContext);
            var sessionEntityFrameworkRepository = new EntityFrameworkRepository<Session>(dbContext);
            var journeyRequestRepository = new EntityFrameworkRepository<JourneyRequest>(dbContext);
            var chatMessageRepository = new EntityFrameworkRepository<ChatMessage>(dbContext);
            var notificationRepository = new EntityFrameworkRepository<Notification>(dbContext);
            var findNDriveUnitOfWork = new FindNDriveUnitOfWork(
                dbContext,
                userRepository,
                journeyRepository,
                sessionEntityFrameworkRepository,
                journeyRequestRepository,
                chatMessageRepository,
                notificationRepository);

            var sessionManager = new SessionManager(findNDriveUnitOfWork);

            var service = this.serviceType.GetConstructor(new[] { typeof(FindNDriveUnitOfWork), typeof(SessionManager), typeof(GCMManager) });
            return service.Invoke(new object[] { findNDriveUnitOfWork, sessionManager, new GCMManager() });
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