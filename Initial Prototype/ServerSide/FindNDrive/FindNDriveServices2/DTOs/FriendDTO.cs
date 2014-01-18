// --------------------------------------------------------------------------------------------------------------------
// <copyright file="TravelBuddyDTO.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the TravelBuddyDTO type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The travel buddy dto.
    /// </summary>
    [DataContract]
    public class FriendDTO
    {
        /// <summary>
        /// Gets or sets the target user id.
        /// </summary>
        [DataMember]
        public int TargetUserId { get; set; }

        /// <summary>
        /// Gets or sets the travel buddy user id.
        /// </summary>
        [DataMember]
        public int FriendUserId { get; set; }
    }
}