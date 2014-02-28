using CODE.Framework.Wpf.Mvvm;
using FindNDriveAdminPanel2.Models.Home;

namespace FindNDriveAdminPanel2.Controllers
{
    public class HomeController : Controller
    {
        public ActionResult Start()
        {
            return Shell(new StartViewModel(), "Business Application");
        }
    }
}
