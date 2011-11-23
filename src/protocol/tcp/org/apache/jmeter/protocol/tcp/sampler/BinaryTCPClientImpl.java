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
 * TCP Sampler Client implementation which reads and writes binary data.
 *
 * Input/Output strings are passed as hex-encoded binary strings.
 *
 */
package org.apache.jmeter.protocol.tcp.sampler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * TCPClient implementation.
 * Reads data until the defined EOM byte is reached.
 * If there is no EOM byte defined, then reads until
 * the end of the stream is reached.
 * The EOM byte is defined by the property "tcp.BinaryTCPClient.eomByte".
 *
 * Input data is assumed to be in hex, and is converted to binary
 */
public class BinaryTCPClientImpl extends AbstractTCPClient {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final int eomInt = JMeterUtils.getPropDefault("tcp.BinaryTCPClient.eomByte", 1000); // $NON_NLS-1$

    public BinaryTCPClientImpl() {
        super();
        setEolByte(eomInt);
        if (useEolByte) {
            log.info("Using eomByte=" + eolByte);
        }
    }

    /**
     * Convert hex string to binary byte array.
     *
     * @param hexEncodedBinary - hex-encoded binary string
     * @return Byte array containing binary representation of input hex-encoded string
     * @throws IllegalArgumentException if string is not an even number of hex digits
     */
    public static final byte[] hexStringToByteArray(String hexEncodedBinary) {
        if (hexEncodedBinary.length() % 2 == 0) {
            char[] sc = hexEncodedBinary.toCharArray();
            byte[] ba = new byte[sc.length / 2];

            for (int i = 0; i < ba.length; i++) {
                int nibble0 = Character.digit(sc[i * 2], 16);
                int nibble1 = Character.digit(sc[i * 2 + 1], 16);
                if (nibble0 == -1 || nibble1 == -1){
                    throw new IllegalArgumentException(
                    "Hex-encoded binary string contains an invalid hex digit in '"+sc[i * 2]+sc[i * 2 + 1]+"'");
                }
                ba[i] = (byte) ((nibble0 << 4) | (nibble1));
            }

            return ba;
        } else {
            throw new IllegalArgumentException(
                    "Hex-encoded binary string contains an uneven no. of digits");
        }
    }

    /**
     * Input (hex) string is converted to binary and written to the output stream.
     * @param os output stream
     * @param hexEncodedBinary hex-encoded binary
     */
    public void write(OutputStream os, String hexEncodedBinary) throws IOException{
        os.write(hexStringToByteArray(hexEncodedBinary));
        os.flush();
        if(log.isDebugEnabled()) {
            log.debug("Wrote: " + hexEncodedBinary);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void write(OutputStream os, InputStream is) {
        throw new UnsupportedOperationException(
                "Method not supported for Length-Prefixed data.");
    }

    /**
     * Reads data until the defined EOM byte is reached.
     * If there is no EOM byte defined, then reads until
     * the end of the stream is reached.
     * Response data is converted to hex-encoded binary
     * @return hex-encoded binary string
     * @throws ReadException 
     */
    public String read(InputStream is) throws ReadException {
    	ByteArrayOutputStream w = new ByteArrayOutputStream();
        try {
			byte[] buffer = new byte[4096];
			int x = 0;
			while ((x = is.read(buffer)) > -1) {
			    w.write(buffer, 0, x);
			    if (useEolByte && (buffer[x - 1] == eolByte)) {
			        break;
			    }
			}

			IOUtils.closeQuietly(w); // For completeness
			final String hexString = JOrphanUtils.baToHexString(w.toByteArray());
			if(log.isDebugEnabled()) {
			    log.debug("Read: " + w.size() + "\n" + hexString);
			}
			return hexString;
		} catch (IOException e) {
			throw new ReadException("", e, JOrphanUtils.baToHexString(w.toByteArray()));
		}
    }

}
