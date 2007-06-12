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

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

/**
 * "Slow" socket implementation to emulate dial-up modems etc
 */
public class SlowSocket extends Socket {

    private final int CPS; // Characters per second to emulate
    
    public SlowSocket(final int cps, String host, int port, InetAddress localAddress, int localPort, int timeout) throws IOException {
    	super();
        if (cps <=0) {
            throw new IllegalArgumentException("Speed (cps) <= 0");
        }
        CPS=cps;
        // This sequence is borrowed from:
        // org.apache.commons.httpclient.protocol.ReflectionSocketFactory.createSocket
        SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
        SocketAddress remoteaddr = new InetSocketAddress(host, port);
        bind(localaddr);
        connect(remoteaddr, timeout);
	}

    /**
     * 
     * @param cps characters per second
     * @param host hostname
     * @param port port
     * @param localAddr local address
     * @param localPort local port
     * 
     * @throws IOException
     * @throws IllegalArgumentException if cps <=0
     */
    public SlowSocket(int cps, String host, int port, InetAddress localAddr, int localPort) throws IOException {
        super(host, port, localAddr, localPort);
        if (cps <=0) {
            throw new IllegalArgumentException("Speed (cps) <= 0");
        }
        CPS=cps;
    }

    /**
     * 
     * @param cps characters per second
     * @param host hostname
     * @param port port
     * 
     * @throws UnknownHostException
     * @throws IOException
     * @throws IllegalArgumentException if cps <=0
     */
    public SlowSocket(int cps, String host, int port) throws UnknownHostException, IOException {
        super(host, port);
        if (cps <=0) {
            throw new IllegalArgumentException("Speed (cps) <= 0");
        }
        CPS=cps;
    }

	// Override so we can intercept the stream
    public OutputStream getOutputStream() throws IOException {
        return new SlowOutputStream(super.getOutputStream());
    }
    
    // Override so we can intercept the stream
    public InputStream getInputStream() throws IOException {
        return new SlowInputStream(super.getInputStream());
    }

    // Conversions for milli and nano seconds
    private static final int MS_PER_SEC = 1000;
    private static final int NS_PER_SEC = 1000000000;
    private static final int NS_PER_MS  = NS_PER_SEC/MS_PER_SEC;
    
    private void pause(int bytes){
    	long sleepMS = (bytes*MS_PER_SEC)/CPS;
    	int  sleepNS = ((bytes*MS_PER_SEC)/CPS) % NS_PER_MS;
        try {
            Thread.sleep(sleepMS,sleepNS);
        } catch (InterruptedException ignored) {
        }
    }
    
    private class SlowInputStream extends FilterInputStream {

        public SlowInputStream(InputStream in) {
            super(in);
        }

        public int read() throws IOException {
            pause(1);
            return in.read();
        }

        public int read(byte[] b, int off, int len) throws IOException {
            pause(len);
            return in.read(b, off, len);
        }

    }
    
    private class SlowOutputStream extends FilterOutputStream {

        public SlowOutputStream(OutputStream out) {
            super(out);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            pause(len);
            out.write(b, off, len);
        }

        public void write(int b) throws IOException {
            pause(1);
            out.write(b);
        }
    }
}