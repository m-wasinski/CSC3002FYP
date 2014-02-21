// --------------------------------------------------------------------------------------------------------------------
// <copyright file="FriendsService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the FriendsService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Services
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The friends service.
    /// </summary>
    [ServiceBehavior(
           InstanceContextMode = InstanceContextMode.PerCall,
           ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class FriendsService : IFriendsService
    {
        /// <summary>
        /// The _find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager sessionManager;

        /// <summary>
        /// The notification manager.
        /// </summary>
        private readonly NotificationManager notificationManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="FriendsService"/> class.
        /// </summary>
        public FriendsService()
        {
            
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="FriendsService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        /// <param name="gcmManager">
        /// The gcm manager.
        /// </param>
        public FriendsService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.notificationManager = notificationManager;
        }

        /// <summary>
        /// The process decision.
        /// </summary>
        /// <param name="friendRequestDTO">
        /// The friend request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<bool> ProcessDecision(FriendRequestDTO friendRequestDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(false);
            }

            var receivingUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .IncludeAll()
                .FirstOrDefault(_ => _.UserId == friendRequestDTO.TargetUserId);

            var requestingUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .IncludeAll()
                .FirstOrDefault(_ => _.UserId == friendRequestDTO.RequestingUserId);

            if (receivingUser == null || requestingUser == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid user id.");
            }

            var targetRequest = this.findNDriveUnitOfWork.FriendRequestsRepository.Find(
                friendRequestDTO.FriendRequestId);

            if (targetRequest.FriendRequestDecision != FriendRequestDecision.Undecided)
            {
                return ServiceResponseBuilder.Failure<bool>(String.Format("You have already {0} this request ", targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted ? "accepted": "denied"));
            }

            targetRequest.DecidedOnDate = DateTime.Now;
            targetRequest.Read = true;
            targetRequest.FriendRequestDecision = friendRequestDTO.FriendRequestDecision;

            if (friendRequestDTO.FriendRequestDecision == FriendRequestDecision.Accepted)
            {
                var match = receivingUser.Friends.FirstOrDefault(_ => _.UserId == requestingUser.UserId);

                if (match == null)
                {
                    receivingUser.Friends.Add(requestingUser);
                    requestingUser.Friends.Add(receivingUser);
                }
                else
                {
                    return ServiceResponseBuilder.Failure<bool>("This user is already in your list of friends.");
                }
            }

            this.findNDriveUnitOfWork.Commit();

            const string SenderMessage = "You have {0} {1}'s friend request.";
            const string ReceiverMessage = "{0} has {1} your friend request.";

            this.notificationManager.SendAppNotification(new List<User> { receivingUser }, targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted ? "Friend request accepted" : "Friend request denied", String.Format(
                                                         SenderMessage,
                                                         targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted ? "accepted" : "denied",
                                                         requestingUser.UserName),
                                                         targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted ? NotificationContext.Positive : NotificationContext.Negative,
                                                         NotificationType.App,
                                                         targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted ? NotificationContentType.FriendRequestAccepted : NotificationContentType.FriendRequestDenied,
                                                         string.Empty);

            this.notificationManager.SendAppNotification(new List<User> { requestingUser }, targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted ? "Friend request accepted" : "Friend request denied", String.Format(
                                                         ReceiverMessage,
                                                         receivingUser.UserName,
                                                         targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted ? "accepted" : "denied"),
                                                         targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted ? NotificationContext.Positive : NotificationContext.Negative,
                                                         NotificationType.Both,
                                                         targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted ? NotificationContentType.FriendRequestAccepted : NotificationContentType.FriendRequestDenied,
                                                         string.Empty);

            this.notificationManager.SendGcmNotification(new Collection<User>{receivingUser}, "Friend request reply", GcmNotificationType.NotificationTickle, string.Empty);

            return ServiceResponseBuilder.Success(true);
        }

        /// <summary>
        /// The send request.
        /// </summary>
        /// <param name="friendRequestDTO">
        /// The friend request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<bool> SendRequest(FriendRequestDTO friendRequestDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(false);
            }

            var receivingUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == friendRequestDTO.TargetUserId);

            var sendingUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == friendRequestDTO.RequestingUserId);

            if (receivingUser == null || sendingUser == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid user id.");
            }

            var match = sendingUser.Friends.FirstOrDefault(_ => _.UserId == receivingUser.UserId);

            if (match != null)
            {
                return ServiceResponseBuilder.Failure<bool>("This user is already in your list of friends.");
            }

            if (friendRequestDTO.TargetUserId == friendRequestDTO.RequestingUserId)
            {
                return ServiceResponseBuilder.Failure<bool>("You cannot invite yourself to your friends list.");
            }

            var friendRequest = new FriendRequest
                                    {
                                        FriendRequestDecision = FriendRequestDecision.Undecided,
                                        SentOnDate = DateTime.Now,
                                        Read = false,
                                        RequestingUserId = friendRequestDTO.RequestingUserId,
                                        TargetUserId = friendRequestDTO.TargetUserId,
                                        Message = friendRequestDTO.Message,
                                        TargetUserName =
                                            receivingUser.FirstName + " " + receivingUser.LastName + " ("
                                            + receivingUser.UserName + ")",
                                        RequestingUserName =
                                            sendingUser.FirstName + " " + sendingUser.LastName + " ("
                                            + sendingUser.UserName + ")"
                                    };

            this.findNDriveUnitOfWork.FriendRequestsRepository.Add(friendRequest);
            this.findNDriveUnitOfWork.Commit();

            const string ReceiverMessage = "You received a friend request from user: {0} {1} ({2})";
            const string SenderMessage = "You sent a friend request to user: {0} {1} ({2})";

            this.notificationManager.SendAppNotification(new List<User> { receivingUser }, "New friend request received.", String.Format(
                                                         ReceiverMessage,
                                                         sendingUser.FirstName,
                                                         sendingUser.LastName,
                                                         sendingUser.UserName), NotificationContext.Positive, NotificationType.Both, NotificationContentType.FriendRequestReceived, friendRequest);

            this.notificationManager.SendAppNotification(new List<User> { sendingUser }, "Friend request sent.", String.Format(
                                                        SenderMessage,
                                                        receivingUser.FirstName,
                                                        receivingUser.LastName,
                                                        receivingUser.UserName), NotificationContext.Positive, NotificationType.App, NotificationContentType.FriendRequestSent, string.Empty);

            this.notificationManager.SendGcmNotification(new List<User> { receivingUser }, "New friend request received.", GcmNotificationType.NotificationTickle, string.Empty);
            
            return ServiceResponseBuilder.Success(true);
        }

        /// <summary>
        /// The get friends.
        /// </summary>
        /// <param name="userId">
        /// The user id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<User>> GetFriends(int userId)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new List<User>());
            }

            var user = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .IncludeAll()
                .FirstOrDefault(_ => _.UserId == userId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<List<User>>("Invalid user id");
            }

            var friends =
                user
                    .Friends.ToList();

            return ServiceResponseBuilder.Success(friends);
        }
    }
}
