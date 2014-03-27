namespace Services.Services
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DataAccessLayer;

    using DomainObjects.Domains;

    using global::Services.Contracts;
    using global::Services.DTOs;
    using global::Services.ServiceResponses;
    using global::Services.ServiceUtils;

    /// <summary>
    /// The journey template service.
    /// </summary>
    [ServiceBehavior(InstanceContextMode = InstanceContextMode.PerCall, ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class JourneyTemplateService : IJourneyTemplateService
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
        /// Initializes a new instance of the <see cref="JourneyTemplateService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        /// <param name="notificationManager">
        /// The notification manager.
        /// </param>
        public JourneyTemplateService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.notificationManager = notificationManager;
            this.random = new Random(Guid.NewGuid().GetHashCode());
        }

        /// <summary>
        /// Retrieves all journey templates for a given user.
        /// </summary>
        /// <param name="userId">
        /// The unique identifier of the user whose journey templates should be retrieved.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<JourneyTemplate>> GetTemplates(int userId)
        {
            var templates = this.findNDriveUnitOfWork.JourneyTemplateRepository.AsQueryable().IncludeChildren().Where(_ => _.UserId == userId).ToList();

            return ServiceResponseBuilder.Success(templates);
        }

        /// <summary>
        /// Creates a new journey template.
        /// </summary>
        /// <param name="journeyTemplateDTO">
        /// Contains the necessary journey template information supplied by the user 
        /// to construct a new JourneyTemplate object and store it in the database.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse CreateNewTemplate(JourneyTemplateDTO journeyTemplateDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            var user =
                this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                    .IncludeChildren()
                    .FirstOrDefault(_ => _.UserId == journeyTemplateDTO.UserId);

            if (user == null)
            {
                return ServiceResponseBuilder.Failure("Invalid user id.");
            }

            var templateExists =
                this.findNDriveUnitOfWork.JourneyTemplateRepository.AsQueryable()
                    .Any(_ => _.Alias.Equals(journeyTemplateDTO.Alias));

            if (templateExists)
            {
                return ServiceResponseBuilder.Failure("Template with this name alrady exists!");
            }

            // Create new journey template object.
            var newTemplate = new JourneyTemplate
            {
                Alias = journeyTemplateDTO.Alias,
                DateAndTimeOfDeparture = journeyTemplateDTO.DateAndTimeOfDeparture,
                Fee = journeyTemplateDTO.Fee,
                Pets = journeyTemplateDTO.Pets,
                VehicleType = journeyTemplateDTO.VehicleType,
                Smokers = journeyTemplateDTO.Smokers,
                GeoAddresses = journeyTemplateDTO.GeoAddresses,
                DateAllowance = journeyTemplateDTO.DateAllowance,
                TimeAllowance = journeyTemplateDTO.TimeAllowance,
                DepartureRadius = journeyTemplateDTO.DepartureRadius,
                DestinationRadius = journeyTemplateDTO.DestinationRadius,
                User = user,
                SearchByTime = journeyTemplateDTO.SearchByTime,
                SearchByDate = journeyTemplateDTO.SearchByDate,
                CreationDate = DateTime.Now
            };

            this.findNDriveUnitOfWork.JourneyTemplateRepository.Add(newTemplate);

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success();
        }

        /// <summary>
        /// Deletes a given journey template.
        /// </summary>
        /// <param name="id">
        /// The unique identifier of the JourneyTemplate object to be deleted.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse DeleteTemplate(int id)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            var template = this.findNDriveUnitOfWork.JourneyTemplateRepository.AsQueryable().IncludeChildren().FirstOrDefault(_ => _.JourneyTemplateId == id);

            if (template == null)
            {
                return ServiceResponseBuilder.Failure("Invalid journey template id");
            }

            this.findNDriveUnitOfWork.JourneyTemplateRepository.Remove(template);
            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success();
        }

        /// <summary>
        /// Updates a given journey template with new information provided by the user.
        /// </summary>
        /// <param name="journeyTemplateDTO">
        /// Contains the new JourneyTemplate information provided by the user 
        /// with which the relevant JourneyTemplate object will be updated.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse UpdateTemplate(JourneyTemplateDTO journeyTemplateDTO)
        {
            if (!this.sessionManager.IsSessionValid())
            {
                return ServiceResponseBuilder.Unauthorised();
            }

            // Find the relevant journey template object in the database.
            var template = this.findNDriveUnitOfWork.JourneyTemplateRepository.AsQueryable().IncludeChildren().FirstOrDefault(_ => _.JourneyTemplateId == journeyTemplateDTO.JourneyTemplateId);

            if (template == null)
            {
                return ServiceResponseBuilder.Failure("Invalid journey template id");
            }

            this.findNDriveUnitOfWork.GeoAddressRepository.RemoveRange(template.GeoAddresses);

            // Update the journey template object with new information.
            template.GeoAddresses = journeyTemplateDTO.GeoAddresses;
            template.DateAndTimeOfDeparture = journeyTemplateDTO.DateAndTimeOfDeparture;
            template.Fee = journeyTemplateDTO.Fee;
            template.Smokers = journeyTemplateDTO.Smokers;
            template.VehicleType = journeyTemplateDTO.VehicleType;
            template.Pets = journeyTemplateDTO.Pets;
            template.SearchByTime = journeyTemplateDTO.SearchByTime;
            template.SearchByDate = journeyTemplateDTO.SearchByDate;
            template.TimeAllowance = journeyTemplateDTO.TimeAllowance;
            template.DateAllowance = journeyTemplateDTO.DateAllowance;
            template.DepartureRadius = journeyTemplateDTO.DepartureRadius;
            template.DestinationRadius = journeyTemplateDTO.DestinationRadius;

            this.findNDriveUnitOfWork.Commit();

            return ServiceResponseBuilder.Success();
        }
    }
}
