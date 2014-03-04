namespace FindNDriveServices2.Services
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;
    using System.Web.UI.WebControls.WebParts;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;
    using FindNDriveServices2.ServiceUtils;

    using Microsoft.Practices.ObjectBuilder2;

    /// <summary>
    /// The journey chat service.
    /// </summary>
    [ServiceBehavior(
           InstanceContextMode = InstanceContextMode.PerCall,
           ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class JourneyChatService : IJourneyChatService
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
        /// Initializes a new instance of the <see cref="JourneyChatService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        /// <param name="notificationManager">
        /// The notification manager.
        /// </param>
        public JourneyChatService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.notificationManager = notificationManager;
        }

        public ServiceResponse<bool> SendMessage(JourneyMessageDTO journeyMessageDTO)
        {   
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(false);
            }

            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == journeyMessageDTO.JourneyId);

            if (journey == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid journey id.");
            }

            var sender = this.findNDriveUnitOfWork.UserRepository.Find(journeyMessageDTO.SenderId);

            if (sender == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid sender id.");
            }

            var journeyMessage = new JourneyMessage
                                     {
                                         JourneyId = journeyMessageDTO.JourneyId,
                                         SenderId = sender.UserId,
                                         MessageBody = journeyMessageDTO.MessageBody,
                                         SenderUsername = journeyMessageDTO.SenderUsername,
                                         SentOnDate = journeyMessageDTO.SentOnDate,
                                         SeenBy = new Collection<User> { sender }
                                     };

            this.findNDriveUnitOfWork.JourneyMessageRepository.Add(journeyMessage);
            
            this.findNDriveUnitOfWork.Commit();

            var participants = journey.Participants.Where(_ => _.UserId != sender.UserId).ToList();

            if (sender.UserId != journey.Driver.UserId)
            {
                participants.Add(journey.Driver);
            }

            this.notificationManager.SendInstantMessage(
                participants,
                GcmNotificationType.JourneyChatMessage,
                -1,
                journey.JourneyId,
                journeyMessage,
                journeyMessage.JourneyMessageId);

            return ServiceResponseBuilder.Success(true);
        }

        public ServiceResponse<List<JourneyMessage>> RetrieveMessages(JourneyMessageRetrieverDTO journeyMessageRetrieverDTO)
        {   
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new List<JourneyMessage>());
            }

            var user = this.findNDriveUnitOfWork.UserRepository.Find(journeyMessageRetrieverDTO.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<List<JourneyMessage>>("Invalid user id");
            }

            var messages =
                this.findNDriveUnitOfWork.JourneyMessageRepository.AsQueryable().IncludeAll()
                    .Where(_ => _.JourneyId == journeyMessageRetrieverDTO.JourneyId).OrderByDescending(x => x.SentOnDate)
                    .Skip(journeyMessageRetrieverDTO.LoadRangeDTO.Skip).Take(journeyMessageRetrieverDTO.LoadRangeDTO.Take);

            messages.ForEach(
                delegate(JourneyMessage journeyMessage)
                    {
                        var ids = journeyMessage.SeenBy.Select(_ => _.UserId).ToList();

                        if (!ids.Contains(journeyMessageRetrieverDTO.UserId))
                        {
                            journeyMessage.SeenBy.Add(user);
                        }
                    });

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(messages.ToList());
        }

        public ServiceResponse<List<JourneyMessage>> RetrieveUnreadMessages(JourneyMessageRetrieverDTO journeyMessageRetrieverDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new List<JourneyMessage>());
            }

            var user = this.findNDriveUnitOfWork.UserRepository.Find(journeyMessageRetrieverDTO.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<List<JourneyMessage>>("Invalid user id");
            }

            var messages =
                this.findNDriveUnitOfWork.JourneyMessageRepository.AsQueryable().IncludeAll()
                    .Where(_ => _.JourneyId == journeyMessageRetrieverDTO.JourneyId && !_.SeenBy.Select(x => x.UserId).ToList().Contains(user.UserId)).OrderBy(x => x.SentOnDate).ToList();

            messages.ForEach(journeyMessage => journeyMessage.SeenBy.Add(user));

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(messages);
        }

        public ServiceResponse<bool> MarkAsRead(JourneyMessageMarkerDTO journeyMessageMarkerDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(false);
            }

            var user = this.findNDriveUnitOfWork.UserRepository.Find(journeyMessageMarkerDTO.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid user id");
            }

            var message =
                this.findNDriveUnitOfWork.JourneyMessageRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyMessageId == journeyMessageMarkerDTO.JourneyMessageId);

            if (message == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid message id");
            }

            message.SeenBy.Add(user);
            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(true);
        }

        public ServiceResponse<JourneyMessage> GetJourneyMessageById(int id)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new JourneyMessage());
            }

            var journeyMessage =
                this.findNDriveUnitOfWork.JourneyMessageRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyMessageId == id);

            return journeyMessage == null
                       ? ServiceResponseBuilder.Failure<JourneyMessage>("Invalid message id")
                       : ServiceResponseBuilder.Success(journeyMessage);
        }

        public ServiceResponse<int> GetUnreadMessagesCount(JourneyMessageRetrieverDTO journeyMessageRetrieverDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(0);
            }

            var user = this.findNDriveUnitOfWork.UserRepository.Find(journeyMessageRetrieverDTO.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<int>("Invalid user id");
            }

            var messages =
                this.findNDriveUnitOfWork.JourneyMessageRepository.AsQueryable().IncludeAll()
                    .Where(_ => _.JourneyId == journeyMessageRetrieverDTO.JourneyId).ToList();

            var count = messages.Count(
                delegate(JourneyMessage journeyMessage)
                {
                    var ids = journeyMessage.SeenBy.Select(_ => _.UserId).ToList();

                    return !ids.Contains(journeyMessageRetrieverDTO.UserId);
                });

            return ServiceResponseBuilder.Success(count);
        }
    }
}
