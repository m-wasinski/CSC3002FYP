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
    using System.Runtime.Serialization.Json;
    using System.Security.Cryptography;
    using System.ServiceModel;
    using System.ServiceModel.Activation;
    using System.ServiceModel.Activities;
    using System.Web.Services.Protocols;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    using Microsoft.Practices.ObjectBuilder2;

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
        /// The _gcm manager.
        /// </summary>
        private readonly GCMManager gcmManager;

        public MessengerService()
        {
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="MessengerService"/> class.
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
        public MessengerService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, GCMManager gcmManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.gcmManager = gcmManager;
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
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(false);
            }

            var targetUser = this.findNDriveUnitOfWork.UserRepository.Find(chatMessageDTO.RecipientId);
            var sendingUser = this.findNDriveUnitOfWork.UserRepository.Find(chatMessageDTO.SenderId);

            if (targetUser == null || sendingUser == null)
            {
                return ResponseBuilder.Failure<bool>("Invalid sender or recipient id");
            }

            var newMessage = new ChatMessage()
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

            chatMessageDTO.ChatMessageId = newMessage.ChatMessageId;

            var message = JsonConvert.SerializeObject(
                chatMessageDTO,
                typeof(ChatMessageDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            if (targetUser.Status == Status.Online)
            {
                this.gcmManager.SendNotification(
                    new Collection<string> { targetUser.GCMRegistrationID },
                    GCMNotificationType.InstantMessenger,
                    "Message",
                    message);
            }
            else
            {
                this.findNDriveUnitOfWork.GCMNotificationsRepository.Add(
                    new GCMNotification
                        {
                            UserId = targetUser.UserId,
                            Delivered = false,
                            ContentTitle = "Message",
                            NotificationType = GCMNotificationType.InstantMessenger,
                            NotificationMessage = message
                        });
                this.findNDriveUnitOfWork.Commit();
            }

            return ResponseBuilder.Success(true);
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
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new List<ChatMessage>());
            }

            var messages =
                this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable()
                    .Where(
                        _ =>
                        (_.SenderId == chatMessageRetrieverDTO.SenderId
                        && _.RecipientId == chatMessageRetrieverDTO.RecipientId) || (_.SenderId == chatMessageRetrieverDTO.RecipientId && _.RecipientId == chatMessageRetrieverDTO.SenderId))
                    .ToList();

            return ResponseBuilder.Success(messages);
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
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(0);
            }

            var unreadMessages =
                this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable().Count(_ => _.RecipientId == userId && !_.Read);

            return ResponseBuilder.Success(unreadMessages);
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
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(0);
            }

            var unreadMessages =
                this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable().Count(_ => _.RecipientId == chatMessageRetrieverDTO.RecipientId && _.SenderId == chatMessageRetrieverDTO.SenderId && !_.Read);

            return ResponseBuilder.Success(unreadMessages);
        }

        /// <summary>
        /// The mark messages as read.
        /// </summary>
        /// <param name="chatMessageDtos">
        /// The chat message dtos.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<bool> MarkMessagesAsRead(List<ChatMessageDTO> chatMessageDtos)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(false);
            }

            var messages = chatMessageDtos.Select(chatmessagedto => this.findNDriveUnitOfWork.ChatMessageRepository.Find(chatmessagedto.ChatMessageId)).ToList();

            messages.ForEach(
                delegate(ChatMessage chatMessage)
                    {
                        chatMessage.Read = true;
                    });

            this.findNDriveUnitOfWork.Commit();

            return ResponseBuilder.Success(true);
        }
    }
}
