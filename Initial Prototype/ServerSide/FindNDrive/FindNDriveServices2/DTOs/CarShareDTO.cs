// --------------------------------------------------------------------------------------------------------------------
// <copyright file="CarShareDTO.cs" company="">
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
    using DomainObjects.DOmains;

    /// <summary>
    /// The car share dto.
    /// </summary>
    [DataContract]
    public class CarShareDTO
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
        public User Driver { get; set; }

        /// <summary>
        /// Gets or sets the departure city.
        /// </summary>
        [Required(ErrorMessage = "Departure city cannot be empty.")]
        [DataMember] 
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
        [Required(ErrorMessage = "You must specify the departure time.")]
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime DateAndTimeOfDeparture { get; set; }

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
        public ICollection<User> Participants { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether smokers allowed.
        /// </summary>
        [Required]
        [DataMember]
        public bool SmokersAllowed { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether women only.
        /// </summary>
        [Required]
        [DataMember]
        public bool WomenOnly { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether private.
        /// </summary>
        [Required]
        [DataMember]
        public bool Private { get; set; }

        /// <summary>
        /// Gets or sets the vehicle type.
        /// </summary>
        [Required]
        [DataMember]
        public VehicleTypes VehicleType { get; set; }
    }
}
