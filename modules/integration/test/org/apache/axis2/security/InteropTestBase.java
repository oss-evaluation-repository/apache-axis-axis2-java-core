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

package org.apache.axis2.security;

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.security.handler.config.InflowConfiguration;
import org.apache.axis2.security.handler.config.OutflowConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public abstract class InteropTestBase extends TestCase {

    protected static final String SCENARIO1_SERVICE_REPOSITORY = "scenario1_service_repo";

    protected static final String SCENARIO1_CLIENT_REPOSITORY = "scenario1_client_repo";

    protected static final String SCENARIO2_SERVICE_REPOSITORY = "scenario2_service_repo";

    protected static final String SCENARIO2_CLIENT_REPOSITORY = "scenario2_client_repo";

    protected static final String SCENARIO2a_SERVICE_REPOSITORY = "scenario2a_service_repo";

    protected static final String SCENARIO2a_CLIENT_REPOSITORY = "scenario2a_client_repo";

    protected static final String SCENARIO3_SERVICE_REPOSITORY = "scenario3_service_repo";

    protected static final String SCENARIO3_CLIENT_REPOSITORY = "scenario3_client_repo";

    protected static final String SCENARIO4_SERVICE_REPOSITORY = "scenario4_service_repo";

    protected static final String SCENARIO4_CLIENT_REPOSITORY = "scenario4_client_repo";

    protected static final String SCENARIO5_SERVICE_REPOSITORY = "scenario5_service_repo";

    protected static final String SCENARIO5_CLIENT_REPOSITORY = "scenario5_client_repo";

    protected static final String SCENARIO6_SERVICE_REPOSITORY = "scenario6_service_repo";

    protected static final String SCENARIO6_CLIENT_REPOSITORY = "scenario6_client_repo";

    protected static final String SCENARIO7_SERVICE_REPOSITORY = "scenario7_service_repo";

    protected static final String SCENARIO7_CLIENT_REPOSITORY = "scenario7_client_repo";

    protected static final String SCENARIO_ST1_SERVICE_REPOSITORY = "scenarioST1_service_repo";

    protected static final String SCENARIO_ST1_CLIENT_REPOSITORY = "scenarioST1_client_repo";

    protected static final String SCENARIO_ST3_SERVICE_REPOSITORY = "scenarioST3_service_repo";

    protected static final String SCENARIO_ST3_CLIENT_REPOSITORY = "scenarioST3_client_repo";

    protected static final String SCENARIO_ST4_SERVICE_REPOSITORY = "scenarioST4_service_repo";

    protected static final String SCENARIO_ST4_CLIENT_REPOSITORY = "scenarioST4_client_repo";

    protected static final String MTOM_SEC_SERVICE_REPOSITORY = "mtom_sec_service_repo";

    protected static final String MTOM_SEC_CLIENT_REPOSITORY = "mtom_sec_client_repo";

    protected static final String COMPLETE_SERVICE_REPOSITORY = "complete_service_repo";

    protected static final String COMPLETE_CLIENT_REPOSITORY = "complete_client_repo";

    protected static final String DEFAULT_CLIENT_REPOSITORY = "default_security_client_repo";

    private String targetEpr = "http://127.0.0.1:" +
            //5556 +
            UtilServer.TESTING_PORT +
            "/axis2/services/PingPort";

    public InteropTestBase() {
        super();
    }

    public InteropTestBase(String arg0) {
        super(arg0);
    }

    /**
     * Each time an interop test is run the relevant service
     * will be started with the given service repository
     *
     * set up the service
     */
    protected void setUp() throws Exception {
        UtilServer.start(Constants.TESTING_PATH + getServiceRepo());
    }

    /**
     * Cleanup
     */
    protected void tearDown() throws Exception {
        UtilServer.stop();
    }


    /**
     * Do test
     */
    public void testInteropWithConfigFiles() {
        try {
            Class interopScenarioClientClass =  Class.forName("org.apache.axis2.security.InteropScenarioClient");
            Constructor c = interopScenarioClientClass.getConstructor(new Class[]{boolean.class});
            Object clientObj = c.newInstance(new Object[]{this.isUseSOAP12InStaticConfigTest()?Boolean.TRUE:Boolean.FALSE});
            Method m = interopScenarioClientClass.getMethod("invokeWithStaticConfig",new Class[]{String.class,String.class});
            m.invoke(clientObj,new Object[]{Constants.TESTING_PATH + getClientRepo(),targetEpr});

        } catch (Exception e) {
            e.printStackTrace();
            fail("Error in introperating with " + targetEpr + ", client configuration: " + getClientRepo());
        }
    }

//    public void testInteropWithDynamicConfig() {
//        try {
//            Class interopScenarioClientClass =  Class.forName("org.apache.axis2.security.InteropScenarioClient");
//            Constructor c = interopScenarioClientClass.getConstructor(new Class[]{boolean.class});
//            Object clientObj = c.newInstance(new Object[]{this.isUseSOAP12InStaticConfigTest()?Boolean.TRUE:Boolean.FALSE});
//            Method m = interopScenarioClientClass.getMethod("invokeWithGivenConfig",new Class[]{String.class,
//                    String.class,
//                    OutflowConfiguration.class,
//                    InflowConfiguration.class
//            });
//            m.invoke(clientObj,new Object[]{Constants.TESTING_PATH + getClientRepo(),targetEpr,getOutflowConfiguration(), getInflowConfiguration()});
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail("Error in introperating with " + targetEpr + ", client configuration: " + getClientRepo());
//        }
//
//    }

    protected abstract OutflowConfiguration getOutflowConfiguration();

    protected abstract InflowConfiguration getInflowConfiguration();

    protected abstract String getClientRepo();

    protected abstract String getServiceRepo();

    protected abstract boolean isUseSOAP12InStaticConfigTest();
}
