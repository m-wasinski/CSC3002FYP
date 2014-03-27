// --------------------------------------------------------------------------------------------------------------------
// <copyright file="RequestService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the RequestService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.Services
{
    using System;
    using System.Collections.Generic;
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

        /// <summary>
        /// The random.
        /// </summary>
        private readonly Random random;

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
            this.random = new Random(Guid.NewGuid().GetHashCode());
        }

        /// <summary>
        /// Allows the user to send a request to the journey's driver.
        /// </summary>
        /// <param name="journeyRequestDTO">
        /// Contains the necessary information from which a proper JourneyRequest object is created and added to the database.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse SendRequest(JourneyRequestDTO journeyRequestDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            // Retrieve the journey to which this particular request relates.
            var journey = this.findNDriveUnitOfWork.JourneyRepository.AsQueryable().IncludeChildren().FirstOrDefault(_ => _.JourneyId == journeyRequestDTO.JourneyId);

            // Invalid journey Id, return failure immediately.
            if (journey == null)
            {
                return ServiceResponseBuilder.Failure("Invalid journey Id");
            }

            if (journey.JourneyStatus != JourneyStatus.OK)
            {
                return ServiceResponseBuilder.Failure(string.Format("This journey is {0}, you request could not be sent.", journey.JourneyStatus == JourneyStatus.Cancelled ? "cancelled" : "expired"));
            }

            // Check if this user is already participating in this journey. If yes, return a failure with appropriate error message.
            var alreadyInJourney =
                   journey.Passengers.FirstOrDefault(_ => _.UserId == journeyRequestDTO.FromUser.UserId);

            if (alreadyInJourney != null)
            {
                return ServiceResponseBuilder.Failure("You are already one of the passengers in this journey.");
            }

            // Check if this user already has a pending request for this journey. This is to avoid user spamming the driver with another when no decision is made.
            var hasPendingRequest = this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable()
                    .IncludeChildren()
                    .FirstOrDefault(_ => _.JourneyId == journeyRequestDTO.JourneyId && _.FromUser.UserId == journeyRequestDTO.FromUser.UserId
                        && _.Decision == Decision.Undecided);

            if (hasPendingRequest != null)
            {
                return ServiceResponseBuilder.Failure("You already have a pending request for this journey.");
            }

            // Also check if the user is not trying to join journey in which they are the driver.
            if (journey.Driver.UserId == journeyRequestDTO.FromUser.UserId)
            {
                return ServiceResponseBuilder.Failure("You are the driver in this journey.");
            }

            journey.UndecidedRequestsCount += 1;

            var targetUser =
                this.findNDriveUnitOfWork.UserRepository.Find(journey.Driver.UserId);

            var requestingUser =
                this.findNDriveUnitOfWork.UserRepository.Find(journeyRequestDTO.FromUser.UserId);

            if (targetUser == null || requestingUser == null)
            {
                return ServiceResponseBuilder.Failure("Invalid user id.");
            }

            var request = new JourneyRequest 
            { 
                JourneyId = journeyRequestDTO.JourneyId,
                Decision = Decision.Undecided, 
                FromUser = requestingUser,
                Read = false, Message = journeyRequestDTO.Message, 
                SentOnDate = DateTime.Now
            };

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

            this.notificationManager.CreateAppNotification(new List<User> { targetUser }, "You have received a new journey request.", receiverMessage, requestingUser.UserId, request.JourneyRequestId, NotificationType.Both, NotificationContentType.JourneyRequestReceived, this.random.Next());

            this.notificationManager.CreateAppNotification(new List<User> { requestingUser }, "You have sent a new journey request.", senderMessage, targetUser.UserId, -1, NotificationType.App, NotificationContentType.JourneyRequestSent, this.random.Next());

            this.notificationManager.SendGcmTickle(new List<User> { targetUser });

            return ServiceResponseBuilder.Success();
        }

        /// <summary>
        /// Used to process the driver's decision for a given journey request.
        /// Accepting a request adds a new passenger to the journey. Denying the request on the other hand has no effect on the journey.
        /// </summary>
        /// <param name="journeyRequestDTO">
        /// Contains the necessary information to find the relevant Journey Request in the database and update it with the new decision.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse ProcessDecision(JourneyRequestDTO journeyRequestDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            var newPassenger = this.findNDriveUnitOfWork.UserRepository.Find(journeyRequestDTO.FromUser.UserId);

            if (newPassenger == null)
            {
                return ServiceResponseBuilder.Failure("User with this id does not exist.");
            }

            var journey = this.findNDriveUnitOfWork.JourneyRepository.AsQueryable().IncludeChildren().FirstOrDefault(_ => _.JourneyId == journeyRequestDTO.JourneyId);

            if (journey == null)
            {
                return ServiceResponseBuilder.Failure("Invalid journey id.");
            }

            if (journey.JourneyStatus != JourneyStatus.OK)
            {
                return ServiceResponseBuilder.Failure(string.Format("This journey is {0}, No more requests can be accepted or denied.", journey.JourneyStatus == JourneyStatus.Cancelled ? "cancelled" : "expired"));
            }

            var request = this.findNDriveUnitOfWork.JourneyRequestRepository.Find(
               journeyRequestDTO.JourneyRequestId);

            if (request == null)
            {
                return ServiceResponseBuilder.Failure("Invalid request id");
            }

            if (request.Decision != Decision.Undecided)
            {
                return ServiceResponseBuilder.Failure(string.Format("This request has already been {0}", request.Decision == Decision.Accepted ? "accepted" : "denied"));
            }

            if (journeyRequestDTO.Decision == Decision.Accepted)
            {   
                // Add new passenger to this journey if there are spaces available.
                if (journey.AvailableSeats > 0)
                {
                    journey.Passengers.Add(newPassenger);
                    journey.AvailableSeats -= 1;
                }
                else
                {
                    return ServiceResponseBuilder.Failure<JourneyRequest>("This journey is full.");
                }
            }


            var decision = (journeyRequestDTO.Decision == Decision.Accepted) ? "accepted." : "denied.";

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
            request.DecidedOnDate = DateTime.Now;
            request.Read = true;

            if (journey.UndecidedRequestsCount > 0)
            {
                journey.UndecidedRequestsCount -= 1;
            }

            this.findNDriveUnitOfWork.Commit();

            // Send a message to the requesting user informing them of the driver's decision.
            this.notificationManager.CreateAppNotification(
                new List<User> { newPassenger },
                journeyRequestDTO.Decision == Decision.Accepted ? "Journey request accepted" : "Journey request denied",
                message,
                journey.Driver.UserId, journeyRequestDTO.Decision == Decision.Accepted ? journey.JourneyId : -1,
                NotificationType.Both,
                journeyRequestDTO.Decision == Decision.Accepted
                    ? NotificationContentType.JourneyRequestAccepted
                    : NotificationContentType.JourneyRequestDenied,
                    this.random.Next());
          
            var driversMessage =
                string.Format(
                    "You have {0} {1}'s request to join journey no: {2} from: {3} to: {4}.",
                    journeyRequestDTO.Decision == Decision.Accepted ? "accepted" : "denied",
                    newPassenger.UserName,
                    journey.JourneyId,
                    journey.GeoAddresses.First().AddressLine,
                    journey.GeoAddresses.Last().AddressLine);

            // Send a reminder to the driver informing them of their decision regarding this request.
            this.notificationManager.CreateAppNotification(
                new List<User> { journey.Driver },
                string.Format("You have {0} a journey request.", journeyRequestDTO.Decision == Decision.Accepted ? "accepted" : "denied"),
                driversMessage,
                newPassenger.UserId, request.JourneyRequestId,
                NotificationType.App,
                journeyRequestDTO.Decision == Decision.Accepted
                    ? NotificationContentType.JourneyRequestAccepted
                    : NotificationContentType.JourneyRequestDenied,
                this.random.Next());

            // Send a tickle to ensure their devices are in sync with the server.
            this.notificationManager.SendGcmTickle(new List<User> { newPassenger });

            return ServiceResponseBuilder.Success();
        }

        /// <summary>
        /// Requests all requests for a given journey.
        /// </summary>
        /// <param name="id">
        /// The unique identifier of the journey for which requests should be retrieved.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<JourneyRequest>> GetAllRequestsForJourney(int id)
        {
            var requests =
               (from journeyRequest in this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable()
                   .IncludeChildren()
                   .Where(_ => _.JourneyId == id).ToList()
                select new JourneyRequest
                {
                    JourneyRequestId = journeyRequest.JourneyRequestId,
                    DecidedOnDate = journeyRequest.DecidedOnDate,
                    Decision = journeyRequest.Decision,
                    FromUser = new User
                    {
                        UserId = journeyRequest.FromUser.UserId,
                        FirstName = journeyRequest.FromUser.FirstName,
                        LastName = journeyRequest.FromUser.LastName,
                        UserName = journeyRequest.FromUser.UserName
                    },
                    Journey = journeyRequest.Journey,
                    JourneyId = journeyRequest.JourneyId,
                    Message = journeyRequest.Message,
                    Read = journeyRequest.Read,
                    SentOnDate = journeyRequest.SentOnDate
                }).ToList();

            return ServiceResponseBuilder.Success(requests);
        }

        /// <summary>
        /// Retrieves specific journey request by its id.
        /// </summary>
        /// <param name="id">
        /// The unique identifier of the journey request to be retrieved.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<JourneyRequest> GetJourneyRequest(int id)
        {
            var request =
               this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable()
                   .IncludeChildren()
                   .FirstOrDefault(_ => _.JourneyRequestId == id);

            if (request == null)
            {
                return ServiceResponseBuilder.Failure<JourneyRequest>("Request with this id does not exist.");
            }

            // For security purposes, we only retrieve the basic information about the user.
            request.FromUser = new User
                                       {
                                           UserId = request.FromUser.UserId,
                                           UserName = request.FromUser.UserName,
                                           FirstName = request.FromUser.FirstName,
                                           LastName = request.FromUser.LastName
                                       };

            return ServiceResponseBuilder.Success(request);
        }
    }
}
