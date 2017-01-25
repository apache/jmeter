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
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLSocket;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.params.HttpParams;
import org.apache.jmeter.util.HttpSSLProtocolSocketFactory;
import org.apache.jmeter.util.JsseSSLManager;

/**
 * Apache HttpClient protocol factory to generate SSL sockets
 */

public class HC4TrustAllSSLSocketFactory extends SSLSocketFactory {

    private static final TrustStrategy TRUSTALL = (chain, authType) -> true;
    private javax.net.ssl.SSLSocketFactory factory;

    /**
     * Create an SSL factory which trusts all certificates and hosts.
     * {@link SSLSocketFactory#SSLSocketFactory(TrustStrategy, org.apache.http.conn.ssl.X509HostnameVerifier)} 
     * @throws GeneralSecurityException if there's a problem setting up the security
     */
    public HC4TrustAllSSLSocketFactory() throws GeneralSecurityException {
        this(new HttpSSLProtocolSocketFactory((JsseSSLManager)JsseSSLManager.getInstance(), JsseSSLManager.CPS));
    }
    
    /**
     * Create an SSL factory which trusts all certificates and hosts.
     * {@link SSLSocketFactory#SSLSocketFactory(TrustStrategy, org.apache.http.conn.ssl.X509HostnameVerifier)} 
     * @param factory javax.net.ssl.SSLSocketFactory 
     * @throws GeneralSecurityException if there's a problem setting up the security
     */
    protected HC4TrustAllSSLSocketFactory(javax.net.ssl.SSLSocketFactory factory) throws GeneralSecurityException {
        super(TRUSTALL, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        this.factory = factory;
    }

    /* (non-Javadoc)
     * @see org.apache.http.conn.ssl.SSLSocketFactory#createSocket(org.apache.http.params.HttpParams)
     */
    @Override
    public Socket createSocket(HttpParams params) throws IOException {
        return factory.createSocket();
    }

    /* (non-Javadoc)
     * @see org.apache.http.conn.ssl.SSLSocketFactory#createSocket()
     */
    @Override
    public Socket createSocket() throws IOException {
        return factory.createSocket();
    }

    /* (non-Javadoc)
     * @see org.apache.http.conn.ssl.SSLSocketFactory#createLayeredSocket(java.net.Socket, java.lang.String, int, boolean)
     */
    @Override
    public Socket createLayeredSocket(Socket socket, String host, int port,
            boolean autoClose) throws IOException, UnknownHostException {
        SSLSocket sslSocket = (SSLSocket) this.factory.createSocket(
                socket,
                host,
                port,
                autoClose
                );
        ALLOW_ALL_HOSTNAME_VERIFIER.verify(host, sslSocket);
        return sslSocket;
    }
}
