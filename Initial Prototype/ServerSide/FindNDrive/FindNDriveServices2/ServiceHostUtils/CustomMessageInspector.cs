// --------------------------------------------------------------------------------------------------------------------
// <copyright file="CustomMessageInspector.cs" company="">
//   
// </copyright>
// <summary>
//   The custom message inspector.
// </summary>
// --------------------------------------------------------------------------------------------------------------------

namespace FindNDriveServices2.ServiceHostUtils
{
    using System;
    using System.ServiceModel;
    using System.ServiceModel.Channels;
    using System.ServiceModel.Dispatcher;

    /// <summary>
    /// The custom message inspector.
    /// </summary>
    public class CustomMessageInspector : IClientMessageInspector, IDispatchMessageInspector
    {
        /// <summary>
        /// The before send request.
        /// </summary>
        /// <param name="request">
        /// The request.
        /// </param>
        /// <param name="channel">
        /// The channel.
        /// </param>
        /// <returns>
        /// The <see cref="object"/>.
        /// </returns>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public object BeforeSendRequest(ref Message request, IClientChannel channel)
        {
            throw new NotImplementedException();
        }

        /// <summary>
        /// The after receive reply.
        /// </summary>
        /// <param name="reply">
        /// The reply.
        /// </param>
        /// <param name="correlationState">
        /// The correlation state.
        /// </param>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public void AfterReceiveReply(ref Message reply, object correlationState)
        {
            throw new NotImplementedException();
        }

        /// <summary>
        /// The after receive request.
        /// </summary>
        /// <param name="request">
        /// The request.
        /// </param>
        /// <param name="channel">
        /// The channel.
        /// </param>
        /// <param name="instanceContext">
        /// The instance context.
        /// </param>
        /// <returns>
        /// The <see cref="object"/>.
        /// </returns>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public object AfterReceiveRequest(ref Message request, IClientChannel channel, InstanceContext instanceContext)
        {
            throw new NotImplementedException();
        }

        /// <summary>
        /// The before send reply.
        /// </summary>
        /// <param name="reply">
        /// The reply.
        /// </param>
        /// <param name="correlationState">
        /// The correlation state.
        /// </param>
        /// <exception cref="NotImplementedException">
        /// </exception>
        public void BeforeSendReply(ref Message reply, object correlationState)
        {
            throw new NotImplementedException();
        }
    }

}