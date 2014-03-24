// --------------------------------------------------------------------------------------------------------------------
// <copyright file="UserRegistrationUnitTest.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the UserRegistrationUnitTest type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Tests.IntegrationTests
{
    using System;
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

    /// <summary>
    /// The user registration unit test.
    /// </summary>
    [TestClass]
    public class UserServiceIntegrationTests
    {
        /// <summary>
        /// The random chars.
        /// </summary>
        private const string RandomChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        /// <summary>
        /// The user.
        /// </summary>
        private User user;

        /// <summary>
        /// The register dto.
        /// </summary>
        private RegisterDTO registerDTO;

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

            var randomString =
                new string(Enumerable.Repeat(RandomChars, 8).Select(s => s[new Random().Next(s.Length)]).ToArray())
                + DateTime.Now.Millisecond;

            this.user = new User
                       {
                           UserName = randomString,
                           EmailAddress = randomString + "@domain.com",
                           Gender = Gender.Male
                       };

            this.registerDTO = new RegisterDTO()
            {
                Password = "password",
                ConfirmedPassword = "password",
                User = this.user
            };
        }
        
        /// <summary>
        /// Tests registration functionality where the data 
        /// </summary>
        [TestMethod]
        public void RegisterNewUser()
        {
            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            //serialise the registerDTO object into a json string.
            var serialisedRegisterDTO = JsonConvert.SerializeObject(
                this.registerDTO,
                typeof(RegisterDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var registrationWebRequest =
                WebRequest.Create("https://findndrive.no-ip.co.uk/Services/UserService.svc/register") as HttpWebRequest;
            Assert.IsNotNull(registrationWebRequest);

            registrationWebRequest.Method = "POST";

            // Add the necessary HTTP headers.
            registrationWebRequest.ContentType = "application/json";
            registrationWebRequest.Headers.Add(SessionConstants.UUID, "test");
            registrationWebRequest.Headers.Add(SessionConstants.DEVICE_ID, "test");
            registrationWebRequest.Headers.Add(SessionConstants.REMEMBER_ME, 0 + string.Empty);

            var bytes = Encoding.UTF8.GetBytes(serialisedRegisterDTO);
            registrationWebRequest.ContentLength = bytes.Length;
            var outputStream = registrationWebRequest.GetRequestStream();
            outputStream.Write(bytes, 0, bytes.Length);
            outputStream.Close();

            // Make call to the service and retrieve response.
            var webResponse = (HttpWebResponse)registrationWebRequest.GetResponse();

            // Deserialise and analyse the response returned from the server.
            using (var sr = new StreamReader(webResponse.GetResponseStream()))
            {
                var serviceResponseString = sr.ReadToEnd();

                Assert.IsNotNull(serviceResponseString);
                Assert.AreNotEqual(string.Empty, serviceResponseString);

                var serviceResponseObject = JsonConvert.DeserializeObject<ServiceResponse<User>>(serviceResponseString);
                
                if (serviceResponseObject.ServiceResponseCode == ServiceResponseCode.Success)
                {
                    this.user = serviceResponseObject.Result;

                    Assert.AreEqual(
                        serviceResponseObject.Result.UserName,
                        this.registerDTO.User.UserName,
                        "Usernames must match.");
                    Assert.AreEqual(
                        serviceResponseObject.Result.EmailAddress,
                        this.registerDTO.User.EmailAddress,
                        "Email addresses must match also.");

                    // Let's now attempt to log in.
                    // Serialise the user object object into a json string.
                    var serialisedLoginDTOobject =
                        JsonConvert.SerializeObject(
                            new LoginDTO
                                {
                                    GCMRegistrationID = "0",
                                    Password = "password",
                                    RememberMe = false,
                                    UserName = serviceResponseObject.Result.UserName
                                },
                            typeof(LoginDTO),
                            Formatting.Indented,
                            new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

                    // Attempt to log in.
                    var loginWebRequest =
                        WebRequest.Create("https://findndrive.no-ip.co.uk/Services/UserService.svc/login") as
                        HttpWebRequest;

                    Assert.IsNotNull(registrationWebRequest);

                    loginWebRequest.Method = "POST";

                    // Add the necessary HTTP headers.
                    loginWebRequest.ContentType = "application/json";
                    loginWebRequest.Headers.Add(SessionConstants.UUID, "test");
                    loginWebRequest.Headers.Add(SessionConstants.DEVICE_ID, "test");

                    var userBytes = Encoding.UTF8.GetBytes(serialisedLoginDTOobject);
                    loginWebRequest.ContentLength = userBytes.Length;
                    var outputStream2 = loginWebRequest.GetRequestStream();
                    outputStream2.Write(userBytes, 0, userBytes.Length);
                    outputStream2.Close();

                    // Make call to the service and retrieve response.
                    var userWebResponse = (HttpWebResponse)loginWebRequest.GetResponse();
                    
                    using (var sr2 = new StreamReader(userWebResponse.GetResponseStream()))
                    {
                        var serviceResponseString2 = sr2.ReadToEnd();

                        Assert.IsNotNull(serviceResponseString2);
                        Assert.AreNotEqual(string.Empty, serviceResponseString2);

                        var serviceResponseObject2 =
                            JsonConvert.DeserializeObject<ServiceResponse<User>>(serviceResponseString2);

                        Assert.AreEqual(
                            ServiceResponseCode.Success,
                            serviceResponseObject2.ServiceResponseCode,
                            "User should be logged in successfully.");
                    }
                }

                if (serviceResponseObject.ServiceResponseCode == ServiceResponseCode.Failure)
                {
                    Assert.IsNotNull(
                        serviceResponseObject.ErrorMessages.FirstOrDefault(
                            _ =>
                            _.Contains("Account with this username already exists.")
                            || _.Contains("Account with this email address already exists.")
                            || _.Contains(
                                "System.Web.Security.MembershipCreateUserException: The username is already in use.")));
                }

                webResponse.Close();
            }
        }

        /// <summary>
        /// Attempts to register an invalid user.
        /// </summary>
        [TestMethod]
        public void RegisterInvalidUser()
        {
            var blankRegisterDto = new RegisterDTO();
            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            //serialise the registerDTO object into a json string.
            var serialisedRegisterDTO = JsonConvert.SerializeObject(
                blankRegisterDto,
                typeof(RegisterDTO),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var registrationWebRequest =
                WebRequest.Create("https://findndrive.no-ip.co.uk/Services/UserService.svc/register") as HttpWebRequest;
            Assert.IsNotNull(registrationWebRequest);

            registrationWebRequest.Method = "POST";

            // Add the necessary HTTP headers.
            registrationWebRequest.ContentType = "application/json";
            registrationWebRequest.Headers.Add(SessionConstants.UUID, "TEST");
            registrationWebRequest.Headers.Add(SessionConstants.DEVICE_ID, "TEST");
            registrationWebRequest.Headers.Add(SessionConstants.REMEMBER_ME, 0 + string.Empty);

            var bytes = Encoding.UTF8.GetBytes(serialisedRegisterDTO);
            registrationWebRequest.ContentLength = bytes.Length;
            var outputStream = registrationWebRequest.GetRequestStream();
            outputStream.Write(bytes, 0, bytes.Length);
            outputStream.Close();

            // Make call to the service and retrieve response.
            var webResponse = (HttpWebResponse)registrationWebRequest.GetResponse();

            // Deserialise and analyse the response returned from the server.
            using (var sr = new StreamReader(webResponse.GetResponseStream()))
            {
                var serviceResponseString = sr.ReadToEnd();

                Assert.IsNotNull(serviceResponseString);
                Assert.AreNotEqual(string.Empty, serviceResponseString);

                var serviceResponseObject = JsonConvert.DeserializeObject<ServiceResponse<User>>(serviceResponseString);

                Assert.AreEqual(
                        serviceResponseObject.ServiceResponseCode,
                        ServiceResponseCode.Failure,
                        "The service response should indicate a failure.");

                Assert.AreEqual(3, serviceResponseObject.ErrorMessages.Count, "There should be 3 error messages.");
            }
        }

        /// <summary>
        /// The tear down.
        /// </summary>
        [TestCleanup]
        public void TearDown()
        {
            if (this.user.UserId == 0)
            {
                return;
            }

            var currentUser = this.findNDriveUnitOfWork.UserRepository.Find(this.user.UserId);
            var currentSession = this.findNDriveUnitOfWork.SessionRepository.Find(this.user.UserId);
            this.findNDriveUnitOfWork.UserRepository.Remove(currentUser);
            this.findNDriveUnitOfWork.SessionRepository.Remove(currentSession);
            this.findNDriveUnitOfWork.Commit();
        }
    }

}