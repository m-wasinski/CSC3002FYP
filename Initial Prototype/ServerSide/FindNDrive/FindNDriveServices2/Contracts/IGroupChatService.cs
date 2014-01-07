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
    using System.ServiceModel;
    using System.ServiceModel.Web;

    using DomainObjects.Domains;

    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The group chat.
    /// </summary>
    [ServiceContract]
    public interface IGroupChatService
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
            UriTemplate = "/sendmessage")]
        ServiceResponse<CarShare> SendNewMessage(CarShareMessageDTO carShareMessageDTO);

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
            UriTemplate = "/markasread")]
        ServiceResponse<CarShare> MarkAsRead(int carShareMessageId);

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
            UriTemplate = "/markasread")]
        ServiceResponse<CarShare> RetrieveMessages(int carShareId);
    }
}