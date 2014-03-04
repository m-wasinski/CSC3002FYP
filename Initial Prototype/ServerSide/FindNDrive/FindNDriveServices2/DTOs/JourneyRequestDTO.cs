// --------------------------------------------------------------------------------------------------------------------
// <copyright file="CarShareRequestDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The car share request dto.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.DTOs
{
    using System;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;
    using System.Web.Providers.Entities;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using User = DomainObjects.Domains.User;

    /// <summary>
    /// The car share request dto.
    /// </summary>
    [DataContract]
    public class JourneyRequestDTO
    {
        /// <summary>
        /// Gets or sets the car share request id.
        /// </summary>
        [DataMember]
        [ConcurrencyCheck]
        public int JourneyRequestId { get; set; }

        /// <summary>
        /// Gets or sets the car share id.
        /// </summary>
        [DataMember]
        public int JourneyId { get; set; }

        /// <summary>
        /// Gets or sets the car share.
        /// </summary>
        public virtual Journey Journey { get; set; }

        /// <summary>
        /// Gets or sets the user.
        /// </summary>
        [DataMember]
        public User FromUser { get; set; }

        /// <summary>
        /// Gets or sets the message.
        /// </summary>
        [DataMember]
        public string Message { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether read.
        /// </summary>
        [DataMember]
        public bool Read { get; set; }

        /// <summary>
        /// Gets or sets the decision.
        /// </summary>
        [DataMember]
        public JourneyRequestDecision Decision { get; set; }

        /// <summary>
        /// Gets or sets the sent on date.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime SentOnDate { get; set; }

        /// <summary>
        /// Gets or sets the decided on date.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime DecidedOnDate { get; set; }
    }
}