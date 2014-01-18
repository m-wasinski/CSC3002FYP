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
    using System.ServiceModel;
    using System.ServiceModel.Activation;
    using System.ServiceModel.Activities;

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
        /// The _gcm manager.
        /// </summary>
        private readonly GCMManager gcmManager;


        public MessengerService()
        {
            
        }

        public MessengerService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, GCMManager gcmManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.gcmManager = gcmManager;
        }
        public ServiceResponse<bool> SendMessage(ChatMessageDTO chatMessageDTO)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(false);
            }

            var senderGCM = this.findNDriveUnitOfWork.UserRepository.Find(chatMessageDTO.SenderId).GCMRegistrationID;
            var recipientGCM = this.findNDriveUnitOfWork.UserRepository.Find(chatMessageDTO.RecipientId).GCMRegistrationID;

            if (recipientGCM != null && senderGCM != null)
            {   
                var settings = new JsonSerializerSettings();
                settings.DateFormatHandling = DateFormatHandling.MicrosoftDateFormat;
                this.gcmManager.SendMessage(
                    new Collection<string> { recipientGCM },
                    2,
                    0,
                    "Message",
                    JsonConvert.SerializeObject(chatMessageDTO, typeof(ChatMessageDTO), Formatting.Indented, settings));
                
                this.findNDriveUnitOfWork.ChatMessageRepository.Add(
                    new ChatMessage()
                        {
                            MessageBody = chatMessageDTO.MessageBody,
                            Read = false,
                            SenderId = chatMessageDTO.SenderId,
                            RecipientId = chatMessageDTO.RecipientId,
                            SentOnDate = chatMessageDTO.SentOnDate,
                            SenderUserName = chatMessageDTO.SenderUserName,
                            RecipientUserName = chatMessageDTO.RecipientUserName
                        });
                this.findNDriveUnitOfWork.Commit();

                return ResponseBuilder.Success(true);
            }

            return ResponseBuilder.Failure<bool>("Invalid sender or recipient id");
        }

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

        public ServiceResponse<bool> MarkMessagesAsReadForFriend(ChatMessageRetrieverDTO chatMessageRetrieverDTO)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(false);
            }

            var unreadMessages =
                this.findNDriveUnitOfWork.ChatMessageRepository.AsQueryable().Where(_ => _.RecipientId == chatMessageRetrieverDTO.RecipientId && _.SenderId == chatMessageRetrieverDTO.SenderId && !_.Read).ToList();

            unreadMessages.ForEach(delegate(ChatMessage chatMessage)
            {
                if (!chatMessage.Read)
                {
                    chatMessage.Read = true;
                }
            });

            this.findNDriveUnitOfWork.Commit();

            return ResponseBuilder.Success(true);
        }
    }
}
