/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation. For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.jmeter.protocol.http.sampler;

import java.io.IOException;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

import org.apache.http.HttpHost;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;
import org.apache.jmeter.util.HostNameSetter;

/**
 * Custom implementation of {@link DefaultClientConnectionOperator} to fix SNI Issue
 * @see "https://bz.apache.org/bugzilla/show_bug.cgi?id=57935"
 * @since 3.0
 * TODO Remove it when full upgrade to 4.5.X is done and cleanup is made in the Socket Factory of JMeter that handles client certificates and Slow socket
 */
public class JMeterClientConnectionOperator extends
        DefaultClientConnectionOperator {

    /**
     * @param schemes
     *            the scheme registry
     */
    public JMeterClientConnectionOperator(final SchemeRegistry schemes) {
        super(schemes);
    }

    /** 
     * @param schemes
     *            the scheme registry
     * @param dnsResolver
     *            the custom DNS lookup mechanism
     */
    public JMeterClientConnectionOperator(final SchemeRegistry schemes,
            final DnsResolver dnsResolver) {
        super(schemes, dnsResolver);
    }

    @Override
    public OperatedClientConnection createConnection() {
        return new JMeterDefaultClientConnection();
    }

    
    private static class JMeterDefaultClientConnection extends DefaultClientConnection {
        public JMeterDefaultClientConnection() {
            super();
        }

        /* (non-Javadoc)
         * @see org.apache.http.impl.conn.DefaultClientConnection#opening(java.net.Socket, org.apache.http.HttpHost)
         */
        @Override
        public void opening(Socket sock, HttpHost target) throws IOException {
            super.opening(sock, target);
            if(sock instanceof SSLSocket) {
                HostNameSetter.setServerNameIndication(target.getHostName(), (SSLSocket) sock);
            }
        }
    }
}
