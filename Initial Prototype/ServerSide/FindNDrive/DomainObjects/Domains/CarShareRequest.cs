// --------------------------------------------------------------------------------------------------------------------
// <copyright file="CarShareRequest.cs" company="">
//   
// </copyright>
// <summary>
//   The car share request.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;
    using DomainObjects.Constants;
    using DomainObjects.DOmains;

    /// <summary>
    /// The car share request.
    /// </summary>
    [DataContract]
    public class CarShareRequest
    {
        /// <summary>
        /// Gets or sets the car share request id.
        /// </summary>
        [DataMember]
        public int CarShareRequestId { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether add to travel buddies.
        /// </summary>
        [DataMember]
        public bool AddToTravelBuddies { get; set; }

        /// <summary>
        /// Gets or sets the car share id.
        /// </summary>
        [DataMember]
        public int CarShareId { get; set; }

        /// <summary>
        /// Gets or sets the car share.
        /// </summary>
        [ForeignKey("CarShareId")]
        public virtual CarShare CarShare { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the user.
        /// </summary>
        [DataMember]
        [ForeignKey("UserId")]
        public virtual User User { get; set; }

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
        public CarShareRequestDecision Decision { get; set; }

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
