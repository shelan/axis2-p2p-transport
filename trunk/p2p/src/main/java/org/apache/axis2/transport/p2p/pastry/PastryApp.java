package org.apache.axis2.transport.p2p.pastry;

import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.p2p.P2pEndpoint;
import org.apache.axis2.transport.p2p.P2pRecieveWorker;
import org.apache.axis2.transport.p2p.P2pSynchronousCallback;
import rice.p2p.commonapi.*;

import java.util.Iterator;
import java.util.Map;


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

public class PastryApp implements Application {
    // pastry endpoint
    private Endpoint endpoint;

    // transport endpoint
    private P2pEndpoint p2pEndpoint;

    private WorkerPool workerPool;

    private boolean isClient = false;

    private ConfigurationContext configCtx;


    private int testCtr = 0;


    public PastryApp(Node node, P2pEndpoint p2pEndpoint, WorkerPool workerpool) {
        // We are only going to use one instance of this application on each PastryNode
        this.endpoint = node.buildEndpoint(this, "axis2App");
        this.p2pEndpoint = p2pEndpoint;
        this.workerPool = workerpool;
        this.getEndpoint().setDeserializer(new MessageDeserailizer());
        this.configCtx = p2pEndpoint.getListener().getConfigurationContext();


        // the rest of the initialization code could go here

        // now we can receive messages
        this.getEndpoint().register();
    }

    /**
     * this constructor will be used by the client
     *
     * @param node   node of the app
     * @param cfgCtx configuration context
     */
    public PastryApp(Node node, ConfigurationContext cfgCtx) {
        this.endpoint = node.buildEndpoint(this, "axis2App");
        this.getEndpoint().setDeserializer(new MessageDeserailizer());

        // : anyone using this constructor will be treated as a client so message will not be processed
        this.isClient = true;
        this.getEndpoint().register();
        this.configCtx = cfgCtx;

    }


    public boolean forward(RouteMessage routeMessage) {

        System.out.println(routeMessage.getNextHopHandle().getId());
        return true;

    }

    /*
        this method will be called when a message is recieved by  the application
     */

    public void deliver(Id id, Message message) {


        PastryMsg msg = (PastryMsg) message;

        testCtr++;

        // System.out.println("Recieved msg :"+testCtr+"    \n" + msg.getEnvelope());

        System.out.println("No of messages recived :" + testCtr + "\n");

        if (!isClient) {

            System.out.println("processing add to server worker pool");
            workerPool.execute(new P2pRecieveWorker(p2pEndpoint, msg));
        } else {


            try {
                MessageContext msgContext = configCtx.createMessageContext();
                msgContext.setEnvelope(msg.getEnvelope());

                Map map = (Map) configCtx.getProperty(BaseConstants.CALLBACK_TABLE);

                Iterator iterator = msg.getEnvelope().getHeader().getChildrenWithLocalName("RelatesTo");


                String messageId = null;

                while (iterator.hasNext()) {

                    SOAPHeaderBlock blk = (SOAPHeaderBlock) iterator.next();
                    System.out.println("elements : " + blk.getText());
                    messageId = blk.getText();
                }

                if (map != null && messageId != null && map.get(messageId) != null) {

                    P2pSynchronousCallback callback = (P2pSynchronousCallback) map.get(messageId);

                    callback.setInMessageContext(msgContext);

                    map.remove(messageId);
                }

                /** check how the incoming is handled at the client side   and remove this code
                 else{
                 AxisEngine.receive(msgContext);
                 }
                 **/
            } catch (AxisFault axisFault) {

                axisFault.printStackTrace();
            }


        }


        System.out.println("processed");
    }


    public void update(NodeHandle nodeHandle, boolean joined) {
        if (joined) {
            System.out.println("Update :" + nodeHandle.getId() + "  joined the ring");
        }

        if (!joined) {
            System.out.println("Update :" + nodeHandle.getId() + "  left the ring");
        }
    }


    public void sendMessage(Id id, Message msg) {

        System.out.println("Sending the message to :" + id);
        getEndpoint().route(id, msg, null);
    }

    public void sendMyMsgDirect(NodeHandle nh, Message msg) {

        System.out.println(this + " sending direct to " + nh);

        getEndpoint().route(null, msg, nh);
    }


    /**
     * The Endpoint represents the underlieing node.  By making calls on the
     * Endpoint, it assures that the message will be delivered to a MyApp on whichever
     * node the message is intended for.
     *
     * @return endpoint
     */
    public Endpoint getEndpoint() {
        return endpoint;
    }


}
