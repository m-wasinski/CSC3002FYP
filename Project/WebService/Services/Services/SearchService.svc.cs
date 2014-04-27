// --------------------------------------------------------------------------------------------------------------------
// <copyright file="SearchService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the SearchService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.Services
{
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Linq;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DataAccessLayer;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using global::Services.Contracts;
    using global::Services.DTOs;
    using global::Services.ServiceResponses;
    using global::Services.ServiceUtils;

    /// <summary>
    /// The search service.
    /// </summary>
    [ServiceBehavior(
          InstanceContextMode = InstanceContextMode.PerCall,
          ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class SearchService : ISearchService
    {
        /// <summary>
        /// Initializes a new instance of the <see cref="SearchService"/> class.
        /// </summary>
        public SearchService(SessionManager sessionManager)
        {
            this.sessionManager = sessionManager;
        }

        /// <summary>
        /// The _find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        private readonly SessionManager sessionManager;

        /// <summary>
        /// The session manager.
        /// </summary>
        private readonly NotificationManager notificationManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="SearchService"/> class. 
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find N Drive Unit Of Work.
        /// </param>
        /// <param name="sessionManager">
        /// The session Manager.
        /// </param>
        /// <param name="notificationManager">
        /// </param>
        public SearchService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, NotificationManager notificationManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.notificationManager = notificationManager;
            this.sessionManager = sessionManager;
        }

        /// <summary>
        /// Performs a search for journeys taking into consideration the search criteria provided by the user.
        /// </summary>
        /// <param name="journeyTemplateDTO">
        /// Contains all the criteria that a successfull journey should meet in order to be returned inside the search results back to the user.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<Journey>> SearchForJourneys(JourneyTemplateDTO journeyTemplateDTO)
        {
            var requestingUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable().IncludeChildren().FirstOrDefault(_ => _.UserId == journeyTemplateDTO.UserId);

            if (requestingUser == null)
            {
                return ServiceResponseBuilder.Failure<List<Journey>>("Invalid user id.");
            }

            var journeys =
                this.findNDriveUnitOfWork.JourneyRepository.AsQueryable()
                    .Include(_ => _.Driver.Friends)
                    .Include(_ => _.GeoAddresses)
                    .Where(
                        _ =>
                        _.JourneyStatus == JourneyStatus.OK).ToList();

            journeys = journeys.Where(_ => SearchUtils.JourneyFilter(_, journeyTemplateDTO, requestingUser)).ToList();

            return ServiceResponseBuilder.Success(journeys);
        }
    }
}
