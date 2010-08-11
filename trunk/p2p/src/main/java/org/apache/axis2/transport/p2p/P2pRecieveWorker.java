package org.apache.axis2.transport.p2p;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.p2p.pastry.PastryMsg;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import java.io.ByteArrayInputStream;
import java.io.InputStream;


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
public class P2pRecieveWorker implements Runnable {

    private PastryMsg msg;
    private P2pEndpoint endpoint;
    private MessageContext msgContext = null;

    private static final Log log = LogFactory.getLog(P2pManager.class);

    public P2pRecieveWorker(P2pEndpoint endpoint, PastryMsg msg) {

        this.msg = msg;
        this.endpoint = endpoint;
    }

    public void run() {

        try {

            msgContext = endpoint.createMessageContext();
            msgContext.setIncomingTransportName(P2pConstants.TRANSPORT_P2P);
            msgContext.setServerSide(true);
            msgContext.setConfigurationContext(endpoint.getListener().getConfigurationContext());

            System.out.println("Message received from:" + msg.getSender());
            log.debug("Message received from:" + msg.getSender());

            P2pOutTransportInfo outInfo = new P2pOutTransportInfo();
            outInfo.setReciever(msg.getSender());
            outInfo.setContentType(endpoint.getContentType());

            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outInfo);
            msgContext.setEnvelope(msg.getEnvelope());

            AxisEngine.receive(msgContext);


        } catch (Exception e) {
           log.error("Error while processing P2P request through the Axis2 engine", e);
        }


    }


}
 