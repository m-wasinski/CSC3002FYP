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
    using DomainObjects.DOmains;

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
        public static IQueryable<CarShare> IncludeAll(this IQueryable<CarShare> queryable)
        {
            return queryable.Include("Driver").Include("Participants").Include("Requests");
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
            return queryable.Include("TravelBuddies");
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
        public static IQueryable<CarShareRequest> IncludeAll(this IQueryable<CarShareRequest> queryable)
        {
            return queryable.Include("User").Include("CarShare");
        }
    }
}