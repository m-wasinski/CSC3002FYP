using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Runtime.Serialization;
using DomainObjects;
using DomainObjects.Enums;

namespace FindNDriveServices.DTOs
{
    public class CarShareDTO
    {
        [Required]
        public virtual string DepartureCity { get; set; }

        [DataMember]
        [Required]
        public virtual string DestinationCity { get; set; }

        [Required]
        [DataMember]
        public virtual DateTimeFormat DateOfDeparture { get; set; }

        [Required]
        [DataMember]
        public virtual DateTimeFormat TimeOfDeparture { get; set; }

        [Required]
        [DataMember]
        public virtual User Driver { get; set; }

        [Required]
        [DataMember]
        public virtual string Description { get; set; }

        [Required]
        [DataMember]
        public virtual double Fee { get; set; }

        [Required]
        [DataMember]
        public virtual int NoOfSeats { get; set; }

        [Required]
        [DataMember]
        public virtual List<User> Participants { get; set; }

        [Required]
        [DataMember]
        public virtual bool SmokersAllowed { get; set; }

        [Required]
        [DataMember]
        public virtual bool WomenOnly { get; set; }

        [Required]
        [DataMember]
        public virtual VehicleType VehicleType { get; set; }
    }
}
