using System.ServiceModel;
using System.ServiceModel.Web;
using DomainObjects;
using FindNDriveServices.DTOs;
using FindNDriveServices.ServiceResponses;

namespace FindNDriveServices.Contracts
{
    [ServiceContract]
    public interface ICarShareService
    {
        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/carshares/get")]
        ServiceResponse<CarShare> GetCarShareListings(CarShareDTO carShareDTO);

        [OperationContract]
        [WebInvoke(Method = "POST",
            ResponseFormat = WebMessageFormat.Json,
            RequestFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Bare,
            UriTemplate = "/carshares/create")]
        ServiceResponse<CarShare> CreateNewCarShareListing(CarShareDTO carShareDTO);
    }
}
