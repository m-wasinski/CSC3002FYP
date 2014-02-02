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
        [ForeignKey("UserId")]
        [DataMember]
        public User User { get; set; }

        /// <summary>
        /// Gets or sets the notification body.
        /// </summary>
        [DataMember]
        public string NotificationBody { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether read.
        /// </summary>
        [DataMember]
        public bool Read { get; set; }

        /// <summary>
        /// Gets or sets the context.
        /// </summary>
        [DataMember]
        public NotificationContext Context { get; set; }

        /// <summary>
        /// Gets or sets the received on date.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime ReceivedOnDate { get; set; }
    }
}
