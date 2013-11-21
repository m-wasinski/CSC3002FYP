using System.Linq;

namespace FindNDriveInfrastructureDataAccessLayer
{
    public interface IRepository<T> where T : class
    {
        void Add(T entity);

        void Remove(T entity);

        void Update(T entity); 

        T Find(int id);

        IQueryable<T> AsQueryable();
    }
}
