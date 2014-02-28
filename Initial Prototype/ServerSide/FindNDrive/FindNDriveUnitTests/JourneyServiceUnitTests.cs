using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace FindNDriveUnitTests
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.IO;
    using System.Net;
    using System.Text;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    using Newtonsoft.Json;

    /// <summary>
    /// The journey unit tests.
    /// </summary>
    [TestClass]
    public class JourneyServiceUnitTests
    {
        /// <summary>
        /// The create new journey.
        /// </summary>
        [TestMethod]
        public void CreateNewJourney()
        {
            /*var geoAddress1 = new GeoAddress { AddressLine = "Dublin", Latitude = 53.3478, Longitude = -6.2597, Order = 1};
            var geoAddress2 = new GeoAddress { AddressLine = "Warrenpoint", Latitude = 54.09900, Longitude = -6.24900, Order = 2 };
            var geoAddress3 = new GeoAddress { AddressLine = "Belfast", Latitude = 54.5970, Longitude = -5.9300, Order = 3};

            var journey = new JourneyDTO()
                        {
                            AvailableSeats = 4,
                            DateAndTimeOfDeparture = new DateTime(2014, 2, 1, 20, 15, 0),
                            GeoAddresses = new List<GeoAddress> { geoAddress1, geoAddress2, geoAddress3 },
                            Description = "Free ride to Dublin!",
                          
                            //DriverId = 4,
                            SmokersAllowed = false,
                            JourneyStatus = JourneyStatus.OK,
                            CreationDate = DateTime.Now,
                        };

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            //serialise the registerDTO object into a json string.
            var serialisedRegisterDTO = JsonConvert.SerializeObject(
                journey,
                typeof(JourneyDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/JourneyService.svc/new") as HttpWebRequest;
            Assert.IsNotNull(webRequest);

            webRequest.Method = "POST";

            // Add the necessary HTTP headers.
            webRequest.ContentType = "application/json";
            webRequest.Headers.Add(SessionConstants.SESSION_ID, "4:Test1");
            webRequest.Headers.Add(SessionConstants.UUID, "Test1");
            webRequest.Headers.Add(SessionConstants.DEVICE_ID, "Test1");
           

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
                Assert.AreEqual(journey.GeoAddresses.Count, serviceResponseObject.Result.GeoAddresses.Count, "Number of geoaddresses must match.");
                Assert.AreEqual(journey.CreationDate, journey.CreationDate, "Creation dates must be identical.");
                webResponse.Close();
            }*/
        }
    }
}
