// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IFriendsService.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the FriendsService type.
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
    /// The friends service.
    /// </summary>
    [ServiceContract]
    public interface IFriendsService
    {
        /// <summary>
        /// The add travel buddy.
        /// </summary>
        /// <param name="friendRequestDTO">
        /// The friend Request DTO.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/processdecision")]
        ServiceResponse<bool> ProcessDecision(FriendRequestDTO friendRequestDTO);

        /// <summary>
        /// The send request.
        /// </summary>
        /// <param name="friendRequestDTO">
        /// The friend request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST", ResponseFormat = WebMessageFormat.Json, BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/sendrequest")]
        ServiceResponse<bool> SendRequest(FriendRequestDTO friendRequestDTO);

        /// <summary>
        /// The get travel buddies.
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
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/getfriends")]
        ServiceResponse<List<User>> GetFriends(int userId);

        /// <summary>
        /// The get friend request.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(
            Method = "POST",
            UriTemplate = "/getrequest",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare)]
        ServiceResponse<FriendRequest> GetFriendRequest(int id);

        /// <summary>
        /// The delete friend.
        /// </summary>
        /// <param name="friendDeletionDTO">
        /// The friend deletion dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(
            Method = "POST",
            UriTemplate = "/delete",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare)]
        ServiceResponse<bool> DeleteFriend(FriendDeletionDTO friendDeletionDTO);
    }
}