// --------------------------------------------------------------------------------------------------------------------
// <copyright file="SessionManager.cs" company="">
//   
// </copyright>
// <summary>
//   The session manager.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2
{
    using System;
    using System.Security.Cryptography;
    using System.ServiceModel.Web;
    using System.Text;

    using DomainObjects.Constants;
    using DomainObjects.Domains;

    using FindNDriveDataAccessLayer;

    /// <summary>
    /// The session manager.
    /// </summary>
    public class SessionManager
    {
        /// <summary>
        /// The _find n drive unit of work.
        /// </summary>
        private readonly FindNDriveUnitOfWork findNDriveUnitOfWork;

        /// <summary>
        /// Initializes a new instance of the <see cref="SessionManager"/> class.
        /// </summary>
        /// <param name="findNDriveUnitOfWork">
        /// The find n drive unit of work.
        /// </param>
        public SessionManager(FindNDriveUnitOfWork findNDriveUnitOfWork)
        {
            this.findNDriveUnitOfWork = findNDriveUnitOfWork;
        }

        /// <summary>
        /// Generates a new session id for the user.
        /// </summary>
        /// <param name="userId">
        /// The user id.
        /// </param>
        /// <returns>
        /// The <see cref="string"/>.
        /// </returns>
        public static string GenerateNewSessionId(int userId)
        {
            return userId + ":" + Guid.NewGuid().ToString().Replace("-", string.Empty).Replace(":", string.Empty).Substring(0, 8);
        }

        /// <summary>
        /// Encrypts a given string value and returns a hash.
        /// </summary>
        /// <param name="value">
        /// The value.
        /// </param>
        /// <returns>
        /// The <see cref="string"/>.
        /// </returns>
        private static string EncryptValue(string value)
        {
            var encoding = new UTF8Encoding();
            var bytes = encoding.GetBytes(value);

            var sha = new SHA1CryptoServiceProvider();
            var hash = sha.ComputeHash(bytes);
            return Convert.ToBase64String(hash);
        }

        /// <summary>
        /// The is session valid.
        /// </summary>
        /// <returns>
        /// The <see cref="bool"/>.
        /// </returns>
        public bool IsSessionValid()
        {
            if (WebOperationContext.Current != null)
            {
                var incomingSessionId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.SESSION_ID];
                var incomingDeviceId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.DEVICE_ID];
                var randomId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.UUID];

                var userId = this.GetUserId(incomingSessionId);

                if (userId == -1)
                {
                    return false;
                }

                var savedSession = this.findNDriveUnitOfWork.SessionRepository.Find(this.GetUserId(incomingSessionId));

                if (savedSession != null)
                {
                    if (savedSession.SessionType == SessionTypes.Temporary)
                    {
                        if (randomId != savedSession.Uuid)
                        {
                            return false;
                        }
                    }

                    if (!incomingSessionId.Equals(savedSession.SessionId))
                    {
                        return false;
                    }

                    var encryptedId = EncryptValue(incomingDeviceId);

                    if (!savedSession.DeviceId.Equals(encryptedId))
                    {
                        return false;
                    }

                    var result = DateTime.Compare(DateTime.Now, savedSession.ExpiryDate);

                    if (result > 0)
                    {
                        this.InvalidateSession(true);
                        return false;
                    }

                    if (savedSession.SessionType == SessionTypes.Temporary)
                    {
                        this.RefreshSession(savedSession);
                    }
                }
                else
                {
                    return false;
                }
            }
            else
            {
                return false;
            }
                
            return true;
        }

        /// <summary>
        /// The is still logged in.
        /// </summary>
        /// <param name="user">
        /// The user.
        /// </param>
        /// <returns>
        /// The <see cref="bool"/>.
        /// </returns>
        public bool IsStillLoggedIn(User user)
        {
            if (user.Status == Status.Offline && user.GCMRegistrationID != null)
            {
                return false;
            }

            var savedSession = this.findNDriveUnitOfWork.SessionRepository.Find(user.UserId);

            var result = DateTime.Compare(DateTime.Now, savedSession.ExpiryDate);

            if (result <= 0)
            {
                return true;
            }

            user.Status = Status.Offline;
            user.GCMRegistrationID = null;
            savedSession.ExpiryDate = DateTime.Now.AddDays(-1);
            this.findNDriveUnitOfWork.Commit();
            return false;
        }

        /// <summary>
        /// The get user id.
        /// </summary>
        /// <returns>
        /// The <see cref="int"/>.
        /// </returns>
        public int GetUserId()
        {
            if (WebOperationContext.Current != null)
            {
                var incomingSessionId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.SESSION_ID];

                if (incomingSessionId != null)
                {
                    return this.GetUserId(incomingSessionId);
                }
            }

            return -1;
        }

        /// <summary>
        /// The get user id.
        /// </summary>
        /// <param name="session">
        /// The session.
        /// </param>
        /// <returns>
        /// The <see cref="int"/>.
        /// </returns>
        public int GetUserId(string session)
        {
            string stringId;

            if (session == null)
            {
                return -1;
            }

            int id;

            try
            {
                stringId = session.Substring(0, session.IndexOf(":", StringComparison.Ordinal));
            }
            catch (ArgumentOutOfRangeException)
            {
                return -1;
            }

            // ToInt32 can throw FormatException or OverflowException. 
            try
            {
                id = Convert.ToInt32(stringId);
            }
            catch (FormatException)
            {
                return -1;
            }
            catch (OverflowException)
            {
                return -1;
            }

            return id;
        }

        /// <summary>
        /// The refresh session.
        /// </summary>
        /// <param name="session">
        /// The session.
        /// </param>
        public void RefreshSession(Session session)
        {
            session.ExpiryDate = DateTime.Now.AddMinutes(30);
            this.findNDriveUnitOfWork.Commit();
        }

        /// <summary>
        /// The generate new session.
        /// </summary>
        /// <param name="userId">
        /// The user id.
        /// </param>
        public void GenerateNewSession(int userId)
        {
            var sessionType = SessionTypes.Temporary;

            if (WebOperationContext.Current == null)
            {
                return;
            }

            var rememberUser = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.REMEMBER_ME];
            var incomingDeviceId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.DEVICE_ID];
            var randomId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.UUID];

            // Set expiration date for the above token, initialy to 30 minutes.
            var validUntil = DateTime.Now.AddMinutes(30);
            var sessionId = GenerateNewSessionId(userId);
            var hashedDeviceId = EncryptValue(incomingDeviceId);
                
            if (rememberUser != null)
            {
                var savedSession = this.findNDriveUnitOfWork.SessionRepository.Find(userId);

                if (rememberUser.Equals("true"))
                {
                    // Make the token expire in two weeks.
                    validUntil = DateTime.Now.AddDays(14);
                    sessionType = SessionTypes.Permanent;
                    sessionId = sessionId + "1";
                }
                else
                {
                    sessionId = sessionId + "0";
                    if (savedSession != null)
                    {
                        savedSession.Uuid = randomId;
                    }
                }

                if (savedSession != null)
                {
                    savedSession.SessionId = sessionId;
                    savedSession.DeviceId = hashedDeviceId;
                    savedSession.ExpiryDate = validUntil;
                    savedSession.SessionType = sessionType;
                }
                else
                {
                    var newSession = new Session
                                         {
                                             Uuid = randomId,
                                             DeviceId = hashedDeviceId,
                                             ExpiryDate = validUntil,
                                             SessionType = sessionType,
                                             SessionId = sessionId,
                                             UserId = userId,
                                         };

                    this.findNDriveUnitOfWork.SessionRepository.Add(newSession);
                }
                        
                this.findNDriveUnitOfWork.Commit();

                WebOperationContext.Current.OutgoingResponse.Headers.Add(SessionConstants.SESSION_ID, sessionId);
            }
        }

        /// <summary>
        /// The invalidate session.
        /// </summary>
        /// <param name="forceInvalidate">
        /// The force invalidate.
        /// </param>
        /// <returns>
        /// The <see cref="bool"/>.
        /// </returns>
        public bool InvalidateSession(bool forceInvalidate)
        {
            if (WebOperationContext.Current == null)
            {
                return false;
            }

            var incomingSessionId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.SESSION_ID];

            var userId = this.GetUserId(incomingSessionId);

            if (userId == -1)
            {
                return false;
            }

            var savedSession = this.findNDriveUnitOfWork.SessionRepository.Find(userId);

            if (userId == -1 || savedSession == null)
            {
                return false;
            }

            if (!forceInvalidate && savedSession.SessionType != SessionTypes.Temporary)
            {
                return false;
            }

            var user = this.findNDriveUnitOfWork.UserRepository.Find(userId);
            user.Status = Status.Offline;
            user.GCMRegistrationID = null;
            savedSession.ExpiryDate = DateTime.Now.AddDays(-1);
            this.findNDriveUnitOfWork.Commit();

            return true;
        }
    }
}