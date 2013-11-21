using System.Runtime.Serialization;

namespace DomainObjects.Constants
{
    public enum SessionTypes
    {   
        [DataMember]
        Temporary,
        [DataMember]
        Permanent
    }
}
