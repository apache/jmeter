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

package org.apache.jmeter.protocol.http.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.jmeter.samplers.SampleResult;

/*
 * Socket that reads back from the output
 */
public class LoopbackHTTPSocket extends Socket {

    // get access to buffer
    static class LoopbackOutputStream extends ByteArrayOutputStream{
        byte [] getBuffer() {
            return buf;
        }
    }

    // wrap read() methods to track output buffer
    static class LoopBackInputStream extends ByteArrayInputStream{
        private LoopbackOutputStream os;
        @Override
        public synchronized int read() {
            buf=os.getBuffer();   // make sure buffer details
            count=buf.length; // track the output
            return super.read();
        }
        @Override
        public synchronized int read(byte[] b, int off, int len) {
            buf=os.getBuffer();
            count=buf.length;
            return super.read(b, off, len);
        }

        public LoopBackInputStream(LoopbackOutputStream _os) {
            super(_os.getBuffer());
            os=_os;
        }
    }

    private final LoopbackOutputStream os;

    private LoopbackHTTPSocket() throws IOException{
        os=new LoopbackOutputStream();
        // Preload the output so that can be read back as HTTP
        os.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n".getBytes(SampleResult.DEFAULT_HTTP_ENCODING));
    }

    public LoopbackHTTPSocket(String host, int port, InetAddress localAddress, int localPort, int timeout) throws IOException {
        this();
    }

    public LoopbackHTTPSocket(String host, int port, InetAddress localAddr, int localPort) throws IOException {
        this();
    }

    public LoopbackHTTPSocket(String host, int port) throws UnknownHostException, IOException {
        this();
    }

    // Override so we can intercept the stream
    @Override
    public OutputStream getOutputStream() throws IOException {
        return os;
    }

    // Override so we can intercept the stream
    @Override
    public InputStream getInputStream() throws IOException {
        return new LoopBackInputStream(os);
    }
}
