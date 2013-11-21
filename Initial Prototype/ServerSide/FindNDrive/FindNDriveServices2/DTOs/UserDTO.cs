using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Runtime.Serialization;
using DomainObjects.Constants;

namespace FindNDriveServices2.DTOs
{
    public class UserDTO
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
        public virtual string FirstName { get; set; }

        [DataMember]
        public virtual string LastName { get; set; }

        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public virtual DateTime DateOfBirth { get; set; }

        [DataMember]
        public virtual Gender Gender { get; set; }

        [DataMember]
        public virtual Roles Role { get; set; }
    }
}