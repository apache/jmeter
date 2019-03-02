/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/*
 * TCP Sampler Client implementation which reads and writes length-prefixed binary data.
 *
 * Input/Output strings are passed as hex-encoded binary strings.
 *
 * 2-Byte or 4-Byte length prefixes are supported.
 *
 * Length prefix is binary of length specified by property "tcp.length.prefix.length".
 *
 */
package org.apache.jmeter.protocol.tcp.sampler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements binary length-prefixed binary data.
 * This is used in ISO8583 for example.
 */
public class LengthPrefixedBinaryTCPClientImpl extends TCPClientDecorator {
    private static final Logger log = LoggerFactory.getLogger(LengthPrefixedBinaryTCPClientImpl.class);

    private final int lengthPrefixLen = JMeterUtils.getPropDefault("tcp.binarylength.prefix.length", 2); // $NON-NLS-1$

    public LengthPrefixedBinaryTCPClientImpl() {
        super(new BinaryTCPClientImpl());
        tcpClient.setEolByte(Byte.MAX_VALUE+1);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void write(OutputStream os, String s)  throws IOException{
        os.write(intToByteArray(s.length()/2,lengthPrefixLen));
        if(log.isDebugEnabled()) {
            log.debug("Wrote: " + s.length()/2 + " bytes");
        }
        this.tcpClient.write(os, s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(OutputStream os, InputStream is) throws IOException {
        this.tcpClient.write(os, is);
    }

    @Deprecated
    public String read(InputStream is) throws ReadException {
        log.warn("Deprecated method, use read(is, sampleResult) instead");
        return read(is, new SampleResult());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String read(InputStream is, SampleResult sampleResult) throws ReadException{
        byte[] msg = new byte[0];
        int msgLen = 0;
        byte[] lengthBuffer = new byte[lengthPrefixLen];
        try {
            if (is.read(lengthBuffer, 0, lengthPrefixLen) == lengthPrefixLen) {
                sampleResult.latencyEnd();
                msgLen = byteArrayToInt(lengthBuffer);
                msg = new byte[msgLen];
                int bytes = JOrphanUtils.read(is, msg, 0, msgLen);
                if (bytes < msgLen) {
                    log.warn("Incomplete message read, expected: {} got: {}", msgLen, bytes);
                }
            }

            String buffer = JOrphanUtils.baToHexString(msg);
            if(log.isDebugEnabled()) {
                log.debug("Read: " + msgLen + "\n" + buffer);
            }
            return buffer;
        }
        catch(IOException e) {
            throw new ReadException("", e, JOrphanUtils.baToHexString(msg));
        }
    }

    /**
     * Not useful, as the byte is never used.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public byte getEolByte() {
        return tcpClient.getEolByte();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEolByte(int eolInt) {
        throw new UnsupportedOperationException("Cannot set eomByte for prefixed messages");
    }
}
