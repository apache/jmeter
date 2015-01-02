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

package org.apache.jmeter.protocol.http.control;

import java.net.URL;
import java.util.Date;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.cookie.MalformedCookieException;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * HTTPClient 3.1 implementation
 */
public class HC3CookieHandler implements CookieHandler {
   private static final Logger log = LoggingManager.getLoggerForClass();

    private final transient CookieSpec cookieSpec;

    /**
     * @param policy
     *            cookie policy to which to conform (see
     *            {@link CookiePolicy#getCookieSpec(String)}
     */
    public HC3CookieHandler(String policy) {
        super();
        this.cookieSpec = CookiePolicy.getCookieSpec(policy);
    }

    /**
     * Create an HttpClient cookie from a JMeter cookie
     */
    private org.apache.commons.httpclient.Cookie makeCookie(Cookie jmc){
        long exp = jmc.getExpiresMillis();
        org.apache.commons.httpclient.Cookie ret=
            new org.apache.commons.httpclient.Cookie(
                jmc.getDomain(),
                jmc.getName(),
                jmc.getValue(),
                jmc.getPath(),
                exp > 0 ? new Date(exp) : null, // use null for no expiry
                jmc.getSecure()
               );
        ret.setPathAttributeSpecified(jmc.isPathSpecified());
        ret.setDomainAttributeSpecified(jmc.isDomainSpecified());
        ret.setVersion(jmc.getVersion());
        return ret;
    }
    /**
     * Get array of valid HttpClient cookies for the URL
     *
     * @param cookiesCP cookies to consider
     * @param url the target URL
     * @param allowVariableCookie flag whether to allow jmeter variables in cookie values
     * @return array of HttpClient cookies
     *
     */
    org.apache.commons.httpclient.Cookie[] getCookiesForUrl(
            CollectionProperty cookiesCP,
            URL url, 
            boolean allowVariableCookie){
        org.apache.commons.httpclient.Cookie cookies[]=
            new org.apache.commons.httpclient.Cookie[cookiesCP.size()];
        int i=0;
        for (PropertyIterator iter = cookiesCP.iterator(); iter.hasNext();) {
            Cookie jmcookie = (Cookie) iter.next().getObjectValue();
            // Set to running version, to allow function evaluation for the cookie values (bug 28715)
            if (allowVariableCookie) {
                jmcookie.setRunningVersion(true);
            }
            cookies[i++] = makeCookie(jmcookie);
            if (allowVariableCookie) {
                jmcookie.setRunningVersion(false);
            }
        }
        String host = url.getHost();
        String protocol = url.getProtocol();
        int port= HTTPSamplerBase.getDefaultPort(protocol,url.getPort());
        String path = url.getPath();
        boolean secure = HTTPSamplerBase.isSecure(protocol);
        return cookieSpec.match(host, port, path, secure, cookies);
    }
    
    /**
     * Find cookies applicable to the given URL and build the Cookie header from
     * them.
     *
     * @param url
     *            URL of the request to which the returned header will be added.
     * @return the value string for the cookie header (goes after "Cookie: ").
     */
    @Override
    public String getCookieHeaderForURL(
            CollectionProperty cookiesCP,
            URL url,
            boolean allowVariableCookie) {
        org.apache.commons.httpclient.Cookie[] c = 
                getCookiesForUrl(cookiesCP, url, allowVariableCookie);
        int count = c.length;
        boolean debugEnabled = log.isDebugEnabled();
        if (debugEnabled){
            log.debug("Found "+count+" cookies for "+url.toExternalForm());
        }
        if (count <=0){
            return null;
        }
        String hdr=cookieSpec.formatCookieHeader(c).getValue();
        if (debugEnabled){
            log.debug("Cookie: "+hdr);
        }
        return hdr;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void addCookieFromHeader(CookieManager cookieManager,
            boolean checkCookies,String cookieHeader, URL url){
        boolean debugEnabled = log.isDebugEnabled();
        if (debugEnabled) {
            log.debug("Received Cookie: " + cookieHeader + " From: " + url.toExternalForm());
        }
        String protocol = url.getProtocol();
        String host = url.getHost();
        int port= HTTPSamplerBase.getDefaultPort(protocol,url.getPort());
        String path = url.getPath();
        boolean isSecure=HTTPSamplerBase.isSecure(protocol);
        org.apache.commons.httpclient.Cookie[] cookies= null;
        try {
            cookies = cookieSpec.parse(host, port, path, isSecure, cookieHeader);
        } catch (MalformedCookieException e) {
            log.warn(cookieHeader+e.getLocalizedMessage());
        } catch (IllegalArgumentException e) {
            log.warn(cookieHeader+e.getLocalizedMessage());
        }
        if (cookies == null) {
            return;
        }
        for(org.apache.commons.httpclient.Cookie cookie : cookies){
            try {
                if (checkCookies) {
                    cookieSpec.validate(host, port, path, isSecure, cookie);
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
                        cookie.getSecure(),
                        exp / 1000,
                        cookie.isPathAttributeSpecified(),
                        cookie.isDomainAttributeSpecified()
                        );

                // Store session cookies as well as unexpired ones
                if (exp == 0 || exp >= System.currentTimeMillis()) {
                    newCookie.setVersion(cookie.getVersion());
                    cookieManager.add(newCookie); // Has its own debug log; removes matching cookies
                } else {
                    cookieManager.removeMatchingCookies(newCookie);
                    if (debugEnabled){
                        log.debug("Dropping expired Cookie: "+newCookie.toString());
                    }
                }
            } catch (MalformedCookieException e) { // This means the cookie was wrong for the URL
                log.warn("Not storing invalid cookie: <"+cookieHeader+"> for URL "+url+" ("+e.getLocalizedMessage()+")");
            } catch (IllegalArgumentException e) {
                log.warn(cookieHeader+e.getLocalizedMessage());
            }
        }

    }
}
