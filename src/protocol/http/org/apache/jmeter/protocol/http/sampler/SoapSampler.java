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

package org.apache.jmeter.protocol.http.sampler;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Commons HTTPClient based soap sampler
 */
public class SoapSampler extends HTTPSampler2 {
    private static final Logger log = LoggingManager.getLoggerForClass();

	public static final String XML_DATA = "HTTPSamper.xml_data"; //$NON-NLS-1$

	public static final String URL_DATA = "SoapSampler.URL_DATA"; //$NON-NLS-1$

	public static final String SOAP_ACTION = "SoapSampler.SOAP_ACTION"; //$NON-NLS-1$

	public static final String SEND_SOAP_ACTION = "SoapSampler.SEND_SOAP_ACTION"; //$NON-NLS-1$

	public static final String XML_DATA_FILE = "SoapSampler.xml_data_file"; //$NON-NLS-1$

	private static final String DOUBLE_QUOTE = "\""; //$NON-NLS-1$

	private static final String SOAPACTION = "SOAPAction"; //$NON-NLS-1$

	public void setXmlData(String data) {
		setProperty(XML_DATA, data);
	}

	public String getXmlData() {
		return getPropertyAsString(XML_DATA);
	}

    /**
     * it's kinda obvious, but we state it anyways. Set the xml file with a
     * string path.
     *
     * @param filename
     */
    public void setXmlFile(String filename) {
        setProperty(XML_DATA_FILE, filename);
    }

    /**
     * Get the file location of the xml file.
     *
     * @return String file path.
     */
    public String getXmlFile() {
        return getPropertyAsString(XML_DATA_FILE);
    }

	public String getURLData() {
		return getPropertyAsString(URL_DATA);
	}

	public void setURLData(String url) {
		setProperty(URL_DATA, url);
	}

	public String getSOAPAction() {
		return getPropertyAsString(SOAP_ACTION);
	}

	public String getSOAPActionQuoted() {
		String action = getSOAPAction();
		StringBuffer sb = new StringBuffer(action.length()+2);
		sb.append(DOUBLE_QUOTE);
		sb.append(action);
		sb.append(DOUBLE_QUOTE);
		return sb.toString();
	}

	public void setSOAPAction(String action) {
		setProperty(SOAP_ACTION, action);
	}

	public boolean getSendSOAPAction() {
		return getPropertyAsBoolean(SEND_SOAP_ACTION);
	}

	public void setSendSOAPAction(boolean action) {
		setProperty(SEND_SOAP_ACTION, String.valueOf(action));
	}

    protected int setPostHeaders(PostMethod post) {
    	int length=0;// Take length from file
        if (getHeaderManager() != null) {
            // headerManager was set, so let's set the connection
            // to use it.
            HeaderManager mngr = getHeaderManager();
            int headerSize = mngr.size();
            for (int idx = 0; idx < headerSize; idx++) {
                Header hd = mngr.getHeader(idx);
                if (HEADER_CONTENT_LENGTH.equalsIgnoreCase(hd.getName())) {// Use this to override file length
                	length = Integer.parseInt(hd.getValue());
                }
                // All the other headers are set up by HTTPSampler2.setupConnection()
            }
        } else {
            // otherwise we use "text/xml" as the default
            post.addParameter(HEADER_CONTENT_TYPE, "text/xml"); //$NON-NLS-1$
        }
        if (getSendSOAPAction()) {
            post.setRequestHeader(SOAPACTION, getSOAPActionQuoted());
        }
        return length;
    }

    /**
     * Send POST data from <code>Entry</code> to the open connection.
     *
     * @param post
     * @throws IOException if an I/O exception occurs
     */
    protected void sendPostData(PostMethod post, final int length) {
        final String xmlFile = getXmlFile();
        if (xmlFile != null && getXmlFile().length() > 0) {
            post.setRequestEntity(new RequestEntity() {
                public boolean isRepeatable() {
                    return true;
                }

                public void writeRequest(OutputStream out) throws IOException {
                    byte[] buf = new byte[1024];
                    // 1k - the previous 100k made no sense (there's tons of buffers
                    // elsewhere in the chain) and it caused OOM when many concurrent
                    // uploads were being done. Could be fixed by increasing the evacuation
                    // ratio in bin/jmeter[.bat], but this is better.
                    int read;
                    InputStream in = new FileInputStream(xmlFile);
                    while ((read = in.read(buf)) > 0) {
                        out.write(buf, 0, read);
                    }
                    in.close();
                    out.flush();
                }

                public long getContentLength() {
                	switch(length){
	                	case -1:
	                		return -1;
	                	case 0: // No header provided
	                		return (new File(xmlFile)).length();
	                	default:
	                		return length;
	                	}
                }

                public String getContentType() {
                    return "text/xml";
                }
            });
        } else {
            post.setRequestEntity(new RequestEntity() {
                public boolean isRepeatable() {
                    return true;
                }

                public void writeRequest(OutputStream out) throws IOException {
                    PrintWriter printer = new PrintWriter(out);
                    printer.print(getXmlData());
                    printer.flush();
                }

                public long getContentLength() {
                    return getXmlData().getBytes().length;// so we don't generate chunked encoding
                }

                public String getContentType() {
                    return "text/xml";
                }
            });
        }
    }

    protected HTTPSampleResult sample(URL url, String method, boolean areFollowingRedirect, int frameDepth) {

        String urlStr = url.toString();

        log.debug("Start : sample" + urlStr);
        log.debug("method" + method);

        PostMethod httpMethod;
        httpMethod = new PostMethod(urlStr);

        HTTPSampleResult res = new HTTPSampleResult();
        res.setMonitor(isMonitor());

        res.setSampleLabel(urlStr); // May be replaced later
        res.setHTTPMethod(method);
        res.sampleStart(); // Count the retries as well in the time
        HttpClient client = null;
        InputStream instream = null;
        try {
            int content_len = setPostHeaders(httpMethod);
            client = setupConnection(url, httpMethod, res);

            res.setQueryString(getQueryString());
            sendPostData(httpMethod,content_len);

            int statusCode = client.executeMethod(httpMethod);

            // Request sent. Now get the response:
            instream = httpMethod.getResponseBodyAsStream();

            if (instream != null) {// will be null for HEAD

                org.apache.commons.httpclient.Header responseHeader = httpMethod.getResponseHeader(TRANSFER_ENCODING);
                if (responseHeader != null && ENCODING_GZIP.equals(responseHeader.getValue())) {
                    instream = new GZIPInputStream(instream);
                }

                //int contentLength = httpMethod.getResponseContentLength();Not visible ...
                //TODO size ouststream according to actual content length
                ByteArrayOutputStream outstream = new ByteArrayOutputStream(4 * 1024);
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
            if (getAutoRedirects()) {
                res.setURL(new URL(httpMethod.getURI().toString()));
            }

            // Store any cookies received in the cookie manager:
            saveConnectionCookies(httpMethod, res.getURL(), getCookieManager());

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

    public URL getUrl() throws MalformedURLException {
        return new URL(getURLData());
    }
}
