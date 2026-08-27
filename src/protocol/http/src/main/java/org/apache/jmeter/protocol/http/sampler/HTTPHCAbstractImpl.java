/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.sampler;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.jmeter.JMeter;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.StringUtilities;
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

    protected static final String PROXY_SCHEME = System.getProperty("http.proxyScheme","http");

    protected static final String PROXY_HOST = System.getProperty("http.proxyHost","");

    protected static final String NONPROXY_HOSTS = System.getProperty("http.nonProxyHosts","");

    protected static final int PROXY_PORT = Integer.parseInt(System.getProperty("http.proxyPort","0"));

    protected static final boolean PROXY_DEFINED = !PROXY_HOST.isEmpty() && PROXY_PORT > 0;

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

    /** Default value of the sampler {@code HTTPSampler.httpVersion} property, see {@code httpclient.version}. */
    protected static final String DEFAULT_HTTP_VERSION = readDefaultHttpVersion();

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
            ThreadLocal.withInitial(() -> false);

    static {
        if (!JMeterUtils.getPropDefault("httpclient.timeout", "").isEmpty()) { //$NON-NLS-1$
            log.warn("You're using property 'httpclient.timeout' that will soon be deprecated for HttpClient3.1, you should either set "
                    + "timeout in HTTP Request GUI, HTTP Request Defaults or set http.socket.timeout in httpclient.parameters");
        }
        if (!NONPROXY_HOSTS.isEmpty()) {
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
        if (!localHostOrIP.isEmpty()) {
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
        return StringUtilities.isNotBlank(proxyHost) && proxyPort > 0;
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
        return StringUtilities.isBlank(value);
    }

    /** Matches the port of a {@code Host} header, only used in {@code matches()}, so unanchored. */
    private static final Pattern PORT_PATTERN = Pattern.compile("\\d+");

    /**
     * Iterates the headers of a request or a response, so that the helpers below can be shared by
     * the HttpClient implementations although they represent a header with different types.
     */
    @FunctionalInterface
    protected interface HeaderIterable {
        /**
         * @param action called with the name and the value of every header, in the order the
         *               message holds them
         */
        void forEach(BiConsumer<String, String> action);
    }

    /**
     * Renders the headers matching the predicate the way the sample result reports them, that is
     * {@code name: value} per line.
     *
     * @param headers   headers of the request or the response
     * @param predicate selects the headers to render by name
     * @return the headers as a string, one per line
     */
    protected static String formatHeaders(HeaderIterable headers, Predicate<? super String> predicate) {
        StringBuilder result = new StringBuilder(150);
        headers.forEach((name, value) -> {
            if (predicate.test(name)) {
                result.append(name).append(": ").append(value).append('\n'); // $NON-NLS-1$
            }
        });
        return result.toString();
    }

    /**
     * Extracts the cookies of the {@link CookieManager} that apply to the URL and sets them as the
     * {@code Cookie} header of the request.
     *
     * @param url           URL of the request
     * @param cookieManager holds the cookies, may be {@code null}
     * @param setHeader     sets a header of the request, replacing an existing one of that name
     * @return the value of the {@code Cookie} header, {@code null} if there is none
     */
    protected static String setConnectionCookie(URL url, CookieManager cookieManager,
            BiConsumer<String, String> setHeader) {
        if (cookieManager == null) {
            return null;
        }
        String cookieHeader = cookieManager.getCookieHeaderForURL(url);
        if (cookieHeader != null) {
            setHeader.accept(HTTPConstants.HEADER_COOKIE, cookieHeader);
        }
        return cookieHeader;
    }

    /**
     * Reads the cookies the request carries itself, which is how they are reported while recording,
     * when the {@link CookieManager} does not handle them.
     *
     * @param headers headers of the request
     * @return the value of the {@code Cookie} header, the values of several such headers joined the
     *         way a single header would hold them, or an empty string if the request has none
     */
    protected static String getOnlyCookieFromHeaders(HeaderIterable headers) {
        StringBuilder cookies = new StringBuilder();
        headers.forEach((name, value) -> {
            if (HTTPConstants.HEADER_COOKIE.equalsIgnoreCase(name) && StringUtilities.isNotBlank(value)) {
                if (cookies.length() > 0) {
                    cookies.append("; "); // $NON-NLS-1$
                }
                cookies.append(value.trim());
            }
        });
        return cookies.toString();
    }

    /**
     * Hands the {@code Set-Cookie} headers of a response to the {@link CookieManager}.
     *
     * @param headers       headers of the response
     * @param url           URL the response was received from
     * @param cookieManager stores the cookies, may be {@code null}
     */
    protected static void saveConnectionCookies(HeaderIterable headers, URL url, CookieManager cookieManager) {
        if (cookieManager == null) {
            return;
        }
        headers.forEach((name, value) -> {
            if (HTTPConstants.HEADER_SET_COOKIE.equalsIgnoreCase(name)) {
                cookieManager.addCookieFromHeader(value, url);
            }
        });
    }

    /**
     * Adds the headers of the {@link HeaderManager} to the request. {@code Content-Length} is never
     * added, as it is determined by the request body, and a {@code Host} header is normalized so
     * that a port which is the default one for the URL is left out.
     *
     * @param headerManager holds the headers of the test plan, may be {@code null}
     * @param url           URL of the request, used to normalize the {@code Host} header
     * @param isAllowed     additional filter of the implementation, by header name
     * @param addHeader     adds a header to the request, keeping the ones already set
     */
    protected static void setConnectionHeaders(HeaderManager headerManager, URL url,
            Predicate<? super String> isAllowed, BiConsumer<String, String> addHeader) {
        if (headerManager == null) {
            return;
        }
        CollectionProperty headers = headerManager.getHeaders();
        if (headers == null) {
            return;
        }
        for (JMeterProperty property : headers) {
            Header header = (Header) property.getObjectValue();
            String headerName = header.getName();
            // Don't allow override of Content-Length
            if (HTTPConstants.HEADER_CONTENT_LENGTH.equalsIgnoreCase(headerName) || !isAllowed.test(headerName)) {
                continue;
            }
            if (HTTPConstants.HEADER_HOST.equalsIgnoreCase(headerName)) {
                addHeader.accept(HTTPConstants.HEADER_HOST, normalizeHostHeader(header.getValue(), url));
            } else {
                addHeader.accept(headerName, header.getValue());
            }
        }
    }

    /**
     * Removes the port from the value of a {@code Host} header when it is the default port of the
     * URL, so that the header looks the way a browser would send it.
     *
     * @param headerValue value of the {@code Host} header
     * @param url         URL of the request
     * @return the value to send
     */
    private static String normalizeHostHeader(String headerValue, URL url) {
        int port = getPortFromHostHeader(headerValue, url.getPort());
        // remove any port specification
        String host = headerValue.replaceFirst(":\\d+$", ""); // $NON-NLS-1$ $NON-NLS-2$
        if (port == -1 || port == url.getDefaultPort()) {
            // no need to specify the port if it is the default
            return host;
        }
        return host + ":" + port; // $NON-NLS-1$
    }

    /**
     * Get port from the value of the Host header, or return the given defaultValue
     *
     * @param hostHeaderValue value of the http Host header
     * @param defaultValue    value to be used, when no port could be extracted from
     *                        hostHeaderValue
     * @return integer representing the port for the host header
     */
    private static int getPortFromHostHeader(String hostHeaderValue, int defaultValue) {
        String[] hostParts = hostHeaderValue.split(":");
        if (hostParts.length > 1) {
            String portString = hostParts[hostParts.length - 1];
            if (PORT_PATTERN.matcher(portString).matches()) {
                return Integer.parseInt(portString);
            }
        }
        return defaultValue;
    }

    /**
     * Passes the name and the value of every parameter that goes into a url-encoded form body to
     * the consumer. A parameter the user entered already encoded is decoded first, so that the
     * encoding HttpClient applies ends up with the value the user had entered.
     *
     * @param contentEncoding encoding of the request body
     * @param consumer        called with the name and the value of every parameter
     * @throws UnsupportedEncodingException when the content encoding is not supported
     */
    protected void forEachFormParameter(String contentEncoding, BiConsumer<String, String> consumer)
            throws UnsupportedEncodingException {
        for (JMeterProperty property : getArguments().getEnabledArguments()) {
            HTTPArgument argument = (HTTPArgument) property.getObjectValue();
            String name = argument.getName();
            if (argument.isSkippable(name)) {
                continue;
            }
            String value = argument.getValue();
            if (!argument.isAlwaysEncoded()) {
                name = URLDecoder.decode(name, contentEncoding);
                value = URLDecoder.decode(value, contentEncoding);
            }
            consumer.accept(name, value);
        }
    }
}
