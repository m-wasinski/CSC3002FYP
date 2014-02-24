// --------------------------------------------------------------------------------------------------------------------
// <copyright file="Service.svc.cs" company="">
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
    using System.Collections.ObjectModel;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DomainObjects.Constants;
    using DomainObjects.Domains;
    using FindNDriveDataAccessLayer;

    using FindNDriveInfrastructureCore;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The car share service.
    /// </summary>
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.PerCall, ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class JourneyService : IJourneyService
    {
        /// <summary>
        /// The _find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager sessionManager;

        /// <summary>
        /// The notification manager.
        /// </summary>
        private readonly NotificationManager notificationManager;

        /// <summary>
        /// The random.
        /// </summary>
        private Random random;

        /// <summary>
        /// Initializes a new instance of the <see cref="JourneyService"/> class. 
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find N Drive Unit Of Work.
        /// </param>
        /// <param name="sessionManager">
        /// The session Manager.
        /// </param>
        /// <param name="notificationManager">
        /// </param>
        public JourneyService(
            FindNDriveUnitOfWork findNDriveUnitOfWork,
            SessionManager sessionManager,
            NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.notificationManager = notificationManager;
            this.random = new Random(Guid.NewGuid().GetHashCode());
        }

        /// <summary>
        /// The get all journeys for user.
        /// </summary>
        /// <param name="loadRangeDTO">
        /// The load range dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<Journey>> GetAllJourneysForUser(LoadRangeDTO loadRangeDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new List<Journey>());
            }

            var journeys =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.DriverId == loadRangeDTO.Id || _.Participants.Any(x => x.UserId == loadRangeDTO.Id))
                    .OrderByDescending(x => x.CreationDate)
                    .ToList();

            journeys.ForEach(
                delegate(Journey journey)
                    {
                        if (journey.DateAndTimeOfDeparture < DateTime.Now
                            && journey.JourneyStatus != JourneyStatus.Expired)
                        {
                            journey.JourneyStatus = JourneyStatus.Expired;
                        }
                    });

            journeys = LoadRangeHelper<Journey>.GetValidRange(
                journeys,
                loadRangeDTO.Index,
                loadRangeDTO.Count,
                loadRangeDTO.LoadMoreData);

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(journeys);
        }

        /// <summary>
        /// The create new journey.
        /// </summary>
        /// <param name="journeyDTO">
        /// The journey dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<Journey> CreateNewJourney(JourneyDTO journeyDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new Journey());
            }


            var validatedJourney = ValidationHelper.Validate(journeyDTO);
            Journey newJourney = null;

            if (!validatedJourney.IsValid)
            {
                return ServiceResponseBuilder.Failure<Journey>("Validation error.");
            }

            var user =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.UserId == journeyDTO.DriverId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<Journey>("Invalid user id.");
            }

            newJourney = new Journey
                             {
                                 AvailableSeats = journeyDTO.AvailableSeats,
                                 DateAndTimeOfDeparture = journeyDTO.DateAndTimeOfDeparture,
                                 Description = journeyDTO.Description,
                                 DriverId = journeyDTO.DriverId,
                                 Fee = journeyDTO.Fee,
                                 Private = journeyDTO.Private,
                                 SmokersAllowed = journeyDTO.SmokersAllowed,
                                 VehicleType = journeyDTO.VehicleType,
                                 PetsAllowed = journeyDTO.PetsAllowed,
                                 JourneyStatus = JourneyStatus.OK,
                                 GeoAddresses = journeyDTO.GeoAddresses,
                                 CreationDate = DateTime.Now,
                                 PreferredPaymentMethod = journeyDTO.PreferredPaymentMethod
                             };

            this.findNDriveUnitOfWork.JourneyRepository.Add(newJourney);

            this.findNDriveUnitOfWork.Commit();

            this.notificationManager.SendAppNotification(
                new Collection<User> {user}, 
                "You offered new journey",
                string.Format("You have offerred new journey from {0} to {1} and its number is: {2}, ", newJourney.GeoAddresses.First().AddressLine, newJourney.GeoAddresses.Last().AddressLine, newJourney.JourneyId),
                user.ProfilePictureId, newJourney.JourneyId,
                NotificationType.App,
                NotificationContentType.JourneyModified, random.Next());

            this.notificationManager.SendAppNotification(
                user.Friends,
                string.Format("{0} {1} ({2}) offered new journey.", user.FirstName, user.LastName, user.UserName),
                "Click this notification to see it.",
                user.ProfilePictureId, newJourney.JourneyId,
                NotificationType.Both,
                NotificationContentType.FriendOfferedNewJourney, random.Next());

            this.notificationManager.SendGcmTickle(
                user.Friends);

            return ServiceResponseBuilder.Success(newJourney);
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
        public ServiceResponse<Journey> GetJourneyById(int id)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new Journey());
            }

            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == id);

            return journey != null ? ServiceResponseBuilder.Success(journey) : ServiceResponseBuilder.Failure<Journey>("Journey with this id does not exist.");
        }

        /// <summary>
        /// The get multiple journeys by id.
        /// </summary>
        /// <param name="ids">
        /// The ids.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<Journey>> GetMultipleJourneysById(Collection<int> ids)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new List<Journey>());
            }

            var journeys =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => ids.Contains(_.JourneyId))
                    .ToList();
            return ServiceResponseBuilder.Success(journeys);
        }

        /// <summary>
        /// The modify journey.
        /// </summary>
        /// <param name="journeyDTO">
        /// The journey dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<Journey> ModifyJourney(JourneyDTO journeyDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(new Journey());
            }

            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == journeyDTO.JourneyId);

            if (journey == null)
            {
                return ServiceResponseBuilder.Failure<Journey>("Invalid journey id");
            }

            if (journey.JourneyStatus != JourneyStatus.OK)
            {
                return ServiceResponseBuilder.Failure<Journey>(string.Format("This journey is {0}, you cannot make a change to it.", journey.JourneyStatus == JourneyStatus.Cancelled ? "cancelled" : "expired"));
            }

            this.findNDriveUnitOfWork.GeoAddressRepository.RemoveRange(journey.GeoAddresses);

            journey.GeoAddresses = journeyDTO.GeoAddresses;
            journey.AvailableSeats = journeyDTO.AvailableSeats;
            journey.DateAndTimeOfDeparture = journeyDTO.DateAndTimeOfDeparture;
            journey.Description = journeyDTO.Description;
            journey.Fee = journeyDTO.Fee;
            journey.Private = journeyDTO.Private;
            journey.SmokersAllowed = journeyDTO.SmokersAllowed;
            journey.VehicleType = journeyDTO.VehicleType;
            journey.PetsAllowed = journeyDTO.PetsAllowed;
            journey.JourneyStatus = journeyDTO.JourneyStatus;
            journey.PreferredPaymentMethod = journeyDTO.PreferredPaymentMethod;

            this.findNDriveUnitOfWork.Commit();

            // Inform all the passengers that a change to one of the journeys they participate in has been made.
            this.notificationManager.SendAppNotification(
                journey.Participants,
                "Journey changed.",
                string.Format(
                    "{0}, {1}, ({2}) has made a change to journey no: {3}, departing from: {4} to {5}. Click here to see the change.",
                    journey.Driver.FirstName,
                    journey.Driver.LastName,
                    journey.Driver.UserName,
                    journey.JourneyId,
                    journey.GeoAddresses.First().AddressLine,
                    journey.GeoAddresses.Last().AddressLine),
                journey.Driver.ProfilePictureId,
                journey.JourneyId,
                NotificationType.Both,
                NotificationContentType.JourneyModified,
                this.random.Next());


            this.notificationManager.SendGcmTickle(
                journey.Participants);

            return ServiceResponseBuilder.Success(journey);
        }

        /// <summary>
        /// The cancel journey.
        /// </summary>
        /// <param name="journeyUserDTO">
        /// The journey user dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<bool> CancelJourney(JourneyUserDTO journeyUserDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(false);
            }

            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == journeyUserDTO.JourneyId);

            if (journey == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Journey with this id does not exist.");
            }

            if (journey.JourneyStatus != JourneyStatus.OK)
            {
                return ServiceResponseBuilder.Failure<bool>(string.Format("You cannot cancel an {0} journey", journey.JourneyStatus == JourneyStatus.Cancelled ? "already cancelled" : "expired"));
            }

            var user = this.findNDriveUnitOfWork.UserRepository.Find(journeyUserDTO.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<bool>("User with this id does not exist.");
            }

            if (journey.Driver.UserId != user.UserId)
            {
                return ServiceResponseBuilder.Failure<bool>("You are not allowed to cancel journey in which you are not the driver.");
            }

            journey.JourneyStatus = JourneyStatus.Cancelled;

            this.findNDriveUnitOfWork.Commit();

            // We must inform all the passengers of this journey of the cancellation.
            this.notificationManager.SendAppNotification(
                journey.Participants,
                "Journey has been cancelled.",
                string.Format(
                    "{0} {1} ({2}) has cancelled the journey no: {3}, from: {4} to: {5}",
                    journey.Driver.FirstName,
                   journey.Driver.LastName,
                   journey.Driver.UserName,
                   journey.JourneyId,
                   journey.GeoAddresses.First().AddressLine,
                   journey.GeoAddresses.Last().AddressLine),
                journey.Driver.ProfilePictureId,
                journey.JourneyId,
                NotificationType.Both,
                NotificationContentType.JourneyCancelled,
                this.random.Next());

            // Send an in-app notification to the driver to confirm that the journey has been succesfully cancelled.
            this.notificationManager.SendAppNotification(
               new List<User> { journey.Driver },
                "You have cancelled your journey.",
                string.Format(
                    "You have cancelled your journey no: {0}, from: {1} to: {2}. All of the passengers have been notified.",
                    journey.JourneyId,
                   journey.GeoAddresses.First().AddressLine,
                   journey.GeoAddresses.Last().AddressLine),
                -1,
                journey.JourneyId,
                NotificationType.Both,
                NotificationContentType.JourneyCancelled,
                this.random.Next());

            // Send tickle to all the passengers to let their apps synchronise with the sever.
            this.notificationManager.SendGcmTickle(
                journey.Participants);

            return ServiceResponseBuilder.Success(true);
        }

        public ServiceResponse<bool> WithdrawFromJourney(JourneyUserDTO journeyUserDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(false);
            }

            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == journeyUserDTO.JourneyId);

            if (journey == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid journey id");
            }

            if (journey.JourneyStatus != JourneyStatus.OK)
            {
                return ServiceResponseBuilder.Failure<bool>(string.Format("This journey is {0}, there is no need to withdraw.", journey.JourneyStatus == JourneyStatus.Cancelled ? "cancelled" : "expired"));
            }

            var passenger = this.findNDriveUnitOfWork.UserRepository.Find(journeyUserDTO.UserId);

            if (passenger == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid passenger id");
            }

            if (passenger.UserId == journey.DriverId)
            {
                return ServiceResponseBuilder.Failure<bool>("As driver, you cannot withdraw from this journey.");
            }
            
            journey.Participants.Remove(passenger);
            journey.AvailableSeats += 1;
            this.findNDriveUnitOfWork.Commit();

            this.notificationManager.SendAppNotification(
                new List<User> { journey.Driver },
                string.Format(
                    "{0} {1} ({2}) has left your journey.",
                    passenger.FirstName,
                    passenger.LastName,
                    passenger.UserName),
                string.Format(
                    "{0} {1} ({2}) has left your journey no: {3}, from: {4} to: {5}",
                    passenger.FirstName,
                    passenger.LastName,
                    passenger.UserName,
                    journey.JourneyId,
                    journey.GeoAddresses.First().AddressLine,
                    journey.GeoAddresses.Last().AddressLine),
                passenger.ProfilePictureId, journey.JourneyId, 
                NotificationType.Both,
                NotificationContentType.PassengerLeftJourney, random.Next());

            this.notificationManager.SendAppNotification(
                new List<User> { passenger },
                "You have left a journey.",
                string.Format(
                    "you have left a journey no: {0} from: {1} to: {2}. The driver {3} {4} ({5}) has been notified.",
                    journey.JourneyId,
                    journey.GeoAddresses.First().AddressLine,
                    journey.GeoAddresses.Last().AddressLine,
                    journey.Driver.FirstName,
                    journey.Driver.LastName,
                    journey.Driver.UserName),
                journey.Driver.ProfilePictureId, -1, 
                NotificationType.App,
                NotificationContentType.IleftAjourney, random.Next());

            this.notificationManager.SendGcmTickle(
                new List<User> { journey.Driver });

            return ServiceResponseBuilder.Success(true);
        }
    }
}
