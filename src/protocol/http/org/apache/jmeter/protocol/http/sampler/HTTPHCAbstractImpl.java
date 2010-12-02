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

import org.apache.jmeter.JMeter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Common parent class for HttpClient implementations.
 * 
 * Includes system property settings that are handled internally by the Java HTTP implementation,
 * but which need to be explicitly configured in HttpClient implementations. 
 */
public abstract class HTTPHCAbstractImpl extends HTTPAbstractImpl {

    private static final Logger log = LoggingManager.getLoggerForClass();

    protected static final String HTTP_AUTHENTICATION_PREEMPTIVE = "http.authentication.preemptive"; // $NON-NLS-1$

    protected static final String PROXY_HOST = System.getProperty("http.proxyHost","");

    protected static final String NONPROXY_HOSTS = System.getProperty("http.nonProxyHosts","");

    protected static final int PROXY_PORT = Integer.parseInt(System.getProperty("http.proxyPort","0"));

    protected static final boolean PROXY_DEFINED = PROXY_HOST.length() > 0 && PROXY_PORT > 0;

    protected static final String PROXY_USER = JMeterUtils.getPropDefault(JMeter.HTTP_PROXY_USER,"");

    protected static final String PROXY_PASS = JMeterUtils.getPropDefault(JMeter.HTTP_PROXY_PASS,"");

    protected static final String PROXY_DOMAIN = JMeterUtils.getPropDefault("http.proxyDomain","");

    public static final String IP_SOURCE = "HTTPSampler.ipSource";

    protected static final InetAddress localAddress;

    protected static final String localHost;

    protected static final Set<String> nonProxyHostFull = new HashSet<String>();

    protected static final List<String> nonProxyHostSuffix = new ArrayList<String>();

    protected static final int nonProxyHostSuffixSize;

    static {
        if (NONPROXY_HOSTS.length() > 0){
            StringTokenizer s = new StringTokenizer(NONPROXY_HOSTS,"|");// $NON-NLS-1$
            while (s.hasMoreTokens()){
                String t = s.nextToken();
                if (t.indexOf("*") ==0){// e.g. *.apache.org // $NON-NLS-1$
                    nonProxyHostSuffix.add(t.substring(1));
                } else {
                    nonProxyHostFull.add(t);// e.g. www.apache.org
                }
            }
        }
        nonProxyHostSuffixSize=nonProxyHostSuffix.size();

        InetAddress inet=null;
        String localHostOrIP =
            JMeterUtils.getPropDefault("httpclient.localaddress",""); // $NON-NLS-1$
        if (localHostOrIP.length() > 0){
            try {
                inet = InetAddress.getByName(localHostOrIP);
                log.info("Using localAddress "+inet.getHostAddress());
            } catch (UnknownHostException e) {
                log.warn(e.getLocalizedMessage());
            }
        } else {
            try {
                InetAddress addr = InetAddress.getLocalHost();
                // Get hostname
                localHostOrIP = addr.getHostName();
            } catch (UnknownHostException e) {
                log.warn("Cannot determine localhost name, and httpclient.localaddress was not specified");
            }
        }
        localAddress = inet;
        localHost = localHostOrIP;
        log.info("Local host = "+localHost);

    }

    protected HTTPHCAbstractImpl(HTTPSamplerBase testElement) {
        super(testElement);
    }

    protected static boolean isNonProxy(String host){
        return nonProxyHostFull.contains(host) || isPartialMatch(host);
    }

    protected static boolean isPartialMatch(String host) {
        for (int i=0;i<nonProxyHostSuffixSize;i++){
            if (host.endsWith(nonProxyHostSuffix.get(i))) {
                return true;
            }
        }
        return false;
    }


}
