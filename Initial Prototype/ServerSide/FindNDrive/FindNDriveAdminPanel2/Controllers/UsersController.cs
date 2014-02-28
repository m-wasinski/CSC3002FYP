using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FindNDriveAdminPanel2.Controllers
{
    using CODE.Framework.Wpf.Mvvm;

    using DomainObjects.Domains;

    using FindNDriveAdminPanel2.Models.Users;

    using FindNDriveDataAccessLayer;

    class UsersController : Controller
    {
        public ActionResult List(List<User> users, string windowTitle)
        {
            var model = new ListViewModel(users, windowTitle);
            return View(model);
        }

        public ActionResult Edit(int id)
        {
            var model = new EditViewModel();
            model.LoadData(id);
            return View(model);
        }
    }
}
