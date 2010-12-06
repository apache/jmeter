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

// For unit tests @see TestCookieManager

package org.apache.jmeter.protocol.http.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.cookie.MalformedCookieException;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * This class provides an interface to the netscape cookies file to pass cookies
 * along with a request.
 *
 * Now uses Commons HttpClient parsing and matching code (since 2.1.2)
 *
 */
public class CookieManager extends ConfigTestElement implements TestListener, Serializable {
    private static final long serialVersionUID = 233L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    //++ JMX tag values
    private static final String CLEAR = "CookieManager.clearEachIteration";// $NON-NLS-1$

    private static final String COOKIES = "CookieManager.cookies";// $NON-NLS-1$

    private static final String POLICY = "CookieManager.policy"; //$NON-NLS-1$
    //-- JMX tag values

    private static final String TAB = "\t"; //$NON-NLS-1$

    // See bug 33796
    private static final boolean DELETE_NULL_COOKIES =
        JMeterUtils.getPropDefault("CookieManager.delete_null_cookies", true);// $NON-NLS-1$

    // See bug 28715
    private static final boolean ALLOW_VARIABLE_COOKIES
        = JMeterUtils.getPropDefault("CookieManager.allow_variable_cookies", true);// $NON-NLS-1$

    private static final String COOKIE_NAME_PREFIX =
        JMeterUtils.getPropDefault("CookieManager.name.prefix", "COOKIE_").trim();// $NON-NLS-1$ $NON-NLS-2$

    private static final boolean SAVE_COOKIES =
        JMeterUtils.getPropDefault("CookieManager.save.cookies", false);// $NON-NLS-1$

    private static final boolean CHECK_COOKIES =
        JMeterUtils.getPropDefault("CookieManager.check.cookies", true);// $NON-NLS-1$

    private transient CookieSpec cookieSpec;

    private transient CollectionProperty initialCookies;

    public static final String DEFAULT_POLICY = CookiePolicy.BROWSER_COMPATIBILITY;

    public CookieManager() {
        clearCookies(); // Ensure that there is always a collection available
    }

    // ensure that the initial cookies are copied to the per-thread instances
    /** {@inheritDoc} */
    @Override
    public Object clone(){
        CookieManager clone = (CookieManager) super.clone();
        clone.initialCookies = initialCookies;
        clone.cookieSpec = cookieSpec;
        return clone;
    }

    public String getPolicy() {
        return getPropertyAsString(POLICY, DEFAULT_POLICY);
    }

    public void setCookiePolicy(String policy){
        setProperty(POLICY, policy, DEFAULT_POLICY);
    }

    public CollectionProperty getCookies() {
        return (CollectionProperty) getProperty(COOKIES);
    }

    public int getCookieCount() {// Used by GUI
        return getCookies().size();
    }

    public boolean getClearEachIteration() {
        return getPropertyAsBoolean(CLEAR);
    }

    public void setClearEachIteration(boolean clear) {
        setProperty(new BooleanProperty(CLEAR, clear));
    }

    /**
     * Save the static cookie data to a file.
     * Cookies are only taken from the GUI - runtime cookies are not included.
     */
    public void save(String authFile) throws IOException {
        File file = new File(authFile);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir") // $NON-NLS-1$
                    + File.separator + authFile);
        }
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        writer.println("# JMeter generated Cookie file");// $NON-NLS-1$
        PropertyIterator cookies = getCookies().iterator();
        long now = System.currentTimeMillis();
        while (cookies.hasNext()) {
            Cookie cook = (Cookie) cookies.next().getObjectValue();
            final long expiresMillis = cook.getExpiresMillis();
            if (expiresMillis == 0 || expiresMillis > now) { // only save unexpired cookies
                writer.println(cookieToString(cook));
            }
        }
        writer.flush();
        writer.close();
    }

    /**
     * Add cookie data from a file.
     */
    public void addFile(String cookieFile) throws IOException {
        File file = new File(cookieFile);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir") // $NON-NLS-1$
                    + File.separator + cookieFile);
        }
        BufferedReader reader = null;
        if (file.canRead()) {
            reader = new BufferedReader(new FileReader(file));
        } else {
            throw new IOException("The file you specified cannot be read.");
        }

        // N.B. this must agree with the save() and cookieToString() methods
        String line;
        try {
            final CollectionProperty cookies = getCookies();
            while ((line = reader.readLine()) != null) {
                try {
                    if (line.startsWith("#") || line.trim().length() == 0) {//$NON-NLS-1$
                        continue;
                    }
                    String[] st = JOrphanUtils.split(line, TAB, false);

                    final int _domain = 0;
                    //final int _ignored = 1;
                    final int _path = 2;
                    final int _secure = 3;
                    final int _expires = 4;
                    final int _name = 5;
                    final int _value = 6;
                    final int _fields = 7;
                    if (st.length!=_fields) {
                        throw new IOException("Expected "+_fields+" fields, found "+st.length+" in "+line);
                    }

                    if (st[_path].length()==0) {
                        st[_path] = "/"; //$NON-NLS-1$
                    }
                    boolean secure = Boolean.valueOf(st[_secure]).booleanValue();
                    long expires = new Long(st[_expires]).longValue();
                    if (expires==Long.MAX_VALUE) {
                        expires=0;
                    }
                    //long max was used to represent a non-expiring cookie, but that caused problems
                    Cookie cookie = new Cookie(st[_name], st[_value], st[_domain], st[_path], secure, expires);
                    cookies.addItem(cookie);
                } catch (NumberFormatException e) {
                    throw new IOException("Error parsing cookie line\n\t'" + line + "'\n\t" + e);
                }
            }
        } finally {
            reader.close();
         }
    }

    private String cookieToString(Cookie c){
        StringBuilder sb=new StringBuilder(80);
        sb.append(c.getDomain());
        //flag - if all machines within a given domain can access the variable.
        //(from http://www.cookiecentral.com/faq/ 3.5)
        sb.append(TAB).append("TRUE");
        sb.append(TAB).append(c.getPath());
        sb.append(TAB).append(JOrphanUtils.booleanToSTRING(c.getSecure()));
        sb.append(TAB).append(c.getExpires());
        sb.append(TAB).append(c.getName());
        sb.append(TAB).append(c.getValue());
        return sb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public void recoverRunningVersion() {
        // do nothing, the cookie manager has to accept changes.
    }

    /** {@inheritDoc} */
    @Override
    public void setRunningVersion(boolean running) {
        // do nothing, the cookie manager has to accept changes.
    }

    /**
     * Add a cookie.
     */
    public void add(Cookie c) {
        String cv = c.getValue();
        String cn = c.getName();
        removeMatchingCookies(c); // Can't have two matching cookies

        if (DELETE_NULL_COOKIES && (null == cv || cv.length()==0)) {
            if (log.isDebugEnabled()) {
                log.debug("Dropping cookie with null value " + c.toString());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Add cookie to store " + c.toString());
            }
            getCookies().addItem(c);
            if (SAVE_COOKIES)  {
                JMeterContext context = getThreadContext();
                if (context.isSamplingStarted()) {
                    context.getVariables().put(COOKIE_NAME_PREFIX+cn, cv);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void clear(){
        super.clear();
        clearCookies(); // ensure data is set up OK initially
    }

    /*
     * Remove all the cookies.
     */
    private void clearCookies() {
        log.debug("Clear all cookies from store");
        setProperty(new CollectionProperty(COOKIES, new ArrayList<Object>()));
    }

    /**
     * Remove a cookie.
     */
    public void remove(int index) {// TODO not used by GUI
        getCookies().remove(index);
    }

    /**
     * Return the cookie at index i.
     */
    public Cookie get(int i) {// Only used by GUI
        return (Cookie) getCookies().get(i).getObjectValue();
    }

    /*
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
     * @param url the target URL
     * @return array of HttpClient cookies
     *
     */
    public org.apache.commons.httpclient.Cookie[] getCookiesForUrl(URL url){
        CollectionProperty jar=getCookies();
        org.apache.commons.httpclient.Cookie cookies[]=
            new org.apache.commons.httpclient.Cookie[jar.size()];
        int i=0;
        for (PropertyIterator iter = getCookies().iterator(); iter.hasNext();) {
            Cookie jmcookie = (Cookie) iter.next().getObjectValue();
            // Set to running version, to allow function evaluation for the cookie values (bug 28715)
            if (ALLOW_VARIABLE_COOKIES) {
                jmcookie.setRunningVersion(true);
            }
            cookies[i++] = makeCookie(jmcookie);
            if (ALLOW_VARIABLE_COOKIES) {
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
    public String getCookieHeaderForURL(URL url) {
        org.apache.commons.httpclient.Cookie[] c = getCookiesForUrl(url);
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


    public void addCookieFromHeader(String cookieHeader, URL url){
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
        for(int i=0;i<cookies.length;i++){
            org.apache.commons.httpclient.Cookie cookie = cookies[i];
            try {
                if (CHECK_COOKIES) {
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
                    add(newCookie); // Has its own debug log; removes matching cookies
                } else {
                    removeMatchingCookies(newCookie);
                    if (debugEnabled){
                        log.debug("Dropping expired Cookie: "+newCookie.toString());
                    }
                }
            } catch (MalformedCookieException e) { // This means the cookie was wrong for the URL
                log.debug("Not storing invalid cookie: <"+cookieHeader+"> for URL "+url+" ("+e.getLocalizedMessage()+")");
            } catch (IllegalArgumentException e) {
                log.warn(cookieHeader+e.getLocalizedMessage());
            }
        }

    }
    /**
     * Check if cookies match, i.e. name, path and domain are equal.
     * <br/>
     * TODO - should we compare secure too?
     * @param a
     * @param b
     * @return true if cookies match
     */
    private boolean match(Cookie a, Cookie b){
        return
        a.getName().equals(b.getName())
        &&
        a.getPath().equals(b.getPath())
        &&
        a.getDomain().equals(b.getDomain());
    }

    private void removeMatchingCookies(Cookie newCookie){
        // Scan for any matching cookies
        PropertyIterator iter = getCookies().iterator();
        while (iter.hasNext()) {
            Cookie cookie = (Cookie) iter.next().getObjectValue();
            if (cookie == null) {// TODO is this possible?
                continue;
            }
            if (match(cookie,newCookie)) {
                if (log.isDebugEnabled()) {
                    log.debug("New Cookie = " + newCookie.toString()
                              + " removing matching Cookie " + cookie.toString());
                }
                iter.remove();
            }
        }
    }

    /** {@inheritDoc} */
    public void testStarted() {
        initialCookies = getCookies();
        cookieSpec = CookiePolicy.getCookieSpec(getPolicy());
        if (log.isDebugEnabled()){
            log.debug("Policy: "+getPolicy()+" Clear: "+getClearEachIteration());
        }
    }

    /** {@inheritDoc} */
    public void testEnded() {
    }

    /** {@inheritDoc} */
    public void testStarted(String host) {
        testStarted();
    }

    /** {@inheritDoc} */
    public void testEnded(String host) {
    }

    /** {@inheritDoc} */
    public void testIterationStart(LoopIterationEvent event) {
        if (getClearEachIteration()) {
            log.debug("Initialise cookies from pre-defined list");
            // No need to call clear
            setProperty((CollectionProperty)initialCookies.clone());
        }
    }

    public static String[] getCookieSpecs(){
        return CookiePolicy.getRegisteredCookieSpecs(); // Commons HttpClient
        //return new DefaultHttpClient().getCookieSpecs().getSpecNames().toArray(new String[]{}); // Apache HttpClient
    }

}