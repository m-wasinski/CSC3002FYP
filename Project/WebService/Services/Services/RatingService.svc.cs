// --------------------------------------------------------------------------------------------------------------------
// <copyright file="RatingService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the RatingService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.Services
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.Data.Entity;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DataAccessLayer;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using global::Services.Contracts;
    using global::Services.DTOs;
    using global::Services.ServiceResponses;
    using global::Services.ServiceUtils;

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
        /// Adds the new rating supplied by the user to the list of ratings of the driver.
        /// </summary>
        /// <param name="ratingDTO">
        /// Contains the basic information required to construct a new Rating object and save it in the database.
        /// This includes information about: user that supplied the rating, score in range 0-5, and a textual message.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public ServiceResponse RateDriver(RatingDTO ratingDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            var driver =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable().Include(_ => _.Ratings).FirstOrDefault(_ => _.UserId == ratingDTO.TargetUserId);

            if (driver == null)
            {
                return ServiceResponseBuilder.Failure("Invalid driver id.");
            }

            var leavingUser = this.findNDriveUnitOfWork.UserRepository.Find(ratingDTO.FromUserId);

            if (leavingUser == null)
            {
                return ServiceResponseBuilder.Failure("Invalid user id.");
            }

            var rating =
                this.findNDriveUnitOfWork.RatingsRepository.AsQueryable()
                    .IncludeChildren()
                    .FirstOrDefault(_ => _.FromUser.UserId == ratingDTO.FromUserId && _.UserId == ratingDTO.TargetUserId);

            if (rating != null)
            {
                return ServiceResponseBuilder.Failure("You have already rated this driver.");
            }

            var ratings =
               this.findNDriveUnitOfWork.RatingsRepository.AsQueryable().Where(_ => _.UserId == ratingDTO.TargetUserId).Select(_ => _.Score).ToList();

            ratings.Add(ratingDTO.Score);
            driver.AverageRating = ratings.Average();

            var newRating = new Rating
                                {
                                    UserId = ratingDTO.TargetUserId,
                                    Score = ratingDTO.Score,
                                    Feedback = ratingDTO.Feedback,
                                    FromUser = leavingUser,
                                    LeftOnDate = DateTime.Now
                                };

            driver.Ratings.Add(newRating);

            this.findNDriveUnitOfWork.Commit();

            this.notificationManager.CreateAppNotification(
                new Collection<User> { driver },
                "New rating received",
                string.Format(
                    "You have received new rating from user {0} {1} ({2}).",
                    leavingUser.FirstName,
                    leavingUser.LastName,
                    leavingUser.UserName),
                leavingUser.UserId,
                -1,
                NotificationType.Both,
                NotificationContentType.RatingReceived,
                newRating.RatingId);

            this.notificationManager.CreateAppNotification(
                new Collection<User> { leavingUser },
                "Rating left.",
                string.Format(
                    "You have left a rating for user {0} {1} ({2}).",
                    driver.FirstName,
                    driver.LastName,
                    driver.UserName),
                leavingUser.UserId,
                -1,
                NotificationType.App,
                NotificationContentType.RatingLeft,
                newRating.RatingId);

            this.notificationManager.SendGcmTickle(new Collection<User> { driver });

            return ServiceResponseBuilder.Success();
        }

        /// <summary>
        /// Retrievesa a list of ratings for a given user.
        /// </summary>
        /// <param name="id">
        /// Unique identifier of the user whose ratings are to be retrieved.
        /// </param>
        /// <returns></returns>
        public ServiceResponse<List<Rating>> GetUserRatings(int id)
        {
            var ratings =
                (from rating in
                     this.findNDriveUnitOfWork.RatingsRepository.AsQueryable()
                     .Include(_ => _.FromUser)
                     .Where(_ => _.UserId == id)
                     .ToList()
                 select
                     new Rating
                         {
                             UserId = rating.UserId,
                             Feedback = rating.Feedback,
                             FromUser =
                                 new User
                                     {
                                         UserId = rating.FromUser.UserId,
                                         FirstName = rating.FromUser.FirstName,
                                         LastName = rating.FromUser.LastName,
                                         UserName = rating.FromUser.UserName
                                     },
                             LeftOnDate = rating.LeftOnDate,
                             Score = rating.Score,
                             RatingId = rating.RatingId
                         }).ToList();

            return ServiceResponseBuilder.Success(ratings);
        }

        /// <summary>
        /// Retrieves the global leader board of the best drivers sorting the list in 
        /// descending order and putting drivers with highest average score on top.
        /// </summary>
        /// <param name="loadRangeDTO">
        /// Required for incremental loading of information. 
        /// Contains information about the indexes and number of elements to be loaded from the list of all drivers.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<User>> GetLeaderboard(LoadRangeDTO loadRangeDTO)
        {
            var users = (from user in this.findNDriveUnitOfWork.UserRepository.AsQueryable().Include(_ => _.Ratings)
                                           .OrderByDescending(_ => _.AverageRating)
                                           .Skip(loadRangeDTO.Skip)
                                           .Take(loadRangeDTO.Take).ToList()
                         select
                             new User
                                 {
                                     UserId = user.UserId,
                                     FirstName = user.FirstName,
                                     LastName = user.LastName,
                                     UserName = user.UserName,
                                     AverageRating = user.AverageRating,
                                     VotesCount = user.Ratings.Count
                                 }).ToList();

            return ServiceResponseBuilder.Success(users);
        }
    }
}