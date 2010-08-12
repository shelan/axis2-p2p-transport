package org.apache.axis2.transport.p2p;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.*;
import org.apache.axis2.transport.p2p.pastry.PastryApp;
import org.apache.axis2.transport.p2p.pastry.PastryNodeUtils;
import org.apache.axis2.transport.p2p.registry.RegistryApp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.pastry.PastryNode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Iterator;

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

public class P2pManager {


    private boolean initialized;

    private ConfigurationContext configurationContext;

    private Environment env;

    private static final Log log = LogFactory.getLog(P2pManager.class);

    private RegistryApp registryApp;


    public synchronized void initAxis2ServerNode
            (ParameterInclude transportDesciption, ConfigurationContext cfgCtx)
            throws IOException, InterruptedException {

        if (!isInitialized()) {

            this.configurationContext = cfgCtx;

            PastryNodeUtils nodeUtils = new PastryNodeUtils();


            if (transportDesciption.getParameter(P2pConstants.BOOT_IP) == null &&
                    transportDesciption.getParameter(P2pConstants.BOOT_PORT) == null) {

                return;
            }


            // getting pastry related information
            String bootIp = (String) transportDesciption.getParameter(P2pConstants.BOOT_IP).getValue();

            String bootport = (String) transportDesciption.getParameter(P2pConstants.BOOT_PORT).getValue();

            Parameter bindport = transportDesciption.getParameter(P2pConstants.BIND_PORT);

            int bindPort;

            // no value is given
            if (bindport == null) {

                bindPort = nodeUtils.createRandomPort();
            } else {
                bindPort = Integer.parseInt((String) bindport.getValue());
            }

            int bootPort = Integer.parseInt(bootport);


            InetSocketAddress bootaddress = nodeUtils.getBootAddress(bootIp, bootPort);


            PastryNode node = nodeUtils.createNewNode(bootaddress, bindPort, null);

            // construct a new MyApp
            PastryApp app = new PastryApp(node, cfgCtx);

            node.boot(bootaddress);

            // to check if this is the bootstrap node
            cfgCtx.setProperty(P2pConstants.PASTRY_APP, app);

            // the node may require sending several messages to fully boot into the ring
            synchronized (node) {
                while (!node.isReady() && !node.joinFailed()) {
                    // delay so we don't busy-wait
                    node.wait(500);

                    // abort if can't join
                    if (node.joinFailed()) {
                        throw new IOException("Could not join the FreePastry ring.  Reason:" +
                                node.joinFailedReason());
                    }
                }
            }

            this.env = nodeUtils.getEnv();

            log.info("Finished creating new node for AXIS2 server :" + node);

            // adding operations into the registry

            if (bootPort == bindPort) {

                registryApp = new RegistryApp(bootIp,
                        Integer.toString(bootPort + 1), Integer.toString(bootPort + 1));
            } else {
                registryApp = new RegistryApp(bootIp,
                        Integer.toString(bootPort + 1), null);
            }

            configurationContext.setProperty(P2pConstants.PASTRY_REGISTRY_APP ,registryApp);


            Parameter insertEnabled = transportDesciption.getParameter(P2pConstants.REGISTRY_INSERT_ENABLED);

            // Insert only if it is a server App
            if (insertEnabled != null && (insertEnabled.getValue()).equals("true")) {

                final Id serverId = app.getEndpoint().getId();

                insertMetaDataToRegistry(configurationContext, registryApp, serverId);
            }


            initialized = true;

            cfgCtx.setProperty(P2pConstants.PASTRY_NODE_STARTED, "started");

            log.info("Application for the Axis2Server started " + app.getEndpoint().getId());
        }
    }


    public void stopPastryNode() {

        if (env != null) {
            env.destroy();

            if (registryApp != null) {
                registryApp.cleanupRegistry();
            }

            log.info("P2P endpoint stopped ");
        }

    }

    //this is to initialize the sender code
    @Deprecated
    public void initSenderNode(ConfigurationContext cfgCtx) throws IOException, InterruptedException {

        PastryNodeUtils nodeUtils = new PastryNodeUtils();

        TransportInDescription transportInDetails = cfgCtx
                .getAxisConfiguration().getTransportIn(P2pConstants.TRANSPORT_P2P);

        String bootIp = (String) transportInDetails.
                getParameter(P2pConstants.BOOT_IP).getValue();

        String bootPort = (String) transportInDetails.
                getParameter(P2pConstants.BOOT_PORT).getValue();

        String bindPort;

        if (transportInDetails.getParameter(P2pConstants.BIND_PORT) != null) {

            bindPort = (String) transportInDetails.getParameter(P2pConstants.BIND_PORT).getValue();
        } else {

            int rndPort = nodeUtils.createRandomPort();

            bindPort = Integer.toString(rndPort);

            log.debug(" generated random port no :" + rndPort);
        }


        InetSocketAddress bootAddress = nodeUtils.getBootAddress(bootIp, Integer.parseInt(bootPort));

        PastryNode node = nodeUtils.createNewNode(bootAddress, Integer.parseInt(bindPort), null);

        PastryApp app = new PastryApp(node, cfgCtx);


        node.boot(bootAddress);

        // the node may require sending several messages to fully boot into the ring
        synchronized (node) {
            while (!node.isReady() && !node.joinFailed()) {
                // delay so we don't busy-wait
                node.wait(500);

                // abort if can't join
                if (node.joinFailed()) {
                    throw new IOException("Could not join the FreePastry ring.  Reason:" +
                            node.joinFailedReason());
                }
            }
        }

        this.env = nodeUtils.getEnv();

        log.info("Finished creating new node " + node);

        cfgCtx.setProperty(P2pConstants.PASTRY_SENDER_APP, app);

        cfgCtx.setProperty(P2pConstants.PASTRY_ENVIRONMENT, nodeUtils.getEnv());

        // getting the next port num
        String regBootPort = String.valueOf(Integer.parseInt(bootPort) + 1);

        log.debug("registry boot ip and port" + bootIp + ":" + regBootPort);

        registryApp = new RegistryApp( bootIp, regBootPort ,null);

        cfgCtx.setProperty(P2pConstants.PASTRY_REGISTRY_APP, registryApp);


        log.info("Application for the Sender started " + app.getEndpoint().getId());


    }


    public boolean isInitialized() {
        return initialized;
    }

    /**
     * this will insert serviceName:operation combinations into the WS-registry for later look up
     *
     * @param cfgCtx
     * @param regApp
     * @param serverId
     */
    private void insertMetaDataToRegistry(ConfigurationContext cfgCtx, RegistryApp regApp, Id serverId) {

        Collection<AxisService> e = cfgCtx.getAxisConfiguration().getServices().values();

        Iterator<AxisService> serviceItr = e.iterator();


        while (serviceItr.hasNext()) {
            AxisService service = serviceItr.next();

            Iterator<AxisOperation> operationItr = service.getOperations();

            while (operationItr.hasNext()) {
                AxisOperation operation = operationItr.next();

                String key = service.getName() + ":" + operation.getName().toString();

                regApp.insertIntoRegistry(key, serverId);


                log.debug("Inserted Service:Operation key :  " + key);

            }


        }

    }


}


