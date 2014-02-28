// --------------------------------------------------------------------------------------------------------------------
// <copyright file="JourneyDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The car share dto.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.DTOs
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    /// <summary>
    /// The car share dto.
    /// </summary>
    [DataContract]
    public class JourneyDTO
    {
        /// <summary>
        /// Gets or sets the car share id.
        /// </summary>
        [DataMember]
        public int JourneyId { get; set; }

        /// <summary>
        /// Gets or sets the driver.
        /// </summary>
        [DataMember]
        public User Driver { get; set; }

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
        public virtual string Description { get; set; }

        /// <summary>
        /// Gets or sets the fee.
        /// </summary>
        [DataMember]
        public double Fee { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether pets allowed.
        /// </summary>
        [DataMember]
        public bool PetsAllowed { get; set; }

        /// <summary>
        /// Gets or sets the available seats.
        /// </summary>
        [DataMember]
        public int AvailableSeats { get; set; }

        /// <summary>
        /// Gets or sets the participants.
        /// </summary>
        [DataMember]
        public ICollection<User> Participants { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether smokers allowed.
        /// </summary>
        [DataMember]
        public bool SmokersAllowed { get; set; }

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
        /// Gets or sets a value indicating whether search by date.
        /// </summary>
        [DataMember]
        public bool SearchByDate { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether search by time.
        /// </summary>
        [DataMember]
        public bool SearchByTime { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether free.
        /// </summary>
        [DataMember]
        public bool Free { get; set; }

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
        [DataMember]
        public ICollection<JourneyRequest> Requests { get; set; }

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
        /// Gets or sets the load range.
        /// </summary>
        public int LoadRange { get; set; }
    }
}
