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

import org.apache.http.client.config.CookieSpecs;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.reflect.ClassTools;
import org.apache.jorphan.util.JMeterException;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides an interface to the netscape cookies file to pass cookies
 * along with a request.
 */
public class CookieManager extends ConfigTestElement implements TestStateListener, TestIterationListener, Serializable {
    private static final long serialVersionUID = 234L;

    private static final Logger log = LoggerFactory.getLogger(CookieManager.class);

    //++ JMX tag values
    private static final String CLEAR = "CookieManager.clearEachIteration";// $NON-NLS-1$
    private static final String COOKIES = "CookieManager.cookies";// $NON-NLS-1$
    private static final String POLICY = "CookieManager.policy"; //$NON-NLS-1$
    private static final String IMPLEMENTATION = "CookieManager.implementation"; //$NON-NLS-1$
    //-- JMX tag values

    private static final String TAB = "\t"; //$NON-NLS-1$

    // See bug 33796
    private static final boolean DELETE_NULL_COOKIES =
        JMeterUtils.getPropDefault("CookieManager.delete_null_cookies", true);// $NON-NLS-1$

    // See bug 28715
    // Package protected for tests
    static final boolean ALLOW_VARIABLE_COOKIES
        = JMeterUtils.getPropDefault("CookieManager.allow_variable_cookies", true);// $NON-NLS-1$

    private static final String COOKIE_NAME_PREFIX =
        JMeterUtils.getPropDefault("CookieManager.name.prefix", "COOKIE_").trim();// $NON-NLS-1$ $NON-NLS-2$

    private static final boolean SAVE_COOKIES =
        JMeterUtils.getPropDefault("CookieManager.save.cookies", false);// $NON-NLS-1$

    private static final boolean CHECK_COOKIES =
        JMeterUtils.getPropDefault("CookieManager.check.cookies", true);// $NON-NLS-1$

    static {
        log.info("Settings: Delete null: {} Check: {} Allow variable: {} Save: {} Prefix: {}", 
                DELETE_NULL_COOKIES, CHECK_COOKIES, ALLOW_VARIABLE_COOKIES, 
                SAVE_COOKIES, COOKIE_NAME_PREFIX);
    }

    private transient CookieHandler cookieHandler;
    private transient CollectionProperty initialCookies;

    /**
     * Defines the policy that is assumed when the JMX file does not contain an entry for it
     * MUST NOT BE CHANGED otherwise JMX files will not be correctly interpreted
     * <p>
     * The default policy for new CookieManager elements is defined by 
     * {@link org.apache.jmeter.protocol.http.gui.CookiePanel#DEFAULT_POLICY CookiePanel#DEFAULT_POLICY}
     *
     */
    private static final String DEFAULT_POLICY = CookieSpecs.STANDARD;
    
    /**
     * Defines the implementation that is assumed when the JMX file does not contain an entry for it
     * MUST NOT BE CHANGED otherwise JMX files will not be correctly interpreted
     * <p>
     * The default implementation for new CookieManager elements is defined by 
     * {@link org.apache.jmeter.protocol.http.gui.CookiePanel#DEFAULT_IMPLEMENTATION CookiePanel#DEFAULT_IMPLEMENTATION}
     *
     */
    private static final String DEFAULT_IMPLEMENTATION = HC4CookieHandler.class.getName();

    public CookieManager() {
        clearCookies(); // Ensure that there is always a collection available
    }

    // ensure that the initial cookies are copied to the per-thread instances
    /** {@inheritDoc} */
    @Override
    public Object clone(){
        CookieManager clone = (CookieManager) super.clone();
        clone.initialCookies = initialCookies;
        clone.cookieHandler = cookieHandler;
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

    public String getImplementation() {
        return getPropertyAsString(IMPLEMENTATION, DEFAULT_IMPLEMENTATION);
    }

    public void setImplementation(String implementation){
        setProperty(IMPLEMENTATION, implementation, DEFAULT_IMPLEMENTATION);
    }

    /**
     * Save the static cookie data to a file.
     * <p>
     * Cookies are only taken from the GUI - runtime cookies are not included.
     *
     * @param authFile
     *            name of the file to store the cookies into. If the name is
     *            relative, the system property <code>user.dir</code> will be
     *            prepended
     * @throws IOException
     *             when writing to that file fails
     */
    public void save(String authFile) throws IOException {
        File file = new File(authFile);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir") // $NON-NLS-1$
                    + File.separator + authFile);
        }
        try(PrintWriter writer = new PrintWriter(new FileWriter(file))) { // TODO Charset ?
            writer.println("# JMeter generated Cookie file");// $NON-NLS-1$
            long now = System.currentTimeMillis();
            for (JMeterProperty jMeterProperty : getCookies()) {
                Cookie cook = (Cookie) jMeterProperty.getObjectValue();
                final long expiresMillis = cook.getExpiresMillis();
                if (expiresMillis == 0 || expiresMillis > now) { // only save unexpired cookies
                    writer.println(cookieToString(cook));
                }
            }
            writer.flush();
        }
    }

    /**
     * Add cookie data from a file.
     *
     * @param cookieFile
     *            name of the file to read the cookies from. If the name is
     *            relative, the system property <code>user.dir</code> will be
     *            prepended
     * @throws IOException
     *             if reading the file fails
     */
    public void addFile(String cookieFile) throws IOException {
        File file = new File(cookieFile);
        if (!file.isAbsolute()) {
            file = new File(System.getProperty("user.dir") // $NON-NLS-1$
                    + File.separator + cookieFile);
        }
        BufferedReader reader = null;
        if (file.canRead()) {
            reader = new BufferedReader(new FileReader(file)); // TODO Charset ?
        } else {
            throw new IOException("The file you specified cannot be read.");
        }

        // N.B. this must agree with the save() and cookieToString() methods
        String line;
        try {
            final CollectionProperty cookies = getCookies();
            while ((line = reader.readLine()) != null) {
                try {
                    if (line.startsWith("#") || JOrphanUtils.isBlank(line)) {//$NON-NLS-1$
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
                    boolean secure = Boolean.parseBoolean(st[_secure]);
                    long expires = Long.parseLong(st[_expires]);
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
     *
     * @param c cookie to be added
     */
    public void add(Cookie c) {
        String cv = c.getValue();
        String cn = c.getName();
        removeMatchingCookies(c); // Can't have two matching cookies

        if (DELETE_NULL_COOKIES && (null == cv || cv.length()==0)) {
            if (log.isDebugEnabled()) {
                log.debug("Dropping cookie with null value {}", c.toString());
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Add cookie to store {}", c.toString());
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
        setProperty(new CollectionProperty(COOKIES, new ArrayList<>()));
    }

    /**
     * Remove a cookie.
     *
     * @param index index of the cookie to remove
     */
    public void remove(int index) {// TODO not used by GUI
        getCookies().remove(index);
    }

    /**
     * Return the cookie at index i.
     *
     * @param i index of the cookie to get
     * @return cookie at index <code>i</code>
     */
    public Cookie get(int i) {// Only used by GUI
        return (Cookie) getCookies().get(i).getObjectValue();
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
        return cookieHandler.getCookieHeaderForURL(getCookies(), url, ALLOW_VARIABLE_COOKIES);
    }


    public void addCookieFromHeader(String cookieHeader, URL url){
        cookieHandler.addCookieFromHeader(this, CHECK_COOKIES, cookieHeader, url);
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

    void removeMatchingCookies(Cookie newCookie){
        // Scan for any matching cookies
        PropertyIterator iter = getCookies().iterator();
        while (iter.hasNext()) {
            Cookie cookie = (Cookie) iter.next().getObjectValue();
            if (cookie == null) {// TODO is this possible?
                continue;
            }
            if (match(cookie,newCookie)) {
                if (log.isDebugEnabled()) {
                    log.debug("New Cookie = {} removing matching Cookie {}",
                            newCookie.toString(), cookie.toString());
                }
                iter.remove();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void testStarted() {
        initialCookies = getCookies();
        try {
            cookieHandler = (CookieHandler) ClassTools.construct(getImplementation(), getPolicy());
        } catch (JMeterException e) {
            log.error("Unable to load or invoke class: {}", getImplementation(), e);
        }
        if (log.isDebugEnabled()){
            log.debug("Policy: {} Clear: {}", getPolicy(), getClearEachIteration());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void testEnded() {
    }

    /** {@inheritDoc} */
    @Override
    public void testStarted(String host) {
        testStarted();
    }

    /** {@inheritDoc} */
    @Override
    public void testEnded(String host) {
    }

    /** {@inheritDoc} */
    @Override
    public void testIterationStart(LoopIterationEvent event) {
        if (getClearEachIteration()) {
            log.debug("Initialise cookies from pre-defined list");
            // No need to call clear
            setProperty(initialCookies.clone());
        }
    }

    /**
     * Package protected for tests
     * @return the cookieHandler
     */
    CookieHandler getCookieHandler() {
        return cookieHandler;
    }
}
