// --------------------------------------------------------------------------------------------------------------------
// <copyright file="CarShareMessage.cs" company="">
//   
// </copyright>
// <summary>
//   The car share message.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System.Collections.Generic;
    using System.Runtime.Serialization;

    /// <summary>
    /// The car share message.
    /// </summary>
    [DataContract]
    public class CarShareMessage
    {
        /// <summary>
        /// Gets or sets the car share message id.
        /// </summary>
        [DataMember]
        public int CarShareMessageId { get; set; }

        /// <summary>
        /// Gets or sets the message body.
        /// </summary>
        [DataMember]
        public string MessageBody { get; set; }

        /// <summary>
        /// Gets or sets the read by users.
        /// </summary>
        [DataMember]
        public List<int> ReadByUsers { get; set; } 
    }
}
