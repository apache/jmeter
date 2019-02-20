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

public class TrustAllSocket extends SocketFactory {
    private static final AtomicReference<TrustAllSocket> defaultFactory = new AtomicReference<>();

    private static SSLSocketFactory sf;

    public TrustAllSocket() {
        //KeyStore keyStore = ... /* Get a keystore containing the self-signed certificate) */
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
                // TODO Auto-generated method stub
            }
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
                throws CertificateException {
                // TODO Auto-generated method stub
            }
            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2)
                throws CertificateException {
                // TODO Auto-generated method stub
            }
            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2)
                throws CertificateException {
                // TODO Auto-generated method stub
            }
        }};

        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            ctx.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        sf = ctx.getSocketFactory();
    }

    public static SocketFactory getDefault() {
        final TrustAllSocket value = defaultFactory.get();
        if (value == null) {
            defaultFactory.compareAndSet(null, new TrustAllSocket());
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
