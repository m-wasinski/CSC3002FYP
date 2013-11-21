using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Runtime.Serialization;
using DomainObjects.Constants;
using DomainObjects.DOmains;

namespace DomainObjects.Domains
{
    public class Session
    {
        [DataMember]
        [Required]
        [Key]
        public virtual int UserId { get; set; }

        [DataMember]
        [Required]
        public virtual String LastKnownId { get; set; }

        [DataMember]
        [Required]
        public virtual String Token { get; set; }

        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        [Required]
        public virtual DateTime SessionExpirationDate { get; set; }

        [DataMember]
        [Required]
        public virtual SessionTypes SessionType { get; set; }
    }
}
