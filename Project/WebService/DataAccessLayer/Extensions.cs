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
        public static IQueryable<Journey> IncludeChildren(this IQueryable<Journey> queryable)
        {
            return
                queryable
                .Include(_ => _.Driver)
                    .Include(_ => _.Passengers)
                    .Include(_ => _.Requests)
                    .Include(_ => _.GeoAddresses);
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
        public static IQueryable<User> IncludeChildren(this IQueryable<User> queryable)
        {
            return queryable
                .Include(_ => _.Friends)
                .Include(_ => _.Ratings)
                .Include(_ => _.ProfilePicture)
                .Include(_ => _.JourneyTemplates)
                .Include(_ => _.PrivacySettings);
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
        public static IQueryable<JourneyRequest> IncludeChildren(this IQueryable<JourneyRequest> queryable)
        {
            return queryable
                .Include(_ => _.FromUser)
                .Include(_ => _.Journey);
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
        public static IQueryable<JourneyMessage> IncludeChildren(this IQueryable<JourneyMessage> queryable)
        {
            return queryable
                .Include(_ => _.SeenBy);
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
        public static IQueryable<Rating> IncludeChildren(this IQueryable<Rating> queryable)
        {
            return queryable
                .Include(_ => _.FromUser);
        }

        public static IQueryable<FriendRequest> IncludeChildren(this IQueryable<FriendRequest> queryable)
        {
            return queryable
                .Include(_ => _.FromUser)
                .Include(_ => _.ToUser);
        }

        public static IQueryable<JourneyTemplate> IncludeChildren(this IQueryable<JourneyTemplate> queryable)
        {
            return queryable
                .Include(_ => _.User)
                .Include(_ => _.GeoAddresses);
        }
    }
}