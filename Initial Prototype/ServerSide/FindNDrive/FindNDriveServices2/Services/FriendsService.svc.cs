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
    using FindNDriveServices2.ServiceUtils;

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
        /// The random.
        /// </summary>
        private readonly Random random;

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
            this.random = new Random(Guid.NewGuid().GetHashCode());
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

            var toUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .IncludeAll()
                .FirstOrDefault(_ => _.UserId == friendRequestDTO.ToUser.UserId);

            var fromUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .IncludeAll()
                .FirstOrDefault(_ => _.UserId == friendRequestDTO.FromUser.UserId);

            if (toUser == null || fromUser == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid user id.");
            }

            var targetRequest = this.findNDriveUnitOfWork.FriendRequestsRepository.Find(
                friendRequestDTO.FriendRequestId);

            if (targetRequest.FriendRequestDecision != FriendRequestDecision.Undecided)
            {
                return ServiceResponseBuilder.Failure<bool>(string.Format("You have already {0} this request ", targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted ? "accepted": "denied"));
            }

            targetRequest.DecidedOnDate = DateTime.Now;
            targetRequest.Read = true;
            targetRequest.FriendRequestDecision = friendRequestDTO.FriendRequestDecision;

            if (friendRequestDTO.FriendRequestDecision == FriendRequestDecision.Accepted)
            {
                var match = toUser.Friends.FirstOrDefault(_ => _.UserId == fromUser.UserId);

                if (match == null)
                {
                    toUser.Friends.Add(fromUser);
                    fromUser.Friends.Add(toUser);
                }
                else
                {
                    return ServiceResponseBuilder.Failure<bool>("This user is already in your list of friends.");
                }
            }

            this.findNDriveUnitOfWork.Commit();

            const string SenderMessage = "You have {0} {1}'s friend request.";
            const string ReceiverMessage = "{0} has {1} your friend request.";

            var notificationTitle = targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted
                                        ? "Friend request accepted"
                                        : "Friend request denied";

            // This notification is sent to the user who replied to the friend request.
            this.notificationManager.SendAppNotification(
                new List<User> { toUser },
                notificationTitle,
                string.Format(
                    SenderMessage,
                    targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted ? "accepted" : "denied",
                    fromUser.UserName),
                fromUser.UserId,
                -1,
                NotificationType.App,
                targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted
                    ? NotificationContentType.FriendRequestAccepted
                    : NotificationContentType.FriendRequestDenied,
                this.random.Next());

            // This notification is sent to the user who sent the friend request in the first place.
            this.notificationManager.SendAppNotification(
                new List<User> { fromUser },
                notificationTitle,
                string.Format(
                    ReceiverMessage,
                    toUser.UserName,
                    targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted ? "accepted" : "denied"),
                toUser.UserId,
                -1,
                NotificationType.Both,
                targetRequest.FriendRequestDecision == FriendRequestDecision.Accepted
                    ? NotificationContentType.FriendRequestAccepted
                    : NotificationContentType.FriendRequestDenied,
                this.random.Next());

            this.notificationManager.SendGcmTickle(new Collection<User>{toUser});

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

            var toUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == friendRequestDTO.ToUser.UserId);

            var fromUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == friendRequestDTO.FromUser.UserId);

            var pendingFriendRequest =
                this.findNDriveUnitOfWork.FriendRequestsRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(
                        _ =>
                        _.FromUser.UserId == fromUser.UserId && _.ToUser.UserId == toUser.UserId
                        && _.FriendRequestDecision == FriendRequestDecision.Undecided);

            if (pendingFriendRequest != null)
            {
                return ServiceResponseBuilder.Failure<bool>(
                    "You already have a pending friend request for this person.");
            }

            if (toUser == null || fromUser == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid user id.");
            }

            var match = fromUser.Friends.FirstOrDefault(_ => _.UserId == toUser.UserId);

            if (match != null)
            {
                return ServiceResponseBuilder.Failure<bool>("This user is already in your list of friends.");
            }

            if (friendRequestDTO.FromUser.UserId == friendRequestDTO.ToUser.UserId)
            {
                return ServiceResponseBuilder.Failure<bool>("You cannot senda friend request to yourself.");
            }

            var friendRequest = new FriendRequest
                                    {
                                        FriendRequestDecision = FriendRequestDecision.Undecided,
                                        SentOnDate = DateTime.Now,
                                        Read = false,
                                        FromUser = fromUser,
                                        ToUser = toUser,
                                        Message = friendRequestDTO.Message
                                    };

            this.findNDriveUnitOfWork.FriendRequestsRepository.Add(friendRequest);
            this.findNDriveUnitOfWork.Commit();

            const string ReceiverMessage = "You received a friend request from user: {0} {1} ({2})";
            const string SenderMessage = "You sent a friend request to user: {0} {1} ({2})";

            this.notificationManager.SendAppNotification(
                new List<User> { toUser },
                "New friend request received.",
                string.Format(
                    ReceiverMessage,
                    fromUser.FirstName,
                    fromUser.LastName,
                    fromUser.UserName),
                fromUser.UserId,
                friendRequest.FriendRequestId,
                NotificationType.Both,
                NotificationContentType.FriendRequestReceived,
                this.random.Next());

            this.notificationManager.SendAppNotification(
                new List<User> { fromUser },
                "Friend request sent.",
                string.Format(
                    SenderMessage,
                    toUser.FirstName,
                toUser.LastName,
                toUser.UserName),
                toUser.UserId,
                -1,
                NotificationType.App,
                NotificationContentType.FriendRequestSent,
                this.random.Next());

            this.notificationManager.SendGcmTickle(new List<User> { toUser });
            
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

            friends.ForEach(
                delegate(User u)
                    {
                        u.UnreadMessagesCount = this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable().Count(_ => _.RecipientId == userId && _.SenderId == u.UserId && !_.Read);
                    });

            return ServiceResponseBuilder.Success(friends);
        }

        public ServiceResponse<FriendRequest> GetFriendRequest(int id)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new FriendRequest());
            }

            var friendRequest = this.findNDriveUnitOfWork.FriendRequestsRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.FriendRequestId == id);

            return friendRequest == null ? ServiceResponseBuilder.Failure<FriendRequest>("Friend request with this id does not exist.") : ServiceResponseBuilder.Success(friendRequest);
        }
    }
}
