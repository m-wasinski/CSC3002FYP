using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace FindNDriveServices2.DTOs
{
    using System.Runtime.Serialization;

    [DataContract]
    public class JourneyPassengerWithdrawDTO
    {
        [DataMember]
        public int UserId { get; set; }

        [DataMember]
        public int JourneyId { get; set; }
    }
}