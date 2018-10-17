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

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509KeyManager;

/**
 * Implementation of {@link KeyManager} that allows using an alias
 * @since 4.0
 *
 */
public class AliasKeyManager implements X509KeyManager {

    private final String alias;
    private final X509KeyManager km;

    public AliasKeyManager(X509KeyManager km, String alias) {
        this.km = km;
        this.alias = alias;
    }

    /**
     * Wraps the first found {@link X509KeyManager} that has a private key for
     * the given {@code alias} as an {@link AliasKeyManager} and returns it as
     * the only element in a newly created array.
     * 
     * @param kms
     *            the KeyManagers to be searched for the {@code alias}
     * @param alias
     *            the name to be searched for
     * @return an array with one {@link AliasKeyManager} that has a private key
     *         named {@code alias}
     * @throws IllegalArgumentException
     *             if no valid KeyManager is found
     */
    public static AliasKeyManager[] wrap(KeyManager[] kms, String alias) {
        AliasKeyManager validManager = Arrays.asList(kms).stream()
                .filter(m -> m instanceof X509KeyManager)
                .map(m -> (X509KeyManager) m)
                .filter(m -> m.getPrivateKey(alias) != null)
                .map(m -> new AliasKeyManager(m, alias)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No key found for alias '" + alias + "'"));
        return new AliasKeyManager[] { validManager };
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers,
            Socket socket) {
        return alias;
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers,
            Socket socket) {
        return alias;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        return this.km.getCertificateChain(alias);
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return this.km.getClientAliases(keyType, issuers);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        return this.km.getPrivateKey(alias);
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return this.km.getServerAliases(keyType, issuers);
    }

}
