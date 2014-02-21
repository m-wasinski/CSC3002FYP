// --------------------------------------------------------------------------------------------------------------------
// <copyright file="NotificationMarkerDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The notification marker dto.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The notification marker dto.
    /// </summary>
    [DataContract]
    public class NotificationMarkerDTO
    {
        /// <summary>
        /// Gets or sets the notification id.
        /// </summary>
        [DataMember]
        public int NotificationId { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }
    }
}