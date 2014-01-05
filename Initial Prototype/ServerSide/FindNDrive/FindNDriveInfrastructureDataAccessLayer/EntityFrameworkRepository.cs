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
        private readonly DbSet<T> _dbSet;

        /// <summary>
        /// The _db context.
        /// </summary>
        private readonly DbContext _dbContext;

        /// <summary>
        /// Initializes a new instance of the <see cref="EntityFrameworkRepository{T}"/> class.
        /// </summary>
        /// <param name="dbContext">
        /// The db context.
        /// </param>
        public EntityFrameworkRepository(DbContext dbContext)
        {
            _dbSet = dbContext.Set<T>();
            _dbContext = dbContext;
        }

        /// <summary>
        /// The add.
        /// </summary>
        /// <param name="entity">
        /// The entity.
        /// </param>
        public void Add(T entity)
        {
            _dbSet.Add(entity);
        }

        /// <summary>
        /// The remove.
        /// </summary>
        /// <param name="entity">
        /// The entity.
        /// </param>
        public void Remove(T entity)
        {
            _dbSet.Remove(entity);
        }

        /// <summary>
        /// The update.
        /// </summary>
        /// <param name="entity">
        /// The entity.
        /// </param>
        public void Update(T entity)
        {
            _dbContext.SaveChanges();

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
            return _dbSet.Find(id);
        }

        /// <summary>
        /// The as queryable.
        /// </summary>
        /// <returns>
        /// The <see cref="IQueryable"/>.
        /// </returns>
        public IQueryable<T> AsQueryable()
        {   
            return _dbSet;
        }
    }
}
