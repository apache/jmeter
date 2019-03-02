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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.control;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.impl.cookie.IgnoreSpecProvider;
import org.apache.http.impl.cookie.NetscapeDraftSpecProvider;
import org.apache.http.impl.cookie.RFC2109SpecProvider;
import org.apache.http.impl.cookie.RFC2965SpecProvider;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.message.BasicHeader;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HC4CookieHandler implements CookieHandler {
    private static final Logger log = LoggerFactory.getLogger(HC4CookieHandler.class);

    // Needed by CookiePanel
    public static final String DEFAULT_POLICY_NAME = CookieSpecs.STANDARD; // NOSONAR 

    private static final String[] AVAILABLE_POLICIES = new String[]{ 
        DEFAULT_POLICY_NAME,
        CookieSpecs.STANDARD_STRICT,
        CookieSpecs.IGNORE_COOKIES,
        CookieSpecs.NETSCAPE,
        CookieSpecs.DEFAULT,
        "rfc2109",
        "rfc2965",
        CookieSpecs.BEST_MATCH,
        CookieSpecs.BROWSER_COMPATIBILITY
    };

    private final transient CookieSpec cookieSpec;
    
    private static final PublicSuffixMatcher publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
    private static Registry<CookieSpecProvider> registry  = 
            RegistryBuilder.<CookieSpecProvider>create()
            // case is ignored bug registry as it converts to lowerCase(Locale.US)
            .register(CookieSpecs.BEST_MATCH, new DefaultCookieSpecProvider(publicSuffixMatcher))
            .register(CookieSpecs.BROWSER_COMPATIBILITY, new DefaultCookieSpecProvider(publicSuffixMatcher))
            .register(CookieSpecs.STANDARD, new RFC6265CookieSpecProvider())
            .register("rfc2109", new RFC2109SpecProvider(publicSuffixMatcher, true)) //$NON-NLS-1$
            .register("rfc2965", new RFC2965SpecProvider(publicSuffixMatcher, true)) //$NON-NLS-1$
            .register(CookieSpecs.STANDARD_STRICT, new RFC6265CookieSpecProvider(
                    org.apache.http.impl.cookie.RFC6265CookieSpecProvider.CompatibilityLevel.STRICT, null))
            .register(CookieSpecs.DEFAULT, new DefaultCookieSpecProvider(publicSuffixMatcher))
            .register(CookieSpecs.IGNORE_COOKIES, new IgnoreSpecProvider())
            .register(CookieSpecs.NETSCAPE, new NetscapeDraftSpecProvider())
            .build();

    /**
     * Default constructor that uses {@link HC4CookieHandler#DEFAULT_POLICY_NAME}
     */
    public HC4CookieHandler() {
        this(DEFAULT_POLICY_NAME);
    }
    
    public HC4CookieHandler(String policy) {
        super();
        if (policy.equalsIgnoreCase("default")) { // tweak diff HC3 vs HC4
            policy = CookieSpecs.DEFAULT;
        }
        HttpClientContext context = HttpClientContext.create();
        this.cookieSpec = registry.lookup(policy).create(context);
    }

    @Override
    public void addCookieFromHeader(CookieManager cookieManager,
            boolean checkCookies, String cookieHeader, URL url) {
            boolean debugEnabled = log.isDebugEnabled();
            if (debugEnabled) {
                log.debug("Received Cookie: {} From: {}", cookieHeader, url.toExternalForm());
            }
            String protocol = url.getProtocol();
            String host = url.getHost();
            int port= HTTPSamplerBase.getDefaultPort(protocol,url.getPort());
            String path = url.getPath();
            boolean isSecure=HTTPSamplerBase.isSecure(protocol);

            List<org.apache.http.cookie.Cookie> cookies = null;
            
            CookieOrigin cookieOrigin = new CookieOrigin(host, port, path, isSecure);
            BasicHeader basicHeader = new BasicHeader(HTTPConstants.HEADER_SET_COOKIE, cookieHeader);

            try {
                cookies = cookieSpec.parse(basicHeader, cookieOrigin);
            } catch (MalformedCookieException e) {
                log.error("Unable to add the cookie", e);
            }
            if (cookies == null) {
                return;
            }
            for (org.apache.http.cookie.Cookie cookie : cookies) {
                try {
                    if (checkCookies) {
                        try {
                            cookieSpec.validate(cookie, cookieOrigin);
                        } catch (MalformedCookieException e) { // This means the cookie was wrong for the URL
                            log.info("Not storing invalid cookie: <{}> for URL {} ({})",
                                cookieHeader, url, e.getLocalizedMessage());
                            continue;
                        }
                    }
                    Date expiryDate = cookie.getExpiryDate();
                    long exp = 0;
                    if (expiryDate!= null) {
                        exp=expiryDate.getTime();
                    }
                    Cookie newCookie = new Cookie(
                            cookie.getName(),
                            cookie.getValue(),
                            cookie.getDomain(),
                            cookie.getPath(),
                            cookie.isSecure(),
                            exp / 1000,
                            ((BasicClientCookie)cookie).containsAttribute(ClientCookie.PATH_ATTR),
                            ((BasicClientCookie)cookie).containsAttribute(ClientCookie.DOMAIN_ATTR),
                            cookie.getVersion());

                    // Store session cookies as well as unexpired ones
                    if (exp == 0 || exp >= System.currentTimeMillis()) {
                        cookieManager.add(newCookie); // Has its own debug log; removes matching cookies
                    } else {
                        cookieManager.removeMatchingCookies(newCookie);
                        if (debugEnabled){
                            log.info("Dropping expired Cookie: {}", newCookie);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    log.warn(cookieHeader+e.getLocalizedMessage());
                }
            }
    }

    @Override
    public String getCookieHeaderForURL(CollectionProperty cookiesCP, URL url,
            boolean allowVariableCookie) {
        List<org.apache.http.cookie.Cookie> c = 
                getCookiesForUrl(cookiesCP, url, allowVariableCookie);
        
        boolean debugEnabled = log.isDebugEnabled();
        if (debugEnabled){
            log.debug("Found {} cookies for {}", c.size(), url);
        }
        if (c.isEmpty()) {
            return null;
        }
        List<Header> lstHdr = cookieSpec.formatCookies(c);
        
        StringBuilder sbHdr = new StringBuilder();
        for (Header header : lstHdr) {
            sbHdr.append(header.getValue());
        }

        return sbHdr.toString();
    }

    /**
     * Get array of valid HttpClient cookies for the URL
     *
     * @param cookiesCP property with all available cookies
     * @param url the target URL
     * @param allowVariableCookie flag whether cookies may contain jmeter variables
     * @return array of HttpClient cookies
     *
     */
    List<org.apache.http.cookie.Cookie> getCookiesForUrl(
            CollectionProperty cookiesCP, URL url, boolean allowVariableCookie) {
        List<org.apache.http.cookie.Cookie> cookies = new ArrayList<>();

        for (JMeterProperty jMeterProperty : cookiesCP) {
            Cookie jmcookie = (Cookie) jMeterProperty.getObjectValue();
            // Set to running version, to allow function evaluation for the cookie values (bug 28715)
            if (allowVariableCookie) {
                jmcookie.setRunningVersion(true);
            }
            cookies.add(makeCookie(jmcookie));
            if (allowVariableCookie) {
                jmcookie.setRunningVersion(false);
            }
        }
        String host = url.getHost();
        String protocol = url.getProtocol();
        int port = HTTPSamplerBase.getDefaultPort(protocol, url.getPort());
        String path = url.getPath();
        boolean secure = HTTPSamplerBase.isSecure(protocol);

        CookieOrigin cookieOrigin = new CookieOrigin(host, port, path, secure);

        List<org.apache.http.cookie.Cookie> cookiesValid = new ArrayList<>();
        for (org.apache.http.cookie.Cookie cookie : cookies) {
            if (cookieSpec.match(cookie, cookieOrigin)) {
                cookiesValid.add(cookie);
            }
        }

        return cookiesValid;
    }
    
    /**
     * Create an HttpClient cookie from a JMeter cookie
     */
    private org.apache.http.cookie.Cookie makeCookie(Cookie jmc) {
        long exp = jmc.getExpiresMillis();
        BasicClientCookie ret = new BasicClientCookie(jmc.getName(),
                jmc.getValue());
        ret.setDomain(jmc.getDomain());
        ret.setPath(jmc.getPath());
        ret.setExpiryDate(exp > 0 ? new Date(exp) : null); // use null for no expiry
        ret.setSecure(jmc.getSecure());
        ret.setVersion(jmc.getVersion());
        if(jmc.isDomainSpecified()) {
            ret.setAttribute(ClientCookie.DOMAIN_ATTR, jmc.getDomain());
        }
        if(jmc.isPathSpecified()) {
            ret.setAttribute(ClientCookie.PATH_ATTR, jmc.getPath());
        }
        return ret;
    }
    
    @Override
    public String getDefaultPolicy() {
        return DEFAULT_POLICY_NAME; 
    }

    @Override
    public String[] getPolicies() {
        return AVAILABLE_POLICIES;
    }
}
