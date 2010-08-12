package org.apache.axis2.transport.p2p;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.base.AbstractTransportListener;

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
public class P2pListner extends AbstractTransportListener {

    private P2pManager p2pmanger;


    @Override
    public void init(ConfigurationContext cfgCtx, TransportInDescription transportIn) throws AxisFault {
        super.init(cfgCtx, transportIn);
        try {

            String started = (String) cfgCtx.getProperty(P2pConstants.PASTRY_NODE_STARTED);

            if (started == null || started.equals("started")) {

                P2pManager manager = new P2pManager();
                manager.initAxis2ServerNode(transportIn, cfgCtx);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void startListeningForService(AxisService axisService) throws AxisFault {

    }

    @Override
    protected void stopListeningForService(AxisService axisService) {

    }
}
