using System;
using System.Collections.Generic;
using System.ServiceModel;
using DomainObjects.Domains;
using FindNDriveDataAccessLayer;
using FindNDriveServices2.Contracts;
using FindNDriveServices2.DTOs;
using FindNDriveServices2.ServiceResponses;
using Omu.ValueInjecter;

namespace FindNDriveServices2.Services
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
            //var aleksandra = new User
            //{
            //    FirstName = "Aleksandra",
            //    LastName = "Szczypior",
            //    DateOfBirth = new DateTime(1992, 11, 15),
            //    EmailAddress = "alex1710@vp.pl",
            //    Gender = Gender.Female,
            //};

            //    _findNDriveUnitOfWork.UserRepository.Add(aleksandra);

            //    _findNDriveUnitOfWork.CarShareRepository.Add(new CarShare()
            //    {
            //        DateOfDeparture = new DateTime(DateTime.Today.Year, DateTime.Today.Month, DateTime.Today.Day),
            //        DepartureCity = "Belfast",
            //        Description = "Test Car Share",
            //        DestinationCity = "Lurgan",
            //        Driver = aleksandra,
            //        Fee = 0.00,
            //        AvailableSeats = 4,
            //        SmokersAllowed = false,
            //        WomenOnly = false,
            //    });

            //_findNDriveUnitOfWork.Commit();

            //var proxies = 
            //_findNDriveUnitOfWork.CarShareRepository.AsQueryable()
            //    .ToList()
            //    .Select(
            //        _ =>
            //            new CarShare()
            //            {
            //                Id = _.Id,
            //                Driver = new User().InjectFrom(_.Driver) as User,
            //                DepartureCity = _.DestinationCity,
            //                DestinationCity = _.DestinationCity,
            //                DateOfDeparture = _.DateOfDeparture,
            //                TimeOfDeparture = _.TimeOfDeparture,
            //                Description = _.Description,
            //                Fee = _.Fee,
            //                AvailableSeats = _.AvailableSeats,
            //                Participants = new Collection<User>().InjectFrom(_.Participants) as Collection<User>,
            //                SmokersAllowed = _.SmokersAllowed,
            //                WomenOnly = _.WomenOnly,
            //                VehicleType = _.VehicleType
            //            })
            //    .ToList();




            //Debug.WriteLine("Service Called! " + proxies.Count);
            //return new ServiceResponse<List<CarShare>>
            //{
            //    Result = proxies,
            //    ServiceReponseCode = ServiceResponseCode.Success,
            //    ErrorMessages = new List<string>() { "testerror" }
            //};

            throw new NotImplementedException();
        }

        public ServiceResponse<CarShare> CreateNewCarShareListing(CarShareDTO carShareDTO)
        {
            throw new NotImplementedException();
        }

        public static ICollection<TTo> InjectFrom<TFrom, TTo>(ICollection<TTo> to, IEnumerable<TFrom> from) where TTo : new()
        {
            foreach (var source in from)
            {
                var target = new TTo();
                target.InjectFrom(source);
                to.Add(target);
            }
            return to;
        }
    }
}
