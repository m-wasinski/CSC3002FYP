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
    using System.IO;
    using System.Linq;
    using System.Net;
    using System.Text;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

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
        private const string ApiKey = "AIzaSyAo1y7Zzp4GAskemJMlWwtYkdmY-_A2zm8";

        /// <summary>
        /// The sende r_ id.
        /// </summary>
        private const string SenderID = "505647745249";

        /// <summary>
        /// The gcm post data.
        /// </summary>
        private const string GCMPostData = "{{ \"registration_ids\": {0} , \"data\": {{\"tickerText\":\"{1}\", \"contentTitle\":\"{2}\", \"notificationType\": \"{3}\", \"message\": {4} }}}}";

        /// <summary>
        /// The content type.
        /// </summary>
        private const string ContentType = "application/json";

        /// <summary>
        /// The method.
        /// </summary>
        private const string Method = "POST";

        /// <summary>
        /// The gcm url.
        /// </summary>
        private const string GCMUrl = "https://android.googleapis.com/gcm/send";

        /// <summary>
        /// The send offline notification.
        /// </summary>
        /// <param name="registrationIds">
        /// The registration ids.
        /// </param>
        /// <param name="notificationType">
        /// The notification type.
        /// </param>
        /// <param name="contentTitle">
        /// The content title.
        /// </param>
        /// <param name="message">
        /// The message.
        /// </param>
        public void SendOfflineNotification(Collection<string> registrationIds, NotificationType notificationType, string contentTitle, string message)
        {
            var gcmPostData = string.Format(GCMPostData, JsonConvert.SerializeObject(registrationIds), string.Empty, contentTitle, JsonConvert.SerializeObject(notificationType), JsonConvert.SerializeObject(message));

            this.SendNotification(gcmPostData);
        }

        /// <summary>
        /// The send logout notification.
        /// </summary>
        /// <param name="registrationIds">
        /// The registration ids.
        /// </param>
        public void SendLogoutNotification(Collection<string> registrationIds)
        {
            var gcmPostData = string.Format(GCMPostData, JsonConvert.SerializeObject(registrationIds), string.Empty, "LOGOUT", JsonConvert.SerializeObject(NotificationType.Logout), JsonConvert.SerializeObject(string.Empty));

            this.SendNotification(gcmPostData);
        }

        /// <summary>
        /// The send chat message.
        /// </summary>
        /// <param name="registrationIds">
        /// The registration ids.
        /// </param>
        /// <param name="message">
        /// The message.
        /// </param>
        public void SendInstantMessageNotification(Collection<string> registrationIds, string message)
        {
            var filteredGcms = registrationIds.Where(_ => !_.Equals("0"));

            var gcmPostData = string.Format(GCMPostData, JsonConvert.SerializeObject(filteredGcms), string.Empty, "Message", JsonConvert.SerializeObject(NotificationType.InstantMessenger), message);

            this.SendNotification(gcmPostData);
        }

        /// <summary>
        /// The send journey request notification.
        /// </summary>
        /// <param name="registrationIds">
        /// The registration ids.
        /// </param>
        /// <param name="message">
        /// The message.
        /// </param>
        public void SendJourneyRequestNotification(Collection<string> registrationIds, string message)
        {
            var filteredGcms = registrationIds.Where(_ => !_.Equals("0"));

            var gcmPostData = string.Format(GCMPostData, JsonConvert.SerializeObject(filteredGcms), string.Empty, "Journey Request", JsonConvert.SerializeObject(NotificationType.JourneyRequestReceived), JsonConvert.SerializeObject(message));

            this.SendNotification(gcmPostData);
        }

        /// <summary>
        /// The send friend request.
        /// </summary>
        /// <param name="registrationIds">
        /// The registration ids.
        /// </param>
        /// <param name="friendRequest">
        /// The friend request.
        /// </param>
        public void SendFriendRequest(Collection<string> registrationIds, string friendRequest)
        {
            var filteredGcms = registrationIds.Where(_ => !_.Equals("0"));

            var gcmPostData = string.Format(GCMPostData, JsonConvert.SerializeObject(filteredGcms), string.Empty, "Friend Request", JsonConvert.SerializeObject(NotificationType.FriendRequest), friendRequest);

            this.SendNotification(gcmPostData);
        }

        /// <summary>
        /// The send notification.
        /// </summary>
        /// <param name="gcmPostData">
        /// The gcm post data.
        /// </param>
        private void SendNotification(string gcmPostData)
        {
            var gcmRequest = (HttpWebRequest)WebRequest.Create(GCMUrl);
            gcmRequest.KeepAlive = false;
            gcmRequest.Method = Method;
            gcmRequest.ContentType = ContentType;
            gcmRequest.Headers.Add(string.Format("Authorization: key={0}", ApiKey));
            gcmRequest.Headers.Add(string.Format("Sender: id={0}", SenderID));

            var byteArray = Encoding.UTF8.GetBytes(gcmPostData);
            gcmRequest.ContentLength = byteArray.Length;

            var dataStream = gcmRequest.GetRequestStream();
            dataStream.Write(byteArray, 0, byteArray.Length);
            dataStream.Close();

            var gcmResponse = gcmRequest.GetResponse();

            dataStream = gcmResponse.GetResponseStream();

            if (dataStream != null)
            {
                var reader = new StreamReader(dataStream);

                var response = reader.ReadToEnd();
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