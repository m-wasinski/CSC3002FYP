using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Runtime.Serialization;

namespace DomainObjects
{
    /// <summary>
    /// Represents User entity for the first prototype service contract.
    /// </summary>
    //[DataContract(Namespace = "http://localhost/Prototype")]
    public class User
    {
        [ScaffoldColumn(false)]
        public virtual int Id { get; set; }

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

        [Required]
        [DataMember]
        public virtual List<CarShare> CarShares { get; set; }
    }
}
