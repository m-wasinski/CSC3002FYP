// --------------------------------------------------------------------------------------------------------------------
// <copyright file="NotificationContentType.cs" company="">
//   
// </copyright>
// <summary>
//   The notification content type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Constants
{
    using System;
    using System.Runtime.Serialization;

    /// <summary>
    /// The notification content type.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum NotificationContentType
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
        /// The journey request sent.
        /// </summary>
        JourneyRequestSent = 3,

        /// <summary>
        /// The journey request.
        /// </summary>
        JourneyRequestReceived = 4,

        /// <summary>
        /// The journey request accepted.
        /// </summary>
        JourneyRequestAccepted = 5,

        /// <summary>
        /// The journey request denied.
        /// </summary>
        JourneyRequestDenied = 6,

        /// <summary>
        /// The friend request.
        /// </summary>
        FriendRequestSent = 7,

        /// <summary>
        /// The friend request received.
        /// </summary>
        FriendRequestReceived = 8,

        /// <summary>
        /// The friend request accepted.
        /// </summary>
        FriendRequestAccepted = 9,

        /// <summary>
        /// The friend request denied.
        /// </summary>
        FriendRequestDenied = 10,

        /// <summary>
        /// The friend offered new journey.
        /// </summary>
        FriendOfferedNewJourney = 11,

        /// <summary>
        /// The journey request sent.
        /// </summary>
        InstantMessenger = 12,

        /// <summary>
        /// The journey chat message.
        /// </summary>
        JourneyChatMessage = 13,

        /// <summary>
        /// The journey modified.
        /// </summary>
        JourneyModified = 14,

        /// <summary>
        /// The passenger left journey.
        /// </summary>
        PassengerLeftJourney = 15,

        /// <summary>
        /// The ileft ajourney.
        /// </summary>
        IleftAjourney = 16,

        /// <summary>
        /// The journey cancelled.
        /// </summary>
        JourneyCancelled = 17,

        /// <summary>
        /// The rating received.
        /// </summary>
        RatingReceived = 18,

        /// <summary>
        /// The rating left.
        /// </summary>
        RatingLeft = 19,

        /// <summary>
        /// The journey matching template offered.
        /// </summary>
        JourneyMatchingTemplateOffered = 20
    }
}
