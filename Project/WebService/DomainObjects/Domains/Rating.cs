// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Rating.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the Rating type.
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
    /// The rating.
    /// </summary>
    [DataContract]
    public class Rating
    {
        /// <summary>
        /// Gets or sets the rating id.
        /// </summary>
        [DataMember]
        [ConcurrencyCheck]
        public int RatingId { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the user.
        /// </summary>
        public User TargetUser { get; set; }

        /// <summary>
        /// Gets or sets the from user name.
        /// </summary>
        [DataMember]
        public User FromUser { get; set; }

        /// <summary>
        /// Gets or sets the left on date.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime LeftOnDate { get; set; }

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
