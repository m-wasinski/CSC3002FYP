namespace FindNDriveServices2.DTOs
{
    using System;
    using System.Runtime.Serialization;

    /// <summary>
    /// The multi choice.
    /// </summary>
    [DataContract]
    [Serializable]
    public enum MultiChoice
    {
        /// <summary>
        /// The idont mind.
        /// </summary>
        [EnumMember(Value = "I don't mind")]
        IdontMind = 0,

        /// <summary>
        /// The no.
        /// </summary>
        [EnumMember(Value = "No")]
        No = 1
    }
}