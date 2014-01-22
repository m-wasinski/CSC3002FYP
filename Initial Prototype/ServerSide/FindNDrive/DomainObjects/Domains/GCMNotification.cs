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
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;

    using Newtonsoft.Json;

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
        public int NotificationType { get; set; }

        /// <summary>
        /// Gets or sets the notification arguments.
        /// </summary>
        [DataMember]
        public int NotificationArguments { get; set; }

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
