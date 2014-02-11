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
    using System.Collections.ObjectModel;
    using System.IO;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DomainObjects.Constants;
    using DomainObjects.Domains;
    using FindNDriveDataAccessLayer;
    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    using Microsoft.Practices.ObjectBuilder2;

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
        public SearchService(SessionManager sessionManager)
        {
            this.sessionManager = sessionManager;
        }

        /// <summary>
        /// The _find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        private readonly SessionManager sessionManager;

        /// <summary>
        /// The session manager.
        /// </summary>
        private readonly NotificationManager notificationManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="SearchService"/> class. 
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find N Drive Unit Of Work.
        /// </param>
        /// <param name="sessionManager">
        /// The session Manager.
        /// </param>
        /// <param name="notificationManager">
        /// </param>
        public SearchService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.notificationManager = notificationManager;
            this.sessionManager = sessionManager;
        }

        /// <summary>
        /// The search for journeys.
        /// </summary>
        /// <param name="journeySearchDTO">
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<Journey>> SearchForJourneys(JourneySearchDTO journeySearchDTO)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new List<Journey>());
            }

            Func<Journey, bool> filter = x => 
                {
                    var matchDeparture = -1;
                    var matchDestination = -1;

                    if (x.JourneyStatus == JourneyStatus.Past || x.DateAndTimeOfDeparture < DateTime.Now)
                    {
                        return false;
                    }

                    foreach (var geoAddress in x.GeoAddresses)
                    {   
                        if (journeySearchDTO.DepartureRadius
                            >= new Haversine().Distance(
                                geoAddress,
                                journeySearchDTO.Journey.GeoAddresses.First(),
                                DistanceType.Miles) && matchDeparture == -1 && matchDestination != geoAddress.Order)
                        {
                            matchDeparture = geoAddress.Order;
                        }

                        if (journeySearchDTO.DestinationRadius
                            >= new Haversine().Distance(
                                geoAddress,
                                journeySearchDTO.Journey.GeoAddresses.Last(),
                                DistanceType.Miles) && matchDestination == -1 && matchDeparture != geoAddress.Order)
                        {
                            matchDestination = geoAddress.Order;
                        }
                    }

                    return matchDeparture < matchDestination && matchDeparture != -1 && matchDestination != -1;
                };

            var journeys =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable().IncludeAll().AsEnumerable().Where(filter).ToList();
                
            /*
            if (journeySearchDTO.SearchByDate)
            {
                journeys =
                    journeys.Where(
                        _ =>
                        Math.Abs(_.DateAndTimeOfDeparture.Date.Subtract(journey.DateAndTimeOfDeparture.Date).TotalDays) <= 5).ToList();
            }

            if (journeySearchDTO.SearchByTime)
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
            }*/

            return new ServiceResponse<List<Journey>>
            {
                Result = journeys,
                ServiceResponseCode = ServiceResponseCode.Success
            };
        }
    }
}
