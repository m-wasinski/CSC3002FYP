// --------------------------------------------------------------------------------------------------------------------
// <copyright file="CarShare.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the CarShare type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Dynamic;
    using System.Runtime.Serialization;
    using DomainObjects.Constants;
    using DomainObjects.DOmains;

    /// <summary>
    /// The car share.
    /// </summary>
    [DataContract]
    public class CarShare
    {
        /// <summary>
        /// Gets or sets the car share id.
        /// </summary>
        [DataMember] 
        public int CarShareId { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>F
        [Required]
        [DataMember]
        public int DriverId { get; set; }

        /// <summary>
        /// Gets or sets the driver.
        /// </summary>
        [DataMember]
        [ForeignKey("DriverId")]
        public virtual User Driver { get; set; }

        /// <summary>
        /// Gets or sets the departure city.
        /// </summary>
        [DataMember] 
        [Required(ErrorMessage = "Departure city cannot be empty.")]
        public string DepartureCity { get; set; }

        /// <summary>
        /// Gets or sets the destination city.
        /// </summary>
        [DataMember] 
        [Required]
        public string DestinationCity { get; set; }

        /// <summary>
        /// Gets or sets the time of departure.
        /// </summary>
        [Required(ErrorMessage = "You must specify the departure date and time.")]
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime DateAndTimeOfDeparture { get; set; }

        /// <summary>
        /// Gets or sets the description.
        /// </summary>
        [Required]
        [DataMember]
        public string Description { get; set; }

        /// <summary>
        /// Gets or sets the fee.
        /// </summary>
        [Required]
        [DataMember]
        public double Fee { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether pets allowed.
        /// </summary>
        [Required]
        [DataMember]
        public bool PetsAllowed { get; set; }

        /// <summary>
        /// Gets or sets the available seats.
        /// </summary>
        [Required]
        [DataMember]
        public int AvailableSeats { get; set; }

        /// <summary>
        /// Gets or sets the participants.
        /// </summary>
        [DataMember]
        public virtual ICollection<User> Participants { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether smokers allowed.
        /// </summary>
        [Required]
        [DataMember]
        public bool SmokersAllowed { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether private.
        /// </summary>
        [Required]
        [DataMember]
        public bool Private { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether women only.
        /// </summary>
        [Required]
        [DataMember]
        public bool WomenOnly { get; set; }

        /// <summary>
        /// Gets or sets the vehicle type.
        /// </summary>
        [Required]
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
        public CarShareStatus CarShareStatus { get; set; }

        /// <summary>
        /// Gets or sets the requests.
        /// </summary>
        public virtual ICollection<CarShareRequest> Requests { get; set; }

        /// <summary>
        /// Gets or sets the messages.
        /// </summary>
        [DataMember]
        public virtual ICollection<CarShareMessage> Messages { get; set; } 
    }
}
