// --------------------------------------------------------------------------------------------------------------------
// <copyright file="IJourneyTemplateService.cs" company="">
//   
// </copyright>
// <summary>
//   Defines the IJourneyTemplateService type.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

using System.Collections.Generic;

namespace FindNDriveServices2.Contracts
{
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
        /// The get templates.
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
        /// The create new template.
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
        ServiceResponse<bool> CreateNewTemplate(JourneyTemplateDTO journeyTemplateDTO);

        /// <summary>
        /// The delete template.
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
        ServiceResponse<bool> DeleteTemplate(int id);

        /// <summary>
        /// The update template.
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
        ServiceResponse<bool> UpdateTemplate(JourneyTemplateDTO journeyTemplateDTO);
    }
}