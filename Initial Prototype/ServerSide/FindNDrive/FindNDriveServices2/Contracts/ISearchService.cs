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
        /// Performs a search for journeys.
        /// </summary>
        /// <param name="journeyTemplateDTO">
        /// The journey Search DTO.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/searchforjourney")]
        ServiceResponse<List<Journey>> SearchForJourneys(JourneyTemplateDTO journeyTemplateDTO);
    }
}