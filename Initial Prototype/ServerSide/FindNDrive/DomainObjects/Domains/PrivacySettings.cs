// --------------------------------------------------------------------------------------------------------------------
// <copyright file="PrivacySettings.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the PrivacySettings type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System.ComponentModel.DataAnnotations;
    using System.Runtime.Serialization;
    using DomainObjects.Constants;

    /// <summary>
    /// The privacy settings.
    /// </summary>
    [DataContract]
    public class PrivacySettings
    {
        /// <summary>
        /// Gets or sets the privacy settings id.
        /// </summary>
        [ConcurrencyCheck]
        public int PrivacySettingsId { get; set; }

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
