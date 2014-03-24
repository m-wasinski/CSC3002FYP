// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IJourneyService.cs" company="">
//   
// </copyright>
// <summary>
//   The CarShareService interface.
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
    /// The CarShareService interface.
    /// </summary>
    [ServiceContract]
    public interface IJourneyService
    {
        /// <summary>
        /// Retrieves list of journeys for the current user, both as driver and as passenger.
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
            UriTemplate = "/user")]
        ServiceResponse<List<Journey>> GetAllJourneysForUser(LoadRangeDTO loadRangeDTO);

        /// <summary>
        /// Creates a new journey.
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
            UriTemplate = "/create")]
        ServiceResponse<Journey> CreateNewJourney(JourneyDTO journeyDTO);

        /// <summary>
        /// Retrieves journey by id.
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
            UriTemplate = "/get")]
        ServiceResponse<Journey> GetJourneyById(int id);

        /// <summary>
        /// Provides the ability for the user to make changes to the journey.
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
        /// Withdraws a passenger from journey.
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
        ServiceResponse WithdrawFromJourney(JourneyUserDTO journeyUserDTO);

        /// <summary>
        /// Cancels a given journey.
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
        ServiceResponse<Journey> CancelJourney(JourneyUserDTO journeyUserDTO);

        /// <summary>
        /// Returns list of passengers for a given journey.
        /// </summary>
        /// <param name="journeyId">
        /// The journey id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/passengers")]
        ServiceResponse<List<User>> GetPassengers(int journeyId);
    }
}
