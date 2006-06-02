/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.NTCredentials;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.TraceMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.SlowHttpClientSocketFactory;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * A sampler which understands all the parts necessary to read statistics about
 * HTTP requests, including cookies and authentication.
 * 
 */
public class HTTPSampler2 extends HTTPSamplerBase {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String PROXY_HOST = 
        System.getProperty("http.proxyHost",""); // $NON-NLS-1$ 

    private static final String NONPROXY_HOSTS = 
        System.getProperty("http.nonProxyHosts",""); // $NON-NLS-1$ 

    private static final int PROXY_PORT = 
        Integer.parseInt(System.getProperty("http.proxyPort", DEFAULT_HTTP_PORT_STRING)); // $NON-NLS-1$ 

    private static final String PROXY_USER = 
        JMeterUtils.getPropDefault(JMeter.HTTP_PROXY_USER,""); // $NON-NLS-1$
    
    private static final String PROXY_PASS = 
        JMeterUtils.getPropDefault(JMeter.HTTP_PROXY_PASS,""); // $NON-NLS-1$
    
    private static InetAddress localAddress = null;
    
    /*
     * Connection is re-used within the thread if possible
     */
	private static final ThreadLocal httpClients = new ThreadLocal();

    private static Set nonProxyHostFull   = new HashSet();// www.apache.org
    private static List nonProxyHostSuffix = new ArrayList();// .apache.org
    private static final int nonProxyHostSuffixSize;

    private static boolean isNonProxy(String host){
        return nonProxyHostFull.contains(host) || isPartialMatch(host);
    }

    private static boolean isPartialMatch(String host) {    
        for (int i=0;i<nonProxyHostSuffixSize;i++){
            if (host.endsWith((String)nonProxyHostSuffix.get(i)))
                return true;
        }
        return false;
    }

	static {
        if (NONPROXY_HOSTS.length() > 0){
            StringTokenizer s = new StringTokenizer(NONPROXY_HOSTS,"|");// $NON-NLS-1$
            while (s.hasMoreTokens()){
                String t = s.nextToken();
                if (t.indexOf("*") ==0){// e.g. *.apache.org // $NON-NLS-1$
                    nonProxyHostSuffix.add(t.substring(1));
                } else {
                    nonProxyHostFull.add(t);// e.g. www.apache.org
                }
            }
        }
        nonProxyHostSuffixSize=nonProxyHostSuffix.size();

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

        String localHostOrIP = 
            JMeterUtils.getPropDefault("httpclient.localaddress",""); // $NON-NLS-1$
        if (localHostOrIP.length() > 0){
            try {
                localAddress = InetAddress.getByName(localHostOrIP);
                log.info("Using localAddress "+localAddress.getHostAddress());
            } catch (UnknownHostException e) {
                log.warn(e.getLocalizedMessage());
            }
        }
	}

	/**
	 * Constructor for the HTTPSampler2 object.
     * 
     * Consider using HTTPSamplerFactory.newInstance() instead
	 */
	public HTTPSampler2() {
	}

	/**
	 * Send POST data from <code>Entry</code> to the open connection.
	 * 
	 * @param connection
	 *            <code>URLConnection</code> where POST data should be sent
	 * @exception IOException
	 *                if an I/O exception occurs
	 */
	private void sendPostData(PostMethod post) throws IOException {
		// If filename was specified then send the post using multipart syntax
		String filename = getFilename();
		if ((filename != null) && (filename.trim().length() > 0)) {
            int argc = getArguments().getArgumentCount();
            Part[] parts = new Part[argc+1]; 
            PropertyIterator args = getArguments().iterator();
            int i = 0;
            while (args.hasNext()) {
                Argument arg = (Argument) args.next().getObjectValue();
                parts[i++] = new StringPart(arg.getName(), arg.getValue());
            }
            File input = new File(filename);
                    //TODO should allow charset to be defined ...
            parts[i]= new FilePart(getFileField(), input, getMimetype(), "UTF-8" );//$NON-NLS-1$
            post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
		} else {
            PropertyIterator args = getArguments().iterator();
            while (args.hasNext()) {
                Argument arg = (Argument) args.next().getObjectValue();
                post.addParameter(arg.getName(), arg.getValue());
            }
        }
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
	private HttpClient setupConnection(URL u, HttpMethodBase httpMethod, HTTPSampleResult res) throws IOException {

		String urlStr = u.toString();

		org.apache.commons.httpclient.URI uri = new org.apache.commons.httpclient.URI(urlStr,false);

		String schema = uri.getScheme();
		if ((schema == null) || (schema.length()==0)) {
			schema = PROTOCOL_HTTP;
		}
		Protocol protocol = Protocol.getProtocol(schema);

		String host = uri.getHost();
		int port = uri.getPort();

		HostConfiguration hc = new HostConfiguration();
		hc.setHost(host, port, protocol); // All needed to ensure re-usablility

        // Set up the local address if one exists
        if (localAddress != null){
            hc.setLocalAddress(localAddress);
        }
        
        Map map = (Map) httpClients.get();
		HttpClient httpClient = (HttpClient) map.get(hc);
		
		if ( httpClient == null )
		{
			httpClient = new HttpClient(new SimpleHttpConnectionManager());
			map.put(hc, httpClient);
		}

        if (PROXY_HOST.length() > 0 && !isNonProxy(host)) {
            if (log.isDebugEnabled()){
                log.debug("Setting proxy: "+PROXY_HOST+":"+PROXY_PORT);
            }
            hc.setProxy(PROXY_HOST, PROXY_PORT);
            if (PROXY_USER.length() > 0){
                httpClient.getState().setProxyCredentials(
                    new AuthScope(PROXY_HOST,PROXY_PORT,null,AuthScope.ANY_SCHEME),
                    // NT Includes other types of Credentials
                    new NTCredentials(
                            PROXY_USER, 
                            PROXY_PASS, 
                            null, // "thishost",
                            "" // domain
                ));
            }
        }

        HttpMethodParams params = httpMethod.getParams();
        params.setCookiePolicy(CookiePolicy.IGNORE_COOKIES);// We do our own cookie handling
        params.setVersion(
                JMeterUtils.getPropDefault("httpclient.version", "1.1").equals("1.0") // $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$ 
                ?
                HttpVersion.HTTP_1_0 : HttpVersion.HTTP_1_1);

		// Set the timeout (if non-zero)
		params.setSoTimeout(JMeterUtils.getPropDefault("httpclient.timeout", 0)); // $NON-NLS-1$

		//httpState = new HttpState();
//		if (httpConn.isProxied() && httpConn.isSecure()) {
//			httpMethod = new ConnectMethod(httpMethod);
//		}

		// Allow HttpClient to handle the redirects:
		httpMethod.setFollowRedirects(getAutoRedirects());

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
            res.setRequestHeaders(hdrs);
            res.setCookies(cookies);
		}

		setConnectionAuthorization(httpClient, u, getAuthManager());

		return httpClient;
	}

	/**
	 * Gets the ResponseHeaders
	 * 
	 * @param method
	 *            connection from which the headers are read
	 * @return string containing the headers, one per line
	 */
	protected String getResponseHeaders(HttpMethod method) {
		StringBuffer headerBuf = new StringBuffer();
		org.apache.commons.httpclient.Header rh[] = method.getResponseHeaders();
		headerBuf.append(method.getStatusLine());// header[0] is not the status line...
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
	 * @param method <code>HttpMethod</code> for the request
	 * @param u <code>URL</code> of the request
	 * @param cookieManager the <code>CookieManager</code> containing all the cookies
     * @return a String containing the cookie details (for the response)
     * May be null
	 */
	private String setConnectionCookie(HttpMethod method, URL u, CookieManager cookieManager) {        
        String cookieHeader = null;
        if (cookieManager != null) {
            cookieHeader = cookieManager.getCookieHeaderForURL(u);
            if (cookieHeader != null) {
                method.setRequestHeader(HEADER_COOKIE, cookieHeader);
            }
        }
		return cookieHeader;
	}

	/**
	 * Extracts all the required non-cookie headers for that particular URL request and
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
	 *            the <code>AuthManager</code> containing all the authorisations for
	 *            this <code>UrlConfig</code>
	 */
	private void setConnectionAuthorization(HttpClient client, URL u, AuthManager authManager) {
		if (authManager != null) {
            Authorization auth = authManager.getAuthForURL(u);
            if (auth != null) {
                    /*
                     * TODO: better method...
                     * HACK: if user contains \ and or @
                     * then assume it is of the form:
                     * domain \ user @ realm (without spaces)
                     */
                    String user = auth.getUser();
                    String realm=null;
                    String domain="";// $NON-NLS-1$
                    String username="";// $NON-NLS-1$
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
					client.getState().setCredentials(
                            new AuthScope(u.getHost(),u.getPort(),realm,AuthScope.ANY_SCHEME),
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

        HttpMethodBase httpMethod = null;

        if (method.equals(POST)) {
            httpMethod = new PostMethod(urlStr);
        } else if (method.equals(PUT)){
            httpMethod = new PutMethod(urlStr);
        } else if (method.equals(HEAD)){
            httpMethod = new HeadMethod(urlStr);
        } else if (method.equals(TRACE)){
            httpMethod = new TraceMethod(urlStr);
        } else if (method.equals(OPTIONS)){
            httpMethod = new OptionsMethod(urlStr);
        } else if (method.equals(DELETE)){
            httpMethod = new DeleteMethod(urlStr);
        } else {
            httpMethod = new GetMethod(urlStr);
        }

		HTTPSampleResult res = new HTTPSampleResult();
		res.setMonitor(isMonitor());
        
		res.setSampleLabel(urlStr); // May be replaced later
        res.setHTTPMethod(method);
		res.sampleStart(); // Count the retries as well in the time
        HttpClient client = null;
        InputStream instream = null;
		try {
			client = setupConnection(url, httpMethod, res);

			if (method.equals(POST)) {
                res.setQueryString(getQueryString());
				sendPostData((PostMethod)httpMethod);
			}else if (method.equals(PUT)) {
                setPutHeaders((PutMethod) httpMethod);
            }

			int statusCode = client.executeMethod(httpMethod);

			// Request sent. Now get the response:
            instream = httpMethod.getResponseBodyAsStream();
            
            if (instream != null) {// will be null for HEAD
            
                Header responseHeader = httpMethod.getResponseHeader(TRANSFER_ENCODING);
                if (responseHeader!= null && ENCODING_GZIP.equals(responseHeader.getValue())) {
                    instream = new GZIPInputStream(instream);
                }
    
                //int contentLength = httpMethod.getResponseContentLength();Not visible ...
                //TODO size ouststream according to actual content length
                ByteArrayOutputStream outstream = new ByteArrayOutputStream(4*1024);
                        //contentLength > 0 ? contentLength : DEFAULT_INITIAL_BUFFER_SIZE);
                byte[] buffer = new byte[4096];
                int len;
                boolean first = true;// first response
                while ((len = instream.read(buffer)) > 0) {
                    if (first) { // save the latency
                        res.latencyEnd();
                        first = false;
                    }
                    outstream.write(buffer, 0, len);
                }
    
                res.setResponseData(outstream.toByteArray());
                outstream.close();            

            }
            
			res.sampleEnd();
			// Done with the sampling proper.

			// Now collect the results into the HTTPSampleResult:

			res.setSampleLabel(httpMethod.getURI().toString());
            // Pick up Actual path (after redirects)
            
			res.setResponseCode(Integer.toString(statusCode));
			res.setSuccessful(isSuccessCode(statusCode));

			res.setResponseMessage(httpMethod.getStatusText());

			String ct = null;
			org.apache.commons.httpclient.Header h 
                = httpMethod.getResponseHeader(HEADER_CONTENT_TYPE);
			if (h != null)// Can be missing, e.g. on redirect
			{
				ct = h.getValue();
				res.setContentType(ct);// e.g. text/html; charset=ISO-8859-1
                res.setEncodingAndType(ct);
			}

			res.setResponseHeaders(getResponseHeaders(httpMethod));
			if (res.isRedirect()) {
				res.setRedirectLocation(httpMethod.getResponseHeader(HEADER_LOCATION).getValue());
			}

            // If we redirected automatically, the URL may have changed
            if (getAutoRedirects()){
                res.setURL(new URL(httpMethod.getURI().toString()));
            }
            
			// Store any cookies received in the cookie manager:
			saveConnectionCookies(client, getCookieManager());

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
            JOrphanUtils.closeQuietly(instream);
			if (httpMethod != null)
				httpMethod.releaseConnection();
		}
	}

    /**
     * Set up the PUT data (if present)
     */
	private void setPutHeaders(PutMethod put) 
         throws IOException
     {
         String filename = getFilename();
         if ((filename != null) && (filename.trim().length() > 0))
         {
             RequestEntity requestEntity = new InputStreamRequestEntity(
                     new FileInputStream(filename),getMimetype());
             put.setRequestEntity(requestEntity);
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
	private void saveConnectionCookies(HttpClient client, CookieManager cookieManager) {
		if (cookieManager != null) {
			org.apache.commons.httpclient.Cookie [] c = client.getState().getCookies();
			for (int i = 0; i < c.length; i++) {
				Date exp = c[i].getExpiryDate();// might be absent
				Cookie cookie = new Cookie(c[i].getName(), 
					c[i].getValue(), c[i].getDomain(), c[i].getPath(), c[i].getSecure(), exp == null ? 0 : exp.getTime() / 1000);
				
				cookieManager.add( cookie );
                if (log.isDebugEnabled()){
                    log.debug("Saved Cookie: " + cookie.toString());
                }
			}
		}
	}
	

	public void threadStarted() {
		log.debug("Thread Started");
        
		// Does not need to be synchronised, as all access is from same thread
        httpClients.set ( new HashMap() );	
    }

	public void threadFinished() {
		log.debug("Thread Finished");

        // Does not need to be synchronised, as all access is from same thread
		Map map = (Map)httpClients.get();

		if ( map != null ) {
			for ( Iterator it = map.entrySet().iterator(); it.hasNext(); )
			{
				Map.Entry entry = (Map.Entry) it.next();
				HttpClient cl = (HttpClient) entry.getValue();
                cl.getHttpConnectionManager().closeIdleConnections(-1000);// Closes the connection
			}
			map.clear();
		}
	}
}
