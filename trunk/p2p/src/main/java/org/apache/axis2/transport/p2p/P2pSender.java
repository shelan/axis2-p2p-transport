package org.apache.axis2.transport.p2p;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.OutInAxisOperation;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.AbstractTransportSender;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.p2p.pastry.PastryApp;
import org.apache.axis2.transport.p2p.pastry.PastryMsg;
import org.apache.axis2.transport.p2p.registry.RegistryApp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import rice.environment.Environment;
import rice.p2p.commonapi.Id;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

public class P2pSender extends AbstractTransportSender {

    protected Log log = LogFactory.getLog(this.getClass());

    Environment env;

    private boolean initialized = false;

    ConfigurationContext configCtx;

    private PastryApp app;

    private int testNum = 0;

    private SOAPEnvelope soapEnv;

    private RegistryApp registryApp;

    private P2pManager manager;


    /**
     * Initialize the generic transport sender.
     *
     * @param cfgCtx       the axis configuration context
     * @param transportOut the transport-out description
     * @throws org.apache.axis2.AxisFault on error
     */
    @Override
    public void init(ConfigurationContext cfgCtx, TransportOutDescription transportOut) throws AxisFault {
        super.init(cfgCtx, transportOut);
        this.configCtx = cfgCtx;

        // set the synchronise callback table
        if (configCtx.getProperty(BaseConstants.CALLBACK_TABLE) == null) {
            configCtx.setProperty(BaseConstants.CALLBACK_TABLE, new ConcurrentHashMap());
        }

        try {

            String started = (String) cfgCtx.getProperty(P2pConstants.PASTRY_NODE_STARTED);

            if (started == null || started.equals("started")) {

                manager = new P2pManager();
                manager.initAxis2ServerNode(transportOut, cfgCtx);
            }
        }
        catch (Exception e) {
            log.error("Error while starting pastry node at the sender", e);
        }

    }

    @Override
    public void sendMessage(MessageContext messageContext, String targetEpr, OutTransportInfo outTransportInfo) throws AxisFault {

        soapEnv = messageContext.getEnvelope();

        // if client invokes the inflow
        if (targetEpr != null) {

            if (app == null) {
                app = (PastryApp) configCtx.getProperty(P2pConstants.PASTRY_APP);
            }

            String key = getServiceName(targetEpr) + ":" + getOperationFromSOAPHeader(soapEnv);


            registryApp = (RegistryApp) configCtx.getProperty(P2pConstants.PASTRY_REGISTRY_APP);


            Id availableServer = null;

            try {
                availableServer = registryApp.lookupRegistry(key);
            } catch (InterruptedException e) {
                handleException("Error in Service registry look up");
            }

            if(availableServer == null){
                log.error("No Server available with the Operation  "+key);
            }

            PastryMsg msg = new PastryMsg(soapEnv, app.getEndpoint().getId());

            if (availableServer != null) {
                app.sendMessage(availableServer, msg);


                if (!messageContext.getOptions().isUseSeparateListener() && !messageContext.isServerSide()) {

                    waitForReply(messageContext);

                }
            }

        }


        //if axis engine invokes the outflow
        else if (outTransportInfo != null && (outTransportInfo instanceof P2pOutTransportInfo)) {

            ConfigurationContext configCtx = messageContext.getConfigurationContext();

            PastryApp app = (PastryApp) configCtx.getProperty(P2pConstants.PASTRY_APP);

            PastryMsg msg = new PastryMsg(messageContext.getEnvelope());

            app.sendMessage(((P2pOutTransportInfo) outTransportInfo).getReciever(), msg);

        }

    }

    public void waitForReply(MessageContext msgContext) throws AxisFault {

        if (!(msgContext.getAxisOperation() instanceof OutInAxisOperation) &&
                msgContext.getProperty(org.apache.axis2.Constants.PIGGYBACK_MESSAGE) == null) {

            return;
        }

        P2pSynchronousCallback synchronousCallback = new P2pSynchronousCallback(msgContext);
        Map callBackMap = (Map) msgContext.getConfigurationContext().
                getProperty(BaseConstants.CALLBACK_TABLE);

        callBackMap.put(msgContext.getMessageID(), synchronousCallback);

        synchronized (synchronousCallback) {
            try {
                synchronousCallback.wait(msgContext.getOptions().getTimeOutInMilliSeconds());
            } catch (InterruptedException e) {
                handleException("Error occured while waiting ..", e);
            }

            if (!synchronousCallback.isComplete()) {
                // when timeout occurs remove this entry.
                callBackMap.remove(msgContext.getMessageID());
                handleException("Timeout while waiting for a response");
            }

        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    /**
     * this method will be used to connect the sender to the pastry ring and initialize.
     * this method will be called only from a client.
     */

    


    @Override
    public void stop() {

       if(manager != null){
           manager.stopPastryNode();
       }
    }


    private String getOperationFromSOAPHeader(SOAPEnvelope envelope) {
        Iterator iterator = envelope.getHeader().getChildrenWithLocalName("Action");


        String operation = null;

        while (iterator.hasNext()) {

            SOAPHeaderBlock blk = (SOAPHeaderBlock) iterator.next();

            operation = blk.getText();
        }

        String[] operationString = operation.split(":");


        if (operationString == null || operationString.length < 2) {
            log.error("Cannot find the urn:Action in the SOAP request");
        }

        return operationString[1];
    }


    private String getServiceName(String epr) throws AxisFault {

        String parts[] = epr.split("/");

        String service = null;

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("services")) {

                //check if there is an element after /services/
                if (i + 1 < parts.length) {
                    service = parts[i + 1];
                }
            }
        }

        if (service == null) {
            handleException("Malformed EPR : missing service name");
        }

        return service;
    }

}
