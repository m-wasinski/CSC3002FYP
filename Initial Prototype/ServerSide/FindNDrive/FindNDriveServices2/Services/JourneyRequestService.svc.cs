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
        /// The _gcm manager.
        /// </summary>
        private readonly GCMManager gcmManager;


        public JourneyRequestService()
        {

        }

        public JourneyRequestService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, GCMManager gcmManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.gcmManager = gcmManager;
        }

        /// <summary>
        /// The send request.
        /// </summary>
        /// <param name="journeyRequestDTO">
        /// The car share request dto.
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

            var targetJourney = this.findNDriveUnitOfWork.JourneyRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.JourneyId == journeyRequestDTO.JourneyId);

            if (targetJourney == null)
            {
                return ResponseBuilder.Failure<JourneyRequest>("Invalud journey Id");
            }

            targetJourney.UnreadRequestsCount += 1;

            var targetUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == targetJourney.DriverId);

            var requestingUser =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == journeyRequestDTO.UserId);
            if (targetUser != null & requestingUser != null)
            {   
                this.findNDriveUnitOfWork.NotificationRepository.Add(new Notification
                        {   
                            UserId = targetUser.UserId,
                            NotificationBody =
                                "You received a request for journey: " + targetJourney.JourneyId
                                + " from: " + requestingUser.UserName,
                            Read = false,
                            Context = NotificationContext.Positive,
                            ReceivedOnDate = DateTime.Now
                        });

                this.findNDriveUnitOfWork.NotificationRepository.Add(new Notification
                {   
                    UserId = requestingUser.UserId,
                    NotificationBody =
                        "You sent a journey request to: " + targetUser.UserName
                        + " for journey id: " + targetJourney.JourneyId + " "
                        + targetJourney.DepartureAddress.AddressLine + " to "
                        + targetJourney.DestinationAddress.AddressLine,
                    Read = false,
                    Context = NotificationContext.Neutral,
                    ReceivedOnDate = DateTime.Now
                });

                var request = new JourneyRequest()
                                  {
                                      AddToTravelBuddies = journeyRequestDTO.AddToTravelBuddies,
                                      JourneyId = journeyRequestDTO.JourneyId,
                                      UserId = journeyRequestDTO.UserId,
                                      Decision = journeyRequestDTO.Decision,
                                      Read = journeyRequestDTO.Read,
                                      Message = journeyRequestDTO.Message,
                                      SentOnDate = journeyRequestDTO.SentOnDate,
                                  };

                targetJourney.Requests.Add(request);
                this.findNDriveUnitOfWork.Commit();

                //TODO
                /*this.gcmManager.SendMessage(
                    new Collection<string> { targetUser.GCMRegistrationID },
                    1,
                    0,
                    "New journey request!",
                    "You have received a new journey request from user " + requestingUser.UserName + " for journey id "
                    + targetJourney.JourneyId + " " + targetJourney.DepartureAddress.AddressLine + " to "
                    + targetJourney.DestinationAddress.AddressLine);*/

                return ResponseBuilder.Success(request);
            }

            return ResponseBuilder.Failure<JourneyRequest>("Invalud journey Id");
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

            var errors = new List<string>();

            var newPassenger = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.UserId == journeyRequestDTO.UserId);

            var journey =
                   this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                       .IncludeAll()
                       .FirstOrDefault(_ => _.JourneyId == journeyRequestDTO.JourneyId);

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
                        errors.Add("This car share is full.");
                        return ResponseBuilder.Failure<JourneyRequest>(errors);
                    } 
                }
                else
                {
                    return ResponseBuilder.Failure<JourneyRequest>("Invalid journey id or passenger id");
                }
            }

            var decision = (journeyRequestDTO.Decision == JourneyRequestDecision.Accepted)
                                         ? "accepted."
                                         : "denied.";
            var message = "Your request to join journey id: " + journey.JourneyId + " "
                          + journey.DepartureAddress.AddressLine + " to " + journey.DestinationAddress.AddressLine
                          + " has been " + decision;

            if (newPassenger != null)
            {
                this.findNDriveUnitOfWork.NotificationRepository.Add(new Notification
                {   
                    UserId = newPassenger.UserId,
                    NotificationBody = message,
                    Read = false,
                    Context = journeyRequestDTO.Decision == JourneyRequestDecision.Accepted ? NotificationContext.Positive : NotificationContext.Negative,
                    ReceivedOnDate = DateTime.Now
                });

                //TODO
                /*this.gcmManager.SendMessage(
                    new Collection<string> { newPassenger.GCMRegistrationID },
                    1,
                    0,
                    "Journey request " + decision,
                    message);*/
            }

            var request = this.findNDriveUnitOfWork.JourneyRequestRepository.Find(
                journeyRequestDTO.JourneyRequestId);

            request.Decision = journeyRequestDTO.Decision;
            request.DecidedOnDate = journeyRequestDTO.DecidedOnDate;

            this.findNDriveUnitOfWork.Commit();

            return ResponseBuilder.Success(request);
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
        public ServiceResponse<JourneyRequest> MarkAsRead(int id)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new JourneyRequest());
            }

            var request =
                this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable().IncludeAll()
                    .FirstOrDefault(_ => _.JourneyRequestId == id);

            if (request != null)
            {
                var journey = this.findNDriveUnitOfWork.JourneyRepository.Find(request.JourneyId);

                if (journey.UnreadRequestsCount > 0)
                {
                    journey.UnreadRequestsCount -= 1;
                }    

                request.Read = true;

                this.findNDriveUnitOfWork.Commit();
                return ResponseBuilder.Success(request);
            }

            return ResponseBuilder.Failure<JourneyRequest>("Invalid journey request!");
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
