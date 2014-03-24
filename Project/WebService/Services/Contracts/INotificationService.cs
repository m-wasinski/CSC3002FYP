// --------------------------------------------------------------------------------------------------------------------
// <copyright file="INotificationService.cs" company="">
//   
// </copyright>
// <summary>
//   The NotificationService interface.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.Contracts
{
    using System.Collections.Generic;
    using System.ServiceModel;
    using System.ServiceModel.Web;

    using DomainObjects.Domains;

    using global::Services.DTOs;
    using global::Services.ServiceResponses;

    /// <summary>
    /// The NotificationService interface.
    /// </summary>
    [ServiceContract]
    public interface INotificationService
    {
        /// <summary>
        /// Retrieves all in-app notifications for a given user.
        /// </summary>
        /// <param name="loadRangeDTO">
        /// The load range dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/app")]
        ServiceResponse<List<Notification>> GetAppNotifications(LoadRangeDTO loadRangeDTO);

        /// <summary>
        /// Retrieves all device-notifications for a given user.
        /// </summary>
        /// <param name="userId">
        /// The user id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/device")]
        ServiceResponse<List<Notification>> GetDeviceNotifications(int userId);

        /// <summary>
        /// Retrieves the count of all unread in-app notifications.
        /// </summary>
        /// <param name="userId">
        /// The user id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/count")]
        ServiceResponse<int> GetUnreadAppNotificationsCount(int userId);

        /// <summary>
        /// Marks a given notification as delivered.
        /// </summary>
        /// <param name="notificationMarkerDTO">
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/delivered")]
        ServiceResponse MarkAsDelivered(NotificationMarkerDTO notificationMarkerDTO);
    }
}