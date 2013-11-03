using System.Collections.Generic;
using System.ServiceModel;
using System.ServiceModel.Web;
using DomainObjects;

namespace FindNDriveServices.Contracts
{
    [ServiceContract]
    public interface IPrototypeService
    {
        //[WebGet(UriTemplate = "user/?id={id}")]
        [WebInvoke(Method = "GET",
            ResponseFormat = WebMessageFormat.Json,
            UriTemplate = "user")]
        [OperationContract]
        User GetUser();

        [WebInvoke(Method = "POST", UriTemplate = "evals")]
        [OperationContract]
        void SaveUser(User user);

        [WebGet(UriTemplate = "users")]
        [OperationContract]
        List<User> GetAllUsers();
    }
}
