using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Runtime.Serialization;
using DomainObjects.Constants;
using DomainObjects.DOmains;

namespace FindNDriveServices2.DTOs
{
    [DataContract]
    public class CarShareDTO
    {
        [DataMember]
        [Required]
        public virtual string DepartureCity { get; set; }

        [DataMember]
        [Required]
        public virtual string DestinationCity { get; set; }

        [Required]
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public virtual DateTime DateOfDeparture { get; set; }

        [Required]
        [DataType(DataType.Date)]
        [DataMember]
        [Column(TypeName = "DateTime2")]
        public virtual DateTime TimeOfDeparture { get; set; }

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
        public virtual VehicleTypes VehicleTypes { get; set; }
    }
}
