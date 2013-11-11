using System.ComponentModel.DataAnnotations;
using System.Runtime.Serialization;
using DomainObjects.DOmains;
using FindNDriveInfrastructureCore;

namespace FindNDriveServices2.DTOs
{
    /// <summary>
    /// DataContract that will be sent as part of the IUserService.RegisterUser request
    /// </summary>
    [DataContract]
    public class RegisterDTO
    {
        /// <summary>
        /// Gets or sets the user.
        /// </summary>
        [DataMember]
        [Required]
        [ValidateObject]
        public User User { get; set; }

        /// <summary>
        /// Gets or sets the password.
        /// </summary>
        [DataMember]
        [Required]
        [Compare("ConfirmedPassword", ErrorMessage = "The password and confirmation password do not match.")]
        public string Password { get; set; }

        /// <summary>
        /// Gets or sets the confirmed password.
        /// </summary>
        [DataMember]
        [Required]
        [Compare("Password", ErrorMessage = "The password and confirmation password do not match.")]
        public string ConfirmedPassword { get; set; }
    }
}
