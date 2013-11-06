using System;
using System.Collections.Generic;
using System.Linq;
using System.ServiceModel;
using DomainObjects;
using FindNDriveDataAccessLayer;
using FindNDriveServices.Contracts;
using FindNDriveServices.DTOs;
using FindNDriveServices.ServiceResponses;

namespace FindNDriveServices.Services
{
    [ServiceBehavior(
        InstanceContextMode = InstanceContextMode.Single,
        ConcurrencyMode = ConcurrencyMode.Multiple)]
    public class CarShareService : ICarShareService
    {
        public CarShareService()
        {
                
        }
        private readonly FindNDriveUnitOfWork _findNDriveUnitOfWork;

        public CarShareService(FindNDriveUnitOfWork findNDriveUnitOf)
        {
            _findNDriveUnitOfWork = findNDriveUnitOf;
        }

        public ServiceResponse<List<CarShare>> GetCarShareListings(CarShareDTO carShareDTO)
        {
            return new ServiceResponse<List<CarShare>>
            {
                Result = _findNDriveUnitOfWork.CarShareRepository.AsQueryable().Where(i => i.DestinationCity == "Lurgan").ToList(),
                ServiceReponseCode = ServiceResponseCode.Success,
                ErrorMessages = new List<string>() { "testerror" }
            };
        }

        public ServiceResponse<CarShare> CreateNewCarShareListing(CarShareDTO carShareDTO)
        {
            throw new NotImplementedException();
        }
    }
}
