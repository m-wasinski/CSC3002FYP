using System;
using System.Collections.Generic;
using System.Linq;
using System.ServiceModel;
using System.ServiceModel.Channels;
using System.ServiceModel.Dispatcher;
using System.Web;

namespace FindNDriveServices2
{
    public class CustomMessageInspector : IClientMessageInspector
    {
        public object BeforeSendRequest(ref Message request, IClientChannel channel)
        {
            HttpRequestMessageProperty prop;
            if (request.Properties.ContainsKey(HttpRequestMessageProperty.Name))
            {
                prop = (HttpRequestMessageProperty)request.Properties[HttpRequestMessageProperty.Name];
            }
            else
            {
                prop = new HttpRequestMessageProperty();
                request.Properties.Add(HttpRequestMessageProperty.Name, prop);
            }

            prop.Headers["Content-Type"] = "text/xml; charset=UTF-8";
            prop.Headers["PropertyOne"] = "One";
            prop.Headers["PropertyTwo"] = "Two";
            prop.Headers["PropertyTwo"] = "Three";
            prop.Headers["PropertyTwo"] = "Four";

            return prop;
        }

        public void AfterReceiveReply(ref Message reply, object correlationState)
        {
            throw new NotImplementedException();
        }
    }
}