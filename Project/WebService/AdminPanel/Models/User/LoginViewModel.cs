namespace AdminPanel.Models.User
{
    using System;
    using System.IO;
    using System.Linq;
    using System.Net;
    using System.Text;
    using System.Windows;

    using AdminPanel.Models.Home;

    using CODE.Framework.Wpf.Mvvm;

    using DomainObjects.Constants;

    using FindNDriveAdminPanel2.Models.User;

    using Newtonsoft.Json;

    using Services.DTOs;
    using Services.ServiceResponses;

    using Formatting = System.Xml.Formatting;
    using MessageBoxResult = CODE.Framework.Wpf.Mvvm.MessageBoxResult;

    public class LoginViewModel : ViewModel
    {
        public LoginViewModel()
        {
            this.Actions.Add(new ViewAction("Login", execute: this.Login));
            this.Actions.Add(new ApplicationShutdownViewAction("Cancel"));
        }

        public string UserName { get; set; }
        public string Password { get; set; }

        private void Login(IViewAction action, object parameter)
        {
            if (UserName == null | Password == null)
            {
                MessageBox.Show("UserName and password cannot be blank");
                return;
            }

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;

            var serialisedLoginDTO = JsonConvert.SerializeObject(
                new LoginDTO { UserName = this.UserName, Password = this.Password }, 
                typeof(LoginDTO),
                Newtonsoft.Json.Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            var webRequest = WebRequest.Create("https://findndrive.no-ip.co.uk/Services/UserService.svc/adminlogin") as HttpWebRequest;

            webRequest.Method = "POST";
            webRequest.ContentType = "application/json";

            var bytes = Encoding.UTF8.GetBytes(serialisedLoginDTO);
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

                var serviceResponseObject = JsonConvert.DeserializeObject<ServiceResponse>(serviceResponseString);

                if (serviceResponseObject.ServiceResponseCode == ServiceResponseCode.Success)
                {
                    AppDomain.CurrentDomain.SetThreadPrincipal(new CODEFrameworkPrincipal(new CODEFrameworkUser(this.UserName)));

                    Controller.CloseViewForModel(this);

                    StartViewModel.Current.LoadActions();
                }
                else
                {
                    MessageBox.Show(serviceResponseObject.ErrorMessages.First());
                }

                webResponse.Close();
            }           
        }
    }
}
