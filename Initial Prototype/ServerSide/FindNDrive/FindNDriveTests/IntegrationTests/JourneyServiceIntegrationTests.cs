// --------------------------------------------------------------------------------------------------------------------
// <copyright file="JourneyServiceIntegrationTests.cs" company="">
//   
// </copyright>
// <summary>
//   The journey unit tests.
// </summary>
// --------------------------------------------------------------------------------------------------------------------
namespace FindNDriveUnitTests.IntegrationTests
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.IO;
    using System.Linq;
    using System.Net;
    using System.Text;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;
    using FindNDriveServices2.ServiceUtils;

    using Microsoft.VisualStudio.TestTools.UnitTesting;

    using Newtonsoft.Json;

    /// <summary>
    /// The journey unit tests.
    /// </summary>
    [TestClass]
    public class JourneyServiceIntegrationTests
    {
        /// <summary>
        /// The journey.
        /// </summary>
        private JourneyDTO journeyDTO;

        /// <summary>
        /// The driver.
        /// </summary>
        private User driver;

        /// <summary>
        /// The session.
        /// </summary>
        private Session session;

        /// <summary>
        /// The journey.
        /// </summary>
        private Journey journey;

        /// <summary>
        /// The find n drive unit of work.
        /// </summary>
        private FindNDriveUnitOfWork findNDriveUnitOfWork;

        /// <summary>
        /// The initialise.
        /// </summary>
        [TestInitialize]
        public void Initialise()
        {
            this.findNDriveUnitOfWork = new FindNDriveUnitOfWork();

            this.driver = this.findNDriveUnitOfWork.UserRepository.Find(1);
            this.session = this.findNDriveUnitOfWork.SessionRepository.Find(1);
            this.session.DeviceId = SessionManager.EncryptValue("test");
            this.findNDriveUnitOfWork.Commit();

            var geoAddress1 = new GeoAddress { AddressLine = "Dublin", Latitude = 53.3478, Longitude = -6.2597, Order = 1 };
            var geoAddress2 = new GeoAddress { AddressLine = "Warrenpoint", Latitude = 54.09900, Longitude = -6.24900, Order = 2 };
            var geoAddress3 = new GeoAddress { AddressLine = "Belfast", Latitude = 54.5970, Longitude = -5.9300, Order = 3 };

            this.journeyDTO = new JourneyDTO
            {
                AvailableSeats = 4,
                Driver = this.driver,
                DateAndTimeOfDeparture = DateTime.Now.AddDays(1),
                GeoAddresses = new List<GeoAddress> { geoAddress1, geoAddress2, geoAddress3 },
                Description = "Free ride to Dublin!",
                JourneyStatus = JourneyStatus.OK,
                CreationDate = DateTime.Now,
                Fee = 0.00,
                Smokers = true,
                Pets = true,
                VehicleType = VehicleTypes.PrivateCar,
                Participants = new Collection<User>()
            };

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            //serialise the registerDTO object into a json string.
            var serialisedRegisterDTO = JsonConvert.SerializeObject(
                this.journeyDTO,
                typeof(JourneyDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/JourneyService.svc/create") as HttpWebRequest;
            Assert.IsNotNull(webRequest);

            webRequest.Method = "POST";

            // Add the necessary HTTP headers.
            webRequest.ContentType = "application/json";
            webRequest.Headers.Add(SessionConstants.SESSION_ID, this.session.SessionId);
            webRequest.Headers.Add(SessionConstants.UUID, this.session.Uuid);
            webRequest.Headers.Add(SessionConstants.DEVICE_ID, "test");


            var bytes = Encoding.UTF8.GetBytes(serialisedRegisterDTO);
            webRequest.ContentLength = bytes.Length;
            var outputStream = webRequest.GetRequestStream();
            outputStream.Write(bytes, 0, bytes.Length);
            outputStream.Close();

            // Make call to the service and retrieve response.
            var webResponse = (HttpWebResponse)webRequest.GetResponse();

            // Deserialise and analyse the response returned from the server.
            using (var sr = new StreamReader(webResponse.GetResponseStream()))
            {
                var serviceResponseString = sr.ReadToEnd();

                Assert.IsNotNull(serviceResponseString);
                Assert.AreNotEqual(string.Empty, serviceResponseString);

                var serviceResponseObject = JsonConvert.DeserializeObject<ServiceResponse<Journey>>(serviceResponseString);
                this.journey = serviceResponseObject.Result;

                Assert.AreEqual(ServiceResponseCode.Success, serviceResponseObject.ServiceResponseCode, "Service respponse code must be equal to success.");

                webResponse.Close();

            }
        }

        /// <summary>
        /// The create new journey.
        /// </summary>
        [TestMethod]
        public void CreateNewJourney()
        {
            var savedJourney =
                    this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                        .IncludeAll()
                        .FirstOrDefault(_ => _.JourneyId == this.journey.JourneyId);

            Assert.AreEqual(this.journey.JourneyId, savedJourney.JourneyId, "Journeys should have the same id.");
        }

        /// <summary>
        /// Testing the web service's functionality to retrieve list of user's journeys.
        /// </summary>
        [TestMethod]
        public void RetrieveUsersJourneys()
        {
            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            //serialise the registerDTO object into a json string.
            var serialiedLoadRange = JsonConvert.SerializeObject(
                new LoadRangeDTO{Id = this.driver.UserId, Skip = 0, Take = 10},
                typeof(LoadRangeDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/JourneyService.svc/user") as HttpWebRequest;
            Assert.IsNotNull(webRequest);

            webRequest.Method = "POST";
            webRequest.ContentType = "application/json";

            var bytes = Encoding.UTF8.GetBytes(serialiedLoadRange);
            webRequest.ContentLength = bytes.Length;
            var outputStream = webRequest.GetRequestStream();
            outputStream.Write(bytes, 0, bytes.Length);
            outputStream.Close();

            // Make call to the service and retrieve response.
            var webResponse = (HttpWebResponse)webRequest.GetResponse();

            // Deserialise and analyse the response returned from the server.
            using (var sr = new StreamReader(webResponse.GetResponseStream()))
            {
                var serviceResponseString = sr.ReadToEnd();

                Assert.IsNotNull(serviceResponseString);
                Assert.AreNotEqual(string.Empty, serviceResponseString);

                var serviceResponseObject = JsonConvert.DeserializeObject<ServiceResponse<List<Journey>>>(serviceResponseString);

                Assert.AreEqual(ServiceResponseCode.Success, serviceResponseObject.ServiceResponseCode, "Service respponse code must be equal to success.");
                webResponse.Close();
            }
        }

        [TestMethod]
        public void TestModifyJourney()
        {
            // Make changes to the current dto object.
            this.journeyDTO.JourneyId = this.journey.JourneyId;
            this.journeyDTO.AvailableSeats = 6;
            this.journeyDTO.GeoAddresses.Remove(this.journeyDTO.GeoAddresses.Last());
            this.journeyDTO.Fee = 10;
            this.journeyDTO.DateAndTimeOfDeparture = DateTime.Now.AddDays(30);
            this.journeyDTO.Description = "test";
            this.journeyDTO.Pets = !this.journeyDTO.Pets;
            this.journeyDTO.Smokers = !this.journeyDTO.Smokers;
            this.journeyDTO.VehicleType = VehicleTypes.Van;

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            //serialise the registerDTO object into a json string.
            var serialisedRegisterDTO = JsonConvert.SerializeObject(
                this.journeyDTO,
                typeof(JourneyDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/JourneyService.svc/edit") as HttpWebRequest;
            Assert.IsNotNull(webRequest);

            webRequest.Method = "POST";

            // Add the necessary HTTP headers.
            webRequest.ContentType = "application/json";
            webRequest.Headers.Add(SessionConstants.SESSION_ID, this.session.SessionId);
            webRequest.Headers.Add(SessionConstants.UUID, this.session.Uuid);
            webRequest.Headers.Add(SessionConstants.DEVICE_ID, "test");


            var bytes = Encoding.UTF8.GetBytes(serialisedRegisterDTO);
            webRequest.ContentLength = bytes.Length;
            var outputStream = webRequest.GetRequestStream();
            outputStream.Write(bytes, 0, bytes.Length);
            outputStream.Close();

            // Make call to the service and retrieve response.
            var webResponse = (HttpWebResponse)webRequest.GetResponse();

            // Deserialise and analyse the response returned from the server.
            using (var sr = new StreamReader(webResponse.GetResponseStream()))
            {
                var serviceResponseString = sr.ReadToEnd();

                Assert.IsNotNull(serviceResponseString);
                Assert.AreNotEqual(string.Empty, serviceResponseString);

                var serviceResponseObject = JsonConvert.DeserializeObject<ServiceResponse<Journey>>(serviceResponseString);

                Assert.AreEqual(ServiceResponseCode.Success, serviceResponseObject.ServiceResponseCode, "Service respponse code must be equal to success.");

                this.journey = serviceResponseObject.Result;

                var savedJourney =
                    this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                        .IncludeAll()
                        .FirstOrDefault(_ => _.JourneyId == serviceResponseObject.Result.JourneyId);

                Assert.AreEqual(savedJourney.JourneyId, this.journey.JourneyId, "Journeys should have the same id.");
                Assert.AreEqual(this.journeyDTO.AvailableSeats, savedJourney.AvailableSeats, "Number of available seats should be equal.");
                Assert.AreEqual(this.journeyDTO.GeoAddresses.Count, savedJourney.GeoAddresses.Count, "GeoAddresses should be equal.");
                Assert.AreEqual(this.journeyDTO.Fee, savedJourney.Fee, "Fees should be equal.");
                Assert.AreEqual(1, DateTime.Compare(this.journeyDTO.DateAndTimeOfDeparture, savedJourney.DateAndTimeOfDeparture), "Dates of departure should be equal.");
                Assert.AreEqual(this.journeyDTO.Description, savedJourney.Description, "Descriptions should be equal.");
                Assert.AreEqual(this.journeyDTO.Pets, savedJourney.Pets, "Pets preference should be equal.");
                Assert.AreEqual(this.journeyDTO.Smokers, savedJourney.Smokers, "Smokers preference should be equal.");
                Assert.AreEqual(this.journeyDTO.VehicleType, savedJourney.VehicleType, "vehicles types should be the same.");
                //CollectionAssert.AreEqual(this.journey.GeoAddresses, savedJourney.GeoAddresses, "GeoAddresses should be equal.");
                
                webResponse.Close();

            }
        }
    }
}
