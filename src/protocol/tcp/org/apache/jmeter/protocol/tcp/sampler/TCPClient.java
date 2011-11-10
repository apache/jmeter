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
 * Created on 24-Sep-2003
 *
 * Interface for generic TCP protocol handler
 *
 */
package org.apache.jmeter.protocol.tcp.sampler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Interface required by TCPSampler for TCPClient implementations.
 */
public interface TCPClient {

    /**
     * Versions of JMeter after 2.3.2 invoke this method when the thread starts.
     */
    void setupTest();

    /**
     * Versions of JMeter after 2.3.2 invoke this method when the thread ends.
     */
    void teardownTest();

    /**
     *
     * @param os -
     *            OutputStream for socket
     * @param is -
     *            InputStream to be written to Socket
     */
    void write(OutputStream os, InputStream is) throws IOException;

    /**
     *
     * @param os -
     *            OutputStream for socket
     * @param s -
     *            String to write
     */
    void write(OutputStream os, String s) throws IOException;

    /**
     *
     * @param is -
     *            InputStream for socket
     * @return String read from socket
     * @throws ReadException exception that can contain partial response (Response until error occured)
     */
    String read(InputStream is) throws ReadException;

    /**
     * Get the end-of-line/end-of-message byte.
     * @return Returns the eolByte.
     */
    public byte getEolByte();

    /**
     * Set the end-of-line/end-of-message byte.
     * If the value is out of range of a byte, then it is to be ignored.
     *
     * @param eolInt
     *            The value to set
     */
    public void setEolByte(int eolInt);
}