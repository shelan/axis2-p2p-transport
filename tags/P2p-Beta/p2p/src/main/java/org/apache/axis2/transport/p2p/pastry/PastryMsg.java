package org.apache.axis2.transport.p2p.pastry;

import org.apache.axiom.soap.SOAPEnvelope;
import rice.p2p.commonapi.Id;
import rice.p2p.commonapi.rawserialization.OutputBuffer;
import rice.p2p.commonapi.rawserialization.RawMessage;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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
public class PastryMsg implements RawMessage {

    private SOAPEnvelope envelope;

    private Id sender;

    // processed at the axis engine


    public PastryMsg(SOAPEnvelope envelope, Id sender) {
        this.envelope = envelope;
        this.sender = sender;
    }

    public PastryMsg(SOAPEnvelope envelope) {
        this.envelope = envelope;

    }

    public int getPriority() {
        return 0;
    }

    public short getType() {
        return 1;
    }

    public void serialize(OutputBuffer buf) throws IOException {


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            getEnvelope().serialize(outputStream);
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }

        outputStream.flush();

        byte[] dataAsBytes = outputStream.toByteArray();

        buf.write(dataAsBytes, 0, dataAsBytes.length);


    }

    public SOAPEnvelope getEnvelope() {
        return envelope;
    }

    public Id getSender() {
        return sender;
    }

}
