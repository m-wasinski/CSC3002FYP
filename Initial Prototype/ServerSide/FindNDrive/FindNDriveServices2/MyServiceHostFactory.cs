using System;
using System.Collections.Generic;
using System.Linq;
using System.ServiceModel;
using System.ServiceModel.Activation;
using System.ServiceModel.Channels;
using System.ServiceModel.Description;
using System.ServiceModel.Dispatcher;
using System.Web;
using DomainObjects.DOmains;
using DomainObjects.Domains;
using FindNDriveDataAccessLayer;
using FindNDriveInfrastructureDataAccessLayer;
using FindNDriveServices2.Services;

namespace FindNDriveServices2
{
    public class MyServiceHostFactory : ServiceHostFactory
    {
        private ApplicationContext testDbContext;
        private EntityFrameworkRepository<User> userEntityFrameworkRepository;
        private EntityFrameworkRepository<CarShare> carShareEntityFrameworkRepository;
        private EntityFrameworkRepository<Session> sessionEntityFrameworkRepository;
 
        private readonly FindNDriveUnitOfWork _findNDriveUnitOfWork;

        public MyServiceHostFactory()
        {
            testDbContext = new ApplicationContext();
            userEntityFrameworkRepository = new EntityFrameworkRepository<User>(testDbContext);
            carShareEntityFrameworkRepository = new EntityFrameworkRepository<CarShare>(testDbContext);
            sessionEntityFrameworkRepository = new EntityFrameworkRepository<Session>(testDbContext);
            _findNDriveUnitOfWork = new FindNDriveUnitOfWork(testDbContext, userEntityFrameworkRepository, carShareEntityFrameworkRepository, sessionEntityFrameworkRepository);
        }

        protected override ServiceHost CreateServiceHost(Type serviceType,
            Uri[] baseAddresses)
        {
            return new MyServiceHost(_findNDriveUnitOfWork, serviceType, baseAddresses);
        }
    }

    public class MyServiceHost : ServiceHost
    {
        public MyServiceHost(FindNDriveUnitOfWork findNDriveUnitOfWork, Type serviceType, params Uri[] baseAddresses)
            : base(serviceType, baseAddresses)
        {
            if (findNDriveUnitOfWork == null)
            {
                throw new ArgumentNullException("dep");
            }

            foreach (var cd in this.ImplementedContracts.Values)
            {
                cd.Behaviors.Add(new MyInstanceProvider(findNDriveUnitOfWork));
            }
        }
    }

    public class MyInstanceProvider : IInstanceProvider, IContractBehavior
    {
        private readonly FindNDriveUnitOfWork _findNDriveUnitOfWork;

        public MyInstanceProvider(FindNDriveUnitOfWork findNDriveUnitOfWork)
        {
            if (findNDriveUnitOfWork == null)
            {
                throw new ArgumentNullException("dep");
            }

            _findNDriveUnitOfWork = findNDriveUnitOfWork;
        }

        #region IInstanceProvider Members

        public object GetInstance(InstanceContext instanceContext, Message message)
        {
            return this.GetInstance(instanceContext);
        }

        public object GetInstance(InstanceContext instanceContext)
        {
            return new UserService(_findNDriveUnitOfWork);
        }

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