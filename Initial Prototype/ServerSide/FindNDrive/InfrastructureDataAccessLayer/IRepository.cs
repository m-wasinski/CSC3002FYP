using System.Linq;

namespace InfrastructureDataAccessLayer
{
    public interface IRepository<T> where T : class
    {
        void Add(T entity);

        void Remove(T entity);

        T Find(int id);

        IQueryable<T> AsQueryable();
    }
}
