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
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BestMatchSpecFactory;
import org.apache.http.impl.cookie.BrowserCompatSpecFactory;
import org.apache.http.impl.cookie.IgnoreSpecFactory;
import org.apache.http.impl.cookie.NetscapeDraftSpecFactory;
import org.apache.http.impl.cookie.RFC2109SpecFactory;
import org.apache.http.impl.cookie.RFC2965SpecFactory;
import org.apache.http.message.BasicHeader;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class HC4CookieHandler implements CookieHandler {
    private static final Logger log = LoggingManager.getLoggerForClass();
    
    private final transient CookieSpec cookieSpec;
    
    private static CookieSpecRegistry registry  = new CookieSpecRegistry();

    static {
        registry.register(CookiePolicy.BEST_MATCH, new BestMatchSpecFactory());
        registry.register(CookiePolicy.BROWSER_COMPATIBILITY, new BrowserCompatSpecFactory());
        registry.register(CookiePolicy.RFC_2109, new RFC2109SpecFactory());
        registry.register(CookiePolicy.RFC_2965, new RFC2965SpecFactory());
        registry.register(CookiePolicy.IGNORE_COOKIES, new IgnoreSpecFactory());
        registry.register(CookiePolicy.NETSCAPE, new NetscapeDraftSpecFactory());
    }

    public HC4CookieHandler(String policy) {
        super();
        if (policy.equals(org.apache.commons.httpclient.cookie.CookiePolicy.DEFAULT)) { // tweak diff HC3 vs HC4
            policy = CookiePolicy.BEST_MATCH;
        }
        this.cookieSpec = registry.getCookieSpec(policy);
    }

    @Override
    public void addCookieFromHeader(CookieManager cookieManager,
            boolean checkCookies, String cookieHeader, URL url) {
            boolean debugEnabled = log.isDebugEnabled();
            if (debugEnabled) {
                log.debug("Received Cookie: " + cookieHeader + " From: " + url.toExternalForm());
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
                        cookieSpec.validate(cookie, cookieOrigin);
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
                            exp / 1000
                            );

                    // Store session cookies as well as unexpired ones
                    if (exp == 0 || exp >= System.currentTimeMillis()) {
                        newCookie.setVersion(cookie.getVersion());
                        cookieManager.add(newCookie); // Has its own debug log; removes matching cookies
                    } else {
                        cookieManager.removeMatchingCookies(newCookie);
                        if (debugEnabled){
                            log.info("Dropping expired Cookie: "+newCookie.toString());
                        }
                    }
                } catch (MalformedCookieException e) { // This means the cookie was wrong for the URL
                    log.warn("Not storing invalid cookie: <"+cookieHeader+"> for URL "+url+" ("+e.getLocalizedMessage()+")");
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
            log.debug("Found "+c.size()+" cookies for "+url.toExternalForm());
        }
        if (c.size() <= 0) {
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
        List<org.apache.http.cookie.Cookie> cookies = new ArrayList<org.apache.http.cookie.Cookie>();

        for (PropertyIterator iter = cookiesCP.iterator(); iter.hasNext();) {
            Cookie jmcookie = (Cookie) iter.next().getObjectValue();
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

        List<org.apache.http.cookie.Cookie> cookiesValid = new ArrayList<org.apache.http.cookie.Cookie>();
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
        return ret;
    }
}
