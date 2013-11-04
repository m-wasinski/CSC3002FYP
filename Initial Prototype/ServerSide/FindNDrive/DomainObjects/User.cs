using System.ComponentModel.DataAnnotations;
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

        [DataMember]
        public virtual string FirstName { get; set; }

        [DataMember]
        public virtual string LastName { get; set; }

        [DataMember]
        public virtual int Age { get; set; }
    }
}
