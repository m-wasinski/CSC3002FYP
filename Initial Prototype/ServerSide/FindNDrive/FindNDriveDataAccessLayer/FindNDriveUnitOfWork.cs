// --------------------------------------------------------------------------------------------------------------------
// <copyright file="FindNDriveUnitOfWork.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the FindNDriveUnitOfWork type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveDataAccessLayer
{
    using System.Data.Entity;

    using DomainObjects.Domains;
    using DomainObjects.DOmains;

    using FindNDriveInfrastructureDataAccessLayer;

    /// <summary>
    /// The find n drive unit of work.
    /// </summary>
    public class FindNDriveUnitOfWork : IUnitOfWork
    {
        /// <summary>
        /// The _db context.
        /// </summary>
        private readonly DbContext _dbContext;

        /**
        * Each IRepository<T> repesents a repository available in the database. 
        **/

        /// <summary>
        /// Gets or sets the user repository.
        /// </summary>
        public IRepository<User> UserRepository { get; set; }

        /// <summary>
        /// Gets or sets the car share repository.
        /// </summary>
        public IRepository<CarShare> CarShareRepository { get; set; }

        /// <summary>
        /// Gets or sets the session repository.
        /// </summary>
        public IRepository<Session> SessionRepository { get; set; }

        /// <summary>
        /// Gets or sets the car share request repository.
        /// </summary>
        public IRepository<CarShareRequest> CarShareRequestRepository { get; set; } 

        /// <summary>
        /// Initializes a new instance of the <see cref="FindNDriveUnitOfWork"/> class.
        /// </summary>
        /// <param name="dbContext">
        /// The db context.
        /// </param>
        /// <param name="userRepository">
        /// The user repository.
        /// </param>
        /// <param name="carShareRepository">
        /// The car share repository.
        /// </param>
        /// <param name="sessionRepository">
        /// The session repository.
        /// </param>
        /// <param name="carShareRequestRepository">
        /// The car Share Request Repository.
        /// </param>
        public FindNDriveUnitOfWork(DbContext dbContext, IRepository<User> userRepository, IRepository<CarShare> carShareRepository, IRepository<Session> sessionRepository, IRepository<CarShareRequest> carShareRequestRepository)
        {
            this._dbContext = dbContext;
            this.UserRepository = userRepository;
            this.CarShareRepository = carShareRepository;
            this.SessionRepository = sessionRepository;
            this.CarShareRequestRepository = carShareRequestRepository;
        }

        /// <summary>
        /// The commit.
        /// </summary>
        public void Commit()
        {
            this._dbContext.SaveChanges();
        }

        /// <summary>
        /// The dispose.
        /// </summary>
        public void Dispose()
        {
            this._dbContext.Dispose();
        }
    }
}