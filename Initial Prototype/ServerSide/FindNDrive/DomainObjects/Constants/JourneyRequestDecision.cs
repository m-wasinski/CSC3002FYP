// --------------------------------------------------------------------------------------------------------------------
// <copyright file="CarShareDecision.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the CarShareDecision type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System;
    using System.Runtime.Serialization;

    /// <summary>
    /// The car share decision.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum JourneyRequestDecision
    {
        /// <summary>
        /// The undecided.
        /// </summary>
        [EnumMember(Value = "Undecided")]
        Undecided = 0,

        /// <summary>
        /// The accepted.
        /// </summary>
        [EnumMember(Value = "Accepted")]
        Accepted = 1,

        /// <summary>
        /// The rejected.
        /// </summary>
        [EnumMember(Value = "Denied")]
        Denied = 2
    }
}
