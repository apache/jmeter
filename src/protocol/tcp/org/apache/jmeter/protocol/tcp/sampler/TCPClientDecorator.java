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
 * TCP Sampler Client decorator to permit wrapping base client implementations with length prefixes.
 * For example, character data or binary data with character length or binary length
 *
 */
package org.apache.jmeter.protocol.tcp.sampler;

public abstract class TCPClientDecorator extends AbstractTCPClient {

    protected final TCPClient tcpClient; // the data implementation

    public TCPClientDecorator(TCPClient tcpClient) {
        this.tcpClient = tcpClient;
    }

    /**
     * Convert int to byte array.
     *
     * @param value
     *            - int to be converted
     * @param len
     *            - length of required byte array
     * @return Byte array representation of input value
     * @throws IllegalArgumentException if not length 2 or 4 or outside range of a short int.
     */
    public static byte[] intToByteArray(int value, int len) {
        if (len == 2 || len == 4) {
            if (len == 2 && (value < Short.MIN_VALUE || value > Short.MAX_VALUE)) {
                throw new IllegalArgumentException("Value outside range for signed short int.");
            } else {
                byte[] b = new byte[len];
                for (int i = 0; i < len; i++) {
                    int offset = (b.length - 1 - i) * 8;
                    b[i] = (byte) ((value >>> offset) & 0xFF);
                }
                return b;
            }
        } else {
            throw new IllegalArgumentException(
                    "Length must be specified as either 2 or 4.");
        }
    }

    /**
     * Convert byte array to int.
     *
     * @param b
     *            - Byte array to be converted
     * @return Integer value of input byte array
     * @throws IllegalArgumentException if ba is null or not length 2 or 4
     */
    public static int byteArrayToInt(byte[] b) {
        if (b != null && (b.length == 2 || b.length == 4)) {
            // Preserve sign on first byte
            int value = b[0] << ((b.length - 1) * 8);

            for (int i = 1; i < b.length; i++) {
                int offset = (b.length - 1 - i) * 8;
                value += (b[i] & 0xFF) << offset;
            }
            return value;
        } else {
            throw new IllegalArgumentException(
                    "Byte array is null or invalid length.");
        }
    }
}
