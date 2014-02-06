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
    /// The gender.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum NotificationType
    {
        /// <summary>
        /// The refresh.
        /// </summary>
        Refresh = 1,

        /// <summary>
        /// The logout.
        /// </summary>
        Logout = 2,

        /// <summary>
        /// The journey request.
        /// </summary>
        JourneyRequest = 3,

        /// <summary>
        /// The journey request accepted.
        /// </summary>
        JourneyRequestAccepted = 4,

        /// <summary>
        /// The journey request denied.
        /// </summary>
        JourneyRequestDenied = 5,

        /// <summary>
        /// The friend request.
        /// </summary>
        FriendRequest = 6,

        /// <summary>
        /// The friend request accepted.
        /// </summary>
        FriendRequestAccepted = 7,

        /// <summary>
        /// The friend requested denied.
        /// </summary>
        FriendRequestedDenied = 8,

        /// <summary>
        /// The friend offered new journey.
        /// </summary>
        FriendOfferedNewJourney = 9,

        /// <summary>
        /// The instant messenger.
        /// </summary>
        InstantMessenger = 10
    }
}
