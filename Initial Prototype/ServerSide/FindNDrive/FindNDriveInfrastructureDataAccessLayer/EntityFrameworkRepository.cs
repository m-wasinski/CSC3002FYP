using System;
using System.Data.Entity;
using System.Data.Entity.Core;
using System.Linq;
using System.Runtime.Remoting.Contexts;

namespace FindNDriveInfrastructureDataAccessLayer
{
    public class EntityFrameworkRepository<T> : IRepository<T> where T : class
    {
        private readonly DbSet<T> _dbSet;
        private readonly DbContext _dbContext;

        public EntityFrameworkRepository(DbContext dbContext)
        {
            _dbSet = dbContext.Set<T>();
            _dbContext = dbContext;
        }

        public void Add(T entity)
        {
            _dbSet.Add(entity);
        }

        public void Remove(T entity)
        {
            _dbSet.Remove(entity);
        }

        public void Update(T entity)
        {
            _dbContext.SaveChanges();
            //try
            //{
            //    var entry = _dbContext.Entry(entity);
            //    _dbContext.Set<T>().Attach(entity);
            //    entry.State = EntityState.Modified;
            //}
            //catch (OptimisticConcurrencyException ex)
            //{
            //    throw ex;
            //}
            
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
