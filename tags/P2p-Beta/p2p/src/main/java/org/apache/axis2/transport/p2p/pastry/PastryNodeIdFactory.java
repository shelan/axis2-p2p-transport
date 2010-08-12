package org.apache.axis2.transport.p2p.pastry;

import rice.environment.Environment;
import rice.environment.logging.Logger;
import rice.pastry.Id;
import rice.pastry.standard.IPNodeIdFactory;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

/**
 * this classed is being used to test the initial implementaion.
 * Unlike the IpNodeIdFactroy this generates the same ID for a given IP and Port.
 * Used before the registry implementation to identify the ID of the AXIS2 sever to send the messages.
 *
 */
public class PastryNodeIdFactory extends IPNodeIdFactory {
    /**
     * Constructor
     */
    InetAddress localIP;
    int port;

    public PastryNodeIdFactory(InetAddress localIP, int port, Environment env) {
        super(localIP, port, env);

        this.localIP = localIP;
        this.port = port;

    }

    /**
     * Generates a nodeId.
     *
     * @return a new node id.
     */
    public Id generateNodeId() {

        byte rawIP[] = localIP.getAddress();

        byte rawPort[] = new byte[2];
        int tmp = port;
        for (int i = 0; i < 2; i++) {
            rawPort[i] = (byte) (tmp & 0xff);
            tmp >>= 8;
        }

        byte raw[] = new byte[4];
        tmp = 0;
        for (int i = 0; i < 4; i++) {
            raw[i] = (byte) (tmp & 0xff);
            tmp >>= 8;
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            if (logger.level <= Logger.SEVERE) logger.log("No SHA support!");
            throw new RuntimeException("No SHA support!", e);
        }

        md.update(rawIP);
        md.update(rawPort);
        md.update(raw);
        byte[] digest = md.digest();

        // now, we randomize the least significant 32 bits to ensure
        // that stale node handles are detected reliably.
        //  byte rand[] = new byte[4];
        //  rng.nextBytes(rand);
        //  for (int i=0; i<4; i++)
        //      digest[i] = rand[i];

        Id nodeId = Id.build(digest);

        return nodeId;

    }
}


