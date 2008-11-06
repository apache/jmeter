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
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Implements binary length-prefixed binary data.
 * This is used in ISO8583 for example.
 */
public class LengthPrefixedBinaryTCPClientImpl extends TCPClientDecorator {
    private static final Logger log = LoggingManager.getLoggerForClass();
    
    private final int lengthPrefixLen = JMeterUtils.getPropDefault("tcp.binarylength.prefix.length", 2); // $NON-NLS-1$

    public LengthPrefixedBinaryTCPClientImpl() {
        super(new BinaryTCPClientImpl());
        tcpClient.setEolByte(Byte.MAX_VALUE+1);
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.protocol.tcp.sampler.TCPClient#write(java.io.OutputStream
     * , java.lang.String)
     */
    public void write(OutputStream os, String s) {
        try {
            os.write(intToByteArray(s.length()/2,lengthPrefixLen));
            log.debug("Wrote: " + s.length()/2 + " bytes");
            this.tcpClient.write(os, s);
        } catch (IOException e) {
            log.warn("Write error", e);
        }
        return;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.protocol.tcp.sampler.TCPClient#write(java.io.OutputStream
     * , java.io.InputStream)
     */
    public void write(OutputStream os, InputStream is) {
        this.tcpClient.write(os, is);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.protocol.tcp.sampler.TCPClient#read(java.io.InputStream
     * )
     */
    public String read(InputStream is) {
        byte[] msg = new byte[0];
        int msgLen = 0;
        try {
            byte[] lengthBuffer = new byte[lengthPrefixLen];
            if (is.read(lengthBuffer, 0, lengthPrefixLen) == lengthPrefixLen) {
                msgLen = byteArrayToInt(lengthBuffer);
                msg = new byte[msgLen];
                is.read(msg);
            }
            /*
             * Timeout is reported as follows: JDK1.3: InterruptedIOException
             * JDK1.4: SocketTimeoutException, which extends
             * InterruptedIOException
             * 
             * So to make the code work on both, just check for
             * InterruptedIOException
             * 
             * If 1.3 support is dropped, can change to using
             * SocketTimeoutException
             * 
             * For more accurate detection of timeouts under 1.3, one could
             * perhaps examine the Exception message text...
             */
        } catch (SocketTimeoutException e) {
            // drop out to handle buffer
        } catch (InterruptedIOException e) {
            // drop out to handle buffer
        } catch (IOException e) {
            log.warn("Read error:" + e);
            return JOrphanUtils.baToHexString(msg);
        }

        String buffer = JOrphanUtils.baToHexString(msg);
        log.debug("Read: " + msgLen + "\n" + buffer);
        return buffer;
    }

    /**
     * Not useful, as the byte is never used.
     * 
     * @return Returns the eolByte.
     */
    public byte getEolByte() {
        return tcpClient.getEolByte();
    }

    /**
     * @param eolInt
     *            The eolByte to set.
     */
    public void setEolByte(int eolInt) {
        throw new UnsupportedOperationException("Cannot set eomByte for prefixed messages");
    }
}
