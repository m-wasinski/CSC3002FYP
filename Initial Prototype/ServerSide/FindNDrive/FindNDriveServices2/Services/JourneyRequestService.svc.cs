// --------------------------------------------------------------------------------------------------------------------
// <copyright file="RequestService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the RequestService type.
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

    using ConcurrencyMode = System.ServiceModel.ConcurrencyMode;

    /// <summary>
    /// The request service.
    /// </summary>
    [ServiceBehavior(
           InstanceContextMode = InstanceContextMode.PerCall,
           ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class JourneyRequestService : IJourneyRequestService
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

        public JourneyRequestService()
        {

        }

        /// <summary>
        /// Initializes a new instance of the <see cref="JourneyRequestService"/> class.
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
        public JourneyRequestService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.notificationManager = notificationManager;
        }

        /// <summary>
        /// Performs appropriate validation and sends a journey request.
        /// </summary>
        /// <param name="journeyRequestDTO">
        /// JourneyRequestDTO
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<JourneyRequest> SendRequest(JourneyRequestDTO journeyRequestDTO)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new JourneyRequest());
            }

            // Retrieve the journey to which this particular request relates.
            var targetJourney = this.findNDriveUnitOfWork.JourneyRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.JourneyId == journeyRequestDTO.JourneyId);

            // Invalid journey Id, return failure immediately.
            if (targetJourney == null)
            {
                return ResponseBuilder.Failure<JourneyRequest>("Invalid journey Id");
            }

            // Check if this user is already participating in this journey. If yes, return a failure with appropriate error message.
            var alreadyInJourney =
                   targetJourney.Participants.FirstOrDefault(_ => _.UserId == journeyRequestDTO.UserId);

            if (alreadyInJourney != null)
            {
                return ResponseBuilder.Failure<JourneyRequest>("You are already one of the passengers in this journey.");
            }

            // Check if this user already has a pending request for this journey. This is to avoid user spamming the driver with request when no decision is made.
            var hasPendingRequest = this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == targetJourney.JourneyId && _.UserId == journeyRequestDTO.UserId && _.Decision == JourneyRequestDecision.Undecided);

            if (hasPendingRequest != null)
            {
                return ResponseBuilder.Failure<JourneyRequest>("You already have a pending request for this journey.");
            }

            // Also check if the user is not trying to join journey in which they are the driver.
            if (targetJourney.Driver.UserId == journeyRequestDTO.UserId)
            {
                return ResponseBuilder.Failure<JourneyRequest>("You are the driver in this journey.");
            }

            targetJourney.UnreadRequestsCount += 1;

            var targetUser =
                this.findNDriveUnitOfWork.UserRepository.Find(targetJourney.DriverId);

            var requestingUser =
                this.findNDriveUnitOfWork.UserRepository.Find(journeyRequestDTO.UserId);

            if (targetUser == null || requestingUser == null)
            {
                return ResponseBuilder.Failure<JourneyRequest>("Could not find user");
            }

            var receiverMessage = "You received a request from user: " + targetUser.UserName + " asking to join journey no: " + targetJourney.JourneyId
            + " " + targetJourney.GeoAddresses.First().AddressLine + " to " + targetJourney.GeoAddresses.Last().AddressLine;

            var senderMessage = "You sent a request to user: " + targetJourney.Driver.UserName + " asking to join journey no: " + targetJourney.JourneyId
            + " " + targetJourney.GeoAddresses.First().AddressLine + " to " + targetJourney.GeoAddresses.Last().AddressLine;

            this.findNDriveUnitOfWork.NotificationRepository.Add(new Notification { UserId = targetUser.UserId, NotificationBody = receiverMessage, Read = false, Context = NotificationContext.Positive, ReceivedOnDate = DateTime.Now, NotificationType = NotificationType.JourneyRequestReceived});

            this.findNDriveUnitOfWork.NotificationRepository.Add(new Notification { UserId = requestingUser.UserId, NotificationBody = senderMessage, Read = false, Context = NotificationContext.Neutral, ReceivedOnDate = DateTime.Now, NotificationType = NotificationType.JourneyRequestSent});

            var request = new JourneyRequest { JourneyId = journeyRequestDTO.JourneyId, UserId = journeyRequestDTO.UserId, Decision = JourneyRequestDecision.Undecided, Read = false, Message = journeyRequestDTO.Message, SentOnDate = journeyRequestDTO.SentOnDate};

            targetJourney.Requests.Add(request);
            this.findNDriveUnitOfWork.Commit();

            this.notificationManager.SendNotification(new Collection<User> { targetUser }, receiverMessage, NotificationType.JourneyRequestReceived, this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.JourneyRequestId == request.JourneyRequestId));

            return ResponseBuilder.Success(request);
        }

        /// <summary>
        /// The process decision.
        /// </summary>
        /// <param name="journeyRequestDTO">
        /// The car share request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<JourneyRequest> ProcessDecision(JourneyRequestDTO journeyRequestDTO)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new JourneyRequest());
            }

            var newPassenger = this.findNDriveUnitOfWork.UserRepository.Find(journeyRequestDTO.UserId);

            var journey =
                   this.findNDriveUnitOfWork.JourneyRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.JourneyId == journeyRequestDTO.JourneyId);

            var request = this.findNDriveUnitOfWork.JourneyRequestRepository.Find(
               journeyRequestDTO.JourneyRequestId);

            if (request == null)
            {
                return ResponseBuilder.Failure<JourneyRequest>("Invalid request id");
            }

            if (request.Decision != JourneyRequestDecision.Undecided)
            {
                return ResponseBuilder.Failure<JourneyRequest>(String.Format("This request has already been {0}", request.Decision == JourneyRequestDecision.Accepted ? "accepted" : "denied"));
            }

            if (journeyRequestDTO.Decision == JourneyRequestDecision.Accepted)
            {
                if (newPassenger != null && journey != null)
                {
                    if (journey.AvailableSeats > 0)
                    {
                        journey.Participants.Add(newPassenger);
                        journey.AvailableSeats -= 1;
                    }
                    else
                    {
                        return ResponseBuilder.Failure<JourneyRequest>("This journey is full.");
                    } 
                }
                else
                {
                    return ResponseBuilder.Failure<JourneyRequest>("Invalid journey id or passenger id");
                }
            }

            var decision = (journeyRequestDTO.Decision == JourneyRequestDecision.Accepted) ? "accepted." : "denied.";

           var message = "Your request to join journey id: " + journey.JourneyId + " from "
                              + journey.GeoAddresses.First().AddressLine + " to " + journey.GeoAddresses.Last().AddressLine
                              + " has been " + decision;
            
            this.findNDriveUnitOfWork.NotificationRepository.Add(new Notification
                                                    {   
                                                        UserId = newPassenger.UserId,
                                                        NotificationBody = message,
                                                        Read = false,
                                                        Context = journeyRequestDTO.Decision == JourneyRequestDecision.Accepted ? NotificationContext.Positive : NotificationContext.Negative,
                                                        ReceivedOnDate = DateTime.Now
                                                    });

            request.Decision = journeyRequestDTO.Decision;
            request.DecidedOnDate = journeyRequestDTO.DecidedOnDate;
            request.Read = true;

            if (journey.UnreadRequestsCount > 0)
            {
                journey.UnreadRequestsCount -= 1;
            }

            this.findNDriveUnitOfWork.Commit();

            this.notificationManager.SendNotification(new Collection<User> { newPassenger }, journeyRequestDTO.Decision == JourneyRequestDecision.Accepted ? "Accepted" : "Denied", journeyRequestDTO.Decision == JourneyRequestDecision.Accepted ? NotificationType.JourneyRequestAccepted : NotificationType.JourneyRequestDenied, message);
            return ResponseBuilder.Success(request);
        }

        /// <summary>
        /// The get all requests for journey.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<JourneyRequest>> GetAllRequestsForJourney(int id)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new List<JourneyRequest>());
            }

            var requests =
                this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.JourneyId == id).ToList();

            return ResponseBuilder.Success(requests);
        }

        /// <summary>
        /// The get all requests for user.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<JourneyRequest>> GetAllRequestsForUser(int id)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new List<JourneyRequest>());
            }

            var requests =
               this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable()
                   .IncludeAll()
                   .Where(_ => _.UserId == id).ToList();

            return ResponseBuilder.Success(requests);
        }
    }
}
