// --------------------------------------------------------------------------------------------------------------------
// <copyright file="RatingService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the RatingService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Services
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The rating service.
    /// </summary>
    [ServiceBehavior(
         InstanceContextMode = InstanceContextMode.PerCall,
         ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class RatingService : IRatingService
    {   
                /// <summary>
        /// The unit of work, which provides access to the required Repositories, and exposes
        /// a commit method to complete the unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager sessionManager;

        /// <summary>
        /// The notification manager.
        /// </summary>
        private readonly NotificationManager notificationManager;

        /// <summary>
        /// The invalid gcm registration id.
        /// </summary>
        private const string InvalidGCMRegistrationId = "0";

        /// <summary>
        /// Initializes a new instance of the <see cref="RatingService"/> class. 
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        /// <param name="notificationManager">
        /// The notification Manager.
        /// </param>
        public RatingService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.notificationManager = notificationManager;
        }

        /// <summary>
        /// The rate driver.
        /// </summary>
        /// <param name="ratingDTO">
        /// The rating dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public ServiceResponse<bool> RateDriver(RatingDTO ratingDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(false);
            }

            var driver =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == ratingDTO.UserId);

            if (driver == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid driver id.");
            }

            var leavingUser = this.findNDriveUnitOfWork.UserRepository.Find(ratingDTO.FromUserId);

            if (leavingUser == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid user id.");
            }

            var rating =
                this.findNDriveUnitOfWork.RatingsRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.FromUser.UserId == ratingDTO.FromUserId && _.UserId == ratingDTO.UserId);

            if (rating != null)
            {
                return ServiceResponseBuilder.Failure<bool>("You have already rated this driver.");
            }

            var ratings =
               this.findNDriveUnitOfWork.RatingsRepository.AsQueryable().Where(_ => _.UserId == ratingDTO.UserId).Select(_ => _.Score).ToList();

            ratings.Add(ratingDTO.Score);
            driver.AverageRating = ratings.Average();

            var newRating = new Rating
                                {
                                    UserId = ratingDTO.UserId,
                                    Score = ratingDTO.Score,
                                    Feedback = ratingDTO.Feedback,
                                    FromUser = leavingUser,
                                    LeftOnDate = DateTime.Now
                                };

            driver.Rating.Add(newRating);

            this.findNDriveUnitOfWork.Commit();

            this.notificationManager.SendAppNotification(
                new Collection<User> { driver },
                "New rating received",
                string.Format(
                    "You have received new rating from user {0} {1} ({2}).",
                    leavingUser.FirstName,
                    leavingUser.LastName,
                    leavingUser.UserName),
                leavingUser.ProfilePictureId,
                -1,
                NotificationType.Both,
                NotificationContentType.RatingReceived,
                newRating.RatingId);

            this.notificationManager.SendAppNotification(
                new Collection<User> { driver },
                "Rating left.",
                string.Format(
                    "You have left a rating for user {0} {1} ({2}).",
                    driver.FirstName,
                    driver.LastName,
                    driver.UserName),
                leavingUser.ProfilePictureId,
                -1,
                NotificationType.App,
                NotificationContentType.RatingLeft,
                newRating.RatingId);

            this.notificationManager.SendGcmTickle(new Collection<User> { driver });

            return ServiceResponseBuilder.Success(true);
        }

        public ServiceResponse<List<Rating>> GetUserRatings(int id)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new List<Rating>());
            }

            var ratings =
                this.findNDriveUnitOfWork.RatingsRepository.AsQueryable().IncludeAll().Where(_ => _.UserId == id).ToList();

            return ServiceResponseBuilder.Success(ratings);
        }

        public ServiceResponse<List<User>> GetLeaderboard()
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new List<User>());
            }

            var rating =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .OrderByDescending(_ => _.AverageRating).ToList();

            return ServiceResponseBuilder.Success(rating);
        }
    }
}