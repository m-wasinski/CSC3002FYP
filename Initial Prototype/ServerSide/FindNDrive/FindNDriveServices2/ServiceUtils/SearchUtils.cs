using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace FindNDriveServices2.ServiceUtils
{
    using System.Security.Cryptography.X509Certificates;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.DTOs;

    public static class SearchUtils
    {
        public static List<Journey> SearchForJourneys(JourneyTemplateDTO journeyTemplateDTO, FindNDriveUnitOfWork findNDriveUnitOfWork, User requestingUser)
        {
            Func<Journey, bool> filter = x =>
            {
                var matchDeparture = -1;
                var matchDestination = -1;

                if (x.JourneyStatus == JourneyStatus.Expired || x.DateAndTimeOfDeparture < DateTime.Now || x.JourneyStatus != JourneyStatus.OK)
                {
                    return false;
                }

                if (!journeyTemplateDTO.Smokers && x.Smokers)
                {
                    return false;
                }

                if (!journeyTemplateDTO.Pets && x.Pets)
                {
                    return false;
                }

                if ((int)journeyTemplateDTO.VehicleType != -1 && x.VehicleType != journeyTemplateDTO.VehicleType)
                {
                    return false;               
                }


                if (journeyTemplateDTO.SearchByDate)
                {
                    if (Math.Abs(journeyTemplateDTO.DateAndTimeOfDeparture.Date.Subtract(x.DateAndTimeOfDeparture.Date).TotalDays) > journeyTemplateDTO.DateAllowance)
                    {
                        return false;
                    }
                }

                if (journeyTemplateDTO.SearchByTime)
                {
                    if (Math.Abs(journeyTemplateDTO.DateAndTimeOfDeparture.TimeOfDay.Subtract(x.DateAndTimeOfDeparture.TimeOfDay).TotalHours) > journeyTemplateDTO.TimeAllowance)
                    {
                        return false;
                    }
                }

                if (journeyTemplateDTO.Fee < x.Fee)
                {
                    return false;
                }

                var driver =
                    findNDriveUnitOfWork.UserRepository.AsQueryable()
                        .IncludeAll()
                        .FirstOrDefault(_ => _.UserId == x.Driver.UserId);

                if (driver != null && (x.Private && !driver.Friends.Select(_ => _.UserId).Contains(requestingUser.UserId)))
                {
                    return false;
                }

                foreach (var geoAddress in x.GeoAddresses)
                {
                    if (journeyTemplateDTO.DepartureRadius
                        >= new Haversine().Distance(
                            geoAddress,
                            journeyTemplateDTO.GeoAddresses.First(),
                            DistanceType.Miles) && matchDeparture == -1 && matchDestination != geoAddress.Order)
                    {
                        matchDeparture = geoAddress.Order;
                    }

                    if (journeyTemplateDTO.DestinationRadius
                        >= new Haversine().Distance(
                            geoAddress,
                            journeyTemplateDTO.GeoAddresses.Last(),
                            DistanceType.Miles) && matchDestination == -1 && matchDeparture != geoAddress.Order)
                    {
                        matchDestination = geoAddress.Order;
                    }
                }

                return matchDeparture < matchDestination && matchDeparture != -1 && matchDestination != -1;
            };

            return findNDriveUnitOfWork.JourneyRepository.AsQueryable().IncludeSearch().Where(filter).ToList();
        }

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
                        >= new Haversine().Distance(
                            geoAddress,
                            journey.GeoAddresses.First(),
                            DistanceType.Miles) && matchDeparture == -1 && matchDestination != geoAddress.Order)
                    {
                        matchDeparture = geoAddress.Order;
                    }

                    if (x.DestinationRadius
                        >= new Haversine().Distance(
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