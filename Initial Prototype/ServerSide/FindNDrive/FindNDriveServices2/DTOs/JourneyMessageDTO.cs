// --------------------------------------------------------------------------------------------------------------------
// <copyright file="CarShareMessage.cs" company="">
//   
// </copyright>
// <summary>
//   The car share message.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.DTOs
{
    using System.Collections.Generic;
    using System.Runtime.Serialization;

    /// <summary>
    /// The car share message.
    /// </summary>
    [DataContract]
    public class JourneyMessageDTO
    {
        /// <summary>
        /// Gets or sets the car share message id.
        /// </summary>
        [DataMember]
        public int JourneyMessageId { get; set; }

        /// <summary>
        /// Gets or sets the car share id.
        /// </summary>
        [DataMember]
        public int JourneyId { get; set; }

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
