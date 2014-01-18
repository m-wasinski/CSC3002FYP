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
        /// Gets or sets the range.
        /// </summary>
        [DataMember]
        public int Index { get; set; }

        /// <summary>
        /// Gets or sets the count.
        /// </summary>
        [DataMember]
        public int Count { get; set; }

        /// <summary>
        /// Gets or sets a value indicating whether poll more data.
        /// </summary>
        [DataMember]
        public bool LoadMoreData { get; set; }
    }
}