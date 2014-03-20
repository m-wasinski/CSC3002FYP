// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IJourneyChatService.cs" company="">
//   
// </copyright>
// <summary>
//   The JourneyChatService interface.
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
    /// The JourneyChatService interface.
    /// </summary>
    [ServiceContract]
    public interface IJourneyChatService
    {
        /// <summary>
        /// Sends new message in a given journey chat room.
        /// </summary>
        /// <param name="journeyMessageDTO">
        /// The journey message dto.
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
        ServiceResponse SendMessage(JourneyMessageDTO journeyMessageDTO);

        /// <summary>
        /// The retrieve messages.
        /// </summary>
        /// <param name="journeyMessageRetrieverDTO">
        /// The journey message retriever dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/getall")]
        ServiceResponse<List<JourneyMessage>> RetrieveMessages(JourneyMessageRetrieverDTO journeyMessageRetrieverDTO);

        /// <summary>
        /// Retrieves a count of unread messages for a given journey.
        /// </summary>
        /// <param name="journeyMessageRetrieverDTO">
        /// The journey message retriever dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/getunreadcount")]
        ServiceResponse<int> GetUnreadMessagesCount(JourneyMessageRetrieverDTO journeyMessageRetrieverDTO);

        /// <summary>
        /// Retrieves all unread messages for a given journey.
        /// </summary>
        /// <param name="journeyMessageRetrieverDTO">
        /// The journey message retriever dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/getunread")]
        ServiceResponse<List<JourneyMessage>> RetrieveUnreadMessages(JourneyMessageRetrieverDTO journeyMessageRetrieverDTO);

        /// <summary>
        /// Marks a given message as read.
        /// </summary>
        /// <param name="journeyMessageMarkerDTO">
        /// The journey Message Marker DTO.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST", 
            ResponseFormat = WebMessageFormat.Json, 
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare, 
            UriTemplate = "/markread")]
        ServiceResponse MarkAsRead(JourneyMessageMarkerDTO journeyMessageMarkerDTO);

        /// <summary>
        /// The get journey message by id.
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
        ServiceResponse<JourneyMessage> GetJourneyMessageById(int id);
    }
}