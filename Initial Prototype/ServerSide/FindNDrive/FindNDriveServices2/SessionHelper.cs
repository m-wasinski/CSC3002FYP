using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Web;
using DomainObjects.Domains;
using Microsoft.SqlServer.Server;

namespace FindNDriveServices2
{
    public static class SessionHelper
    {
        public static string GenerateNewSessionId()
        {
            var encoding = new UTF8Encoding();
            Byte[] bytes = encoding.GetBytes(GenerateRandomString());

            var SHA = new SHA1CryptoServiceProvider();
            byte[] hash = SHA.ComputeHash(bytes);
            return Convert.ToBase64String(hash);
        }

        public static string EncryptValue(string value)
        {
            var encoding = new UTF8Encoding();
            Byte[] bytes = encoding.GetBytes(value);

            var SHA = new SHA1CryptoServiceProvider();
            byte[] hash = SHA.ComputeHash(bytes);
            return Convert.ToBase64String(hash);
        }

        public static string GenerateRandomString()
        {
            var r = new Random();
            return new String(Enumerable.Range(0, 16).Select(n => (Char)(r.Next(16, 32))).ToArray());
        }

        public static bool ValidateSession(string token, string id, Session savedSession)
        {
            if (!token.Equals(savedSession.Token))
                return false;

            var encryptedId = EncryptValue(id);

            if (!savedSession.LastKnownId.Equals(encryptedId))
                return false;

            int result = DateTime.Compare(DateTime.Now, savedSession.SessionExpirationDate);

            if (result > 0)
                return false;

            return true;
        }
    }
}