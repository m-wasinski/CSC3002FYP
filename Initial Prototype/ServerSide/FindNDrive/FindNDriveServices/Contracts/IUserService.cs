using System.Collections.Generic;
using System.ServiceModel;
using System.ServiceModel.Web;
using DomainObjects;
using FindNDriveServices.DTOs;
using FindNDriveServices.ServiceResponses;

namespace FindNDriveServices.Contracts
{
    [ServiceContract]
    public interface IUserService
    {
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/login")]
        ServiceResponse<User> LoginUser(LoginDTO login);

        [OperationContract]
        [WebInvoke(Method = "GET",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "register")]
        ServiceResponse<User> RegisterUser();
        [OperationContract]
        [WebInvoke(Method = "GET",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "users")]
        ServiceResponse<User> GetUsers();
    }
}
