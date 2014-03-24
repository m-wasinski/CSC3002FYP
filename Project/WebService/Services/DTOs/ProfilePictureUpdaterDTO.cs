// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ProfilePictureUpdaterDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The profile picture updater dto.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The profile picture updater dto.
    /// </summary>
    [DataContract]
    public class ProfilePictureUpdaterDTO
    {
        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the picture.
        /// </summary>
        [DataMember]
        public string Picture { get; set; }
    }
}