// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This class provides an interface to the netscape cookies file to
 * pass cookies along with a request.
 *
 * @author  <a href="mailto:sdowd@arcmail.com">Sean Dowd</a>
 * @version $Revision$ $Date$
 */
public class CookieManager
    extends ConfigTestElement
    implements TestListener, Serializable
{
    transient private static Logger log = LoggingManager.getLoggerForClass();

    public static final String CLEAR = "CookieManager.clearEachIteration";
    public static final String COOKIES = "CookieManager.cookies";

    // SimpleDateFormat isn't thread-safe
    // TestElements are cloned for each thread, so
    // we use an instance variable.
    private SimpleDateFormat dateFormat =
        new SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss zzz",Locale.US);

    public CookieManager()
    {
		// The cookie specification requires that the timezone be GMT.
		// See http://developer.netscape.com/docs/manuals/communicator/jsguide4/cookies.htm
		// See http://www.cookiecentral.com/faq/
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        setProperty(new CollectionProperty(COOKIES, new ArrayList()));
        setProperty(new BooleanProperty(CLEAR, false));
    }

    public CollectionProperty getCookies()
    {
        return (CollectionProperty) getProperty(COOKIES);
    }

    public int getCookieCount()
    {
        return getCookies().size();
    }

    public boolean getClearEachIteration()
    {
        return getPropertyAsBoolean(CLEAR);
    }

    public void setClearEachIteration(boolean clear)
    {
        setProperty(new BooleanProperty(CLEAR, clear));
    }

    // Incorrect method. Always returns String. I changed CookiePanel code to
    // perform this lookup.
    //public Class getColumnClass(int column)
    //{
    //  return columnNames[column].getClass();
    //}

    public Cookie getCookie(int row)
    {
        return (Cookie) getCookies().get(row);
    }

    /**
     * Save the cookie data to a file.
     */
    public void save(String authFile) throws IOException
    {
        File file = new File(authFile);
        if (!file.isAbsolute())
            file =
                new File(
                    System.getProperty("user.dir") + File.separator + authFile);
        PrintWriter writer = new PrintWriter(new FileWriter(file));
        writer.println("# JMeter generated Cookie file");
        PropertyIterator cookies = getCookies().iterator();
        while (cookies.hasNext())
        {
            Cookie cook = (Cookie) cookies.next().getObjectValue();
            writer.println(cook.toString());
        }
        writer.flush();
        writer.close();
    }

    /**
     * Add cookie data from a file.
     */
    public void addFile(String cookieFile) throws IOException
    {
        File file = new File(cookieFile);
        if (!file.isAbsolute())
            file =
                new File(
                    System.getProperty("user.dir")
                        + File.separator
                        + cookieFile);
        BufferedReader reader = null;
        if (file.canRead())
        {
            reader = new BufferedReader(new FileReader(file));
        }
        else
        {
            throw new IOException("The file you specified cannot be read.");
        }

        String line;
        while ((line = reader.readLine()) != null)
        {
            try
            {
                if (line.startsWith("#") || line.trim().length() == 0)
                    continue;
                String[] st = split(line, "\t", " ");
                int domain = 0;
                int path = 2;
                if (st[path].equals(" "))
                    st[path] = "/";
                boolean secure = Boolean.valueOf(st[3]).booleanValue();
                long expires = new Long(st[4]).longValue();
                int name = 5;
                int value = 6;
                Cookie cookie =
                    new Cookie(
                        st[name],
                        st[value],
                        st[domain],
                        st[path],
                        secure,
                        expires);
                getCookies().addItem(cookie);
            }
            catch (Exception e)
            {
            	reader.close();
                throw new IOException(
                    "Error parsing cookie line\n\t'" + line + "'\n\t" + e);
            }
        }
        reader.close();
    }

    public void recoverRunningVersion()
    {
        //do nothing, the cookie manager has to accept changes.
    }

    public void setRunningVersion(boolean running)
    {
        //do nothing, the cookie manager has to accept changes.
    }

    /**
     * Add a cookie.
     */
    public void add(Cookie c)
    {
    	JMeterContext context = getThreadContext();
        getCookies().addItem(c);
        if(context.isSamplingStarted())
        {
            context.getVariables().put(c.getName(),c.getValue());
        }
    }

    /**
     * Add an empty cookie.
     */
    public void add()
    {
        getCookies().addItem(new Cookie());
    }
    
    /**
     * Remove all the cookies.
     */
    public void clear()
    {
        /*      boolean clear = getClearEachIteration();
                super.clear();
                setClearEachIteration(clear);*/
        setProperty(new CollectionProperty(COOKIES, new ArrayList()));
    }

    /**
     * Remove a cookie.
     */
    public void remove(int index)
    {
        getCookies().remove(index);
    }

    /**
     * Return the number of cookies.
     */
    public int size()
    {
        return getCookies().size();
    }

    /**
     * Return the cookie at index i.
     */
    public Cookie get(int i)
    {
        return (Cookie) getCookies().get(i);
    }

    public String convertLongToDateFormatStr(long dateLong)
    {
        return dateFormat.format(new Date(dateLong));
    }

    /**
     * Find cookies applicable to the given URL and build the Cookie header from
     * them.
     * 
     * @param url URL of the request to which the returned header will be added.
     * @return the value string for the cookie header (goes after "Cookie: "). 
     */
    public String getCookieHeaderForURL(URL url)
    {
        if (!url.getProtocol().toUpperCase().trim().equals("HTTP")
            && !url.getProtocol().toUpperCase().trim().equals("HTTPS"))
            return null;

        StringBuffer header = new StringBuffer();
        for (PropertyIterator enum = getCookies().iterator(); enum.hasNext();)
        {
            Cookie cookie = (Cookie) enum.next().getObjectValue();
            // Add a leading dot to the host name so that host X matches
            // domain .X. This is a breach of the standard, but it's how
            // browsers behave:
            String host= "."+url.getHost();
            if (host.endsWith(cookie.getDomain())
                && url.getFile().startsWith(cookie.getPath())
                && ((cookie.getExpires()==0) // treat as never expiring (bug 27713)
				    ||
                	(System.currentTimeMillis() / 1000) <= cookie.getExpires())				)
            {
                if (header.length() > 0)
                {
                    header.append("; ");
                }
                header.append(cookie.getName()).append("=").append(
                    cookie.getValue());
            }
        }

        if (header.length() != 0)
        {
            return header.toString();
        }
        else
        {
            return null;
        }
    }

    /**
     * Parse the set-cookie header value and store the cookies for later
     * retrieval.
     *
     * @param cookieHeader found after the "Set-Cookie: " in the response header
     * @param url URL used in the request for the above-mentioned response.
     */
    public void addCookieFromHeader(String cookieHeader, URL url)
    {
        StringTokenizer st = new StringTokenizer(cookieHeader, ";");
        String nvp;

        // first n=v is name=value
        nvp = st.nextToken();
        int index = nvp.indexOf("=");
        String name = nvp.substring(0, index);
        String value = nvp.substring(index + 1);
        String domain = "."+url.getHost(); // this is the default
                // the leading dot breaks the standard, but helps in
                // reproducing actual browser behaviour.
        String path = "/"; // this is the default

        Cookie newCookie =
            new Cookie(
                name,
                value,
                domain,
                path,
                false,
                System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        // check the rest of the headers
        while (st.hasMoreTokens())
        {
            nvp = st.nextToken();
            nvp = nvp.trim();
            index = nvp.indexOf("=");
            if (index == -1)
            {
                index = nvp.length();
            }
            String key = nvp.substring(0, index);
            if (key.equalsIgnoreCase("expires"))
            {
                try
                {
                    String expires = nvp.substring(index + 1);
                    Date date = dateFormat.parse(expires);
                    //Always set expiry date - see Bugzilla id 29493
                    newCookie.setExpires(date.getTime());
                }
                catch (ParseException pe)
                {
                    // This means the cookie did not come in the proper format.
                    // Log an error and don't set an expiration time:
                    log.error("Couldn't parse Cookie expiration time.", pe);
                }
                catch (Exception e)
                {
                    // DateFormat.parse() has been known to throw various
                    // unchecked exceptions in the past, and does still do that
                    // occasionally at the time of this writing (1.4.2 JDKs).
                    // E.g. see http://developer.java.sun.com/developer/bugParade/bugs/4699765.html
                    //
                    // As a workaround for such issues we will catch all
                    // exceptions and react just as we did for ParseException
                    // above:
                    log.error(
                        "Couln't parse Cookie expiration time: likely JDK bug.",
                        e);
                }
            }
            else if (key.equalsIgnoreCase("domain"))
            {
                //trim() is a workaround for bug in Oracle8iAS wherere
            	//cookies would have leading spaces in the domain portion
            	domain= nvp.substring(index + 1).trim();
                
                // The standard dictates domains must have a leading dot,
                // but the new standard (Cookie2) tells us to add it if it's not
                // there:
                if (!domain.startsWith("."))
                {
                    domain= "."+domain;
                }
                
                newCookie.setDomain(domain);
            }
            else if (key.equalsIgnoreCase("path"))
            {
                newCookie.setPath(nvp.substring(index + 1));
            }
            else if (key.equalsIgnoreCase("secure"))
            {
                newCookie.setSecure(true);
            }
        }

        Vector removeIndices = new Vector();
        for (int i = getCookies().size() - 1; i >= 0; i--)
        {
            Cookie cookie = (Cookie) getCookies().get(i).getObjectValue();
            if (cookie == null)
                continue;
            if (cookie.getPath().equals(newCookie.getPath())
                && cookie.getDomain().equals(newCookie.getDomain())
                && cookie.getName().equals(newCookie.getName()))
            {
                removeIndices.addElement(new Integer(i));
            }
        }

        for (Enumeration e = removeIndices.elements(); e.hasMoreElements();)
        {
            index = ((Integer) e.nextElement()).intValue();
            remove(index);
        }

        if (newCookie.getExpires() >= System.currentTimeMillis())
        {
            add(newCookie);
        }
    }

    public void removeCookieNamed(String name)
    {
        PropertyIterator iter = getCookies().iterator();
        while (iter.hasNext())
        {
            Cookie cookie = (Cookie) iter.next().getObjectValue();
            if (cookie.getName().equals(name))
            {
                iter.remove();
            }
        }
    }

    /**
     * Takes a String and a tokenizer character, and returns a new array of
     * strings of the string split by the tokenizer character.
     * 
     * @param splittee  string to be split
     * @param splitChar character to split the string on
     * @param def       default value to place between two split chars that have
     *                  nothing between them
     * @return array of all the tokens.
     */
    public String[] split(String splittee, String splitChar, String def)
    {
        if (splittee == null || splitChar == null)
            return new String[0];
        StringTokenizer tokens;
        String temp;
        int spot;
        while ((spot = splittee.indexOf(splitChar + splitChar)) != -1)
            splittee =
                splittee.substring(0, spot + splitChar.length())
                    + def
                    + splittee.substring(
                        spot + 1 * splitChar.length(),
                        splittee.length());
        Vector returns = new Vector();
        tokens = new StringTokenizer(splittee, splitChar);
        while (tokens.hasMoreTokens())
        {
            temp = (String) tokens.nextToken();
            returns.addElement(temp);
        }
        String[] values = new String[returns.size()];
        returns.copyInto(values);
        return values;
    }

    public String getClassLabel()
    {
        return JMeterUtils.getResString("cookie_manager_title");
    }

    public void testStarted()
    {
    }

    public void testEnded()
    {
    }

    public void testStarted(String host)
    {
    }

    public void testEnded(String host)
    {
    }

    public void testIterationStart(LoopIterationEvent event)
    {
        if (getClearEachIteration())
            clear();
    }

    public static class Test extends TestCase
    {
    	CookieManager man = null;
    	
        public Test(String name)
        {
            super(name);
        }
        private JMeterContext jmctx = null;
        
        public void setUp()
    	{
        	jmctx = JMeterContextService.getContext();
            man = new CookieManager();
            man.setThreadContext(jmctx);
        }


        public void testRemoveCookie() throws Exception
        {
            man.setThreadContext(jmctx);
            man.add(new Cookie("id", "me", "127.0.0.1", "/", false, 0));
            man.removeCookieNamed("id");
            assertEquals(0, man.getCookieCount());
        }

        public void testSendCookie() throws Exception
        {
            man.add(
                new Cookie(
                    "id",
                    "value",
                    "jakarta.apache.org",
                    "/",
                    false,
                    9999999999L));
            HTTPSampler sampler = new HTTPSampler();
            sampler.setDomain("jakarta.apache.org");
            sampler.setPath("/index.html");
            sampler.setMethod(HTTPSampler.GET);
            assertNotNull(man.getCookieHeaderForURL(sampler.getUrl()));
        }

        public void testSendCookie2() throws Exception
        {
            man.add(
                new Cookie(
                    "id",
                    "value",
                    ".apache.org",
                    "/",
                    false,
                    9999999999L));
            HTTPSampler sampler = new HTTPSampler();
            sampler.setDomain("jakarta.apache.org");
            sampler.setPath("/index.html");
            sampler.setMethod(HTTPSampler.GET);
            assertNotNull(man.getCookieHeaderForURL(sampler.getUrl()));
        }
        
        /**
         * Test that the cookie domain field is actually handled as
         * browsers do (i.e.: host X matches domain .X):
         */
        public void testDomainHandling() throws Exception
        {
            URL url= new URL("http://jakarta.apache.org/");
            man.addCookieFromHeader("test=1;domain=.jakarta.apache.org", url);
            assertNotNull(man.getCookieHeaderForURL(url));
        }
        
        /**
         * Test that we won't be tricked by similar host names (this was a past
         * bug, although it never got reported in the bug database):
         */
        public void testSimilarHostNames() throws Exception
        {
            URL url= new URL("http://ache.org/");
            man.addCookieFromHeader("test=1", url);
            url= new URL("http://jakarta.apache.org/");
            assertNull(man.getCookieHeaderForURL(url));
        }
    }
}