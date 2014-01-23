// --------------------------------------------------------------------------------------------------------------------
// <copyright file="TravelLocation.cs" company="">
//   
// </copyright>
// <summary>
//   The travel location.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The travel location.
    /// </summary>
    [DataContract]
    public class TravelLocation
    {
        /// <summary>
        /// Gets or sets the travel location id.
        /// </summary>
        [DataMember]
        public int TravelLocationId { get; set; }

        /// <summary>
        /// Gets or sets the order.
        /// </summary>
        [DataMember]
        public int Order { get; set; }

        /// <summary>
        /// Gets or sets the geo address.
        /// </summary>
        [DataMember]
        public GeoAddress GeoAddress { get; set; }
    }
}
