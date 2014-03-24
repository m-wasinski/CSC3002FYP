// --------------------------------------------------------------------------------------------------------------------
// <copyright file="FriendRequest.cs" company="">
//   
// </copyright>
// <summary>
//   The friend request dto.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
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
    public class FriendRequest
    {
        /// <summary>
        /// Gets or sets the friend request id.
        /// </summary>
        [DataMember]
        [ConcurrencyCheck]
        public int FriendRequestId { get; set; }

        /// <summary>
        /// Gets or sets the target user name.
        /// </summary>
        [DataMember]
        public User FromUser { get; set; }

        /// <summary>
        /// Gets or sets the requesting user id.
        /// </summary>
        [DataMember]
        public User ToUser { get; set; }

        /// <summary>
        /// Gets or sets the message.
        /// </summary>
        [DataMember]
        public string Message { get; set; }

        /// <summary>
        /// Gets or sets the friend request decision.
        /// </summary>
        [DataMember]
        public Decision Decision { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether read.
        /// </summary>
        [DataMember]
        public bool Read { get; set; }

        /// <summary>
        /// Gets or sets the sent on date.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime SentOnDate { get; set; }

        /// <summary>
        /// Gets or sets the decided on date.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime DecidedOnDate { get; set; }
    }
}