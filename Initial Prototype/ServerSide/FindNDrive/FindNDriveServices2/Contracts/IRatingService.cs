// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IRatingService.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the IRatingService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Contracts
{
    using System.Collections.Generic;
    using System.ServiceModel;
    using System.ServiceModel.Web;
    using DomainObjects.Domains;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The RatingService interface.
    /// </summary>
    [ServiceContract]
    public interface IRatingService
    {
        /// <summary>
        /// Provides user with the ability to rate driver of a journey they participated in.
        /// </summary>
        /// <param name="ratingDTO">
        /// The rating dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/rate")]
        ServiceResponse RateDriver(RatingDTO ratingDTO);

        /// <summary>
        /// Retrieves all ratings for a given user.
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
            UriTemplate = "/user")]
        ServiceResponse<List<Rating>> GetUserRatings(int id);

        /// <summary>
        /// Retrievers the system-wide leaderboard.
        /// </summary>
        /// <param name="loadRangeDTO">
        /// The load Range DTO.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/leaderboard")]
        ServiceResponse<List<User>> GetLeaderboard(LoadRangeDTO loadRangeDTO);
    }
}