/*
 * Copyright 2006 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.axis2.jaxws.client;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import javax.xml.ws.AsyncHandler;
import javax.xml.ws.Response;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.jaxws.BindingProvider;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.client.async.AsyncResponse;
import org.apache.axis2.jaxws.core.InvocationContext;
import org.apache.axis2.jaxws.core.InvocationContextFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.core.controller.AxisInvocationController;
import org.apache.axis2.jaxws.core.controller.InvocationController;
import org.apache.axis2.jaxws.handler.PortData;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.impl.AsyncListener;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.message.MessageException;
import org.apache.axis2.jaxws.message.XMLFault;
import org.apache.axis2.jaxws.spi.ServiceDelegate;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BaseDispatch<T> extends BindingProvider 
    implements javax.xml.ws.Dispatch {

    private Log log = LogFactory.getLog(BaseDispatch.class);
    
    protected InvocationController ic;
    protected ServiceDelegate serviceDelegate;
    protected ServiceClient serviceClient;
    protected Mode mode;
    protected PortData port;
    
    protected BaseDispatch(PortData p) {
        super();
        
        port = p;
        ic = new AxisInvocationController();
        setRequestContext();
    }
    
    /**
     * Take the input object and turn it into an OMElement so that it can
     * be sent.
     * 
     * @param value
     * @return
     */
    protected abstract Message createMessageFromValue(Object value);
    
    /**
     * Given a message, return the business object based on the requestor's
     * required format (PAYLOAD vs. MESSAGE) and datatype.
     * 
     * @param message
     * @return
     */
    protected abstract Object getValueFromMessage(Message message);
    
    /**
     * Creates an instance of the AsyncListener that is to be used for waiting
     * for async responses.
     * 
     * @return a configured AsyncListener instance
     */
    protected abstract AsyncResponse createAsyncResponseListener();
    
    public Object invoke(Object obj) throws WebServiceException {
        if (log.isDebugEnabled()) { 
            log.debug("Entered synchronous invocation: BaseDispatch.invoke()");
        }
        
        // Create the InvocationContext instance for this request/response flow.
        InvocationContext invocationContext = InvocationContextFactory.createInvocationContext(null);
        invocationContext.setServiceClient(serviceClient);
        
        // Create the MessageContext to hold the actual request message and its
        // associated properties
        MessageContext requestMsgCtx = new MessageContext();
        invocationContext.setRequestMessageContext(requestMsgCtx);
        
        Message requestMsg = null;
        if (isValidInvocationParam(obj)) {
            requestMsg = createMessageFromValue(obj);
        }
        else {
            throw ExceptionFactory.makeWebServiceException("dispatchInvalidParam");
        }
        
        setupMessageProperties(requestMsg);
        requestMsgCtx.setMessage(requestMsg);            
            
        // Copy the properties from the request context into the MessageContext
        requestMsgCtx.getProperties().putAll(requestContext);
        
        // Send the request using the InvocationController
        ic.invoke(invocationContext);
        
        MessageContext responseMsgCtx = invocationContext.getResponseMessageContext();
        
        //FIXME: This is temporary until more of the Message model is available
        Message responseMsg = responseMsgCtx.getMessage();
        try {
            if (responseMsg.isFault()) {
                XMLFault fault = responseMsg.getXMLFault();
                throw ExceptionFactory.makeWebServiceException(fault.getReason().getText());
            }
            else if (responseMsg.getLocalException() != null) {
                // use the factory, it'll throw the right thing:
                throw ExceptionFactory.makeWebServiceException(responseMsg.getLocalException());
            }
        } catch (MessageException e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        
        Object returnObj = getValueFromMessage(responseMsg);
        
        //Check to see if we need to maintain session state
        if (requestMsgCtx.isMaintainSession()) {
            //TODO: Need to figure out a cleaner way to make this call. 
            setupSessionContext(invocationContext.getServiceClient().getServiceContext().getProperties());
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Synchronous invocation completed: BaseDispatch.invoke()");
        }
        
        return returnObj;
    }
    
    public void invokeOneWay(Object obj) throws WebServiceException{
        if (log.isDebugEnabled()) { 
            log.debug("Entered one-way invocation: BaseDispatch.invokeOneWay()");
        }
        
        // Create the InvocationContext instance for this request/response flow.
        InvocationContext invocationContext = InvocationContextFactory.createInvocationContext(null);
        invocationContext.setServiceClient(serviceClient);
       
        // Create the MessageContext to hold the actual request message and its
        // associated properties
        MessageContext requestMsgCtx = new MessageContext();
        invocationContext.setRequestMessageContext(requestMsgCtx);
       
        Message requestMsg = null;
        if (isValidInvocationParam(obj)) {
            requestMsg = createMessageFromValue(obj);
        }
        else {
            throw ExceptionFactory.makeWebServiceException("dispatchInvalidParam");
        }
        
        setupMessageProperties(requestMsg);
        requestMsgCtx.setMessage(requestMsg);
       
        // Copy the properties from the request context into the MessageContext
        requestMsgCtx.getProperties().putAll(requestContext);
       
        // Send the request using the InvocationController
        ic.invokeOneWay(invocationContext);
        
        //Check to see if we need to maintain session state
        if (requestMsgCtx.isMaintainSession()) {
            //TODO: Need to figure out a cleaner way to make this call. 
            setupSessionContext(invocationContext.getServiceClient().getServiceContext().getProperties());
        }
       
        if (log.isDebugEnabled()) {
            log.debug("One-way invocation completed: BaseDispatch.invokeOneWay()");
        }
       
        return;
    }
   
    public Future<?> invokeAsync(Object obj, AsyncHandler asynchandler) throws WebServiceException {
        if (log.isDebugEnabled()) { 
            log.debug("Entered asynchronous (callback) invocation: BaseDispatch.invokeAsync()");
        }
        
        // Create the InvocationContext instance for this request/response flow.
        InvocationContext invocationContext = InvocationContextFactory.createInvocationContext(null);
        invocationContext.setServiceClient(serviceClient);
        
        // Create the MessageContext to hold the actual request message and its
        // associated properties
        MessageContext requestMsgCtx = new MessageContext();
        invocationContext.setRequestMessageContext(requestMsgCtx);
        
        Message requestMsg = null;
        if (isValidInvocationParam(obj)) {
            requestMsg = createMessageFromValue(obj);
        }
        else {
            throw ExceptionFactory.makeWebServiceException("dispatchInvalidParam");
        }
        
        setupMessageProperties(requestMsg);
        requestMsgCtx.setMessage(requestMsg);
        
        // Copy the properties from the request context into the MessageContext
        requestMsgCtx.getProperties().putAll(requestContext);

        // Setup the Executor that will be used to drive async responses back to 
        // the client.
        // FIXME: We shouldn't be getting this from the ServiceDelegate, rather each 
        // Dispatch object should have it's own.
        Executor e = serviceDelegate.getExecutor();
        invocationContext.setExecutor(e);
        
        // Create the AsyncListener that is to be used by the InvocationController.
        AsyncResponse listener = createAsyncResponseListener();
        invocationContext.setAsyncResponseListener(listener);
        
        // Send the request using the InvocationController
        Future<?> asyncResponse = ic.invokeAsync(invocationContext, asynchandler);
        
        //Check to see if we need to maintain session state
        if (requestMsgCtx.isMaintainSession()) {
            //TODO: Need to figure out a cleaner way to make this call. 
            setupSessionContext(invocationContext.getServiceClient().getServiceContext().getProperties());
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Asynchronous (callback) invocation sent: BaseDispatch.invokeAsync()");
        }
        
        return asyncResponse;
    }
  
    public Response invokeAsync(Object obj)throws WebServiceException{
        if (log.isDebugEnabled()) { 
            log.debug("Entered asynchronous (polling) invocation: BaseDispatch.invokeAsync()");
        }
        
        // Create the InvocationContext instance for this request/response flow.
        InvocationContext invocationContext = InvocationContextFactory.createInvocationContext(null);
        invocationContext.setServiceClient(serviceClient);
        
        // Create the MessageContext to hold the actual request message and its
        // associated properties
        MessageContext requestMsgCtx = new MessageContext();
        invocationContext.setRequestMessageContext(requestMsgCtx);
        
        Message requestMsg = null;
        if (isValidInvocationParam(obj)) {
            requestMsg = createMessageFromValue(obj);
        }
        else {
            throw ExceptionFactory.makeWebServiceException("dispatchInvalidParam");
        }
        
        setupMessageProperties(requestMsg);
        requestMsgCtx.setMessage(requestMsg);
        
        // Copy the properties from the request context into the MessageContext
        requestMsgCtx.getProperties().putAll(requestContext);

        // Setup the Executor that will be used to drive async responses back to 
        // the client.
        // FIXME: We shouldn't be getting this from the ServiceDelegate, rather each 
        // Dispatch object should have it's own.
        Executor e = serviceDelegate.getExecutor();
        invocationContext.setExecutor(e);
        
        // Create the AsyncListener that is to be used by the InvocationController.
        AsyncResponse listener = createAsyncResponseListener();
        invocationContext.setAsyncResponseListener(listener);
        
        // Send the request using the InvocationController
        Response asyncResponse = ic.invokeAsync(invocationContext);
        
        //Check to see if we need to maintain session state
        if (requestMsgCtx.isMaintainSession()) {
            //TODO: Need to figure out a cleaner way to make this call. 
            setupSessionContext(invocationContext.getServiceClient().getServiceContext().getProperties());
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Asynchronous (polling) invocation sent: BaseDispatch.invokeAsync()");
        }
        
        return asyncResponse;
    }
    
    //FIXME: This needs to be moved up to the BindingProvider and should actually
    //be called "initRequestContext()" or something like that.
    protected void setRequestContext(){
        String endPointAddress = port.getEndpointAddress();
        //WSDLWrapper wsdl =  axisController.getWSDLContext();
        //QName serviceName = axisController.getServiceName();
        //QName portName = axisController.getPortName();
        
        
        if(endPointAddress != null && !"".equals(endPointAddress)){
            getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endPointAddress);
        }
        //else if(wsdl != null){
        //    String soapAddress = wsdl.getSOAPAddress(serviceName, portName);
        //    getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, soapAddress);
        //}
        
        //if(wsdl != null){
        //    String soapAction = wsdl.getSOAPAction(serviceName, portName);
        //    getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);
        //}
    }
    
    public ServiceDelegate getServiceDelegate() {
        return serviceDelegate;
    }
    
    public void setServiceDelegate(ServiceDelegate sd) {
        serviceDelegate = sd;
    }
    
    public void setServiceClient(ServiceClient sc) {
        serviceClient = sc;
    }
    
    public Mode getMode() {
        return mode;
    }
    
    public void setMode(Mode m) {
        mode = m;
    }

    public PortData getPort() {
        return port;
    }
    
    /*
     * Configure any properties that will be needed on the Message
     */
    private void setupMessageProperties(Message msg) {
        // If the user has enabled MTOM on the SOAPBinding, we need
        // to make sure that gets pushed to the Message object.
        if (binding != null && binding instanceof SOAPBinding) {
            SOAPBinding soapBinding = (SOAPBinding) binding;
            if (soapBinding.isMTOMEnabled())
                msg.setMTOMEnabled(true);
        }
        
        // Check if the user enabled MTOM using the SOAP binding 
        // properties for MTOM
        String bindingID = this.port.getBindingID();
        if((bindingID.equalsIgnoreCase(SOAPBinding.SOAP11HTTP_MTOM_BINDING) ||
        	bindingID.equalsIgnoreCase(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) &&
        	!msg.isMTOMEnabled()){
        	msg.setMTOMEnabled(true);
        }
    }
    
    /*
     * Validate the invocation param for the Dispatch.  There are 
     * some cases when nulls are allowed and others where it's 
     * a violation.
     */
    private boolean isValidInvocationParam(Object object){
        String bindingId = port.getBindingID();
        
        // If no bindingId was found, use the default.
        if (bindingId == null) {
            bindingId = SOAPBinding.SOAP11HTTP_BINDING;
        }
        
        // If it's not an HTTP_BINDING, then we can allow for null params,  
        // but only in PAYLOAD mode per JAX-WS Section 4.3.2.
        if (!bindingId.equals(HTTPBinding.HTTP_BINDING)) { 
            if (mode.equals(Mode.MESSAGE) && object == null) {
                throw ExceptionFactory.makeWebServiceException("dispatchNullParamMessageMode");
            }
        }
        else {
            // In all cases (PAYLOAD and MESSAGE) we must throw a WebServiceException
            // if the parameter is null.
            if (object == null) {
                throw ExceptionFactory.makeWebServiceException("dispatchNullParamHttpBinding");
            }
        }
        
        // If we've gotten this far, then all is good.
        return true;
    }
}
