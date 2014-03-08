// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Journey.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the Journey type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;
    using DomainObjects.Constants;

    using FindNDriveServices2.DTOs;

    /// <summary>
    /// The car share.
    /// </summary>
    [DataContract]
    public class Journey
    {
        /// <summary>
        /// Gets or sets the car share id.
        /// </summary>
        [DataMember] 
        [ConcurrencyCheck]
        public int JourneyId { get; set; }

        /// <summary>
        /// Gets or sets the driver.
        /// </summary>
        [DataMember]
        public virtual User Driver { get; set; }

        /// <summary>
        /// Gets or sets the travel locations.
        /// </summary>
        [DataMember]
        public virtual List<GeoAddress> GeoAddresses { get; set; }

        /// <summary>
        /// Gets or sets the time of departure.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime DateAndTimeOfDeparture { get; set; }

        /// <summary>
        /// Gets or sets the description.
        /// </summary>
        [DataMember]
        public string Description { get; set; }

        /// <summary>
        /// Gets or sets the fee.
        /// </summary>
        [DataMember]
        public double Fee { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether pets allowed.
        /// </summary>
        [DataMember]
        public bool Pets { get; set; }

        /// <summary>
        /// Gets or sets the available seats.
        /// </summary>
        [DataMember]
        public int AvailableSeats { get; set; }

        /// <summary>
        /// Gets or sets the participants.
        /// </summary>
        public virtual ICollection<User> Participants { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether smokers allowed.
        /// </summary>
        [DataMember]
        public bool Smokers { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether private.
        /// </summary>
        [DataMember]
        public bool Private { get; set; }

        /// <summary>
        /// Gets or sets the vehicle type.
        /// </summary>
        [DataMember]
        public VehicleTypes VehicleType { get; set; }

        /// <summary>
        /// Gets or sets the unread requests count.
        /// </summary>
        [DataMember]
        public int UnreadRequestsCount { get; set; }

        /// <summary>
        /// Gets or sets the car share status.
        /// </summary>
        [DataMember]
        public JourneyStatus JourneyStatus { get; set; }

        /// <summary>
        /// Gets or sets the requests.
        /// </summary>
        public virtual ICollection<JourneyRequest> Requests { get; set; }

        /// <summary>
        /// Gets or sets the creation date.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime CreationDate { get; set; }

        /// <summary>
        /// Gets or sets the preferred payment method.
        /// </summary>
        [DataMember]
        public string PreferredPaymentMethod { get; set; }

        /// <summary>
        /// Gets or sets the unread messages count.
        /// </summary>
        [DataMember]
        [NotMapped]
        public int UnreadMessagesCount { get; set; }
    }
}
