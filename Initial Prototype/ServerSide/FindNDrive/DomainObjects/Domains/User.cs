// --------------------------------------------------------------------------------------------------------------------
// <copyright file="User.cs" company="">
//   
// </copyright>
// <summary>
//   Represents User entity.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;
    using DomainObjects.Constants;

    /// <summary>
    /// Represents User entity.
    /// </summary>
    [DataContract]
    public class User
    {
        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        [ConcurrencyCheck]
        [DatabaseGenerated(DatabaseGeneratedOption.None)]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the user name.
        /// </summary>
        [Required]
        [DataMember]
        public string UserName { get; set; }

        /// <summary>
        /// Gets or sets the email address.
        /// </summary>
        [EmailAddress]
        [Required(ErrorMessage = "You must provide valid email address.")]
        [DataMember]
        public string EmailAddress { get; set; }

        /// <summary>
        /// Gets or sets the first name.
        /// </summary>
        [DataMember]
        public string FirstName { get; set; }

        /// <summary>
        /// Gets or sets the last name.
        /// </summary>
        [DataMember]
        public string LastName { get; set; }

        /// <summary>
        /// Gets or sets the date of birth.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public virtual DateTime DateOfBirth { get; set; }

        /// <summary>
        /// Gets or sets the gender.
        /// </summary>
        [DataMember]
        public virtual Gender Gender { get; set; }

        /// <summary>
        /// Gets or sets the role.
        /// </summary>
        [DataMember]
        public virtual Roles Role { get; set; }

        /// <summary>
        /// Gets or sets the gcm registration id.
        /// </summary>
        [DataMember]
        public string GCMRegistrationID { get; set; }

        /// <summary>
        /// Gets or sets the travel buddies.
        /// </summary>
        public virtual ICollection<User> Friends { get; set; }

        /// <summary>
        /// Gets or sets the status.
        /// </summary>
        [DataMember]
        public Status Status { get; set; }

        /// <summary>
        /// Gets or sets the rating.
        /// </summary>
        [DataMember]
        public int Rating { get; set; }
    }
}
