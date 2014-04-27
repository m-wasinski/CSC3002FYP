namespace Tests.UnitTests
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using Microsoft.VisualStudio.TestTools.UnitTesting;

    using Services.DTOs;
    using Services.ServiceUtils;

    [TestClass]
    public class SearchUnitTests
    {
        private Journey journey;

        private JourneyTemplateDTO journeyTemplateDTO;

        private User driver, participant1, participant2;


        [TestInitialize]
        public void Initialise()
        {
            var geoAddress1 = new GeoAddress { AddressLine = "Dublin City Centre", Latitude = 53.3478, Longitude = -6.2597, Order = 0};
            var geoAddress2 = new GeoAddress { AddressLine = "Warrenpoint City Centre", Latitude = 54.09900, Longitude = -6.24900, Order = 1 };
            var geoAddress3 = new GeoAddress { AddressLine = "Belfast City Centre", Latitude = 54.5970, Longitude = -5.9300, Order = 2};

            driver = new User { FirstName = "Michal", LastName = "Wasinski" };
            participant1 = new User { FirstName = "John", LastName = "Doe" };
            participant2 = new User { FirstName = "Adam", LastName = "McDonald" };

            journey = new Journey
                          {
                              AvailableSeats = 2,
                              DateAndTimeOfDeparture = DateTime.Now.AddDays(1),
                              GeoAddresses = new List<GeoAddress> { geoAddress1, geoAddress2, geoAddress3 },
                              Description = "Lift to dublin",
                              Fee = 10.00,
                              Driver = driver,
                              Smokers = true,
                              Pets = true,
                              VehicleType = VehicleTypes.PrivateCar,
                              JourneyStatus = JourneyStatus.OK,
                              CreationDate = DateTime.Now,
                              PreferredPaymentMethod = "Cash in hand preferred",
                              Passengers = new Collection<User> { participant1, participant2 }
                          };


            var templateGeoAddress1 = new GeoAddress { AddressLine = "Dublin Airport", Latitude = 53.4214, Longitude = -6.2700, Order = 0 };
            var templateGeoAddress2 = new GeoAddress { AddressLine = "Eglantine Avenue", Latitude = 54.5814623, Longitude = -5.942235699999969, Order = 2 };

            journeyTemplateDTO = new JourneyTemplateDTO
            {
                DateAndTimeOfDeparture = DateTime.Now.AddDays(1),
                SearchByDate = false,
                SearchByTime = false,
                Smokers = true,
                Pets = true,
                Fee = 15,
                DestinationRadius = 2,
                DepartureRadius = 10,
                GeoAddresses = new List<GeoAddress> { templateGeoAddress1, templateGeoAddress2 },
            };

        }

        /// <summary>
        /// Tests the haversine's ability to calculate distance between two points.
        /// </summary>
        [TestMethod]
        public void TestLocationAwareness()
        {
            // Check if the haversine function is working properly.
            Assert.IsTrue(SearchUtils.JourneyFilter(journey, journeyTemplateDTO, journey.Driver));

            //Now, decrease the radius.
            journeyTemplateDTO.DepartureRadius = 1;

            //We should not expect a failure since the radius is too small and the destination target is outside the radius circle.
            Assert.IsTrue(!SearchUtils.JourneyFilter(journey, journeyTemplateDTO, journey.Driver));
        }

        [TestMethod]
        public void TestTimeAllowance()
        {
            journeyTemplateDTO.SearchByTime = true;
            journeyTemplateDTO.TimeAllowance = 2;
            journeyTemplateDTO.DateAndTimeOfDeparture = journey.DateAndTimeOfDeparture.AddHours(1);

            Assert.IsTrue(SearchUtils.JourneyFilter(journey, journeyTemplateDTO, journey.Driver));

            journeyTemplateDTO.DateAndTimeOfDeparture = journey.DateAndTimeOfDeparture.AddHours(-1);

            Assert.IsTrue(SearchUtils.JourneyFilter(journey, journeyTemplateDTO, journey.Driver));

            journeyTemplateDTO.DateAndTimeOfDeparture = journey.DateAndTimeOfDeparture.AddHours(2);

            Assert.IsTrue(SearchUtils.JourneyFilter(journey, journeyTemplateDTO, journey.Driver));

            journeyTemplateDTO.DateAndTimeOfDeparture = journey.DateAndTimeOfDeparture.AddHours(3);

            Assert.IsTrue(!SearchUtils.JourneyFilter(journey, journeyTemplateDTO, journey.Driver));
        }

        [TestMethod]
        public void TestDateAllowance()
        {
            journeyTemplateDTO.SearchByDate = true;
            journeyTemplateDTO.DateAllowance = 2;
            journeyTemplateDTO.DateAndTimeOfDeparture = journey.DateAndTimeOfDeparture.AddDays(1);

            Assert.IsTrue(SearchUtils.JourneyFilter(journey, journeyTemplateDTO, journey.Driver));

            journeyTemplateDTO.DateAndTimeOfDeparture = journey.DateAndTimeOfDeparture.AddDays(-1);

            Assert.IsTrue(SearchUtils.JourneyFilter(journey, journeyTemplateDTO, journey.Driver));

            journeyTemplateDTO.DateAndTimeOfDeparture = journey.DateAndTimeOfDeparture.AddDays(2);

            Assert.IsTrue(SearchUtils.JourneyFilter(journey, journeyTemplateDTO, journey.Driver));

            journeyTemplateDTO.DateAndTimeOfDeparture = journey.DateAndTimeOfDeparture.AddDays(3);

            Assert.IsTrue(!SearchUtils.JourneyFilter(journey, journeyTemplateDTO, journey.Driver));
        }


    }
}
