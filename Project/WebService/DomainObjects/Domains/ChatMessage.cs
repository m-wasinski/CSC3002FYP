// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ChatMessage.cs" company="">
//   
// </copyright>
// <summary>
//   The chat message.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;

    /// <summary>
    /// The chat message.
    /// </summary>
    [DataContract]
    public class ChatMessage
    {
        /// <summary>
        /// Gets or sets the chat message id.
        /// </summary>
        [DataMember]
        [ConcurrencyCheck]
        public int ChatMessageId { get; set; }

        /// <summary>
        /// Gets or sets the sender id.
        /// </summary>
        [DataMember]
        public int SenderId { get; set; }

        /// <summary>
        /// Gets or sets the recipient id.
        /// </summary>
        [DataMember]
        public int RecipientId { get; set; }

        /// <summary>
        /// Gets or sets the message body.
        /// </summary>
        [DataMember]
        [Required(ErrorMessage = "Message is required.")]
        [StringLength(1, MinimumLength = 250,
            ErrorMessage = "Message must be smaller than 250 characters.")]
        public string MessageBody { get; set; }

        /// <summary>
        /// Gets or sets the sent on date.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime SentOnDate { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether read.
        /// </summary>
        [DataMember]
        public bool Read { get; set; }

        /// <summary>
        /// Gets or sets the recipient user name.
        /// </summary>
        [DataMember]
        public string RecipientUserName { get; set; }

        /// <summary>
        /// Gets or sets the sender user name.
        /// </summary>
        [DataMember]
        public string SenderUserName { get; set; }
    }
}
