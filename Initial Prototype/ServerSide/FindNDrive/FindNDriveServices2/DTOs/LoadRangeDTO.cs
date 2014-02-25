// --------------------------------------------------------------------------------------------------------------------
// <copyright file="LoadRangeDTO.cs" company="">
//   
// </copyright>
// <summary>
//   The load range dto.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.DTOs
{
    using System.Runtime.Serialization;

    /// <summary>
    /// The load range dto.
    /// </summary>
    [DataContract]
    public class LoadRangeDTO
    {
        /// <summary>
        /// Gets or sets the id.
        /// </summary>
        [DataMember]
        public int Id { get; set; }

        /// <summary>
        /// Gets or sets the skip.
        /// </summary>
        [DataMember]
        public int Skip { get; set; }

        /// <summary>
        /// Gets or sets the count.
        /// </summary>
        [DataMember]
        public int Take { get; set; }
    }
}