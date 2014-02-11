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
    using System.Security.Cryptography.X509Certificates;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    using Newtonsoft.Json;

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
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(false);
            }

            var targetUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .IncludeAll()
                .FirstOrDefault(_ => _.UserId == friendRequestDTO.TargetUserId);

            var requestingUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .IncludeAll()
                .FirstOrDefault(_ => _.UserId == friendRequestDTO.RequestingUserId);

            if (targetUser == null || requestingUser == null)
            {
                return ResponseBuilder.Failure<bool>("Invalid user id.");
            }

            var targetRequest = this.findNDriveUnitOfWork.FriendRequestsRepository.Find(
                friendRequestDTO.FriendRequestId);

            targetRequest.DecidedOnDate = DateTime.Now;
            targetRequest.Read = true;
            targetRequest.FriendRequestDecision = friendRequestDTO.FriendRequestDecision;

            if (friendRequestDTO.FriendRequestDecision == FriendRequestDecision.Accepted)
            {
                var match = targetUser.Friends.FirstOrDefault(_ => _.UserId == requestingUser.UserId);

                if (match == null)
                {
                    targetUser.Friends.Add(requestingUser);
                    requestingUser.Friends.Add(targetUser);
                }
                else
                {
                    return ResponseBuilder.Failure<bool>("This user is already in your list of friends.");
                }
            }

            this.findNDriveUnitOfWork.Commit();
            return ResponseBuilder.Success(true);
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
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(false);
            }

            var targetUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == friendRequestDTO.TargetUserId);

            var requestingUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == friendRequestDTO.RequestingUserId);

            if (targetUser == null || requestingUser == null)
            {
                return ResponseBuilder.Failure<bool>("Invalid user id.");
            }

            var match = requestingUser.Friends.FirstOrDefault(_ => _.UserId == targetUser.UserId);

            if (match != null)
            {
                return ResponseBuilder.Failure<bool>("This user is already in your list of friends.");
            }

            if (friendRequestDTO.TargetUserId == friendRequestDTO.RequestingUserId)
            {
                return ResponseBuilder.Failure<bool>("You cannot invite yourself to your friends list.");
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
                                            targetUser.FirstName + " " + targetUser.LastName + " ("
                                            + targetUser.UserName + ")",
                                        RequestingUserName =
                                            requestingUser.FirstName + " " + requestingUser.LastName + " ("
                                            + requestingUser.UserName + ")"
                                    };

            this.findNDriveUnitOfWork.FriendRequestsRepository.Add(friendRequest);

            var receiverMessage = "You received a friend request from user: {0} {1} ({2})";
            var senderMessage = "You sent a friend request to user: {0} {1} ({2})";

            var targetUserNotification = new Notification
                                             {
                                                 UserId = targetUser.UserId,
                                                 NotificationBody =
                                                     String.Format(
                                                         receiverMessage,
                                                         requestingUser.FirstName,
                                                         requestingUser.LastName,
                                                         requestingUser.UserName),
                                                 Read = false,
                                                 Context = NotificationContext.Positive,
                                                 ReceivedOnDate = DateTime.Now,
                                                 NotificationType = NotificationType.FriendRequest
                                             };

            this.findNDriveUnitOfWork.NotificationRepository.Add(targetUserNotification);

            this.findNDriveUnitOfWork.NotificationRepository.Add(
                new Notification
                {
                    UserId = requestingUser.UserId,
                    NotificationBody =
                        String.Format(
                            senderMessage,
                            targetUser.FirstName,
                            targetUser.LastName,
                            targetUser.UserName),
                    Read = false,
                    Context = NotificationContext.Positive,
                    ReceivedOnDate = DateTime.Now,
                    NotificationType = NotificationType.FriendRequest
                });

            this.findNDriveUnitOfWork.Commit();


            this.notificationManager.SendNotification(
                new Collection<User> { targetUser },
                "New friend request",
                NotificationType.FriendRequest,
                friendRequest);

            return ResponseBuilder.Success(true);
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
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new List<User>());
            }

            var user = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .IncludeAll()
                .FirstOrDefault(_ => _.UserId == userId);

            if (user != null)
            {
                var friends =
                    user
                        .Friends.ToList();

                return ResponseBuilder.Success(friends);
            }

            return ResponseBuilder.Failure<List<User>>("Invalid user id");
        }
    }
}
