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

import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.params.HttpParams;
import org.apache.jmeter.util.SlowSocket;

/**
 * Apache HttpClient protocol factory to generate "slow" SSL sockets for emulating dial-up modems
 */

public class SlowHC4SSLSocketFactory extends SSLSocketFactory {

    private static final TrustStrategy TRUSTALL = new TrustStrategy(){
        public boolean isTrusted(X509Certificate[] chain, String authType) {
            return true;
        }
    };

    private static final AllowAllHostnameVerifier ALLOW_ALL_HOSTS = new AllowAllHostnameVerifier();

    private final int CPS; // Characters per second to emulate

    /**
     * Create a factory 
     * @param cps - characters per second
     * @throws GeneralSecurityException if there's a problem setting up the security
     */
    public SlowHC4SSLSocketFactory(final int cps) throws GeneralSecurityException {
        super(TRUSTALL, ALLOW_ALL_HOSTS);
        CPS = cps;
    }

    @Override
    public Socket createSocket(final HttpParams params) {
        return new SlowSocket(CPS);
    }

    @Override
    public Socket createSocket() { // probably not used
        return new SlowSocket(CPS);
    }
    
}
