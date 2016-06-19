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
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLSocket;

import org.apache.http.HttpHost;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.OperatedClientConnection;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.impl.conn.DefaultClientConnectionOperator;

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
    
    /**
     * Uses the underlying implementation to support Server Name Indication (SNI).
     * @author Michael Locher <cmbntr@gmail.com>
     * @see <a href="https://issues.apache.org/jira/browse/HTTPCLIENT-1119">HTTPCLIENT-1119</a>
     */
    private static class HostNameSetter {

        private static final AtomicReference<HostNameSetter> CURRENT = new AtomicReference<>();

        private final WeakReference<Class<?>> cls;
        private final WeakReference<Method> setter;

        private HostNameSetter(Class<?> clazz, Method setter) {
            this.cls = new WeakReference<Class<?>>(clazz);
            this.setter = setter == null ? null : new WeakReference<>(setter);
        }

        private static Method init(Class<?> cls) {
            Method s = null;
            try {
                s = cls.getMethod("setHost", String.class);
            } catch (SecurityException | NoSuchMethodException e) {
                initFail(e);
            }
            CURRENT.set(new HostNameSetter(cls, s));
            return s;
        }

        private static void initFail(Exception e) {
            // ignore
        }

        private Method reuse(Class<?> cls) {
            final boolean wrongClass = this.cls.get() != cls;
            if (wrongClass) {
                return init(cls);
            }

            final boolean setterNotSupported = this.setter == null;
            if (setterNotSupported) {
                return null;
            }

            final Method s = setter.get();
            final boolean setterLost = s == null;
            return setterLost ? init(cls) : s;
        }

        /**
         * Invokes the {@code #setName(String)} method if one is present.
         *
         * @param hostname the name to set
         * @param sslsock the socket
         */
        public static void setServerNameIndication(String hostname, SSLSocket sslsock) {
            final Class<?> cls = sslsock.getClass();
            final HostNameSetter current = CURRENT.get();
            final Method setter = (current == null) ? init(cls) : current.reuse(cls);
            if (setter != null) {
                try {
                    setter.invoke(sslsock, hostname);
                } catch (IllegalArgumentException
                        | IllegalAccessException
                        | InvocationTargetException e) {
                    setServerNameIndicationFail(e);
                }
            }
        }

        private static void setServerNameIndicationFail(Exception e) {
            // ignore
        }
    }
}
