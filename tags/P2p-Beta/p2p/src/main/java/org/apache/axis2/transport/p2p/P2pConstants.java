package org.apache.axis2.transport.p2p;

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
public class P2pConstants {

    public static final String TRANSPORT_P2P = "p2p";
    //port used to boot the ring
    public static final String BOOT_PORT = "transport.p2p.bootport";
    //IP used to Boot IP
    public static final String BOOT_IP = "transport.p2p.bootip";
    //Port used to bind
    public static final String BIND_PORT = "transport.p2p.bindport";

    public static final String PARAM_CONTENT_TYPE = "transport.p2p.contentType";

    public static final String P2P_DEFAULT_CONTENT_TYPE = "text/xml";


    //pastry Related

    public static final String PASTRY_NODE_STARTED = "pastryNodeStarted";

    public static final String PASTRY_ENVIRONMENT = "pastryEnviornment";

    public static final String PASTRY_SERVER_APP = "pastryAxis2App";

    public static final String PASTRY_SENDER_APP = "pastrySenderApp";

     public static final String PASTRY_REGISTRY_APP = "pastryRegistryApp";


}


