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

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "SearchService" in code, svc and config file together.
    // NOTE: In order to launch WCF Test Client for testing this service, please select SearchService.svc or SearchService.svc.cs at the Solution Explorer and start debugging.
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
        private readonly FindNDriveUnitOfWork _findNDriveUnitOfWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager _sessionManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="SearchService"/> class. 
        /// Initializes a new instance of the <see cref="CarShareService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOf">
        /// The find n drive unit of.
        /// </param>
        /// <param name="sessionManager">
        /// The session Manager.
        /// </param>
        public SearchService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, GCMManager gcmManager)
        {
            this._findNDriveUnitOfWork = findNDriveUnitOfWork;
            this._sessionManager = sessionManager;
        }

        /// <summary>
        /// The search car shares.
        /// </summary>
        /// <param name="journey">
        /// The car share.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public ServiceResponse<List<Journey>> SearchForJourneys(JourneyDTO journey)
        {
            /*var carShares =
                this._findNDriveUnitOfWork.journeyRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.DepartureAddress.AddressLine == journey.DepartureAddress.AddressLine &&
                            _.DestinationAddress.AddressLine == journey.DestinationAddress.AddressLine && _.AvailableSeats > 0 && _.JourneyStatus == JourneyStatus.Upcoming)
                    .ToList();*/

            var journeys =
                this._findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll().ToList();

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
