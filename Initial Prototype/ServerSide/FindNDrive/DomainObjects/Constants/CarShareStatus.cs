// --------------------------------------------------------------------------------------------------------------------
// <copyright file="CarShareStatus.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the CarShareStatus type.
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
    public enum CarShareStatus
    {
        /// <summary>
        /// The upcoming.
        /// </summary>
        [EnumMember(Value = "Upcoming")]
        Upcoming = 1,

        /// <summary>
        /// The past.
        /// </summary>
        [EnumMember(Value = "Past")]
        Past = 2,

        /// <summary>
        /// The cancelled.
        /// </summary>
        [EnumMember(Value = "Cancelled")]
        Cancelled = 3
    }
}
