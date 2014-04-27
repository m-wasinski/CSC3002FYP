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
        public static bool JourneyFilter (Journey journey, JourneyTemplateDTO journeyTemplateDTO, User requestingUser)
        {
            var matchDeparture = -1;
            var matchDestination = -1;

            // Journey must have at least one available seat.
            if (journey.AvailableSeats <= 0)
            {
                return false;
            }

            // Its status must be OK.
            if (journey.JourneyStatus == JourneyStatus.Expired || journey.DateAndTimeOfDeparture < DateTime.Now || journey.JourneyStatus != JourneyStatus.OK)
            {
                return false;
            }

            // Check if smokers criteria was set.
            if (!journeyTemplateDTO.Smokers && journey.Smokers)
            {
                return false;
            }

            // Check if pets criteria was set.
            if (!journeyTemplateDTO.Pets && journey.Pets)
            {
                return false;
            }

            // Check if the vehicle type criteria was set.
            if ((int)journeyTemplateDTO.VehicleType != -1 && journey.VehicleType != journeyTemplateDTO.VehicleType)
            {
                return false;
            }

            // Check if user decided to search by date of departure.
            if (journeyTemplateDTO.SearchByDate)
            {
                // If yes, we must include all journeys within the 'flexible days' period.
                if (Math.Abs(journeyTemplateDTO.DateAndTimeOfDeparture.Date.Subtract(journey.DateAndTimeOfDeparture.Date).TotalDays) > journeyTemplateDTO.DateAllowance)
                {
                    return false;
                }
            }

            // Check if user decided to search by time of departure.
            if (journeyTemplateDTO.SearchByTime)
            {
                // If yes, we must include all journeys within the 'flexible time' period.
                if (Math.Abs(journeyTemplateDTO.DateAndTimeOfDeparture.TimeOfDay.Subtract(journey.DateAndTimeOfDeparture.TimeOfDay).TotalHours) > journeyTemplateDTO.TimeAllowance)
                {
                    return false;
                }
            }

            // Check the fee.
            if (journeyTemplateDTO.Fee < journey.Fee)
            {
                return false;
            }

            // If this journey has been marked private, we can only return it if the user who initiated the search is one of the driver's friends.
            if (journey.Private && !journey.Driver.Friends.Select(_ => _.UserId).Contains(requestingUser.UserId))
            {
                return false;
            }

            /* 
             * The most important part of the search algorithm checks whether 
             * the departure and destination points provided by the user are within the
             * allowed distance of the departure and destination points or infact any of the waypoints of this journey.
             */
            foreach (var geoAddress in journey.GeoAddresses)
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
        public static bool TemplateFilter(Journey journey, JourneyTemplate journeyTemplate)
        {
                var matchDeparture = -1;
                var matchDestination = -1;

                if (journey.Smokers && !journeyTemplate.Smokers)
                {
                    return false;
                }

                if (journey.Pets && !journeyTemplate.Pets)
                {
                    return false;
                }

                if ((int)journeyTemplate.VehicleType != -1 && journeyTemplate.VehicleType != journey.VehicleType)
                {
                    return false;
                }


                if (journeyTemplate.SearchByDate)
                {
                    if (Math.Abs(journeyTemplate.DateAndTimeOfDeparture.Date.Subtract(journey.DateAndTimeOfDeparture.Date).TotalDays) > journeyTemplate.DateAllowance)
                    {
                        return false;
                    }
                }

                if (journeyTemplate.SearchByTime)
                {
                    if (Math.Abs(journeyTemplate.DateAndTimeOfDeparture.TimeOfDay.Subtract(journey.DateAndTimeOfDeparture.TimeOfDay).TotalHours) > journeyTemplate.TimeAllowance)
                    {
                        return false;
                    }
                }

                if (journeyTemplate.Fee < journey.Fee)
                {
                    return false;
                }

                // If this journey has been marked private, we can only return it if the user who initiated the search is one of the driver's friends.
                if (journey.Private && !journey.Driver.Friends.Select(_ => _.UserId).Contains(journeyTemplate.User.UserId))
                {
                    return false;
                }

                foreach (var geoAddress in journeyTemplate.GeoAddresses)
                {
                    if (journeyTemplate.DepartureRadius
                        >= HaversineCalculator.CalculateDistance(
                            geoAddress,
                            journey.GeoAddresses.First(),
                            DistanceType.Miles) && matchDeparture == -1 && matchDestination != geoAddress.Order)
                    {
                        matchDeparture = geoAddress.Order;
                    }

                    if (journeyTemplate.DestinationRadius
                        >= HaversineCalculator.CalculateDistance(
                            geoAddress,
                            journey.GeoAddresses.Last(),
                            DistanceType.Miles) && matchDestination == -1 && matchDeparture != geoAddress.Order)
                    {
                        matchDestination = geoAddress.Order;
                    }
                }

                return matchDeparture < matchDestination && matchDeparture != -1 && matchDestination != -1;
        }
    }
}