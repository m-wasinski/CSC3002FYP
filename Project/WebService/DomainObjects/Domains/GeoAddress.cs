// --------------------------------------------------------------------------------------------------------------------
// <copyright file="GeoAddress.cs" company="">
//   
// </copyright>
// <summary>
//   The geo address.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System.ComponentModel.DataAnnotations;
    using System.Runtime.Serialization;

    /// <summary>
    /// The geo address.
    /// </summary>
    [DataContract]
    public class GeoAddress
    {
        /// <summary>
        /// Gets or sets the geo address id.
        /// </summary>
        [DataMember]
        [ConcurrencyCheck]
        public int GeoAddressId { get; set; }

        /// <summary>
        /// Gets or sets the order.
        /// </summary>
        [DataMember]
        public int Order { get; set; }

        /// <summary>
        /// Gets or sets the address line.
        /// </summary>
        [DataMember]
        public string AddressLine { get; set; }

        /// <summary>
        /// Gets or sets the latitude.
        /// </summary>
        [DataMember]
        public double Latitude { get; set; }

        /// <summary>
        /// Gets or sets the longitute.
        /// </summary>
        [DataMember]
        public double Longitude { get; set; }
    }
}
