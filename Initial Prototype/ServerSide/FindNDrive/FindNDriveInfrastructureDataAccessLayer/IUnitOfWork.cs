using System;

namespace FindNDriveInfrastructureDataAccessLayer
{
    public interface IUnitOfWork : IDisposable
    {
        void Commit();
    }
}
