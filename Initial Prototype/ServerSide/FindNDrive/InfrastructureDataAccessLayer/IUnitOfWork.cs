using System;
using System.Data.Entity;
using System.Linq;

namespace InfrastructureDataAccessLayer
{
    public interface IUnitOfWork : IDisposable
    {
        void Commit();
    }
}
