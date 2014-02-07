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
        /// The _gcm manager.
        /// </summary>
        private readonly GCMManager gcmManager;

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
        public FriendsService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, GCMManager gcmManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.gcmManager = gcmManager;
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

            this.findNDriveUnitOfWork.FriendRequestsRepository.Add(
                new FriendRequest
                    {
                        FriendRequestDecision = FriendRequestDecision.Undecided,
                        SentOnDate = DateTime.Now,
                        Read = false,
                        RequestingUserId = friendRequestDTO.RequestingUserId,
                        TargetUserId = friendRequestDTO.TargetUserId,
                        Message = friendRequestDTO.Message
                    });

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
                                                 NotificationType = NotificationType.JourneyRequestReceived
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
                    NotificationType = NotificationType.JourneyRequestReceived
                });

            var message = JsonConvert.SerializeObject(
                targetUserNotification,
                typeof(FriendRequest),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            if (targetUser.Status == Status.Online)
            {
                this.gcmManager.SendFriendRequest(new Collection<string> { targetUser.GCMRegistrationID }, message);
            }
            else
            {
                this.findNDriveUnitOfWork.GCMNotificationsRepository.Add(new GCMNotification
                {
                    UserId = targetUser.UserId,
                    Delivered = false,
                    ContentTitle = "Friend Request",
                    NotificationType = NotificationType.FriendRequest,
                    NotificationMessage = message
                });
            }

            this.findNDriveUnitOfWork.Commit();
            return ResponseBuilder.Success(true);
        }

        /// <summary>
        /// The mark as read.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<bool> MarkAsRead(int id)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(false);
            }

            var request = this.findNDriveUnitOfWork.FriendRequestsRepository.Find(id);

            if (request == null)
            {
                return ResponseBuilder.Failure<bool>("Invalid friend request id");
            }

            request.Read = true;
            this.findNDriveUnitOfWork.Commit();

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
