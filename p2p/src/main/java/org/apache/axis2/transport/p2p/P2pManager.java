package org.apache.axis2.transport.p2p;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.base.threads.WorkerPool;
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


    public synchronized void initAxis2ServerNode(P2pEndpoint endpoint, WorkerPool workerpool) throws IOException,
            InterruptedException {

        if (!isInitialized()) {

            this.configurationContext = endpoint.getListener().getConfigurationContext();

            PastryNodeUtils nodeUtils = new PastryNodeUtils();


            // getting pastry related information
            String bootIp = endpoint.getBootIp();

            int bootPort = endpoint.getBootPort();

            int bindPort = endpoint.getBindPort();

            if (bindPort == -1) {

                bindPort = nodeUtils.createRandomPort();
            }


            InetSocketAddress bootaddress = nodeUtils.getBootAddress(bootIp, bootPort);


            PastryNode node = nodeUtils.createNewNode(bootaddress, bindPort, null);

            // construct a new MyApp
            PastryApp app = new PastryApp(node, endpoint, workerpool);

            node.boot(bootaddress);

            endpoint.setNodeId(node.getNodeId());

            // to check if this is the bootstrap node
            configurationContext.setProperty("axis2ServerApp", app);

            configurationContext.setProperty(P2pConstants.PASTRY_NODE_STARTED, true);

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

            registryApp = new RegistryApp(configurationContext, bootIp,
                    Integer.toString(bootPort + 1), Integer.toString(bootPort + 1));


            final Id serverId = app.getEndpoint().getId();

            insertMetaDataToRegistry(configurationContext, registryApp, serverId);

            initialized = true;

            log.info("Application for the Axis2Server started " + app.getEndpoint().getId());
        }
    }


    public void stopPastryNode() {

        if (env != null) {
            env.destroy();

            if(registryApp != null){
                registryApp.cleanupRegistry();
            }

            log.info("P2P endpoint stopped ");
        }

    }

    //this is to initialize the sender code

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

        cfgCtx.setProperty("axis2ClientApp", app);

        cfgCtx.setProperty(P2pConstants.PASTRY_ENVIRONMENT, nodeUtils.getEnv());

        // getting the next port num
        String regBootPort = String.valueOf(Integer.parseInt(bootPort) + 1);

        log.debug("registry boot ip and port" + bootIp + ":" + regBootPort);

        registryApp = new RegistryApp(cfgCtx, bootIp, regBootPort);

        cfgCtx.setProperty("registryApp", registryApp);


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

                System.out.println();

                log.debug("Inserted Service:Operation key :  " + key);

            }


        }

    }


}


