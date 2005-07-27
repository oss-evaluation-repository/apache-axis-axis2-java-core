/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.integration;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.transport.tcp.TCPServer;
import org.apache.axis2.AxisFault;

import javax.xml.namespace.QName;
import java.io.File;

public class UtilsTCPServer {
    private static int count = 0;
    private static TCPServer receiver;

    private static ConfigurationContext configurationContext;
    public static final int TESTING_PORT = 5555;
    public static final String FAILURE_MESSAGE = "Intentional Faliure";

    public static synchronized void deployService(ServiceDescription service)
            throws AxisFault {
        configurationContext.getAxisConfiguration().addService(service);

    }

    public static synchronized void unDeployService(QName service)
            throws AxisFault {
        configurationContext.getAxisConfiguration().removeService(service);
    }

    public static synchronized void start() throws Exception {
        if (count == 0) {

            //start tcp server

            ConfigurationContextFactory erfac =
                    new ConfigurationContextFactory();
            File file = new File(org.apache.axis2.Constants.TESTING_REPOSITORY);
            System.out.println(file.getAbsoluteFile());
            if (!file.exists()) {
                throw new Exception("repository directory does not exists");
            }

            configurationContext =
                    erfac.buildConfigurationContext(file.getAbsolutePath());
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                throw new AxisFault("Thread interuptted", e1);
            }
            configurationContext.getAxisConfiguration().engageModule(
                    new QName("addressing"));
            receiver =
                    new TCPServer(UtilServer.TESTING_PORT,
                            configurationContext);
            receiver.start();

        }
        count++;
    }

    public static synchronized void stop() {
        try {
            if (count == 1) {
                receiver.stop();
                count = 0;
                System.out.print("Server stopped .....");
            } else {
                count--;
            }
        } catch (AxisFault e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }
}
