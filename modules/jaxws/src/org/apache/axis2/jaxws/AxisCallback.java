/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.jaxws;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.MessageContext;

public class AxisCallback extends Callback {

    private SOAPEnvelope responseEnv;
    private MessageContext responseMsgCtx;
    
    public void onComplete(AsyncResult result) {
        responseEnv = result.getResponseEnvelope();
        responseMsgCtx = result.getResponseMessageContext();
    }

    public void onError(Exception e) {
        e.printStackTrace();
    }
    
    public SOAPEnvelope getSOAPEnvelope() {
        return responseEnv;
    }
    
    public MessageContext getResponseMessageContext() {
        return responseMsgCtx;
    }
}
