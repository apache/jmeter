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
import org.apache.http.conn.scheme.SchemeSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpParams;
import org.apache.jmeter.protocol.http.util.HC4TrustAllSSLSocketFactory;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Lazy SchemeSocketFactory that lazily initializes HTTPS Socket Factory
 * @since 3.0
 */
public final class LazySchemeSocketFactory implements SchemeSocketFactory{
    private static final Logger LOG = LoggingManager.getLoggerForClass();
    
    private volatile SchemeSocketFactory adaptee;
    
    /**
     * 
     */
    public LazySchemeSocketFactory() {
        super();
    }
    
    /**
     * @param params
     * @return
     * @throws IOException
     * @see org.apache.http.conn.scheme.SchemeSocketFactory#createSocket(org.apache.http.params.HttpParams)
     */
    @Override
    public Socket createSocket(HttpParams params) throws IOException {
        checkAndInit();
        return adaptee.createSocket(params);
    }
    
    /**
     * @throws SSLInitializationException
     */
    private void checkAndInit() throws SSLInitializationException {
        if(adaptee == null) {
            synchronized (this) {
                if(adaptee==null) {
                    LOG.info("Setting up HTTPS TrustAll Socket Factory");
                    try {
                        adaptee = new HC4TrustAllSSLSocketFactory();
                    } catch (GeneralSecurityException e) {
                        LOG.warn("Failed to initialise HTTPS HC4TrustAllSSLSocketFactory", e);
                        adaptee = SSLSocketFactory.getSocketFactory();
                    }
                }
            }
        }
    }
    
    /**
     * @param sock
     * @param remoteAddress
     * @param localAddress
     * @param params
     * @return
     * @throws IOException
     * @throws UnknownHostException
     * @throws ConnectTimeoutException
     * @see org.apache.http.conn.scheme.SchemeSocketFactory#connectSocket(java.net.Socket, java.net.InetSocketAddress, java.net.InetSocketAddress, org.apache.http.params.HttpParams)
     */
    @Override
    public Socket connectSocket(Socket sock, InetSocketAddress remoteAddress,
            InetSocketAddress localAddress, HttpParams params)
            throws IOException, UnknownHostException, ConnectTimeoutException {
        checkAndInit();
        return adaptee.connectSocket(sock, remoteAddress, localAddress, params);
    }
    
    /**
     * @param sock
     * @return
     * @throws IllegalArgumentException
     * @see org.apache.http.conn.scheme.SchemeSocketFactory#isSecure(java.net.Socket)
     */
    @Override
    public boolean isSecure(Socket sock) throws IllegalArgumentException {
        checkAndInit();
        return adaptee.isSecure(sock);
    }
}
