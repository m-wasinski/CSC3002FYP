// --------------------------------------------------------------------------------------------------------------------
// <copyright file="GCMConstants.cs" company="">
//   
// </copyright>
// <summary>
//   The gender.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System;
    using System.Runtime.Serialization;

    /// <summary>
    /// The notification type.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum NotificationType
    {
        /// <summary>
        /// The in app.
        /// </summary>
        App = 0,

        /// <summary>
        /// The device.
        /// </summary>
        Device = 1,

        /// <summary>
        /// The both.
        /// </summary>
        Both = 2
    }
}
