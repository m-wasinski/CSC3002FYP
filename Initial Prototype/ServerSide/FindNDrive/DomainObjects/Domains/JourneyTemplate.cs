using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace DomainObjects.Domains
{
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;

    using DomainObjects.Constants;

    using FindNDriveServices2.DTOs;

    /// <summary>
    /// The journey template.
    /// </summary>
    [DataContract]
    public class JourneyTemplate
    {
        /// <summary>
        /// Gets or sets the journey template id.
        /// </summary>
        [DataMember]
        [ConcurrencyCheck]
        public int JourneyTemplateId { get; set; }

        /// <summary>
        /// Gets or sets the alias.
        /// </summary>
        [DataMember]
        public string Alias { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        public virtual User User { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether free.
        /// </summary>
        [DataMember]
        public double Fee { get; set; }

        /// <summary>
        /// Gets or sets the departure radius.
        /// </summary>
        [DataMember]
        public double DepartureRadius { get; set; }

        /// <summary>
        /// Gets or sets the destination radius.
        /// </summary>
        [DataMember]
        public double DestinationRadius { get; set; }

        /// <summary>
        /// Gets or sets the pets.
        /// </summary>
        [DataMember]
        public bool Pets { get; set; }

        /// <summary>
        /// Gets or sets the smokers.
        /// </summary>
        [DataMember]
        public bool Smokers { get; set; }

        /// <summary>
        /// Gets or sets the vehicle type.
        /// </summary>
        [DataMember]
        public VehicleTypes VehicleType { get; set; }

        /// <summary>
        /// Gets or sets the geo addresses.
        /// </summary>
        [DataMember]
        public List<GeoAddress> GeoAddresses { get; set; }

        /// <summary>
        /// Gets or sets the date allowance.
        /// </summary>
        [DataMember]
        public int DateAllowance { get; set; }

        /// <summary>
        /// Gets or sets the time allowance.
        /// </summary>
        [DataMember]
        public int TimeAllowance { get; set; }

        /// <summary>
        /// Gets or sets the departure date.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime DateAndTimeOfDeparture { get; set; }

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
        /// Gets or sets the departure date.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime CreationDate { get; set; }
    }
}
