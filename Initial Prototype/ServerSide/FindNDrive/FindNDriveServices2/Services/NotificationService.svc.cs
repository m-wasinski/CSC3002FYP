// --------------------------------------------------------------------------------------------------------------------
// <copyright file="NotificationService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the NotificationService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

using System;

namespace FindNDriveServices2.Services
{
    using System.Collections.Generic;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The notification service.
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
        public NotificationService()
        {
        }

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
        /// The mark as read.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<bool> MarkAsRead(int id)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(false);
            }

            var notification =
                this.findNDriveUnitOfworkWork.NotificationRepository.Find(id);

            if (notification == null)
            {
                return ResponseBuilder.Failure<bool>("Invalid notification Id");
            }

            if (notification.Read)
            {
                return ResponseBuilder.Success(true);
            }

            notification.Read = true;
            this.findNDriveUnitOfworkWork.Commit();

            return ResponseBuilder.Success(true);
        }

        /// <summary>
        /// The retrieve notifications.
        /// </summary>
        /// <param name="loadRangeDTO">
        /// The load range dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<Notification>> RetrieveNotifications(LoadRangeDTO loadRangeDTO)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new List<Notification>());
            }

            var notifications = this.findNDriveUnitOfworkWork.NotificationRepository.AsQueryable().Where(_ => _.UserId == loadRangeDTO.Id).OrderByDescending(x => x.ReceivedOnDate).ToList();

            notifications = LoadRangeHelper<Notification>.GetValidRange(notifications, loadRangeDTO.Index, loadRangeDTO.Count, loadRangeDTO.LoadMoreData);

            return ResponseBuilder.Success(notifications);
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
        public ServiceResponse<int> GetUnreadNotificationsCount(int userId)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(0);
            }

            var count = this.findNDriveUnitOfworkWork.NotificationRepository.AsQueryable().Count(_ => _.UserId == userId && !_.Read);

            return ResponseBuilder.Success(count);
        }
    }
}
