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
package org.apache.axis2.transport.p2p;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;

import java.util.ArrayList;


public class SampleServiceClient {

    private boolean initilized = false;

    private int noOfClients = 10;


    ConfigurationContext cfgCtx;

    static ArrayList<org.apache.axis2.client.ServiceClient> list = new ArrayList<org.apache.axis2.client.ServiceClient>();


    private void init() throws Exception {

        for (int i = 0; i < noOfClients; i++) {

            cfgCtx = UtilServer.createClientConfigurationContext();

            list.add(new org.apache.axis2.client.ServiceClient(cfgCtx, null));

        }

        initilized = true;
    }

    public void invokeSendReceive() throws Exception {
        try {

            if (!initilized) {
                this.init();
            }

            for (int k = 0; k < list.size(); k++) {

                org.apache.axis2.client.ServiceClient serviceClient = list.get(k);

                serviceClient.setTargetEPR(new EndpointReference("p2p://localhost:6060/axis2/services/SampleService"));

                //invoking in out operation
                serviceClient.getOptions().setAction("urn:SampleInOutOperation");
                OMElement response = serviceClient.sendReceive(getOMElement());

                //serviceClient.sendReceiveNonBlocking(getOMElement(), new TestCallback());
                System.out.println("Response received for client no:   " + k + " :==> " + response.toString());

            }

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }


    }


    public void invokeFireForget() throws Exception {

        try {

            if (!initilized) {
                this.init();
            }

            for (int k = 0; k < list.size(); k++) {

                org.apache.axis2.client.ServiceClient serviceClient = list.get(k);

                serviceClient.setTargetEPR(new EndpointReference("p2p://localhost:6060/axis2/services/SampleService"));

                //invoking in out operation
                serviceClient.getOptions().setAction("urn:SampleInOutOperation");

                serviceClient.fireAndForget(getOMElement());

                //serviceClient.sendReceiveNonBlocking(getOMElement(), new TestCallback());
                System.out.println(" Sent fire and forget request from service client no : " + k);


            }

        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }

    }


    private OMElement getOMElement() {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMNamespace omNamespace = omFactory.createOMNamespace("http://sample.api", "ns1");
        OMElement omElement = omFactory.createOMElement("Request", omNamespace);
        omElement.setText("Hello service");
        return omElement;
    }


}
