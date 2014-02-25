namespace FindNDriveAdminPanel.ViewModels
{
    using System.ServiceModel;
    using System.Windows.Input;

    using FindNDriveAdminPanel.Models;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    public class LoginViewModel 
    {
        public LoginModel LoginModel { get; set; }
        public ICommand LoginCommand { get; set; }

        private string serviceResponseErrors;
        private string serviceResponse;

        public LoginViewModel()
        {
            LoginModel = new LoginModel();
        }

        public bool Login()
        {
            // Create the proxy
            var channelFactory = new ChannelFactory<IUserService>("/manuallogin");
            var restfulProxy = channelFactory.CreateChannel();

            // Invoke a method
            var response =
                restfulProxy.ManualUserLogin(
                    new LoginDTO
                        {
                            GCMRegistrationID = "0",
                            Password = LoginModel.Password,
                            RememberMe = false,
                            UserName = LoginModel.Username
                        });

            // Return true only if the service respond code was a success
            return response.ServiceResponseCode == ServiceResponseCode.Success;
        }  
    }
}
