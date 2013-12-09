using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.Mvc;

namespace WebApiTest.Controllers
{
    using FindNDriveDataAccessLayer;

    public class HomeController : Controller
    {
        public ActionResult Index()
        {
            return View();
        }
    }
}
