// --------------------------------------------------------------------------------------------------------------------
// <copyright file="User.cs" company="">
//   
// </copyright>
// <summary>
//   Represents User entity.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DomainObjects.Domains
{
    using System;
    using System.Collections.Generic;
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;
    using DomainObjects.Constants;

    /// <summary>
    /// Represents User entity.
    /// </summary>
    [DataContract]
    public class User
    {
        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        [ConcurrencyCheck]
        [DatabaseGenerated(DatabaseGeneratedOption.None)]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the profile image.
        /// </summary>
        public ProfilePicture ProfilePicture { get; set; }

        /// <summary>
        /// Gets or sets the user name.
        /// </summary>
        [Required(ErrorMessage = "Username is required")]
        [DataMember]
        public string UserName { get; set; }

        /// <summary>
        /// Gets or sets the email address.
        /// </summary>
        [EmailAddress]
        [Required(ErrorMessage = "Valid email address is required")]
        [DataMember]
        public string EmailAddress { get; set; }

        /// <summary>
        /// Gets or sets the first name.
        /// </summary>
        [DataMember]
        public string FirstName { get; set; }

        /// <summary>
        /// Gets or sets the last name.
        /// </summary>
        [DataMember]
        public string LastName { get; set; }

        /// <summary>
        /// Gets or sets the date of birth.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime? DateOfBirth { get; set; }

        /// <summary>
        /// Gets or sets the gender.
        /// </summary>
        [DataMember]
        public virtual Gender? Gender { get; set; }

        /// <summary>
        /// Gets or sets the gcm registration id.
        /// </summary>
        [DataMember]
        public string GCMRegistrationID { get; set; }

        /// <summary>
        /// Gets or sets the travel buddies.
        /// </summary>
        public virtual ICollection<User> Friends { get; set; }

        /// <summary>
        /// Gets or sets the status.
        /// </summary>
        [DataMember]
        public Status Status { get; set; }

        /// <summary>
        /// Gets or sets the rating.
        /// </summary>
        public virtual ICollection<Rating> Rating { get; set; }

        /// <summary>
        /// Gets or sets the last logon.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime LastLogon { get; set; }

        /// <summary>
        /// Gets or sets the member since.
        /// </summary>
        [DataMember]
        [DataType(DataType.Date)]
        [Column(TypeName = "DateTime2")]
        public DateTime MemberSince { get; set; }

        /// <summary>
        /// Gets or sets the phone number.
        /// </summary>
        [DataMember]
        public string PhoneNumber { get; set; }

        /// <summary>
        /// Gets or sets the average rating.
        /// </summary>
        [DataMember]
        public double? AverageRating { get; set; }

        /// <summary>
        /// Gets or sets the unread messages count.
        /// </summary>
        [DataMember]
        [NotMapped]
        public int UnreadMessagesCount { get; set; }

        /// <summary>
        /// Gets or sets the journey templates.
        /// </summary>
        public virtual List<JourneyTemplate> JourneyTemplates { get; set; }

        /// <summary>
        /// Gets or sets the notifications.
        /// </summary>
        public virtual List<Notification> Notifications { get; set; }

        /// <summary>
        /// Gets or sets the privacy settings.
        /// </summary>
        [DataMember]
        public PrivacySettings PrivacySettings { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether journeys visible.
        /// </summary>
        [DataMember]
        public bool JourneysVisible { get; set; }

        /// <summary>
        /// Gets or sets the votes count.
        /// </summary>
        [DataMember]
        public int VotesCount { get; set; }
    }
}
