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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import java.util.zip.GZIPInputStream;

import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;

import org.apache.jmeter.samplers.SampleResult;
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
 */
public class HTTPSampler extends HTTPSamplerBase {
    private static final Logger log = LoggingManager.getLoggerForClass();

	private static final int MAX_CONN_RETRIES = 10; // Maximum connection retries

	static {// TODO - document what this is doing and why
		System.setProperty("java.protocol.handler.pkgs", // $NON-NLS-1$ 
                JMeterUtils.getPropDefault("ssl.pkgs", // $NON-NLS-1$
				"com.sun.net.ssl.internal.www.protocol")); // $NON-NLS-1$
	}

	/**
	 * Constructor for the HTTPSampler object.
     * 
     * Consider using HTTPSamplerFactory.newInstance() instead
	 */
	public HTTPSampler() {
	}

	/**
	 * Set request headers in preparation to opening a connection.
	 * 
	 * @param conn
	 *            <code>URLConnection</code> to set headers on
	 * @exception IOException
	 *                if an I/O exception occurs
	 */
	protected void setPostHeaders(URLConnection conn) throws IOException {
		PostWriter.setHeaders(conn, this);
	}

    private void setPutHeaders(URLConnection conn)
     {
         String filename = getFilename();
         if ((filename != null) && (filename.trim().length() > 0))
         {
             conn.setRequestProperty(HEADER_CONTENT_TYPE, getMimetype());
             conn.setDoOutput(true);
             conn.setDoInput(true);
        }
    }

	/**
	 * Send POST data from <code>Entry</code> to the open connection.
	 * 
	 * @param connection
	 *            <code>URLConnection</code> where POST data should be sent
	 * @exception IOException
	 *                if an I/O exception occurs
	 */
	protected void sendPostData(URLConnection connection) throws IOException {
		PostWriter.sendPostData(connection, this);
	}

    private void sendPutData(URLConnection conn) throws IOException {
        String filename = getFilename();
        if ((filename != null) && (filename.trim().length() > 0)) {
            OutputStream out = conn.getOutputStream();
            byte[] buf = new byte[1024];
            int read;
            InputStream in = new BufferedInputStream(new FileInputStream(filename));
            while ((read = in.read(buf)) > 0) {
                out.write(buf, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        }
    }


	/**
	 * Returns an <code>HttpURLConnection</code> fully ready to attempt
	 * connection. This means it sets the request method (GET or POST), headers,
	 * cookies, and authorization for the URL request.
	 * <p>
	 * The request infos are saved into the sample result if one is provided.
	 * 
	 * @param u
	 *            <code>URL</code> of the URL request
	 * @param method
	 *            GET, POST etc
	 * @param res
	 *            sample result to save request infos to
	 * @return <code>HttpURLConnection</code> ready for .connect
	 * @exception IOException
	 *                if an I/O Exception occurs
	 */
	protected HttpURLConnection setupConnection(URL u, String method, HTTPSampleResult res) throws IOException {
		HttpURLConnection conn;

        SSLManager sslmgr = null;
        if (PROTOCOL_HTTPS.equalsIgnoreCase(u.getProtocol())) {
            try {
                sslmgr=SSLManager.getInstance();
            } catch (Exception e) {
                log.warn("You may have forgotten to set the ssl.provider property " + "in jmeter.properties", e);
            }
        }
		
        conn = (HttpURLConnection) u.openConnection();
        // Update follow redirects setting just for this connection
        conn.setInstanceFollowRedirects(getAutoRedirects());

        if (PROTOCOL_HTTPS.equalsIgnoreCase(u.getProtocol())) {
			try {
				sslmgr.setContext(conn);
			} catch (Exception e) {
				log.warn("You may have forgotten to set the ssl.provider property " + "in jmeter.properties", e);
			}
		}

		// a well-bahaved browser is supposed to send 'Connection: close'
		// with the last request to an HTTP server. Instead, most browsers
		// leave it to the server to close the connection after their
		// timeout period. Leave it to the JMeter user to decide.
		if (getUseKeepAlive()) {
			conn.setRequestProperty(HEADER_CONNECTION, KEEP_ALIVE);
		} else {
			conn.setRequestProperty(HEADER_CONNECTION, CONNECTION_CLOSE);
		}

		conn.setRequestMethod(method);
		String hdrs = setConnectionHeaders(conn, u, getHeaderManager());
		String cookies = setConnectionCookie(conn, u, getCookieManager());

        if (res != null) {
            res.setURL(u);
            res.setHTTPMethod(method);
            res.setRequestHeaders(hdrs);
            res.setCookies(cookies);
            if (method.equals(POST)) {
                res.setQueryString(getQueryString());
            }
        }

		setConnectionAuthorization(conn, u, getAuthManager());
		if (method.equals(POST)) {
			setPostHeaders(conn);
		} else if (method.equals(PUT)) {
            setPutHeaders(conn);
        }
		return conn;
	}

	/**
	 * Reads the response from the URL connection.
	 * 
	 * @param conn
	 *            URL from which to read response
	 * @return response content
	 * @exception IOException
	 *                if an I/O exception occurs
	 */
	protected byte[] readResponse(HttpURLConnection conn, SampleResult res) throws IOException {
		byte[] readBuffer = getThreadContext().getReadBuffer();
		BufferedInputStream in;
		try {
            // works OK even if ContentEncoding is null
			if (ENCODING_GZIP.equals(conn.getContentEncoding())) {
				in = new BufferedInputStream(new GZIPInputStream(conn.getInputStream()));
			} else {
				in = new BufferedInputStream(conn.getInputStream());
			}
		} catch (IOException e) {
			// TODO JDK1.4: if (!e.getCause() instanceof FileNotFoundException)
			// JDK1.4: {
            // TODO: what about other 4xx errors? Do we need to log them?
			if (conn.getResponseCode() != 404) // for JDK1.3
			{
				log.error("readResponse: "+e.toString());
				// JDK1.4: Throwable cause = e.getCause();
				// JDK1.4: if (cause != null){
				// JDK1.4: log.error("Cause: "+cause);
				// JDK1.4: }
			}
			// Normal InputStream is not available
			in = new BufferedInputStream(conn.getErrorStream());
		} catch (Exception e) {
			log.error("readResponse: "+e.toString());
			// JDK1.4: Throwable cause = e.getCause();
			// JDK1.4: if (cause != null){
			// JDK1.4: log.error("Cause: "+cause);
			// JDK1.4: }
			in = new BufferedInputStream(conn.getErrorStream());
		}
		java.io.ByteArrayOutputStream w = new ByteArrayOutputStream();
		int x = 0;
		boolean first = true;
		while ((x = in.read(readBuffer)) > -1) {
			if (first) {
				res.latencyEnd();
				first = false;
			}
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
	 * @param conn
	 *            connection from which the headers are read
	 * @return string containing the headers, one per line
	 */
	protected String getResponseHeaders(HttpURLConnection conn) {
		StringBuffer headerBuf = new StringBuffer();
		headerBuf.append(conn.getHeaderField(0));// Leave header as is
		// headerBuf.append(conn.getHeaderField(0).substring(0, 8));
		// headerBuf.append(" ");
		// headerBuf.append(conn.getResponseCode());
		// headerBuf.append(" ");
		// headerBuf.append(conn.getResponseMessage());
		headerBuf.append("\n"); //$NON-NLS-1$

        String hfk;
		for (int i = 1; (hfk=conn.getHeaderFieldKey(i)) != null; i++) {
            // TODO - why is this not saved? A: it might be a proxy server specific field.
            // If JMeter is using a proxy, the browser wouldn't know about that.
            if (!TRANSFER_ENCODING.equalsIgnoreCase(hfk)) {
                headerBuf.append(hfk);
                headerBuf.append(": "); // $NON-NLS-1$
                headerBuf.append(conn.getHeaderField(i));
                headerBuf.append("\n"); // $NON-NLS-1$
            }
		}
		return headerBuf.toString();
	}

	/**
	 * Extracts all the required cookies for that particular URL request and
	 * sets them in the <code>HttpURLConnection</code> passed in.
	 * 
	 * @param conn
	 *            <code>HttpUrlConnection</code> which represents the URL
	 *            request
	 * @param u
	 *            <code>URL</code> of the URL request
	 * @param cookieManager
	 *            the <code>CookieManager</code> containing all the cookies
	 *            for this <code>UrlConfig</code>
	 */
	private String setConnectionCookie(HttpURLConnection conn, URL u, CookieManager cookieManager) {
		String cookieHeader = null;
		if (cookieManager != null) {
			cookieHeader = cookieManager.getCookieHeaderForURL(u);
			if (cookieHeader != null) {
				conn.setRequestProperty(HEADER_COOKIE, cookieHeader);
			}
		}
		return cookieHeader;
	}

	/**
	 * Extracts all the required headers for that particular URL request and
	 * sets them in the <code>HttpURLConnection</code> passed in
	 * 
	 * @param conn
	 *            <code>HttpUrlConnection</code> which represents the URL
	 *            request
	 * @param u
	 *            <code>URL</code> of the URL request
	 * @param headerManager
	 *            the <code>HeaderManager</code> containing all the cookies
	 *            for this <code>UrlConfig</code>
	 * @return the headers as a string
	 */
	private String setConnectionHeaders(HttpURLConnection conn, URL u, HeaderManager headerManager) {
		StringBuffer hdrs = new StringBuffer(100);
		if (headerManager != null) {
			CollectionProperty headers = headerManager.getHeaders();
			if (headers != null) {
				PropertyIterator i = headers.iterator();
				while (i.hasNext()) {
					Header header = (Header) i.next().getObjectValue();
					String n = header.getName();
					String v = header.getValue();
					conn.setRequestProperty(n, v);
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
	 * and sets it in the <code>HttpURLConnection</code> passed in.
	 * 
	 * @param conn
	 *            <code>HttpUrlConnection</code> which represents the URL
	 *            request
	 * @param u
	 *            <code>URL</code> of the URL request
	 * @param authManager
	 *            the <code>AuthManager</code> containing all the cookies for
	 *            this <code>UrlConfig</code>
	 */
	private void setConnectionAuthorization(HttpURLConnection conn, URL u, AuthManager authManager) {
		if (authManager != null) {
			Authorization auth = authManager.getAuthForURL(u);
			if (auth != null) {
				conn.setRequestProperty(HEADER_AUTHORIZATION, auth.toBasicHeader());
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
		HttpURLConnection conn = null;

		String urlStr = url.toString();
		log.debug("Start : sample" + urlStr);

		HTTPSampleResult res = new HTTPSampleResult();
		res.setMonitor(isMonitor());
        
		res.setSampleLabel(urlStr);
		res.sampleStart(); // Count the retries as well in the time
		try {
			// Sampling proper - establish the connection and read the response:
			// Repeatedly try to connect:
			int retry;
			for (retry = 1; retry <= MAX_CONN_RETRIES; retry++) {
				try {
					conn = setupConnection(url, method, res);
					// Attempt the connection:
					conn.connect();
					break;
				} catch (BindException e) {
					if (retry >= MAX_CONN_RETRIES) {
						log.error("Can't connect", e);
						throw e;
					}
					log.debug("Bind exception, try again");
					conn.disconnect();
					this.setUseKeepAlive(false);
					continue; // try again
				} catch (IOException e) {
					log.debug("Connection failed, giving up");
					throw e;
				}
			}
			if (retry > MAX_CONN_RETRIES) {
				// This should never happen, but...
				throw new BindException();
			}
			// Nice, we've got a connection. Finish sending the request:
			if (method.equals(POST)) {
				sendPostData(conn);
			} else if (method.equals(PUT)) {
                sendPutData(conn);
            }
			// Request sent. Now get the response:
			byte[] responseData = readResponse(conn, res);

			res.sampleEnd();
			// Done with the sampling proper.

			// Now collect the results into the HTTPSampleResult:

			res.setResponseData(responseData);

			int errorLevel = conn.getResponseCode();
            String respMsg = conn.getResponseMessage();
            if (errorLevel == -1){// Bug 38902 - sometimes -1 seems to be returned unnecessarily
                try {
                    errorLevel = Integer.parseInt(respMsg.substring(0, 3));
                    log.warn("ResponseCode==-1; parsed "+respMsg+ " as "+errorLevel);
                  } catch (NumberFormatException e) {
                    log.warn("ResponseCode==-1; could not parse "+respMsg);
                  }                
            }
			res.setResponseCode(Integer.toString(errorLevel));
			res.setSuccessful(isSuccessCode(errorLevel));

			res.setResponseMessage(respMsg);

			String ct = conn.getContentType();
			res.setContentType(ct);// e.g. text/html; charset=ISO-8859-1
            res.setEncodingAndType(ct);

			res.setResponseHeaders(getResponseHeaders(conn));
			if (res.isRedirect()) {
				res.setRedirectLocation(conn.getHeaderField(HEADER_LOCATION));
			}

            // If we redirected automatically, the URL may have changed
            if (getAutoRedirects()){
                res.setURL(conn.getURL());
            }
            
			// Store any cookies received in the cookie manager:
			saveConnectionCookies(conn, url, getCookieManager());

			res = resultProcessing(areFollowingRedirect, frameDepth, res);

			log.debug("End : sample");
			return res;
		} catch (IOException e) {
			res.sampleEnd();
			// We don't want to continue using this connection, even if KeepAlive is set
            conn.disconnect();
            conn=null; // Don't process again
			return errorResult(e, res);
		} finally {
			// calling disconnect doesn't close the connection immediately,
			// but indicates we're through with it. The JVM should close
			// it when necessary.
			disconnect(conn); // Disconnect unless using KeepAlive
		}
	}

	protected void disconnect(HttpURLConnection conn) {
		if (conn != null) {
			String connection = conn.getHeaderField(HEADER_CONNECTION);
			String protocol = conn.getHeaderField(0);
			if ((connection == null && (protocol == null || !protocol.startsWith(HTTP_1_1)))
					|| (connection != null && connection.equalsIgnoreCase(CONNECTION_CLOSE))) {
				conn.disconnect();
			}
		}
	}

	/**
	 * From the <code>HttpURLConnection</code>, store all the "set-cookie"
	 * key-pair values in the cookieManager of the <code>UrlConfig</code>.
	 * 
	 * @param conn
	 *            <code>HttpUrlConnection</code> which represents the URL
	 *            request
	 * @param u
	 *            <code>URL</code> of the URL request
	 * @param cookieManager
	 *            the <code>CookieManager</code> containing all the cookies
	 *            for this <code>UrlConfig</code>
	 */
	private void saveConnectionCookies(HttpURLConnection conn, URL u, CookieManager cookieManager) {
		if (cookieManager != null) {
			for (int i = 1; conn.getHeaderFieldKey(i) != null; i++) {
				if (conn.getHeaderFieldKey(i).equalsIgnoreCase(HEADER_SET_COOKIE)) {
					cookieManager.addCookieFromHeader(conn.getHeaderField(i), u);
				}
			}
		}
	}
}
