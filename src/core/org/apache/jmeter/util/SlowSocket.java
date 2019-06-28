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

package org.apache.jmeter.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * "Slow"  (non-SSL) socket implementation to emulate dial-up modems etc
 */
public class SlowSocket extends Socket {

    private final int charactersPerSecond; // Characters per second to emulate

    public SlowSocket(final int cps, String host, int port, InetAddress localAddress, int localPort, int timeout) throws IOException {
        super();
        if (cps <=0) {
            throw new IllegalArgumentException("Speed (cps) <= 0");
        }
        charactersPerSecond=cps;
        // This sequence is borrowed from:
        // org.apache.commons.httpclient.protocol.ReflectionSocketFactory.createSocket
        SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
        SocketAddress remoteaddr = new InetSocketAddress(host, port);
        bind(localaddr);
        connect(remoteaddr, timeout);
    }

    /**
     *
     * @param cps
     *            characters per second
     * @param host
     *            hostname
     * @param port
     *            port
     * @param localAddr
     *            local address
     * @param localPort
     *            local port
     *
     * @throws IOException
     *             if an I/O error occurs during initialization
     * @throws IllegalArgumentException
     *             if cps &lt;= 0, or if the <code>port</code> or
     *             <code>localPort</code> values lie outside of the allowed
     *             range between <code>0</code> and <code>65535</code>
     */
    public SlowSocket(int cps, String host, int port, InetAddress localAddr, int localPort) throws IOException {
        super(host, port, localAddr, localPort);
        if (cps <=0) {
            throw new IllegalArgumentException("Speed (cps) <= 0");
        }
        charactersPerSecond=cps;
    }

    /**
     *
     * @param cps
     *            characters per second
     * @param host
     *            hostname
     * @param port
     *            port
     *
     * @throws UnknownHostException
     *             if the name of the host can not be determined automatically
     * @throws IOException
     *             if an I/O error occurs during initialization
     * @throws IllegalArgumentException
     *             if cps &lt;= 0, or if the <code>port</code> or
     *             <code>localPort</code> values lie outside of the allowed
     *             range between <code>0</code> and <code>65535</code>
     */
    public SlowSocket(int cps, String host, int port) throws UnknownHostException, IOException {
        super(host, port);
        if (cps <=0) {
            throw new IllegalArgumentException("Speed (cps) <= 0");
        }
        charactersPerSecond=cps;
    }

    /**
     * Added for use by SlowHC4SocketFactory.
     *
     * @param cps characters per second
     */
    public SlowSocket(int cps) {
        super();
        charactersPerSecond = cps;
    }

    // Override so we can intercept the stream
    @Override
    public OutputStream getOutputStream() throws IOException {
        return new SlowOutputStream(super.getOutputStream(), charactersPerSecond);
    }

    // Override so we can intercept the stream
    @Override
    public InputStream getInputStream() throws IOException {
        return new SlowInputStream(super.getInputStream(), charactersPerSecond);
    }

}
