// --------------------------------------------------------------------------------------------------------------------
// <copyright file="JourneyChatService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   The journey chat service.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.Services
{
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

        /// <summary>
        /// Allows users to send a new message in the journey chat room.
        /// </summary>
        /// <param name="journeyMessageDTO">
        /// Contains the necessary information to construct a new JourneyMessage object and save it in the database.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse SendMessage(JourneyMessageDTO journeyMessageDTO)
        {   
            // Check if current session is still valid, otherwise log the user out.
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == journeyMessageDTO.JourneyId);

            if (journey == null)
            {
                return ServiceResponseBuilder.Failure("Invalid journey id.");
            }

            var sender = this.findNDriveUnitOfWork.UserRepository.Find(journeyMessageDTO.SenderId);

            if (sender == null)
            {
                return ServiceResponseBuilder.Failure("Invalid sender id.");
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

            var participants = journey.Passengers.Where(_ => _.UserId != sender.UserId).ToList();

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

            return ServiceResponseBuilder.Success();
        }

        /// <summary>
        /// Retrieves conversation history for a given journey chat room.
        /// </summary>
        /// <param name="journeyMessageRetrieverDTO">
        /// Contains of the unique identifier of the user retrieving the messages as well as 
        /// the unique identifier of the journey for which the messages should be retrieved.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<JourneyMessage>> RetrieveMessages(JourneyMessageRetrieverDTO journeyMessageRetrieverDTO)
        {
            // Check if current session is still valid, otherwise log the user out.
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised<List<JourneyMessage>>();
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

            // Mark each unread message as read now that the user will receive them.
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

        /// <summary>
        /// Retrieves all unread messages for a given journey.
        /// </summary>
        /// <param name="journeyMessageRetrieverDTO">
        /// Contains of the unique identifier of the user retrieving the messages as well as 
        /// the unique identifier of the journey for which the messages should be retrieved.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<JourneyMessage>> RetrieveUnreadMessages(JourneyMessageRetrieverDTO journeyMessageRetrieverDTO)
        {
            // Check if current session is still valid, otherwise log the user out.
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised<List<JourneyMessage>>();
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

        /// <summary>
        /// Marks a given journey chat message as read.
        /// </summary>
        /// <param name="journeyMessageMarkerDTO">
        /// Contains the necessary information to identify the journey message 
        /// to be marked as read and the identifier of the user who has read the message.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse MarkAsRead(JourneyMessageMarkerDTO journeyMessageMarkerDTO)
        {
            // Check if current session is still valid, otherwise log the user out.
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            var user = this.findNDriveUnitOfWork.UserRepository.Find(journeyMessageMarkerDTO.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure("Invalid user id");
            }

            var message =
                this.findNDriveUnitOfWork.JourneyMessageRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyMessageId == journeyMessageMarkerDTO.JourneyMessageId);

            if (message == null)
            {
                return ServiceResponseBuilder.Failure("Invalid message id");
            }

            message.SeenBy.Add(user);
            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(true);
        }

        /// <summary>
        /// Retrieves a specific journey chat message by its id.
        /// </summary>
        /// <param name="id">
        /// The unique identifier of the Journey Message to be retrieved.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<JourneyMessage> GetJourneyMessageById(int id)
        {
            var journeyMessage =
                this.findNDriveUnitOfWork.JourneyMessageRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyMessageId == id);

            return journeyMessage == null
                       ? ServiceResponseBuilder.Failure<JourneyMessage>("Invalid message id")
                       : ServiceResponseBuilder.Success(journeyMessage);
        }

        /// <summary>
        /// Retrieves the number of unread messages for a given journey.
        /// </summary>
        /// <param name="journeyMessageRetrieverDTO">
        /// Contains of the unique identifier of the user retrieving the messages as well as 
        /// the unique identifier of the journey for which the messages should be retrieved.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<int> GetUnreadMessagesCount(JourneyMessageRetrieverDTO journeyMessageRetrieverDTO)
        {
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
