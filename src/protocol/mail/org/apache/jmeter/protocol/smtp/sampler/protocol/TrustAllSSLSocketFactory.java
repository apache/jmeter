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

package org.apache.jmeter.protocol.smtp.sampler.protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This class can be used as a SocketFactory with SSL-connections.
 * Its purpose is to ensure that all certificates - no matter from which CA - are accepted to secure the SSL-connection.
 * @deprecated Will be removed in next version, use {@link org.apache.jmeter.util.TrustAllSSLSocketFactory}
 */
@Deprecated
public class TrustAllSSLSocketFactory extends SSLSocketFactory  {

    private final SSLSocketFactory factory;
    
    // Empty arrays are immutable
    private static final X509Certificate[] EMPTY_X509Certificate = new X509Certificate[0];

    /**
     * Standard constructor
     */
    public TrustAllSSLSocketFactory(){
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContext.getInstance("TLS"); // $NON-NLS-1$
            sslcontext.init( null, new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return EMPTY_X509Certificate;
                        }
                        @Override
                        public void checkClientTrusted(
                                X509Certificate[] certs, String authType) {
                        }
                        @Override
                        public void checkServerTrusted(
                                X509Certificate[] certs, String authType) {
                        }
                    }
                },
                        new java.security.SecureRandom());
        } catch (Exception e) {
            throw new RuntimeException("Could not create the SSL context",e);
        }
        factory = sslcontext.getSocketFactory();
    }

    /**
     * Factory method
     * @return New TrustAllSSLSocketFactory
     */
    public static synchronized SocketFactory getDefault() {
        return new TrustAllSSLSocketFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket( Socket socket, String s, int i, boolean
            flag)
            throws IOException {
        return factory.createSocket( socket, s, i, flag);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket( InetAddress inaddr, int i,
            InetAddress inaddr1, int j) throws IOException {
        return factory.createSocket( inaddr, i, inaddr1, j);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket( InetAddress inaddr, int i) throws
            IOException {
        return factory.createSocket( inaddr, i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket( String s, int i, InetAddress inaddr, int j)
    throws IOException {
        return factory.createSocket( s, i, inaddr, j);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket( String s, int i) throws IOException {
        return factory.createSocket( s, i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Socket createSocket() throws IOException {
        return factory.createSocket();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getDefaultCipherSuites() {
        return factory.getSupportedCipherSuites();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getSupportedCipherSuites() {
        return factory.getSupportedCipherSuites();
    }
}
