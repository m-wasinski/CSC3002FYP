// --------------------------------------------------------------------------------------------------------------------
// <copyright file="JourneyMessageRetrieverDTO.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the JourneyMessageRetrieverDTO type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The journey message retriever dto.
    /// </summary>
    [DataContract]
    public class JourneyMessageRetrieverDTO
    {
        /// <summary>
        /// Gets or sets the journey id.
        /// </summary>
        [DataMember]
        public int JourneyId { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the load range dto.
        /// </summary>
        [DataMember]
        public LoadRangeDTO LoadRangeDTO { get; set; }
    }
}