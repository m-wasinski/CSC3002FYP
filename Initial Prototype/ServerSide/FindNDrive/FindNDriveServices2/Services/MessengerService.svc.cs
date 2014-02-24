// --------------------------------------------------------------------------------------------------------------------
// <copyright file="MessengerService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the MessengerService type.
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

    using Newtonsoft.Json;

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
        /// The send message.
        /// </summary>
        /// <param name="chatMessageDTO">
        /// The chat message dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<bool> SendMessage(ChatMessageDTO chatMessageDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(false);
            }

            var targetUser = this.findNDriveUnitOfWork.UserRepository.Find(chatMessageDTO.RecipientId);
            var sendingUser = this.findNDriveUnitOfWork.UserRepository.Find(chatMessageDTO.SenderId);

            if (targetUser == null || sendingUser == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid sender or recipient id");
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
                sendingUser.ProfilePictureId,
                sendingUser.UserId,
                newMessage);

            return ServiceResponseBuilder.Success(true);
        }

        /// <summary>
        /// The retrieve messages.
        /// </summary>
        /// <param name="chatMessageRetrieverDTO">
        /// The chat message retriever dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<ChatMessage>> RetrieveMessages(ChatMessageRetrieverDTO chatMessageRetrieverDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new List<ChatMessage>());
            }

            var messages =
                this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable()
                    .Where(
                        _ =>
                        (_.SenderId == chatMessageRetrieverDTO.SenderId
                        && _.RecipientId == chatMessageRetrieverDTO.RecipientId) || (_.SenderId == chatMessageRetrieverDTO.RecipientId && _.RecipientId == chatMessageRetrieverDTO.SenderId))
                    .OrderBy(x => x.SentOnDate).ToList();

            messages = LoadRangeHelper<ChatMessage>.GetConversations(messages, chatMessageRetrieverDTO.LoadRangeDTO.Index, chatMessageRetrieverDTO.LoadRangeDTO.Count, chatMessageRetrieverDTO.LoadRangeDTO.LoadMoreData);

            messages.ForEach(
                delegate(ChatMessage chatMessage)
                    {
                        if (!chatMessage.Read && chatMessage.RecipientId == chatMessageRetrieverDTO.SenderId)
                        {
                            chatMessage.Read = true;
                        }
                    });

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(messages);
        }

        /// <summary>
        /// The get unread messages.
        /// </summary>
        /// <param name="chatMessageRetrieverDTO">
        /// The chat message retriever dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<ChatMessage>> GetUnreadMessages(ChatMessageRetrieverDTO chatMessageRetrieverDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new List<ChatMessage>());
            }

            var messages =
                this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable()
                    .Where(
                        _ => _.RecipientId == chatMessageRetrieverDTO.RecipientId && !_.Read && _.SenderId == chatMessageRetrieverDTO.SenderId ).OrderBy(x => x.SentOnDate).ToList();

            messages = LoadRangeHelper<ChatMessage>.GetConversations(messages, chatMessageRetrieverDTO.LoadRangeDTO.Index, chatMessageRetrieverDTO.LoadRangeDTO.Count, chatMessageRetrieverDTO.LoadRangeDTO.LoadMoreData);

            messages.ForEach(
                delegate(ChatMessage chatMessage)
                {
                    chatMessage.Read = true;
                });

            this.findNDriveUnitOfWork.Commit();
            return ServiceResponseBuilder.Success(messages);
        }

        /// <summary>
        /// The get unread messages count.
        /// </summary>
        /// <param name="userId">
        /// The user id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<int> GetUnreadMessagesCount(int userId)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(0);
            }

            var unreadMessages =
                this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable().Count(_ => _.RecipientId == userId && !_.Read);

            return ServiceResponseBuilder.Success(unreadMessages);
        }

        /// <summary>
        /// The get unread messages count for friend.
        /// </summary>
        /// <param name="chatMessageRetrieverDTO">
        /// The chat message retriever dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<int> GetUnreadMessagesCountForFriend(ChatMessageRetrieverDTO chatMessageRetrieverDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(0);
            }

            var unreadMessages =
                this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable().Count(_ => _.RecipientId == chatMessageRetrieverDTO.RecipientId && _.SenderId == chatMessageRetrieverDTO.SenderId && !_.Read);

            return ServiceResponseBuilder.Success(unreadMessages);
        }

        public ServiceResponse<bool> MarkAsRead(int id)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(false);
            }

            var unreadMessage = this.findNDriveUnitOfWork.ChatMessageRepository.Find(id);

            if (unreadMessage == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid message id");
            }

            if (!unreadMessage.Read)
            {
                unreadMessage.Read = true;
                this.findNDriveUnitOfWork.Commit();
            }

            return ServiceResponseBuilder.Success(true);
        }
    }
}
