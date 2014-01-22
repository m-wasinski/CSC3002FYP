// --------------------------------------------------------------------------------------------------------------------
// <copyright file="GCMManager.cs" company="">
//   
// </copyright>
// <summary>
//   The gcm manager.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2
{
    using System;
    using System.Collections.ObjectModel;
    using System.Collections.Specialized;
    using System.IO;
    using System.Net;
    using System.Text;
    using System.Web;

    using Newtonsoft.Json;

    /// <summary>
    /// The gcm manager.
    /// </summary>
    public class GCMManager
    {
        /// <summary>
        /// The ap i_ key.
        /// </summary>
        private const string API_KEY = "AIzaSyAo1y7Zzp4GAskemJMlWwtYkdmY-_A2zm8";

        /// <summary>
        /// The sende r_ id.
        /// </summary>
        private const string SENDER_ID = "505647745249";

        /// <summary>
        /// The send request.
        /// </summary>
        /// <param name="registrationIds">
        /// The registration ids.
        /// </param>
        /// <param name="notificationType">
        /// The notification Type.
        /// </param>
        /// <param name="notificationArguments">
        /// The notification Arguments.
        /// </param>
        /// <param name="contentTitle">
        /// The notification Title.
        /// </param>
        /// <param name="message">
        /// The message.
        /// </param>
        public void SendNotification(
            Collection<string> registrationIds,
            int notificationType,
            int notificationArguments,
            string contentTitle,
            string message)
        {
            var gcmRequest = (HttpWebRequest)WebRequest.Create("https://android.googleapis.com/gcm/send");
            gcmRequest.KeepAlive = false;
            gcmRequest.Method = "POST";

            gcmRequest.ContentType = "application/json";
            gcmRequest.Headers.Add(string.Format("Authorization: key={0}", API_KEY));
            gcmRequest.Headers.Add(string.Format("Sender: id={0}", SENDER_ID));

            var postData = "{ \"registration_ids\":" + JsonConvert.SerializeObject(registrationIds) + ", " +
             "\"data\": {\"tickerText\":\"" + string.Empty + "\", " +
             "\"contentTitle\":\"" + contentTitle + "\", " +
             "\"notificationType\":\"" + notificationType + "\", " +
             "\"notificationArguments\":\"" + notificationArguments + "\", " +
             "\"message\":" + message + "}}";

            // Write the string to a file.
            var file = new StreamWriter("c:\\CSC3002FYP\\gcm_post_data.txt");
            file.WriteLine(postData);
            file.Close();

            var byteArray = Encoding.UTF8.GetBytes(postData);
            gcmRequest.ContentLength = byteArray.Length;

            var dataStream = gcmRequest.GetRequestStream();
            dataStream.Write(byteArray, 0, byteArray.Length);
            dataStream.Close();

            var gcmResponse = gcmRequest.GetResponse();

            dataStream = gcmResponse.GetResponseStream();

            if (dataStream != null)
            {
                var reader = new StreamReader(dataStream);

                string response = reader.ReadToEnd();
                reader.Close();
            }

            if (dataStream != null)
            {
                dataStream.Close();
            }

            gcmResponse.Close();

            

        }
    }
}