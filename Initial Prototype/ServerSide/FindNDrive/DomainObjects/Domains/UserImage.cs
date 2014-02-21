namespace DomainObjects.Domains
{
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Runtime.Serialization;

    /// <summary>
    /// The user image.
    /// </summary>
    [DataContract]
    public class UserImage
    {
        /// <summary>
        /// Gets or sets the user image id.
        /// </summary>
        [DataMember]
        public int UserImageId { get; set; }
        
        /// <summary>
        /// Gets or sets the user id.
        /// </summary>
        [DataMember]
        public int UserId { get; set; }

        /// <summary>
        /// Gets or sets the user.
        /// </summary>
        [ForeignKey("UserId")]
        public User User { get; set; }

        /// <summary>
        /// Gets or sets the profile image.
        /// </summary>
        [DataMember]
        public byte[] ProfileImage { get; set; }
    }
}
