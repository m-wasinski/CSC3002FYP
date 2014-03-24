// --------------------------------------------------------------------------------------------------------------------
// <copyright file="PrivacyLevel.cs" company="">
//   
// </copyright>
// <summary>
//   The privacy level.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System;
    using System.Runtime.Serialization;

    /// <summary>
    /// The privacy level.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum PrivacyLevel
    {
        /// <summary>
        /// The private.
        /// </summary>
        [EnumMember(Value = "Private")]
        Private = 0,

        /// <summary>
        /// The friends only.
        /// </summary>
        [EnumMember(Value = "FriendsOnly")]
        FriendsOnly = 1,

        /// <summary>
        /// The everyone.
        /// </summary>
        [EnumMember(Value = "Everyone")]
        Everyone = 2
    }
}
