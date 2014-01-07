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
    using System.Runtime.Serialization;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DomainObjects.Constants;
    using DomainObjects.Domains;
    using FindNDriveDataAccessLayer;

    using FindNDriveInfrastructureCore;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    using Microsoft.Practices.ObjectBuilder2;

    /// <summary>
    /// The car share service.
    /// </summary>
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.PerCall, ConcurrencyMode = ConcurrencyMode.Multiple)]
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
        /// <param name="sessionManager">
        /// The session Manager.
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
            var isAuthenticated = _sessionManager.ValidateSession();

            if (!isAuthenticated)
            {
                return new ServiceResponse<List<CarShare>>()
                           {
                               Result = null,
                               ServiceResponseCode = ServiceResponseCode.Unauthorized
                           };
            }

            var carShares =
                this._findNDriveUnitOfWork.CarShareRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.DriverId == id)
                    .ToList();

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
            var isAuthenticated = _sessionManager.ValidateSession();

            if (!isAuthenticated)
            {
                return new ServiceResponse<List<CarShare>>()
                           {
                               Result = null,
                               ServiceResponseCode = ServiceResponseCode.Unauthorized
                           };
            }

            var validSession = _sessionManager.ValidateSession();

            if (!validSession)
            {
                return new ServiceResponse<List<CarShare>>()
                           {
                               Result = null,
                               ServiceResponseCode = ServiceResponseCode.Unauthorized
                           };
            }

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

        /// <summary>
        /// The get all car share listings for user.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<CarShare>> GetAllCarShareListingsForUser(int id)
        {
            var isAuthenticated = this._sessionManager.ValidateSession();

            if (!isAuthenticated)
            {
                return new ServiceResponse<List<CarShare>>()
                           {
                               Result = null,
                               ServiceResponseCode = ServiceResponseCode.Unauthorized
                           };
            }

            var carShares =
                this._findNDriveUnitOfWork.CarShareRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.DriverId == id || _.Participants.Any(x => x.UserId == id))
                    .ToList();

            carShares.ForEach(delegate(CarShare carShare)
                {
                    if (carShare.DateAndTimeOfDeparture < DateTime.Now)
                    {
                        carShare.CarShareStatus = CarShareStatus.Past;
                    }
                });

            this._findNDriveUnitOfWork.Commit();

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
            var validatedCarShare = ValidationHelper.Validate(carShareDTO);
            CarShare newCarShare = null;

            if (validatedCarShare.IsValid)
            {
                newCarShare = new CarShare
                                  {
                                      AvailableSeats = carShareDTO.AvailableSeats,
                                      DateAndTimeOfDeparture = carShareDTO.DateAndTimeOfDeparture,
                                      DepartureCity = carShareDTO.DepartureCity,
                                      DestinationCity = carShareDTO.DestinationCity,
                                      Description = carShareDTO.Description,
                                      DriverId = carShareDTO.DriverId,
                                      Fee = carShareDTO.Fee,
                                      Private = carShareDTO.Private,
                                      WomenOnly = carShareDTO.WomenOnly,
                                      SmokersAllowed = carShareDTO.SmokersAllowed,
                                      VehicleType = carShareDTO.VehicleType,
                                      PetsAllowed = carShareDTO.PetsAllowed,
                                      CarShareStatus = CarShareStatus.Upcoming
                                  };

                this._findNDriveUnitOfWork.CarShareRepository.Add(newCarShare);
                this._findNDriveUnitOfWork.Commit();
            }

            return new ServiceResponse<CarShare>
                       {
                           Result = newCarShare,
                           ErrorMessages = validatedCarShare.ErrorMessages,
                           ServiceResponseCode =
                               (newCarShare != null)
                                   ? ServiceResponseCode.Success
                                   : ServiceResponseCode.Failure
                       };
        }

        /// <summary>
        /// The get car share by id.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<CarShare> GetCarShareById(int id)
        {
            var carShare =
                this._findNDriveUnitOfWork.CarShareRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.CarShareId == id);

            return new ServiceResponse<CarShare>
                       {
                           Result = carShare,
                           ServiceResponseCode = ServiceResponseCode.Success
                       };
        }

        /// <summary>
        /// The get car shares by id.
        /// </summary>
        /// <param name="ids">
        /// The ids.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<CarShare>> GetCarSharesById(List<int> ids)
        {
            var carShares =
                this._findNDriveUnitOfWork.CarShareRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => ids.Contains(_.CarShareId))
                    .ToList();

            return new ServiceResponse<List<CarShare>>
                       {
                           Result = carShares,
                           ServiceResponseCode = ServiceResponseCode.Success
                       };
        }

        /// <summary>
        /// The modify car share.
        /// </summary>
        /// <param name="carShareDTO">
        /// The car share dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public ServiceResponse<CarShare> ModifyCarShare(CarShareDTO carShareDTO)
        {
            var carShare =
                this._findNDriveUnitOfWork.CarShareRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.CarShareId == carShareDTO.CarShareId);

            if (carShare != null)
            {
                carShare.AvailableSeats = carShareDTO.AvailableSeats;
                carShare.DateAndTimeOfDeparture = carShareDTO.DateAndTimeOfDeparture;
                carShare.DepartureCity = carShareDTO.DepartureCity;
                carShare.DestinationCity = carShareDTO.DestinationCity;
                carShare.Description = carShareDTO.Description;
                carShare.DriverId = carShareDTO.DriverId;
                carShare.Fee = carShareDTO.Fee;
                carShare.Private = carShareDTO.Private;
                carShare.WomenOnly = carShareDTO.WomenOnly;
                carShare.SmokersAllowed = carShareDTO.SmokersAllowed;
                carShare.VehicleType = carShareDTO.VehicleType;
                carShare.PetsAllowed = carShareDTO.PetsAllowed;
                carShare.CarShareStatus = carShareDTO.CarShareStatus;
            }

            this._findNDriveUnitOfWork.Commit();

            return new ServiceResponse<CarShare>()
                       {
                           Result = carShare,
                           ServiceResponseCode =
                               (carShare == null)
                                   ? ServiceResponseCode.Failure
                                   : ServiceResponseCode.Success
                       };
        }

        /// <summary>
        /// The send new message.
        /// </summary>
        /// <param name="carShareMessageDTO">
        /// The car share message dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<CarShare> SendNewMessage(CarShareMessageDTO carShareMessageDTO)
        {
            var newMessage = new CarShareMessage()
                                 {
                                     MessageBody = carShareMessageDTO.MessageBody,
                                     ReadByUsers = new List<int>()
                                 };
            var carShare =
                this._findNDriveUnitOfWork.CarShareRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.CarShareId == carShareMessageDTO.CarShareId);

            carShare.Messages.Add(newMessage);

            this._findNDriveUnitOfWork.Commit();

            return new ServiceResponse<CarShare>()
                       {
                           Result = carShare,
                           ServiceResponseCode = ServiceResponseCode.Success
                       };

        }
    }
}
