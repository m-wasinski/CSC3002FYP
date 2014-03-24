// --------------------------------------------------------------------------------------------------------------------
// <copyright file="EmailUtils.cs" company="">
//   
// </copyright>
// <summary>
//   The email utils.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace Services.ServiceUtils
{
    using System.Net;
    using System.Net.Mail;

    using Microsoft.Practices.ObjectBuilder2;

    /// <summary>
    /// The email utils.
    /// </summary>
    public static class EmailUtils
    {
        /// <summary>
        /// The smtpserver.
        /// </summary>
        private const string SMTPSERVER = "smtp.gmail.com";

        /// <summary>
        /// The portno.
        /// </summary>
        private const int PortNumber = 587;

        /// <summary>
        /// Used to send emails.
        /// </summary>
        /// <param name="emailToAddress">
        /// The email to address.
        /// </param>
        /// <param name="ccemailTo">
        /// The ccemail to.
        /// </param>
        /// <param name="subject">
        /// The subject.
        /// </param>
        /// <param name="body">
        /// The body.
        /// </param>
        /// <param name="isBodyHtml">
        /// The is body html.
        /// </param>
        public static void SendEmail(string[] emailToAddress, string[] ccemailTo, string subject, string body, bool isBodyHtml)
        {
            if (emailToAddress == null || emailToAddress.Length == 0)
            {
                return;
            }

            var smtpClient = new SmtpClient(SMTPSERVER, PortNumber)
                                 {
                                     EnableSsl = true,
                                     DeliveryMethod = SmtpDeliveryMethod.Network,
                                     UseDefaultCredentials = false,
                                     Credentials =
                                         new NetworkCredential(
                                         "noreply.findndrive@gmail.com",
                                         "findndrivepassword")
                                 };

            using (var mailMessage = new MailMessage())
            {
                mailMessage.From = new MailAddress("noreply.findndrive@gmail.com");
                mailMessage.Subject = subject ?? string.Empty;
                mailMessage.Body = body ?? string.Empty;
                mailMessage.IsBodyHtml = isBodyHtml;

                emailToAddress.ForEach(mailMessage.To.Add);

                if (ccemailTo != null)
                {
                    ccemailTo.ForEach(mailMessage.CC.Add);
                }

                smtpClient.Send(mailMessage);
            }
        }
    }
}