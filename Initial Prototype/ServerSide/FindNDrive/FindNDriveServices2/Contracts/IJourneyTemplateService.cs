// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IJourneyTemplateService.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the IJourneyTemplateService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.Contracts
{
    using System.Collections.Generic;
    using System.ServiceModel;
    using System.ServiceModel.Web;
    using DomainObjects.Domains;
    using FindNDriveServices2.DTOs;
    using FindNDriveServices2.ServiceResponses;

    /// <summary>
    /// The JourneyTemplateService interface.
    /// </summary>
    [ServiceContract]
    public interface IJourneyTemplateService
    {
        /// <summary>
        /// Retrieves a list of journey templates for a given user.
        /// </summary>
        /// <param name="userId">
        /// The user id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/get")]
        ServiceResponse<List<JourneyTemplate>> GetTemplates(int userId);

        /// <summary>
        /// Creates a new journey template.
        /// </summary>
        /// <param name="journeyTemplateDTO">
        /// The journey search dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/create")]
        ServiceResponse CreateNewTemplate(JourneyTemplateDTO journeyTemplateDTO);

        /// <summary>
        /// Deletes a journey template.
        /// </summary>
        /// <param name="id">
        /// The id.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/delete")]
        ServiceResponse DeleteTemplate(int id);

        /// <summary>
        /// Updates a given journey template.
        /// </summary>
        /// <param name="journeyTemplateDTO">
        /// The journey template dto.
        /// </param>
        /// <returns>
        /// The <see cref="ServiceResponse"/>.
        /// </returns>
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/update")]
        ServiceResponse UpdateTemplate(JourneyTemplateDTO journeyTemplateDTO);
    }
}