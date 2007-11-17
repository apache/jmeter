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

package org.apache.jmeter.protocol.http.proxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.httpclient.HttpConstants;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.parser.HTMLParseException;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

/**
 * Thread to handle one client request. Gets the request from the client and
 * passes it on to the server, then sends the response back to the client.
 * Information about the request and response is stored so it can be used in a
 * JMeter test plan.
 * 
 */
public class Proxy extends Thread {
	private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String NEW_LINE = "\n"; // $NON-NLS-1$

    private static final String[] headersToRemove;

    // Allow list of headers to be overridden
	private static final String PROXY_HEADERS_REMOVE = "proxy.headers.remove"; // $NON-NLS-1$

	private static final String PROXY_HEADERS_REMOVE_DEFAULT = "If-Modified-Since"; // $NON-NLS-1$

    private static final String PROXY_HEADERS_REMOVE_SEPARATOR = ","; // $NON-NLS-1$

    static {
    	String removeList = JMeterUtils.getPropDefault(PROXY_HEADERS_REMOVE,PROXY_HEADERS_REMOVE_DEFAULT);
    	headersToRemove = JOrphanUtils.split(removeList,PROXY_HEADERS_REMOVE_SEPARATOR);
    	log.info("Proxy will remove the headers: "+removeList);
    }

	/** Socket to client. */
	private Socket clientSocket = null;

	/** Target to receive the generated sampler. */
	private ProxyControl target;

	/** Whether or not to capture the HTTP headers. */
	private boolean captureHttpHeaders;

	/** Whether to try to spoof as https **/
	private boolean httpsSpoof;

	private String httpsSpoofMatch; // if non-empty, then URLs must match in order to be spoofed

    /** Reference to Deamon's Map of url string to page character encoding of that page */
    private Map pageEncodings;
    /** Reference to Deamon's Map of url string to character encoding for the form */
    private Map formEncodings;

	/**
	 * Default constructor - used by newInstance call in Daemon
	 */
	public Proxy() {
	}

	/**
	 * Create and configure a new Proxy object.
	 * 
	 * @param clientSocket
	 *            the socket connection to the client
	 * @param target
	 *            the ProxyControl which will receive the generated sampler
	 */
	Proxy(Socket clientSocket, ProxyControl target) {
		configure(clientSocket, target);
	}

	/**
	 * Configure the Proxy.
	 * 
	 * @param clientSocket
	 *            the socket connection to the client
	 * @param target
	 *            the ProxyControl which will receive the generated sampler
	 */
	void configure(Socket _clientSocket, ProxyControl _target) {
        configure(_clientSocket, _target, null, null);
    }
    
    /**
     * Configure the Proxy.
     * 
     * @param _clientSocket
     *            the socket connection to the client
     * @param _target
     *            the ProxyControl which will receive the generated sampler
     * @param _pageEncodings
     *            reference to the Map of Deamon, with mappings from page urls to encoding used
     * @param formEncodingsEncodings
     *            reference to the Map of Deamon, with mappings from form action urls to encoding used
     */
    void configure(Socket _clientSocket, ProxyControl _target, Map _pageEncodings, Map _formEncodings) {
        this.target = _target;
        this.clientSocket = _clientSocket;
        this.captureHttpHeaders = _target.getCaptureHttpHeaders();
        this.httpsSpoof = _target.getHttpsSpoof();
        this.httpsSpoofMatch = _target.getHttpsSpoofMatch();
        this.pageEncodings = _pageEncodings;
        this.formEncodings = _formEncodings;
    }

	/**
	 * Main processing method for the Proxy object
	 */
	public void run() {
		// Check which HTTPSampler class we should use
		String httpSamplerName = HTTPSamplerFactory.DEFAULT_CLASSNAME;
		if(target.getSamplerTypeName() == ProxyControl.SAMPLER_TYPE_HTTP_SAMPLER) {
			httpSamplerName = HTTPSamplerFactory.HTTP_SAMPLER_JAVA;
		}
		else if(target.getSamplerTypeName() == ProxyControl.SAMPLER_TYPE_HTTP_SAMPLER2) {
			httpSamplerName = HTTPSamplerFactory.HTTP_SAMPLER_APACHE;
		}
		// Instantiate the sampler
		HTTPSamplerBase sampler = HTTPSamplerFactory.newInstance(httpSamplerName);
		
		HttpRequestHdr request = new HttpRequestHdr(sampler);
		SampleResult result = null;
		HeaderManager headers = null;

		try {
			request.parse(new BufferedInputStream(clientSocket.getInputStream()));

			// Populate the sampler. It is the same sampler as we sent into
			// the constructor of the HttpRequestHdr instance above 
            request.getSampler(pageEncodings, formEncodings);

			/*
			 * Create a Header Manager to ensure that the browsers headers are
			 * captured and sent to the server
			 */
			headers = request.getHeaderManager();
			sampler.setHeaderManager(headers);
			
			/* 
			 * If we are trying to spoof https, change the protocol
			 */
			if (httpsSpoof) {
				if (httpsSpoofMatch.length() > 0){
					String url = request.getUrl();
					if (url.matches(httpsSpoofMatch)){
						sampler.setProtocol(HTTPConstants.PROTOCOL_HTTPS);						
					}
				} else {
				    sampler.setProtocol(HTTPConstants.PROTOCOL_HTTPS);
				}
			}
			sampler.threadStarted(); // Needed for HTTPSampler2
			result = sampler.sample();
			
			/*
			 * If we're dealing with text data, and if we're spoofing https, 
			 * replace all occurences of "https:" with "http:" for the client. 
			 */
			if (httpsSpoof && SampleResult.TEXT.equals(result.getDataType()))
			{
				String noHttpsResult = new String(result.getResponseData());
				result.setResponseData(noHttpsResult.replaceAll("https:", "http:").getBytes());// TODO this could mangle the encoding
			}

            // Find the page encoding and possibly encodings for forms in the page
            // in the response from the web server
            String pageEncoding = addPageEncoding(result);
            addFormEncodings(result, pageEncoding);

			writeToClient(result, new BufferedOutputStream(clientSocket.getOutputStream()));
		} catch (UnknownHostException uhe) {
			log.warn("Server Not Found.", uhe);
			writeErrorToClient(HttpReplyHdr.formServerNotFound());
			result = generateErrorResult(result, uhe); // Generate result (if nec.) and populate it
		} catch (IllegalArgumentException e) {
			log.error("Not implemented (probably used https)", e);
			writeErrorToClient(HttpReplyHdr.formNotImplemented());			
			result = generateErrorResult(result, e); // Generate result (if nec.) and populate it
		} catch (Exception e) {
			log.error("Exception when processing sample", e);
			writeErrorToClient(HttpReplyHdr.formTimeout());
			result = generateErrorResult(result, e); // Generate result (if nec.) and populate it
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("Will deliver sample " + sampler.getName());
			}
			/*
			 * We don't want to store any cookies in the generated test plan
			 */
			if (headers != null) {
				headers.removeHeaderNamed("cookie");// Always remove cookies // $NON-NLS-1$
				headers.removeHeaderNamed("Authorization");// Always remove authorization // $NON-NLS-1$
				// Remove additional headers
				for(int i=0; i < headersToRemove.length; i++){
					headers.removeHeaderNamed(headersToRemove[i]);
				}
		    }
			target.deliverSampler(sampler, new TestElement[] { captureHttpHeaders ? headers : null }, result);
			try {
				clientSocket.close();
			} catch (Exception e) {
				log.error("", e);
			}
			sampler.threadFinished(); // Needed for HTTPSampler2
		}
	}

	private SampleResult generateErrorResult(SampleResult result, Exception e) {
		if (result == null) {
			result = new SampleResult();
			result.setSampleLabel("Sample failed");
		}
		result.setResponseMessage(e.getMessage());
		return result;
	}

	/**
	 * Write output to the output stream, then flush and close the stream.
	 * 
	 * @param inBytes
	 *            the bytes to write
	 * @param out
	 *            the output stream to write to
	 * @throws IOException
	 *             if an IOException occurs while writing
	 */
	private void writeToClient(SampleResult res, OutputStream out) throws IOException {
		try {
			String responseHeaders = massageResponseHeaders(res);
            out.write(responseHeaders.getBytes());
			out.write('\n'); // $NON-NLS-1$
			out.write(res.getResponseData());
			out.flush();
			log.debug("Done writing to client");
		} catch (IOException e) {
			log.error("", e);
			throw e;
		} finally {
			try {
				out.close();
			} catch (Exception ex) {
				log.warn("Error while closing socket", ex);
			}
		}
	}

	/**
	 * In the event the content was gzipped and unpacked, the content-encoding
	 * header must be removed and the content-length header should be corrected.
	 * 
	 * The Transfer-Encoding header is also removed.
	 * 
	 * @param res - response
	 * 
	 * @return updated headers to be sent to client
	 */
	private String massageResponseHeaders(SampleResult res) {
		String headers = res.getResponseHeaders();
		String [] headerLines=headers.split(NEW_LINE, 0); // drop empty trailing content
		int contentLengthIndex=-1;
		boolean fixContentLength = false;
		for (int i=0;i<headerLines.length;i++){
			String line=headerLines[i];
			String[] parts=line.split(":\\s+",2); // $NON-NLS-1$
			if (parts.length==2){
				if (HTTPConstants.TRANSFER_ENCODING.equalsIgnoreCase(parts[0])){
					headerLines[i]=null; // We don't want this passed on to browser
					continue;
				}
				if (HTTPConstants.HEADER_CONTENT_ENCODING.equalsIgnoreCase(parts[0])
					&&
					HTTPConstants.ENCODING_GZIP.equalsIgnoreCase(parts[1])
				){
					headerLines[i]=null; // We don't want this passed on to browser
					fixContentLength = true;
					continue;
				}
				if (HTTPConstants.HEADER_CONTENT_LENGTH.equalsIgnoreCase(parts[0])){
					contentLengthIndex=i;
					continue;
				}
			}
		}
		if (fixContentLength && contentLengthIndex>=0){// Fix the content length
			headerLines[contentLengthIndex]=HTTPConstants.HEADER_CONTENT_LENGTH+": "+res.getResponseData().length;
		}
		StringBuffer sb = new StringBuffer(headers.length());
		for (int i=0;i<headerLines.length;i++){
			String line=headerLines[i];
			if (line != null){
				sb.append(line).append(NEW_LINE);
			}
		}
		return sb.toString();
	}

	/**
	 * Write an error message to the client. The message should be the full HTTP
	 * response.
	 * 
	 * @param message
	 *            the message to write
	 */
	private void writeErrorToClient(String message) {
		try {
			OutputStream sockOut = clientSocket.getOutputStream();
			DataOutputStream out = new DataOutputStream(sockOut);
			out.writeBytes(message);
			out.flush();
		} catch (Exception e) {
			log.warn("Exception while writing error", e);
		}
	}

    /**
     * Add the page encoding of the sample result to the Map with page encodings
     * 
     * @param result the sample result to check
     * @return the page encoding found for the sample result, or null
     */
    private String addPageEncoding(SampleResult result) {
        String pageEncoding = getContentEncoding(result);
        if(pageEncoding != null) {
            String urlWithoutQuery = getUrlWithoutQuery(result.getURL());
            synchronized(pageEncodings) {
                pageEncodings.put(urlWithoutQuery, pageEncoding);
            }
        }
        return pageEncoding;
    }
    
    /**
     * Add the form encodings for all forms in the sample result
     * 
     * @param result the sample result to check
     * @param pageEncoding the encoding used for the sample result page
     */
    private void addFormEncodings(SampleResult result, String pageEncoding) {
        FormCharSetFinder finder = new FormCharSetFinder();
        if (!result.getContentType().startsWith("text/")){ // TODO perhaps make more specific than this?
        	return; // no point parsing anything else, e.g. GIF ...
        }
        try {
            finder.addFormActionsAndCharSet(result.getResponseDataAsString(), formEncodings, pageEncoding);
        }
        catch (HTMLParseException parseException) {
            log.debug("Unable to parse response, could not find any form character set encodings");
        }
    }

    /**
     * Get the value of the charset of the content-type header of the sample result
     * 
     * @param res the sample result to find the charset for
     * @return the charset found, or null
     */
    private String getContentEncoding(SampleResult res) {
        String contentTypeHeader = res.getContentType();
        String charSet = null;
        if (contentTypeHeader != null) {
            int charSetStartPos = contentTypeHeader.toLowerCase().indexOf("charset=");
            if (charSetStartPos > 0) {
                charSet = contentTypeHeader.substring(charSetStartPos + "charset=".length());
                if (charSet != null) {
                    if (charSet.trim().length() > 0) {
                        charSet = charSet.trim();
                    } else {
                        charSet = null;
                    }
                }
            }
        }
        return charSet;
    }

    private String getUrlWithoutQuery(URL url) {
        String fullUrl = url.toString();
        String urlWithoutQuery = fullUrl;
        String query = url.getQuery();
        if(query != null) {
            // Get rid of the query and the ?
            urlWithoutQuery = urlWithoutQuery.substring(0, urlWithoutQuery.length() - query.length() - 1);
        }
        return urlWithoutQuery;
    }
}
