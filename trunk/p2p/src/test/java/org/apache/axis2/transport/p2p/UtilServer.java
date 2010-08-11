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

package org.apache.axis2.transport.p2p;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: shelan
 * Date: 9 Aug, 2010
 * Time: 5:54:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class UtilServer {


    private static P2pListner receiver;

    private static int ctr=0;


    public static void startServer() throws Exception {
        // start tcp server

        if(ctr<1){

            File file = new File(prefixBaseDirectory(Constants.TESTING_REPOSITORY));
            System.out.println(file.getAbsoluteFile());
            if (!file.exists()) {
                 System.out.println("file doesnot exist");
                throw new Exception("Repository directory does not exist");

            }
        ConfigurationContext er =
                ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                        file.getAbsolutePath() + "/repository",
                        file.getAbsolutePath() + "/conf/axis2.xml");

            receiver = new P2pListner();

            receiver.init(er, er.getAxisConfiguration().getTransportIn(P2pConstants.TRANSPORT_P2P));

            receiver.start();

    //        receiver.startEndpoint(receiver.createEndpoint());



            ctr ++;
        }

    }


    public static void stop() throws AxisFault {

        receiver.stop();
        receiver.getConfigurationContext().terminate();

        ctr = 0 ;
    }

    public static String prefixBaseDirectory(String path) {
        String baseDir;
        try {
            baseDir = new File(System.getProperty("basedir", ".")).getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baseDir + "/" + path;
    }

    public static ConfigurationContext createClientConfigurationContext() throws Exception {
        File file = new File(prefixBaseDirectory(Constants.TESTING_REPOSITORY));
        ConfigurationContext configContext =
            ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                    file.getAbsolutePath(), file.getAbsolutePath() + "/conf/client_axis2.xml");
        return configContext;
    }

}
