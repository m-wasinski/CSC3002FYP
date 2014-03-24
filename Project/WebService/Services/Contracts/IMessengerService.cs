// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IMessengerService.cs" company="">
//   
// </copyright>
// <summary>
//   The MessengerService interface.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.Contracts
{
    using System.Collections.Generic;
    using System.ServiceModel;
    using System.ServiceModel.Web;

    using DomainObjects.Domains;

    using global::Services.DTOs;
    using global::Services.ServiceResponses;

    /// <summary>
    /// The MessengerService interface.
    /// </summary>
    [ServiceContract]
    public interface IMessengerService
    {
        /// <summary>
        /// Sends a new chat message from one user to another.
        /// </summary>
        /// <param name="chatMessageDTO">
        /// The chat message dto.
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
        ServiceResponse SendMessage(ChatMessageDTO chatMessageDTO);

        /// <summary>
        /// Retrieves a conversation history between two users.
        /// </summary>
        /// <param name="chatMessageRetrieverDTO">
        /// The chat message retriever dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/all")]
        ServiceResponse<List<ChatMessage>> RetrieveMessages(ChatMessageRetrieverDTO chatMessageRetrieverDTO);

        /// <summary>
        /// Retrieves the count of all unread messages for a given user.
        /// </summary>
        /// <param name="userId">
        /// The user id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/unreadcount")]
        ServiceResponse<int> GetUnreadMessagesCount(int userId);

        /// <summary>
        /// Retrieves the number of unread messages for a specific friend.
        /// </summary>
        /// <param name="chatMessageRetrieverDTO">
        /// The chat message retriever dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/unreadcountfriend")]
        ServiceResponse<int> GetUnreadMessagesCountForFriend(ChatMessageRetrieverDTO chatMessageRetrieverDTO);

        /// <summary>
        /// Retrieves only unread messages for a given conversation history.
        /// </summary>
        /// <param name="chatMessageRetrieverDTO">
        /// The chat message retriever dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST", 
            ResponseFormat = WebMessageFormat.Json, 
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/unread")]
        ServiceResponse<List<ChatMessage>> GetUnreadMessages(ChatMessageRetrieverDTO chatMessageRetrieverDTO);

        /// <summary>
        /// Marks a given message as read.
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
            UriTemplate = "/read")]
        ServiceResponse MarkAsRead(int id);

        /// <summary>
        /// The get message by id.
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
        ServiceResponse<ChatMessage> GetMessageById(int id);
    }
}