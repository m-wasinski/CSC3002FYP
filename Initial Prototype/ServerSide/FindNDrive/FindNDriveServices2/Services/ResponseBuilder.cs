// --------------------------------------------------------------------------------------------------------------------
// <copyright file="ResponseBuilder.cs" company="">
//   
// </copyright>
// <summary>
//   Builder class used to produce instantiations of the ServiceResponse class.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Services
{
    using System.Collections.Generic;

    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    ///     Builder class used to produce instantiations of the ServiceResponse class.
    /// </summary>
    public class ResponseBuilder
    {
        #region Public Methods and Operators

        /// <summary>
        ///     Creates a new Failure ServiceResponse
        /// </summary>
        /// <returns>A new instance of the ServiceResponse code, with the ServiceResponseCode set to failure</returns>
        public static ServiceResponse Failure()
        {
            return new ServiceResponse
            {
                ServiceResponseCode = ServiceResponseCode.Failure,
                ErrorMessages = new List<string>()
            };
        }

        /// <summary>
        /// Creates a new instance of a failure ServiceResponse with the given error Messages
        /// </summary>
        /// <param name="errorMessages">
        /// The reasons for failure
        /// </param>
        /// <returns>
        /// A new serviceresponse failure message
        /// </returns>
        public static ServiceResponse Failure(List<string> errorMessages)
        {
            return new ServiceResponse
            {
                ServiceResponseCode = ServiceResponseCode.Failure,
                ErrorMessages = errorMessages
            };
        }

        /// <summary>
        /// Returns a new failure service response, of type T.
        /// </summary>
        /// <typeparam name="T">
        /// Generic type of the reponse.
        /// </typeparam>
        /// <param name="errorMessages">
        /// The error messages
        /// </param>
        /// <returns>A faillure service response</returns>
        public static ServiceResponse<T> Failure<T>(List<string> errorMessages)
        {
            return new ServiceResponse<T>
            {
                ServiceResponseCode = ServiceResponseCode.Failure,
                ErrorMessages = errorMessages
            };
        }

        /// <summary>
        /// Creates a new failure response with one string error message.
        /// </summary>
        /// <param name="errorMessage">
        /// The error message.
        /// </param>
        /// <typeparam name="T">
        /// </typeparam>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        public static ServiceResponse<T> Failure<T>(string errorMessage)
        {
            return Failure<T>(new List<string>() { errorMessage });
        }

        /// <summary>
        ///     Helper method that takes one error message and inserts it into the list of errors.
        /// </summary>
        /// <param name="errorMessage">
        ///     The single error message
        /// </param>
        /// <returns>
        ///     ServiceResponse
        /// </returns>
        public static ServiceResponse Failure(string errorMessage)
        {
            return Failure(new List<string>() { errorMessage });
        }

        /// <summary>
        ///     Creates a new successful response
        /// </summary>
        /// <returns>A new ServiceResponse instance with the ServiceResponseCode equal to Success</returns>
        public static ServiceResponse Success()
        {
            return new ServiceResponse
            {
                ServiceResponseCode = ServiceResponseCode.Success,
                ErrorMessages = new List<string>()
            };
        }

        /// <summary>
        /// Returns a new succesfull response, of type T
        /// </summary>
        /// <typeparam name="T">
        /// The generic type of the response
        /// </typeparam>
        /// <param name="result">
        /// The returned result
        /// </param>
        /// <returns>
        /// A service response with the given result, and a success ServiceResponseCode
        /// </returns>
        public static ServiceResponse<T> Success<T>(T result)
        {
            return new ServiceResponse<T>
            {
                ServiceResponseCode = ServiceResponseCode.Success,
                Result = result,
                ErrorMessages = new List<string>()
            };
        }

        #endregion
    }
}