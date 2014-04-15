namespace Tests.IntegrationTests
{
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
    public class FriendsServiceIntegrationTests
    {
        /// <summary>
        /// The driver.
        /// </summary>
        private User user1;

        /// <summary>
        /// The participant.
        /// </summary>
        private User user2;

        /// <summary>
        /// The participants session.
        /// </summary>
        private Session user2Session;

        /// <summary>
        /// The driver's session.
        /// </summary>
        private Session user1Session;

        private FriendRequestDTO friendRequestDTO;

        private FriendRequest friendRequest;

        /// <summary>
        /// The find n drive unit of work.
        /// </summary>
        private FindNDriveUnitOfWork findNDriveUnitOfWork;

        [TestInitialize]
        public void Initialise()
        {
            this.findNDriveUnitOfWork = new FindNDriveUnitOfWork();

            this.user1 = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeChildren().First(_ => _.UserId == 1);
            this.user1Session = this.findNDriveUnitOfWork.SessionRepository.Find(1);
            this.user1Session.DeviceId = SessionManager.EncryptValue("test");
            this.user1Session.ExpiryDate = this.user1Session.ExpiryDate.AddDays(2);

            this.user2 = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeChildren().First(_ => _.UserId == 2);
            this.user2Session = this.findNDriveUnitOfWork.SessionRepository.Find(2);
            this.user2Session.DeviceId = SessionManager.EncryptValue("test");
            this.user2Session.ExpiryDate = this.user2Session.ExpiryDate.AddDays(2);

            this.friendRequest =
                    this.findNDriveUnitOfWork.FriendRequestsRepository.AsQueryable()
                        .IncludeChildren()
                        .FirstOrDefault(_ => _.FromUser.UserId == user1.UserId && _.ToUser.UserId == user2.UserId);

            if (friendRequest != null)
            {
                this.findNDriveUnitOfWork.FriendRequestsRepository.Remove(friendRequest);
                this.findNDriveUnitOfWork.Commit();
            }

            if (user1.Friends.Contains(user2) && user2.Friends.Contains(user1))
            {
                user1.Friends.Remove(user2);
                user2.Friends.Remove(user1);
            }

            this.findNDriveUnitOfWork.Commit();

            this.friendRequestDTO = new FriendRequestDTO
                                        {
                                            FromUser = user1,
                                            ToUser = user2,
                                            Message = "Hello!"
                                        };

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            //serialise the registerDTO object into a json string.
            var serialisedFriendRequestDTO = JsonConvert.SerializeObject(
                this.friendRequestDTO,
                typeof(FriendRequestDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/FriendsService.svc/sendrequest") as HttpWebRequest;
            Assert.IsNotNull(webRequest);

            webRequest.Method = "POST";

            // Add the necessary HTTP headers.
            webRequest.ContentType = "application/json";
            webRequest.Headers.Add(SessionConstants.SESSION_ID, this.user1Session.SessionId);
            webRequest.Headers.Add(SessionConstants.UUID, this.user1Session.Uuid);
            webRequest.Headers.Add(SessionConstants.DEVICE_ID, "test");


            var bytes = Encoding.UTF8.GetBytes(serialisedFriendRequestDTO);
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

                this.friendRequest =
                    this.findNDriveUnitOfWork.FriendRequestsRepository.AsQueryable()
                        .IncludeChildren()
                        .FirstOrDefault(_ => _.FromUser.UserId == user1.UserId && _.ToUser.UserId == user2.UserId);

                webResponse.Close();
            }
        }

        [TestMethod]
        public void TestSendFriendRequest()
        {
            Assert.IsNotNull(this.friendRequest);
        }

        [TestMethod]
        public void TestAcceptFriendRequest()
        {
            this.friendRequestDTO.Decision = Decision.Accepted;
            this.friendRequestDTO.FriendRequestId = this.friendRequest.FriendRequestId;

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            //serialise the registerDTO object into a json string.
            var serialisedFriendRequestDTO = JsonConvert.SerializeObject(
                this.friendRequestDTO,
                typeof(FriendRequestDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/FriendsService.svc/process") as HttpWebRequest;
            Assert.IsNotNull(webRequest);

            webRequest.Method = "POST";

            // Add the necessary HTTP headers.
            webRequest.ContentType = "application/json";
            webRequest.Headers.Add(SessionConstants.SESSION_ID, this.user1Session.SessionId);
            webRequest.Headers.Add(SessionConstants.UUID, this.user1Session.Uuid);
            webRequest.Headers.Add(SessionConstants.DEVICE_ID, "test");


            var bytes = Encoding.UTF8.GetBytes(serialisedFriendRequestDTO);
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

                this.user1 =
                    this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                        .Include(_ => _.Friends)
                        .FirstOrDefault(_ => _.UserId == user1.UserId);
                
                this.user2 =
                    this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                        .Include(_ => _.Friends)
                        .FirstOrDefault(_ => _.UserId == user2.UserId);

                Assert.AreEqual(1, user1.Friends.Count);
                Assert.AreEqual(1, user2.Friends.Count);

                Assert.AreEqual(user1.UserName, user2.Friends.First().UserName);
                Assert.AreEqual(user2.UserName, user1.Friends.First().UserName);

                user1.Friends.Remove(user2);
                user2.Friends.Remove(user1);
                this.findNDriveUnitOfWork.Commit();

                webResponse.Close();
            }
        }

        [TestMethod]
        public void TestDenyFriendRequest()
        {
            this.friendRequestDTO.Decision = Decision.Denied;
            this.friendRequestDTO.FriendRequestId = this.friendRequest.FriendRequestId;

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            //serialise the registerDTO object into a json string.
            var serialisedFriendRequestDTO = JsonConvert.SerializeObject(
                this.friendRequestDTO,
                typeof(FriendRequestDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/FriendsService.svc/process") as HttpWebRequest;
            Assert.IsNotNull(webRequest);

            webRequest.Method = "POST";

            // Add the necessary HTTP headers.
            webRequest.ContentType = "application/json";
            webRequest.Headers.Add(SessionConstants.SESSION_ID, this.user1Session.SessionId);
            webRequest.Headers.Add(SessionConstants.UUID, this.user1Session.Uuid);
            webRequest.Headers.Add(SessionConstants.DEVICE_ID, "test");


            var bytes = Encoding.UTF8.GetBytes(serialisedFriendRequestDTO);
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

                this.user1 =
                    this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                        .Include(_ => _.Friends)
                        .First(_ => _.UserId == user1.UserId);

                this.user2 =
                    this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                        .Include(_ => _.Friends)
                        .First(_ => _.UserId == user2.UserId);

                Assert.AreEqual(0, user1.Friends.Count);
                Assert.AreEqual(0, user2.Friends.Count);

                webResponse.Close();
            }
        }
    }
}
