// --------------------------------------------------------------------------------------------------------------------
// <copyright file="RequestService.svc.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the RequestService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Services
{
    using System;
    using System.Collections.Generic;
    using System.Collections.ObjectModel;
    using System.IO;
    using System.Linq;
    using System.Net;
    using System.ServiceModel;
    using System.ServiceModel.Activation;
    using System.Text;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    using FindNDriveServices2.Contracts;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "RequestService" in code, svc and config file together.
    // NOTE: In order to launch WCF Test Client for testing this service, please select RequestService.svc or RequestService.svc.cs at the Solution Explorer and start debugging.
    /// <summary>
    /// The request service.
    /// </summary>
    [ServiceBehavior(
           InstanceContextMode = InstanceContextMode.PerCall,
           ConcurrencyMode = ConcurrencyMode.Multiple)]
    [AspNetCompatibilityRequirements(RequirementsMode = AspNetCompatibilityRequirementsMode.Required)]
    public class RequestService : IRequestService
    {
        /// <summary>
        /// The _find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork _findNDriveUnitOfWork;

        /// <summary>
        /// The _session manager.
        /// </summary>
        private readonly SessionManager _sessionManager;

        public RequestService()
        {

        }

        public RequestService(FindNDriveUnitOfWork findNDriveUnitOfWork, SessionManager sessionManager)
        {
            this._findNDriveUnitOfWork = findNDriveUnitOfWork;
            this._sessionManager = sessionManager;
        }



        /// <summary>
        /// The send request.
        /// </summary>
        /// <param name="carShareRequestDTO">
        /// The car share request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public ServiceResponse<CarShareRequest> SendRequest(CarShareRequestDTO carShareRequestDTO)
        {
            var targetCarShare = this._findNDriveUnitOfWork.CarShareRepository.AsQueryable().IncludeAll().FirstOrDefault(_ => _.CarShareId == carShareRequestDTO.CarShareId);
            targetCarShare.UnreadRequestsCount += 1;
           
            var targetUser = this._findNDriveUnitOfWork.UserRepository.Find(targetCarShare.DriverId);
            
            var request = new CarShareRequest()
                                          {
                                              AddToTravelBuddies = carShareRequestDTO.AddToTravelBuddies,
                                              CarShareId = carShareRequestDTO.CarShareId,
                                              UserId = carShareRequestDTO.UserId,
                                              Decision = carShareRequestDTO.Decision,
                                              Read = carShareRequestDTO.Read,
                                              Message = carShareRequestDTO.Message,
                                              SentOnDate = carShareRequestDTO.SentOnDate,
                                          };

            targetCarShare.Requests.Add(request);
            this._findNDriveUnitOfWork.Commit();

            const string ApiKey = "AIzaSyAo1y7Zzp4GAskemJMlWwtYkdmY-_A2zm8";
            const string SenderID = "505647745249";

            var gcmRequest = (HttpWebRequest)WebRequest.Create("https://android.googleapis.com/gcm/send");
            gcmRequest.KeepAlive = false;
            gcmRequest.Method = "POST";

            gcmRequest.ContentType = "application/json";
            gcmRequest.Headers.Add(string.Format("Authorization: key={0}", ApiKey));

            gcmRequest.Headers.Add(string.Format("Sender: id={0}", SenderID));

            var postData = "{ \"registration_ids\": [ \"" + targetUser.GCMRegistrationID + "\" ], " +
            "\"data\": {\"tickerText\":\"" + "ticker text" + "\", " +
             "\"contentTitle\":\"" + "content text" + "\", " +
             "\"message\": \"" + carShareRequestDTO.Message + "\"}}";

            var byteArray = Encoding.UTF8.GetBytes(postData);
            gcmRequest.ContentLength = byteArray.Length;

            var dataStream = gcmRequest.GetRequestStream();
            dataStream.Write(byteArray, 0, byteArray.Length);
            dataStream.Close();

            var gcmResponse = gcmRequest.GetResponse();

            dataStream = gcmResponse.GetResponseStream();

            var reader = new StreamReader(dataStream);

            string sResponseFromServer = reader.ReadToEnd();

            reader.Close();
            dataStream.Close();
            gcmResponse.Close();


            return new ServiceResponse<CarShareRequest>()
                       {
                           Result = request,
                           ServiceResponseCode = ServiceResponseCode.Success
                       };
        }

        /// <summary>
        /// The make decision.
        /// </summary>
        /// <param name="carShareRequestDTO">
        /// The car share request dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public ServiceResponse<CarShareRequest> ProcessDecision(CarShareRequestDTO carShareRequestDTO)
        {
            var errors = new List<string>();

            if (carShareRequestDTO.Decision == CarShareRequestDecision.Accepted)
            {
                var carShare =
                    this._findNDriveUnitOfWork.CarShareRepository.AsQueryable()
                        .IncludeAll()
                        .FirstOrDefault(_ => _.CarShareId == carShareRequestDTO.CarShareId);

                var newPassenger = this._findNDriveUnitOfWork.UserRepository.AsQueryable().FirstOrDefault(_ => _.UserId == carShareRequestDTO.UserId);

                if (carShare != null)
                {
                    if (carShare.AvailableSeats > 0)
                    {
                        carShare.Participants.Add(newPassenger);
                        carShare.AvailableSeats -= 1;
                    }
                    else
                    {
                        errors.Add("This car share is full.");
                    }
                }
            }

            var request = this._findNDriveUnitOfWork.CarShareRequestRepository.Find(
                carShareRequestDTO.CarShareRequestId);

            request.Decision = carShareRequestDTO.Decision;
            request.DecidedOnDate = carShareRequestDTO.DecidedOnDate;

            this._findNDriveUnitOfWork.Commit();

            return new ServiceResponse<CarShareRequest>()
                       {
                           Result = request,
                           ServiceResponseCode = ServiceResponseCode.Success,
                           ErrorMessages = errors
                       };
        }

        /// <summary>
        /// The mark as read.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public ServiceResponse<CarShareRequest> MarkAsRead(int id)
        {
            var request = this._findNDriveUnitOfWork.CarShareRequestRepository.AsQueryable()
                    .IncludeAll()
                    .FirstOrDefault(_ => _.CarShareRequestId == id);

            if (request != null)
            {
                var carShare = this._findNDriveUnitOfWork.CarShareRepository.Find(request.CarShareId);

                if (carShare.UnreadRequestsCount > 0)
                {
                    carShare.UnreadRequestsCount -= 1;
                }    

                request.Read = true;

                this._findNDriveUnitOfWork.Commit();
            }
            
            return new ServiceResponse<CarShareRequest>()
            {
                Result = request,
                ServiceResponseCode =
                   (request == null)? ServiceResponseCode.Failure : ServiceResponseCode.Success
            };
        }

        public ServiceResponse<List<CarShareRequest>> GetAllRequestsForCarShare(int id)
        {
            var requests =
                this._findNDriveUnitOfWork.CarShareRequestRepository.AsQueryable()
                    .IncludeAll()
                    .Where(_ => _.CarShareId == id).ToList();

            return new ServiceResponse<List<CarShareRequest>>()
                       {
                           Result = requests,
                           ServiceResponseCode =
                               ServiceResponseCode.Success
                       };
        }

        public ServiceResponse<List<CarShareRequest>> GetAllRequestsForUser(int id)
        {
            var requests =
               this._findNDriveUnitOfWork.CarShareRequestRepository.AsQueryable()
                   .IncludeAll()
                   .Where(_ => _.UserId == id).ToList();

            return new ServiceResponse<List<CarShareRequest>>()
            {
                Result = requests,
                ServiceResponseCode =
                    ServiceResponseCode.Success
            };
        }
    }
}
