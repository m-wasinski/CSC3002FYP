// --------------------------------------------------------------------------------------------------------------------
// <copyright file="JourneySearchDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The journey search dto.
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
    /// The journey search dto.
    /// </summary>
    [DataContract]
    public class JourneySearchDTO
    {
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
        public MultiChoice Pets { get; set; }

        /// <summary>
        /// Gets or sets the smokers.
        /// </summary>
        [DataMember]
        public MultiChoice Smokers { get; set; }

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
    }
}