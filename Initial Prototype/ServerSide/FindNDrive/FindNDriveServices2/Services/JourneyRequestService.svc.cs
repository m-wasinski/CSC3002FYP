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
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new JourneyRequest());
            }

            // Retrieve the journey to which this particular request relates.
            var journey = this.findNDriveUnitOfWork.JourneyRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.JourneyId == journeyRequestDTO.JourneyId);

            // Invalid journey Id, return failure immediately.
            if (journey == null)
            {
                return ServiceResponseBuilder.Failure<JourneyRequest>("Invalid journey Id");
            }

            if (journey.JourneyStatus != JourneyStatus.OK)
            {
                return ServiceResponseBuilder.Failure<JourneyRequest>(string.Format("This journey is {0}, you request could not be sent.", journey.JourneyStatus == JourneyStatus.Cancelled ? "cancelled" : "expired"));
            }

            // Check if this user is already participating in this journey. If yes, return a failure with appropriate error message.
            var alreadyInJourney =
                   journey.Participants.FirstOrDefault(_ => _.UserId == journeyRequestDTO.UserId);

            if (alreadyInJourney != null)
            {
                return ServiceResponseBuilder.Failure<JourneyRequest>("You are already one of the passengers in this journey.");
            }

            // Check if this user already has a pending request for this journey. This is to avoid user spamming the driver with request when no decision is made.
            var hasPendingRequest = this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == journey.JourneyId && _.UserId == journeyRequestDTO.UserId && _.Decision == JourneyRequestDecision.Undecided);

            if (hasPendingRequest != null)
            {
                return ServiceResponseBuilder.Failure<JourneyRequest>("You already have a pending request for this journey.");
            }

            // Also check if the user is not trying to join journey in which they are the driver.
            if (journey.Driver.UserId == journeyRequestDTO.UserId)
            {
                return ServiceResponseBuilder.Failure<JourneyRequest>("You are the driver in this journey.");
            }

            journey.UnreadRequestsCount += 1;

            var targetUser =
                this.findNDriveUnitOfWork.UserRepository.Find(journey.DriverId);

            var requestingUser =
                this.findNDriveUnitOfWork.UserRepository.Find(journeyRequestDTO.UserId);

            if (targetUser == null || requestingUser == null)
            {
                return ServiceResponseBuilder.Failure<JourneyRequest>("Could not find user");
            }

            var request = new JourneyRequest { JourneyId = journeyRequestDTO.JourneyId, UserId = journeyRequestDTO.UserId, Decision = JourneyRequestDecision.Undecided, Read = false, Message = journeyRequestDTO.Message, SentOnDate = journeyRequestDTO.SentOnDate};

            journey.Requests.Add(request);
            this.findNDriveUnitOfWork.Commit();



            var receiverMessage =
                string.Format(
                    "You received a journey request from user: {0} asking to join journey no: {1} from: {2} to: {3} and their message is: {4}",
                    requestingUser.UserName,
                    journey.JourneyId,
                    journey.GeoAddresses.First().AddressLine,
                    journey.GeoAddresses.Last().AddressLine,
                    journeyRequestDTO.Message);

            var senderMessage =
                string.Format(
                    "You sent a journey request to user: {0}, asking to join journey no: {1}, from: {2} to: {3}.",
                    journey.Driver.UserName,
                    journey.JourneyId,
                journey.GeoAddresses.First().AddressLine, 
                journey.GeoAddresses.Last().AddressLine);

            this.notificationManager.SendAppNotification(new List<User> { targetUser }, "You have received a new journey request.", receiverMessage, NotificationContext.Positive, NotificationType.Both, NotificationContentType.JourneyRequestReceived, request);

            this.notificationManager.SendAppNotification(new List<User> { requestingUser }, "You have sent a new journey request.", senderMessage, NotificationContext.Positive, NotificationType.App, NotificationContentType.JourneyRequestSent, string.Empty);

            this.notificationManager.SendGcmNotification(new List<User> { targetUser }, "You have received a new journey request.", GcmNotificationType.NotificationTickle, string.Empty);

            return ServiceResponseBuilder.Success(request);
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
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new JourneyRequest());
            }

            var newPassenger = this.findNDriveUnitOfWork.UserRepository.Find(journeyRequestDTO.UserId);

            var journey =
                   this.findNDriveUnitOfWork.JourneyRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.JourneyId == journeyRequestDTO.JourneyId);

            if (journey == null)
            {
                return ServiceResponseBuilder.Failure<JourneyRequest>("Invalid journey id.");
            }

            if (journey.JourneyStatus != JourneyStatus.OK)
            {
                return ServiceResponseBuilder.Failure<JourneyRequest>(string.Format("This journey is {0}, No more requests can be accepted or denied.", journey.JourneyStatus == JourneyStatus.Cancelled ? "cancelled" : "expired"));
            }

            var request = this.findNDriveUnitOfWork.JourneyRequestRepository.Find(
               journeyRequestDTO.JourneyRequestId);

            if (request == null)
            {
                return ServiceResponseBuilder.Failure<JourneyRequest>("Invalid request id");
            }

            if (request.Decision != JourneyRequestDecision.Undecided)
            {
                return ServiceResponseBuilder.Failure<JourneyRequest>(string.Format("This request has already been {0}", request.Decision == JourneyRequestDecision.Accepted ? "accepted" : "denied"));
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
                        return ServiceResponseBuilder.Failure<JourneyRequest>("This journey is full.");
                    } 
                }
                else
                {
                    return ServiceResponseBuilder.Failure<JourneyRequest>("Invalid journey id or passenger id");
                }
            }

            var decision = (journeyRequestDTO.Decision == JourneyRequestDecision.Accepted) ? "accepted." : "denied.";

            var message =
                string.Format(
                    "Your request to join journey no: {0} from: {1} to: {2}, has been {3} by the driver {4} {5} ({6}))",
                    journey.JourneyId,
                    journey.GeoAddresses.First().AddressLine,
                    journey.GeoAddresses.Last().AddressLine,
                    decision,
                    journey.Driver.FirstName,
                    journey.Driver.LastName,
                    journey.Driver.UserName); 

            request.Decision = journeyRequestDTO.Decision;
            request.DecidedOnDate = journeyRequestDTO.DecidedOnDate;
            request.Read = true;

            if (journey.UnreadRequestsCount > 0)
            {
                journey.UnreadRequestsCount -= 1;
            }

            this.findNDriveUnitOfWork.Commit();

            this.notificationManager.SendAppNotification(
                new List<User> { newPassenger }, 
                journeyRequestDTO.Decision == JourneyRequestDecision.Accepted ? "Journey request accepted" : "Journey request denied",
                message,
                journeyRequestDTO.Decision == JourneyRequestDecision.Accepted
                    ? NotificationContext.Positive
                    : NotificationContext.Negative,
                NotificationType.Both,
                journeyRequestDTO.Decision == JourneyRequestDecision.Accepted
                    ? NotificationContentType.JourneyRequestAccepted
                    : NotificationContentType.JourneyRequestDenied,
                    string.Empty);

            var driversMessage =
                string.Format(
                    "You have {0} {1}'s request to join journey no: {2} from: {3} to: {4}.",
                    journeyRequestDTO.Decision == JourneyRequestDecision.Accepted ? "accepted" : "denied",
                    newPassenger.UserName,
                    journey.JourneyId,
                    journey.GeoAddresses.First().AddressLine,
                    journey.GeoAddresses.Last().AddressLine); 

            this.notificationManager.SendAppNotification(
                new List<User> { journey.Driver },
                string.Format("You have {0} a journey request.", journeyRequestDTO.Decision == JourneyRequestDecision.Accepted ? "accepted" : "denied"),
                driversMessage,
                NotificationContext.Neutral,
                NotificationType.App,
                journeyRequestDTO.Decision == JourneyRequestDecision.Accepted
                    ? NotificationContentType.JourneyRequestAccepted
                    : NotificationContentType.JourneyRequestDenied,
                string.Empty);

            this.notificationManager.SendGcmNotification(
                new List<User> { newPassenger },
                "Journey request reply",
                GcmNotificationType.NotificationTickle,
                string.Empty);

            return ServiceResponseBuilder.Success(request);
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
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new List<JourneyRequest>());
            }

            var requests =
                this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.JourneyId == id).ToList();

            return ServiceResponseBuilder.Success(requests);
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
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new List<JourneyRequest>());
            }

            var requests =
               this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable()
                   .IncludeAll()
                   .Where(_ => _.UserId == id).ToList();

            return ServiceResponseBuilder.Success(requests);
        }
    }
}
