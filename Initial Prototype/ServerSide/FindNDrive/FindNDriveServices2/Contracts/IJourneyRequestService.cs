// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IRequestService.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the IRequestService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Contracts
{
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
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
        /// The send request.
        /// </summary>
        /// <param name="journeyRequestDTO">
        /// The car share request dto.
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
        ServiceResponse<JourneyRequest> SendRequest(JourneyRequestDTO journeyRequestDTO);

        /// <summary>
        /// The make decision.
        /// </summary>
        /// <param name="journeyRequestDTO">
        /// The car share request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/processdecision")]
        ServiceResponse<JourneyRequest> ProcessDecision(JourneyRequestDTO journeyRequestDTO);

        /// <summary>
        /// The mark as read.
        /// </summary>
        /// <param name="carShareRequestDTO">
        /// The car share request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/markasread")]
        ServiceResponse<JourneyRequest> MarkAsRead(int id);

        /// <summary>
        /// The get all requests for journey.
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
            UriTemplate = "/getforjourney")]
        ServiceResponse<List<JourneyRequest>> GetAllRequestsForJourney(int id);

        /// <summary>
        /// The get all requests for user.
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
            UriTemplate = "/getforuser")]
        ServiceResponse<List<JourneyRequest>> GetAllRequestsForUser(int id);
    }
}