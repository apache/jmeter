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

package org.apache.jmeter.protocol.http.sampler.hc;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.http.HttpHost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.protocol.HttpContext;
import org.apache.jmeter.util.HttpSSLProtocolSocketFactory;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.JsseSSLManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LazyLayeredConnectionSocketFactory that lazily initializes HTTPS Socket Factory
 * @since 5.0
 */
public final class LazyLayeredConnectionSocketFactory implements LayeredConnectionSocketFactory{
    private static final Logger LOG = LoggerFactory.getLogger(LazyLayeredConnectionSocketFactory.class);
    private static final String PROTOCOL_LIST =
            JMeterUtils.getPropDefault("https.socket.protocols", ""); // $NON-NLS-1$ $NON-NLS-2$

    private static final String CIPHER_LIST =
            JMeterUtils.getPropDefault("https.socket.ciphers", ""); // $NON-NLS-1$ $NON-NLS-2$

    private static final String[] SUPPORTED_PROTOCOL_LIST = 
            PROTOCOL_LIST.isEmpty() ? 
                    null: PROTOCOL_LIST.split(" "); // $NON-NLS-1$
    private static final String[] SUPPORTED_CIPHER_LIST = 
            CIPHER_LIST.isEmpty() ? 
                    null : CIPHER_LIST.split(" "); // $NON-NLS-1$

    private static class AdapteeHolder { // IODH idiom
        private static final LayeredConnectionSocketFactory ADAPTEE = checkAndInit();  

        /**
         * @throws SSLInitializationException
         */
        private static LayeredConnectionSocketFactory checkAndInit() throws SSLInitializationException {
            LOG.info("Setting up HTTPS TrustAll Socket Factory");
            return new SSLConnectionSocketFactory(
                    new HttpSSLProtocolSocketFactory(JsseSSLManager.CPS),
                    SUPPORTED_PROTOCOL_LIST,
                    SUPPORTED_CIPHER_LIST,
                    NoopHostnameVerifier.INSTANCE);
        }

        static LayeredConnectionSocketFactory getINSTANCE() {
            return ADAPTEE;
        }
    }
    
    /**
     * 
     */
    public LazyLayeredConnectionSocketFactory() {
        super();
    }

    @Override
    public Socket createSocket(HttpContext paramHttpContext) throws IOException {
        return AdapteeHolder.getINSTANCE().createSocket(paramHttpContext);
    }

    @Override
    public Socket connectSocket(int paramInt, Socket paramSocket, HttpHost paramHttpHost,
            InetSocketAddress paramInetSocketAddress1, InetSocketAddress paramInetSocketAddress2,
            HttpContext paramHttpContext) throws IOException {
        return AdapteeHolder.getINSTANCE().connectSocket(paramInt, paramSocket, paramHttpHost,
                paramInetSocketAddress1, paramInetSocketAddress2,
                paramHttpContext);
    }

    @Override
    public Socket createLayeredSocket(Socket paramSocket, String paramString, int paramInt,
            HttpContext paramHttpContext) throws IOException {
        return AdapteeHolder.getINSTANCE().createLayeredSocket(paramSocket, paramString, paramInt,
            paramHttpContext);
    }
}
