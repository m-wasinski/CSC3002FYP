// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ProfilePicture.cs" company="">
//   
// </copyright>
// <summary>
//   The profile picture.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;

    /// <summary>
    /// The profile picture.
    /// </summary>
    [DataContract]
    public class ProfilePicture
    {
        /// <summary>
        /// Gets or sets the profile picture id.
        /// </summary>
        [DataMember]
        [ConcurrencyCheck]
        [DatabaseGenerated(DatabaseGeneratedOption.None)]
        public int ProfilePictureId { get; set; }

        /// <summary>
        /// Gets or sets the profile picture bytes.
        /// </summary>
        [DataMember]
        public byte[] ProfilePictureBytes { get; set; }
    }
}
