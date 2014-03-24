// --------------------------------------------------------------------------------------------------------------------
// <copyright file="NotificationContext.cs" company="">
//   
// </copyright>
// <summary>
//   The notification context.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System;
    using System.Runtime.Serialization;

    /// <summary>
    /// The notification context.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum NotificationContext
    {
        /// <summary>
        /// The positive.
        /// </summary>
        [EnumMember(Value = "Positive")]
        Positive = 1,

        /// <summary>
        /// The negative.
        /// </summary>
        [EnumMember(Value = "Negative")]
        Negative = 2,

        /// <summary>
        /// The neutral.
        /// </summary>
        [EnumMember(Value = "Neutral")]
        Neutral = 3
    }
}
