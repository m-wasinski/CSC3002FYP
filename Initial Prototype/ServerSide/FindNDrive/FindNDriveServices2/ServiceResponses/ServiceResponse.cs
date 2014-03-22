using System.Collections.Generic;
using System.Runtime.Serialization;

namespace FindNDriveServices2.ServiceResponses
{
    /// <summary>
    /// Represents a service response which will be returned from all service operations.
    /// This is used a mechanism for handling error scenarios in a consistent manner.
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
        public ServiceResponseCode ServiceResponseCode { get; set; }

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
