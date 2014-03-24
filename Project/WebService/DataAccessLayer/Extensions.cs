// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Extensions.cs" company="">
//   
// </copyright>
// <summary>
//   The extensions.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace DataAccessLayer
{
    using System.Data.Entity;
    using System.Linq;

    using DomainObjects.Domains;

    /// <summary>
    /// The extensions.
    /// </summary>
    public static class Extensions
    {
        /// <summary>
        /// The include all.
        /// </summary>
        /// <param name="queryable">
        /// The queryable.
        /// </param>
        /// <returns>
        /// The <see cref="IQueryable"/>.
        /// </returns>
        public static IQueryable<Journey> IncludeAll(this IQueryable<Journey> queryable)
        {
            return
                queryable.Include("Driver")
                    .Include("Passengers")
                    .Include("Requests")
                    .Include("GeoAddresses");
        }

        /// <summary>
        /// The include all.
        /// </summary>
        /// <param name="queryable">
        /// The queryable.
        /// </param>
        /// <returns>
        /// The <see cref="IQueryable"/>.
        /// </returns>
        public static IQueryable<Journey> IncludeSearch(this IQueryable<Journey> queryable)
        {
            return
                queryable.Include("Driver")
                    .Include("Passengers")
                    .Include("GeoAddresses");
        }


        /// <summary>
        /// The include all.
        /// </summary>
        /// <param name="queryable">
        /// The queryable.
        /// </param>
        /// <returns>
        /// The <see cref="IQueryable"/>.
        /// </returns>
        public static IQueryable<User> IncludeAll(this IQueryable<User> queryable)
        {
            return queryable.Include("Friends").Include("Rating").Include("ProfilePicture").Include("JourneyTemplates").Include("PrivacySettings");
        }

        /// <summary>
        /// The include friends.
        /// </summary>
        /// <param name="queryable">
        /// The queryable.
        /// </param>
        /// <returns>
        /// The <see cref="IQueryable"/>.
        /// </returns>
        public static IQueryable<User> IncludeFriends(this IQueryable<User> queryable)
        {
            return queryable.Include("Friends");
        }

        /// <summary>
        /// The include all.
        /// </summary>
        /// <param name="queryable">
        /// The queryable.
        /// </param>
        /// <returns>
        /// The <see cref="IQueryable"/>.
        /// </returns>
        public static IQueryable<JourneyRequest> IncludeAll(this IQueryable<JourneyRequest> queryable)
        {
            return queryable.Include("FromUser").Include("Journey");
        }

        /// <summary>
        /// The include all.
        /// </summary>
        /// <param name="queryable">
        /// The queryable.
        /// </param>
        /// <returns>
        /// The <see cref="IQueryable"/>.
        /// </returns>
        public static IQueryable<JourneyMessage> IncludeAll(this IQueryable<JourneyMessage> queryable)
        {
            return queryable.Include("SeenBy");
        }

        /// <summary>
        /// The include all.
        /// </summary>
        /// <param name="queryable">
        /// The queryable.
        /// </param>
        /// <returns>
        /// The <see cref="IQueryable"/>.
        /// </returns>
        public static IQueryable<Rating> IncludeAll(this IQueryable<Rating> queryable)
        {
            return queryable.Include("FromUser");
        }

        public static IQueryable<Notification> IncludeAll(this IQueryable<Notification> queryable)
        {
            return queryable.Include("ProfilePicture");
        }

        public static IQueryable<FriendRequest> IncludeAll(this IQueryable<FriendRequest> queryable)
        {
            return queryable.Include("FromUser").Include("ToUser");
        }

        public static IQueryable<JourneyTemplate> IncludeAll(this IQueryable<JourneyTemplate> queryable)
        {
            return queryable.Include("User").Include("GeoAddresses");
        }
    }
}