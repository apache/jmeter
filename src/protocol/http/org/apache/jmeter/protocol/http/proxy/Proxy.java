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

import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.parser.HTMLParseException;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Thread to handle one client request. Gets the request from the client and
 * passes it on to the server, then sends the response back to the client.
 * Information about the request and response is stored so it can be used in a
 * JMeter test plan.
 * 
 * @author mike
 * @version $Revision$ Last updated: $Date$ Created
 *          June 8, 2001
 */
public class Proxy extends Thread {
    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String NEW_LINE = "\n"; // $NON-NLS-1$

	/** Socket to client. */
	private Socket clientSocket = null;

	/** Target to receive the generated sampler. */
	private ProxyControl target;

	/** Whether or not to capture the HTTP headers. */
	private boolean captureHttpHeaders;

	/** Whether to try to spoof as https **/
	private boolean httpsSpoof;

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
     * @param clientSocket
     *            the socket connection to the client
     * @param target
     *            the ProxyControl which will receive the generated sampler
     * @param pageEncodings
     *            reference to the Map of Deamon, with mappings from page urls to encoding used
     * @param formEncodingsEncodings
     *            reference to the Map of Deamon, with mappings from form action urls to encoding used
     */
    void configure(Socket clientSocket, ProxyControl target, Map pageEncodings, Map formEncodings) {
        this.target = target;
        this.clientSocket = clientSocket;
        this.captureHttpHeaders = target.getCaptureHttpHeaders();
        this.httpsSpoof = target.getHttpsSpoof();
        this.pageEncodings = pageEncodings;
        this.formEncodings = formEncodings;
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
				sampler.setProtocol("https");
			}
			sampler.threadStarted(); // Needed for HTTPSampler2
			result = sampler.sample();
			
			/*
			 * If we're dealing with text data, and if we're spoofing https, 
			 * replace all occurences of "https" with "http" for the client. 
			 */
			if (httpsSpoof && SampleResult.TEXT.equals(result.getDataType()))
			{
				String noHttpsResult = new String(result.getResponseData());
				result.setResponseData(noHttpsResult.replaceAll("https", "http").getBytes());
			}

            // Find the page encoding and possibly encodings for forms in the page
            // in the response from the web server
            String pageEncoding = addPageEncoding(result);
            addFormEncodings(result, pageEncoding);

			writeToClient(result, new BufferedOutputStream(clientSocket.getOutputStream()));
			/*
			 * We don't want to store any cookies in the generated test plan
			 */
			headers.removeHeaderNamed("cookie");// Always remove cookies // $NON-NLS-1$
		} catch (UnknownHostException uhe) {
			log.warn("Server Not Found.", uhe);
			writeErrorToClient(HttpReplyHdr.formServerNotFound());
		} catch (Exception e) {
			log.error("", e);
			writeErrorToClient(HttpReplyHdr.formTimeout());
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("Will deliver sample " + sampler.getName());
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
			String responseHeaders = massageResponseHeaders(res, res.getResponseHeaders());
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
	 * @param res
	 * @param headers
	 * @return
	 */
	private String massageResponseHeaders(SampleResult res, String headers) {
		int encodingHeaderLoc = headers.indexOf(": gzip"); // $NON-NLS-1$
		String newHeaders = headers;
		if (encodingHeaderLoc > -1) {
			int end = headers.indexOf(NEW_LINE, encodingHeaderLoc);
			int begin = headers.lastIndexOf(NEW_LINE, encodingHeaderLoc);
			newHeaders = newHeaders.substring(0, begin) + newHeaders.substring(end);
			int lengthIndex = newHeaders.indexOf("ength: "); // $NON-NLS-1$
			end = newHeaders.indexOf(NEW_LINE, lengthIndex);
			newHeaders = newHeaders.substring(0, lengthIndex + 7) + res.getResponseData().length
					+ newHeaders.substring(end);
		}
		return newHeaders;
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
