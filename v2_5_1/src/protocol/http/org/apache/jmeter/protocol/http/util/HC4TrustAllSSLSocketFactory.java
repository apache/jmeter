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

package org.apache.jmeter.protocol.http.util;

import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;

/**
 * Apache HttpClient protocol factory to generate SSL sockets
 */

public class HC4TrustAllSSLSocketFactory extends SSLSocketFactory {

    private static final TrustStrategy TRUSTALL = new TrustStrategy(){
        public boolean isTrusted(X509Certificate[] chain, String authType) {
            return true;
        }
    };

    /**
     * Create an SSL factory which trusts all certificates and hosts.
     * {@link SSLSocketFactory#SSLSocketFactory(TrustStrategy, org.apache.http.conn.ssl.X509HostnameVerifier)} 
     * @throws GeneralSecurityException if there's a problem setting up the security
     */
    public HC4TrustAllSSLSocketFactory() throws GeneralSecurityException {
        super(TRUSTALL, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }
}
