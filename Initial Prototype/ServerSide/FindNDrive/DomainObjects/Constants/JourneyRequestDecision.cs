// --------------------------------------------------------------------------------------------------------------------
// <copyright file="CarShareDecision.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the CarShareDecision type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

using System;

namespace DomainObjects.Constants
{
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
         [EnumMember(Value = "Rejected")]
        Rejected = 2
    }
}
