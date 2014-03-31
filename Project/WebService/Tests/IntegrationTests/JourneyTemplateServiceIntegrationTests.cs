namespace Tests.IntegrationTests
{
    using System;
    using System.Collections.Generic;
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
    public class JourneyTemplateServiceIntegrationTests
    {
        /// <summary>
        /// The find n drive unit of work.
        /// </summary>
        private FindNDriveUnitOfWork findNDriveUnitOfWork;

        private JourneyTemplateDTO journeyTemplateDTO;

        private JourneyTemplate journeyTemplate;

        private User user;

        private Session session;

        private const string MyTemplateName = "My Journey Template";

        [TestInitialize]
        public void Initialise()
        {
            this.findNDriveUnitOfWork = new FindNDriveUnitOfWork();

            this.user = this.findNDriveUnitOfWork.UserRepository.Find(1);
            this.session = this.findNDriveUnitOfWork.SessionRepository.Find(1);
            this.session.DeviceId = SessionManager.EncryptValue("test");
            this.session.ExpiryDate = this.session.ExpiryDate.AddDays(2);
            this.findNDriveUnitOfWork.Commit();

            var geoAddress1 = new GeoAddress { AddressLine = "Dublin", Latitude = 53.3478, Longitude = -6.2597, Order = 1 };
            var geoAddress2 = new GeoAddress { AddressLine = "Belfast", Latitude = 54.5970, Longitude = -5.9300, Order = 3 };

            this.journeyTemplate =
                   this.findNDriveUnitOfWork.JourneyTemplateRepository.AsQueryable()
                       .IncludeChildren()
                       .FirstOrDefault(_ => _.Alias.Equals(MyTemplateName));

            if (this.journeyTemplate != null)
            {
                this.findNDriveUnitOfWork.JourneyTemplateRepository.Remove(journeyTemplate);
                this.findNDriveUnitOfWork.Commit();
            }

            this.journeyTemplateDTO = new JourneyTemplateDTO
            {
                UserId = user.UserId,
                Alias = MyTemplateName,
                Fee = 25,
                TimeAllowance = 5,
                DateAllowance = 5,
                SearchByTime = true,
                SearchByDate = true,
                DestinationRadius = 5,
                DateAndTimeOfDeparture = DateTime.Now.AddDays(5),
                GeoAddresses = new List<GeoAddress> { geoAddress1, geoAddress2 },
                DepartureRadius = 5,
                Smokers = true,
                Pets = true,
                VehicleType = VehicleTypes.PrivateCar,
            };

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            //serialise the registerDTO object into a json string.
            var serialisedRegisterDTO = JsonConvert.SerializeObject(
                this.journeyTemplateDTO,
                typeof(JourneyTemplateDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/JourneyTemplateService.svc/create") as HttpWebRequest;
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

                var serviceResponseObject = JsonConvert.DeserializeObject<ServiceResponse>(serviceResponseString);

                Assert.AreEqual(ServiceResponseCode.Success, serviceResponseObject.ServiceResponseCode, "Service respponse code must be equal to success.");

                webResponse.Close();

                this.journeyTemplate =
                    this.findNDriveUnitOfWork.JourneyTemplateRepository.AsQueryable()
                        .IncludeChildren()
                        .FirstOrDefault(_ => _.Alias.Equals(MyTemplateName));

            }
        }

        [TestMethod]
        public void TestCreateNewJourneyTemplate()
        {
            Assert.IsNotNull(this.journeyTemplate);
        }

        [TestMethod]
        public void TestModifyJourneyTemplate()
        {
            this.findNDriveUnitOfWork = new FindNDriveUnitOfWork();
            var geoAddress1 = new GeoAddress { AddressLine = "Dublin", Latitude = 53.3478, Longitude = -6.2597, Order = 1 };
            var geoAddress2 = new GeoAddress { AddressLine = "Warrenpoint", Latitude = 54.09900, Longitude = -6.24900, Order = 2 };

            this.journeyTemplateDTO = new JourneyTemplateDTO
            {
                JourneyTemplateId = this.journeyTemplate.JourneyTemplateId,
                UserId = user.UserId,
                Alias = MyTemplateName,
                Fee = 10,
                TimeAllowance = 2,
                DateAllowance = 2,
                SearchByTime = true,
                SearchByDate = true,
                DestinationRadius = 2,
                DateAndTimeOfDeparture = DateTime.Now.AddDays(10),
                GeoAddresses = new List<GeoAddress> { geoAddress1, geoAddress2 },
                DepartureRadius = 2,
                Smokers = false,
                Pets = false,
                VehicleType = VehicleTypes.PrivateCar,
            };

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            //serialise the registerDTO object into a json string.
            var serialisedJourneyTemplateDTO = JsonConvert.SerializeObject(
                this.journeyTemplateDTO,
                typeof(JourneyTemplateDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/JourneyTemplateService.svc/update") as HttpWebRequest;
            Assert.IsNotNull(webRequest);

            webRequest.Method = "POST";

            // Add the necessary HTTP headers.
            webRequest.ContentType = "application/json";
            webRequest.Headers.Add(SessionConstants.SESSION_ID, this.session.SessionId);
            webRequest.Headers.Add(SessionConstants.UUID, this.session.Uuid);
            webRequest.Headers.Add(SessionConstants.DEVICE_ID, "test");


            var bytes = Encoding.UTF8.GetBytes(serialisedJourneyTemplateDTO);
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

                var serviceResponseObject = JsonConvert.DeserializeObject<ServiceResponse>(serviceResponseString);

                Assert.AreEqual(ServiceResponseCode.Success, serviceResponseObject.ServiceResponseCode, "Service respponse code must be equal to success.");

                webResponse.Close();

                this.journeyTemplate =
                    this.findNDriveUnitOfWork.JourneyTemplateRepository.AsQueryable()
                        .IncludeChildren()
                        .FirstOrDefault(_ => _.Alias.Equals(MyTemplateName));

                Assert.AreEqual(this.journeyTemplate.Pets, this.journeyTemplateDTO.Pets);

            }
        }
    }
}
