// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ICarShareService.cs" company="">
//   
// </copyright>
// <summary>
//   The CarShareService interface.
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
    /// The CarShareService interface.
    /// </summary>
    [ServiceContract]
    public interface IJourneyService
    {
        /// <summary>
        /// The get all car share listings for user.
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
            UriTemplate = "/getall")]
        ServiceResponse<List<Journey>> GetAllJourneysForUser(LoadRangeDTO loadRangeDTO);

        /// <summary>
        /// The create new car share listing.
        /// </summary>
        /// <param name="journeyDTO">
        /// The car share dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/new")]
        ServiceResponse<Journey> CreateNewJourney(JourneyDTO journeyDTO);

        /// <summary>
        /// The get car share by id.
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
            UriTemplate = "/getsingle")]
        ServiceResponse<Journey> GetJourneyById(int id);

        /// <summary>
        /// The get car shares by id.
        /// </summary>
        /// <param name="ids">
        /// The ids.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/getmany")]
        ServiceResponse<List<Journey>> GetMultipleJourneysById(Collection<int> ids);

        /// <summary>
        /// The modify car share.
        /// </summary>
        /// <param name="journeyDTO">
        /// The car share dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/edit")]
        ServiceResponse<Journey> ModifyJourney(JourneyDTO journeyDTO);

        /// <summary>
        /// The withdraw from journey.
        /// </summary>
        /// <param name="journeyUserDTO">
        /// The journey passenger withdraw dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/withdraw")]
        ServiceResponse<bool> WithdrawFromJourney(JourneyUserDTO journeyUserDTO);

        /// <summary>
        /// The cancel journey.
        /// </summary>
        /// <param name="journeyUserDTO">
        /// The journey user dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/cancel")]
        ServiceResponse<bool> CancelJourney(JourneyUserDTO journeyUserDTO);
    }
}
