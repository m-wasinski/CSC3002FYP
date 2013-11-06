using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;
using Newtonsoft.Json.Converters;

namespace DomainObjects
{   
    [DataContract]
    [Serializable]
    public enum Gender
    { /// <summary>
        /// Represents a male person.
        /// </summary>
        /// 
        [EnumMember(Value = "Male")]
        Male = 1,

        /// <summary>
        /// Represents a female person.
        /// </summary>
        [EnumMember(Value = "Female")]
        Female = 2
    }
}
