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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.Date;

import org.apache.commons.httpclient.ConnectMethod;
import org.apache.commons.httpclient.DefaultMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;

import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;

import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;

import org.apache.jorphan.logging.LoggingManager;

import org.apache.log.Logger;

/**
 * A sampler which understands all the parts necessary to read statistics about
 * HTTP requests, including cookies and authentication.
 *
 * @version $Revision$ Last updated $Date$
 */
public class HTTPSampler2 extends HTTPSamplerBase
{
	transient private static Logger log= LoggingManager.getLoggerForClass();

	static {
        // Set the default to Avalon Logkit, if not already defined:
        if (System.getProperty("org.apache.commons.logging.Log")==null)
        {
        	System.setProperty("org.apache.commons.logging.Log"
        			,"org.apache.commons.logging.impl.LogKitLogger");
        }
    }

    /*
     * Connection is re-used if possible
     */
    private transient HttpConnection httpConn = null;

    /*
     * These variables are recreated every time
     * Find a better way of passing them round
     */
    private transient HttpMethodBase httpMethod = null;
    private transient HttpState httpState = null;

    /**
	 * Constructor for the HTTPSampler2 object.
	 */
	public HTTPSampler2()
	{
		setArguments(new Arguments());
	}

    /**
     * Set request headers in preparation to opening a connection.
     *
     * @param conn       <code>URLConnection</code> to set headers on
     * @exception IOException  if an I/O exception occurs
     */
    private void setPostHeaders(PostMethod post) throws IOException
    {
    	// Probably nothing needed, because the PostMethod class takes care of it
//        /*postWriter.*/
//    	setHeaders(post, this);
    }

    /**
     * Send POST data from <code>Entry</code> to the open connection.
     *
     * @param connection <code>URLConnection</code> where POST data should
     *                   be sent
     * @exception IOException  if an I/O exception occurs
     */
    private void sendPostData(HttpMethod connection) throws IOException
    {
        /*postWriter.*/
    	sendPostData((PostMethod)connection, this);
    }

    /**
     * Send POST data from Entry to the open connection.
     */
    public void sendPostData(PostMethod post, HTTPSampler2 sampler)
        throws IOException
    {
        PropertyIterator args = sampler.getArguments().iterator();
        while (args.hasNext())
        {
            Argument arg = (Argument) args.next().getObjectValue();
            post.addParameter(arg.getName(),arg.getValue());
        }
        // If filename was specified then send the post using multipart syntax
        String filename = sampler.getFilename();
        if ((filename != null) && (filename.trim().length() > 0))
        {
        	File input = new File(filename);
            if (input.length() < Integer.MAX_VALUE) {
                post.setRequestContentLength((int)input.length());
            } else {
                post.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
            }
            //TODO - is this correct?
            post.setRequestHeader("Content-Disposition",
        		"form-data; name=\""
	            + encode(sampler.getFileField())
	            + "\"; filename=\""
	            + encode(filename)
	            + "\"");
            // Specify content type and encoding
            post.setRequestHeader("Content-Type", sampler.getMimetype());
            post.setRequestBody(new FileInputStream(input));
        }
    }

    private String encode(String value)
    {
        StringBuffer newValue = new StringBuffer();
        char[] chars = value.toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            if (chars[i] == '\\')
            {
                newValue.append("\\\\");
            }
            else
            {
                newValue.append(chars[i]);
            }
        }
        return newValue.toString();
    }

    /**
     * Returns an <code>HttpConnection</code> fully ready to attempt 
     * connection. This means it sets the request method (GET or
     * POST), headers, cookies, and authorization for the URL request.
     * <p>
     * The request infos are saved into the sample result if one is provided.
     *
     * @param u                <code>URL</code> of the URL request
     * @param method            http/https
     * @param res               sample result to save request infos to 
     * @return                 <code>HttpConnection</code> ready for .connect
     * @exception IOException  if an I/O Exception occurs
     */
    private HttpConnection setupConnection(
        URL u,
        String method,
        HTTPSampleResult res)
        throws IOException
    {
    
    	String urlStr = u.toString();

       	org.apache.commons.httpclient.URI 
		    uri = new org.apache.commons.httpclient.URI(urlStr);

        String schema = uri.getScheme();
        if ((schema == null) || (schema.equals("")))
        {
            schema = "http";
        }
        Protocol protocol = Protocol.getProtocol(schema);

        String host = uri.getHost();
        int port = uri.getPort();
        
        HostConfiguration hc = new HostConfiguration();
        hc.setHost(host,port,protocol);
        if (httpConn!= null && hc.hostEquals(httpConn))
        {
        	//Same details, no need to reset
        }
        else
        {
        	httpConn = new HttpConnection(hc);
        	//TODO check these
            httpConn.setProxyHost(System.getProperty("http.proxyHost"));
            httpConn.setProxyPort( Integer.parseInt(System.getProperty("http.proxyPort","80")));
        }

        if (method.equals(POST))
        {
            httpMethod = new PostMethod(urlStr);
        }
        else
        {
        	httpMethod = new GetMethod(urlStr);
        	//httpMethod;
        	new DefaultMethodRetryHandler();
        }

        httpMethod.setHttp11(!JMeterUtils.getPropDefault("httpclient.version","1.1").equals("1.0"));

        // Set the timeout (if non-zero)
        httpConn.setSoTimeout(JMeterUtils.getPropDefault("httpclient.timeout",0));

        httpState = new HttpState();
        if (httpConn.isProxied() && httpConn.isSecure()) {
            httpMethod = new ConnectMethod(httpMethod);
        }
        
        // Allow HttpClient to handle the redirects:
        httpMethod.setFollowRedirects(getPropertyAsBoolean(AUTO_REDIRECTS));
        
        // a well-behaved browser is supposed to send 'Connection: close'
        // with the last request to an HTTP server. Instead, most browsers
        // leave it to the server to close the connection after their
        // timeout period. Leave it to the JMeter user to decide.
        if (getUseKeepAlive())
        {
        	httpMethod.setRequestHeader("Connection", "keep-alive");
        }
        else
        {
        	httpMethod.setRequestHeader("Connection", "close");
        }

        String hdrs=setConnectionHeaders(httpMethod, u, getHeaderManager());
        String cookies= setConnectionCookie(httpMethod, u, getCookieManager());

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

        setConnectionAuthorization(httpMethod, u, getAuthManager());

        if (method.equals(POST))
        {
            setPostHeaders((PostMethod) httpMethod);
        }
        return httpConn;
    }

    /**
     * Gets the ResponseHeaders
     *
     * @param method  connection from which the headers are read
     * @return string containing the headers, one per line
     */
    protected String getResponseHeaders(HttpMethod method)
        throws IOException
    {
        StringBuffer headerBuf= new StringBuffer();
        org.apache.commons.httpclient.Header rh[] = method.getResponseHeaders();
		headerBuf.append(method.getStatusLine());//header[0] is not the status line... 
        headerBuf.append("\n");

        for (int i= 0; i < rh.length; i++)
        {
        	String key = rh[i].getName();
            if (!key.equalsIgnoreCase("transfer-encoding"))//TODO - why is this not saved?
            {
                headerBuf.append(key);
                headerBuf.append(": ");
                headerBuf.append(rh[i].getValue());
                headerBuf.append("\n");
            }
        }
        return headerBuf.toString();
    }

    /**
     * Extracts all the required cookies for that particular URL request and
     * sets them in the <code>HttpMethod</code> passed in.
     *
     * @param method  <code>HttpMethod</code> which represents the request
     * @param u             <code>URL</code> of the request
     * @param cookieManager the <code>CookieManager</code> containing all the
     *                      cookies for this <code>UrlConfig</code>
     */
    private String setConnectionCookie(
        HttpMethod method,
        URL u,
        CookieManager cookieManager)
    {
        String cookieHeader= null;
        if (cookieManager != null)
        {
            cookieHeader= cookieManager.getCookieHeaderForURL(u);
            if (cookieHeader != null)
            {
                method.setRequestHeader("Cookie", cookieHeader);
            }
        }
        return cookieHeader;
    }

    /**
     * Extracts all the required headers for that particular URL request and
     * sets them in the <code>HttpMethod</code> passed in
     *
     *@param method           <code>HttpMethod</code> which represents the request
     *@param u              <code>URL</code> of the URL request
     *@param headerManager  the <code>HeaderManager</code> containing all the
     *                      cookies for this <code>UrlConfig</code>
     * @return the headers as a string
     */
    private String setConnectionHeaders(
        HttpMethod method,
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
                    org.apache.jmeter.protocol.http.control.Header header =
                    	(org.apache.jmeter.protocol.http.control.Header)i.next().getObjectValue();
                    String n=header.getName();
                    String v=header.getValue();
                    method.setRequestHeader(n,v);
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
     * and sets it in the <code>HttpMethod</code> passed in.
     *
     * @param method    <code>HttpMethod</code> which represents the request
     * @param u           <code>URL</code> of the URL request
     * @param authManager the <code>AuthManager</code> containing all the
     *                    cookies for this <code>UrlConfig</code>
     */
    private void setConnectionAuthorization(
        HttpMethod method,
        URL u,
        AuthManager authManager)
    {
        if (authManager != null)
        {
        	// Old method
            String authHeader= authManager.getAuthHeaderForURL(u);
            if (authHeader != null)
            {
                method.setRequestHeader("Authorization", authHeader);
            }

            //TODO: replace with something like this:
//            Authorization auth= authManager.getAuthForURL(u);
//            if (auth != null)
//            {
//            	// TODO - set up realm, thishost and domain
//            	httpState.setCredentials(
//                	null,         // "realm"
//					auth.getURL(),
//                	new NTCredentials(// Includes other types of Credentials
//                			auth.getUser(),
//							auth.getPass(),
//                			null, // "thishost",
//							null  // "targetdomain"
//							)
//					);
//            }
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

        String urlStr = url.toString();
        
		log.debug("Start : sample" + urlStr);
		log.debug("method" + method);

		httpMethod=null;
		
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
            HttpConnection connection = setupConnection(url, method, res);
            	
            if (method.equals(POST))
            {
                sendPostData(httpMethod);
            }

            int statusCode = httpMethod.execute(httpState, connection);

            // Request sent. Now get the response:
            byte[] responseData= httpMethod.getResponseBody();

            res.sampleEnd();
            // Done with the sampling proper.

            // Now collect the results into the HTTPSampleResult:

            res.setSampleLabel(httpMethod.getURI().toString());// Pick up Actual path (after redirects)
            res.setResponseData(responseData);

            res.setResponseCode(Integer.toString(statusCode));
            res.setSuccessful(200 <= statusCode && statusCode <= 399);

            res.setResponseMessage(httpMethod.getStatusText());

            String ct=null;
            org.apache.commons.httpclient.Header h = 
            	httpMethod.getResponseHeader("Content-Type");
            if (h!=null)// Can be missing, e.g. on redirect
            {
                ct= h.getValue();
                res.setContentType(ct);// e.g. text/html; charset=ISO-8859-1
            }
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

            res.setResponseHeaders(getResponseHeaders(httpMethod));
            if (res.isRedirect())
            {
                res.setRedirectLocation(httpMethod.getResponseHeader("Location").getValue());
            }

            // Store any cookies received in the cookie manager:
            saveConnectionCookies(httpState, getCookieManager());

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
            if (httpMethod != null) httpMethod.releaseConnection();
            return res;
        }
        catch (IllegalArgumentException e)// e.g. some kinds of invalid URL
        {
        	res.sampleEnd();
            HTTPSampleResult err = errorResult(e, url.toString(), res.getTime());
            err.setSampleLabel("Error: "+url.toString());
            return err;
        }
        catch (IOException e)
        {
        	res.sampleEnd();
            HTTPSampleResult err = errorResult(e, url.toString(), res.getTime());
            err.setSampleLabel("Error: "+url.toString());
            return err;
        }
        finally
        {
            if (httpMethod != null) httpMethod.releaseConnection();
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

    /**
     * From the <code>HttpState</code>, store all the "set-cookie"
     * key-pair values in the cookieManager of the <code>UrlConfig</code>.
     *
     * @param state        <code>HttpState</code> which represents the request
     * @param u             <code>URL</code> of the URL request
     * @param cookieManager the <code>CookieManager</code> containing all the
     *                      cookies for this <code>UrlConfig</code>
     */
    private void saveConnectionCookies(
        HttpState state,
        CookieManager cookieManager)
    {
        if (cookieManager != null)
        {
        	org.apache.commons.httpclient.Cookie [] c = state.getCookies();
            for (int i= 0; i < c.length ; i++)
            {
            	   Date exp = c[i].getExpiryDate();// might be absent
                   cookieManager.add(
                   		new org.apache.jmeter.protocol.http.control.
						Cookie(c[i].getName(),
								c[i].getValue(),
								c[i].getDomain(),
								c[i].getPath(),
								c[i].getSecure(),
								exp != null ? exp.getTime()
								: System.currentTimeMillis() + 1000 * 60 * 60 * 24 //cf CookieManager
							  )
						);
            }
        }
    }

    public void threadStarted()
	{
    	log.info("Thread Started");
    }
    
    public void threadFinished()
	{
    	log.info("Thread Finished");
    	if (httpConn != null) httpConn.close();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////
    
    public static class Test extends junit.framework.TestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void testArgumentWithoutEquals() throws Exception
        {
            HTTPSampler2 sampler= new HTTPSampler2();
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
            HTTPSampler2 config= new HTTPSampler2();
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
            HTTPSampler2 config= new HTTPSampler2();
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
            HTTPSampler2 config= new HTTPSampler2();
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
            HTTPSampler2 config= new HTTPSampler2();
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
            HTTPSampler2 config= new HTTPSampler2();
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
            HTTPSampler2 config= new HTTPSampler2();
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
            HTTPSampler2 config= new HTTPSampler2();
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
            HTTPSampler2 config= new HTTPSampler2();
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
            HTTPSampler2 config= new HTTPSampler2();
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
            HTTPSampler2 config= new HTTPSampler2();
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
