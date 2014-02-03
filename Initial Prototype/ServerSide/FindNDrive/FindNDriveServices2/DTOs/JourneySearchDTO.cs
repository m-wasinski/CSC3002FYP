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
    using System.Runtime.Serialization;
    using DomainObjects.Domains;

    /// <summary>
    /// The journey search dto.
    /// </summary>
    [DataContract]
    public class JourneySearchDTO
    {
        /// <summary>
        /// Gets or sets the journey.
        /// </summary>
        [DataMember]
        public Journey Journey { get; set; }

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
        /// Gets or sets the departure radius.
        /// </summary>
        [DataMember]
        public double DepartureRadius { get; set; }

        /// <summary>
        /// Gets or sets the destination radius.
        /// </summary>
        [DataMember]
        public double DestinationRadius { get; set; }
    }
}