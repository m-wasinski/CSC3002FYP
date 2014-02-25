// --------------------------------------------------------------------------------------------------------------------
// <copyright file="LoginModel.cs" company="">
//   
// </copyright>
// <summary>
//   Represents the Login module within the system.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveAdminPanel.Models
{
    using System.ComponentModel;

    /// <summary>
    /// Represents the Login module within the system.
    /// </summary>
    public class LoginModel : INotifyPropertyChanged
    {
        /// <summary>
        /// The property changed.
        /// </summary>
        public event PropertyChangedEventHandler PropertyChanged;

        /// <summary>
        /// Gets or sets the username.
        /// </summary>
        public string Username { get; set; }

        /// <summary>
        /// Gets or sets the password.
        /// </summary>
        public string Password { get; set; }
    }
}
