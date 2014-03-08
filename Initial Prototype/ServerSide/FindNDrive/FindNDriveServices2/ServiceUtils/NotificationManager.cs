// --------------------------------------------------------------------------------------------------------------------
// <copyright file="NotificationManager.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the NotificationManager type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.ServiceUtils
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
        private const string GCMInstantMessage = "{{ \"registration_ids\": {0} , \"data\": {{\"tickerText\":\"{1}\", \"contentTitle\":\"{2}\", \"notificationType\": {3}, \"collapsibleKey\": {4}, \"payload\": {5}, \"pictureId\": {6} }}}}";

        /// <summary>
        /// The gcm tickle.
        /// </summary>
        private const string GCMTickle = "{{ \"registration_ids\": {0} , \"data\": {{\"tickerText\":\"{1}\", \"contentTitle\":\"{2}\", \"notificationType\": {3} }}}}";

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
        /// The session manager.
        /// </summary>
        private readonly SessionManager sessionManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="NotificationManager"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        public NotificationManager(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
        }

        /// <summary>
        /// The send gcm tickle.
        /// </summary>
        /// <param name="users">
        /// The users.
        /// </param>
        public void SendGcmTickle(ICollection<User> users)
        {
            // Determine which users are online.
            var onlineUsers = users.Where(_ => this.sessionManager.IsStillLoggedIn(_)).ToList();

            if (onlineUsers.Count <= 0)
            {
                return;
            }

            var gcmPostData = string.Format(
                GCMTickle,
                JsonConvert.SerializeObject(onlineUsers.Select(_ => _.GCMRegistrationID).ToList()),
                string.Empty,
                "tickle",
                JsonConvert.SerializeObject(GcmNotificationType.NotificationTickle));

            this.ForwardGCMNotification(gcmPostData);
        }

        /// <summary>
        /// The send instant message.
        /// </summary>
        /// <param name="users">
        /// The users.
        /// </param>
        /// <param name="gcmNotificationType">
        /// The gcm notification type.
        /// </param>
        /// <param name="pictureId">
        /// The picture id.
        /// </param>
        /// <param name="collapsibleKey">
        /// The collapsible key.
        /// </param>
        /// <param name="message">
        /// The message.
        /// </param>
        /// <param name="targetObjectId">
        /// The target object id.
        /// </param>
        /// <typeparam name="T">
        /// </typeparam>
        public void SendInstantMessage<T>(ICollection<User> users, GcmNotificationType gcmNotificationType, int pictureId, int collapsibleKey, T message, int targetObjectId)
        {
            // Determine which users are online.
            var onlineUsers = users.Where(_ => this.sessionManager.IsStillLoggedIn(_)).ToList();
            var offlineUsers = users.Where(_ => !this.sessionManager.IsStillLoggedIn(_)).ToList();

            if (onlineUsers.Count > 0)
            {
                // Serialise the message using the provided type.
                var serialisedMessage = JsonConvert.SerializeObject(
                    message,
                    typeof(T),
                    Formatting.None,
                    new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

                var gcmPostData = string.Format(
                    GCMInstantMessage,
                    JsonConvert.SerializeObject(onlineUsers.Select(_ => _.GCMRegistrationID).ToList()),
                    string.Empty,
                    "New message",
                    JsonConvert.SerializeObject(gcmNotificationType),
                    JsonConvert.SerializeObject(collapsibleKey),
                    serialisedMessage,
                    JsonConvert.SerializeObject(pictureId));

                this.ForwardGCMNotification(gcmPostData);
            }

            if (offlineUsers.Count <= 0)
            {
                return;
            }

            foreach (var offlineUser in offlineUsers)
            {
                this.findNDriveUnitOfWork.NotificationRepository.Add(new Notification
                {
                    ProfilePictureId = pictureId,
                    CollapsibleKey = collapsibleKey,
                    Delivered = false,
                    NotificationType = NotificationType.Device,
                    NotificationMessage = string.Empty,
                    NotificationContentType = gcmNotificationType == GcmNotificationType.JourneyChatMessage ?
                                                  NotificationContentType.JourneyChatMessage : NotificationContentType.InstantMessenger,
                    User = offlineUser,
                    ReceivedOnDate = DateTime.Now,
                    NotificationTitle = "New message",
                    TargetObjectId = targetObjectId
                });
            }

            this.findNDriveUnitOfWork.Commit();
        }

        /// <summary>
        /// The send app notification.
        /// </summary>
        /// <param name="users">
        /// The users.
        /// </param>
        /// <param name="notificationTitle">
        /// The notification title.
        /// </param>
        /// <param name="notificationMessage">
        /// The notification message.
        /// </param>
        /// <param name="profilePicture">
        /// The profile picture.
        /// </param>
        /// <param name="targetObjectId">
        /// The target object id.
        /// </param>
        /// <param name="notificationType">
        /// The notification type.
        /// </param>
        /// <param name="notificationContentType">
        /// The notification content type.
        /// </param>
        /// <param name="collapsibleKey">
        /// The collapsible key.
        /// </param>
        public void SendAppNotification(ICollection<User> users, string notificationTitle, string notificationMessage, int profilePicture, int targetObjectId, NotificationType notificationType, NotificationContentType notificationContentType, int collapsibleKey)
        {
            foreach (var user in users)
            {
                this.findNDriveUnitOfWork.NotificationRepository.Add(
                    new Notification
                        {
                            User = user,
                            NotificationTitle = notificationTitle,
                            NotificationMessage = notificationMessage,
                            CollapsibleKey = collapsibleKey,
                            NotificationContentType = notificationContentType,
                            Delivered = false,
                            ProfilePictureId = profilePicture,
                            ReceivedOnDate = DateTime.Now,
                            NotificationType = notificationType,
                            TargetObjectId = targetObjectId
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