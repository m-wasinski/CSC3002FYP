// --------------------------------------------------------------------------------------------------------------------
// <copyright file="MessengerService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the MessengerService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.Services
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
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

    using Microsoft.Practices.ObjectBuilder2;

    /// <summary>
    /// The messenger service.
    /// </summary>
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.PerCall, ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class MessengerService : IMessengerService
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
        private Random random;

        /// <summary>
        /// Initializes a new instance of the <see cref="MessengerService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        /// <param name="notificationManager">
        /// </param>
        public MessengerService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.notificationManager = notificationManager;
            this.random = new Random(Guid.NewGuid().GetHashCode());
        }

        /// <summary>
        /// Adds a new message to the conversation history of two users.
        /// Depending on the current online status of the receiving user, the message will either
        /// be sent immediately or saved in the repository for later retrieval.
        /// </summary>
        /// <param name="chatMessageDTO">
        /// Contains the necessary information to construct a new ChatMessage object and add it to the database such as:
        /// Message Body, Information About Sending User, Information About Receiving User.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse SendMessage(ChatMessageDTO chatMessageDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            var targetUser = this.findNDriveUnitOfWork.UserRepository.Find(chatMessageDTO.RecipientId);
            var sendingUser = this.findNDriveUnitOfWork.UserRepository.Find(chatMessageDTO.SenderId);

            if (targetUser == null || sendingUser == null)
            {
                return ServiceResponseBuilder.Failure("Invalid sender or recipient id");
            }

            var newMessage = new ChatMessage
                                 {
                                     MessageBody = chatMessageDTO.MessageBody,
                                     Read = false,
                                     SenderId = chatMessageDTO.SenderId,
                                     RecipientId = chatMessageDTO.RecipientId,
                                     SentOnDate = chatMessageDTO.SentOnDate,
                                     SenderUserName = chatMessageDTO.SenderUserName,
                                     RecipientUserName = chatMessageDTO.RecipientUserName
                                 };

            this.findNDriveUnitOfWork.ChatMessageRepository.Add(newMessage);
            this.findNDriveUnitOfWork.Commit();

            this.notificationManager.SendInstantMessage(
                new Collection<User> { targetUser },
                GcmNotificationType.ChatMessage,
                sendingUser.UserId,
                sendingUser.UserId,
                newMessage,
                newMessage.ChatMessageId);

            return ServiceResponseBuilder.Success();
        }

        /// <summary>
        /// Retrieves all messages for a given conversation between two users.
        /// </summary>
        /// <param name="chatMessageRetrieverDTO">
        /// Contains the necessary unique identifiers for the two users for which the conversation should be retrieved.
        /// Also contains the LoadRangeDTO object used to determine how many items should be retrieved from the database.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<ChatMessage>> RetrieveMessages(ChatMessageRetrieverDTO chatMessageRetrieverDTO)
        {
            var messages =
                this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable()
                    .Where(
                        _ =>
                        (_.SenderId == chatMessageRetrieverDTO.SenderId
                        && _.RecipientId == chatMessageRetrieverDTO.RecipientId) || (_.SenderId == chatMessageRetrieverDTO.RecipientId && _.RecipientId == chatMessageRetrieverDTO.SenderId))
                    .OrderByDescending(x => x.SentOnDate).Skip(chatMessageRetrieverDTO.LoadRangeDTO.Skip).Take(chatMessageRetrieverDTO.LoadRangeDTO.Take);

            messages.ForEach(
                delegate(ChatMessage chatMessage)
                    {
                        if (!chatMessage.Read && chatMessage.RecipientId == chatMessageRetrieverDTO.SenderId)
                        {
                            chatMessage.Read = true;
                        }
                    });

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(messages.ToList());
        }

        /// <summary>
        /// Retrieves any new (unread) messages for a given conversation between two users.
        /// </summary>
        /// <param name="chatMessageRetrieverDTO">
        /// Contains the necessary unique identifiers for the two users for which the conversation should be retrieved.
        /// Also contains the LoadRangeDTO object used to determine how many items should be retrieved from the database.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<ChatMessage>> GetUnreadMessages(ChatMessageRetrieverDTO chatMessageRetrieverDTO)
        {
            var messages =
                this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable()
                    .Where(
                        _ =>
                        _.RecipientId == chatMessageRetrieverDTO.RecipientId && !_.Read
                        && _.SenderId == chatMessageRetrieverDTO.SenderId)
                    .OrderBy(x => x.SentOnDate).ToList();

            messages.ForEach(
                delegate(ChatMessage chatMessage)
                {
                    chatMessage.Read = true;
                });

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(messages);
        }

        /// <summary>
        /// Retrieves the total number of all new (unread) messages for all friends.
        /// </summary>
        /// <param name="userId">
        /// The unique identifier of the user for whom the count of unread messages should be retrieved.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<int> GetUnreadMessagesCount(int userId)
        {
            var unreadMessages =
                this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable().Count(_ => _.RecipientId == userId && !_.Read);

            return ServiceResponseBuilder.Success(unreadMessages);
        }

        /// <summary>
        /// Retrieves the total number of new (unread) messages for a specific friend.
        /// </summary>
        /// <param name="chatMessageRetrieverDTO">
        /// Contains the necessary unique identifiers for the two users for which the conversation should be retrieved.
        /// Also contains the LoadRangeDTO object used to determine how many items should be retrieved from the database.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<int> GetUnreadMessagesCountForFriend(ChatMessageRetrieverDTO chatMessageRetrieverDTO)
        {
            var unreadMessages =
                this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable().Count(_ => _.RecipientId == chatMessageRetrieverDTO.RecipientId && _.SenderId == chatMessageRetrieverDTO.SenderId && !_.Read);

            return ServiceResponseBuilder.Success(unreadMessages);
        }

        /// <summary>
        /// Retrieves a specific message by its id.
        /// </summary>
        /// <param name="id">
        /// The unique identifier of the ChatMessage to be retrieved from the database.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<ChatMessage> GetMessageById(int id)
        {
            var message =
                this.findNDriveUnitOfWork.ChatMessageRepository.Find(id);


            return message != null
                       ? ServiceResponseBuilder.Success(message)
                       : ServiceResponseBuilder.Failure<ChatMessage>("Invalid message id");
        }

        /// <summary>
        /// Marks a specific message as read.
        /// </summary>
        /// <param name="id">
        /// The unique identifier of the ChatMessage which is to be marked as read.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse MarkAsRead(int id)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            var unreadMessage = this.findNDriveUnitOfWork.ChatMessageRepository.Find(id);

            if (unreadMessage == null)
            {
                return ServiceResponseBuilder.Failure("Invalid message id");
            }

            if (!unreadMessage.Read)
            {
                unreadMessage.Read = true;
                this.findNDriveUnitOfWork.Commit();
            }

            return ServiceResponseBuilder.Success();
        }
    }
}
