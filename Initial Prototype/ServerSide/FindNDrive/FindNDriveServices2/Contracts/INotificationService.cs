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
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/markasread")]
        ServiceResponse<bool> MarkAsRead(int id);

        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/getall")]
        ServiceResponse<List<Notification>> RetrieveNotifications(LoadRangeDTO loadRangeDTO);

        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/getunreadcount")]
        ServiceResponse<int> GetUnreadNotificationsCount(int userId);
    }
}