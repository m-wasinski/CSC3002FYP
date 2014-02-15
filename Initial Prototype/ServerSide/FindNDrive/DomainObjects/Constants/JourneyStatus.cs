// --------------------------------------------------------------------------------------------------------------------
// <copyright file="JourneyStatus.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the JourneyStatus type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System;
    using System.Runtime.Serialization;

    /// <summary>
    /// The car share status.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum JourneyStatus
    {
        /// <summary>
        /// The OK.
        /// </summary>
        [EnumMember(Value = "OK")]
        OK = 1,

        /// <summary>
        /// The past.
        /// </summary>
        [EnumMember(Value = "Past")]
        Expired = 2,

        /// <summary>
        /// The cancelled.
        /// </summary>
        [EnumMember(Value = "Cancelled")]
        Cancelled = 3
    }
}
