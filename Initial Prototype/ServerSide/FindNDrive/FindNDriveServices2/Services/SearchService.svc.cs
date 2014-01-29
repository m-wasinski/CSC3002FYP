// --------------------------------------------------------------------------------------------------------------------
// <copyright file="SearchService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the SearchService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Services
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;
    using DomainObjects.Domains;
    using FindNDriveDataAccessLayer;
    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The search service.
    /// </summary>
    [ServiceBehavior(
          InstanceContextMode = InstanceContextMode.PerCall,
          ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class SearchService : ISearchService
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="SearchService"/> class.
        /// </summary>
        public SearchService()
        {
        }

        /// <summary>
        /// The _find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        /// <summary>
        /// The session manager.
        /// </summary>
        private readonly SessionManager sessionManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="SearchService"/> class. 
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find N Drive Unit Of Work.
        /// </param>
        /// <param name="sessionManager">
        /// The session Manager.
        /// </param>
        /// <param name="gcmManager">
        /// The gcm Manager.
        /// </param>
        public SearchService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, GCMManager gcmManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
        }

        /// <summary>
        /// The search for journeys.
        /// </summary>
        /// <param name="journey">
        /// The journey.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<Journey>> SearchForJourneys(JourneyDTO journey)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new List<Journey>());
            }

            /*var carShares =
                this._findNDriveUnitOfWork.journeyRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.DepartureAddress.AddressLine == journey.DepartureAddress.AddressLine &&
                            _.DestinationAddress.AddressLine == journey.DestinationAddress.AddressLine && _.AvailableSeats > 0 && _.JourneyStatus == JourneyStatus.Upcoming)
                    .ToList();*/

            var journeys =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll().ToList();
            journeys = journeys.GetRange(0, 1);
            if (journey.SearchByDate)
            {
                journeys =
                    journeys.Where(
                        _ =>
                        Math.Abs(_.DateAndTimeOfDeparture.Date.Subtract(journey.DateAndTimeOfDeparture.Date).TotalDays) <= 5).ToList();
            }

            if (journey.SearchByTime)
            {   
                journeys =
                    journeys.Where(
                        _ =>
                        Math.Abs(_.DateAndTimeOfDeparture.TimeOfDay.Subtract(journey.DateAndTimeOfDeparture.TimeOfDay).TotalHours) <= 1).ToList();
            }

            if (journey.SmokersAllowed)
            {
                journeys = journeys.Where(_ => _.SmokersAllowed).ToList();
            }

            if (journey.WomenOnly)
            {
                journeys = journeys.Where(_ => _.WomenOnly).ToList();
            }

            if (journey.PetsAllowed)
            {
                journeys = journeys.Where(_ => _.PetsAllowed).ToList();
            }


            if (journey.Free)
            {
                journeys = journeys.Where(_ => _.Fee == 0.00).ToList();
            }

            return new ServiceResponse<List<Journey>>
            {
                Result = journeys,
                ServiceResponseCode = ServiceResponseCode.Success
            };
        }
    }
}
