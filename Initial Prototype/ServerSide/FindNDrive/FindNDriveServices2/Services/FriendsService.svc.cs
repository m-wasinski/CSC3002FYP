// --------------------------------------------------------------------------------------------------------------------
// <copyright file="FriendsService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the FriendsService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Services
{
    using System;
    using System.Collections.Generic;
    using System.Linq;
    using System.Security.Cryptography.X509Certificates;
    using System.ServiceModel;
    using System.ServiceModel.Activation;

    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The friends service.
    /// </summary>
    [ServiceBehavior(
           InstanceContextMode = InstanceContextMode.PerCall,
           ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class FriendsService : IFriendsService
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
        /// The _gcm manager.
        /// </summary>
        private readonly GCMManager gcmManager;

        /// <summary>
        /// Initializes a new instance of the <see cref="FriendsService"/> class.
        /// </summary>
        public FriendsService()
        {
            
        }

        /// <summary>
        /// Initializes a new instance of the <see cref="FriendsService"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        /// <param name="sessionManager">
        /// The session manager.
        /// </param>
        /// <param name="gcmManager">
        /// The gcm manager.
        /// </param>
        public FriendsService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager, GCMManager gcmManager)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
            this.sessionManager = sessionManager;
            this.gcmManager = gcmManager;
        }

        public ServiceResponse<bool> AddFriend(FriendDTO friendDTO)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(false);
            }

            var user = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .IncludeAll()
                .FirstOrDefault(_ => _.UserId == friendDTO.TargetUserId);

            var requestingUser = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .IncludeAll()
                .FirstOrDefault(_ => _.UserId == friendDTO.FriendUserId);

            if (user != null)
            {
                var friends =
                    user
                        .Friends.ToList();

                var match = friends.FirstOrDefault(_ => _.UserId == friendDTO.FriendUserId);

                if (match == null)
                {
                    user.Friends.Add(requestingUser);
                    requestingUser.Friends.Add(user);
                }
                else
                {
                    return ResponseBuilder.Failure<bool>("This user is already in your list of friends.");
                }
            }

            return ResponseBuilder.Success(true);
        }

        /// <summary>
        /// The get friends.
        /// </summary>
        /// <param name="userId">
        /// The user id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public ServiceResponse<List<User>> GetFriends(int userId)
        {
            if (!this.sessionManager.ValidateSession())
            {
                return ResponseBuilder.Unauthorised(new List<User>());
            }

            var user = this.findNDriveUnitOfWork.UserRepository.AsQueryable()
                .IncludeAll()
                .FirstOrDefault(_ => _.UserId == userId);

            if (user != null)
            {
                var friends =
                    user
                        .Friends.ToList();

                return ResponseBuilder.Success(friends);
            }

            return ResponseBuilder.Failure<List<User>>("Invalid user id");
        }
    }
}
