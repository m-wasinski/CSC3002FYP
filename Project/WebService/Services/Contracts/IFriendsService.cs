// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IFriendsService.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the FriendsService type.
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
    /// The friends service.
    /// </summary>
    [ServiceContract]
    public interface IFriendsService
    {
        /// <summary>
        /// Processes the decision submitted by the user for given friend request.
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
            UriTemplate = "/process")]
        ServiceResponse ProcessDecision(FriendRequestDTO friendRequestDTO);

        /// <summary>
        /// Sends a new friend request to a given user.
        /// </summary>
        /// <param name="friendRequestDTO">
        /// The friend request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST", 
            ResponseFormat = WebMessageFormat.Json, 
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/sendrequest")]
        ServiceResponse SendRequest(FriendRequestDTO friendRequestDTO);

        /// <summary>
        /// Retrieves list of user's friends.
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
        /// Retrieves a list of friend requests for a given user.
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
        /// Deletes a friend from a given user's friend list.
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
        ServiceResponse DeleteFriend(FriendDeletionDTO friendDeletionDTO);
    }
}