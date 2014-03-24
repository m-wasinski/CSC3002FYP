// --------------------------------------------------------------------------------------------------------------------
// <copyright file="LoginDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The login DTO (Data Transfer Object) used to interact with the user service.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The login DTO (Data Transfer Object) used to interact with the user service.
    /// </summary>
    [DataContract]
    public class LoginDTO
    {
        /// <summary>
        /// Gets or sets the user name.
        /// </summary>
        [DataMember]
        public string UserName { get; set; }

        /// <summary>
        /// Gets or sets the password.
        /// </summary>
        [DataMember]
        public string Password { get; set; }

        /// <summary>
        /// Indicates whether user wants to create a session.
        /// </summary>
        [DataMember]
        public bool RememberMe { get; set; }

        /// <summary>
        /// Gets or sets the gcm registration id.
        /// </summary>
        [DataMember]
        public string GCMRegistrationID { get; set; }
    }
}
