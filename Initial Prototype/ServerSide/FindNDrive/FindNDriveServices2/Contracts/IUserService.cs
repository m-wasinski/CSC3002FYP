// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IUserService.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the IUserService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------
namespace FindNDriveServices2.Contracts
{
    using System.Security.Permissions;
    using System.ServiceModel;
    using System.ServiceModel.Web;
    using DomainObjects.DOmains;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The UserService interface.
    /// </summary>
    [ServiceContract]
    public interface IUserService
    {
        /// <summary>
        /// The manual user login.
        /// </summary>
        /// <param name="login">
        /// The login.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/manuallogin")]
        [PrincipalPermission(SecurityAction.Demand, Authenticated = true)]
        ServiceResponse<User> ManualUserLogin(LoginDTO login);

        /// <summary>
        /// The auto user login.
        /// </summary>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/autologin")]
        [PrincipalPermission(SecurityAction.Demand, Authenticated = true)]
        ServiceResponse<User> AutoUserLogin();

        /// <summary>
        /// The register user.
        /// </summary>
        /// <param name="register">
        /// The register.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/register")]
        ServiceResponse<User> RegisterUser(RegisterDTO register);

        /// <summary>
        /// The logout user.
        /// </summary>
        /// <param name="forceDelete">
        /// The force delete.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/logout")]
        ServiceResponse<bool> LogoutUser(bool forceDelete);
    }
}
