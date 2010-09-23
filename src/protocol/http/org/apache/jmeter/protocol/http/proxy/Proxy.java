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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.parser.HTMLParseException;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.util.ConversionUtils;
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

    private static final byte[] CRLF_BYTES = { 0x0d, 0x0a };
    private static final String CRLF_STRING = "\r\n";

    private static final String NEW_LINE = "\n"; // $NON-NLS-1$

    private static final String[] headersToRemove;

    // Allow list of headers to be overridden
    private static final String PROXY_HEADERS_REMOVE = "proxy.headers.remove"; // $NON-NLS-1$

    private static final String PROXY_HEADERS_REMOVE_DEFAULT = "If-Modified-Since,If-None-Match,Host"; // $NON-NLS-1$

    private static final String PROXY_HEADERS_REMOVE_SEPARATOR = ","; // $NON-NLS-1$

    // for ssl connection
    private static final String KEYSTORE_TYPE =
        JMeterUtils.getPropDefault("proxy.cert.type", "JKS"); // $NON-NLS-1$ $NON-NLS-2$

    private static final String KEYMANAGERFACTORY =
        JMeterUtils.getPropDefault("proxy.cert.factory", "SunX509"); // $NON-NLS-1$ $NON-NLS-2$

    private static final String SSLCONTEXT_PROTOCOL =
        JMeterUtils.getPropDefault("proxy.ssl.protocol", "SSLv3"); // $NON-NLS-1$ $NON-NLS-2$

    // HashMap to save ssl connection between Jmeter proxy and browser
    private static final HashMap<String, SSLSocketFactory> hashHost = new HashMap<String, SSLSocketFactory>();

    // Proxy configuration SSL
    private static final String CERT_DIRECTORY =
        JMeterUtils.getPropDefault("proxy.cert.directory", JMeterUtils.getJMeterBinDir()); // $NON-NLS-1$

    private static final String CERT_FILE_DEFAULT = "proxyserver.jks";// $NON-NLS-1$

    private static final String CERT_FILE =
        JMeterUtils.getPropDefault("proxy.cert.file", CERT_FILE_DEFAULT); // $NON-NLS-1$

    private static final char[] KEYSTORE_PASSWORD =
        JMeterUtils.getPropDefault("proxy.cert.keystorepass", "password").toCharArray(); // $NON-NLS-1$ $NON-NLS-2$

    private static final char[] KEY_PASSWORD =
        JMeterUtils.getPropDefault("proxy.cert.keypassword","password").toCharArray(); // $NON-NLS-1$ $NON-NLS-2$

    // Use with SSL connection
    private OutputStream outStreamClient = null;

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
    private Map<String, String> pageEncodings;
    /** Reference to Deamon's Map of url string to character encoding for the form */
    private Map<String, String> formEncodings;

    /**
     * Default constructor - used by newInstance call in Daemon
     */
    public Proxy() {
    }

    /**
     * Configure the Proxy.
     * Intended to be called directly after construction.
     * Should not be called after it has been passed to a new thread,
     * otherwise the variables may not be published correctly.
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
    void configure(Socket _clientSocket, ProxyControl _target, Map<String, String> _pageEncodings, Map<String, String> _formEncodings) {
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
    @Override
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
            // Now, parse only first line
            request.parse(new BufferedInputStream(clientSocket.getInputStream()));
            outStreamClient = clientSocket.getOutputStream();

            if ((request.getMethod().startsWith(HTTPConstants.CONNECT)) && (outStreamClient != null)) {
                log.debug("Method CONNECT => SSL");
                // write a OK reponse to browser, to engage SSL exchange
                outStreamClient.write(("HTTP/1.0 200 OK\r\n\r\n").getBytes()); // $NON-NLS-1$ // TODO charset?
                outStreamClient.flush();
               // With ssl request, url is host:port (without https:// or path)
                String[] param = request.getUrl().split(":");  // $NON-NLS-1$
                if (param.length == 2) {
                    log.debug("Start to negotiate SSL connection, host: " + param[0]);
                    clientSocket = startSSL(clientSocket, param[0]);
                } else {
                    log.warn("In SSL request, unable to find host and port in CONNECT request");
                }
                // Re-parse (now it's the http request over SSL)
                request.parse(new BufferedInputStream(clientSocket.getInputStream()));
            }

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
            boolean forcedHTTPS = false; // so we know when to revert
            if (httpsSpoof) {
                if (httpsSpoofMatch.length() > 0){
                    String url = request.getUrl();
                    if (url.matches(httpsSpoofMatch)){
                        sampler.setProtocol(HTTPConstants.PROTOCOL_HTTPS);
                        forcedHTTPS = true;
                    }
                } else {
                    sampler.setProtocol(HTTPConstants.PROTOCOL_HTTPS);
                    forcedHTTPS = true;
                }
            }
            sampler.threadStarted(); // Needed for HTTPSampler2
            result = sampler.sample();

            /*
             * If we're dealing with text data, and if we're spoofing https,
             * replace all occurences of "https://" with "http://" for the client.
             * TODO - also check the match string to restrict the changes further?
             */
            if (httpsSpoof && SampleResult.TEXT.equals(result.getDataType()))
            {
                final String enc = result.getDataEncodingWithDefault();
                String noHttpsResult = new String(result.getResponseData(),enc);
                final String HTTPS_HOST = // match https://host[:port]/ and drop default port if present
                    "https://([^:/]+)(:"+HTTPConstants.DEFAULT_HTTPS_PORT_STRING+")?"; // $NON-NLS-1$ $NON-NLS-2$
                noHttpsResult = noHttpsResult.replaceAll(HTTPS_HOST, "http://$1"); // $NON-NLS-1$
                result.setResponseData(noHttpsResult.getBytes(enc));
            }

            // Find the page encoding and possibly encodings for forms in the page
            // in the response from the web server
            String pageEncoding = addPageEncoding(result);
            addFormEncodings(result, pageEncoding);

            writeToClient(result, new BufferedOutputStream(clientSocket.getOutputStream()), forcedHTTPS);
        } catch (UnknownHostException uhe) {
            log.warn("Server Not Found.", uhe);
            writeErrorToClient(HttpReplyHdr.formServerNotFound());
            result = generateErrorResult(result, uhe); // Generate result (if nec.) and populate it
        } catch (IllegalArgumentException e) {
            log.error("Not implemented (probably used https)", e);
            writeErrorToClient(HttpReplyHdr.formNotImplemented("Probably used https instead of http. " +
                    "To record https requests, see " +
                    "<a href=\"http://jakarta.apache.org/jmeter/usermanual/component_reference.html#HTTP_Proxy_Server\">HTTP Proxy Server documentation</a>"));
            result = generateErrorResult(result, e); // Generate result (if nec.) and populate it
        } catch (IOException ioe) {
            log.error("Problem with SSL certificate? Ensure browser is set to accept the JMeter proxy cert: "+ioe.getLocalizedMessage());
            // won't work: writeErrorToClient(HttpReplyHdr.formInternalError());
            if (result == null) {
                result = new SampleResult();
                result.setSampleLabel("Sample failed");
            }
            result.setResponseMessage(ioe.getMessage()+ "\n**ensure browser is set to accept the JMeter proxy certificate**");
        } catch (Exception e) {
            log.error("Exception when processing sample", e);
            writeErrorToClient(HttpReplyHdr.formInternalError());
            result = generateErrorResult(result, e); // Generate result (if nec.) and populate it
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("Will deliver sample " + sampler.getName());
            }
            /*
             * We don't want to store any cookies in the generated test plan
             */
            if (headers != null) {
                headers.removeHeaderNamed(HTTPConstants.HEADER_COOKIE);// Always remove cookies
                headers.removeHeaderNamed(HTTPConstants.HEADER_AUTHORIZATION);// Always remove authorization
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

    /**
     * Get SSL connection from hashmap, creating it if necessary.
     *
     * @param host
     * @return a ssl socket factory
     * @throws IOException
     */
    private SSLSocketFactory getSSLSocketFactory(String host) throws IOException {
        synchronized (hashHost) {
            if (hashHost.containsKey(host)) {
                log.debug("Good, already in map, host=" + host);
                return hashHost.get(host);
            }
            InputStream in = getCertificate();
            Exception except = null;
            if (in != null) {
                KeyStore ks = null;
                KeyManagerFactory kmf = null;
                SSLContext sslcontext = null;
                try {
                    ks = KeyStore.getInstance(KEYSTORE_TYPE);
                    ks.load(in, KEYSTORE_PASSWORD);
                    kmf = KeyManagerFactory.getInstance(KEYMANAGERFACTORY);
                    kmf.init(ks, KEY_PASSWORD);
                    sslcontext = SSLContext.getInstance(SSLCONTEXT_PROTOCOL);
                    sslcontext.init(kmf.getKeyManagers(), null, null);
                    SSLSocketFactory sslFactory = sslcontext.getSocketFactory();
                    hashHost.put(host, sslFactory);
                    log.info("KeyStore for SSL loaded OK and put host in map ("+host+")");
                    return sslFactory;
                } catch (NoSuchAlgorithmException e) {
                    except=e;
                } catch (KeyManagementException e) {
                    except=e;
                } catch (KeyStoreException e) {
                    except=e;
                } catch (UnrecoverableKeyException e) {
                    except=e;
                } catch (CertificateException e) {
                    except=e;
                } finally {
                    if (except != null){
                        log.error("Problem with SSL certificate",except);
                    }
                    IOUtils.closeQuietly(in);
                }
            } else {
                throw new IOException("Unable to read keystore");
            }
            return null;
        }
    }

    /**
     * Negotiate a SSL connection.
     *
     * @param sock socket in
     * @param host
     * @return a new client socket over ssl
     * @throws Exception if negotiation failed
     */
    private Socket startSSL(Socket sock, String host) throws IOException {
        SSLSocketFactory sslFactory = getSSLSocketFactory(host);
        SSLSocket secureSocket;
        if (sslFactory != null) {
            try {
                secureSocket = (SSLSocket) sslFactory.createSocket(sock,
                        sock.getInetAddress().getHostName(), sock.getPort(), true);
                secureSocket.setUseClientMode(false);
                if (log.isDebugEnabled()){
                    log.debug("SSL transaction ok with cipher: " + secureSocket.getSession().getCipherSuite());
                }
                return secureSocket;
            } catch (IOException e) {
                log.error("Error in SSL socket negotiation: ", e);
                throw e;
            }
        } else {
            log.warn("Unable to negotiate SSL transaction, no keystore?");
            throw new IOException("Unable to negotiate SSL transaction, no keystore?");
        }
    }

    /**
     * Open the local certificate file.
     *
     * @return stream to key cert; null if there was a problem opening it
     */
    private InputStream getCertificate() {
        File certFile = new File(CERT_DIRECTORY, CERT_FILE);
        InputStream in = null;
        final String certPath = certFile.getAbsolutePath();
        if (certFile.exists() && certFile.canRead()) {
            try {
                in = new FileInputStream(certFile);
                log.info("Opened Keystore file: "+certPath);
            } catch (FileNotFoundException e) {
                log.error("No server cert file found: "+certPath, e);
            }
        } else {
            log.error("No server cert file found: "+certPath);
        }
        return in;
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
     * @param forcedHTTPS if we changed the protocol to https
     * @throws IOException
     *             if an IOException occurs while writing
     */
    private void writeToClient(SampleResult res, OutputStream out, boolean forcedHTTPS) throws IOException {
        try {
            String responseHeaders = massageResponseHeaders(res, forcedHTTPS);
            out.write(responseHeaders.getBytes()); // TODO - charset?
            out.write(CRLF_BYTES);
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
     * If the protocol was changed to HTTPS then change any Location header back to http
     * @param res - response
     * @param forcedHTTPS  if we changed the protocol to https
     *
     * @return updated headers to be sent to client
     */
    private String massageResponseHeaders(SampleResult res, boolean forcedHTTPS) {
        String headers = res.getResponseHeaders();
        String [] headerLines=headers.split(NEW_LINE, 0); // drop empty trailing content
        int contentLengthIndex=-1;
        boolean fixContentLength = forcedHTTPS;
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
                final String HTTPS_PREFIX = "https://";
                if (forcedHTTPS && HTTPConstants.HEADER_LOCATION.equalsIgnoreCase(parts[0])
                        && parts[1].substring(0, HTTPS_PREFIX.length()).equalsIgnoreCase(HTTPS_PREFIX)){
                    headerLines[i]=headerLines[i].replaceFirst(parts[1].substring(0,HTTPS_PREFIX.length()), "http://");
                    continue;
                }
                if (forcedHTTPS && HTTPConstants.HEADER_COOKIE.equalsIgnoreCase(parts[0]) || HTTPConstants.HEADER_SET_COOKIE.equalsIgnoreCase(parts[0]))
                {
                    headerLines[i]=headerLines[i].replaceAll(" secure", "").trim(); //in forced https cookies need to be unsecured...
                }
            }
        }
        if (fixContentLength && contentLengthIndex>=0){// Fix the content length
            headerLines[contentLengthIndex]=HTTPConstants.HEADER_CONTENT_LENGTH+": "+res.getResponseData().length;
        }
        StringBuilder sb = new StringBuilder(headers.length());
        for (int i=0;i<headerLines.length;i++){
            String line=headerLines[i];
            if (line != null){
                sb.append(line).append(CRLF_STRING);
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
        String pageEncoding = ConversionUtils.getEncodingFromContentType(result.getContentType());
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
