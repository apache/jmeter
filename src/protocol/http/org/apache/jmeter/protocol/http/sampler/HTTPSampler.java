/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.jmeter.protocol.http.sampler;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
//import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.SSLManager;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.oro.text.regex.StringSubstitution;
import org.apache.oro.text.regex.Substitution;
import org.apache.oro.text.regex.Util;

/**
 * A sampler which understands all the parts necessary to read statistics about
 * HTTP requests, including cookies and authentication.
 *
 * @author    Michael Stover
 * @version   $Revision$
 */
public class HTTPSampler extends AbstractSampler
{
    public final static String HEADERS = "headers";
    public final static String HEADER = "header";
    public final static String ARGUMENTS = "HTTPsampler.Arguments";
    public final static String AUTH_MANAGER = "HTTPSampler.auth_manager";
    public final static String COOKIE_MANAGER = "HTTPSampler.cookie_manager";
    public final static String HEADER_MANAGER = "HTTPSampler.header_manager";
    public final static String MIMETYPE = "HTTPSampler.mimetype";
    public final static String DOMAIN = "HTTPSampler.domain";
    public final static String PORT = "HTTPSampler.port";
    public final static String METHOD = "HTTPSampler.method";
    public final static String PATH = "HTTPSampler.path";
    public final static String FOLLOW_REDIRECTS =
        "HTTPSampler.follow_redirects";
    public final static String PROTOCOL = "HTTPSampler.protocol";
    public final static String DEFAULT_PROTOCOL = "http";
    public final static String URL = "HTTPSampler.URL";
    public final static String POST = "POST";
    public final static String GET = "GET";
    public final static String USE_KEEPALIVE = "HTTPSampler.use_keepalive";
    public final static String FILE_NAME = "HTTPSampler.FILE_NAME";
    public final static String FILE_FIELD = "HTTPSampler.FILE_FIELD";
    public final static String FILE_DATA = "HTTPSampler.FILE_DATA";
    public final static String FILE_MIMETYPE = "HTTPSampler.FILE_MIMETYPE";
    public final static String CONTENT_TYPE = "HTTPSampler.CONTENT_TYPE";
    public final static String NORMAL_FORM = "normal_form";
    public final static String MULTIPART_FORM = "multipart_form";
    public final static String ENCODED_PATH = "HTTPSampler.encoded_path";
    public final static String IMAGE_PARSER = "HTTPSampler.image_parser";

    /** A number to indicate that the port has not been set.  **/
    public static final int UNSPECIFIED_PORT = 0;
    private static final int MAX_REDIRECTS = 10;
    protected static String encoding = "iso-8859-1";
    private static final PostWriter postWriter = new PostWriter();
    transient protected HttpURLConnection conn;
	private HTTPSamplerFull imageSampler;

    static {
        System.setProperty(
            "java.protocol.handler.pkgs",
            JMeterUtils.getPropDefault(
                "ssl.pkgs",
                "com.sun.net.ssl.internal.www.protocol"));
        System.setProperty("javax.net.ssl.debug", "all");
    }

    private static PatternCacheLRU patternCache =
        new PatternCacheLRU(1000, new Perl5Compiler());

    private static ThreadLocal localMatcher = new ThreadLocal()
    {
        protected synchronized Object initialValue()
        {
            return new Perl5Matcher();
        }
    };

    private static Substitution spaceSub = new StringSubstitution("%20");

    private int connectionTries = 0;
    
    /* Delegate redirects to the URLConnection implementation - this can be useful
     * with alternate URLConnection implementations.
     * 
     * Defaults to false, to maintain backward compatibility. 
     */
    private static boolean delegateRedirects =
    	JMeterUtils.getJMeterProperties().
    	getProperty("HTTPSampler.delegateRedirects","false").equalsIgnoreCase("true") ;

    
    public void setFileField(String value)
    {
        setProperty(FILE_FIELD, value);
    }
    public String getFileField()
    {
        return getPropertyAsString(FILE_FIELD);
    }
    public void setFilename(String value)
    {
        setProperty(FILE_NAME, value);
    }
    public String getFilename()
    {
        return getPropertyAsString(FILE_NAME);
    }
    public void setProtocol(String value)
    {
        setProperty(PROTOCOL, value);
    }
    public String getProtocol()
    {
        String protocol = getPropertyAsString(PROTOCOL);
        if (protocol == null || protocol.equals(""))
        {
            return DEFAULT_PROTOCOL;
        }
        else
        {
            return protocol;
        }
    }

    /**
     *  Sets the Path attribute of the UrlConfig object
     *
     *@param  path  The new Path value
     */
    public void setPath(String path)
    {
        if (GET.equals(getMethod()))
        {
            int index = path.indexOf("?");
            if (index > -1)
            {
                setProperty(PATH, path.substring(0, index));
                parseArguments(path.substring(index + 1));
            }
            else
            {
                setProperty(PATH, path);
            }
        }
        else
        {
            setProperty(PATH, path);
        }
    }

    public void setEncodedPath(String path)
    {
        path = encodePath(path);
        setProperty(ENCODED_PATH, path);
    }

    private String encodePath(String path)
    {
        path =
            Util.substitute(
                (Perl5Matcher) localMatcher.get(),
                patternCache.getPattern(
                    " ",
                    Perl5Compiler.READ_ONLY_MASK
                        & Perl5Compiler.SINGLELINE_MASK),
                spaceSub,
                path,
                Util.SUBSTITUTE_ALL);
        return path;
    }

    public String getEncodedPath()
    {
        return getPropertyAsString(ENCODED_PATH);
    }

    public void setProperty(JMeterProperty prop)
    {
        super.setProperty(prop);
        if (PATH.equals(prop.getName()))
        {
            setEncodedPath(prop.getStringValue());
        }
    }

    public void addProperty(JMeterProperty prop)
    {
        super.addProperty(prop);
        if (PATH.equals(prop.getName()))
        {
            super.addProperty(
                new StringProperty(
                    ENCODED_PATH,
                    encodePath(prop.getStringValue())));
        }
    }

    public String getPath()
    {
        return getPropertyAsString(PATH);
    }

    public void setFollowRedirects(boolean value)
    {
        setProperty(new BooleanProperty(FOLLOW_REDIRECTS, value));
    }

    public boolean getFollowRedirects()
    {
        return getPropertyAsBoolean(FOLLOW_REDIRECTS);
    }

    public void setMethod(String value)
    {
        setProperty(METHOD, value);
    }

    public String getMethod()
    {
        return getPropertyAsString(METHOD);
    }

    public void setUseKeepAlive(boolean value)
    {
        setProperty(new BooleanProperty(USE_KEEPALIVE, value));
    }

    public boolean getUseKeepAlive()
    {
        return getPropertyAsBoolean(USE_KEEPALIVE);
    }

    public void addEncodedArgument(String name, String value, String metaData)
    {
        log.debug(
            "adding argument: name: "
                + name
                + " value: "
                + value
                + " metaData: "
                + metaData);
        Arguments args = getArguments();
        HTTPArgument arg = new HTTPArgument(name, value, metaData, true);

        if (arg.getName().equals(arg.getEncodedName())
            && arg.getValue().equals(arg.getEncodedValue()))
        {
            arg.setAlwaysEncoded(false);
        }
        args.addArgument(arg);
    }

    public void addArgument(String name, String value)
    {
        Arguments args = this.getArguments();
        args.addArgument(new HTTPArgument(name, value));
    }

    public void addTestElement(TestElement el)
    {
        if (el instanceof CookieManager)
        {
            setCookieManager((CookieManager) el);
        }
        else if (el instanceof HeaderManager)
        {
            setHeaderManager((HeaderManager) el);
        }
        else if (el instanceof AuthManager)
        {
            setAuthManager((AuthManager) el);
        }
        else
        {
            super.addTestElement(el);
        }
    }

    public void addArgument(String name, String value, String metadata)
    {
        Arguments args = this.getArguments();
        args.addArgument(new HTTPArgument(name, value, metadata));
    }

    public void setPort(int value)
    {
        setProperty(new IntegerProperty(PORT, value));
    }

    public int getPort()
    {
        int port = getPropertyAsInt(PORT);
        if (port == UNSPECIFIED_PORT)
        {
            if ("https".equalsIgnoreCase(getProtocol()))
            {
                return 443;
            }
            return 80;
        }
        return port;
    }

    public void setDomain(String value)
    {
        setProperty(DOMAIN, value);
    }

    public String getDomain()
    {
        return getPropertyAsString(DOMAIN);
    }

    public void setArguments(Arguments value)
    {
        setProperty(new TestElementProperty(ARGUMENTS, value));
    }

    public Arguments getArguments()
    {
        return (Arguments) getProperty(ARGUMENTS).getObjectValue();
    }

    public void setAuthManager(AuthManager value)
    {
        setProperty(new TestElementProperty(AUTH_MANAGER, value));
    }

    public AuthManager getAuthManager()
    {
        return (AuthManager) getProperty(AUTH_MANAGER).getObjectValue();
    }

    public void setHeaderManager(HeaderManager value)
    {
        setProperty(new TestElementProperty(HEADER_MANAGER, value));
    }

    public HeaderManager getHeaderManager()
    {
        return (HeaderManager) getProperty(HEADER_MANAGER).getObjectValue();
    }

    public void setCookieManager(CookieManager value)
    {
        setProperty(new TestElementProperty(COOKIE_MANAGER, value));
    }

    public CookieManager getCookieManager()
    {
        return (CookieManager) getProperty(COOKIE_MANAGER).getObjectValue();
    }

    public void setMimetype(String value)
    {
        setProperty(MIMETYPE, value);
    }

    public String getMimetype()
    {
        return getPropertyAsString(MIMETYPE);
    }

    public boolean isImageParser()
    {
        return getPropertyAsBoolean(IMAGE_PARSER);
    }

    public void setImageParser(boolean parseImages)
    {
        setProperty(new BooleanProperty(IMAGE_PARSER, parseImages));
    }

    protected final static String NON_HTTP_RESPONSE_CODE =
        "Non HTTP response code";
    protected final static String NON_HTTP_RESPONSE_MESSAGE =
        "Non HTTP response message";
    transient private static Logger log = LoggingManager.getLoggerForClass();

    /**
     * Holds a list of URLs sampled - so we're not flooding stdout with debug
     * information
     */
    //private ArrayList m_sampledURLs = new ArrayList();
    
    /**
     * Constructor for the HTTPSampler object.
     */
    public HTTPSampler()
    {
        setArguments(new Arguments());
    }
    
    public HTTPSampler(URL u)
    {
        setMethod(GET);
        setDomain(u.getHost());
        setPath(u.getPath());
        setPort(u.getPort());
        setProtocol(u.getProtocol());
        parseArguments(u.getQuery());
        setFollowRedirects(true);
        setUseKeepAlive(true);
        setArguments(new Arguments());
    }
    
    /**
     * Do a sampling and return its results.
     *
     * @param e  <code>Entry</code> to be sampled
     * @return   results of the sampling
     */
    public SampleResult sample(Entry e)
    {
        return sample(0);
    }
    
    public SampleResult sample()
    {
        return sample(0);
    }

    public URL getUrl() throws MalformedURLException
    {
        String pathAndQuery = null;
        if (this.getMethod().equals(HTTPSampler.GET)
            && getQueryString().length() > 0)
        {
            if (this.getEncodedPath().indexOf("?") > -1)
            {
                pathAndQuery = this.getEncodedPath() + "&" + getQueryString();
            }
            else
            {
                pathAndQuery = this.getEncodedPath() + "?" + getQueryString();
            }
        }
        else
        {
            pathAndQuery = this.getEncodedPath();
        }
        if (!pathAndQuery.startsWith("/"))
        {
            pathAndQuery = "/" + pathAndQuery;
        }
        if (getPort() == UNSPECIFIED_PORT || getPort() == 80)
        {
            return new URL(getProtocol(), getDomain(), pathAndQuery);
        }
        else
        {
            return new URL(
                getProtocol(),
                getPropertyAsString(HTTPSampler.DOMAIN),
                getPort(),
                pathAndQuery);
        }
    }

    /**
     * Gets the QueryString attribute of the UrlConfig object.
     *
     * @return    the QueryString value
     */
    public String getQueryString()
    {
        StringBuffer buf = new StringBuffer();
        PropertyIterator iter = getArguments().iterator();
        boolean first = true;
        while (iter.hasNext())
        {
            HTTPArgument item = null;
            try
            {
                item = (HTTPArgument) iter.next().getObjectValue();
            }
            catch (ClassCastException e)
            {
                item =
                    new HTTPArgument((Argument) iter.next().getObjectValue());
            }
            if (!first)
            {
                buf.append("&");
            }
            else
            {
                first = false;
            }
            buf.append(item.getEncodedName());
            if (item.getMetaData() == null)
            {
                buf.append("=");
            }
            else
            {
                buf.append(item.getMetaData());
            }
            buf.append(item.getEncodedValue());
        }
        return buf.toString();
    }

    /**
     * Set request headers in preparation to opening a connection.
     *
     * @param conn       <code>URLConnection</code> to set headers on
     * @exception IOException  if an I/O exception occurs
     */
    public void setPostHeaders(URLConnection conn) throws IOException
    {
        postWriter.setHeaders(conn, this);
    }

    /**
     * Send POST data from <code>Entry</code> to the open connection.
     *
     * @param connection <code>URLConnection</code> of where POST data should
     *                   be sent
     * @exception IOException  if an I/O exception occurs
     */
    public void sendPostData(URLConnection connection) throws IOException
    {
        postWriter.sendPostData(connection, this);
    }

    /**
     * Returns a <code>HttpURLConnection</code> with request method(GET or
     * POST), headers, cookies, authorization properly set for the URL request.
     *
     * @param u                <code>URL</code> of the URL request
     * @param method            http/https
     * @param res               the sample result
     * @return                 <code>HttpURLConnection</code> of the URL request
     * @exception IOException  if an I/O Exception occurs
     */
    protected HttpURLConnection setupConnection(
        URL u,
        String method,
        SampleResult res)
        throws IOException
    {
        HttpURLConnection conn;
        // [Jordi <jsalvata@atg.com>]
        // I've not been able to find out why we're not using this
        // feature of HttpURLConnections and we're doing redirection
        // by hand instead. Everything would be so much simpler...
        // [/Jordi]
        // Mike: answer - it didn't work.  Maybe in JDK1.4 it works, but
        // honestly, it doesn't seem like they're working on this.
        // My longer term plan is to use Apache's home grown HTTP Client, or
        // maybe even HTTPUnit's classes.  I'm sure both would be better than
        // Sun's.
        
        // [sebb] Make redirect following configurable (see bug 19004)
        // They do seem to work on JVM 1.4.1_03 (Sun/WinXP)
        HttpURLConnection.setFollowRedirects(delegateRedirects);
        
        conn = (HttpURLConnection) u.openConnection();
        // Delegate SSL specific stuff to SSLManager so that compilation still
        // works otherwise.
        if ("https".equals(u.getProtocol()))
        {
            try
            {
                SSLManager.getInstance().setContext(conn);
            }
            catch (Exception e)
            {
                log.warn(
                    "You may have forgotten to set the ssl.provider property "
                        + "in jmeter.properties",
                    e);
            }
        }

        // a well-bahaved browser is supposed to send 'Connection: close'
        // with the last request to an HTTP server. Instead, most browsers
        // leave it to the server to close the connection after their
        // timeout period. Leave it to the JMeter user to decide.
        if (getUseKeepAlive())
        {
            conn.setRequestProperty("Connection", "keep-alive");
        }
        else
        {
            conn.setRequestProperty("Connection", "close");
        }

        conn.setRequestMethod(method);
        setConnectionHeaders(conn, u, getHeaderManager());
        String cookies = setConnectionCookie(conn, u, getCookieManager());
        if (res != null)
        {
            StringBuffer sb = new StringBuffer();
            sb.append(this.toString());
            if (cookies != null){ //TODO put these in requestHeaders.
				sb.append("\nCookie Data:\n");
				sb.append(cookies);
            }
            res.setSamplerData(sb.toString());
            res.setRequestHeaders("TODO");//TODO
        }
        setConnectionAuthorization(conn, u, getAuthManager());
        return conn;
    }

    //Mark Walsh 2002-08-03, modified to also parse a parameter name value
    //string, where string contains only the parameter name and no equal sign.
    /**
     * This method allows a proxy server to send over the raw text from a
     * browser's output stream to be parsed and stored correctly into the
     * UrlConfig object.
     */
    public void parseArguments(String queryString)
    {
        String[] args = JOrphanUtils.split(queryString, "&");
        for (int i = 0; i < args.length; i++)
        {
            // need to handle four cases:   string contains name=value
            //                              string contains name=
            //                              string contains name
            //                              empty string
            // find end of parameter name
            int endOfNameIndex = 0;
            String metaData = ""; // records the existance of an equal sign
            if (args[i].indexOf("=") != -1)
            {
                // case of name=value, name=
                endOfNameIndex = args[i].indexOf("=");
                metaData = "=";
            }
            else
            {
                metaData = "";
                if (args[i].length() > 0)
                {
                    endOfNameIndex = args[i].length(); // case name
                }
                else
                {
                    endOfNameIndex = 0; //case where name value string is empty
                }
            }
            // parse name
            String name = ""; // for empty string
            if (args[i].length() > 0)
            {
                //for non empty string
                name = args[i].substring(0, endOfNameIndex);
            }
            // parse value
            String value = "";
            if ((endOfNameIndex + 1) < args[i].length())
            {
                value = args[i].substring(endOfNameIndex + 1, args[i].length());
            }
            if (name.length() > 0)
            {
                // In JDK 1.2, the decode() method has a throws clause:
                // "throws Exception". In JDK 1.3, the method does not have
                // a throws clause. So, in order to be JDK 1.2 compliant,
                // we need to add a try/catch around the method call.
                try
                {
                    addEncodedArgument(name, value, metaData);
                }
                catch (Exception e)
                {
                    log.error(
                        "UrlConfig:parseArguments(): Unable to parse argument=["
                            + value
                            + "]");
                    log.error(
                        "UrlConfig:parseArguments(): queryString=["
                            + queryString
                            + "]",
                        e);
                }
            }
        }
    }

    /**
     * Reads the response from the URL connection.
     *
     * @param conn             URL from which to read response
     * @return                 response in <code>String</code>
     * @exception IOException  if an I/O exception occurs
     */
    protected byte[] readResponse(HttpURLConnection conn) throws IOException
    {
        byte[] readBuffer = JMeterContextService.getContext().getReadBuffer();
        BufferedInputStream in;
        try
        {
            if (conn.getContentEncoding() != null
                && conn.getContentEncoding().equals("gzip"))
            {
                in =
                    new BufferedInputStream(
                        new GZIPInputStream(conn.getInputStream()));
            }
            else
            {
                in = new BufferedInputStream(conn.getInputStream());
            }
        }
        catch(IOException e){
        	if (e.getCause() instanceof FileNotFoundException){
				log.warn(e.getCause().toString());
        	} else {
				log.error("Getting error message from server",e);
        	}
			in = new BufferedInputStream(conn.getErrorStream());
        }
        catch (Exception e)
        {
            log.warn("Getting error message from server",e);
            in = new BufferedInputStream(conn.getErrorStream());
        }
        java.io.ByteArrayOutputStream w = new ByteArrayOutputStream();
        int x = 0;
        while ((x = in.read(readBuffer)) > -1)
        {
            w.write(readBuffer, 0, x);
        }
        in.close();
        w.flush();
        w.close();
        return w.toByteArray();
    }
	/**
	 * Gets the ResponseHeaders from the URLConnection
	 *
	 * @param conn  connection from which the headers are read
	 * 
	 * @return string containing the headers, one per line
	 */
	protected String getResponseHeaders(
		HttpURLConnection conn)
		throws IOException
	{
		StringBuffer headerBuf = new StringBuffer();
		headerBuf.append(conn.getHeaderField(0).substring(0, 8));
		headerBuf.append(" ");
		headerBuf.append(conn.getResponseCode());
		headerBuf.append(" ");
		headerBuf.append(conn.getResponseMessage());
		headerBuf.append("\n");

		for (int i = 1; conn.getHeaderFieldKey(i) != null; i++)
		{
			if (!conn //TODO - why is this not saved?
				.getHeaderFieldKey(i)
				.equalsIgnoreCase("transfer-encoding"))
			{
				headerBuf.append(conn.getHeaderFieldKey(i));
				headerBuf.append(": ");
				headerBuf.append(conn.getHeaderField(i));
				headerBuf.append("\n");
			}
		}
		return headerBuf.toString();
	}

    /**
     * Extracts all the required cookies for that particular URL request and set
     * them in the <code>HttpURLConnection</code> passed in.
     *
     * @param conn          <code>HttpUrlConnection</code> which represents the
     *                      URL request
     * @param u             <code>URL</code> of the URL request
     * @param cookieManager the <code>CookieManager</code> containing all the
     *                      cookies for this <code>UrlConfig</code>
     */
    private String setConnectionCookie(
        HttpURLConnection conn,
        URL u,
        CookieManager cookieManager)
    {
        String cookieHeader = null;
        if (cookieManager != null)
        {
            cookieHeader = cookieManager.getCookieHeaderForURL(u);
            if (cookieHeader != null)
            {
                conn.setRequestProperty("Cookie", cookieHeader);
            }
        }
        return cookieHeader;
    }

    /**
     * Extracts all the required headers for that particular URL request and set
     * them in the <code>HttpURLConnection</code> passed in
     *
     *@param conn           <code>HttpUrlConnection</code> which represents the
     *                      URL request
     *@param u              <code>URL</code> of the URL request
     *@param headerManager  the <code>HeaderManager</code> containing all the
     *                      cookies for this <code>UrlConfig</code>
     */
    private void setConnectionHeaders(
        HttpURLConnection conn,
        URL u,
        HeaderManager headerManager)
    {
        if (headerManager != null)
        {
            CollectionProperty headers = headerManager.getHeaders();
            if (headers != null)
            {
                PropertyIterator i = headers.iterator();
                while (i.hasNext())
                {
                    Header header = (Header) i.next().getObjectValue();
                    conn.setRequestProperty(
                        header.getName(),
                        header.getValue());
                }
            }
        }
    }

    /**
     * Extracts all the required authorization for that particular URL request
     * and set them in the <code>HttpURLConnection</code> passed in.
     *
     * @param conn        <code>HttpUrlConnection</code> which represents the
     *                    URL request
     * @param u           <code>URL</code> of the URL request
     * @param authManager the <code>AuthManager</code> containing all the
     *                    cookies for this <code>UrlConfig</code>
     */
    private void setConnectionAuthorization(
        HttpURLConnection conn,
        URL u,
        AuthManager authManager)
    {
        if (authManager != null)
        {
            String authHeader = authManager.getAuthHeaderForURL(u);
            if (authHeader != null)
            {
                conn.setRequestProperty("Authorization", authHeader);
            }
        }
    }

    /**
     * Get the response code of the URL connection and divide it by 100 thus
     * returning 2(for 2xx response codes), 3(for 3xx reponse codes), etc
     *
     * @param conn  <code>HttpURLConnection</code> of URL request
     * @param res   where all results of sampling will be stored
     * @param time  time when the URL request was first started
     * @return      HTTP response code divided by 100
     */
    private int getErrorLevel(
        HttpURLConnection conn,
        SampleResult res,
        long time)
        throws IOException
    {
        int errorLevel = 200;
        String message = null;
        errorLevel = ((HttpURLConnection) conn).getResponseCode();
        message = ((HttpURLConnection) conn).getResponseMessage();
        res.setResponseCode(Integer.toString(errorLevel));
        res.setResponseMessage(message);
        return errorLevel;
    }
    
    public void removeArguments()
    {
        setProperty(
            new TestElementProperty(HTTPSampler.ARGUMENTS, new Arguments()));
    }
    
    /**
     * Follow redirection manually. Normally if the web server does a
     * redirection the intermediate page is not returned. Only the resultant
     * page and the response code for the page will be returned. With
     * redirection turned off, the response code of 3xx will be returned
     * together with a "Location" header-value pair to indicate that the
     * "Location" value needs to be followed to get the resultant page.
     *
     * @param conn                       connection
     * @param u
     * @exception MalformedURLException  if URL is not understood
     */
    private void redirectUrl(HttpURLConnection conn, URL u)
        throws MalformedURLException
    {
        String loc = conn.getHeaderField("Location");
        if (loc != null)
        {
            if (loc.indexOf("http") == -1)
            {
                String tempURL = u.toString();
                if (loc.startsWith("/"))
                {
                    int ind = tempURL.indexOf("//") + 2;
                    loc =
                        tempURL.substring(0, tempURL.indexOf("/", ind) + 1)
                            + loc.substring(1);
                }
                else
                {
                    loc =
                        u.toString().substring(
                            0,
                            u.toString().lastIndexOf('/') + 1)
                            + loc;
                }
            }
        }

        URL newUrl = new URL(loc);
        setMethod(GET);
        setProtocol(newUrl.getProtocol());
        setDomain(newUrl.getHost());
        setPort(newUrl.getPort());
        setPath(newUrl.getFile());
        removeArguments();
        parseArguments(newUrl.getQuery());
    }

    protected long connect() throws IOException
    {
        long time = System.currentTimeMillis();
        try
        {
            conn.connect();
            connectionTries = 0;
        }
        catch (BindException e)
        {
            log.debug("Bind exception, try again");
            if (connectionTries++ == 10)
            {
                log.error("Can't connect", e);
                throw e;
            }
            conn.disconnect();
            conn = null;
            System.gc();
            Runtime.getRuntime().runFinalization();
            this.setUseKeepAlive(false);
            conn = setupConnection(getUrl(), getMethod(), null);
            if (getMethod().equals(HTTPSampler.POST))
            {
                setPostHeaders(conn);
            }
            time = connect();
        }
        catch (IOException e)
        {
            log.debug("Connection failed, giving up");
            conn.disconnect();
            conn = null;
            System.gc();
            Runtime.getRuntime().runFinalization();
            throw e;
        }
        return time;
    }

    /**
     * Samples <code>Entry</code> passed in and stores the result in
     * <code>SampleResult</code>.
     *
     * @param e           <code>Entry</code> to be sampled
     * @param redirects   the level of redirection we're processing (0 means
     *                    original request) -- just used to prevent
     *                    an infinite loop.
     * @return            results of the sampling
     */
    private SampleResult sample(int redirects)
    {
        log.debug("Start : sample, redirects ="+redirects);
        long time = System.currentTimeMillis();
        SampleResult res = new SampleResult();
        URL u = null;
        try
        {
            u = getUrl();
            res.setSampleLabel(getName());
            // specify the data to the result.
            //            res.setSamplerData(this.toString());
            /****************************************
             * END - cached logging hack
             ***************************************/
            if (log.isDebugEnabled())
            {
                log.debug("sample2 : sampling url - " + u);
            }
            conn = setupConnection(u, getMethod(), res);
            if (getMethod().equals(HTTPSampler.POST))
            {
                setPostHeaders(conn);
                time = connect();
                sendPostData(conn);
            }
            else
            {
                time = connect();
            }
            saveConnectionCookies(conn, u, getCookieManager());
            int errorLevel = 0;
            try
            {
                errorLevel = getErrorLevel(conn, res, time);
            }
            catch (IOException e)
            {
                time = bundleResponseInResult(time, res, conn);
                res.setSuccessful(false);
                res.setTime(time);
                return res;
            }
            if (errorLevel / 100 == 2 || errorLevel == 304)
            {
                time = bundleResponseInResult(time, res, conn);
            }
            else if (errorLevel / 100 == 3)
            {
                if (redirects >= MAX_REDIRECTS)
                {
                    throw new IOException(
                        "Maximum number of redirects exceeded");
                }

                if (!getFollowRedirects())
                {
                    time = bundleResponseInResult(time, res, conn);
                }
                else
                {
                    time = bundleResponseInResult(time, res, conn);
                    //System.currentTimeMillis() - time;
                    redirectUrl(conn, u);
                    SampleResult redirectResult = sample(redirects + 1);
                    res.addSubResult(redirectResult);
                    //res.setResponseData(redirectResult.getResponseData());
                    res.setSuccessful(redirectResult.isSuccessful());
                    time += redirectResult.getTime();
                }
            }
            else
            {
                // Could not sample the URL
                time = bundleResponseInResult(time, res, conn);
                res.setSuccessful(false);
            }
            res.setTime(time);
            log.debug("End : sample, redirects="+redirects);
            if (isImageParser())
            {
                if (imageSampler == null)
                {
                    imageSampler = new HTTPSamplerFull();
                }
                res = imageSampler.parseForImages(res, this);
            }
            return res;
        }
        catch (Exception ex)
        {
            log.warn(ex.getMessage(), ex);
            res.setDataType(SampleResult.TEXT);
            res.setResponseData(ex.toString().getBytes());
            res.setResponseCode(NON_HTTP_RESPONSE_CODE);
            res.setResponseMessage(NON_HTTP_RESPONSE_MESSAGE);
            res.setTime(System.currentTimeMillis() - time);
            res.setSuccessful(false);
        }
        finally
        {
            try
            {
                // calling disconnect doesn't close the connection immediately,
                // but indicates we're through with it.  The JVM should close
                // it when necessary.
                disconnect(conn);
            }
            catch (Exception e)
            {}

        }
        log.debug("End : sample, redirects="+redirects);
        return res;
    }
    protected void disconnect(HttpURLConnection conn)
    {
        String connection = conn.getHeaderField("Connection");
        boolean http11 = conn.getHeaderField(0).startsWith("HTTP/1.1");
        if ((connection == null && !http11)
            || (connection != null && connection.equalsIgnoreCase("close")))
        {
            conn.disconnect();
        }
    }

    private long bundleResponseInResult(
        long time,
        SampleResult res,
        HttpURLConnection conn)
        throws IOException, FileNotFoundException
    {
        byte[] ret = readResponse(conn);
        time = System.currentTimeMillis() - time;
        String ct = conn.getHeaderField("Content-type");
        res.setContentType(ct);
        res.setResponseHeaders(getResponseHeaders(conn)); 
		res.setResponseData(ret);
		if (ct.startsWith("image/")){
			res.setDataType(SampleResult.BINARY);
		} else {
			res.setDataType(SampleResult.TEXT);
		}
        res.setSuccessful(true);
        return time;
    }

    /**
     * From the <code>HttpURLConnection</code>, store all the "set-cookie"
     * key-pair values in the cookieManager of the <code>UrlConfig</code>.
     *
     * @param conn          <code>HttpUrlConnection</code> which represents the
     *                      URL request
     * @param u             <code>URL</code> of the URL request
     * @param cookieManager the <code>CookieManager</code> containing all the
     *                      cookies for this <code>UrlConfig</code>
     */
    private void saveConnectionCookies(
        HttpURLConnection conn,
        URL u,
        CookieManager cookieManager)
    {
        if (cookieManager != null)
        {
            for (int i = 1; conn.getHeaderFieldKey(i) != null; i++)
            {
                if (conn.getHeaderFieldKey(i).equalsIgnoreCase("set-cookie"))
                {
                    cookieManager.addCookieFromHeader(
                        conn.getHeaderField(i),
                        u);
                }
            }
        }
    }

    public String toString()
    {
        try
        {
            return this.getUrl().toString()
                + ((POST.equals(getMethod()))
                    ? "\nQuery Data: " + getQueryString()
                    : "");
        }
        catch (MalformedURLException e)
        {
            return "";
        }
    }

    public static class Test extends junit.framework.TestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void testArgumentWithoutEquals() throws Exception
        {
            HTTPSampler sampler = new HTTPSampler();
            sampler.setProtocol("http");
            sampler.setMethod(HTTPSampler.GET);
            sampler.setPath("/index.html?pear");
            sampler.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?pear",
                sampler.getUrl().toString());
        }

        public void testMakingUrl() throws Exception
        {
            HTTPSampler config = new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSampler.GET);
            config.addArgument("param1", "value1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=value1",
                config.getUrl().toString());
        }
        public void testMakingUrl2() throws Exception
        {
            HTTPSampler config = new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSampler.GET);
            config.addArgument("param1", "value1");
            config.setPath("/index.html?p1=p2");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=value1&p1=p2",
                config.getUrl().toString());
        }
        public void testMakingUrl3() throws Exception
        {
            HTTPSampler config = new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSampler.POST);
            config.addArgument("param1", "value1");
            config.setPath("/index.html?p1=p2");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?p1=p2",
                config.getUrl().toString());
        }
        
        // test cases for making Url, and exercise method
        // addArgument(String name,String value,String metadata)

        public void testMakingUrl4() throws Exception
        {
            HTTPSampler config = new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSampler.GET);
            config.addArgument("param1", "value1", "=");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=value1",
                config.getUrl().toString());
        }
        public void testMakingUrl5() throws Exception
        {
            HTTPSampler config = new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSampler.GET);
            config.addArgument("param1", "", "=");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=",
                config.getUrl().toString());
        }
        public void testMakingUrl6() throws Exception
        {
            HTTPSampler config = new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSampler.GET);
            config.addArgument("param1", "", "");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1",
                config.getUrl().toString());
        }

        // test cases for making Url, and exercise method
        // parseArguments(String queryString)
        
        public void testMakingUrl7() throws Exception
        {
            HTTPSampler config = new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSampler.GET);
            config.parseArguments("param1=value1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=value1",
                config.getUrl().toString());
        }

        public void testMakingUrl8() throws Exception
        {
            HTTPSampler config = new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSampler.GET);
            config.parseArguments("param1=");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=",
                config.getUrl().toString());
        }

        public void testMakingUrl9() throws Exception
        {
            HTTPSampler config = new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSampler.GET);
            config.parseArguments("param1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1",
                config.getUrl().toString());
        }

        public void testMakingUrl10() throws Exception
        {
            HTTPSampler config = new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(HTTPSampler.GET);
            config.parseArguments("");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html",
                config.getUrl().toString());
        }
    }
}
