// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ChatMessageRetrieverDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The chat message retriever dto.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The chat message retriever dto.
    /// </summary>
    [DataContract]
    public class ChatMessageRetrieverDTO
    {
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
        /// Gets or sets the load range dto.
        /// </summary>
        [DataMember]
        public LoadRangeDTO LoadRangeDTO { get; set; }
    }
}