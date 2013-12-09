// --------------------------------------------------------------------------------------------------------------------
// <copyright file="VehicleTypes.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the VehicleTypes type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System;
    using System.Runtime.Serialization;

    /// <summary>
    /// The vehicle types.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum VehicleTypes
    {
        /// <summary>
        /// The private car.
        /// </summary>
        [EnumMember(Value = "Private Car")]
        PrivateCar = 0,

        /// <summary>
        /// The minivan.
        /// </summary>
        [EnumMember(Value = "Minivan")]
        Minivan = 1,

        /// <summary>
        /// The van.
        /// </summary>
        [EnumMember(Value = "Van")]
        Van = 2,

        /// <summary>
        /// The lorry.
        /// </summary>
        [EnumMember(Value = "Lorry")]
        Lorry = 3,

        /// <summary>
        /// The motorbike.
        /// </summary>
        [EnumMember(Value = "Motorbike")]
        Motorbike = 4,

        /// <summary>
        /// The other.
        /// </summary>
        [EnumMember(Value = "Other")]
        Other = 5
    }
}
