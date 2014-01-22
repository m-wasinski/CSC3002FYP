﻿// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IMessengerService.cs" company="">
//   
// </copyright>
// <summary>
//   The MessengerService interface.
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
    /// The MessengerService interface.
    /// </summary>
    [ServiceContract]
    public interface IMessengerService
    {
        /// <summary>
        /// The send new message.
        /// </summary>
        /// <param name="carShareMessageDTO">
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
        ServiceResponse<bool> SendMessage(ChatMessageDTO chatMessageDTO);

        /// <summary>
        /// The retrieve messages.
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
            UriTemplate = "/getall")]
        ServiceResponse<List<ChatMessage>> RetrieveMessages(ChatMessageRetrieverDTO chatMessageRetrieverDTO);

        /// <summary>
        /// The get unread messages count.
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
            UriTemplate = "/getunreadcount")]
        ServiceResponse<int> GetUnreadMessagesCount(int userId);

        /// <summary>
        /// The get unread messages for friend.
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
            UriTemplate = "/getunreadcountforfriend")]
        ServiceResponse<int> GetUnreadMessagesCountForFriend(ChatMessageRetrieverDTO chatMessageRetrieverDTO);

        /// <summary>
        /// The mark messages as read for friend.
        /// </summary>
        /// <param name="chatMessageDtos">
        /// The chat Message Dtos.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/markasreadforfriend")]
        ServiceResponse<bool> MarkMessagesAsRead(List<ChatMessageDTO> chatMessageDtos);
    }
}