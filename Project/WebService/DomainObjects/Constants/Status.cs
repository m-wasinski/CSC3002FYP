// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Status.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the Status type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System;
    using System.Runtime.Serialization;

    /// <summary>
    /// The status.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum Status
    {
        /// <summary>
        /// Represents a male person.
        /// </summary>
        [EnumMember(Value = "Online")]
        Online = 1,

        /// <summary>
        /// Represents a female person.
        /// </summary>
        [EnumMember(Value = "Offline")]
        Offline = 0
    }
}
