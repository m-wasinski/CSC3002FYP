// --------------------------------------------------------------------------------------------------------------------
// <copyright file="NotificationManager.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the NotificationManager type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2
{
    using System;
    using System.Collections.Generic;
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
    /// The notification manager.
    /// </summary>
    public class NotificationManager
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
        private const string GCMPostData = "{{ \"registration_ids\": {0} , \"data\": {{\"tickerText\":\"{1}\", \"contentTitle\":\"{2}\", \"notificationType\": {3}, \"payload\": {4} }}}}";

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
        /// The find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        /// <summary>
        /// The invalid gcm registration id.
        /// </summary>
        private const string InvalidGcmRegistrationId = "0";

        /// <summary>
        /// Initializes a new instance of the <see cref="NotificationManager"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        public NotificationManager(FindNDriveUnitOfWork findNDriveUnitOfWork)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
        }

        /// <summary>
        /// Send appropriate type of notification based on current status of the user.
        /// </summary>
        /// <typeparam name="T">
        /// </typeparam>
        /// <param name="users">
        /// The users.
        /// </param>
        /// <param name="contentTitle">
        /// The content Title.
        /// </param>
        /// <param name="gcmNotificationType">
        /// The gcm Notification Type.
        /// </param>
        /// <param name="payload">
        /// The payload.
        /// </param>
        public void SendGcmNotification<T>(ICollection<User> users, string contentTitle, GcmNotificationType gcmNotificationType, T payload)
        {
            // Determine which users are online.
            var onlineUsers = users.Where(_ => _.Status == Status.Online && !_.GCMRegistrationID.Equals(InvalidGcmRegistrationId)).ToList();

            // Serialise the message using the provided type.
            var serialisedPayload = JsonConvert.SerializeObject(
                payload,
                typeof(T),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            if (onlineUsers.Count == 0)
            {
                return;
            }

            var gcmPostData = string.Format(
                    GCMPostData,
                    JsonConvert.SerializeObject(onlineUsers.Select(_ => _.GCMRegistrationID).ToList()),
                    string.Empty,
                    contentTitle,
                    JsonConvert.SerializeObject(gcmNotificationType),
                    serialisedPayload);

            this.ForwardGCMNotification(gcmPostData);
        }

        /// <summary>
        /// The send app notification.
        /// </summary>
        /// <param name="userId">
        /// The user id.
        /// </param>
        /// <param name="notificationTitle">
        /// The notification title.
        /// </param>
        /// <param name="notificationMessage">
        /// The notification message.
        /// </param>
        /// <param name="context">
        /// The context.
        /// </param>
        /// <param name="notificationType">
        /// The notification type.
        /// </param>
        /// <param name="notificationContentType">
        /// The notification content type.
        /// </param>
        /// <param name="payload">
        /// The payload.
        /// </param>
        /// <typeparam name="T">
        /// </typeparam>
        public void SendAppNotification<T>(ICollection<User> users, string notificationTitle, string notificationMessage, NotificationContext context, NotificationType notificationType, NotificationContentType notificationContentType, T payload)
        {
            var serialisedPayload = string.Empty;

            if(payload.GetType() != typeof(String))
            {
                // Serialise the message using the provided type.
                serialisedPayload = JsonConvert.SerializeObject(
                    payload,
                    typeof(T),
                    Formatting.Indented,
                    new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });
            }

            foreach (var user in users)
            {
                this.findNDriveUnitOfWork.NotificationRepository.Add(
                new Notification
                {
                    UserId = user.UserId,
                    NotificationTitle = notificationTitle,
                    NotificationMessage = notificationMessage,
                    NotificationContentType = notificationContentType,
                    Delivered = false,
                    Context = context,
                    ReceivedOnDate = DateTime.Now,
                    NotificationType = notificationType,
                    NotificationPayload = serialisedPayload
                });
            }

            this.findNDriveUnitOfWork.Commit();
        }

        /// <summary>
        /// The send notification.
        /// </summary>
        /// <param name="gcmPostData">
        /// The gcm post data.
        /// </param>
        private void ForwardGCMNotification(string gcmPostData)
        {
            var gcmRequest = (HttpWebRequest)WebRequest.Create(GCMUrl);
            gcmRequest.KeepAlive = true;
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