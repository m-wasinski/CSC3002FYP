using System;
using CODE.Framework.Wpf.Mvvm;

namespace FindNDriveAdminPanel2.Models.User
{
    using AdminPanel.Models.Home;

    public class LoginViewModel : ViewModel
    {
        public LoginViewModel()
        {
            Actions.Add(new ViewAction("Login", execute: Login));
            Actions.Add(new ApplicationShutdownViewAction("Cancel"));
        }

        public string UserName { get; set; }
        public string Password { get; set; }

        private void Login(IViewAction action, object parameter)
        {
            // TODO: Perform actual user login here

            AppDomain.CurrentDomain.SetThreadPrincipal(new CODEFrameworkPrincipal(new CODEFrameworkUser(UserName)));

            Controller.CloseViewForModel(this);

            StartViewModel.Current.LoadActions();
        }
    }
}
