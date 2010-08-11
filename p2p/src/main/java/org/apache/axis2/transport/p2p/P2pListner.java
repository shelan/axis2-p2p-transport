package org.apache.axis2.transport.p2p;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.base.AbstractTransportListenerEx;

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
public class P2pListner extends AbstractTransportListenerEx<P2pEndpoint> {

    private P2pManager p2pmanger;

    protected void doInit() throws AxisFault {

    }

    @Override
    protected P2pEndpoint createEndpoint() {
        return new P2pEndpoint();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void startEndpoint(P2pEndpoint protocolEndpoint) throws AxisFault {
        this.p2pmanger = new P2pManager();
        try {
            p2pmanger.initAxis2ServerNode(protocolEndpoint, workerPool);
        } catch (Exception e) {
            handleException("Error while starting the P2P endpoint", e);
        }
    }

    @Override
    protected void stopEndpoint(P2pEndpoint protocolEndpoint) {
        if (p2pmanger != null)
            try {
                p2pmanger.stopPastryNode();
            } catch (Exception e) {

                log.error("Error while stopping the P2P endpoint", e);
            }
    }
}
