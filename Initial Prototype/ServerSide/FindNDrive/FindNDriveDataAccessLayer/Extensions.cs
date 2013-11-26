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
        public static IQueryable<CarShare> IncludeAll(this IQueryable<CarShare> queryable)
        {
            return queryable
                .Include("Driver")
                .Include("Participants");
        }
    }
}