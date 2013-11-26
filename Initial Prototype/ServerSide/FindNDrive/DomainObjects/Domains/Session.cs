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
        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        [Required]
        [Key]
        public virtual int UserId { get; set; }

        /// <summary>
        /// Gets or sets the last known id.
        /// </summary>
        [DataMember]
        [Required]
        public virtual string LastKnownId { get; set; }

        /// <summary>
        /// Gets or sets the session id.
        /// </summary>
        [DataMember]
        [Required]
        public virtual string SessionId { get; set; }

        /// <summary>
        /// Gets or sets the expires on.
        /// </summary>
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        [Required]
        public virtual DateTime ExpiresOn { get; set; }

        /// <summary>
        /// Gets or sets the session type.
        /// </summary>
        [DataMember]
        [Required]
        public virtual SessionTypes SessionType { get; set; }
    }
}
