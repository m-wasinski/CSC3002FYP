// --------------------------------------------------------------------------------------------------------------------
// <copyright file="SearchService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the SearchService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Services
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "SearchService" in code, svc and config file together.
    // NOTE: In order to launch WCF Test Client for testing this service, please select SearchService.svc or SearchService.svc.cs at the Solution Explorer and start debugging.
    [ServiceBehavior(
          InstanceContextMode = InstanceContextMode.PerCall,
          ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class SearchService : ISearchService
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="SearchService"/> class.
        /// </summary>
        public SearchService()
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
        /// Initializes a new instance of the <see cref="SearchService"/> class. 
        /// Initializes a new instance of the <see cref="CarShareService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOf">
        /// The find n drive unit of.
        /// </param>
        /// <param name="sessionManager">
        /// The session Manager.
        /// </param>
        public SearchService(FindNDriveUnitOfWork findNDriveUnitOf, SessionManager sessionManager)
        {
            this._findNDriveUnitOfWork = findNDriveUnitOf;
            this._sessionManager = sessionManager;
        }

        /// <summary>
        /// The search car shares.
        /// </summary>
        /// <param name="carShare">
        /// The car share.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public ServiceResponse<List<CarShare>> SearchCarShares(CarShareDTO carShare)
        {
            var carShares =
                this._findNDriveUnitOfWork.CarShareRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.DepartureCity == carShare.DepartureCity &&
                            _.DestinationCity == carShare.DestinationCity &&
                            _.DateOfDeparture == carShare.DateOfDeparture &&
                            _.Fee == carShare.Fee &&
                            _.WomenOnly == carShare.WomenOnly)
                    .ToList();

            return new ServiceResponse<List<CarShare>>
            {
                Result = carShares,
                ServiceResponseCode = ServiceResponseCode.Success
            };
        }
    }
}
