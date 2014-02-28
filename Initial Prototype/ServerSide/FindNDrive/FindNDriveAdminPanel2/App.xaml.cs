using System.Windows;
using CODE.Framework.Wpf.Mvvm;
using CODE.Framework.Wpf.Mvvm.Tools;


namespace FindNDriveAdminPanel2
{
    /// <summary>
    /// Interaction logic for App.xaml
    /// </summary>
    public partial class App
    {
        public App()
        {
            Startup += ApplicationStartup;
        }

        /// <summary>
        /// This method is where all the application startup code should go
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void ApplicationStartup(object sender, StartupEventArgs e)
        {
#if DEBUG
            var viewVisualizer = new ViewVisualizer();
            viewVisualizer.Show();
            Controller.RegisterViewHandler(viewVisualizer);
#endif

            // Launching a main form ('shell') and showing a login screen right away
            Controller.Action("Home", "Start");
            Controller.Action("User", "Login");
        }
    }
}
