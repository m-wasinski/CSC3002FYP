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
    using System.Runtime.Serialization;
    using DomainObjects.Constants;
    using DomainObjects.DOmains;

    /// <summary>
    /// The car share.
    /// </summary>
    public class CarShare
    {
        /// <summary>
        /// Gets or sets the car share id.
        /// </summary>
        public int CarShareId { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>F
        [Required]
        [DataMember]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the driver.
        /// </summary>
        [Required]
        [DataMember]
        [ForeignKey("UserId")]
        public virtual User Driver { get; set; }

        /// <summary>
        /// Gets or sets the departure city.
        /// </summary>
        [Required(ErrorMessage = "Date of departure cannot be empty.")]
        public virtual string DepartureCity { get; set; }

        /// <summary>
        /// Gets or sets the destination city.
        /// </summary>
        [DataMember]
        [Required]
        public virtual string DestinationCity { get; set; }

        /// <summary>
        /// Gets or sets the date of departure.
        /// </summary>
        [Required(ErrorMessage = "You must specify the departure date.")]
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public virtual DateTime DateOfDeparture { get; set; }

        /// <summary>
        /// Gets or sets the time of departure.
        /// </summary>
        [Required(ErrorMessage = "You must specify the departure time.")]
        [DataMember]
        [DataType(DataType.Time)]
        [Column(TypeName = "DateTime2")]
        public virtual DateTime TimeOfDeparture { get; set; }

        /// <summary>
        /// Gets or sets the description.
        /// </summary>
        [Required]
        [DataMember]
        public virtual string Description { get; set; }

        /// <summary>
        /// Gets or sets the fee.
        /// </summary>
        [Required]
        [DataMember]
        public virtual double Fee { get; set; }

        /// <summary>
        /// Gets or sets the available seats.
        /// </summary>
        [Required]
        [DataMember]
        public virtual int AvailableSeats { get; set; }

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
        public virtual bool SmokersAllowed { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether women only.
        /// </summary>
        [Required]
        [DataMember]
        public virtual bool WomenOnly { get; set; }

        /// <summary>
        /// Gets or sets the vehicle type.
        /// </summary>
        [Required]
        [DataMember]
        public virtual VehicleTypes VehicleType { get; set; }

    }
}
