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

package org.apache.jmeter.protocol.http.proxy;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509KeyManager;

/**
 * X509KeyManager wrapper class which returns a specific server alias.
 */
class ServerAliasKeyManager implements X509KeyManager {

    private final X509KeyManager km;

    private final String serverAlias;

    /**
     * Create a wrapper class that always returns the specified server alias
     * 
     * @param km the key manager to wrap
     * @param serverAlias the server alias which {@link #chooseServerAlias(String, Principal[], Socket)} will return
     */
    ServerAliasKeyManager(KeyManager km, String serverAlias) {
        this.km = (X509KeyManager) km;
        this.serverAlias = serverAlias;
    }

    /**
     * {@inheritDoc}
     * @return always returns the specified server alias
     */
    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return serverAlias;
    }

    //    Remaining implementations delegate to the wrapped key manager

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        return km.chooseClientAlias(keyType, issuers, socket);
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        return km.getCertificateChain(alias);
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return km.getClientAliases(keyType, issuers);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        return km.getPrivateKey(alias);
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return km.getServerAliases(keyType, issuers);
    }

}
