// --------------------------------------------------------------------------------------------------------------------
// <copyright file="GcmNotificationType.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the GcmNotificationType type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System;
    using System.Runtime.Serialization;

    /// <summary>
    /// The gcm notification type.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum GcmNotificationType
    {
        /// <summary>
        /// The notification tickle.
        /// </summary>
        NotificationTickle = 1,

        /// <summary>
        /// The chat message.
        /// </summary>
        ChatMessage = 2,

        /// <summary>
        /// The journey chat message.
        /// </summary>
        JourneyChatMessage = 3,

        /// <summary>
        /// The logout.
        /// </summary>
        Logout = 4
    }
}
