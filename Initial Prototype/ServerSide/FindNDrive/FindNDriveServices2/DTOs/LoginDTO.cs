using System.Runtime.Serialization;

namespace FindNDriveServices2.DTOs
{
    /// <summary>
    /// The login DTO (Data Transfer Object) used to interact with the user service.
    /// </summary>
    [DataContract]
    public class LoginDTO
    {
        /// <summary>
        /// Gets or sets the user name.
        /// </summary>
        [DataMember(IsRequired = true)]
        public string UserName { get; set; }

        /// <summary>
        /// Gets or sets the password.
        /// </summary>
        [DataMember(IsRequired = true)]
        public string Password { get; set; }
    }
}
