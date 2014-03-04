// --------------------------------------------------------------------------------------------------------------------
// <copyright file="MyServiceHost.cs" company="">
//   
// </copyright>
// <summary>
//   The my service host.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.ServiceHostUtils
{
    using System;
    using System.ServiceModel;

    /// <summary>
    /// The my service host.
    /// </summary>
    public class MyServiceHost : ServiceHost
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="MyServiceHost"/> class.
        /// </summary>
        /// <param name="serviceType">
        /// The service type.
        /// </param>
        /// <param name="baseAddresses">
        /// The base addresses.
        /// </param>
        /// <exception cref="ArgumentNullException">
        /// </exception>
        public MyServiceHost(Type serviceType, params Uri[] baseAddresses)
            : base(serviceType, baseAddresses)
        {
            foreach (var cd in this.ImplementedContracts.Values)
            {
                cd.Behaviors.Add(new MyInstanceProvider(serviceType));
            }
        }
    }
}