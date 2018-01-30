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

package org.apache.jmeter.protocol.http.sampler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.SchemeLayeredSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpParams;
import org.apache.jmeter.protocol.http.util.HC4TrustAllSSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lazy SchemeSocketFactory that lazily initializes HTTPS Socket Factory
 * @since 3.0
 */
public final class LazySchemeSocketFactory implements SchemeLayeredSocketFactory{
    private static final Logger LOG = LoggerFactory.getLogger(LazySchemeSocketFactory.class);

    private static class AdapteeHolder { // IODH idiom
        private static final SchemeLayeredSocketFactory ADAPTEE = checkAndInit();  

        /**
         * @throws SSLInitializationException
         */
        private static SchemeLayeredSocketFactory checkAndInit() throws SSLInitializationException {
            LOG.info("Setting up HTTPS TrustAll Socket Factory");
            try {
                return new HC4TrustAllSSLSocketFactory();
            } catch (GeneralSecurityException e) {
                LOG.warn("Failed to initialise HTTPS HC4TrustAllSSLSocketFactory", e);
                return SSLSocketFactory.getSocketFactory();
            }
        }

        static SchemeLayeredSocketFactory getINSTANCE() {
            return ADAPTEE;
        }
    }
    
    /**
     * 
     */
    public LazySchemeSocketFactory() {
        super();
    }
    
    /**
     * @param params {@link HttpParams}
     * @return the socket
     * @throws IOException when the socket creation fails
     * @see org.apache.http.conn.scheme.SchemeSocketFactory#createSocket(org.apache.http.params.HttpParams)
     */
    @Override
    public Socket createSocket(HttpParams params) throws IOException {
        return AdapteeHolder.getINSTANCE().createSocket(params);
    }
    
    /**
     * @param sock {@link Socket}
     * @param remoteAddress {@link InetSocketAddress}
     * @param localAddress {@link InetSocketAddress}
     * @param params {@link HttpParams}
     * @return the socket
     * @throws IOException when the socket creation fails
     * @throws UnknownHostException when the remote or local addresses can't be resolved
     * @throws ConnectTimeoutException when the connection times out
     * @see org.apache.http.conn.scheme.SchemeSocketFactory#connectSocket(java.net.Socket, java.net.InetSocketAddress, java.net.InetSocketAddress, org.apache.http.params.HttpParams)
     */
    @Override
    public Socket connectSocket(Socket sock, InetSocketAddress remoteAddress,
            InetSocketAddress localAddress, HttpParams params)
            throws IOException, UnknownHostException, ConnectTimeoutException {
        return AdapteeHolder.getINSTANCE().connectSocket(sock, remoteAddress, localAddress, params);
    }
    
    /**
     * @param sock {@link Socket}
     * @return true if the socket is secure
     * @throws IllegalArgumentException when the socket is not valid
     * @see org.apache.http.conn.scheme.SchemeSocketFactory#isSecure(java.net.Socket)
     */
    @Override
    public boolean isSecure(Socket sock) throws IllegalArgumentException {
        return AdapteeHolder.getINSTANCE().isSecure(sock);
    }

    /**
     * @param socket {@link Socket}
     * @param target {@link String}  
     * @param port int port of socket
     * @param params {@link HttpParams}
     * @return the socket
     */
    @Override
    public Socket createLayeredSocket(Socket socket, String target, int port,
            HttpParams params) throws IOException, UnknownHostException {
        return AdapteeHolder.getINSTANCE().createLayeredSocket(socket, target, port, params);
    }
}
