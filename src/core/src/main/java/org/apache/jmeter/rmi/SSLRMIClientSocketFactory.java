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
import java.net.Socket;
import java.rmi.server.RMIClientSocketFactory;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Implementation of {@link RMIClientSocketFactory} that:
 * <ul>
 *  <li>Establishes SSL connection</li>
 * </ul>
 * @since 4.0
 */
public class SSLRMIClientSocketFactory
        implements RMIClientSocketFactory, Serializable {

    private static final long serialVersionUID = 1L;

    private String alias;
    private String keyStoreLocation;
    private String keyStorePassword;
    private String keyStoreType;
    private String trustStoreLocation;
    private String trustStorePassword;
    private String trustStoreType;

    public void setAlias(String alias) {
        this.alias = alias;
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

    @Override
    public Socket createSocket(String host, int port) throws IOException {

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

        KeyManagerFactory kmf;
        SSLContext ctx;
        try {
            kmf = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, passphrase);
            ctx = SSLContext.getInstance("TLS");
            TrustManagerFactory tmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            ctx.init(AliasKeyManager.wrap(kmf.getKeyManagers(), alias), tmf.getTrustManagers(), null);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
        SSLSocketFactory factory = ctx.getSocketFactory();
        if (factory == null) {
            throw new IOException(
                    "Unable to obtain SSLSocketFactory for provided KeyStore");
        }

        return factory.createSocket(host, port);
    }

    private KeyStore loadStore(String location, char[] passphrase, String type)
            throws IOException {
        try {
            KeyStore store = KeyStore.getInstance(type);
            store.load(new FileInputStream(location), passphrase);
            return store;
        } catch (NoSuchAlgorithmException | CertificateException
                | KeyStoreException e) {
            throw new IOException("Can't load " + location + " as type " + type, e);
        }
    }

    @Override
    public String toString() {
        return "SSLRMIClientSocketFactory(keyStoreLocation=" + this.keyStoreLocation + ", type="
                + this.keyStoreType +", trustStoreLocation=" + this.trustStoreLocation + ", type="
                + this.trustStoreType + ", alias=" + this.alias + ')';
    }
}
