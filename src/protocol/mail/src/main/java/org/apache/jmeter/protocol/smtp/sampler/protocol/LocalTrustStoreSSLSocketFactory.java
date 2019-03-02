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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * This class implements an SSLSocketFactory which supports a local truststore.
 */
public class LocalTrustStoreSSLSocketFactory extends SSLSocketFactory  {

    private final SSLSocketFactory factory;

    public LocalTrustStoreSSLSocketFactory(File truststore){
        SSLContext sslcontext = null;
        try {
            KeyStore ks = KeyStore.getInstance("JKS"); // $NON-NLS-1$
            try (FileInputStream fileStream = new FileInputStream(truststore);
                    InputStream stream = new BufferedInputStream(fileStream)) {
                ks.load(stream, null);
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            TrustManager[] trustmanagers = tmf.getTrustManagers();
            sslcontext = SSLContext.getInstance("TLS"); // $NON-NLS-1$
            sslcontext.init( null, trustmanagers, new SecureRandom());
        } catch (Exception e) {
            throw new RuntimeException("Could not create the SSL context",e);
        }
        factory = sslcontext.getSocketFactory();
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
