using System.Runtime.Serialization;

namespace DomainObjects
{
    /// <summary>
    /// Represents User entity for the first prototype service contract.
    /// </summary>
    [DataContract(Namespace = "http://localhost/Prototype")]
    public class User
    {
        [DataMember]
        public int Id;

        [DataMember]
        public string FirstName;

        [DataMember]
        public string LastName;

        [DataMember]
        public int Age;
    }
}
