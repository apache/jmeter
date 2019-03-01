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
package org.apache.jmeter.protocol.ldap.sampler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustAllSocketFactory extends SocketFactory {
    private static final AtomicReference<TrustAllSocketFactory> defaultFactory = new AtomicReference<>();

    private static final Logger log = LoggerFactory.getLogger(TrustAllSocketFactory.class);

    public TrustAllSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509ExtendedTrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[0];
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        // NOOP
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        // NOOP
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2)
                        throws CertificateException {
                        // NOOP
                    }
                    @Override
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
                        throws CertificateException {
                        // NOOP
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2)
                        throws CertificateException {
                        // NOOP
                    }
                    @Override
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
                        throws CertificateException {
                        // NOOP
                    }
                }
        };

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAllCerts, new java.security.SecureRandom());
    }

    public static SocketFactory getDefault() {
        final TrustAllSocketFactory value = defaultFactory.get();
        if (value == null) {
            try {
                defaultFactory.compareAndSet(null, new TrustAllSocketFactory());
            } catch (KeyManagementException e) {
                log.error("KeyManagementException: {}", e.getLocalizedMessage());
            } catch (NoSuchAlgorithmException e) {
                log.error("NoSuchAlgorithmException: {}", e.getLocalizedMessage());
            }
            return defaultFactory.get();
        }
        return value;
    }
    @Override
    public Socket createSocket() throws IOException{
        return getDefault().createSocket();
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        return getDefault().createSocket(host, port);
    }

    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localHost, final int localPort) throws IOException {
        return getDefault().createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(final InetAddress localHost, final int localPort) throws IOException {
        return getDefault().createSocket(localHost, localPort);
    }

    @Override
    public Socket createSocket(final InetAddress address, final int port, final InetAddress localHost, final int localPort) 
            throws IOException {
        return getDefault().createSocket(address, port, localHost, localPort);
    }
}
