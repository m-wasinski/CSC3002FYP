﻿// --------------------------------------------------------------------------------------------------------------------
// <copyright file="NotificationService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the NotificationService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Services
{
    using System.Collections.Generic;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;
    using FindNDriveServices2.ServiceUtils;

    using Newtonsoft.Json.Linq;

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
        /// </summary>
        /// <param name="loadRangeDTO">
        /// The load range dto.
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
        /// </summary>
        /// <param name="userId">
        /// The user id.
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
        /// The get unread notifications count.
        /// </summary>
        /// <param name="userId">
        /// The user id.
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
        /// The get unread notifications count.
        /// </summary>
        /// <param name="notificationMarkerDTO">
        /// The notification Marker DTO.
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
