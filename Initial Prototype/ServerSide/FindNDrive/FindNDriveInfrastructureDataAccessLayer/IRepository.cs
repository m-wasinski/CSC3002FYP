// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IRepository.cs" company="">
//   
// </copyright>
// <summary>
//   The Repository interface.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

using System.Linq;

namespace FindNDriveInfrastructureDataAccessLayer
{
    using System.Collections;
    using System.Collections.Generic;

    /// <summary>
    /// The Repository interface.
    /// </summary>
    /// <typeparam name="T">
    /// </typeparam>
    public interface IRepository<T> where T : class
    {
        /// <summary>
        /// The add.
        /// </summary>
        /// <param name="entity">
        /// The entity.
        /// </param>
        void Add(T entity);

        /// <summary>
        /// The remove.
        /// </summary>
        /// <param name="entity">
        /// The entity.
        /// </param>
        void Remove(T entity);

        /// <summary>
        /// The remove range.
        /// </summary>
        /// <param name="entity">
        /// The entity.
        /// </param>
        void RemoveRange(IEnumerable<T> entity);

        /// <summary>
        /// The find.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="T"/>.
        /// </returns>
        T Find(int id);

        /// <summary>
        /// The as queryable.
        /// </summary>
        /// <returns>
        /// The <see cref="IQueryable"/>.
        /// </returns>
        IQueryable<T> AsQueryable();
    }
}
