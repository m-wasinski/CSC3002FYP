using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;
using System.Threading.Tasks;
using DomainObjects;
using DomainObjects.Domains;
using DomainObjects.DOmains;
using DomainObjects.Enums;

namespace FindNDriveServices.DTOs
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
        [DataMember(IsRequired = true),
        Microsoft.Build.Framework.Required]
        public User User { get; set; }

        /// <summary>
        /// Gets or sets the password.
        /// </summary>
        [DataMember]
        [Microsoft.Build.Framework.Required]
        public string Password { get; set; }

        /// <summary>
        /// Gets or sets the confirmed password.
        /// </summary>
        /// TODO Alan: Discuss why this is here
        [DataMember]
        [Microsoft.Build.Framework.Required]
        [Compare("Password", ErrorMessage = "The password and confirmation password do not match.")]
        public string ConfirmedPassword { get; set; }
    }
}
