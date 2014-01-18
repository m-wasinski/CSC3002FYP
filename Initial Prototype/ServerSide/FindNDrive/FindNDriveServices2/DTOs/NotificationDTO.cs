// --------------------------------------------------------------------------------------------------------------------
// <copyright file="NotificationDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The notification dto.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The notification dto.
    /// </summary>
    [DataContract]
    public class NotificationDTO
    {
        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the notification id.
        /// </summary>
        [DataMember]
        public int NotificationId { get; set; }
    }
}