/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.jmeter.util;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Custom TrustManager ignores all certificate errors
 *
 * TODO: implement conditional checking and logging
 *
 * (Derived from AuthSSLX509TrustManager in HttpClient contrib directory)
 */

public class CustomX509TrustManager implements X509TrustManager
{
    private final X509TrustManager defaultTrustManager;

    private static final Logger log = LoggingManager.getLoggerForClass();

    public CustomX509TrustManager(final X509TrustManager defaultTrustManager) {
        super();
        if (defaultTrustManager == null) {
            throw new IllegalArgumentException("Trust manager may not be null");
        }
        this.defaultTrustManager = defaultTrustManager;
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],String)
     */
    @Override
    public void checkClientTrusted(X509Certificate[] certificates,String authType) throws CertificateException {
        if (certificates != null && log.isDebugEnabled()) {
            for (int c = 0; c < certificates.length; c++) {
                X509Certificate cert = certificates[c];
                log.debug(" Client certificate " + (c + 1) + ":");
                log.debug("  Subject DN: " + cert.getSubjectDN());
                log.debug("  Signature Algorithm: " + cert.getSigAlgName());
                log.debug("  Valid from: " + cert.getNotBefore() );
                log.debug("  Valid until: " + cert.getNotAfter());
                log.debug("  Issuer: " + cert.getIssuerDN());
            }
        }
//        try {
//            defaultTrustManager.checkClientTrusted(certificates,authType);
//        } catch (CertificateException e){
//            log.warn("Ignoring failed Client trust check: "+e.getMessage());
//        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],String)
     */
    @Override
    public void checkServerTrusted(X509Certificate[] certificates,String authType) throws CertificateException {
        if (certificates != null && log.isDebugEnabled()) {
            for (int c = 0; c < certificates.length; c++) {
                X509Certificate cert = certificates[c];
                log.debug(" Server certificate " + (c + 1) + ":");
                log.debug("  Subject DN: " + cert.getSubjectDN());
                log.debug("  Signature Algorithm: " + cert.getSigAlgName());
                log.debug("  Valid from: " + cert.getNotBefore() );
                log.debug("  Valid until: " + cert.getNotAfter());
                log.debug("  Issuer: " + cert.getIssuerDN());
            }
        }
//        try{
//            defaultTrustManager.checkServerTrusted(certificates,authType);
//        } catch (CertificateException e){
//            log.warn("Ignoring failed Server trust check: "+e.getMessage());
//        }
    }

    /**
     * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
     */
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return this.defaultTrustManager.getAcceptedIssuers();
    }
}
