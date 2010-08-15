package org.apache.axis2.transport.p2p.pastry;

import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.axis2.transport.p2p.P2pEndpoint;
import org.apache.axis2.transport.p2p.P2pReceiveWorker;
import org.apache.axis2.transport.p2p.P2pSynchronousCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

    private boolean isClient = false;

    private ConfigurationContext configCtx;


    private int testCtr = 0;

    private static final Log log = LogFactory.getLog(PastryApp.class);


    public PastryApp(Node node,ConfigurationContext cfgCtx) {
        // We are only going to use one instance of this application on each PastryNode
        this.endpoint = node.buildEndpoint(this, "axis2App");
        this.getEndpoint().setDeserializer(new MessageDeserailizer());
        this.configCtx = cfgCtx;

        // the rest of the initialization code could go here

        // now we can receive messages
        this.getEndpoint().register();
    }



    public boolean forward(RouteMessage routeMessage) {

        System.out.println(routeMessage.getNextHopHandle().getId());
        return true;

    }

    /*
        this method will be called when a message is recieved by  the application
     */

    public void deliver(Id id, Message message) {

        testCtr++;

              log.debug("No of messages recived in the application" + endpoint.getId() + " : " + testCtr + "\n");

              PastryMsg msg = (PastryMsg) message;

              log.debug("message recieved :"+msg.getEnvelope());

              Iterator relatesToItr = msg.getEnvelope().getHeader().getChildrenWithLocalName("RelatesTo");


             {
                  try {

                      MessageContext msgContext = configCtx.createMessageContext();
                      msgContext.setEnvelope(msg.getEnvelope());

                      Map map = (Map) configCtx.getProperty(BaseConstants.CALLBACK_TABLE);

                      String messageId = null;

                      while (relatesToItr.hasNext()) {
                          SOAPHeaderBlock blk = (SOAPHeaderBlock) relatesToItr.next();
                          messageId = blk.getText();
                      }

                      if (map != null && messageId != null && map.get(messageId) != null) {

                          P2pSynchronousCallback callback = (P2pSynchronousCallback) map.get(messageId);

                          callback.setInMessageContext(msgContext);

                          map.remove(messageId);
                      }

                      else {

                   log.debug("processing added to server worker pool");

                  configCtx.getThreadPool().execute(new P2pReceiveWorker(configCtx, msg));
                      }   

                  } catch (AxisFault axisFault) {

                      log.error("error in handling the incoming message at Pastry App " , axisFault);
                  }

            }



    }


    public void update(NodeHandle nodeHandle, boolean joined) {
        if (joined) {
            log.info("Update :" + nodeHandle.getId() + "  joined the ring");
        }

        if (!joined) {
            log.info("Update :" + nodeHandle.getId() + "  left the ring");
        }
    }


    public void sendMessage(Id id, Message msg) {

        log.debug("Sending the message to :" + id);
        getEndpoint().route(id, msg, null);
    }

    public void sendMyMsgDirect(NodeHandle nh, Message msg) {

        log.debug(this + " sending direct to " + nh);

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
