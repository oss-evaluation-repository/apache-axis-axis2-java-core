/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.transport.http.impl.httpclient4;

import java.io.IOException;
import java.net.URL;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.Request;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.AbstractHttpClient;

class GetRequest implements Request {
    private static final Log log = LogFactory.getLog(GetRequest.class);

    private final HTTPSenderImpl sender;
    private final MessageContext msgContext;
    private final String soapActionString;
    private final URL url;

    GetRequest(HTTPSenderImpl sender, MessageContext msgContext, String soapActionString, URL url) {
        this.sender = sender;
        this.msgContext = msgContext;
        this.soapActionString = soapActionString;
        this.url = url;
    }

    @Override
    public void execute() throws AxisFault {
        HttpGet method = new HttpGet();
        AbstractHttpClient httpClient = sender.getHttpClient(msgContext);
        MessageFormatter messageFormatter = sender.populateCommonProperties(msgContext, url, method,
                                                                     httpClient, soapActionString);

        // Need to have this here because we can have soap action when using the
        // soap response MEP
        String soapAction = messageFormatter
                .formatSOAPAction(msgContext, sender.getFormat(), soapActionString);

        if (soapAction != null && !msgContext.isDoingREST()) {
            method.setHeader(HTTPConstants.HEADER_SOAP_ACTION, soapAction);
        }

        /*
         * main execution takes place..
         */
        HttpResponse response = null;
        try {
            response = sender.executeMethod(httpClient, msgContext, url, method);
            sender.handleResponse(msgContext, response);
        } catch (IOException e) {
            log.info("Unable to sendViaGet to url[" + url + "]", e);
            throw AxisFault.makeFault(e);
        } finally {
            sender.cleanup(msgContext, response);
        }
    }
}