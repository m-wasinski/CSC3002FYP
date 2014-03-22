using System;
using System.Runtime.Serialization;

namespace FindNDriveServices2.ServiceResponses
{
    /// <summary>
    /// The service response code returned in the service response.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum ServiceResponseCode
    {
        /// <summary>
        /// Response code in the scenario of Failure. For instance if a Find operation returns no matches.
        /// </summary>
        [EnumMember(Value = "Success")]
        Success = 0,

        /// <summary>
        /// Response code in the scenario of success. For instance no issues occurred with validation, or data retrieval.
        /// </summary>
        [EnumMember(Value = "Failure")]
        Failure = 1,

        /// <summary>
        /// Response code in the scenario of success. For instance no issues occurred with validation, or data retrieval.
        /// </summary>
        [EnumMember(Value = "Unauthorized")]
        Unauthorized = 2
    }
}
