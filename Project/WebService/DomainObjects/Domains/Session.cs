// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Session.cs" company="">
//   
// </copyright>
// <summary>
//   The session.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;

    using DomainObjects.Constants;

    /// <summary>
    /// The session.
    /// </summary>
    public class Session
    {
        [ConcurrencyCheck]
        public int SessionId { get; set; }

        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        public int UserId { get; set; }

        [Required]
        public User User { get; set; }

        /// <summary>
        /// Gets or sets the last known id.
        /// </summary>
        [Required]
        public string DeviceId { get; set; }

        /// <summary>
        /// Gets or sets the last random id.
        /// </summary>
        public string RandomID { get; set; }

        /// <summary>
        /// Gets or sets the session id.
        /// </summary>
        [Required]
        public string SessionString { get; set; }

        /// <summary>
        /// Gets or sets the expires on.
        /// </summary>
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        [Required]
        public virtual DateTime ExpiryDate { get; set; }

        /// <summary>
        /// Gets or sets the session type.
        /// </summary>
        [Required]
        public SessionTypes SessionType { get; set; }
    }
}
