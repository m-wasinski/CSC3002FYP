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

    using Microsoft.Practices.ObjectBuilder2;

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
        public JourneyService(){}

        /// <summary>
        /// The _find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork _findNDriveUnitOfWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager _sessionManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="JourneyService"/> class. 
        /// </summary>
        /// <param name="findNDriveUnitOf">
        /// The find n drive unit of.
        /// </param>
        /// <param name="sessionManager">
        /// The session Manager.
        /// </param>
        public JourneyService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, GCMManager gcmManager)
        {
            this._findNDriveUnitOfWork = findNDriveUnitOfWork;
            this._sessionManager = sessionManager;
        }

        public ServiceResponse<List<Journey>> GetAllJourneysForUser(LoadRangeDTO loadRangeDTO)
        {
            if (!this._sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new List<Journey>());
            }

            var journeys =
                this._findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.DriverId == loadRangeDTO.Id || _.Participants.Any(x => x.UserId == loadRangeDTO.Id))
                    .OrderByDescending(x => x.CreationDate)
                    .ToList();

            journeys = LoadRangeHelper<Journey>.GetValidRange(journeys, loadRangeDTO.Index, loadRangeDTO.Count, loadRangeDTO.LoadMoreData);

            journeys.ForEach(delegate(Journey journey)
                {
                    if (journey.DateAndTimeOfDeparture < DateTime.Now)
                    {
                        journey.JourneyStatus = JourneyStatus.Past;
                    }
                });

            //this._findNDriveUnitOfWork.Commit();

            return ResponseBuilder.Success(journeys);
        }

        public ServiceResponse<Journey> CreateNewJourney(JourneyDTO journeyDTO)
        {
            if (!this._sessionManager.ValidateSession())
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
                                 JourneyStatus = JourneyStatus.Upcoming,
                                 GeoAddresses = journeyDTO.GeoAddresses,
                                 CreationDate = journeyDTO.CreationDate
                             };

            this._findNDriveUnitOfWork.JourneyRepository.Add(newJourney);
            this._findNDriveUnitOfWork.Commit();
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
            if (!this._sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new Journey());
            }

            var journey =
                this._findNDriveUnitOfWork.JourneyRepository.AsQueryable()
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
            if (!this._sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new List<Journey>());
            }

            var journeys =
               this._findNDriveUnitOfWork.JourneyRepository.AsQueryable()
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
            if (!this._sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new Journey());
            }

            var journey =
                this._findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == journeyDTO.JourneyId);

            if (journey != null)
            {
                journey.AvailableSeats = journeyDTO.AvailableSeats;
                journey.DateAndTimeOfDeparture = journeyDTO.DateAndTimeOfDeparture;
                journey.GeoAddresses = journeyDTO.GeoAddresses;
                journey.Description = journeyDTO.Description;
                journey.DriverId = journeyDTO.DriverId;
                journey.Fee = journeyDTO.Fee;
                journey.Private = journeyDTO.Private;
                journey.WomenOnly = journeyDTO.WomenOnly;
                journey.SmokersAllowed = journeyDTO.SmokersAllowed;
                journey.VehicleType = journeyDTO.VehicleType;
                journey.PetsAllowed = journeyDTO.PetsAllowed;
                journey.JourneyStatus = journeyDTO.JourneyStatus;
            }

            this._findNDriveUnitOfWork.Commit();
            return ResponseBuilder.Success(journey);
        }
    }
}
