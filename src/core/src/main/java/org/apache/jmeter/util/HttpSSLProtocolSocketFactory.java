/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Derived from EasySSLProtocolFactory
 *
 * Used by JsseSSLManager to set up the Java https socket handling
 */
public class HttpSSLProtocolSocketFactory
    extends SSLSocketFactory {// for java sockets

    private static final Logger log = LoggerFactory.getLogger(HttpSSLProtocolSocketFactory.class);

    private static final String PROTOCOL_LIST =
            JMeterUtils.getPropDefault("https.socket.protocols", ""); // $NON-NLS-1$ $NON-NLS-2$

    private static final String[] protocols = PROTOCOL_LIST.split(" "); // $NON-NLS-1$

    private static final String CIPHER_LIST =
            JMeterUtils.getPropDefault("https.cipherSuites", ""); // $NON-NLS-1$ $NON-NLS-2$

    private static final String[] ciphers = CIPHER_LIST.split(", *"); // $NON-NLS-1$

    static {
        if (!PROTOCOL_LIST.isEmpty()) {
            log.info("Using protocol list:{} and cipher list: {}", PROTOCOL_LIST, CIPHER_LIST);
        }
    }

    private final int cps; // Characters per second to emulate

    public HttpSSLProtocolSocketFactory() {
        this(0);
    }

    public HttpSSLProtocolSocketFactory(int cps) {
        this.cps = cps;
    }


    private static void configureSocket(Socket socket){
        if (!(socket instanceof SSLSocket)) {
            throw new IllegalArgumentException("Expected SSLSocket");
        }
        SSLSocket sock = (SSLSocket) socket;
        if (!PROTOCOL_LIST.isEmpty()) {
            try {
                sock.setEnabledProtocols(protocols);
            } catch (IllegalArgumentException e) { // NOSONAR
                if (log.isWarnEnabled()) {
                    log.warn("Could not set protocol list: {}.", PROTOCOL_LIST);
                    log.warn("Valid protocols are: {}", join(sock.getSupportedProtocols()));
                }
            }
        }

        if (!CIPHER_LIST.isEmpty()) {
            try {
                sock.setEnabledCipherSuites(ciphers);
            } catch (IllegalArgumentException e) { // NOSONAR
                if (log.isWarnEnabled()) {
                    log.warn("Could not set cipher list: {}.", CIPHER_LIST);
                    log.warn("Valid ciphers are: {}", join(sock.getSupportedCipherSuites()));
                }
            }
        }
    }

    private static String join(String[] strings) {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<strings.length;i++){
            if (i>0) {
                sb.append(' ');
            }
            sb.append(strings[i]);
        }
        return sb.toString();
    }

    private static SSLSocketFactory getSSLSocketFactory() throws IOException {
        try {
            SSLContext sslContext = ((JsseSSLManager)SSLManager.getInstance()).getContext();
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException ex) {
            throw new IOException("Rethrown as IOE", ex);
        }
    }

    /*
     * Wraps the socket in a slow SSL socket if necessary
     */
    private Socket wrapSocket(Socket sock){
        if (cps >0) {
            return new SlowSSLSocket((SSLSocket) sock, cps);
        }
        return sock;
    }

    /**
     * @see javax.net.SocketFactory#createSocket()
     */
    @Override
    public Socket createSocket() throws IOException, UnknownHostException {
        SSLSocketFactory sslfac = getSSLSocketFactory();
        Socket sock = sslfac.createSocket();
        configureSocket(sock);
        return wrapSocket(sock);
    }


    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        SSLSocketFactory sslfac = getSSLSocketFactory();
        Socket sock=sslfac.createSocket(host,port);
        configureSocket(sock);
        return wrapSocket(sock);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        SSLSocketFactory sslfac = getSSLSocketFactory();
        Socket sock=sslfac.createSocket(address, port, localAddress, localPort);
        configureSocket(sock);
        return wrapSocket(sock);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        try {
            SSLSocketFactory sslfac = getSSLSocketFactory();
            return sslfac.getDefaultCipherSuites();
        } catch (IOException ex) {
            return new String[] {};
        }
    }

    @Override
    public String[] getSupportedCipherSuites() {
        try {
            SSLSocketFactory sslfac = getSSLSocketFactory();
            return sslfac.getSupportedCipherSuites();
        } catch (IOException ex) {
            return new String[] {};
        }
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        SSLSocketFactory sslfac = getSSLSocketFactory();
        Socket sock=sslfac.createSocket(s, host,port, autoClose);
        configureSocket(sock);
        return wrapSocket(sock);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        SSLSocketFactory sslfac = getSSLSocketFactory();
        Socket sock=sslfac.createSocket(host,port);
        configureSocket(sock);
        return wrapSocket(sock);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress inetAddress, int localPort)
            throws IOException, UnknownHostException {
        SSLSocketFactory sslfac = getSSLSocketFactory();
        Socket sock=sslfac.createSocket(host, port, inetAddress, localPort);
        configureSocket(sock);
        return wrapSocket(sock);

    }
}
