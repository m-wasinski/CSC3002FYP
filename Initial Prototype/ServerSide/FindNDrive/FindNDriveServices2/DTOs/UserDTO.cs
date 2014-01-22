// --------------------------------------------------------------------------------------------------------------------
// <copyright file="UserDTO.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the UserDTO type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.DTOs
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    /// <summary>
    /// The user dto.
    /// </summary>
    [DataContract]
    public class UserDTO
    {
        /// <summary>
        /// Gets or sets the id.
        /// </summary>
        [ScaffoldColumn(false)]
        public virtual int Id { get; set; }

        /// <summary>
        /// Gets or sets the user name.
        /// </summary>
        [Required]
        [DataMember]
        public virtual string UserName { get; set; }

        /// <summary>
        /// Gets or sets the email address.
        /// </summary>
        [EmailAddress]
        [Required]
        [DataMember]
        public virtual string EmailAddress { get; set; }

        /// <summary>
        /// Gets or sets the first name.
        /// </summary>
        [DataMember]
        public virtual string FirstName { get; set; }

        /// <summary>
        /// Gets or sets the last name.
        /// </summary>
        [DataMember]
        public virtual string LastName { get; set; }

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
        [DataMember]
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