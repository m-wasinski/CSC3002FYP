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
    using System.Collections.ObjectModel;
    using System.IO;
    using System.Linq;
    using System.Net;
    using System.Text;

    using DomainObjects.Constants;

    using FindNDriveDataAccessLayer;

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
        /// <param name="contentTitle">
        /// The notification Title.
        /// </param>
        /// <param name="message">
        /// The message.
        /// </param>
        public void SendNotification(
            Collection<string> registrationIds,
            GCMNotificationType notificationType,
            string contentTitle,
            string message)
        {
            var gcmRequest = (HttpWebRequest)WebRequest.Create("https://android.googleapis.com/gcm/send");
            gcmRequest.KeepAlive = false;
            gcmRequest.Method = "POST";

            gcmRequest.ContentType = "application/json";
            gcmRequest.Headers.Add(string.Format("Authorization: key={0}", API_KEY));
            gcmRequest.Headers.Add(string.Format("Sender: id={0}", SENDER_ID));

            var filteredGcms = registrationIds.Where(_ => !_.Equals("0"));

            var postData = "{ \"registration_ids\":" + JsonConvert.SerializeObject(filteredGcms) + ", " +
             "\"data\": {\"tickerText\":\"" + string.Empty + "\", " +
             "\"contentTitle\":\"" + contentTitle + "\", " +
             "\"notificationType\":\"" + JsonConvert.SerializeObject(notificationType) + "\", " +
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