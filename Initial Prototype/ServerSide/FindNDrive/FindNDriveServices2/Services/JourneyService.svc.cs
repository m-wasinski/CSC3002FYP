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
    using System.Data.Entity;
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
    using FindNDriveServices2.ServiceUtils;

    using Microsoft.Practices.ObjectBuilder2;

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

            var user = this.findNDriveUnitOfWork.UserRepository.Find(loadRangeDTO.Id);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<List<Journey>>("Invalid user id");
            }

            var journeys =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .Include(_ => _.Driver).Include(_ => _.GeoAddresses)
                    .Where(_ => _.Driver.UserId == loadRangeDTO.Id || _.Participants.Any(x => x.UserId == loadRangeDTO.Id))
                    .OrderByDescending(x => x.CreationDate)
                    .Skip(loadRangeDTO.Skip)
                    .Take(loadRangeDTO.Take);

            journeys.ForEach(
                delegate(Journey journey)
                    {
                        if (journey.DateAndTimeOfDeparture < DateTime.Now && journey.JourneyStatus != JourneyStatus.Expired)
                        {
                            journey.JourneyStatus = JourneyStatus.Expired;
                        }

                        var messages = this.findNDriveUnitOfWork.JourneyMessageRepository.AsQueryable().IncludeAll().Where(_ => _.JourneyId == journey.JourneyId).ToList();

                        journey.UnreadMessagesCount = messages.Count(
                            delegate(JourneyMessage journeyMessage)
                            {
                                var ids = journeyMessage.SeenBy.Select(_ => _.UserId).ToList();

                                return !ids.Contains(loadRangeDTO.Id);
                            });
                    });

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success(journeys.ToList());
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
        public ServiceResponse<bool> CreateNewJourney(JourneyDTO journeyDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised<bool>();
            }

            var user =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable().Include(_ => _.Friends).FirstOrDefault(_ => _.UserId == journeyDTO.Driver.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid user id.");
            }

            if (journeyDTO.DateAndTimeOfDeparture < DateTime.Now)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid date or time!");
            }

            var newJourney = new Journey
                                     {
                                         AvailableSeats = journeyDTO.AvailableSeats,
                                         DateAndTimeOfDeparture = journeyDTO.DateAndTimeOfDeparture,
                                         Description = journeyDTO.Description,
                                         Driver = user,
                                         Fee = journeyDTO.Fee,
                                         Private = journeyDTO.Private,
                                         Smokers = journeyDTO.Smokers,
                                         VehicleType = journeyDTO.VehicleType,
                                         Pets = journeyDTO.Pets,
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
                -1, newJourney.JourneyId,
                NotificationType.App,
                NotificationContentType.JourneyModified, random.Next());

            this.notificationManager.SendAppNotification(
                user.Friends,
                string.Format("{0} {1} ({2}) offered new journey.", user.FirstName, user.LastName, user.UserName),
                "Click this notification to see it.",
                user.UserId, newJourney.JourneyId,
                NotificationType.Both,
                NotificationContentType.FriendOfferedNewJourney, random.Next());

            this.notificationManager.SendGcmTickle(
                user.Friends);

            var interestedUsers =
               SearchUtils.SearchForTemplates(newJourney, this.findNDriveUnitOfWork).Select(_ => _.User).ToList();

            this.notificationManager.SendAppNotification(
               interestedUsers,
               string.Format("New journey found!."),"Journey matching your criteria has just been offered.", newJourney.Driver.UserId, newJourney.JourneyId,
               NotificationType.Both,
               NotificationContentType.FriendOfferedNewJourney, random.Next());

            this.notificationManager.SendGcmTickle(
               interestedUsers);

            return ServiceResponseBuilder.Success(true);
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
                    .Include(_ => _.Driver).Include(_ => _.GeoAddresses)
                    .FirstOrDefault(_ => _.JourneyId == id);

            if (journey == null)
            {
                return ServiceResponseBuilder.Failure<Journey>("Invalid journey id");
            }

            journey.Driver = new User { UserId = journey.Driver.UserId, FirstName = journey.Driver.FirstName, LastName = journey.Driver.LastName};

            return ServiceResponseBuilder.Success(journey);
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
        public ServiceResponse<bool> ModifyJourney(JourneyDTO journeyDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised<bool>();
            }

            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .Include(_ => _.GeoAddresses).Include(_ => _.Participants)
                    .FirstOrDefault(_ => _.JourneyId == journeyDTO.JourneyId);

            if (journey == null)
            {
                return ServiceResponseBuilder.Failure<bool>("Invalid journey id");
            }

            if (journey.JourneyStatus != JourneyStatus.OK)
            {
                return ServiceResponseBuilder.Failure<bool>(string.Format("This journey is {0}, you cannot make a change to it.", journey.JourneyStatus == JourneyStatus.Cancelled ? "cancelled" : "expired"));
            }

            this.findNDriveUnitOfWork.GeoAddressRepository.RemoveRange(journey.GeoAddresses);

            journey.GeoAddresses = journeyDTO.GeoAddresses;
            journey.AvailableSeats = journeyDTO.AvailableSeats;
            journey.DateAndTimeOfDeparture = journeyDTO.DateAndTimeOfDeparture;
            journey.Description = journeyDTO.Description;
            journey.Fee = journeyDTO.Fee;
            journey.Private = journeyDTO.Private;
            journey.Smokers = journeyDTO.Smokers;
            journey.VehicleType = journeyDTO.VehicleType;
            journey.Pets = journeyDTO.Pets;
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
                journey.Driver.UserId,
                journey.JourneyId,
                NotificationType.Both,
                NotificationContentType.JourneyModified,
                this.random.Next());


            this.notificationManager.SendGcmTickle(
                journey.Participants);

            return ServiceResponseBuilder.Success(true);
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
        public ServiceResponse<Journey> CancelJourney(JourneyUserDTO journeyUserDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised<Journey>();
            }

            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.JourneyId == journeyUserDTO.JourneyId);

            if (journey == null)
            {
                return ServiceResponseBuilder.Failure<Journey>("Journey with this id does not exist.");
            }

            if (journey.JourneyStatus != JourneyStatus.OK)
            {
                return ServiceResponseBuilder.Failure<Journey>(string.Format("You cannot cancel an {0} journey", journey.JourneyStatus == JourneyStatus.Cancelled ? "already cancelled" : "expired"));
            }

            var user = this.findNDriveUnitOfWork.UserRepository.Find(journeyUserDTO.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure<Journey>("User with this id does not exist.");
            }

            if (journey.Driver.UserId != user.UserId)
            {
                return ServiceResponseBuilder.Failure<Journey>("You are not allowed to cancel journey in which you are not the driver.");
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
                journey.Driver.UserId,
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

            return ServiceResponseBuilder.Success(journey);
        }

        public ServiceResponse<bool> WithdrawFromJourney(JourneyUserDTO journeyUserDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised(false);
            }

            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .Include(_ => _.Participants).Include(_ => _.Driver).Include(_ => _.GeoAddresses)
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

            if (passenger.UserId == journey.Driver.UserId)
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
                passenger.UserId, journey.JourneyId, 
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
                journey.Driver.UserId, -1, 
                NotificationType.App,
                NotificationContentType.IleftAjourney, random.Next());

            this.notificationManager.SendGcmTickle(
                new List<User> { journey.Driver });

            return ServiceResponseBuilder.Success(true);
        }


        public ServiceResponse<List<User>> GetPassengers(int journeyId)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised<List<User>>();
            }

            var journey =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .Include(_ => _.Participants)
                    .FirstOrDefault(_ => _.JourneyId == journeyId);

            if (journey == null)
            {
                return ServiceResponseBuilder.Failure<List<User>>("Invalid journey id.");
            }

            var users =
                (from user in journey.Participants select new User { UserId = user.UserId, FirstName = user.FirstName, LastName = user.LastName, UserName = user.UserName }).ToList(); 

            return ServiceResponseBuilder.Success(users.ToList());
        }
    }
}
