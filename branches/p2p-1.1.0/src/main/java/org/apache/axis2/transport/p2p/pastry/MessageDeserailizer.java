/*
 * /*
 *  * Licensed to the Apache Software Foundation (ASF) under one
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
 *  */


package org.apache.axis2.transport.p2p.pastry;

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import rice.p2p.commonapi.Message;
import rice.p2p.commonapi.NodeHandle;
import rice.p2p.commonapi.rawserialization.InputBuffer;
import rice.p2p.commonapi.rawserialization.MessageDeserializer;

import java.io.ByteArrayInputStream;
import java.io.IOException;


public class MessageDeserailizer implements MessageDeserializer {

    /*
    * @param inbuf    accessor to the bytes
    * @param type     the message type, defined in RawMessage.getType()
    * @param priority the priority of the message
    * @param sender   the sender of the Message (may be null if not specified).
    * @return The deserialized message.
    * @throws java.io.IOException
    *
    */

    public Message deserialize(InputBuffer inbuf, short type, int priority, NodeHandle sender) throws IOException {

        int remainingBytes = inbuf.bytesRemaining();
        byte[] data = new byte[remainingBytes];
        inbuf.read(data);

        StAXSOAPModelBuilder builder = null;
        SOAPEnvelope envelope = null;


        try {


            builder = new StAXSOAPModelBuilder
                    (StAXUtils.createXMLStreamReader(new ByteArrayInputStream(data)));

            envelope = builder.getSOAPEnvelope();


        } catch (Exception e) {
            System.out.println("Error while creating the Soap Envelope  ");
            e.printStackTrace();
        }


        // check for in-out messages
        if (envelope != null && sender.getId() != null) {

            return new PastryMsg(envelope, sender.getId());

        } else {
            System.out.println("Error while deserializing the message");
            return null;
        }


    }
}
