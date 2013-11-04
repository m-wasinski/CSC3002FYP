using System.Data.Entity;
using System.Linq;

namespace InfrastructureDataAccessLayer
{
    public class EntityFrameworkRepository<T> : IRepository<T> where T : class
    {
        private readonly DbSet<T> _dbSet;

        public EntityFrameworkRepository(DbContext dbContext)
        {
            _dbSet = dbContext.Set<T>();
        }

        public void Add(T entity)
        {
            _dbSet.Add(entity);
        }

        public void Remove(T entity)
        {
            _dbSet.Remove(entity);
        }

        public T Find(int id)
        {
            return _dbSet.Find(id);
        }

        public IQueryable<T> AsQueryable()
        {
            return _dbSet;
        }
    }
}
