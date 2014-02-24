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
    using System.IO;
    using System.Security.Permissions;
    using System.ServiceModel;
    using System.ServiceModel.Web;
    using System.Web.Providers.Entities;

    using DomainObjects.Domains;

    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    using User = DomainObjects.Domains.User;

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

        /// <summary>
        /// The refresh user.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebGet(
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/refresh/?id={id}")]
        ServiceResponse<User> RefreshUser(int id);

        /// <summary>
        /// The update user.
        /// </summary>
        /// <param name="userDTO">
        /// The user dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/update")]
        ServiceResponse<User> UpdateUser(UserDTO userDTO);

        /// <summary>
        /// The get user picture by id.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebGet(
            UriTemplate = "/getProfilePicture?id={id}",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare)]
        Stream GetUserProfilePicture(int id);

        /// <summary>
        /// The update profile picture.
        /// </summary>
        /// <param name="profilePictureUpdaterDTO">
        /// The profile Picture Updater DTO.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(
            Method = "POST",
            UriTemplate = "/updateProfilePicture",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare)]
        ServiceResponse<bool> UpdateProfilePicture(ProfilePictureUpdaterDTO profilePictureUpdaterDTO);
    }
}
