// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Roles.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the Roles type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System;
    using System.Runtime.Serialization;

    /// <summary>
    /// The roles.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum Roles
    {
        /// <summary>
        /// The user.
        /// </summary>
        [EnumMember(Value = "User")]
        User = 0,

        /// <summary>
        /// The administrator.
        /// </summary>
        [EnumMember(Value = "Administrator")]
        Administrator = 1
    }
}
