using System;
using System.Runtime.Serialization;

namespace DomainObjects.Constants
{
    [DataContract]
    [Serializable]
    public enum Roles
    {
        [EnumMember(Value = "User")]
        User = 0,
        [EnumMember(Value = "Administrator")]
        Administrator = 1
    }
}
