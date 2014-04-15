namespace Tests.IntegrationTests
{
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
    public class RatingServiceIntegrationTests
    {
        private User driver;

        private Session driverSession;

        private User ratingUser;

        private Session ratingUserSession;

        private FindNDriveUnitOfWork findNDriveUnitOfWork;

        private RatingDTO ratingDTO;

        private Rating rating;

        [TestInitialize]
        public void Initialise()
        {
            this.findNDriveUnitOfWork = new FindNDriveUnitOfWork();

            this.driver = this.findNDriveUnitOfWork.UserRepository.Find(1);
            this.driverSession = this.findNDriveUnitOfWork.SessionRepository.Find(1);
            this.driverSession.DeviceId = SessionManager.EncryptValue("test");
            this.driverSession.ExpiryDate = this.driverSession.ExpiryDate.AddDays(1);

            this.ratingUser = this.findNDriveUnitOfWork.UserRepository.Find(2);
            this.ratingUserSession = this.findNDriveUnitOfWork.SessionRepository.Find(2);
            this.ratingUserSession.DeviceId = SessionManager.EncryptValue("test");
            this.ratingUserSession.ExpiryDate = this.ratingUserSession.ExpiryDate.AddDays(2);

            this.rating =
                    this.findNDriveUnitOfWork.RatingsRepository.AsQueryable()
                        .IncludeChildren()
                        .FirstOrDefault(_ => _.FromUser.UserId == ratingUser.UserId && _.UserId == driver.UserId);

            if (rating != null)
            {
                this.findNDriveUnitOfWork.RatingsRepository.Remove(rating);
            }

            this.findNDriveUnitOfWork.Commit();

            this.ratingDTO = new RatingDTO
            {
                FromUserId = ratingUser.UserId,
                TargetUserId = driver.UserId,
                Feedback = "Great driver",
                Score = 5
            };

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            //serialise the registerDTO object into a json string.
            var serialisedRatingDTO = JsonConvert.SerializeObject(
                this.ratingDTO,
                typeof(RatingDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/RatingService.svc/rate") as HttpWebRequest;
            Assert.IsNotNull(webRequest);

            webRequest.Method = "POST";

            // Add the necessary HTTP headers.
            webRequest.ContentType = "application/json";
            webRequest.Headers.Add(SessionConstants.SESSION_ID, this.ratingUserSession.SessionId);
            webRequest.Headers.Add(SessionConstants.UUID, this.ratingUserSession.Uuid);
            webRequest.Headers.Add(SessionConstants.DEVICE_ID, "test");


            var bytes = Encoding.UTF8.GetBytes(serialisedRatingDTO);
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

                this.rating =
                    this.findNDriveUnitOfWork.RatingsRepository.AsQueryable()
                        .IncludeChildren()
                        .FirstOrDefault(_ => _.FromUser.UserId == ratingUser.UserId && _.UserId == driver.UserId);

                webResponse.Close();
            }
        }

        [TestMethod]
        public void TestSubmitRating()
        {
            Assert.IsNotNull(this.rating);
        }
    }
}
