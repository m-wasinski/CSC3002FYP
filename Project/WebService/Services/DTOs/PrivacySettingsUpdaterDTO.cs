// --------------------------------------------------------------------------------------------------------------------
// <copyright file="PrivacySettingsUpdaterDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The privacy settings updater dto.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.DTOs
{
    using System.Runtime.Serialization;

    using DomainObjects.Constants;

    /// <summary>
    /// The privacy settings updater dto.
    /// </summary>
    [DataContract]
    public class PrivacySettingsUpdaterDTO
    {
        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the email privacy level.
        /// </summary>
        [DataMember]
        public PrivacyLevel EmailPrivacyLevel { get; set; }

        /// <summary>
        /// Gets or sets the gender privacy level.
        /// </summary>
        [DataMember]
        public PrivacyLevel GenderPrivacyLevel { get; set; }

        /// <summary>
        /// Gets or sets the date of birth privacy level.
        /// </summary>
        [DataMember]
        public PrivacyLevel DateOfBirthPrivacyLevel { get; set; }

        /// <summary>
        /// Gets or sets the phone number privacy level.
        /// </summary>
        [DataMember]
        public PrivacyLevel PhoneNumberPrivacyLevel { get; set; }

        /// <summary>
        /// Gets or sets the rating privacy level.
        /// </summary>
        [DataMember]
        public PrivacyLevel RatingPrivacyLevel { get; set; }

        /// <summary>
        /// Gets or sets the journeys privacy level.
        /// </summary>
        [DataMember]
        public PrivacyLevel JourneysPrivacyLevel { get; set; }

    }
}