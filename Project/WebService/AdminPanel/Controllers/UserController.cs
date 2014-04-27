using CODE.Framework.Wpf.Mvvm;
using FindNDriveAdminPanel2.Models.User;

namespace FindNDriveAdminPanel2.Controllers
{
    using AdminPanel.Models.User;

    public class UserController : Controller
    {
        public ActionResult Login()
        {
            return ViewModal(new LoginViewModel(), ViewLevel.Popup);
        }
    }
}
