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
 */
package org.apache.jmeter.protocol.http.sampler;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.zip.GZIPInputStream;

import org.apache.jmeter.config.Arguments;

import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;

import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.SSLManager;

import org.apache.jorphan.logging.LoggingManager;

import org.apache.log.Logger;

/**
 * A sampler which understands all the parts necessary to read statistics about
 * HTTP requests, including cookies and authentication.
 *
 * @version $Revision$ Last updated $Date$
 */
public class HTTPSampler extends HTTPSamplerBase
{
	transient private static Logger log= LoggingManager.getLoggerForClass();

    private static final int MAX_CONN_RETRIES = 10; // Maximum connection retries
    
    //protected static String encoding= "iso-8859-1";
    private static final PostWriter postWriter= new PostWriter();

    static {// TODO - document what this is doing and why
        System.setProperty(
            "java.protocol.handler.pkgs",
            JMeterUtils.getPropDefault(
                "ssl.pkgs",
                "com.sun.net.ssl.internal.www.protocol"));
        System.setProperty("javax.net.ssl.debug", "all");
    }

	/**
	 * Constructor for the HTTPSampler object.
	 */
	public HTTPSampler()
	{
		setArguments(new Arguments());
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
     * @param connection <code>URLConnection</code> where POST data should
     *                   be sent
     * @exception IOException  if an I/O exception occurs
     */
    public void sendPostData(URLConnection connection) throws IOException
    {
        postWriter.sendPostData(connection, this);
    }

    /**
     * Returns an <code>HttpURLConnection</code> fully ready to attempt 
     * connection. This means it sets the request method (GET or
     * POST), headers, cookies, and authorization for the URL request.
     * <p>
     * The request infos are saved into the sample result if one is provided.
     *
     * @param u                <code>URL</code> of the URL request
     * @param method            http/https
     * @param res               sample result to save request infos to 
     * @return                 <code>HttpURLConnection</code> ready for .connect
     * @exception IOException  if an I/O Exception occurs
     */
    protected HttpURLConnection setupConnection(
        URL u,
        String method,
        HTTPSampleResult res)
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
        HttpURLConnection.setFollowRedirects(getPropertyAsBoolean(AUTO_REDIRECTS));

        conn= (HttpURLConnection)u.openConnection();
        // Delegate SSL specific stuff to SSLManager so that compilation still
        // works otherwise.
        if ("https".equalsIgnoreCase(u.getProtocol()))
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
        String hdrs=setConnectionHeaders(conn, u, getHeaderManager());
        String cookies= setConnectionCookie(conn, u, getCookieManager());
        if (res != null)
        {
            StringBuffer sb= new StringBuffer();
            if (method.equals(POST))
            {
            	String q = this.getQueryString();
				res.setQueryString(q);
                sb.append("Query data:\n");
                sb.append(q);
                sb.append('\n');
            }
            if (cookies != null)
            { 
            	res.setCookies(cookies);
                sb.append("\nCookie Data:\n");
                sb.append(cookies);
                sb.append('\n');
            }
            res.setSamplerData(sb.toString());
            //TODO rather than stuff all the information in here,
            //pick it up from the individual fields later 
            
            res.setURL(u);
            res.setHTTPMethod(method);
            res.setRequestHeaders(hdrs);
        }
        setConnectionAuthorization(conn, u, getAuthManager());
        if (method.equals(POST))
        {
            setPostHeaders(conn);
        }
        return conn;
    }

    /**
     * Reads the response from the URL connection.
     *
     * @param conn             URL from which to read response
     * @return                 response content
     * @exception IOException  if an I/O exception occurs
     */
    protected byte[] readResponse(HttpURLConnection conn) throws IOException
    {
        byte[] readBuffer= getThreadContext().getReadBuffer();
        BufferedInputStream in;
        boolean logError=false; // Should we log the error?
        try
        {
            if ("gzip".equals(conn.getContentEncoding()))// works OK even if CE is null
            {
                in=
                    new BufferedInputStream(
                        new GZIPInputStream(conn.getInputStream()));
            }
            else
            {
                in= new BufferedInputStream(conn.getInputStream());
            }
        }
        catch (IOException e)
        {
        	//TODO: try to improve error discrimination when using JDK1.3
        	// and/or conditionally call .getCause()
        	
			//JDK1.4: if (e.getCause() instanceof FileNotFoundException)
			//JDK1.4: {
			//JDK1.4:     log.warn(e.getCause().toString());
			//JDK1.4: }
			//JDK1.4: else
            {
                log.error(e.toString());
				//JDK1.4: Throwable cause = e.getCause();
				//JDK1.4: if (cause != null){
				//JDK1.4: 	log.error("Cause: "+cause);
				//JDK1.4: }
                logError=true;
            }
            in= new BufferedInputStream(conn.getErrorStream());
        }
        catch (Exception e)
        {
            log.error(e.toString());
			//JDK1.4: Throwable cause = e.getCause();
			//JDK1.4: if (cause != null){
			//JDK1.4: 	log.error("Cause: "+cause);
			//JDK1.4: }
            in= new BufferedInputStream(conn.getErrorStream());
			logError=true;
        }
        java.io.ByteArrayOutputStream w= new ByteArrayOutputStream();
        int x= 0;
        while ((x= in.read(readBuffer)) > -1)
        {
            w.write(readBuffer, 0, x);
        }
        in.close();
        w.flush();
        w.close();
        if (logError)
        {
        	String s;
        	if (w.size() > 1000){
				s="\n"+w.toString().substring(0,1000)+"\n\t...";
        	} else {
				s="\n"+w.toString();
        	}
        	log.error(s);
        }
        return w.toByteArray();
    }

    /**
     * Gets the ResponseHeaders from the URLConnection
     *
     * @param conn  connection from which the headers are read
     * @return string containing the headers, one per line
     */
    protected String getResponseHeaders(HttpURLConnection conn)
        throws IOException
    {
        StringBuffer headerBuf= new StringBuffer();
		headerBuf.append(conn.getHeaderField(0));//Leave header as is 
//        headerBuf.append(conn.getHeaderField(0).substring(0, 8));
//        headerBuf.append(" ");
//        headerBuf.append(conn.getResponseCode());
//        headerBuf.append(" ");
//        headerBuf.append(conn.getResponseMessage());
        headerBuf.append("\n");

        for (int i= 1; conn.getHeaderFieldKey(i) != null; i++)
        {
            modifyHeaderValues(conn,i, headerBuf);
        }
        return headerBuf.toString();
    }

    /**
     * @param conn connection
     * @param headerIndex which header to use
     * @param resultBuf output string buffer
     */
    protected void modifyHeaderValues(HttpURLConnection conn, int headerIndex, StringBuffer resultBuf) 
    {
        if ("transfer-encoding" //TODO - why is this not saved? A: it might be a proxy server specific field.
                	// If JMeter is using a proxy, the browser wouldn't know about that.
            .equalsIgnoreCase(conn.getHeaderFieldKey(headerIndex)))
        {
           return; 
        }
        resultBuf.append(conn.getHeaderFieldKey(headerIndex));
        resultBuf.append(": ");
        resultBuf.append(conn.getHeaderField(headerIndex));
        resultBuf.append("\n");
    }

    /**
     * Extracts all the required cookies for that particular URL request and
     * sets them in the <code>HttpURLConnection</code> passed in.
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
        String cookieHeader= null;
        if (cookieManager != null)
        {
            cookieHeader= cookieManager.getCookieHeaderForURL(u);
            if (cookieHeader != null)
            {
                conn.setRequestProperty("Cookie", cookieHeader);
            }
        }
        return cookieHeader;
    }

    /**
     * Extracts all the required headers for that particular URL request and
     * sets them in the <code>HttpURLConnection</code> passed in
     *
     *@param conn           <code>HttpUrlConnection</code> which represents the
     *                      URL request
     *@param u              <code>URL</code> of the URL request
     *@param headerManager  the <code>HeaderManager</code> containing all the
     *                      cookies for this <code>UrlConfig</code>
     * @return the headers as a string
     */
    private String setConnectionHeaders(
        HttpURLConnection conn,
        URL u,
        HeaderManager headerManager)
    {
    	StringBuffer hdrs = new StringBuffer(100);
        if (headerManager != null)
        {
            CollectionProperty headers= headerManager.getHeaders();
            if (headers != null)
            {
                PropertyIterator i= headers.iterator();
                while (i.hasNext())
                {
                    Header header= (Header)i.next().getObjectValue();
                    String n=header.getName();
                    String v=header.getValue();
                    conn.setRequestProperty(n,v);
                    hdrs.append(n);  
					hdrs.append(": ");  
					hdrs.append(v);  
					hdrs.append("\n");  
                }
            }
        }
        return hdrs.toString();
    }

    /**
     * Extracts all the required authorization for that particular URL request
     * and sets it in the <code>HttpURLConnection</code> passed in.
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
            String authHeader= authManager.getAuthHeaderForURL(u);
            if (authHeader != null)
            {
                conn.setRequestProperty("Authorization", authHeader);
            }
        }
    }

    /**
     * Samples the URL passed in and stores the result in
     * <code>HTTPSampleResult</code>, following redirects and downloading
     * page resources as appropriate.
     * <p>
     * When getting a redirect target, redirects are not followed and 
     * resources are not downloaded. The caller will take care of this.
     *
     * @param url           URL to sample
     * @param method        HTTP method: GET, POST,...
     * @param areFollowingRedirect whether we're getting a redirect target
     * @param frameDepth    Depth of this target in the frame structure.
     *                      Used only to prevent infinite recursion.
     * @return              results of the sampling
     */
    protected HTTPSampleResult sample(
        URL url,
        String method,
        boolean areFollowingRedirect,
        int frameDepth)
    {
        HttpURLConnection conn= null;

		String urlStr = url.toString();
		log.debug("Start : sample" + urlStr);

        HTTPSampleResult res= new HTTPSampleResult();
        if(this.getPropertyAsBoolean(MONITOR)){
            res.setMonitor(true);
        } else {
			res.setMonitor(false);
        }
		res.setSampleLabel(urlStr);
		res.sampleStart(); // Count the retries as well in the time

        try
        {
            // Sampling proper - establish the connection and read the response:
            // Repeatedly try to connect:
            int retry;
            for (retry= 1; retry <= MAX_CONN_RETRIES; retry++)
            {
                try
                {
                    conn= setupConnection(url, method, res);
                    // Attempt the connection:
                    conn.connect();
                    break;
                }
                catch (BindException e)
                {
                    if (retry >= MAX_CONN_RETRIES)
                    {
                        log.error("Can't connect", e);
                        throw e;
                    }
                    log.debug("Bind exception, try again");
                    conn.disconnect();
                    this.setUseKeepAlive(false);
                    continue; // try again
                }
                catch (IOException e)
                {
                    log.debug("Connection failed, giving up");
                    throw e;
                }
            }
            if (retry > MAX_CONN_RETRIES)
            {
                // This should never happen, but...
                throw new BindException();
            }
            // Nice, we've got a connection. Finish sending the request:
            if (method.equals(POST))
            {
                sendPostData(conn);
            }
            // Request sent. Now get the response:
            byte[] responseData= readResponse(conn);

            res.sampleEnd();
            // Done with the sampling proper.

            // Now collect the results into the HTTPSampleResult:

            res.setResponseData(responseData);

            int errorLevel= conn.getResponseCode();
            res.setResponseCode(Integer.toString(errorLevel));
            res.setSuccessful(200 <= errorLevel && errorLevel <= 399);

            res.setResponseMessage(conn.getResponseMessage());

            String ct= conn.getContentType();//getHeaderField("Content-type");
            res.setContentType(ct);// e.g. text/html; charset=ISO-8859-1
            if (ct != null)
            {
            	// Extract charset and store as DataEncoding
            	// TODO do we need process http-equiv META tags, e.g.:
            	// <META http-equiv="content-type" content="text/html; charset=foobar">
            	// or can we leave that to the renderer ?
            	String de=ct.toLowerCase();
            	final String cs="charset=";
            	int cset= de.indexOf(cs);
            	if (cset >= 0)
            	{
                	res.setDataEncoding(de.substring(cset+cs.length()));
            	}
	           	if (ct.startsWith("image/"))
	            {
	                res.setDataType(HTTPSampleResult.BINARY);
	            }
	            else
	            {
	                res.setDataType(HTTPSampleResult.TEXT);
	            }
            }

            res.setResponseHeaders(getResponseHeaders(conn));
            if (res.isRedirect())
            {
                res.setRedirectLocation(conn.getHeaderField("Location"));
            }

            // Store any cookies received in the cookie manager:
            saveConnectionCookies(conn, url, getCookieManager());

            // Follow redirects and download page resources if appropriate:
            if (!areFollowingRedirect)
            {
                boolean didFollowRedirects= false;
                if (res.isRedirect())
                {
                    log.debug("Location set to - " + res.getRedirectLocation());
                    
                    if (getFollowRedirects())
                    {
                        res= followRedirects(res, frameDepth);
                        didFollowRedirects= true;
                    }
                }

                if (isImageParser()
                    && (HTTPSampleResult.TEXT).equals(res.getDataType())
                    && res.isSuccessful())
                {
                    if (frameDepth > MAX_FRAME_DEPTH)
                    {
                        res.addSubResult(
                            errorResult(
                                new Exception("Maximum frame/iframe nesting depth exceeded."),
                                null,
                                0));
                    }
                    else
                    {
                        // If we followed redirects, we already have a container:
                        boolean createContainerResults= !didFollowRedirects;

                        res=
                            downloadPageResources(
                                res,
                                createContainerResults,
                                frameDepth);
                    }
                }
            }

            log.debug("End : sample");
            return res;
        }
        catch (IOException e)
        {
        	res.sampleEnd();
            return errorResult(e, url.toString(), res.getTime());
        }
        finally
        {
            // calling disconnect doesn't close the connection immediately,
            // but indicates we're through with it.  The JVM should close
            // it when necessary.
            disconnect(conn);
        }
    }

    /**
     * Iteratively download the redirect targets of a redirect response.
     * <p>
     * The returned result will contain one subsample for each request issued,
     * including the original one that was passed in. It will be an
     * HTTPSampleResult that should mostly look as if the final destination
     * of the redirect chain had been obtained in a single shot.
     * 
     * @param res result of the initial request - must be a redirect response
     * @param frameDepth    Depth of this target in the frame structure.
     *                      Used only to prevent infinite recursion.
     * @return "Container" result with one subsample per request issued
     */
    private HTTPSampleResult followRedirects(
        HTTPSampleResult res,
        int frameDepth)
    {
        HTTPSampleResult totalRes= new HTTPSampleResult(res);
        HTTPSampleResult lastRes= res;

        int redirect;
        for (redirect= 0; redirect < MAX_REDIRECTS; redirect++)
        {
            String location= encodeSpaces(lastRes.getRedirectLocation());
                // Browsers seem to tolerate Location headers with spaces,
                // replacing them automatically with %20. We want to emulate
                // this behaviour.
            try
            {
                lastRes=
                    sample(
                        new URL(lastRes.getURL(), location),
                        GET,
                        true,
                        frameDepth);
            }
            catch (MalformedURLException e)
            {
                lastRes= errorResult(e, location, 0);
            }
            totalRes.addSubResult(lastRes);

            if (!lastRes.isRedirect())
            {
                break;
            }
        }
        if (redirect >= MAX_REDIRECTS)
        {
            lastRes=
                errorResult(
                    new IOException("Exceeeded maximum number of redirects: "+MAX_REDIRECTS),
                    null,
                    0);
            totalRes.addSubResult(lastRes);
        }

        // Now populate the any totalRes fields that need to
        // come from lastRes:
        totalRes.setSampleLabel(
            totalRes.getSampleLabel() + "->" + lastRes.getSampleLabel());
        // The following three can be discussed: should they be from the
        // first request or from the final one? I chose to do it this way
        // because that's what browsers do: they show the final URL of the
        // redirect chain in the location field. 
        totalRes.setURL(lastRes.getURL());
        totalRes.setHTTPMethod(lastRes.getHTTPMethod());
        totalRes.setRequestHeaders(lastRes.getRequestHeaders());

        totalRes.setResponseData(lastRes.getResponseData());
        totalRes.setResponseCode(lastRes.getResponseCode());
        totalRes.setSuccessful(lastRes.isSuccessful());
        totalRes.setResponseMessage(lastRes.getResponseMessage());
        totalRes.setDataType(lastRes.getDataType());
        totalRes.setResponseHeaders(lastRes.getResponseHeaders());
        return totalRes;
    }

    protected void disconnect(HttpURLConnection conn)
    {
        if (conn != null)
        {
            String connection= conn.getHeaderField("Connection");
            String protocol= conn.getHeaderField(0);
            if ((connection == null && (protocol == null || !protocol.startsWith("HTTP/1.1")))
                || (connection != null && connection.equalsIgnoreCase("close")))
            {
                conn.disconnect();
            }
        }
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
            for (int i= 1; conn.getHeaderFieldKey(i) != null; i++)
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

    public static class Test extends junit.framework.TestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void testArgumentWithoutEquals() throws Exception
        {
            HTTPSampler sampler= new HTTPSampler();
            sampler.setProtocol("http");
            sampler.setMethod(GET);
            sampler.setPath("/index.html?pear");
            sampler.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?pear",
                sampler.getUrl().toString());
        }

        public void testMakingUrl() throws Exception
        {
            HTTPSampler config= new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(GET);
            config.addArgument("param1", "value1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=value1",
                config.getUrl().toString());
        }
        public void testMakingUrl2() throws Exception
        {
            HTTPSampler config= new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(GET);
            config.addArgument("param1", "value1");
            config.setPath("/index.html?p1=p2");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=value1&p1=p2",
                config.getUrl().toString());
        }
        public void testMakingUrl3() throws Exception
        {
            HTTPSampler config= new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(POST);
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
            HTTPSampler config= new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(GET);
            config.addArgument("param1", "value1", "=");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=value1",
                config.getUrl().toString());
        }
        public void testMakingUrl5() throws Exception
        {
            HTTPSampler config= new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(GET);
            config.addArgument("param1", "", "=");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=",
                config.getUrl().toString());
        }
        public void testMakingUrl6() throws Exception
        {
            HTTPSampler config= new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(GET);
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
            HTTPSampler config= new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(GET);
            config.parseArguments("param1=value1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=value1",
                config.getUrl().toString());
        }

        public void testMakingUrl8() throws Exception
        {
            HTTPSampler config= new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(GET);
            config.parseArguments("param1=");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1=",
                config.getUrl().toString());
        }

        public void testMakingUrl9() throws Exception
        {
            HTTPSampler config= new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(GET);
            config.parseArguments("param1");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html?param1",
                config.getUrl().toString());
        }

        public void testMakingUrl10() throws Exception
        {
            HTTPSampler config= new HTTPSampler();
            config.setProtocol("http");
            config.setMethod(GET);
            config.parseArguments("");
            config.setPath("/index.html");
            config.setDomain("www.apache.org");
            assertEquals(
                "http://www.apache.org/index.html",
                config.getUrl().toString());
        }
    }
}
