// --------------------------------------------------------------------------------------------------------------------
// <copyright file="SessionManager.cs" company="">
//   
// </copyright>
// <summary>
//   The session manager.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.ServiceUtils
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
        private string EncryptValue(string value)
        {
            var encoding = new UTF8Encoding();
            var bytes = encoding.GetBytes(value);

            var sha = new SHA1CryptoServiceProvider();
            var hash = sha.ComputeHash(bytes);
            return Convert.ToBase64String(hash);
        }

        /// <summary>
        /// Checks whether the session arguments supplied inside HTTP headers are valid.
        /// Any mismatch in the session arguments when compared with the values stored in the database results in automatic user logout from the app.
        /// </summary>
        /// <returns>
        /// The <see cref="bool"/>.
        /// </returns>
        public bool IsSessionValid()
        {
            if (WebOperationContext.Current != null)
            {
                // Retrieve session arguments from HTTP headers.
                var incomingSessionId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.SESSION_ID];
                var incomingDeviceId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.DEVICE_ID];
                var randomId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.UUID];

                // Retrieve the user id from the session string.
                var userId = this.ExtractUserId(incomingSessionId);

                if (userId == -1)
                {
                    return false;
                }

                // Retrieve the actual session from the database.
                var savedSession = this.findNDriveUnitOfWork.SessionRepository.Find(userId);

                // Perform all the checks to ensure that the session is valid.
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

                    // Session has timed out, user must be logged out.
                    var result = DateTime.Compare(DateTime.Now, savedSession.ExpiryDate);

                    if (result > 0)
                    {
                        this.InvalidateSession(true);
                        return false;
                    }

                    // Since we have made it this far, it means that the current session is valid and can be extended by another 30 minutes.
                    if (savedSession.SessionType == SessionTypes.Temporary)
                    {
                        savedSession.ExpiryDate = DateTime.Now.AddMinutes(30);
                        this.findNDriveUnitOfWork.Commit();
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
        /// Checks if a given user is still logged in.
        /// This method is used by the notification manager before a notification is sent.
        /// This helps it decide whether notification should be dispatched to the user immediately or saved in the repository for later retrieval.
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

            this.InvalidateSession(true, user.UserId);

            return false;
        }

        /// <summary>
        /// Used by the auto-login feature, extracts user id from the session string.
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
                    return this.ExtractUserId(incomingSessionId);
                }
            }

            return -1;
        }

        /// <summary>
        /// Extracts user id from the session string.
        /// </summary>
        /// <param name="session">
        /// The session.
        /// </param>
        /// <returns>
        /// The <see cref="int"/>.
        /// </returns>
        private int ExtractUserId(string session)
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
        /// Generates a new session.
        /// Used when a new user registers with the system and when a current user logs in.
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

            // Retrieve session arguments from the HTTP headers.
            var rememberUser = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.REMEMBER_ME];
            var incomingDeviceId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.DEVICE_ID];
            var randomId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.UUID];

            // Set expiration date for the above token, initialy to 30 minutes.
            var validUntil = DateTime.Now.AddMinutes(30);
            var sessionId = GenerateNewSessionId(userId);
            var hashedDeviceId = EncryptValue(incomingDeviceId);
                
            // Check if user has ticked the remember me checkbox.
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
                    // Update existing session.
                    savedSession.SessionId = sessionId;
                    savedSession.DeviceId = hashedDeviceId;
                    savedSession.ExpiryDate = validUntil;
                    savedSession.SessionType = sessionType;
                }
                else
                {
                    // Create the new session.
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

                // Attach the newly generated session id to the outgoing HTTP response as a header.
                WebOperationContext.Current.OutgoingResponse.Headers.Add(SessionConstants.SESSION_ID, sessionId);
            }
        }

        /// <summary>
        /// Invalidates session for a given user and logs them out of the app.
        /// </summary>
        /// <param name="forceInvalidate">
        /// The force invalidate.
        /// </param>
        /// <param name="id">
        /// The id.
        /// </param>
        public void InvalidateSession(bool forceInvalidate, int id = -1)
        {
            var userId = 0;

            if (id != -1)
            {
                userId = id;
            }
            else
            {
                if (WebOperationContext.Current != null)
                {
                    var incomingSessionId = WebOperationContext.Current.IncomingRequest.Headers[SessionConstants.SESSION_ID];

                    userId = this.ExtractUserId(incomingSessionId);
                }
            }

            var savedSession = this.findNDriveUnitOfWork.SessionRepository.Find(userId);

            if (userId == -1 || savedSession == null)
            {
                return;
            }

            if (!forceInvalidate && savedSession.SessionType != SessionTypes.Temporary)
            {
                return;
            }

            var user = this.findNDriveUnitOfWork.UserRepository.Find(userId);
            user.Status = Status.Offline;
            user.GCMRegistrationID = null;
            savedSession.ExpiryDate = DateTime.Now.AddDays(-1);
            this.findNDriveUnitOfWork.Commit();
        }
    }
}