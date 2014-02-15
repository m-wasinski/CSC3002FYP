// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Service.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the CarShareService type.
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

    using FindNDriveInfrastructureCore;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The car share service.
    /// </summary>
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.PerCall, ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class JourneyService : IJourneyService
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="JourneyService"/> class.
        /// </summary>
        public JourneyService()
        {
        }

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
        /// Initializes a new instance of the <see cref="JourneyService"/> class. 
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find N Drive Unit Of Work.
        /// </param>
        /// <param name="sessionManager">
        /// The session Manager.
        /// </param>
        /// <param name="notificationManager">
        /// </param>
        public JourneyService(
            FindNDriveUnitOfWork findNDriveUnitOfWork,
            SessionManager sessionManager,
            NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.notificationManager = notificationManager;
        }

        /// <summary>
        /// The get all journeys for user.
        /// </summary>
        /// <param name="loadRangeDTO">
        /// The load range dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<Journey>> GetAllJourneysForUser(LoadRangeDTO loadRangeDTO)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new List<Journey>());
            }

            var journeys =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.DriverId == loadRangeDTO.Id || _.Participants.Any(x => x.UserId == loadRangeDTO.Id))
                    .OrderByDescending(x => x.CreationDate)
                    .ToList();

            journeys.ForEach(
                delegate(Journey journey)
                    {
                        if (journey.DateAndTimeOfDeparture < DateTime.Now
                            && journey.JourneyStatus != JourneyStatus.Expired)
                        {
                            journey.JourneyStatus = JourneyStatus.Expired;
                        }
                    });

            journeys = LoadRangeHelper<Journey>.GetValidRange(
                journeys,
                loadRangeDTO.Index,
                loadRangeDTO.Count,
                loadRangeDTO.LoadMoreData);

            this.findNDriveUnitOfWork.Commit();

            return ResponseBuilder.Success(journeys);
        }

        /// <summary>
        /// The create new journey.
        /// </summary>
        /// <param name="journeyDTO">
        /// The journey dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<Journey> CreateNewJourney(JourneyDTO journeyDTO)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new Journey());
            }

            var validatedJourney = ValidationHelper.Validate(journeyDTO);
            Journey newJourney = null;

            if (!validatedJourney.IsValid)
            {
                return ResponseBuilder.Failure<Journey>("Validation error");
            }

            newJourney = new Journey
                             {
                                 AvailableSeats = journeyDTO.AvailableSeats,
                                 DateAndTimeOfDeparture = journeyDTO.DateAndTimeOfDeparture,
                                 Description = journeyDTO.Description,
                                 DriverId = journeyDTO.DriverId,
                                 Fee = journeyDTO.Fee,
                                 Private = journeyDTO.Private,
                                 SmokersAllowed = journeyDTO.SmokersAllowed,
                                 VehicleType = journeyDTO.VehicleType,
                                 PetsAllowed = journeyDTO.PetsAllowed,
                                 JourneyStatus = JourneyStatus.OK,
                                 GeoAddresses = journeyDTO.GeoAddresses,
                                 CreationDate = DateTime.Now,
                                 PreferredPaymentMethod = journeyDTO.PreferredPaymentMethod
                             };

            this.findNDriveUnitOfWork.JourneyRepository.Add(newJourney);
            this.findNDriveUnitOfWork.Commit();
            return ResponseBuilder.Success(newJourney);
        }

        /// <summary>
        /// The get car share by id.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<Journey> GetSingleJourneyById(int id)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new Journey());
            }

            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == id);
            return ResponseBuilder.Success(journey);
        }

        /// <summary>
        /// The get multiple journeys by id.
        /// </summary>
        /// <param name="ids">
        /// The ids.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<Journey>> GetMultipleJourneysById(Collection<int> ids)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new List<Journey>());
            }

            var journeys =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => ids.Contains(_.JourneyId))
                    .ToList();
            return ResponseBuilder.Success(journeys);
        }

        /// <summary>
        /// The modify journey.
        /// </summary>
        /// <param name="journeyDTO">
        /// The journey dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<Journey> ModifyJourney(JourneyDTO journeyDTO)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new Journey());
            }

            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == journeyDTO.JourneyId);

            if (journey == null)
            {
                return ResponseBuilder.Failure<Journey>("Invalid journey id");
            }

            this.findNDriveUnitOfWork.GeoAddressRepository.RemoveRange(journey.GeoAddresses);

            journey.GeoAddresses = journeyDTO.GeoAddresses;
            journey.AvailableSeats = journeyDTO.AvailableSeats;
            journey.DateAndTimeOfDeparture = journeyDTO.DateAndTimeOfDeparture;
            journey.Description = journeyDTO.Description;
            journey.Fee = journeyDTO.Fee;
            journey.Private = journeyDTO.Private;
            journey.SmokersAllowed = journeyDTO.SmokersAllowed;
            journey.VehicleType = journeyDTO.VehicleType;
            journey.PetsAllowed = journeyDTO.PetsAllowed;
            journey.JourneyStatus = journeyDTO.JourneyStatus;
            journey.PreferredPaymentMethod = journeyDTO.PreferredPaymentMethod;

            this.findNDriveUnitOfWork.Commit();

            this.notificationManager.SendAppNotification(
                journey.Participants,
                "Journey modified",
                "Change has been made lol",
                NotificationContext.Neutral,
                NotificationType.Both,
                NotificationContentType.JourneyModified,
                journey);
            this.notificationManager.SendGcmNotification(
                journey.Participants,
                "Journey modified",
                GcmNotificationType.NotificationTickle,
                string.Empty);

            return ResponseBuilder.Success(journey);
        }

        public ServiceResponse<bool> CancelJourney(int id)
        {
            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == id);

            if (journey == null)
            {
                return ResponseBuilder.Failure<bool>("Invalid journey id");
            }

            journey.JourneyStatus = JourneyStatus.Cancelled;
            this.findNDriveUnitOfWork.Commit();

            return ResponseBuilder.Success(true);
        }

        public ServiceResponse<bool> WithdrawFromJourney(JourneyPassengerWithdrawDTO journeyPassengerWithdrawDTO)
        {
            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == journeyPassengerWithdrawDTO.JourneyId);

            if (journey == null)
            {
                return ResponseBuilder.Failure<bool>("Invalid journey id");
            }

            var passenger = this.findNDriveUnitOfWork.UserRepository.Find(journeyPassengerWithdrawDTO.UserId);

            if (passenger == null)
            {
                return ResponseBuilder.Failure<bool>("Invalid passenger id");
            }

            if (passenger.UserId == journey.DriverId)
            {
                return ResponseBuilder.Failure<bool>("As driver, you cannot withdraw from this journey.");
            }

            journey.Participants.Remove(passenger);
            this.findNDriveUnitOfWork.Commit();

            return ResponseBuilder.Success(true);
        }
    }
}
