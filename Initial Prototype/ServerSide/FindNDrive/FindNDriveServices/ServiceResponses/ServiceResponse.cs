using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FindNDriveServices.ServiceResponses
{
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Runtime.Serialization;

    /// <summary>
    /// Represents a service response which will be returned from all service operations.
    /// This is used a mechanism for handling error scenarios in a consistent manner.
    /// For instance if there is a validation error, this will be determinable by the ServiceResponseCode,
    /// rather than a SoapFault/Exception.
    /// </summary>
    [DataContract]
    [KnownType(typeof(ServiceResponse<object>))]
    [KnownType(typeof(List<string>))]
    public class ServiceResponse
    {
        /// <summary>
        /// Gets or sets the overall Service Response Code. This should be checked for Success before attempting
        /// to interact with any additional Result properties.
        /// </summary>
        [DataMember]
        public ServiceResponseCode ServiceReponseCode { get; set; }

        /// <summary>
        /// Gets or sets the error messages.
        /// </summary>
        [DataMember]
        public List<string> ErrorMessages { get; set; }
    }

    /// <summary>
    /// The service response which contains an additional Result of type T.
    /// </summary>
    /// <typeparam name="T">The generic type of the responses' Result.
    /// </typeparam>
    [DataContract]
    public class ServiceResponse<T> : ServiceResponse
    {
        /// <summary>
        /// Gets or sets the result returned by the service layer. If the ResponseCode is set as Failure, it is likely this will be null.
        /// This will be specified otherwise on the operation contract documentation header.
        /// </summary>
        [DataMember]
        public T Result { get; set; }
    }
}
