namespace FindNDriveAdminPanel.ViewModels
{
    using System;
    using System.Net;
    using System.ServiceModel;
    using System.ServiceModel.Channels;
    using System.Web.Security;
    using System.Windows.Controls;
    using System.Windows.Input;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveAdminPanel.Models;

    using FindNDriveDataAccessLayer;

    using FindNDriveInfrastructureDataAccessLayer;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    using Newtonsoft.Json;

    using WebMatrix.WebData;

    public class LoginViewModel 
    {
        public LoginModel LoginModel { get; set; }
        public ICommand LoginCommand { get; set; }

        private string serviceResponseErrors;
        private string serviceResponse;

        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        public LoginViewModel()
        {
            LoginModel = new LoginModel();
            LoginCommand = new LoginCommand(this);

            var dbContext = new ApplicationContext();
            var userRepository = new EntityFrameworkRepository<User>(dbContext);
            var journeyRepository = new EntityFrameworkRepository<Journey>(dbContext);
            var sessionEntityFrameworkRepository = new EntityFrameworkRepository<Session>(dbContext);
            var journeyRequestRepository = new EntityFrameworkRepository<JourneyRequest>(dbContext);
            var chatMessageRepository = new EntityFrameworkRepository<ChatMessage>(dbContext);
            var notificationRepository = new EntityFrameworkRepository<Notification>(dbContext);
            var friendsRequestRepository = new EntityFrameworkRepository<FriendRequest>(dbContext);
            var journeyMessageRepository = new EntityFrameworkRepository<JourneyMessage>(dbContext);
            var geoAddressRepository = new EntityFrameworkRepository<GeoAddress>(dbContext);
            var ratingsRepository = new EntityFrameworkRepository<Rating>(dbContext);
            var profilePictureRepository = new EntityFrameworkRepository<ProfilePicture>(dbContext);

            this.findNDriveUnitOfWork = new FindNDriveUnitOfWork(
                dbContext,
                userRepository,
                journeyRepository,
                sessionEntityFrameworkRepository,
                journeyRequestRepository,
                chatMessageRepository,
                notificationRepository,
                friendsRequestRepository,
                journeyMessageRepository,
                geoAddressRepository,
                ratingsRepository,
                profilePictureRepository);
        }

        public bool CanLogin
        {
            get
            {
                return !string.IsNullOrWhiteSpace(LoginModel.Username);
            }
        }
        interface IAuthentication
        {
            void Login(string username);
            void Logout();
        }

        class FormsAuth : IAuthentication
        {
            public void Login(string username)
            {
                FormsAuthentication.SetAuthCookie(username, false);
            }

            public void Logout()
            {
                FormsAuthentication.SignOut();
            }
        }
        public bool Login(string username, string password)
        {
            FormsAuth form = new FormsAuth();
            form.Login("laura");
            Console.Write(string.Format("{0}, {1}", username, password));
            // Initialise the WebSecurity module.
            if (!WebSecurity.Initialized)
            {
                WebSecurity.InitializeDatabaseConnection("FindNDriveConnectionString", "User", "Id", "UserName", true);
            }
            WebSecurity.Login("laura", "p", false);

            // Accept any SSL certificates.
            ServicePointManager.ServerCertificateValidationCallback = (obj, certificate, chain, errors) => true;
            // Create the proxy
            var channelFactory = new ChannelFactory<IUserService>("/manuallogin");
            //channelFactory.Endpoint.Behaviors.Add(new WebHttpBehavior());
            var restfulProxy = channelFactory.CreateChannel();

            //Create OperationContextScope with the proxy:
            var contextScope = new OperationContextScope((IContextChannel) restfulProxy);
            //Create HttpRequestMessageProperty and configure credentials:
            HttpRequestMessageProperty httpHeaders = new HttpRequestMessageProperty();

            httpHeaders.Headers[SessionConstants.DEVICE_ID] = "TEST";
            httpHeaders.Headers[SessionConstants.UUID] = "TEST";
            httpHeaders.Headers[SessionConstants.REMEMBER_ME] = "0";


            //Add HttpRequestMessageProperty to the OperationContexts outgoing message properties:
            OperationContext.Current.OutgoingMessageProperties[HttpRequestMessageProperty.Name] = httpHeaders;

            // Invoke a method
            var response =
                restfulProxy.ManualUserLogin(
                    new LoginDTO
                        {
                            Password = "p",
                            GCMRegistrationID = null,
                            RememberMe = false,
                            UserName = "laura"
                        });

            Console.Write(JsonConvert.SerializeObject(response.Result));
            // Return true only if the service respond code was a success*/
            return response.ServiceResponseCode == ServiceResponseCode.Success;
        }  
    }

    public class LoginCommand : ICommand
    {
        private LoginViewModel loginViewModel;

        public LoginCommand(LoginViewModel loginViewModel)
        {
            this.loginViewModel = loginViewModel;
        }

        public bool CanExecute(object parameter)
        {
            return loginViewModel.CanLogin;
        }

        public void Execute(object parameter)
        {
            var passwordBox = parameter as PasswordBox;
            var password = passwordBox.Password;

            this.loginViewModel.Login(loginViewModel.LoginModel.Username, password);
        }

        public event EventHandler CanExecuteChanged
        {
            add
            {
                CommandManager.RequerySuggested += value;
            }

            remove
            {
                CommandManager.RequerySuggested -= value;
            }
        }
    }
}
