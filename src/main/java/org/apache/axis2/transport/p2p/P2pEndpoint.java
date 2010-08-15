package org.apache.axis2.transport.p2p;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.ParamUtils;
import org.apache.axis2.transport.base.ProtocolEndpoint;
import rice.p2p.commonapi.Application;
import rice.pastry.Id;

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
public class P2pEndpoint extends ProtocolEndpoint {

    //Pastry node id
    private Id nodeId;

    private String contentType;

    private String bootIp = "0.0.0.0";

    private int bootPort = -1;

    private int bindPort = -1;

    private Application application;


    public P2pEndpoint() {

    }

    @Override
    public boolean loadConfiguration(ParameterInclude parameterInclude) throws AxisFault {


        bootIp = ParamUtils.getOptionalParam(parameterInclude, P2pConstants.BOOT_IP);

        if (bootIp == null) {
            bootIp = "0.0.0.0";
            return false;
        }


        bootPort = ParamUtils.getOptionalParamInt(parameterInclude, P2pConstants.BOOT_PORT, -1);

        bindPort = ParamUtils.getOptionalParamInt(parameterInclude, P2pConstants.BIND_PORT, -1);

        if (bootPort == -1) {
            return false;
        }


        contentType = ParamUtils.getOptionalParam(parameterInclude, P2pConstants.PARAM_CONTENT_TYPE);
        if (getContentType() == null) {
            contentType = P2pConstants.P2P_DEFAULT_CONTENT_TYPE;
        }


        return true;
    }

    public EndpointReference[] getEndpointReferences(AxisService service,
                                                     String ip) throws AxisFault {

        if (nodeId != null) {
            String url = "p2p://" + nodeId;

            String context = getListener().getConfigurationContext().getServiceContextPath();
            url += (context.startsWith("/") ? "" : "/") + context +
                    (context.endsWith("/") ? "" : "/") +
                    (getService() == null ? service.getName() : getServiceName());
            return new EndpointReference[]{new EndpointReference(url)};

        } else {
            return new EndpointReference[0];
        }

    }


    public Id getNodeId() {
        return nodeId;
    }

    public void setNodeId(Id nodeId) {
        this.nodeId = nodeId;
    }

    public String getContentType() {
        return contentType;
    }


    public String getBootIp() {
        return bootIp;
    }

    public int getBootPort() {
        return bootPort;
    }

    public int getBindPort() {
        return bindPort;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }
}
