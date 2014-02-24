// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Extensions.cs" company="">
//   
// </copyright>
// <summary>
//   The extensions.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveDataAccessLayer
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
                    .Include("Participants")
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
        public static IQueryable<User> IncludeAll(this IQueryable<User> queryable)
        {
            return queryable.Include("Friends").Include("Rating").Include("ProfilePicture");
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
            return queryable.Include("User").Include("Journey");
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
    }
}