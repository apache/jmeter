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

package org.apache.jmeter.protocol.http.sampler;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common parent class for HttpClient implementations.
 * 
 * Includes system property settings that are handled internally by the Java HTTP implementation,
 * but which need to be explicitly configured in HttpClient implementations. 
 */
public abstract class HTTPHCAbstractImpl extends HTTPAbstractImpl {

    private static final Logger log = LoggerFactory.getLogger(HTTPHCAbstractImpl.class);

    protected static final String PROXY_HOST = System.getProperty("http.proxyHost","");

    protected static final String NONPROXY_HOSTS = System.getProperty("http.nonProxyHosts","");

    protected static final int PROXY_PORT = Integer.parseInt(System.getProperty("http.proxyPort","0"));

    protected static final boolean PROXY_DEFINED = PROXY_HOST.length() > 0 && PROXY_PORT > 0;

    protected static final String PROXY_USER = JMeterUtils.getPropDefault(JMeter.HTTP_PROXY_USER,"");

    protected static final String PROXY_PASS = JMeterUtils.getPropDefault(JMeter.HTTP_PROXY_PASS,"");

    protected static final String PROXY_DOMAIN = JMeterUtils.getPropDefault("http.proxyDomain","");

    protected static final InetAddress localAddress;

    protected static final String LOCALHOST;

    protected static final Set<String> nonProxyHostFull = new HashSet<>();

    protected static final List<String> nonProxyHostSuffix = new ArrayList<>();

    protected static final int NON_PROXY_HOST_SUFFIX_SIZE;

    protected static final int CPS_HTTP = JMeterUtils.getPropDefault("httpclient.socket.http.cps", 0);
    
    /**
     * @deprecated Not used
     */
    @Deprecated
    protected static final int CPS_HTTPS = JMeterUtils.getPropDefault("httpclient.socket.https.cps", 0);

    protected static final boolean USE_LOOPBACK = JMeterUtils.getPropDefault("httpclient.loopback", false);
    
    protected static final String HTTP_VERSION = JMeterUtils.getPropDefault("httpclient.version", "1.1");

    // -1 means not defined
    protected static final int SO_TIMEOUT = JMeterUtils.getPropDefault("httpclient.timeout", -1);
    
    /**
     * Reset HTTP State when starting a new Thread Group iteration
     */
    protected static final boolean RESET_STATE_ON_THREAD_GROUP_ITERATION = 
            JMeterUtils.getPropDefault("httpclient.reset_state_on_thread_group_iteration", true);//$NON-NLS-1$

    /**
     * Control reuse of cached SSL Context in subsequent iterations
     * @deprecated use httpclient.reset_state_on_thread_group_iteration instead
     */
    @Deprecated
    protected static final boolean USE_CACHED_SSL_CONTEXT = 
            JMeterUtils.getPropDefault("https.use.cached.ssl.context", false);//$NON-NLS-1$

    /**
     *  Whether SSL State/Context should be reset
     *  Shared state for any HC based implementation, because SSL contexts are the same 
     */
    protected static final ThreadLocal<Boolean> resetStateOnThreadGroupIteration =
            ThreadLocal.withInitial(() -> Boolean.FALSE);
    
    static {
        if(!StringUtils.isEmpty(JMeterUtils.getProperty("httpclient.timeout"))) { //$NON-NLS-1$
            log.warn("You're using property 'httpclient.timeout' that will soon be deprecated for HttpClient3.1, you should either set "
                    + "timeout in HTTP Request GUI, HTTP Request Defaults or set http.socket.timeout in httpclient.parameters");
        }
        if (NONPROXY_HOSTS.length() > 0) {
            StringTokenizer s = new StringTokenizer(NONPROXY_HOSTS,"|");// $NON-NLS-1$
            while (s.hasMoreTokens()) {
                String t = s.nextToken();
                if (t.indexOf('*') ==0) {// e.g. *.apache.org // $NON-NLS-1$
                    nonProxyHostSuffix.add(t.substring(1));
                } else {
                    nonProxyHostFull.add(t);// e.g. www.apache.org
                }
            }
        }
        NON_PROXY_HOST_SUFFIX_SIZE=nonProxyHostSuffix.size();

        InetAddress inet = null;
        String localHostOrIP =
            JMeterUtils.getPropDefault("httpclient.localaddress",""); // $NON-NLS-1$
        if (localHostOrIP.length() > 0) {
            try {
                inet = InetAddress.getByName(localHostOrIP);
                log.info("Using localAddress {}", inet.getHostAddress());
            } catch (UnknownHostException e) {
                log.warn(e.getLocalizedMessage());
            }
        } else {
            // Get hostname
            localHostOrIP = JMeterUtils.getLocalHostName();
        }
        localAddress = inet;
        LOCALHOST = localHostOrIP;
        log.info("Local host = {}", LOCALHOST);
    }

    protected HTTPHCAbstractImpl(HTTPSamplerBase testElement) {
        super(testElement);
    }

    protected static boolean isNonProxy(String host){
        return nonProxyHostFull.contains(host) || isPartialMatch(host);
    }

    protected static boolean isPartialMatch(String host) {
        for (int i=0;i<NON_PROXY_HOST_SUFFIX_SIZE;i++){
            if (host.endsWith(nonProxyHostSuffix.get(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Is a dynamic proxy defined?
     *
     * @param proxyHost the host to check
     * @param proxyPort the port to check
     * @return {@code true} iff both ProxyPort and ProxyHost are defined.
     */
    protected boolean isDynamicProxy(String proxyHost, int proxyPort){
        return !JOrphanUtils.isBlank(proxyHost) && proxyPort > 0;        
    }

    /**
     * Is a static proxy defined?
     * 
     * @param host to check against non-proxy hosts
     * @return {@code true} iff a static proxy has been defined.
     */
    protected static boolean isStaticProxy(String host){
        return PROXY_DEFINED && !isNonProxy(host);
    }
    
    /**
     * @param value String value to test
     * @return true if value is null or empty trimmed
     */
    protected static boolean isNullOrEmptyTrimmed(String value) {
        return JOrphanUtils.isBlank(value);
    }
}
