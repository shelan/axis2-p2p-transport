/*
* * Licensed to the Apache Software Foundation (ASF) under one
*  * or more contributor license agreements. See the NOTICE file
*  * distributed with this work for additional information
*  * regarding copyright ownership. The ASF licenses this file
*  * to you under the Apache License, Version 2.0 (the
*  * "License"); you may not use this file except in compliance
*  * with the License. You may obtain a copy of the License at
*  *
*  * http://www.apache.org/licenses/LICENSE-2.0
*  *
*  * Unless required by applicable law or agreed to in writing,
*  * software distributed under the License is distributed on an
*  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  * KIND, either express or implied. See the License for the
*  * specific language governing permissions and limitations
*  * under the License.
*/

package org.apache.axis2.transport.p2p.pastry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import rice.environment.Environment;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.IPNodeIdFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Random;


public class PastryNodeUtils {


    private Environment env;


    protected Log log = LogFactory.getLog(this.getClass());

    /**
     * @param bootaddress
     * @param bindPort
     * @param environment -creates a new environment if null provided
     * @return
     * @throws IOException
     */
    public PastryNode createNewNode(InetSocketAddress bootaddress, int bindPort, Environment environment) throws IOException {

        if (environment == null) {
            // Loads pastry settings
            this.setEnv(new Environment());

            // disable the UPnP setting (in case you are testing this on a NATted LAN)
            getEnv().getParameters().setString("nat_search_policy", "never");
        }
        // Generate the NodeIds
        NodeIdFactory nidFactory = new IPNodeIdFactory(bootaddress.getAddress(), bindPort, getEnv());


        PastryNodeFactory factory;

        // construct the PastryNodeFactory, this is how we use rice.pastry.socket
        //check if this is a test for localhost or real deployment with realIP
        if (bootaddress.getAddress().isLoopbackAddress()) {
            factory = new SocketPastryNodeFactory(nidFactory,
                    bootaddress.getAddress(), bindPort, getEnv());
        } else {
            factory = new SocketPastryNodeFactory(nidFactory,
                    bindPort, getEnv());
        }


        //  SocketPastryNodeFactory
        // construct a node
        PastryNode node = factory.newNode();


        return node;
    }


    public InetSocketAddress getBootAddress(String bootIp, int bootPort) throws UnknownHostException {

        InetAddress bootAddress = InetAddress.getByName(bootIp);

        return new InetSocketAddress(bootAddress, bootPort);
    }

    public Environment getEnv() {
        if (env == null) {
            env = new Environment();
        }
        return env;
    }

    public void setEnv(Environment env) {
        this.env = env;
    }

    public int createRandomPort() {
        Random x = new Random();

        int rndPort = x.nextInt(64400) + 1023;


        return rndPort;

    }


}
