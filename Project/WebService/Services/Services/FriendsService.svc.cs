// --------------------------------------------------------------------------------------------------------------------
// <copyright file="FriendsService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the FriendsService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.Services
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Data.Entity;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DataAccessLayer;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using global::Services.Contracts;
    using global::Services.DTOs;
    using global::Services.ServiceResponses;
    using global::Services.ServiceUtils;

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
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        /// <param name="notificationManager">
        /// The notification Manager.
        /// </param>
        public FriendsService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.notificationManager = notificationManager;
            this.random = new Random(Guid.NewGuid().GetHashCode());
        }

        /// <summary>
        /// Processes decision submitted by the user for a given friend request.
        /// </summary>
        /// <param name="friendRequestDTO">
        /// Contains all the necessary information in regards to the decision made by the user and to identify the friend request in the database.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse ProcessDecision(FriendRequestDTO friendRequestDTO)
        {
            // Check if session is still valid, if not send unauthorised response to log the user out.
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            // Retrieve both users from the database to that entity framework can track the entities.
            var toUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .Include(_ => _.Friends).FirstOrDefault(_ => _.UserId == friendRequestDTO.ToUser.UserId);

            var fromUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .Include(_ => _.Friends).FirstOrDefault(_ => _.UserId == friendRequestDTO.FromUser.UserId);

            if (toUser == null || fromUser == null)
            {
                return ServiceResponseBuilder.Failure("Invalid user id.");
            }

            var targetRequest = this.findNDriveUnitOfWork.FriendRequestsRepository.Find(
                friendRequestDTO.FriendRequestId);

            if (targetRequest == null)
            {
                return ServiceResponseBuilder.Failure("Invalid friend request id.");
            }

            // Check if the request has already been replied to.
            if (targetRequest.Decision != Decision.Undecided)
            {
                return ServiceResponseBuilder.Failure(string.Format("You have already {0} this request ", targetRequest.Decision == Decision.Accepted ? "accepted" : "denied"));
            }

            targetRequest.DecidedOnDate = DateTime.Now;
            targetRequest.Read = true;
            targetRequest.Decision = friendRequestDTO.Decision;

            // Request has been accepted by the user.
            if (friendRequestDTO.Decision == Decision.Accepted)
            {
                var match = toUser.Friends.FirstOrDefault(_ => _.UserId == fromUser.UserId);

                if (match == null)
                {
                    toUser.Friends.Add(fromUser);
                    fromUser.Friends.Add(toUser);
                }
                else 
                {
                    // This user is already in the other user's friends list.
                    return ServiceResponseBuilder.Failure<bool>("This user is already in your list of friends.");
                }
            }

            this.findNDriveUnitOfWork.Commit();

            const string SenderMessage = "You have {0} {1}'s friend request.";
            const string ReceiverMessage = "{0} has {1} your friend request.";

            var notificationTitle = targetRequest.Decision == Decision.Accepted
                                        ? "Friend request accepted"
                                        : "Friend request denied";

            // This notification is sent to the user who replied to the friend request.
            this.notificationManager.CreateAppNotification(
                new List<User> { toUser },
                notificationTitle,
                string.Format(
                    SenderMessage,
                    targetRequest.Decision == Decision.Accepted ? "accepted" : "denied",
                    fromUser.UserName),
                fromUser.UserId,
                -1,
                NotificationType.App,
                targetRequest.Decision == Decision.Accepted
                    ? NotificationContentType.FriendRequestAccepted
                    : NotificationContentType.FriendRequestDenied,
                this.random.Next());

            // This notification is sent to the user who sent the friend request in the first place.
            this.notificationManager.CreateAppNotification(
                new List<User> { fromUser },
                notificationTitle,
                string.Format(
                    ReceiverMessage,
                    toUser.UserName,
                    targetRequest.Decision == Decision.Accepted ? "accepted" : "denied"),
                toUser.UserId,
                -1,
                NotificationType.Both,
                targetRequest.Decision == Decision.Accepted
                    ? NotificationContentType.FriendRequestAccepted
                    : NotificationContentType.FriendRequestDenied,
                this.random.Next());

            this.notificationManager.SendGcmTickle(new Collection<User>{toUser});

            return ServiceResponseBuilder.Success();
        }

        /// <summary>
        /// Sends a new friend request.
        /// </summary>
        /// <param name="friendRequestDTO">
        /// The DTO object used by the client to gather all required data for a new friend request to be created.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse SendRequest(FriendRequestDTO friendRequestDTO)
        {
            // Check if session is still valid, if not send unauthorised response to log the user out.
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            // Retrieve both users to allow entity framework tracking.
            var toUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Include(_ => _.Friends).FirstOrDefault(_ => _.UserId == friendRequestDTO.ToUser.UserId);

            var fromUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Include(_ => _.Friends).FirstOrDefault(_ => _.UserId == friendRequestDTO.FromUser.UserId);

            var pendingFriendRequest =
                this.findNDriveUnitOfWork.FriendRequestsRepository.AsQueryable()
                    .IncludeChildren()
                    .FirstOrDefault(
                        _ =>
                        _.FromUser.UserId == fromUser.UserId && _.ToUser.UserId == toUser.UserId
                        && _.Decision == Decision.Undecided);

            // Check if there is a pending request from this user. This is to prevent one user spamming another one with requests.
            if (pendingFriendRequest != null)
            {
                return ServiceResponseBuilder.Failure(
                    "You already have a pending friend request for this person.");
            }

            if (toUser == null || fromUser == null)
            {
                return ServiceResponseBuilder.Failure("Invalid user id.");
            }

            var match = fromUser.Friends.FirstOrDefault(_ => _.UserId == toUser.UserId);

            // Alrady in friends list.
            if (match != null)
            {
                return ServiceResponseBuilder.Failure("This user is already in your list of friends.");
            }

            // User tried to add themselves to the list of friends. App doesn't allow that so this is purely a security measure.
            if (friendRequestDTO.FromUser.UserId == friendRequestDTO.ToUser.UserId)
            {
                return ServiceResponseBuilder.Failure("You cannot send a friend request to yourself.");
            }

            // Create an new FriendRequest object and add it to the database.
            var friendRequest = new FriendRequest
                                    {
                                        Decision = Decision.Undecided,
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

            // Send appropriate notifications to both users.
            this.notificationManager.CreateAppNotification(
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

            this.notificationManager.CreateAppNotification(
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
            
            return ServiceResponseBuilder.Success();
        }

        /// <summary>
        /// Retrievers a given user's list of friends.
        /// </summary>
        /// <param name="userId">
        /// Unique identifier of of the user whose friends are to be retrieved.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<User>> GetFriends(int userId)
        {
            var currentUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .Include(_ => _.Friends)
                .FirstOrDefault(_ => _.UserId == userId);

            if (currentUser == null)
            {
                return ServiceResponseBuilder.Failure<List<User>>("Invalid user id");
            }

            // For security purposes, we only return part of the information about the user.
            var friends = (from user in currentUser.Friends.ToList()
                            select
                             new User
                             {
                                 UserId = user.UserId,
                                 FirstName = user.FirstName,
                                 LastName = user.LastName,
                                 UserName = user.UserName,
                                 Status = user.Status,
                                 UnreadMessagesCount = 0
                             }).ToList();

            // Let's calculate the number of unread messages for each friend.
            friends.ForEach(
                delegate(User u)
                    {
                        u.UnreadMessagesCount = this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable().Count(_ => _.RecipientId == userId && _.SenderId == u.UserId && !_.Read);
                    });

            return ServiceResponseBuilder.Success(friends);
        }

        /// <summary>
        /// Retrieves a specific friend request by its id.
        /// </summary>
        /// <param name="id">
        /// The unique identifier of the friend request object to be retrieved.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<FriendRequest> GetFriendRequest(int id)
        {
            // Retrieve the target FriendRequest from database.
            var friendRequest =
                this.findNDriveUnitOfWork.FriendRequestsRepository.AsQueryable()
                    .IncludeChildren().FirstOrDefault(_ => _.FriendRequestId == id);

            if (friendRequest == null)
            {
                return ServiceResponseBuilder.Failure<FriendRequest>("Invalid friend request id");
            }

            friendRequest.FromUser = new User
            {
                UserId = friendRequest.FromUser.UserId,
                FirstName = friendRequest.FromUser.FirstName,
                LastName = friendRequest.FromUser.LastName,
                UserName = friendRequest.FromUser.UserName
            };

            return ServiceResponseBuilder.Success(friendRequest);
        }

        /// <summary>
        /// Deletes a friend from a given user's friends list.
        /// </summary>
        /// <param name="friendDeletionDTO">
        /// The friend deletion dto - Contains the id's of both, the deleting friend and the friend to be deleted.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse DeleteFriend(FriendDeletionDTO friendDeletionDTO)
        {
            // Check if session is still valid, if not send unauthorised response to log the user out.
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            // Retrieve both users to enable Entity Framework tracking.
            var firstUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Include(_ => _.Friends).FirstOrDefault(_ => _.UserId == friendDeletionDTO.UserId);

            var secondUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .Include(_ => _.Friends).FirstOrDefault(_ => _.UserId == friendDeletionDTO.FriendId);

            if (firstUser == null || secondUser == null)
            {
                return ServiceResponseBuilder.Failure("Invalid user id");
            }

            if (!firstUser.Friends.Select(_ => _.UserId).Contains(secondUser.UserId) ||
                !secondUser.Friends.Select(_ => _.UserId).Contains(firstUser.UserId))
            {
                return ServiceResponseBuilder.Failure("Invalid friend id");
            }

            // Friends list have to be updated for both users.
            firstUser.Friends.Remove(secondUser);
            secondUser.Friends.Remove(firstUser);

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success();
        }
    }
}
