// --------------------------------------------------------------------------------------------------------------------
// <copyright file="RatingDTO.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the RatingDTO type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The rating dto.
    /// </summary>
    [DataContract]
    public class RatingDTO
    {
        /// <summary>
        /// Gets or sets the rating id.
        /// </summary>
        [DataMember]
        public int RatingId { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int TargetUserId { get; set; }

        /// <summary>
        /// Gets or sets the from user id.
        /// </summary>
        [DataMember]
        public int FromUserId { get; set; }

        /// <summary>
        /// Gets or sets the score.
        /// </summary>
        [DataMember]
        public int Score { get; set; }

        /// <summary>
        /// Gets or sets the feedback.
        /// </summary>
        [DataMember]
        public string Feedback { get; set; }
    }
}
