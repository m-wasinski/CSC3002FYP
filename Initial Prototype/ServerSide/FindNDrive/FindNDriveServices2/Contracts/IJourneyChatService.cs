// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IGroupChat.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the IGroupChat type.
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
    /// The group chat.
    /// </summary>
    [ServiceContract]
    public interface IJourneyChatService
    {
        /// <summary>
        /// The send new message.
        /// </summary>
        /// <param name="journeyMessageDTO">
        /// The car share message dto.
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
        ServiceResponse<bool> SendMessage(JourneyMessageDTO journeyMessageDTO);

        /// <summary>
        /// The send new message.
        /// </summary>
        /// <param name="journeyId">
        /// The car Share Id.
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
        /// The get unread messages count.
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
        /// The retrieve unread messages.
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
        /// The mark as read.
        /// </summary>
        /// <param name="journeyMessageMarkerDTO">
        /// The journey Message Marker DTO.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST", ResponseFormat = WebMessageFormat.Json, RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare, UriTemplate = "/markread")]
        ServiceResponse<bool> MarkAsRead(JourneyMessageMarkerDTO journeyMessageMarkerDTO);

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
        [WebInvoke(Method = "POST", ResponseFormat = WebMessageFormat.Json, RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare, UriTemplate = "/get")]
        ServiceResponse<JourneyMessage> GetJourneyMessageById(int id);
    }
}