using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Runtime.Serialization;
using DomainObjects.Constants;

namespace DomainObjects.DOmains
{
    /// <summary>
    /// Represents User entity.
    /// </summary>
    public class User
    {
        [ScaffoldColumn(false)]
        public virtual int Id { get; set; }

        [Required]
        [DataMember]
        public virtual string UserName { get; set; }

        [EmailAddress]
        [Required]
        [DataMember]
        public virtual string EmailAddress { get; set; }

        [DataMember]
        [Required]
        public virtual string FirstName { get; set; }

        [Required]
        [DataMember]
        public virtual string LastName { get; set; }

        [Required]
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public virtual DateTime DateOfBirth { get; set; }

        [Required]
        [DataMember]
        public virtual Gender Gender { get; set; }

        [DataMember]
        public virtual Roles Role { get; set; }
    }
}
