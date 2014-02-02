// --------------------------------------------------------------------------------------------------------------------
// <copyright file="GCMNotification.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the GCMNotification type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System.ComponentModel.DataAnnotations;
    using System.Runtime.Serialization;
    using DomainObjects.Constants;

    /// <summary>
    /// The gcm notification.
    /// </summary>
    [DataContract]
    public class GCMNotification
    {
        /// <summary>
        /// Gets or sets the gcm notification id.
        /// </summary>
        [DataMember]
        [ConcurrencyCheck]
        public int GCMNotificationId { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the notification type.
        /// </summary>
        [DataMember]
        public GCMNotificationType NotificationType { get; set; }

        /// <summary>
        /// Gets or sets the ContentTitle title.
        /// </summary>
        [DataMember]
        public string ContentTitle { get; set; }

        /// <summary>
        /// Gets or sets the notification message.
        /// </summary>
        [DataMember]
        public string NotificationMessage { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether delivered.
        /// </summary>
        [DataMember]
        public bool Delivered { get; set; }
    }
}
