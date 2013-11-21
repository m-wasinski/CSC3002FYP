using System.Collections.Generic;
using System.Security.Permissions;
using System.ServiceModel;
using System.ServiceModel.Web;
using DomainObjects.Domains;
using FindNDriveServices2.DTOs;
using FindNDriveServices2.ServiceResponses;

namespace FindNDriveServices2.Contracts
{
    [ServiceContract]
    public interface ICarShareService
    {
        [OperationContract]
        [PrincipalPermission(SecurityAction.Demand, Authenticated = true)]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/get")]
        ServiceResponse<List<CarShare>> GetCarShareListings(CarShareDTO carShareDTO);

        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/create")]
        ServiceResponse<CarShare> CreateNewCarShareListing(CarShareDTO carShareDTO);
    }
}
