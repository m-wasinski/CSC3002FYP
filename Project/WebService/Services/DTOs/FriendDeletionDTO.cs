// --------------------------------------------------------------------------------------------------------------------
// <copyright file="FriendDeletionDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The friend deletion dto.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The friend deletion dto.
    /// </summary>
    [DataContract]
    public class FriendDeletionDTO
    {
        /// <summary>
        /// The user id.
        /// </summary>
        [DataMember]
        public int UserId;

        /// <summary>
        /// The friend id.
        /// </summary>
        [DataMember]
        public int FriendId;
    }
}