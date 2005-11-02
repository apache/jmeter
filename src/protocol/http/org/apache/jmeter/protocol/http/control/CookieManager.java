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

package org.apache.jmeter.protocol.http.control;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPNullSampler;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * This class provides an interface to the netscape cookies file to pass cookies
 * along with a request.
 * 
 * author <a href="mailto:sdowd@arcmail.com">Sean Dowd</a>
 * @version $Revision$ $Date$
 */
public class CookieManager extends ConfigTestElement implements TestListener, Serializable {
	transient private static Logger log = LoggingManager.getLoggerForClass();

	public static final String CLEAR = "CookieManager.clearEachIteration";// $NON-NLS-1$

	public static final String COOKIES = "CookieManager.cookies";// $NON-NLS-1$

	// SimpleDateFormat isn't thread-safe
	// TestElements are cloned for each thread, so we use an instance variable.
	private SimpleDateFormat dateFormat 
    = new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US);// $NON-NLS-1$

	// See bug 33796
	private static final boolean DELETE_NULL_COOKIES 
        = JMeterUtils.getPropDefault("CookieManager.delete_null_cookies", true);// $NON-NLS-1$

	public CookieManager() {
		// The cookie specification requires that the timezone be GMT.
		// See:
		// http://wp.netscape.com/newsref/std/cookie_spec.html (Netscape)
        // http://www.w3.org/Protocols/rfc2109/rfc2109.txt
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));// $NON-NLS-1$

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

	// Incorrect method. Always returns String. I changed CookiePanel code to
	// perform this lookup.
	// public Class getColumnClass(int column)
	// {
	// return columnNames[column].getClass();
	// }

	public Cookie getCookie(int row) {
		return (Cookie) getCookies().get(row);
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
		if (DELETE_NULL_COOKIES && (null == cv || "".equals(cv))) {
            if (log.isDebugEnabled()) {
                log.debug("Removing cookie with null value " + c.toString());
            }
			removeCookieNamed(cn);
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
		/*
		 * boolean clear = getClearEachIteration(); super.clear();
		 * setClearEachIteration(clear);
		 */
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

	public String convertLongToDateFormatStr(long dateLong) {
		return dateFormat.format(new Date(dateLong));
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
		boolean debugEnabled = log.isDebugEnabled();

		if (!url.getProtocol().toUpperCase().trim().equals("HTTP")// $NON-NLS-1$
				&& !url.getProtocol().toUpperCase().trim().equals("HTTPS"))// $NON-NLS-1$
			return null;

		StringBuffer header = new StringBuffer();
		String host = "." + url.getHost();
		if (debugEnabled) {
			log.debug("Get cookie for URL= " + url);
			log.debug("URL Host=" + host);
			log.debug("Time now (secs)" + (System.currentTimeMillis() / 1000));
        }
		for (PropertyIterator iter = getCookies().iterator(); iter.hasNext();) {
			Cookie cookie = (Cookie) iter.next().getObjectValue();
			// Add a leading dot to the host name so that host X matches
			// domain .X. This is a breach of the standard, but it's how
			// browsers behave:
			if (debugEnabled) {
				log.debug("Possible Cookie. Name=" + cookie.getName() 
                        + " domain=" + cookie.getDomain() 
                        + " path=" + cookie.getPath() 
                        + " expires=" + cookie.getExpires());
			}
			if (host.endsWith(cookie.getDomain()) && url.getFile().startsWith(cookie.getPath())
					&& ((cookie.getExpires() == 0) // treat as never expiring
													// (bug 27713)
					|| (System.currentTimeMillis() / 1000) <= cookie.getExpires())) {
				if (header.length() > 0) {
					header.append("; ");
				}
				if (debugEnabled) {
					log.debug("Matched cookie:"
							+ " name=" + cookie.getName()
							+ " value=" + cookie.getValue());
                }
				header.append(cookie.getName()).append("=").append(cookie.getValue());
			}
		}

		if (header.length() != 0) {
			if (debugEnabled){
				log.debug(header.toString());
            }
			return header.toString();
		} else {
			return null;
		}
	}

	/**
	 * Parse the set-cookie header value and store the cookies for later
	 * retrieval.
	 * 
	 * @param cookieHeader
	 *            found after the "Set-Cookie: " in the response header
	 * @param url
	 *            URL used in the request for the above-mentioned response.
	 */
	public void addCookieFromHeader(String cookieHeader, URL url) {
        boolean debugEnabled = log.isDebugEnabled(); 
		if (debugEnabled) {
			log.debug("Received Cookie: " + cookieHeader + " From:" + url.toExternalForm());
		}
		StringTokenizer st = new StringTokenizer(cookieHeader, ";");// $NON-NLS-1$
		String nvp;

		// first n=v is name=value
		nvp = st.nextToken();
		int index = nvp.indexOf("=");// $NON-NLS-1$
		String name = nvp.substring(0, index);
		String value = nvp.substring(index + 1);
		String domain = "." + url.getHost(); // this is the default
		// the leading dot breaks the standard, but helps in
		// reproducing actual browser behaviour.
		// The default is the path of the request URL (upto and including the last slash)
		String path = url.getPath();
		if (path.length() == 0) {
			path = "/"; // $NON-NLS-1$ default if no path specified
		} else {
			int lastSlash = path.lastIndexOf("/");// $NON-NLS-1$
			if (lastSlash > 0) {// Must be after initial character
                // Upto, but not including, trailing slash for Set-Cookie:
                // (Set-Cookie2: would need the trailing slash as well
				path=path.substring(0,lastSlash);
			}
		}

		Cookie newCookie = new Cookie(name, value, domain, path, false
                                    , 0); // No expiry means session cookie

		// check the rest of the headers
		while (st.hasMoreTokens()) {
			nvp = st.nextToken();
			nvp = nvp.trim();
			index = nvp.indexOf("=");// $NON-NLS-1$
			if (index == -1) {
				index = nvp.length();
			}
			String key = nvp.substring(0, index);
			if (key.equalsIgnoreCase("expires")) {// $NON-NLS-1$
				try {
					String expires = nvp.substring(index + 1);
					Date date = dateFormat.parse(expires);
					// Always set expiry date - see Bugzilla id 29493
					newCookie.setExpires(date.getTime() / 1000); // Set time
																	// in
																	// seconds
				} catch (ParseException pe) {
					// This means the cookie did not come in the proper format.
					// Log an error and don't set an expiration time:
					log.error("Couldn't parse Cookie expiration time: "
							+cookieHeader, pe);
				} catch (Exception e) {
					// DateFormat.parse() has been known to throw various
					// unchecked exceptions in the past, and does still do that
					// occasionally at the time of this writing (1.4.2 JDKs).
					// E.g. see
					// http://developer.java.sun.com/developer/bugParade/bugs/4699765.html
					//
					// As a workaround for such issues we will catch all
					// exceptions and react just as we did for ParseException
					// above:
					log.error("Couln't parse Cookie expiration time: likely JDK bug: "
							+cookieHeader, e);
				}
			} else if (key.equalsIgnoreCase("domain")) {// $NON-NLS-1$
				// trim() is a workaround for bug in Oracle8iAS wherere
				// cookies would have leading spaces in the domain portion
				domain = nvp.substring(index + 1).trim();

				// The standard dictates domains must have a leading dot,
				// but the new standard (Cookie2) tells us to add it if it's not
				// there:
				if (!domain.startsWith(".")) {// $NON-NLS-1$
					domain = "." + domain;// $NON-NLS-1$
				}

				newCookie.setDomain(domain);
			} else if (key.equalsIgnoreCase("path")) {// $NON-NLS-1$
				newCookie.setPath(nvp.substring(index + 1).trim());
			} else if (key.equalsIgnoreCase("secure")) {// $NON-NLS-1$
				newCookie.setSecure(true);
			}
		}

        // Scan for any matching cookies
        Vector removeIndices = new Vector();
		for (int i = getCookies().size() - 1; i >= 0; i--) {
			Cookie cookie = (Cookie) getCookies().get(i).getObjectValue();
			if (cookie == null)
				continue;
			if (cookie.getPath().equals(newCookie.getPath()) 
                    && cookie.getDomain().equals(newCookie.getDomain())
					&& cookie.getName().equals(newCookie.getName())) {
				if (debugEnabled) {
					log.debug("New Cookie = " + newCookie.toString()
                              + " removing matching Cookie " + cookie.toString());
				}
				removeIndices.addElement(new Integer(i));
			}
		}

        // Now remove the matching cookies
		for (Enumeration e = removeIndices.elements(); e.hasMoreElements();) {
			index = ((Integer) e.nextElement()).intValue();
			remove(index);
		}

		long exp = newCookie.getExpires();
		// Store session cookies as well as unexpired ones
		if (exp == 0 || exp >= System.currentTimeMillis() / 1000) {
			add(newCookie); // Has its own debug log
		} else {
            if (debugEnabled){
                log.debug("Dropping expired Cookie: "+newCookie.toString());
            }      
        }
	}

	public void removeCookieNamed(String name) {
		if (log.isDebugEnabled())
			log.debug("Remove cookie named " + name);
		PropertyIterator iter = getCookies().iterator();
		while (iter.hasNext()) {
			Cookie cookie = (Cookie) iter.next().getObjectValue();
			if (cookie.getName().equals(name)) {
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
    
    ///////////////////////////////////////////// TEST CASES ////////////////////////////////////

	public static class Test extends TestCase {
		CookieManager man = null;

		public Test(String name) {
			super(name);
		}

		private JMeterContext jmctx = null;

		public void setUp() {
			jmctx = JMeterContextService.getContext();
			man = new CookieManager();
			man.setThreadContext(jmctx);
		}

		public void testRemoveCookie() throws Exception {
			man.setThreadContext(jmctx);
			man.add(new Cookie("id", "me", "127.0.0.1", "/", false, 0));
			man.removeCookieNamed("id");
			assertEquals(0, man.getCookieCount());
		}

		public void testSendCookie() throws Exception {
			man.add(new Cookie("id", "value", "jakarta.apache.org", "/", false, 9999999999L));
			HTTPSamplerBase sampler = new HTTPNullSampler();
			sampler.setDomain("jakarta.apache.org");
			sampler.setPath("/index.html");
			sampler.setMethod(HTTPSamplerBase.GET);
			assertNotNull(man.getCookieHeaderForURL(sampler.getUrl()));
		}

		public void testSendCookie2() throws Exception {
			man.add(new Cookie("id", "value", ".apache.org", "/", false, 9999999999L));
			HTTPSamplerBase sampler = new HTTPNullSampler();
			sampler.setDomain("jakarta.apache.org");
			sampler.setPath("/index.html");
			sampler.setMethod(HTTPSamplerBase.GET);
			assertNotNull(man.getCookieHeaderForURL(sampler.getUrl()));
		}

		/**
		 * Test that the cookie domain field is actually handled as browsers do
		 * (i.e.: host X matches domain .X):
		 */
		public void testDomainHandling() throws Exception {
			URL url = new URL("http://jakarta.apache.org/");
			man.addCookieFromHeader("test=1;domain=.jakarta.apache.org", url);
			assertNotNull(man.getCookieHeaderForURL(url));
		}

		/**
		 * Test that we won't be tricked by similar host names (this was a past
		 * bug, although it never got reported in the bug database):
		 */
		public void testSimilarHostNames() throws Exception {
			URL url = new URL("http://ache.org/");
			man.addCookieFromHeader("test=1", url);
			url = new URL("http://jakarta.apache.org/");
			assertNull(man.getCookieHeaderForURL(url));
		}

		// Test session cookie is returned
		public void testSessionCookie() throws Exception {
			URL url = new URL("http://a.b.c/");
			man.addCookieFromHeader("test=1", url);
			String s = man.getCookieHeaderForURL(url);
			assertNotNull(s);
			assertEquals("test=1", s);
		}

		// Test Old cookie is not returned
		public void testOldCookie() throws Exception {
			URL url = new URL("http://a.b.c/");
			man.addCookieFromHeader("test=1; expires=Mon, 01-Jan-1990 00:00:00 GMT", url);
			String s = man.getCookieHeaderForURL(url);
			assertNull(s);
		}

		// Test New cookie is returned
		public void testNewCookie() throws Exception {
			URL url = new URL("http://a.b.c/");
			man.addCookieFromHeader("test=1; expires=Mon, 01-Jan-2990 00:00:00 GMT", url);
			String s = man.getCookieHeaderForURL(url);
			assertNotNull(s);
			assertEquals("test=1", s);
		}
	}
}