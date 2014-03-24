using CODE.Framework.Wpf.Mvvm;

namespace FindNDriveAdminPanel2.Controllers
{
    using AdminPanel.Models.Home;

    public class HomeController : Controller
    {
        public ActionResult Start()
        {
            return Shell(new StartViewModel(), "Business Application");
        }
    }
}
