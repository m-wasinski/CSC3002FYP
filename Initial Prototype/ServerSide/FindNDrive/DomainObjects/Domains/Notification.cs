// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Notification.cs" company="">
//   
// </copyright>
// <summary>
//   The notification.
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
    /// The notification.
    /// </summary>
    [DataContract]
    public class Notification
    {
        /// <summary>
        /// Gets or sets the notification id.
        /// </summary>
        [DataMember]
        [ConcurrencyCheck]
        public int NotificationId { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the user.
        /// </summary>
        public User User { get; set; }

        /// <summary>
        /// Gets or sets the profile picture id.
        /// </summary>
        [DataMember]
        public int ProfilePictureId { get; set; }

        /// <summary>
        /// Gets or sets the collapsible key.
        /// </summary>
        [DataMember]
        public int CollapsibleKey { get; set; }

        /// <summary>
        /// Gets or sets the notification type.
        /// </summary>
        [DataMember]
        public NotificationType NotificationType { get; set; }

        /// <summary>
        /// Gets or sets the notification content type.
        /// </summary>
        [DataMember]
        public NotificationContentType NotificationContentType { get; set; }

        /// <summary>
        /// Gets or sets the notification title.
        /// </summary>
        [DataMember]
        public string NotificationTitle { get; set; }

        /// <summary>
        /// Gets or sets the notification message.
        /// </summary>
        [DataMember]
        public string NotificationMessage { get; set; }

        /// <summary>
        /// Gets or sets the notification payload.
        /// </summary>
        [DataMember]
        public int TargetObjectId { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether read.
        /// </summary>
        [DataMember]
        public bool Delivered { get; set; }

        /// <summary>
        /// Gets or sets the received on date.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime ReceivedOnDate { get; set; }
    }
}
