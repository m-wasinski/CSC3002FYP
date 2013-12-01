// --------------------------------------------------------------------------------------------------------------------
// <copyright file="CarShareService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the CarShareService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Services
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;
    using System.Text.RegularExpressions;
    using System.Threading;
    using System.Web;
    using DomainObjects.Domains;
    using FindNDriveDataAccessLayer;
    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    using Microsoft.Practices.ObjectBuilder2;

    /// <summary>
    /// The car share service.
    /// </summary>
    [ServiceBehavior(
           InstanceContextMode = InstanceContextMode.PerCall,
           ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class CarShareService : ICarShareService
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="CarShareService"/> class.
        /// </summary>
        public CarShareService()
        {
       
        }

        /// <summary>
        /// The _find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork _findNDriveUnitOfWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager _sessionManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="CarShareService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOf">
        /// The find n drive unit of.
        /// </param>
        public CarShareService(FindNDriveUnitOfWork findNDriveUnitOf, SessionManager sessionManager)
        {
            this._findNDriveUnitOfWork = findNDriveUnitOf;
            this._sessionManager = sessionManager;
        }

        /// <summary>
        /// The get car share listings by user.
        /// </summary>
        /// <param name="id">
        ///     The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public ServiceResponse<List<CarShare>> GetCarShareListingsAsDriver(int id)
        {
            var carShares =
                this._findNDriveUnitOfWork.CarShareRepository.AsQueryable().IncludeAll().Where(_ => _.UserId == id).ToList();

            return new ServiceResponse<List<CarShare>>
            {
                Result = carShares,
                ServiceResponseCode = ServiceResponseCode.Success
            };
        }

        /// <summary>
        /// The get car share listings as participant.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<CarShare>> GetCarShareListingsAsParticipant(int id)
        {
            var carShares =
                this._findNDriveUnitOfWork.CarShareRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.Participants.Count > 0 && _.Participants.Any(x => x.UserId == id))
                    .ToList(); 

            return new ServiceResponse<List<CarShare>>
            {
                Result = carShares,
                ServiceResponseCode = ServiceResponseCode.Success
            };
        }

        public ServiceResponse<List<CarShare>> GetAllCarShareListingsForUser(int id)
        {
            var carShares =
                this._findNDriveUnitOfWork.CarShareRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.UserId == id || _.Participants.Any(x => x.UserId == id))
                    .ToList();

            return new ServiceResponse<List<CarShare>>
            {
                Result = carShares,
                ServiceResponseCode = ServiceResponseCode.Success
            };
        }

        /// <summary>
        /// The create new car share listing.
        /// </summary>
        /// <param name="carShareDTO">
        /// The car share dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public ServiceResponse<CarShare> CreateNewCarShareListing(CarShareDTO carShareDTO)
        {
            throw new NotImplementedException();
        }
    }
}
