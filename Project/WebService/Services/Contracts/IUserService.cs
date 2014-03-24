// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IUserService.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the IUserService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------
namespace Services.Contracts
{
    using System.IO;
    using System.ServiceModel;
    using System.ServiceModel.Web;

    using DomainObjects.Domains;

    using global::Services.DTOs;
    using global::Services.ServiceResponses;

    /// <summary>
    /// The UserService interface.
    /// </summary>
    [ServiceContract]
    public interface IUserService
    {
        /// <summary>
        /// Performs manual user login using user's username and password.
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
            UriTemplate = "/login")]
        ServiceResponse<User> ManualUserLogin(LoginDTO login);

        /// <summary>
        /// Performs automatic user login using the session information contained within HTTP headers.
        /// </summary>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/auto")]
        ServiceResponse<User> AutoUserLogin();

        /// <summary>
        /// Registers a new account within the system.
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
        /// Logs out the user.
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
        ServiceResponse LogoutUser(bool forceDelete);

        /// <summary>
        /// Updates information about given user.
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
        /// Retrieves profile picture of a given user.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebGet(
            UriTemplate = "/getpicture?id={id}",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare)]
        Stream GetUserProfilePicture(int id);

        /// <summary>
        /// Updates user's profile picture.
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
            UriTemplate = "/updatepicture",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare)]
        ServiceResponse UpdateProfilePicture(ProfilePictureUpdaterDTO profilePictureUpdaterDTO);

        /// <summary>
        /// Updates user's privacy settings.
        /// </summary>
        /// <param name="dto">
        /// The dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(
            Method = "POST",
            UriTemplate = "/updateprivacy",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare)]
        ServiceResponse<User> UpdatePrivacySettings(PrivacySettingsUpdaterDTO dto);

        /// <summary>
        /// Retrieves the user by its id.
        /// </summary>
        /// <param name="dto">
        /// The dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [WebInvoke(
            Method = "POST",
            UriTemplate = "/get",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare)]
        ServiceResponse<User> GetUser(UserRetrieverDTO dto);
    }
}
