using System;
using DomainObjects;
using FindNDriveServices.Contracts;
using FindNDriveServices.DTOs;
using FindNDriveServices.ServiceResponses;

namespace FindNDriveServices.Services
{
    class CarShareService : ICarShareService
    {
        public ServiceResponse<CarShare> GetCarShareListings(CarShareDTO carShareDTO)
        {
            throw new NotImplementedException();
        }

        public ServiceResponse<CarShare> CreateNewCarShareListing(CarShareDTO carShareDTO)
        {
            throw new NotImplementedException();
        }
    }
}
