// --------------------------------------------------------------------------------------------------------------------
// <copyright file="JourneyUserDTO.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the JourneyUserDTO type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The journey user dto.
    /// </summary>
    [DataContract]
    public class JourneyUserDTO
    {
        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the journey id.
        /// </summary>
        [DataMember]
        public int JourneyId { get; set; }
    }
}