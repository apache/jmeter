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

package org.apache.jmeter.rmi;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link RMIServerSocketFactoryImpl} that:
 * <ul>
 *  <li>Binds socket to an address</li>
 *  <li>Establishes SSL connection</li>
 * </ul>
 * @since 4.0
 */
public class SSLRMIServerSocketFactory implements RMIServerSocketFactory, Serializable {

    private static final long serialVersionUID = 258730225720182190L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SSLRMIServerSocketFactory.class);
    private final InetAddress localAddress;

    private String alias;
    private String keyStoreLocation;
    private String keyStorePassword;
    private String keyStoreType;
    private String trustStoreLocation;
    private String trustStorePassword;
    private String trustStoreType;

    private boolean clientAuth;

    public SSLRMIServerSocketFactory(InetAddress pAddress) {
        this.localAddress = pAddress;
    }

    public void setNeedClientAuth(boolean clientAuth) {
        this.clientAuth = clientAuth;
    }

    public void setKeystore(String location, String type, String password) {
        this.keyStoreLocation = location;
        this.keyStoreType = type;
        this.keyStorePassword = password;
    }

    public void setTruststore(String location, String type, String password) {
        this.trustStoreLocation = location;
        this.trustStoreType = type;
        this.trustStorePassword = password;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        char[] passphrase = null;
        if (keyStorePassword != null) {
            passphrase = keyStorePassword.toCharArray();
        }

        KeyStore keyStore = null;
        if (keyStoreLocation != null) {
            keyStore = loadStore(keyStoreLocation, passphrase, keyStoreType);
        }

        KeyStore trustStore;
        if (trustStoreLocation != null) {
            trustStore = loadStore(trustStoreLocation, trustStorePassword.toCharArray(), trustStoreType);
        } else {
            trustStore = keyStore;
        }

        if (alias == null) {
            throw new IOException(
                    "SSL certificate alias cannot be null; MUST be set for SSLServerSocketFactory!");
        }

        SSLContext ctx;
        try {
            KeyManagerFactory kmf = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, passphrase);
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            ctx = SSLContext.getInstance("TLS");
            ctx.init(AliasKeyManager.wrap(kmf.getKeyManagers(), alias), tmf.getTrustManagers(), null);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
        SSLServerSocketFactory factory = ctx.getServerSocketFactory();
        if (factory == null) {
            throw new IOException(
                    "Unable to obtain SSLServerSocketFactory for provided KeyStore");
        }

        SSLServerSocket socket;
        try {
            socket = (SSLServerSocket) factory
                    .createServerSocket(port, 0, localAddress);
        } catch (BindException e) {
            throw new IOException("Could not bind to " + localAddress + " using port " + port, e);
        }
        socket.setNeedClientAuth(clientAuth);
        LOGGER.info("Created SSLSocket: {}", socket);
        return socket;
    }

    private KeyStore loadStore(String location, char[] passphrase, String type)
            throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(location)){
            KeyStore store = KeyStore.getInstance(type);
            store.load(fileInputStream, passphrase);
            return store;
        } catch (NoSuchAlgorithmException | CertificateException
                | KeyStoreException e) {
            throw new IOException(e);
        }
    }

    @Override
    public String toString() {
        return "SSLRMIServerSocketFactory(host=" + localAddress 
                + ", keyStoreLocation=" + this.keyStoreLocation + ", type="
                + this.keyStoreType +", trustStoreLocation=" + this.trustStoreLocation + ", type="
                + this.trustStoreType + ", alias=" + this.alias + ')';
    }

}
