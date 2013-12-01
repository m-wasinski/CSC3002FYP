using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace FindNDriveServices2.Contracts
{
    using System.Security.Permissions;
    using System.ServiceModel;
    using System.ServiceModel.Web;
    using DomainObjects.DOmains;
    using DomainObjects.Domains;

    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The i search service.
    /// </summary>
    [ServiceContract]
    public interface ISearchService
    {
        /// <summary>
        /// The search car shares.
        /// </summary>
        /// <param name="login">
        /// The login.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/searchcarshare")]
        ServiceResponse<List<CarShare>> SearchCarShares(CarShareDTO carShare);
    }
}