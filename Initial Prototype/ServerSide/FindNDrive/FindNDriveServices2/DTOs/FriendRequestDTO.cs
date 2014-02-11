// --------------------------------------------------------------------------------------------------------------------
// <copyright file="FriendRequestDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The friend request dto.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.DTOs
{
    using System;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;

    using DomainObjects.Constants;

    /// <summary>
    /// The friend request dto.
    /// </summary>
    [DataContract]
    public class FriendRequestDTO
    {
        /// <summary>
        /// Gets or sets the friend request id.
        /// </summary>
        [DataMember]
        public int FriendRequestId { get; set; }

        /// <summary>
        /// Gets or sets the target user id.
        /// </summary>
        [DataMember]
        public int TargetUserId { get; set; }

        /// <summary>
        /// Gets or sets the target user name.
        /// </summary>
        [DataMember]
        public string TargetUserName { get; set; }

        /// <summary>
        /// Gets or sets the requesting user id.
        /// </summary>
        [DataMember]
        public int RequestingUserId { get; set; }

        /// <summary>
        /// Gets or sets the requesting user name.
        /// </summary>
        [DataMember]
        public string RequestingUserName { get; set; }

        /// <summary>
        /// Gets or sets the message.
        /// </summary>
        [DataMember]
        public string Message { get; set; }

        /// <summary>
        /// Gets or sets the friend request decision.
        /// </summary>
        [DataMember]
        public FriendRequestDecision FriendRequestDecision { get; set; }
    }
}