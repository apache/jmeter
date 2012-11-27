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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.net.UnknownHostException;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

/**
 * Commons HttpClient protocol factory to generate Loopback HTTP sockets
 */

public class LoopbackHttpClientSocketFactory implements ProtocolSocketFactory {

    public LoopbackHttpClientSocketFactory() {
        super();
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress clientHost,
            int clientPort) throws IOException, UnknownHostException {
        return new LoopbackHTTPSocket(host,port,clientHost,clientPort);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        return new LoopbackHTTPSocket(host,port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort,
            HttpConnectionParams params)
    throws IOException, UnknownHostException, ConnectTimeoutException {
        int timeout = params.getConnectionTimeout();
        if (timeout == 0) {
            return new LoopbackHTTPSocket(host,port,localAddress,localPort);
        } else {
            return new LoopbackHTTPSocket(host,port,localAddress,localPort, timeout);
        }
    }

    /**
     * Convenience method to set up the necessary HttpClient protocol and URL handler.
     *
     * Only works for HttpClient, because it's not possible (or at least very difficult)
     * to provide a different socket factory for the HttpURLConnection class.
     */
    public static void setup(){
        final String LOOPBACK = "loopback"; // $NON-NLS-1$

        // This ensures tha HttpClient knows about the protocol
        Protocol.registerProtocol(LOOPBACK, new Protocol(LOOPBACK,new LoopbackHttpClientSocketFactory(),1));

        // Now allow the URL handling to work.
        URLStreamHandlerFactory ushf = new URLStreamHandlerFactory(){
            @Override
            public URLStreamHandler createURLStreamHandler(String protocol) {
                if (protocol.equalsIgnoreCase(LOOPBACK)){
                    return new URLStreamHandler(){
                        @Override
                        protected URLConnection openConnection(URL u) throws IOException {
                            return null;// not needed for HttpClient
                        }
                    };
                }
                return null;
            }
        };

        java.net.URL.setURLStreamHandlerFactory(ushf);
    }
}
