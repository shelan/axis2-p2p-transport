package org.apache.axis2.transport.p2p;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.p2p.pastry.PastryMsg;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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
public class P2pReceiveWorker implements Runnable {

    private PastryMsg msg;
    private ConfigurationContext cfgCtx;
    private MessageContext msgContext = null;

    private static final Log log = LogFactory.getLog(P2pManager.class);

    public P2pReceiveWorker(ConfigurationContext cfgCtx, PastryMsg msg) {

        this.msg = msg;
        this.cfgCtx = cfgCtx;
    }

    public void run() {

        try {

            msgContext = cfgCtx.createMessageContext();
            msgContext.setIncomingTransportName(P2pConstants.TRANSPORT_P2P);
            msgContext.setServerSide(true);
            msgContext.setConfigurationContext(cfgCtx);

            TransportOutDescription out = cfgCtx.getAxisConfiguration().getTransportOut(P2pConstants.TRANSPORT_P2P);
            msgContext.setTransportOut(out);

            System.out.println("Message received from:" + msg.getSender());
            log.debug("Message received from:" + msg.getSender());

            P2pOutTransportInfo outInfo = new P2pOutTransportInfo();
            outInfo.setReciever(msg.getSender());
            outInfo.setContentType(P2pConstants.P2P_DEFAULT_CONTENT_TYPE);


            msgContext.setProperty(Constants.OUT_TRANSPORT_INFO, outInfo);
            msgContext.setEnvelope(msg.getEnvelope());

            AxisEngine.receive(msgContext);


        } catch (Exception e) {
            log.error("Error while processing P2P request through the Axis2 engine", e);
        }


    }


}
 