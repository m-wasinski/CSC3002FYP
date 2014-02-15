// --------------------------------------------------------------------------------------------------------------------
// <copyright file="EntityFrameworkRepository.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the EntityFrameworkRepository type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveInfrastructureDataAccessLayer
{
    using System.Collections;
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;

    /// <summary>
    /// The entity framework repository.
    /// </summary>
    /// <typeparam name="T">
    /// </typeparam>
    public class EntityFrameworkRepository<T> : IRepository<T> where T : class
    {
        /// <summary>
        /// The _db set.
        /// </summary>
        private readonly DbSet<T> dbSet;

        /// <summary>
        /// Initializes a new instance of the <see cref="EntityFrameworkRepository{T}"/> class.
        /// </summary>
        /// <param name="dbContext">
        /// The db context.
        /// </param>
        public EntityFrameworkRepository(DbContext dbContext)
        {
            this.dbSet = dbContext.Set<T>();
        }

        /// <summary>
        /// The add.
        /// </summary>
        /// <param name="entity">
        /// The entity.
        /// </param>
        public void Add(T entity)
        {
            this.dbSet.Add(entity);
        }

        /// <summary>
        /// The remove.
        /// </summary>
        /// <param name="entity">
        /// The entity.
        /// </param>
        public void Remove(T entity)
        {
            this.dbSet.Remove(entity);
        }

        /// <summary>
        /// The remove range.
        /// </summary>
        /// <param name="entity">
        /// The entity.
        /// </param>
        public void RemoveRange(IEnumerable<T> entity)
        {
            this.dbSet.RemoveRange(entity);
        }

        /// <summary>
        /// The find.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="T"/>.
        /// </returns>
        public T Find(int id)
        {
            return this.dbSet.Find(id);
        }

        /// <summary>
        /// The as queryable.
        /// </summary>
        /// <returns>
        /// The <see cref="IQueryable"/>.
        /// </returns>
        public IQueryable<T> AsQueryable()
        {   
            return this.dbSet;
        }
    }
}
