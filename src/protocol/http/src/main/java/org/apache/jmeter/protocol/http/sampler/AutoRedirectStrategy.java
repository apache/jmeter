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

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redirect strategy of {@link HTTPHC5Impl} for the redirects HttpClient follows on its own, when
 * "Redirect Automatically" is selected.
 * <p>
 * HttpClient sends every hop with the headers of the initial request, except when the hop changes the
 * method (301/302 after POST, 303 after any non-GET), where it builds a GET with no headers at all.
 * Its default strategy refuses to follow a redirect to another origin when the hop includes a
 * {@code Cookie} or an {@code Authorization} header, so as not to leak them, and returns the redirect
 * as the response.
 * JMeter sets both headers itself, from the Cookie and the Authorization Manager, so a redirect to an
 * identity provider, for example, would end the sample. Instead, each hop is sent with what the
 * managers hold for its own URL, the way a sample of "Follow Redirects" would be:
 * <ul>
 * <li>The cookies a redirect sets are stored in the Cookie Manager, against the URL of the redirect.</li>
 * <li>The {@code Cookie} header holds the cookies of the Cookie Manager for the URL of the hop.</li>
 * <li>The credentials of the Authorization Manager for that URL answer a challenge of the host of the hop,
 * and those of an entry with the BASIC mechanism are sent pre-emptively, see {@link HC5Authentication}.</li>
 * <li>A {@code Cookie} or {@code Authorization} header of the Header Manager is only sent to the origin
 * of the sampled URL, and only where the managers do not replace it, as for the initial request.</li>
 * <li>Other Header Manager headers are re-applied when HttpClient does not copy them, except that a
 * {@code Host} override is only kept for the sampled origin.</li>
 * </ul>
 */
final class AutoRedirectStrategy extends DefaultRedirectStrategy {

    private static final Logger log = LoggerFactory.getLogger(AutoRedirectStrategy.class);

    static final AutoRedirectStrategy INSTANCE = new AutoRedirectStrategy();

    private static final String CONTEXT_ATTRIBUTE = "__jmeter.AUTO_REDIRECTS__"; //$NON-NLS-1$

    private AutoRedirectStrategy() {
    }

    /**
     * Sets up the hops of the request executed with the given context.
     *
     * @param context       context the request is executed with
     * @param url           URL of the sampled request
     * @param cookieManager holds the cookies of the thread, may be {@code null}
     * @param authManager   holds the credentials of the thread, may be {@code null}
     * @param headerManager holds the headers of the sampler, may be {@code null}
     * @param credentials   credentials of the request, which HttpClient answers challenges with
     */
    static void attach(HttpClientContext context, URL url, CookieManager cookieManager, AuthManager authManager,
            HeaderManager headerManager, BasicCredentialsProvider credentials) {
        context.setAttribute(CONTEXT_ATTRIBUTE,
                new Hops(url, cookieManager, authManager, headerManager, credentials));
    }

    @Override
    public boolean isRedirectAllowed(HttpHost currentTarget, HttpHost newTarget, HttpRequest redirect,
            HttpContext context) {
        if (!(context.getAttribute(CONTEXT_ATTRIBUTE) instanceof Hops hops)) {
            return super.isRedirectAllowed(currentTarget, newTarget, redirect, context);
        }
        URL url;
        try {
            url = redirect.getUri().toURL();
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            log.debug("Not applying the managers to the redirect to {}", newTarget, e);
            return super.isRedirectAllowed(currentTarget, newTarget, redirect, context);
        }
        hops.next(HttpClientContext.cast(context), url, redirect);
        return true;
    }

    /** State of the redirects HttpClient follows for a single sample. */
    private static final class Hops {
        private final URL sampledUrl;
        private final CookieManager cookieManager;
        private final AuthManager authManager;
        private final List<String> headerManagerCookies = new ArrayList<>();
        private final List<String> headerManagerAuthorizations = new ArrayList<>();
        /** Header Manager headers other than Cookie and Authorization, as name-value pairs. */
        private final List<String[]> headerManagerGeneral = new ArrayList<>();
        private final BasicCredentialsProvider credentials;
        /** URL the response being redirected was received from. */
        private URL currentUrl;

        private Hops(URL url, CookieManager cookieManager, AuthManager authManager, HeaderManager headerManager,
                BasicCredentialsProvider credentials) {
            this.sampledUrl = url;
            this.currentUrl = url;
            this.cookieManager = cookieManager;
            this.authManager = authManager;
            this.credentials = credentials;
            HTTPHCAbstractImpl.setConnectionHeaders(headerManager, url,
                    name -> true,
                    (name, value) -> {
                        if (HttpHeaders.COOKIE.equalsIgnoreCase(name)) {
                            headerManagerCookies.add(value);
                        } else if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                            headerManagerAuthorizations.add(value);
                        } else {
                            headerManagerGeneral.add(new String[]{name, value});
                        }
                    });
        }

        /**
         * Prepares the request of the next hop.
         *
         * @param context context of the sample, which holds the response being redirected
         * @param url     URL of the next hop
         * @param request request of the next hop
         */
        void next(HttpClientContext context, URL url, HttpRequest request) {
            HttpResponse redirect = context.getResponse();
            if (redirect != null) {
                HTTPHCAbstractImpl.saveConnectionCookies(action -> {
                    for (Header header : redirect.getHeaders()) {
                        action.accept(header.getName(), header.getValue());
                    }
                }, currentUrl, cookieManager);
            }
            boolean sampledOrigin = isSameOrigin(url, sampledUrl);

            request.removeHeaders(HttpHeaders.COOKIE);
            String cookies = cookieManager == null ? null : cookieManager.getCookieHeaderForURL(url);
            if (cookies != null) {
                request.setHeader(HttpHeaders.COOKIE, cookies);
            } else if (sampledOrigin) {
                headerManagerCookies.forEach(value -> request.addHeader(HttpHeaders.COOKIE, value));
            }

            request.removeHeaders(HttpHeaders.AUTHORIZATION);
            Authorization authorization = authManager == null ? null : authManager.getAuthForURL(url);
            boolean kerberos = authorization != null
                    && AuthManager.Mechanism.KERBEROS.equals(authorization.getMechanism());
            if (authorization != null && !kerberos) {
                HC5Authentication.setTargetCredentials(credentials, url, authorization);
            }
            if (authorization != null && AuthManager.Mechanism.BASIC.equals(authorization.getMechanism())) {
                HC5Authentication.preemptBasic(context, url, request, authorization);
            } else if (sampledOrigin) {
                headerManagerAuthorizations.forEach(value -> request.addHeader(HttpHeaders.AUTHORIZATION, value));
            }

            // For method-changing redirects (301/302 after POST, 303 after non-GET), HttpClient
            // builds a GET with no headers, losing the Header Manager headers the initial request
            // carried. Re-apply them so that Accept, User-Agent, and custom headers survive.
            Set<String> present = new HashSet<>();
            for (Header header : request.getHeaders()) {
                present.add(header.getName().toLowerCase(Locale.ROOT));
            }
            for (String[] header : headerManagerGeneral) {
                if (present.contains(header[0].toLowerCase(Locale.ROOT))) {
                    continue;
                }
                // Host is specific to the sampled origin. Let HttpClient generate it for another origin.
                if (HttpHeaders.HOST.equalsIgnoreCase(header[0]) && !sampledOrigin) {
                    continue;
                }
                request.addHeader(header[0], header[1]);
            }
            currentUrl = url;
        }

        private static boolean isSameOrigin(URL url, URL other) {
            return url.getProtocol().equalsIgnoreCase(other.getProtocol())
                    && url.getHost().toLowerCase(Locale.ROOT).equals(other.getHost().toLowerCase(Locale.ROOT))
                    && getPort(url) == getPort(other);
        }

        private static int getPort(URL url) {
            return url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
        }
    }
}
