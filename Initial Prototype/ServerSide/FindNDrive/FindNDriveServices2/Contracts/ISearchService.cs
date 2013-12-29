// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ISearchService.cs" company="">
//   
// </copyright>
// <summary>
//   The i search service.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Contracts
{
    using System.Collections.Generic;
    using System.ServiceModel;
    using System.ServiceModel.Web;
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
        /// <param name="carShare">
        /// The car share.
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