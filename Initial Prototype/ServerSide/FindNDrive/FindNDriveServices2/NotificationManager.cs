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

    using FindNDriveServices2.DTOs;

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
        /// The find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

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
        /// <param name="users">
        /// The users.
        /// </param>
        /// <param name="contentTitle">
        /// The content Title.
        /// </param>
        /// <param name="notificationType">
        /// The notification Type.
        /// </param>
        /// <param name="message">
        /// </param>
        public void SendNotification<T>(Collection<User> users, String contentTitle, NotificationType notificationType, T message)
        {
            // Determine which users are online.
            var onlineUsers = users.Where(_ => _.Status == Status.Online && !_.GCMRegistrationID.Equals("0")).ToList();

            // Serialise the message using the provided type.
            var serialisedMessage = JsonConvert.SerializeObject(
                message,
                typeof(T),
                Formatting.Indented,
                new JsonSerializerSettings { DateFormatHandling = DateFormatHandling.MicrosoftDateFormat });

            if (onlineUsers.Count > 0)
            {
                this.BuildGCMNotification(onlineUsers.Select(_ => _.GCMRegistrationID).ToList(), notificationType, contentTitle, serialisedMessage);
            }
            
            // Determine which users are offline.
            var offlineUsers = users.Where(_ => _.Status == Status.Offline || _.GCMRegistrationID.Equals("0")).ToList();

            // If none of the users are currently offline, no need to go any further.
            if (!offlineUsers.Any())
            {
                return;
            }

            foreach (var user in offlineUsers)
            {
                // User is offline, create an offline GCM notification to be received next time they log in.
                var gcmNotification = new GCMNotification
                                          {
                                              UserId = user.UserId,
                                              Delivered = false,
                                              NotificationMessage = serialisedMessage,
                                              ContentTitle = contentTitle,
                                              NotificationType = notificationType
                                          };

                this.findNDriveUnitOfWork.GCMNotificationsRepository.Add(gcmNotification);
            }

            this.findNDriveUnitOfWork.Commit();
        }

        /// <summary>
        /// The send offline gcm notification.
        /// </summary>
        /// <param name="user">
        /// The user.
        /// </param>
        public void SendOfflineGCMNotification(User user)
        {
            // Gather all unread offline GCM notifications.
            var gcmNotifications =
                this.findNDriveUnitOfWork.GCMNotificationsRepository.AsQueryable().Where(_ => _.UserId == user.UserId && !_.Delivered).ToList();

            // And forward them to the user.
            gcmNotifications.ForEach(
                delegate(GCMNotification gcmNotification)
                    {
                        gcmNotification.Delivered = true;
                        this.BuildGCMNotification(new List<string> {user.GCMRegistrationID}, gcmNotification.NotificationType, gcmNotification.ContentTitle, gcmNotification.NotificationMessage);
                    });

            this.findNDriveUnitOfWork.Commit();
        }

        /// <summary>
        /// The build gcm notification.
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
        /// <param name="type">
        /// The type.
        /// </param>
        private void BuildGCMNotification(List<string> registrationIds, NotificationType notificationType, string contentTitle, string message)
        {
            var gcmPostData = string.Format(
                GCMPostData,
                JsonConvert.SerializeObject(registrationIds),
                string.Empty,
                contentTitle,
                JsonConvert.SerializeObject(notificationType),
                message);
            
            this.SendGCMNotification(gcmPostData);
        }

        /// <summary>
        /// The send notification.
        /// </summary>
        /// <param name="gcmPostData">
        /// The gcm post data.
        /// </param>
        private void SendGCMNotification(string gcmPostData)
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