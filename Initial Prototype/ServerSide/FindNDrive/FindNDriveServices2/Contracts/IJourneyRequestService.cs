// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IJourneyRequestService.cs" company="">
//   
// </copyright>
// <summary>
//   The RequestService interface.
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
    /// The RequestService interface.
    /// </summary>
    [ServiceContract]
    public interface IJourneyRequestService
    {
        /// <summary>
        /// Send a new request for a given journey.
        /// </summary>
        /// <param name="journeyRequestDTO">
        /// The journey request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/send")]
        ServiceResponse SendRequest(JourneyRequestDTO journeyRequestDTO);

        /// <summary>
        /// Processes decision submitted by the user for a given journey request.
        /// </summary>
        /// <param name="journeyRequestDTO">
        /// The journey request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/process")]
        ServiceResponse ProcessDecision(JourneyRequestDTO journeyRequestDTO);

        /// <summary>
        /// Retrieves all requests for a given journey.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/journey")]
        ServiceResponse<List<JourneyRequest>> GetAllRequestsForJourney(int id);

        /// <summary>
        /// Retrieves all journey requests for a given user.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/user")]
        ServiceResponse<List<JourneyRequest>> GetAllRequestsForUser(int id);

        /// <summary>
        /// Retrieves a journey request by its id.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/get")]
        ServiceResponse<JourneyRequest> GetJourneyRequest(int id);
    }
}