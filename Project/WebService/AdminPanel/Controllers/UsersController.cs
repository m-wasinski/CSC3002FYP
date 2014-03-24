namespace AdminPanel.Controllers
{
    using System.Collections.Generic;

    using AdminPanel.Models.Users;

    using CODE.Framework.Wpf.Mvvm;

    using DomainObjects.Domains;

    class UsersController : Controller
    {
        public ActionResult List(List<User> users, string windowTitle)
        {
            var model = new ListViewModel(users, windowTitle);
            return this.View(model);
        }

        public ActionResult Edit(int id)
        {
            var model = new EditViewModel();
            model.LoadData(id);
            return this.View(model);
        }

        public ActionResult Search(List<User> users, string windowTitle)
        {
            var model = new SearchViewModel(users, windowTitle);
            return this.View(model);
        }
    }
}
