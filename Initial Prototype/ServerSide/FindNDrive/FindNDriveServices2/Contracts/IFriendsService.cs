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
        /// <param name="user">
        /// The user.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/add")]
        ServiceResponse<bool> AddFriend(FriendDTO user);

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
            UriTemplate = "/get")]
        ServiceResponse<List<User>> GetFriends(int userId);

    }
}