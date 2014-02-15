// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Message.cs" company="">
//   
// </copyright>
// <summary>
//   The car share message.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;

    /// <summary>
    /// The car share message.
    /// </summary>
    [DataContract]
    public class JourneyMessage
    {
        /// <summary>
        /// Gets or sets the car share message id.
        /// </summary>
        [DataMember]
        [ConcurrencyCheck]
        public int JourneyMessageId { get; set; }

        /// <summary>
        /// Gets or sets the car share id.
        /// </summary>
        [DataMember]
        public int JourneyId { get; set; }

        /// <summary>
        /// Gets or sets the sender id.
        /// </summary>
        [DataMember]
        public int SenderId { get; set; }

        /// <summary>
        /// Gets or sets the sender username.
        /// </summary>
        [DataMember]
        public string SenderUsername { get; set; }

        /// <summary>
        /// Gets or sets the message body.
        /// </summary>
        [DataMember]
        public string MessageBody { get; set; }

        /// <summary>
        /// Gets or sets the sent on date.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime SentOnDate { get; set; }

        /// <summary>
        /// Gets or sets the seen by.
        /// </summary>
        public virtual ICollection<User> SeenBy { get; set; }  
    }
}
