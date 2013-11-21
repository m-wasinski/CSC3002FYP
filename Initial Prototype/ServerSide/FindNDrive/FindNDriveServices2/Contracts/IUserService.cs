using System.Security.Permissions;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.Web.Security;
using DomainObjects.DOmains;
using FindNDriveServices2.DTOs;
using FindNDriveServices2.ServiceResponses;

namespace FindNDriveServices2.Contracts
{
    [ServiceContract]
    public interface IUserService
    {
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/manuallogin")]
        [PrincipalPermission(SecurityAction.Demand, Authenticated = true)]
        ServiceResponse<User> ManualUserLogin(LoginDTO login);

        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/autologin")]
        [PrincipalPermission(SecurityAction.Demand, Authenticated = true)]
        ServiceResponse<User> AutoUserLogin();

        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/register")]
        ServiceResponse<User> RegisterUser(RegisterDTO register);

        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "users")]
        [PrincipalPermission(SecurityAction.Demand, Authenticated = true)]
        ServiceResponse<User> GetUsers();

        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "test")]
        ServiceResponse<User> TestAuthentication(UserDTO userDTO);
    }
}
