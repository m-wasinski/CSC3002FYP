namespace Tests.IntegrationTests
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Data.Entity;
    using System.IO;
    using System.Linq;
    using System.Net;
    using System.Text;

    using DataAccessLayer;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using Microsoft.VisualStudio.TestTools.UnitTesting;

    using Newtonsoft.Json;

    using Services.DTOs;
    using Services.ServiceResponses;
    using Services.ServiceUtils;

    [TestClass]
    public class JourneyRequestServiceIntegrationTests
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
        /// The participant.
        /// </summary>
        private User participant;

        /// <summary>
        /// The participants session.
        /// </summary>
        private Session participantSession;

        /// <summary>
        /// The driver's session.
        /// </summary>
        private Session driverSession;

        /// <summary>
        /// The journey.
        /// </summary>
        private Journey journey;

        /// <summary>
        /// The find n drive unit of work.
        /// </summary>
        private FindNDriveUnitOfWork findNDriveUnitOfWork;

        /// <summary>
        /// The journey_request_dto.
        /// </summary>
        private JourneyRequestDTO journeyRequestDTO;

        /// <summary>
        /// The journey request.
        /// </summary>
        private JourneyRequest journeyRequest;

        [TestInitialize]
        public void Initialise()
        {
            this.findNDriveUnitOfWork = new FindNDriveUnitOfWork();

            this.driver = this.findNDriveUnitOfWork.UserRepository.Find(1);
            this.driverSession = this.findNDriveUnitOfWork.SessionRepository.Find(1);
            this.driverSession.DeviceId = SessionManager.EncryptValue("test");
            this.driverSession.ExpiryDate = this.driverSession.ExpiryDate.AddDays(2);

            this.participant = this.findNDriveUnitOfWork.UserRepository.Find(2);
            this.participantSession = this.findNDriveUnitOfWork.SessionRepository.Find(2);
            this.participantSession.DeviceId = SessionManager.EncryptValue("test");
            this.participantSession.ExpiryDate = this.participantSession.ExpiryDate.AddDays(2);

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
                Fee = 0.00,
                Smokers = true,
                Pets = true,
                VehicleType = VehicleTypes.PrivateCar,
                Passengers = new Collection<User>()
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
            webRequest.Headers.Add(SessionConstants.SESSION_ID, this.driverSession.SessionString);
            webRequest.Headers.Add(SessionConstants.UUID, this.driverSession.RandomID);
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

                this.journeyRequestDTO = new JourneyRequestDTO
                                             {
                                                 FromUser = this.participant,
                                                 Journey = this.journey,
                                                 Message = "Test Message",
                                                 JourneyId = this.journey.JourneyId
                                             };

                var serialisedJourneyRequestDTO = JsonConvert.SerializeObject(
                    this.journeyRequestDTO,
                    typeof(JourneyRequestDTO),
                    Formatting.Indented,
                    new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

                var journeyRequestServiceWebRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/JourneyRequestService.svc/send") as HttpWebRequest;
                Assert.IsNotNull(journeyRequestServiceWebRequest);

                journeyRequestServiceWebRequest.Method = "POST";

                // Add the necessary HTTP headers.
                journeyRequestServiceWebRequest.ContentType = "application/json";
                journeyRequestServiceWebRequest.Headers.Add(SessionConstants.SESSION_ID, this.participantSession.SessionString);
                journeyRequestServiceWebRequest.Headers.Add(SessionConstants.UUID, this.participantSession.RandomID);
                journeyRequestServiceWebRequest.Headers.Add(SessionConstants.DEVICE_ID, "test");


                var bytes2 = Encoding.UTF8.GetBytes(serialisedJourneyRequestDTO);
                journeyRequestServiceWebRequest.ContentLength = bytes2.Length;
                var outputStream2 = journeyRequestServiceWebRequest.GetRequestStream();
                outputStream2.Write(bytes2, 0, bytes2.Length);
                outputStream2.Close();

                var webResponse2 = (HttpWebResponse)journeyRequestServiceWebRequest.GetResponse();

                using (var sr2 = new StreamReader(webResponse2.GetResponseStream()))
                {
                    serviceResponseString = sr2.ReadToEnd();

                    Assert.IsNotNull(serviceResponseString);
                    Assert.AreNotEqual(string.Empty, serviceResponseString);

                    var response = JsonConvert.DeserializeObject<ServiceResponse>(serviceResponseString);
                    Assert.AreEqual(ServiceResponseCode.Success, response.ServiceResponseCode, "Request should be sent successfully.");

                    this.journeyRequest =
                        this.findNDriveUnitOfWork.JourneyRequestRepository.AsQueryable()
                            .FirstOrDefault(_ => _.JourneyId == this.journey.JourneyId);
                }
            }
        }

        [TestMethod]
        public void TestSendJourneyRequest()
        {
            Assert.IsNotNull(this.journeyRequest);
        }

        [TestMethod]
        public void TestAcceptRequest()
        {
            Assert.IsNotNull(this.journeyRequest);
            Assert.IsNotNull(this.journeyRequestDTO);

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            this.journeyRequestDTO.Decision = Decision.Accepted;
            this.journeyRequestDTO.JourneyRequestId = this.journeyRequest.JourneyRequestId;

            //serialise the registerDTO object into a json string.
            var serialisedRegisterDTO = JsonConvert.SerializeObject(
                this.journeyRequestDTO,
                typeof(JourneyRequestDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/JourneyRequestService.svc/process") as HttpWebRequest;
            Assert.IsNotNull(webRequest);

            webRequest.Method = "POST";

            // Add the necessary HTTP headers.
            webRequest.ContentType = "application/json";
            webRequest.Headers.Add(SessionConstants.SESSION_ID, this.driverSession.SessionString);
            webRequest.Headers.Add(SessionConstants.UUID, this.driverSession.RandomID);
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

                var serviceResponseObject =
                    JsonConvert.DeserializeObject<ServiceResponse>(serviceResponseString);

                Assert.AreEqual(
                    ServiceResponseCode.Success,
                    serviceResponseObject.ServiceResponseCode,
                    "Service respponse code must be equal to success.");

                this.journey =
                    this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                        .Include(_ => _.Passengers)
                        .FirstOrDefault(_ => _.JourneyId == this.journey.JourneyId);

                Assert.AreEqual(1, this.journey.Passengers.Count);
            }
        }

        [TestMethod]
        public void TestDenyRequest()
        {
            Assert.IsNotNull(this.journeyRequest);
            Assert.IsNotNull(this.journeyRequestDTO);

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            this.journeyRequestDTO.Decision = Decision.Denied;
            this.journeyRequestDTO.JourneyRequestId = this.journeyRequest.JourneyRequestId;

            //serialise the registerDTO object into a json string.
            var serialisedRegisterDTO = JsonConvert.SerializeObject(
                this.journeyRequestDTO,
                typeof(JourneyRequestDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/JourneyRequestService.svc/process") as HttpWebRequest;
            Assert.IsNotNull(webRequest);

            webRequest.Method = "POST";

            // Add the necessary HTTP headers.
            webRequest.ContentType = "application/json";
            webRequest.Headers.Add(SessionConstants.SESSION_ID, this.driverSession.SessionString);
            webRequest.Headers.Add(SessionConstants.UUID, this.driverSession.RandomID);
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

                var serviceResponseObject =
                    JsonConvert.DeserializeObject<ServiceResponse>(serviceResponseString);

                Assert.AreEqual(
                    ServiceResponseCode.Success,
                    serviceResponseObject.ServiceResponseCode,
                    "Service respponse code must be equal to success.");

                this.journey =
                    this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                        .Include(_ => _.Passengers)
                        .FirstOrDefault(_ => _.JourneyId == this.journey.JourneyId);

                Assert.AreEqual(0, this.journey.Passengers.Count);
            }
        }
    }
}
