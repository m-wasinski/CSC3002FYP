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
        /// The rate driver.
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
        ServiceResponse<bool> RateDriver(RatingDTO ratingDTO);

        /// <summary>
        /// The get ratings.
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
            UriTemplate = "/getuserratings")]
        ServiceResponse<List<Rating>> GetUserRatings(int id);

        /// <summary>
        /// The get leaderboard.
        /// </summary>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/getleaderboard")]
        ServiceResponse<List<User>> GetLeaderboard(LoadRangeDTO loadRangeDTO);
    }
}