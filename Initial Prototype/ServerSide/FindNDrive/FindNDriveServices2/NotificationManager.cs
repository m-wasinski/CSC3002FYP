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
        private const string GCMPostData = "{{ \"registration_ids\": {0} , \"data\": {{\"tickerText\":\"{1}\", \"contentTitle\":\"{2}\", \"notificationType\": {3}, \"collapsibleKey\": {4}, \"payload\": {5} }}}}";

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

        private readonly SessionManager sessionManager;

        /// <summary>
        /// The invalid gcm registration id.
        /// </summary>
        private const string InvalidGcmRegistrationId = "0";

        /// <summary>
        /// Initializes a new instance of the <see cref="NotificationManager"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        ///     The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager"></param>
        public NotificationManager(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
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
        public void SendGcmNotification<T>(ICollection<User> users, string contentTitle, GcmNotificationType gcmNotificationType, T payload, int collapsibleKey = -1)
        {
            // Determine which users are online.
            var onlineUsers = users.Where(_ => this.sessionManager.IsStillLoggedIn(_)).ToList();
            var offlineUsers = users.Where(_ => !this.sessionManager.IsStillLoggedIn(_)).ToList();

            // Serialise the message using the provided type.
            var serialisedPayload = JsonConvert.SerializeObject(
                payload,
                typeof(T),
                Formatting.None,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            if (onlineUsers.Count > 0)
            {
                var gcmPostData = string.Format(
                    GCMPostData,
                    JsonConvert.SerializeObject(onlineUsers.Select(_ => _.GCMRegistrationID).ToList()),
                    string.Empty,
                    contentTitle,
                    JsonConvert.SerializeObject(gcmNotificationType),
                    collapsibleKey,
                    serialisedPayload);

                this.ForwardGCMNotification(gcmPostData);
            }

            if (offlineUsers.Count <= 0
                || (gcmNotificationType != GcmNotificationType.ChatMessage
                    && gcmNotificationType != GcmNotificationType.JourneyChatMessage))
            {
                return;
            }

            foreach (var offlineUser in offlineUsers)
            {
                this.findNDriveUnitOfWork.NotificationRepository.Add(new Notification
                                                                         {
                                                                             Context = NotificationContext.Neutral,
                                                                             CollapsibleKey = collapsibleKey,
                                                                             Delivered = false,
                                                                             NotificationType = NotificationType.Device,
                                                                             NotificationMessage = string.Empty,
                                                                             NotificationContentType = gcmNotificationType == GcmNotificationType.JourneyChatMessage ?
                                                                                                           NotificationContentType.JourneyChatMessage : NotificationContentType.InstantMessenger,
                                                                             UserId = offlineUser.UserId,
                                                                             ReceivedOnDate = DateTime.Now,
                                                                             NotificationTitle = contentTitle,
                                                                             NotificationPayload = serialisedPayload
                                                                         });
            }

            this.findNDriveUnitOfWork.Commit();
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
        public void SendAppNotification<T>(ICollection<User> users, string notificationTitle, string notificationMessage, NotificationContext context, NotificationType notificationType, NotificationContentType notificationContentType, T payload, int collapsibleKey = -1)
        {
            var serialisedPayload = string.Empty;

            if(payload.GetType() != typeof(String))
            {
                // Serialise the message using the provided type.
                serialisedPayload = JsonConvert.SerializeObject(
                    payload,
                    typeof(T),
                    Formatting.None,
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
                    CollapsibleKey = collapsibleKey,
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