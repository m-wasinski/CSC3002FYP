// --------------------------------------------------------------------------------------------------------------------
// <copyright file="JourneyServiceIntegrationTests.cs" company="">
//   
// </copyright>
// <summary>
//   The journey unit tests.
// </summary>
// --------------------------------------------------------------------------------------------------------------------
namespace FindNDriveUnitTests
{
    using System;
    using System.Collections.Generic;
    using System.IO;
    using System.Net;
    using System.Text;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

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
                VehicleType = VehicleTypes.PrivateCar
            };
        }

        /// <summary>
        /// The create new journey.
        /// </summary>
        [TestMethod]
        public void CreateNewJourney()
        {
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
            webRequest.Headers.Add(SessionConstants.DEVICE_ID, this.session.DeviceId);
           

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

                var serviceResponseObject = JsonConvert.DeserializeObject<ServiceResponse<bool>>(serviceResponseString);

                Assert.AreEqual(ServiceResponseCode.Success, serviceResponseObject.ServiceResponseCode, "Service respponse code must be equal to success.");
                webResponse.Close();
            }
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

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/JourneyService.svc/getall") as HttpWebRequest;
            Assert.IsNotNull(webRequest);

            webRequest.Method = "POST";

            // Add the necessary HTTP headers.
            webRequest.ContentType = "application/json";
            webRequest.Headers.Add(SessionConstants.SESSION_ID, this.session.SessionId);
            webRequest.Headers.Add(SessionConstants.UUID, this.session.Uuid);
            webRequest.Headers.Add(SessionConstants.DEVICE_ID, this.session.DeviceId);


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
    }
}
