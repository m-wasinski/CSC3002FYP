// --------------------------------------------------------------------------------------------------------------------
// <copyright file="UserRegistrationUnitTest.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the UserRegistrationUnitTest type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveUnitTests
{
    using System;
    using System.IO;
    using System.Linq;
    using System.Net;
    using System.Text;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    using Microsoft.VisualStudio.TestTools.UnitTesting;

    using Newtonsoft.Json;

    /// <summary>
    /// The user registration unit test.
    /// </summary>
    [TestClass]
    public class UserServiceRegistrationUnitTest
    {
        /// <summary>
        /// The register user.
        /// </summary>
        [TestMethod]
        public void RegisterNewUser()
        {
            {
                // Create a unique combination for user name and email address to allow registration without errors.
                var timeSpan = DateTime.Now - new DateTime(1970, 1, 1, 0, 0, 0);
                var totalMilliseconds = (long)timeSpan.TotalMilliseconds + new Random().Next(0, 56456456);
                const string RandomChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

                var randomString =
                    new string(Enumerable.Repeat(RandomChars, 8).Select(s => s[new Random().Next(s.Length)]).ToArray());

                var registerDTO = new RegisterDTO()
                                      {
                                          Password = "password",
                                          ConfirmedPassword = "password",
                                          User =
                                              new User
                                                  {
                                                      UserName = "" + totalMilliseconds + randomString,
                                                      EmailAddress =
                                                          randomString + "" + totalMilliseconds
                                                          + "@domain.com",
                                                      Gender = Gender.Male
                                                  }
                                      };

                // Accept any SSL certificates.
                ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

                //serialise the registerDTO object into a json string.
                var serialisedRegisterDTO = JsonConvert.SerializeObject(
                    registerDTO,
                    typeof(RegisterDTO),
                    Formatting.Indented,
                    new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

                var registrationWebRequest =
                    WebRequest.Create("https://findndrive.no-ip.co.uk/Services/UserService.svc/register") as
                    HttpWebRequest;
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

                    var serviceResponseObject =
                        JsonConvert.DeserializeObject<ServiceResponse<User>>(serviceResponseString);

                    if (serviceResponseObject.ServiceResponseCode == ServiceResponseCode.Success)
                    {
                        Assert.AreEqual(
                            serviceResponseObject.Result.UserName,
                            registerDTO.User.UserName,
                            "Usernames must match.");
                        Assert.AreEqual(
                            serviceResponseObject.Result.EmailAddress,
                            registerDTO.User.EmailAddress,
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
                                new JsonSerializerSettings
                                    {
                                        DateFormatHandling = DateFormatHandling.MicrosoftDateFormat
                                    });

                        // Attempt to log in.
                        var loginWebRequest =
                            WebRequest.Create("https://findndrive.no-ip.co.uk/Services/UserService.svc/manuallogin") as
                            HttpWebRequest;

                        Assert.IsNotNull(registrationWebRequest);

                        loginWebRequest.Method = "POST";

                        // Add the necessary HTTP headers.
                        loginWebRequest.ContentType = "application/json";
                        loginWebRequest.Headers.Add(SessionConstants.UUID, "TEST");
                        loginWebRequest.Headers.Add(SessionConstants.DEVICE_ID, "TEST");

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
        }
    }
}