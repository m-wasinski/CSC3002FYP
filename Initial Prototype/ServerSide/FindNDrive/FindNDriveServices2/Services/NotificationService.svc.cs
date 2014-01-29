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
        /// The _gcm manager.
        /// </summary>
        private readonly GCMManager gcmManager;

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
        /// <param name="gcmManager">
        /// The gcm Manager.
        /// </param>
        public NotificationService(FindNDriveUnitOfWork findNDriveUnitOfwork, SessionManager sessionManager, GCMManager gcmManager)
        {
            this.findNDriveUnitOfworkWork = findNDriveUnitOfwork;
            this.sessionManager = sessionManager;
            this.gcmManager = gcmManager;
        }

        public ServiceResponse<bool> MarkAsRead(int id)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(false);
            }

            var notification =
                this.findNDriveUnitOfworkWork.NotificationRepository.AsQueryable()
                    .FirstOrDefault(_ => _.NotificationId == id);
            if (notification != null)
            {
                if (!notification.Read)
                {
                    notification.Read = true;
                    this.findNDriveUnitOfworkWork.Commit();
                    return ResponseBuilder.Success(true);
                }
                
            }

            return ResponseBuilder.Failure<Boolean>("Invalid notification Id");
        }

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

        public ServiceResponse<int> GetUnreadNotificationsCount(int userId)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(0);
            }

            var count = this.findNDriveUnitOfworkWork.NotificationRepository.AsQueryable().Where(_ => _.UserId == userId && !_.Read).Count();

            return ResponseBuilder.Success(count);
        }
    }
}
