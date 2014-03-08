// --------------------------------------------------------------------------------------------------------------------
// <copyright file="UserRetrieverDTO.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the UserRetrieverDTO type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The user retriever dto.
    /// </summary>
    [DataContract]
    public class UserRetrieverDTO
    {
        /// <summary>
        /// Gets or sets the retrieving user id.
        /// </summary>
        [DataMember]
        public int RetrievingUserId { get; set; }

        /// <summary>
        /// Gets or sets the target user id.
        /// </summary>
        [DataMember]
        public int TargetUserId { get; set; }
    }
}