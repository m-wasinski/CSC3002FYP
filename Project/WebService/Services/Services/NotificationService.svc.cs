// --------------------------------------------------------------------------------------------------------------------
// <copyright file="NotificationService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the NotificationService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.Services
{
    using System.Collections.Generic;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DataAccessLayer;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using global::Services.Contracts;
    using global::Services.DTOs;
    using global::Services.ServiceResponses;
    using global::Services.ServiceUtils;

    /// <summary>
    /// Responsible for the retrieval and creation of notifications.
    /// </summary>
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.PerCall, ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class NotificationService : INotificationService
    {
        /// <summary>
        /// The _find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork findNDriveUnitOfworkWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager sessionManager;

        /// <summary>
        /// The notification manager.
        /// </summary>
        private readonly NotificationManager notificationManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="NotificationService"/> class.  
        /// </summary>
        /// <param name="findNDriveUnitOfwork">
        /// The find n drive unit of.
        /// </param>
        /// <param name="sessionManager">
        /// The session Manager.
        /// </param>
        /// <param name="notificationManager">
        /// The notification Manager.
        /// </param>
        public NotificationService(FindNDriveUnitOfWork findNDriveUnitOfwork, SessionManager sessionManager, NotificationManager notificationManager)
        {
            this.findNDriveUnitOfworkWork = findNDriveUnitOfwork;
            this.sessionManager = sessionManager;
            this.notificationManager = notificationManager;
        }

        /// <summary>
        /// Retrieves the list of in-app notifications for a specific user.
        /// App notification is a notification which is accessed through the Notifications menu item in the app.
        /// It contains a text body as well as an optional action associated with it. For example, viewing a journey that a friend has offered.
        /// If a notification has an action associated with it, the action will contain the id of the object to be retrieved from the database 
        /// when the notification is clicked, for example the above mentioned journey.
        /// </summary>
        /// <param name="loadRangeDTO">
        /// LoadRangeDTO is used in incremental loading of information to determine which items should be retrieved from the database.
        /// It ontains the unique identifier of the user requesting notifications as well as the number and position of items to be retrieved from the database.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<Notification>> GetAppNotifications(LoadRangeDTO loadRangeDTO)
        {
            var notifications =
                this.findNDriveUnitOfworkWork.NotificationRepository.AsQueryable()
                    .Where(
                        _ =>
                        _.UserId == loadRangeDTO.Id
                        && (_.NotificationType == NotificationType.App || _.NotificationType == NotificationType.Both))
                    .OrderByDescending(x => x.ReceivedOnDate)
                    .Skip(loadRangeDTO.Skip)
                    .Take(loadRangeDTO.Take);

            return ServiceResponseBuilder.Success(notifications.ToList());
        }

        /// <summary>
        /// Retrieves the list of device notifications for a specific user.
        /// Device Notification is a notification which appears in the top left corner of users' device.
        /// </summary>
        /// <param name="userId">
        /// Unique identifier of the user whose device notifications are to be retrieved.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<Notification>> GetDeviceNotifications(int userId)
        {
            var notifications = this.findNDriveUnitOfworkWork.NotificationRepository.AsQueryable().Where(_ => _.UserId == userId &&
                !_.Delivered && (_.NotificationType == NotificationType.Device || _.NotificationType == NotificationType.Both)).ToList();

            return ServiceResponseBuilder.Success(notifications);
        }

        /// <summary>
        /// Retrieves the number of user's unread app notifications.
        /// </summary>
        /// <param name="userId">
        /// Unique identifier of the user whose unread app notifications count should be retrieved.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<int> GetUnreadAppNotificationsCount(int userId)
        {
            var count = this.findNDriveUnitOfworkWork.NotificationRepository.AsQueryable().Count(_ => _.UserId == userId && !_.Delivered && (_.NotificationType == NotificationType.App || _.NotificationType == NotificationType.Both));

            return ServiceResponseBuilder.Success(count);
        }

        /// <summary>
        /// Marks a given notification as delivered. This means that this particular notification
        /// will not appear as new to the user anymore.
        /// </summary>
        /// <param name="notificationMarkerDTO">
        /// Contains the unique identifiers of the user who owns the notification as well as the
        /// unique identifier of the notification itself.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse MarkAsDelivered(NotificationMarkerDTO notificationMarkerDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            var notification = this.findNDriveUnitOfworkWork.NotificationRepository.Find(notificationMarkerDTO.NotificationId);

            if (notification == null)
            {
                return ServiceResponseBuilder.Failure("Invalid notification id.");
            }

            if (notification.Delivered)
            {
                return ServiceResponseBuilder.Success();
            }

            notification.Delivered = true;
            this.findNDriveUnitOfworkWork.Commit();

            return ServiceResponseBuilder.Success();
        }
    }
}
