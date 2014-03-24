// --------------------------------------------------------------------------------------------------------------------
// <copyright file="SearchUtils.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the SearchUtils type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.ServiceUtils
{
    using System;
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;

    using DataAccessLayer;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using global::Services.DTOs;

    /// <summary>
    /// The search utils.
    /// </summary>
    public static class SearchUtils
    {
        /// <summary>
        /// Performs search for a journey matching the criteria provided by the user.
        /// </summary>
        /// <param name="journeyTemplateDTO">
        /// The journey template dto.
        /// </param>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="requestingUser">
        /// The requesting user.
        /// </param>
        /// <returns>
        /// The <see cref="List"/>.
        /// </returns>
        public static List<Journey> SearchForJourneys(JourneyTemplateDTO journeyTemplateDTO, FindNDriveUnitOfWork findNDriveUnitOfWork, User requestingUser)
        {
            // Performs a verity of checks the journey being passed in to check if it satisfies the user's search criteria.
            Func<Journey, bool> filter = x =>
            {
                var matchDeparture = -1;
                var matchDestination = -1;

                // Journey must have at least one available seat.
                if (x.AvailableSeats <= 0)
                {
                    return false;
                }

                // Its status must be OK.
                if (x.JourneyStatus == JourneyStatus.Expired || x.DateAndTimeOfDeparture < DateTime.Now || x.JourneyStatus != JourneyStatus.OK)
                {
                    return false;
                }

                // Check if smokers criteria was set.
                if (!journeyTemplateDTO.Smokers && x.Smokers)
                {
                    return false;
                }

                // Check if pets criteria was set.
                if (!journeyTemplateDTO.Pets && x.Pets)
                {
                    return false;
                }

                // Check if the vehicle type criteria was set.
                if ((int)journeyTemplateDTO.VehicleType != -1 && x.VehicleType != journeyTemplateDTO.VehicleType)
                {
                    return false;               
                }

                // Check if user decided to search by date of departure.
                if (journeyTemplateDTO.SearchByDate)
                {
                    // If yes, we must include all journeys within the 'flexible days' period.
                    if (Math.Abs(journeyTemplateDTO.DateAndTimeOfDeparture.Date.Subtract(x.DateAndTimeOfDeparture.Date).TotalDays) > journeyTemplateDTO.DateAllowance)
                    {
                        return false;
                    }
                }

                // Check if user decided to search by time of departure.
                if (journeyTemplateDTO.SearchByTime)
                {
                    // If yes, we must include all journeys within the 'flexible time' period.
                    if (Math.Abs(journeyTemplateDTO.DateAndTimeOfDeparture.TimeOfDay.Subtract(x.DateAndTimeOfDeparture.TimeOfDay).TotalHours) > journeyTemplateDTO.TimeAllowance)
                    {
                        return false;
                    }
                }

                // Check the fee.
                if (journeyTemplateDTO.Fee < x.Fee)
                {
                    return false;
                }

                var driver =
                    findNDriveUnitOfWork.UserRepository.AsQueryable().Include(_ => _.Friends).FirstOrDefault(_ => _.UserId == x.Driver.UserId);

                // If this journey has been marked private, we can only return it if the user who initiated the search is one of the driver's friends.
                if (driver != null && (x.Private && !driver.Friends.Select(_ => _.UserId).Contains(requestingUser.UserId)))
                {
                    return false;
                }

                /* 
                 * The most important part of the search algorithm checks whether 
                 * the departure and destination points provided by the user are within the
                 * allowed distance of the departure and destination points or infact any of the waypoints of this journey.
                 */
                foreach (var geoAddress in x.GeoAddresses)
                {
                    if (journeyTemplateDTO.DepartureRadius
                        >= HaversineCalculator.CalculateDistance(
                            geoAddress,
                            journeyTemplateDTO.GeoAddresses.First(),
                            DistanceType.Miles) && matchDeparture == -1 && matchDestination != geoAddress.Order)
                    {
                        matchDeparture = geoAddress.Order;
                    }

                    if (journeyTemplateDTO.DestinationRadius
                        >= HaversineCalculator.CalculateDistance(
                            geoAddress,
                            journeyTemplateDTO.GeoAddresses.Last(),
                            DistanceType.Miles) && matchDestination == -1 && matchDeparture != geoAddress.Order)
                    {
                        matchDestination = geoAddress.Order;
                    }
                }

                return matchDeparture < matchDestination && matchDeparture != -1 && matchDestination != -1;
            };

            return findNDriveUnitOfWork.JourneyRepository.AsQueryable().Include(_ => _.Driver).Include(_ => _.GeoAddresses).Where(filter).ToList();
        }

        /// <summary>
        /// Once a journey has been offered by a user, we must check all templates within the system to notify any other user who 
        /// waits for a journey like this.
        /// The search algorithm is very similar to the one above except it's the list of journeytemplates that we iterate through this time.
        /// </summary>
        /// <param name="journey">
        /// The journey.
        /// </param>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <returns>
        /// The <see cref="List"/>.
        /// </returns>
        public static List<JourneyTemplate> SearchForTemplates(Journey journey, FindNDriveUnitOfWork findNDriveUnitOfWork)
        {
            Func<JourneyTemplate, bool> filter = x =>
            {
                var matchDeparture = -1;
                var matchDestination = -1;

                if (journey.Smokers && !x.Smokers)
                {
                    return false;
                }

                if (journey.Pets && !x.Pets)
                {
                    return false;
                }

                if ((int)x.VehicleType != -1 && x.VehicleType != journey.VehicleType)
                {
                    return false;
                }


                if (x.SearchByDate)
                {
                    if (Math.Abs(x.DateAndTimeOfDeparture.Date.Subtract(journey.DateAndTimeOfDeparture.Date).TotalDays) > x.DateAllowance)
                    {
                        return false;
                    }
                }

                if (x.SearchByTime)
                {
                    if (Math.Abs(x.DateAndTimeOfDeparture.TimeOfDay.Subtract(journey.DateAndTimeOfDeparture.TimeOfDay).TotalHours) > x.TimeAllowance)
                    {
                        return false;
                    }
                }

                if (x.Fee < journey.Fee)
                {
                    return false;
                }

                foreach (var geoAddress in x.GeoAddresses)
                {
                    if (x.DepartureRadius
                        >= HaversineCalculator.CalculateDistance(
                            geoAddress,
                            journey.GeoAddresses.First(),
                            DistanceType.Miles) && matchDeparture == -1 && matchDestination != geoAddress.Order)
                    {
                        matchDeparture = geoAddress.Order;
                    }

                    if (x.DestinationRadius
                        >= HaversineCalculator.CalculateDistance(
                            geoAddress,
                            journey.GeoAddresses.Last(),
                            DistanceType.Miles) && matchDestination == -1 && matchDeparture != geoAddress.Order)
                    {
                        matchDestination = geoAddress.Order;
                    }
                }

                return matchDeparture < matchDestination && matchDeparture != -1 && matchDestination != -1;
            };

            return findNDriveUnitOfWork.JourneyTemplateRepository.AsQueryable().IncludeAll().Where(filter).ToList();
        }
    }
}