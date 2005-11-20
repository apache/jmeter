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
 */
package org.apache.jmeter.protocol.http.sampler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.Enumeration;

import org.apache.commons.httpclient.ConnectMethod;
import org.apache.commons.httpclient.DefaultMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpConnection;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.jmeter.config.Argument;

import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.apache.jmeter.protocol.http.util.SlowHttpClientSocketFactory;

import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;

import org.apache.jorphan.logging.LoggingManager;

import org.apache.log.Logger;

/**
 * A sampler which understands all the parts necessary to read statistics about
 * HTTP requests, including cookies and authentication.
 * 
 */
public class HTTPSampler2 extends HTTPSamplerBase {
    private static final Logger log = LoggingManager.getLoggerForClass();

    /*
     * Connection is re-used within the thread if possible
     */
	transient private static ThreadLocal httpClients = null;

    private static boolean basicAuth 
    = JMeterUtils.getPropDefault("httpsampler2.basicauth", false); // $NON-NLS-1$

	static {
		// Set the default to Avalon Logkit, if not already defined:
		if (System.getProperty("org.apache.commons.logging.Log") == null) { // $NON-NLS-1$
			System.setProperty("org.apache.commons.logging.Log" // $NON-NLS-1$
                    , "org.apache.commons.logging.impl.LogKitLogger"); // $NON-NLS-1$
		}
        log.info("httpsampler2.basicauth=" + basicAuth); // $NON-NLS-1$
        
        System.setProperty("apache.commons.httpclient.cookiespec", // $NON-NLS-1$
                "COMPATIBILITY"); // $NON-NLS-1$

        int cps =
            JMeterUtils.getPropDefault("httpclient.socket.http.cps", 0); // $NON-NLS-1$        

        if (cps > 0) {
            log.info("Setting up HTTP SlowProtocol, cps="+cps);
            Protocol.registerProtocol(PROTOCOL_HTTP, 
                    new Protocol(PROTOCOL_HTTP,new SlowHttpClientSocketFactory(cps),DEFAULT_HTTP_PORT));
        }
        cps =
            JMeterUtils.getPropDefault("httpclient.socket.https.cps", 0); // $NON-NLS-1$        

        if (cps > 0) {
            log.info("Setting up HTTPS SlowProtocol, cps="+cps);
            Protocol.registerProtocol(PROTOCOL_HTTPS, 
                    new Protocol(PROTOCOL_HTTPS,new SlowHttpClientSocketFactory(cps),DEFAULT_HTTPS_PORT));
        }
	}

	/*
	 * These variables are recreated every time Find a better way of passing
	 * them round
	 */
	private transient HttpMethodBase httpMethod = null;

	private transient HttpState httpState = null;

	/**
	 * Constructor for the HTTPSampler2 object.
	 */
	public HTTPSampler2() {
	}

	/**
	 * Set request headers in preparation to opening a connection.
	 * 
	 * @param conn
	 *            <code>URLConnection</code> to set headers on
	 * @exception IOException
	 *                if an I/O exception occurs
	 */
	private void setPostHeaders(PostMethod post) throws IOException {
		// Probably nothing needed, because the PostMethod class takes care of
		// it
		// /*postWriter.*/
		// setHeaders(post, this);
	}

	/**
	 * Send POST data from <code>Entry</code> to the open connection.
	 * 
	 * @param connection
	 *            <code>URLConnection</code> where POST data should be sent
	 * @exception IOException
	 *                if an I/O exception occurs
	 */
	private void sendPostData(HttpMethod connection) throws IOException {
		/* postWriter. */
		sendPostData((PostMethod) connection, this);
	}

	/**
	 * Send POST data from Entry to the open connection.
	 */
	public void sendPostData(PostMethod post, HTTPSampler2 sampler) throws IOException {
		PropertyIterator args = sampler.getArguments().iterator();
		while (args.hasNext()) {
			Argument arg = (Argument) args.next().getObjectValue();
			post.addParameter(arg.getName(), arg.getValue());
		}
		// If filename was specified then send the post using multipart syntax
		String filename = sampler.getFilename();
		if ((filename != null) && (filename.trim().length() > 0)) {
			File input = new File(filename);
			if (input.length() < Integer.MAX_VALUE) {
				post.setRequestContentLength((int) input.length());
			} else {
				post.setRequestContentLength(EntityEnclosingMethod.CONTENT_LENGTH_CHUNKED);
			}
			// TODO - is this correct?
			post.setRequestHeader(HEADER_CONTENT_DISPOSITION
                    , "form-data; name=\"" // $NON-NLS-1$ // $NON-NLS-1$
                    + encode(sampler.getFileField())
					+ "\"; filename=\""  // $NON-NLS-1$
                    + encode(filename) + "\""); // $NON-NLS-1$
			// Specify content type and encoding
			post.setRequestHeader(HEADER_CONTENT_TYPE, sampler.getMimetype());
			post.setRequestBody(new FileInputStream(input));
		}
	}

    // Convert \ to \\
	private String encode(String value) {
		StringBuffer newValue = new StringBuffer();
		for (int i = 0; i < value.length(); i++) {
			if (value.charAt(i) == '\\') { // $NON-NLS-1$
				newValue.append("\\\\"); // $NON-NLS-1$
			} else {
				newValue.append(value.charAt(i));
			}
		}
		return newValue.toString();
	}

	/**
	 * Returns an <code>HttpConnection</code> fully ready to attempt
	 * connection. This means it sets the request method (GET or POST), headers,
	 * cookies, and authorization for the URL request.
	 * <p>
	 * The request infos are saved into the sample result if one is provided.
	 * 
	 * @param u
	 *            <code>URL</code> of the URL request
	 * @param method
	 *            http/https
	 * @param res
	 *            sample result to save request infos to
	 * @return <code>HttpConnection</code> ready for .connect
	 * @exception IOException
	 *                if an I/O Exception occurs
	 */
	private HttpConnection setupConnection(URL u, String method, HTTPSampleResult res) throws IOException {

		String urlStr = u.toString();

		org.apache.commons.httpclient.URI uri = new org.apache.commons.httpclient.URI(urlStr);

		String schema = uri.getScheme();
		if ((schema == null) || (schema.length()==0)) {
			schema = PROTOCOL_HTTP;
		}
		Protocol protocol = Protocol.getProtocol(schema);

		String host = uri.getHost();
		int port = uri.getPort();

		HostConfiguration hc = new HostConfiguration();
		hc.setHost(host, port, protocol); // All needed to ensure re-usablility

		HttpConnection httpConn = null;
		Map map = (Map)httpClients.get();
		synchronized ( map ) // TODO: does this need to be synchronized?
		{
			httpConn = (HttpConnection)map.get(hc);
			
			if ( httpConn == null )
			{
				httpConn = new HttpConnection(hc);
				// TODO check these
				httpConn.setProxyHost(System.getProperty("http.proxyHost")); // $NON-NLS-1$
				httpConn.setProxyPort(Integer.parseInt(System.getProperty("http.proxyPort", "80"))); // $NON-NLS-1$
				
				map.put(hc, httpConn);
			}
		}

		if (method.equals(POST)) {
			httpMethod = new PostMethod(urlStr);
		} else {
			httpMethod = new GetMethod(urlStr);
			// httpMethod;
			new DefaultMethodRetryHandler();//TODO what is this doing??
		}

		httpMethod.setHttp11(!JMeterUtils.getPropDefault("httpclient.version", "1.1").equals("1.0")); // $NON-NLS-1$ // $NON-NLS-2$ // $NON-NLS-3$

		// Set the timeout (if non-zero)
		httpConn.setSoTimeout(JMeterUtils.getPropDefault("httpclient.timeout", 0)); // $NON-NLS-1$

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
		if (getUseKeepAlive()) {
			httpMethod.setRequestHeader(HEADER_CONNECTION, KEEP_ALIVE);
		} else {
			httpMethod.setRequestHeader(HEADER_CONNECTION, CONNECTION_CLOSE);
		}

		String hdrs = setConnectionHeaders(httpMethod, u, getHeaderManager());
		String cookies = setConnectionCookie(httpMethod, u, getCookieManager());

		if (res != null) {
            res.setURL(u);
            res.setHTTPMethod(method);
            res.setRequestHeaders(hdrs);
            res.setCookies(cookies);
            if (method.equals(POST)) {
                res.setQueryString(getQueryString());
            }
		}

		setConnectionAuthorization(httpMethod, u, getAuthManager());

		if (method.equals(POST)) {
			setPostHeaders((PostMethod) httpMethod);
		}

//		System.out.println("Dumping Request Headers:");
//		System.out.println(method.getRequestHeaders().toString());
//		Header[] headers = method.getRequestHeaders();
//		for (int i = 0; i < headers.length; i++)
//		{
//			System.out.println("Header["+i+"]:");
//			org.apache.commons.httpclient.HeaderElement[] elements = headers[i].getElements();
//			
//			for (j=0; j<elements.length; j++)
//			{
//				System.out.println("Element["+j+"]:");
//				org.apache.commons.httpclient.NameValuePair[] pairs = elements[j].getParameters();
//				
//				for (k=0; k<pairs.length;k++)
//				{
//					System.out.println("pair["+k+"]: " + pairs[k].getName() + "=" + pairs[k].getValue());
//				}
//			}
//		}

		return httpConn;
	}

	/**
	 * Gets the ResponseHeaders
	 * 
	 * @param method
	 *            connection from which the headers are read
	 * @return string containing the headers, one per line
	 */
	protected String getResponseHeaders(HttpMethod method) throws IOException {
		StringBuffer headerBuf = new StringBuffer();
		org.apache.commons.httpclient.Header rh[] = method.getResponseHeaders();
		headerBuf.append(method.getStatusLine());// header[0] is not the
													// status line...
		headerBuf.append("\n"); // $NON-NLS-1$

		for (int i = 0; i < rh.length; i++) {
			String key = rh[i].getName();
			if (!key.equalsIgnoreCase(TRANSFER_ENCODING))
                // TODO - why is this not saved?
			{
				headerBuf.append(key);
				headerBuf.append(": "); // $NON-NLS-1$
				headerBuf.append(rh[i].getValue());
				headerBuf.append("\n"); // $NON-NLS-1$
			}
		}
		return headerBuf.toString();
	}

	/**
	 * Extracts all the required cookies for that particular URL request and
	 * sets them in the <code>HttpMethod</code> passed in.
	 * 
	 * @param method
	 *            <code>HttpMethod</code> which represents the request
	 * @param u
	 *            <code>URL</code> of the request
	 * @param cookieManager
	 *            the <code>CookieManager</code> containing all the cookies
	 *            for this <code>UrlConfig</code>
	 */
	private String setConnectionCookie(HttpMethod method, URL u, CookieManager cookieManager) {
        // TODO recode to use HTTPClient matches methods or similar
        
		StringBuffer cookieHeader = new StringBuffer(100);
        if (cookieManager!=null){
    		String host = "." + u.getHost(); // $NON-NLS-1$
    		
    		for (int i = cookieManager.getCookies().size() - 1; i >= 0; i--) {
    			Cookie cookie = (Cookie) cookieManager.getCookies().get(i).getObjectValue();
                if (cookie == null)
                    continue;
                long exp = cookie.getExpires();
                long now = System.currentTimeMillis() / 1000 ;
    			if ( host.endsWith(cookie.getDomain())
                        && u.getFile().startsWith(cookie.getPath()) 
                        && (exp == 0 || exp > now)) {
    				org.apache.commons.httpclient.Cookie newCookie
                    = new org.apache.commons.httpclient.Cookie(cookie.getDomain(), cookie.getName(),
    				     cookie.getValue(), cookie.getPath(), null, false);
    				httpState.addCookie(newCookie);
    				cookieHeader.append(cookie.getName());
                    cookieHeader.append("="); // $NON-NLS-1$
                    cookieHeader.append(cookie.getValue());
    			}
    		}
        }
		return cookieHeader.toString();
	}

	/**
	 * Extracts all the required headers for that particular URL request and
	 * sets them in the <code>HttpMethod</code> passed in
	 * 
	 * @param method
	 *            <code>HttpMethod</code> which represents the request
	 * @param u
	 *            <code>URL</code> of the URL request
	 * @param headerManager
	 *            the <code>HeaderManager</code> containing all the cookies
	 *            for this <code>UrlConfig</code>
	 * @return the headers as a string
	 */
	private String setConnectionHeaders(HttpMethod method, URL u, HeaderManager headerManager) {
		StringBuffer hdrs = new StringBuffer(100);
		if (headerManager != null) {
			CollectionProperty headers = headerManager.getHeaders();
			if (headers != null) {
				PropertyIterator i = headers.iterator();
				while (i.hasNext()) {
					org.apache.jmeter.protocol.http.control.Header header 
                    = (org.apache.jmeter.protocol.http.control.Header) 
                       i.next().getObjectValue();
					String n = header.getName();
					String v = header.getValue();
					method.addRequestHeader(n, v);
					hdrs.append(n);
					hdrs.append(": "); // $NON-NLS-1$
					hdrs.append(v);
					hdrs.append("\n"); // $NON-NLS-1$
				}
			}
		}
		return hdrs.toString();
	}

	/**
	 * Extracts all the required authorization for that particular URL request
	 * and sets it in the <code>HttpMethod</code> passed in.
	 * 
	 * @param method
	 *            <code>HttpMethod</code> which represents the request
	 * @param u
	 *            <code>URL</code> of the URL request
	 * @param authManager
	 *            the <code>AuthManager</code> containing all the cookies for
	 *            this <code>UrlConfig</code>
	 */
	private void setConnectionAuthorization(HttpMethod method, URL u, AuthManager authManager) {
		if (authManager != null) {
            Authorization auth = authManager.getAuthForURL(u);
            if (auth != null) {
    			if (basicAuth) {
    					method.setRequestHeader(HEADER_AUTHORIZATION, auth.toBasicHeader());
    			} else {
                    /*
                     * TODO: better method...
                     * HACK: if user contains \ and or @
                     * then assume it is of the form:
                     * domain \ user @ realm (without spaces)
                     */
                    String user = auth.getUser();
                    String realm=null;
                    String domain=null;
                    String username;
                    int bs=user.indexOf('\\'); // $NON-NLS-1$
                    int at=user.indexOf('@'); // $NON-NLS-1$
                    if (bs > 0) {
                        domain=user.substring(0,bs);
                    }
                    if (at > 0 && at > bs+1) {
                        realm=user.substring(at+1);
                    } else {
                        at = user.length();
                    }
                    username=user.substring(bs+1,at);
                    if (log.isDebugEnabled()){
                        log.debug(user + " >  D="+ username + " D="+domain+" R="+realm);
                    }
					httpState.setCredentials(
                            realm,
							auth.getURL(),
                            // NT Includes other types of Credentials
                            new NTCredentials(
									username, 
                                    auth.getPass(), 
                                    null, // "thishost",
									domain
							));
				}
			}
		}
	}

	/**
	 * Samples the URL passed in and stores the result in
	 * <code>HTTPSampleResult</code>, following redirects and downloading
	 * page resources as appropriate.
	 * <p>
	 * When getting a redirect target, redirects are not followed and resources
	 * are not downloaded. The caller will take care of this.
	 * 
	 * @param url
	 *            URL to sample
	 * @param method
	 *            HTTP method: GET, POST,...
	 * @param areFollowingRedirect
	 *            whether we're getting a redirect target
	 * @param frameDepth
	 *            Depth of this target in the frame structure. Used only to
	 *            prevent infinite recursion.
	 * @return results of the sampling
	 */
	protected HTTPSampleResult sample(URL url, String method, boolean areFollowingRedirect, int frameDepth) {

		String urlStr = url.toString();

		log.debug("Start : sample" + urlStr);
		log.debug("method" + method);

		httpMethod = null;

		HTTPSampleResult res = new HTTPSampleResult();
		if (this.getPropertyAsBoolean(MONITOR)) {
			res.setMonitor(true);
		} else {
			res.setMonitor(false);
		}
		res.setSampleLabel(urlStr);
		res.sampleStart(); // Count the retries as well in the time

		try {
			HttpConnection connection = setupConnection(url, method, res);

			if (method.equals(POST)) {
				sendPostData(httpMethod);
			}

			int statusCode = httpMethod.execute(httpState, connection);

			// Request sent. Now get the response:
            InputStream instream = httpMethod.getResponseBodyAsStream();
            
            //int contentLength = httpMethod.getResponseContentLength();Not visible ...
            //TODO size ouststream according to actual content length
            ByteArrayOutputStream outstream = new ByteArrayOutputStream(4*1024);
                    //contentLength > 0 ? contentLength : DEFAULT_INITIAL_BUFFER_SIZE);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = instream.read(buffer)) > 0) {
                outstream.write(buffer, 0, len);
            }
            outstream.close();
            
			byte[] responseData = outstream.toByteArray();

			res.sampleEnd();
			// Done with the sampling proper.

			// Now collect the results into the HTTPSampleResult:

			res.setSampleLabel(httpMethod.getURI().toString());
            // Pick up Actual path (after redirects)
            
			res.setResponseData(responseData);

			res.setResponseCode(Integer.toString(statusCode));
			res.setSuccessful(isSuccessCode(statusCode));

			res.setResponseMessage(httpMethod.getStatusText());

			String ct = null;
			org.apache.commons.httpclient.Header h 
                = httpMethod.getResponseHeader(HEADER_CONTENT_TYPE); // $NON-NLS-1$
			if (h != null)// Can be missing, e.g. on redirect
			{
				ct = h.getValue();
				res.setContentType(ct);// e.g. text/html; charset=ISO-8859-1
                res.setEncodingAndType(ct);
			}

			res.setResponseHeaders(getResponseHeaders(httpMethod));
			if (res.isRedirect()) {
				res.setRedirectLocation(httpMethod.getResponseHeader("Location").getValue()); // $NON-NLS-1$
			}

			// Store any cookies received in the cookie manager:
			saveConnectionCookies(httpState, getCookieManager());

			// Follow redirects and download page resources if appropriate:
			res = resultProcessing(areFollowingRedirect, frameDepth, res);

			log.debug("End : sample");
			if (httpMethod != null)
				httpMethod.releaseConnection();
			return res;
		} catch (IllegalArgumentException e)// e.g. some kinds of invalid URL
		{
			res.sampleEnd();
			HTTPSampleResult err = errorResult(e, res);
			err.setSampleLabel("Error: " + url.toString());
			return err;
		} catch (IOException e) {
			res.sampleEnd();
			HTTPSampleResult err = errorResult(e, res);
			err.setSampleLabel("Error: " + url.toString());
			return err;
		} finally {
			if (httpMethod != null)
				httpMethod.releaseConnection();
		}
	}

	/**
	 * From the <code>HttpState</code>, store all the "set-cookie" key-pair
	 * values in the cookieManager of the <code>UrlConfig</code>.
	 * 
	 * @param state
	 *            <code>HttpState</code> which represents the request
	 * @param u
	 *            <code>URL</code> of the URL request
	 * @param cookieManager
	 *            the <code>CookieManager</code> containing all the cookies
	 *            for this <code>UrlConfig</code>
	 */
	private void saveConnectionCookies(HttpState state, CookieManager cookieManager) {
		if (cookieManager != null) {
			org.apache.commons.httpclient.Cookie [] c = state.getCookies();
			for (int i = 0; i < c.length; i++) {
				Date exp = c[i].getExpiryDate();// might be absent
				//System.out.println("Cookie[" + i + "]: " + c[i].getName() + " := " + c[i].getValue());

				Cookie cookie = new Cookie(c[i].getName(), 
					c[i].getValue(), c[i].getDomain(), c[i].getPath(), c[i].getSecure(), exp == null ? 0 : exp.getTime() / 1000);
				
				removeExistingCookie( cookie, cookieManager);
				
				cookieManager.add( cookie );
			}
		}
	}
	
	private void removeExistingCookie( Cookie newCookie, CookieManager cookieManager )
	{
		Vector removeIndices = new Vector();
		for (int i = cookieManager.getCookies().size() - 1; i >= 0; i--) {
			Cookie cookie = (Cookie) cookieManager.getCookies().get(i).getObjectValue();
			if (cookie == null)
				continue;
			if (cookie.getPath().equals(newCookie.getPath()) && cookie.getDomain().equals(newCookie.getDomain())
					&& cookie.getName().equals(newCookie.getName())) {
				if (log.isDebugEnabled()) {
					//System.out.println("New Cookie = " + newCookie.toString() + " removing matching Cookie "
									//+ cookie.toString());
				}
				removeIndices.addElement(new Integer(i));
			}
		}

		int index = 0;
		for (Enumeration e = removeIndices.elements(); e.hasMoreElements();) {
			index = ((Integer) e.nextElement()).intValue();
			cookieManager.remove(index);
		}
	}

	public void threadStarted() {
		log.debug("Thread Started");
		
		synchronized ( this.getClass() )
		{
			if ( httpClients == null ) {
				httpClients = new ThreadLocal();
			}
			httpClients.set ( new HashMap() );
		}
	}

	public void threadFinished() {
		log.debug("Thread Finished");
		//if (httpConn != null)
		//	httpConn.close();
		
		synchronized ( this.getClass() )
		{
			Map map = (Map)httpClients.get();

			HttpConnection conn = null;
			if ( map != null ) {
				for ( Iterator it = map.entrySet().iterator(); it.hasNext(); )
				{
					Object obj = it.next();
					conn = (HttpConnection) ((Map.Entry)obj).getValue();
					conn.close();
				}
				map.clear();
			}
			
			httpClients.set( null );
		}
	}

}
