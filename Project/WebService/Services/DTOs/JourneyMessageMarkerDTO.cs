// --------------------------------------------------------------------------------------------------------------------
// <copyright file="JourneyMessageMarkerDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The journey message marker dto.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The journey message marker dto.
    /// </summary>
    [DataContract]
    public class JourneyMessageMarkerDTO
    {
        /// <summary>
        /// Gets or sets the journey message id.
        /// </summary>
        [DataMember]
        public int JourneyMessageId { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }
    }
}