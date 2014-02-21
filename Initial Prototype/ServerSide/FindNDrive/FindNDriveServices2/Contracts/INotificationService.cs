// --------------------------------------------------------------------------------------------------------------------
// <copyright file="INotificationService.cs" company="">
//   
// </copyright>
// <summary>
//   The NotificationService interface.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Contracts
{
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.ServiceModel;
    using System.ServiceModel.Web;

    using DomainObjects.Domains;

    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The NotificationService interface.
    /// </summary>
    [ServiceContract]
    public interface INotificationService
    {
        /// <summary>
        /// The get in app notifications.
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
            UriTemplate = "/getappnotifications")]
        ServiceResponse<List<Notification>> GetAppNotifications(LoadRangeDTO loadRangeDTO);

        /// <summary>
        /// The get device notifications.
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
            UriTemplate = "/getdevicenotifications")]
        ServiceResponse<List<Notification>> GetDeviceNotifications(int userId);

        /// <summary>
        /// The get unread in app notifications count.
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
            UriTemplate = "/getcount")]
        ServiceResponse<int> GetUnreadAppNotificationsCount(int userId);

        /// <summary>
        /// The mark as delivered.
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
            UriTemplate = "/markdelivered")]
        ServiceResponse<bool> MarkAsDelivered(NotificationMarkerDTO notificationMarkerDTO);
    }
}