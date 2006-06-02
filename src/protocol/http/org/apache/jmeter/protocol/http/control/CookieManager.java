/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * author <a href="mailto:sdowd@arcmail.com">Sean Dowd</a>
 * @version $Revision$ $Date$
 */
public class CookieManager extends ConfigTestElement implements TestListener, Serializable {
	transient private static Logger log = LoggingManager.getLoggerForClass();

	public static final String CLEAR = "CookieManager.clearEachIteration";// $NON-NLS-1$

	public static final String COOKIES = "CookieManager.cookies";// $NON-NLS-1$

	// See bug 33796
	private static final boolean DELETE_NULL_COOKIES 
        = JMeterUtils.getPropDefault("CookieManager.delete_null_cookies", true);// $NON-NLS-1$

    // TODO implement other policies
    private transient CookieSpec cookieSpec = CookiePolicy.getCookieSpec(CookiePolicy.BROWSER_COMPATIBILITY);

	public CookieManager() {
		setProperty(new CollectionProperty(COOKIES, new ArrayList()));
		setProperty(new BooleanProperty(CLEAR, false));
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
		if (!file.isAbsolute())
			file = new File(System.getProperty("user.dir") // $NON-NLS-1$
                    + File.separator + authFile);
		PrintWriter writer = new PrintWriter(new FileWriter(file));
		writer.println("# JMeter generated Cookie file");// $NON-NLS-1$
		PropertyIterator cookies = getCookies().iterator();
		long now = System.currentTimeMillis() / 1000;
		while (cookies.hasNext()) {
			Cookie cook = (Cookie) cookies.next().getObjectValue();
			// Note: now is always > 0, so no need to check for that separately
			if (cook.getExpires() > now) { // only save unexpired cookies
				writer.println(cook.toString());
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
		if (!file.isAbsolute())
			file = new File(System.getProperty("user.dir") // $NON-NLS-1$
                    + File.separator + cookieFile);
		BufferedReader reader = null;
		if (file.canRead()) {
			reader = new BufferedReader(new FileReader(file));
		} else {
			throw new IOException("The file you specified cannot be read.");
		}

		String line;
		while ((line = reader.readLine()) != null) {
			try {
				if (line.startsWith("#") || line.trim().length() == 0)
					continue;
				String[] st = JOrphanUtils.split(line, "\t", " ");
				int domain = 0;
				int path = 2;
				if (st[path].equals(" "))
					st[path] = "/";
				boolean secure = Boolean.valueOf(st[3]).booleanValue();
				long expires = new Long(st[4]).longValue();
				int name = 5;
				int value = 6;
				Cookie cookie = new Cookie(st[name], st[value], st[domain], st[path], secure, expires);
				getCookies().addItem(cookie);
			} catch (Exception e) {
				reader.close();
				throw new IOException("Error parsing cookie line\n\t'" + line + "'\n\t" + e);
			}
		}
		reader.close();
	}

	public void recoverRunningVersion() {
		// do nothing, the cookie manager has to accept changes.
	}

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
            // Store cookie as a thread variable. 
            // TODO - should we add a prefix to these variables?
            // TODO - should storing cookie values be optional?
            JMeterContext context = getThreadContext();
			if (context.isSamplingStarted()) {
				context.getVariables().put(cn, cv);
			}
		}
	}

	/**
	 * Remove all the cookies.
	 */
	public void clear() {
		log.debug("Clear all cookies from store");
		setProperty(new CollectionProperty(COOKIES, new ArrayList()));
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
        long exp = jmc.getExpires() * 1000;
        return new org.apache.commons.httpclient.Cookie(
                jmc.getDomain(),
                jmc.getName(),
                jmc.getValue(),
                jmc.getPath(),
                exp > 0 ? new Date(exp) : null, // use null for no expiry
                jmc.getSecure()
               );
    }
    
    /**
     * Get array of valid HttpClient cookies for the URL
     * 
     * @param URL
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
            cookies[i++] = makeCookie(jmcookie);
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
        log.debug("*** Cookies for "+url.toExternalForm()+" = "+count);
        if (count <=0){
            return null;
        }
        StringBuffer sb = new StringBuffer(count*20);
        for(int i=0;i<count;i++){
            if (i>0){
                sb.append("; "); //$NON-NLS-1$ separator
            }
            sb.append(c[i].getName());
            sb.append("="); //$NON-NLS-1$
            sb.append(c[i].getValue());
        }
        return sb.toString();
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
        if (cookies == null) return;
        for(int i=0;i<cookies.length;i++){
            Date expiryDate = cookies[i].getExpiryDate();
            long exp = 0;
            if (expiryDate!= null) {
                exp=expiryDate.getTime() / 1000;
            }
            Cookie newCookie = new Cookie(
                    cookies[i].getName(),
                    cookies[i].getValue(),
                    cookies[i].getDomain(),
                    cookies[i].getPath(),
                    cookies[i].getSecure(), 
                    exp);

            // Store session cookies as well as unexpired ones
            if (exp == 0 || exp >= (System.currentTimeMillis() / 1000)) {
                add(newCookie); // Has its own debug log; removes matching cookies
            } else {
                removeMatchingCookies(newCookie);
                if (debugEnabled){
                    log.debug("Dropping expired Cookie: "+newCookie.toString());
                }      
            }
        }

    }
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
            if (cookie == null)
                continue;
            if (match(cookie,newCookie)) { 
                if (log.isDebugEnabled()) {
                    log.debug("New Cookie = " + newCookie.toString()
                              + " removing matching Cookie " + cookie.toString());
                }
                iter.remove();
            }
        }       
    }
    
	public String getClassLabel() {
		return JMeterUtils.getResString("cookie_manager_title");// $NON-NLS-1$
	}

	public void testStarted() {
	}

	public void testEnded() {
	}

	public void testStarted(String host) {
	}

	public void testEnded(String host) {
	}

	public void testIterationStart(LoopIterationEvent event) {
		if (getClearEachIteration())
			clear();
	}
}