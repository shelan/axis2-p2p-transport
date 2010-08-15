/*
 * * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements. See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership. The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License. You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied. See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 */

package org.apache.axis2.transport.p2p;

/**
 * Created by IntelliJ IDEA.
 * User: shelan
 * Date: 9 Aug, 2010
 * Time: 4:53:17 PM
 * To change this template use File | Settings | File Templates.
 */


import junit.framework.TestCase;
import org.apache.axis2.addressing.EndpointReference;

public class Test extends TestCase {


    private EndpointReference targetEPR =
            new EndpointReference("p2p://127.0.0.1/"

                    + "/axis2/services/EchoXMLService/echoOMElement");

    SampleServiceClient client;


    public Test() {
        super(Test.class.getName());
    }

    @Override
    protected void setUp() throws Exception {

        UtilServer.startServer();

        client = new SampleServiceClient();


    }

    @Override
    protected void tearDown() throws Exception {

        UtilServer.stop();

    }

    public void testSendAndReceive() throws Exception {

        client.invokeSendReceive();
    }

    public void testFireAndForget() throws Exception {

        client.invokeFireForget();
    }
}
