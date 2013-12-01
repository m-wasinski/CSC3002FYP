// --------------------------------------------------------------------------------------------------------------------
// <copyright file="MyServiceHost.cs" company="">
//   
// </copyright>
// <summary>
//   The my service host.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2
{
    using System;
    using System.ServiceModel;

    using FindNDriveDataAccessLayer;

    /// <summary>
    /// The my service host.
    /// </summary>
    public class MyServiceHost : ServiceHost
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="MyServiceHost"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        /// <param name="serviceType">
        /// The service type.
        /// </param>
        /// <param name="baseAddresses">
        /// The base addresses.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// </exception>
        public MyServiceHost(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, Type serviceType, params Uri[] baseAddresses)
            : base(serviceType, baseAddresses)
        {
            foreach (var cd in this.ImplementedContracts.Values)
            {
                cd.Behaviors.Add(new MyInstanceProvider(findNDriveUnitOfWork, sessionManager, serviceType));
            }
        }
    }
}