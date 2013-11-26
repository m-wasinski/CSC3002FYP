// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Gender.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the Gender type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System;
    using System.Runtime.Serialization;

    /// <summary>
    /// The gender.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum Gender
    { 
        /// <summary>
        /// Represents a male person.
        /// </summary>
        [EnumMember(Value = "Male")]
        Male = 1,

        /// <summary>
        /// Represents a female person.
        /// </summary>
        [EnumMember(Value = "Female")]
        Female = 2
    }
}
