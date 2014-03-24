// --------------------------------------------------------------------------------------------------------------------
// <copyright file="SessionTypes.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the SessionTypes type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The session types.
    /// </summary>
    public enum SessionTypes
    {
        /// <summary>
        /// The temporary.
        /// </summary>
        [DataMember]
        Temporary = 0,

        /// <summary>
        /// The permanent.
        /// </summary>
        [DataMember]
        Permanent = 1
    }
}
