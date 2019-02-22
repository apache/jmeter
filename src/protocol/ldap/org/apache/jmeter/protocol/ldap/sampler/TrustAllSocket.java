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
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrustAllSocket extends SocketFactory {
    private static final AtomicReference<TrustAllSocket> defaultFactory = new AtomicReference<>();

    private static final Logger log = LoggerFactory.getLogger(TrustAllSocket.class);

    private static SSLSocketFactory sf;

    public TrustAllSocket() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[] {new X509ExtendedTrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2)
                throws CertificateException {
            }
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
                throws CertificateException {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2)
                throws CertificateException {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
                throws CertificateException {
            }
        }};

        SSLContext ctx = null;
        ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAllCerts, new java.security.SecureRandom());
        sf = ctx.getSocketFactory();
    }

    public static SocketFactory getDefault() {
        final TrustAllSocket value = defaultFactory.get();
        if (value == null) {
            try {
                defaultFactory.compareAndSet(null, new TrustAllSocket());
            } catch (KeyManagementException e) {
                log.error("KeyManagementException: "+e.getLocalizedMessage());
            } catch (NoSuchAlgorithmException e) {
                log.error("NoSuchAlgorithmException: "+e.getLocalizedMessage());
            }
            return defaultFactory.get();
        }
        return value;
    }
    @Override
    public Socket createSocket() throws IOException{
        return sf.createSocket();
    }

    @Override
    public Socket createSocket(final String s, final int i) throws IOException {
        return sf.createSocket(s, i);
    }

    @Override
    public Socket createSocket(final String s, final int i, final InetAddress inetAddress, final int i1) throws IOException {
        return sf.createSocket(s, i, inetAddress, i1);
    }

    @Override
    public Socket createSocket(final InetAddress inetAddress, final int i) throws IOException {
        return sf.createSocket(inetAddress, i);
    }

    @Override
    public Socket createSocket(final InetAddress inetAddress, final int i, final InetAddress inetAddress1, final int i1) throws IOException {
        return sf.createSocket(inetAddress, i, inetAddress1, i1);
    }
}
