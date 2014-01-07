// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IRequestService.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the IRequestService type.
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
    /// The RequestService interface.
    /// </summary>
    [ServiceContract]
    public interface IRequestService
    {
        /// <summary>
        /// The send request.
        /// </summary>
        /// <param name="carShareRequestDTO">
        /// The car share request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/sendrequest")]
        ServiceResponse<CarShareRequest> SendRequest(CarShareRequestDTO carShareRequestDTO);

        /// <summary>
        /// The make decision.
        /// </summary>
        /// <param name="carShareRequestDTO">
        /// The car share request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/processdecision")]
        ServiceResponse<CarShareRequest> ProcessDecision(CarShareRequestDTO carShareRequestDTO);

        /// <summary>
        /// The mark as read.
        /// </summary>
        /// <param name="carShareRequestDTO">
        /// The car share request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/markasread")]
        ServiceResponse<CarShareRequest> MarkAsRead(int id);

        /// <summary>
        /// The get all requests for car share.
        /// </summary>
        /// <param name="carShareRequestDTO">
        /// The car share request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/getrequests")]
        ServiceResponse<List<CarShareRequest>> GetAllRequestsForCarShare(int id);

        /// <summary>
        /// The get all requests for user.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/getrequestsforuser")]
        ServiceResponse<List<CarShareRequest>> GetAllRequestsForUser(int id);
    }
}