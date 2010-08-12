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

package org.apache.axis2.transport.p2p.registry;

import org.apache.axis2.transport.p2p.pastry.PastryNodeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import rice.Continuation;
import rice.environment.Environment;
import rice.p2p.commonapi.Id;
import rice.p2p.past.Past;
import rice.p2p.past.PastContent;
import rice.p2p.past.PastImpl;
import rice.pastry.NodeIdFactory;
import rice.pastry.PastryNode;
import rice.pastry.PastryNodeFactory;
import rice.pastry.commonapi.PastryIdFactory;
import rice.pastry.socket.SocketPastryNodeFactory;
import rice.pastry.standard.RandomNodeIdFactory;
import rice.persistence.*;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

public class RegistryApp {

    private Past pastApp;

    private PastryIdFactory idf;

    private boolean initialized;

    private Environment env;

    private String baseDir = "pastry";

    private File storageFile = null;

    private static final Log log = LogFactory.getLog(RegistryApp.class);


    public RegistryApp( String bootIp, String bootPort, String bindPort) throws IOException, InterruptedException {

        this.initialize( bootIp, bootPort, bindPort);
    }


    private void initialize( String bootIp, String bootPort, String bindPort) throws IOException, InterruptedException {


        PastryNodeUtils nodeUtils = new PastryNodeUtils();


        int bindport;

        if (bindPort == null) {

            bindport = nodeUtils.createRandomPort();

        } else {
            bindport = Integer.parseInt(bindPort);
        }


        System.out.println("generated random port no for the Registry application :" + bindport);


        InetSocketAddress bootaddress = nodeUtils.getBootAddress(bootIp, Integer.parseInt(bootPort));


        this.env = new Environment();


        // Generate the NodeIds Randomly
        NodeIdFactory nidFactory = new RandomNodeIdFactory(env);

        PastryNodeFactory factory = null;

        if (bootaddress.getAddress().isLoopbackAddress()) {
            factory = new SocketPastryNodeFactory(nidFactory,
                    bootaddress.getAddress(), bindport, env);
        } else {
            // construct the PastryNodeFactory, this is how we use rice.pastry.socket
            factory = new SocketPastryNodeFactory(nidFactory,
                    bindport, env);
        }
        // construct a node, passing the null boothandle on the first loop will
        // cause the node to start its own ring
        PastryNode node = factory.newNode();

        // used for generating PastContent object Ids.
        // this implements the "hash function" for our DHT
        this.idf = new rice.pastry.commonapi.PastryIdFactory(env);


        // create a different storage root for each node
        String storageDirectory = "./" + baseDir + "/storage" + node.getId().hashCode();

        this.storageFile = new File(storageDirectory);

        // create the persistent part
        Storage store = new PersistentStorage(idf, storageDirectory, 4 * 1024 * 1024, node
                .getEnvironment());
        //Storage store = new MemoryStorage(idf);
        Past app = new PastImpl(node, new StorageManagerImpl(idf, store, new LRUCache(
                new MemoryStorage(idf), 512 * 1024, node.getEnvironment())), 3, "");

        this.pastApp = app;

        node.boot(bootaddress);

        // the node may require sending several messages to fully boot into the ring
        synchronized (node) {
            while (!node.isReady() && !node.joinFailed()) {
                // delay so we don't busy-wait
                node.wait(500);

                // abort if can't join
                if (node.joinFailed()) {
                    throw new IOException("Could not join the FreePastry ring.  Reason:" + node.joinFailedReason());
                }
            }
        }


        this.setInitialized(true);

        System.out.println("Finished creating new node for Registry " + node);

    }

    //TODO need to handle duplicate inserts
    public void insertIntoRegistry(String operation, Id serverId) {

        final RegistryContent myContent = new RegistryContent(idf.buildId(operation), serverId);

        System.out.println(" storing key for" + operation + "  :" + myContent.getId());

        pastApp.insert(myContent, new Continuation<Boolean[], Exception>() {
            // the result is an Array of Booleans for each insert
            public void receiveResult(Boolean[] results) {
                int numSuccessfulStores = 0;
                for (int ctr = 0; ctr < results.length; ctr++) {
                    if (results[ctr].booleanValue())
                        numSuccessfulStores++;
                }
                log.debug(myContent + " successfully stored at " + +
                        numSuccessfulStores + " locations.");
            }

            public void receiveException(Exception result) {
                log.debug("Error storing " + myContent);
                //result.printStackTrace();
            }
        });
    }


    public Id lookupRegistry(String operation) throws InterruptedException {

        final Id lookupKey = idf.buildId(operation);


        //TODO is this a good approch to assignt the result to final variable ?
        final PastContent[] resultContent = new PastContent[1];

        final BlockFlag blockFlag = new BlockFlag(true);


        pastApp.lookup(lookupKey, new Continuation<PastContent, Exception>() {

            public void receiveResult(PastContent result) {
                System.out.println("Successfully looked up Successfully looked up" + result + " for key " + lookupKey + ".");

                resultContent[0] = result;

                blockFlag.unblock();
            }

            public void receiveException(Exception result) {
                log.error("Error looking up " + lookupKey);
                result.printStackTrace();

                blockFlag.unblock();


            }
        });

        while (blockFlag.isBlocked()) {

            env.getTimeSource().sleep(200);
        }
           if(resultContent.length>0){
        return ((RegistryContent) resultContent[0]).getFirstServerId();
           }
        else{
               return null;
           }

    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }


    private class BlockFlag {
        boolean block;
        int maxBlockCount = 10;
        int count = 0;

        BlockFlag(boolean blockstaus) {
            this.block = blockstaus;

        }

        void block() {
            block = true;
        }

        void unblock() {
            block = false;
        }

        boolean isBlocked() {
            count++;
            if (maxBlockCount == count) {
                return false;
            }
            return block;
        }
    }


    public void cleanupRegistry() {

        if (env != null) {
            env.destroy();
        }

        if (storageFile != null) {

            if (deleteDirectory(new File(baseDir))) {

                log.debug("cleaned up registry file :" + storageFile);
            }
        }

    }

    static public boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }


}
