using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FindNDriveServices.ServiceResponses
{
    /// <summary>
    /// The service response code value returned in ServiceResponse.
    /// </summary>
    public enum ServiceResponseCode
    {
        /// <summary>
        /// Response code in the scenario of Failure. For instance if a Find operation returns no matches.
        /// </summary>
        Failure,

        /// <summary>
        /// Response code in the scenario of success. For instance no issues occurred with validation, or data retrieval.
        /// </summary>
        Success
    }
}
