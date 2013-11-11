using System;
using System.Runtime.Serialization;

namespace DomainObjects.Constants
{
    [DataContract]
    [Serializable]
    public enum VehicleTypes
    {
        [EnumMember(Value = "Private Car")]
        PrivateCar = 0,
        [EnumMember(Value = "Minivan")]
        Minivan = 1,
        [EnumMember(Value = "Van")]
        Van = 3,
        [EnumMember(Value = "Lorry")]
        Lorry = 4,
        [EnumMember(Value = "Motorbike")]
        Motorbike = 5,
        [EnumMember(Value = "Other")]
        Other = 6
    }
}
