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
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.IllegalCharsetNameException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.parser.HTMLParseException;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.ConversionUtils;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JMeterException;
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread to handle one client request. Gets the request from the client and
 * passes it on to the server, then sends the response back to the client.
 * Information about the request and response is stored so it can be used in a
 * JMeter test plan.
 */
public class Proxy extends Thread {
    private static final Logger log = LoggerFactory.getLogger(Proxy.class);

    private static final byte[] CRLF_BYTES = { 0x0d, 0x0a };
    private static final String CRLF_STRING = "\r\n";

    private static final String NEW_LINE = "\n"; // $NON-NLS-1$

    private static final String[] HEADERS_TO_REMOVE;

    // Allow list of headers to be overridden
    private static final String PROXY_HEADERS_REMOVE = "proxy.headers.remove"; // $NON-NLS-1$

    private static final String PROXY_HEADERS_REMOVE_DEFAULT = "If-Modified-Since,If-None-Match,Host"; // $NON-NLS-1$

    private static final String PROXY_HEADERS_REMOVE_SEPARATOR = ","; // $NON-NLS-1$

    private static final String KEYMANAGERFACTORY =
        JMeterUtils.getPropDefault("proxy.cert.factory", "SunX509"); // $NON-NLS-1$ $NON-NLS-2$

    private static final String SSLCONTEXT_PROTOCOL =
        JMeterUtils.getPropDefault("proxy.ssl.protocol", "TLS"); // $NON-NLS-1$ $NON-NLS-2$

    // HashMap to save ssl connection between Jmeter proxy and browser
    private static final HashMap<String, SSLSocketFactory> HOST2SSL_SOCK_FAC = new HashMap<>();

    private static final SamplerCreatorFactory SAMPLERFACTORY = new SamplerCreatorFactory();

    static {
        String removeList = JMeterUtils.getPropDefault(PROXY_HEADERS_REMOVE,PROXY_HEADERS_REMOVE_DEFAULT);
        HEADERS_TO_REMOVE = JOrphanUtils.split(removeList,PROXY_HEADERS_REMOVE_SEPARATOR);
        log.info("Proxy will remove the headers: {}", removeList);
    }

    // Use with SSL connection
    private OutputStream outStreamClient = null;

    /** Socket to client. */
    private Socket clientSocket = null;

    /** Target to receive the generated sampler. */
    private ProxyControl target;

    /** Whether or not to capture the HTTP headers. */
    private boolean captureHttpHeaders;

    /** Reference to Daemon's Map of url string to page character encoding of that page */
    private Map<String, String> pageEncodings;
    /** Reference to Daemon's Map of url string to character encoding for the form */
    private Map<String, String> formEncodings;

    private String port; // For identifying log messages

    private KeyStore keyStore; // keystore for SSL keys; fixed at config except for dynamic host key generation

    private String keyPassword;

    /**
     * Default constructor - used by newInstance call in Daemon
     */
    public Proxy() {
        port = "";
    }

    /**
     * Configure the Proxy.
     * Intended to be called directly after construction.
     * Should not be called after it has been passed to a new thread,
     * otherwise the variables may not be published correctly.
     *
     * @param clientSocket
     *            the socket connection to the client
     * @param target
     *            the ProxyControl which will receive the generated sampler
     * @param pageEncodings
     *            reference to the Map of Deamon, with mappings from page urls to encoding used
     * @param formEncodings
     *            reference to the Map of Deamon, with mappings from form action urls to encoding used
     */
    void configure(Socket clientSocket, ProxyControl target, Map<String, String> pageEncodings, Map<String, String> formEncodings) {
        this.target = target;
        this.clientSocket = clientSocket;
        this.captureHttpHeaders = target.getCaptureHttpHeaders();
        this.pageEncodings = pageEncodings;
        this.formEncodings = formEncodings;
        this.port = "["+ clientSocket.getPort() + "] ";
        this.keyStore = target.getKeyStore();
        this.keyPassword = target.getKeyPassword();
    }

    /**
     * Main processing method for the Proxy object
     */
    @Override
    public void run() {
        // Check which HTTPSampler class we should use
        String httpSamplerName = target.getSamplerTypeName();
        
        HttpRequestHdr request = new HttpRequestHdr(target.getPrefixHTTPSampleName(), httpSamplerName,target.getHTTPSampleNamingMode());
        SampleResult result = null;
        HeaderManager headers = null;
        HTTPSamplerBase sampler = null;
        final boolean isDebug = log.isDebugEnabled();
        log.debug("{} ====================================================================", port);
        SamplerCreator samplerCreator = null;
        try {
            JMeterContextService.getContext().setRecording(true);
            // Now, parse initial request (in case it is a CONNECT request)
            byte[] ba = request.parse(new BufferedInputStream(clientSocket.getInputStream()));
            if (ba.length == 0) {
                log.debug("{} Empty request, ignored", port);
                throw new JMeterException(); // hack to skip processing
            }
            if (isDebug) {
                log.debug("{} Initial request: {}", port, new String(ba));
            }
            outStreamClient = clientSocket.getOutputStream();

            if ((request.getMethod().startsWith(HTTPConstants.CONNECT)) && (outStreamClient != null)) {
                log.debug("{} Method CONNECT => SSL", port);
                // write a OK response to browser, to engage SSL exchange
                outStreamClient.write(
                        "HTTP/1.0 200 OK\r\n\r\n".getBytes(SampleResult.DEFAULT_HTTP_ENCODING)); // $NON-NLS-1$
                outStreamClient.flush();
               // With ssl request, url is host:port (without https:// or path)
                String[] param = request.getUrl().split(":");  // $NON-NLS-1$
                if (param.length == 2) {
                    log.debug("{} Start to negotiate SSL connection, host: {}", port ,param[0]);
                    clientSocket = startSSL(clientSocket, param[0]);
                } else {
                    // Should not happen, but if it does we don't want to continue 
                    log.error("In SSL request, unable to find host and port in CONNECT request: {}", request.getUrl());
                    throw new JMeterException(); // hack to skip processing
                }
                // Re-parse (now it's the http request over SSL)
                try {
                    ba = request.parse(new BufferedInputStream(clientSocket.getInputStream()));
                } catch (IOException ioe) { // most likely this is because of a certificate error
                    // param.length is 2 here
                    final String url = " for '"+ param[0] +"'";
                    log.warn("{} Problem with SSL certificate for url {}? Ensure browser is set to accept the JMeter proxy cert: {}",
                            port, url,ioe.getMessage());
                    // Generate result (if nec.) and populate it
                    result = generateErrorResult(result, request, ioe, "\n**ensure browser is set to accept the JMeter proxy certificate**");
                    throw new JMeterException(); // hack to skip processing
                }
                if (isDebug) {
                    log.debug("{} Reparse: {}", port, new String(ba));
                }
                if (ba.length == 0) {
                    log.warn("{} Empty response to http over SSL. Probably waiting for user to authorize the certificate for {}",
                                port, request.getUrl());
                    throw new JMeterException(); // hack to skip processing
                }
            }

            samplerCreator = SAMPLERFACTORY.getSamplerCreator(request, pageEncodings, formEncodings);
            sampler = samplerCreator.createAndPopulateSampler(request, pageEncodings, formEncodings);

            /*
             * Create a Header Manager to ensure that the browsers headers are
             * captured and sent to the server
             */
            headers = request.getHeaderManager();
            sampler.setHeaderManager(headers);

            sampler.threadStarted(); // Needed for HTTPSampler2
            if (isDebug) {
                log.debug("{} Execute sample: {} and url {}",port, sampler.getMethod(), sampler.getUrl());
            }
            result = sampler.sample();

            // Find the page encoding and possibly encodings for forms in the page
            // in the response from the web server
            String pageEncoding = addPageEncoding(result);
            addFormEncodings(result, pageEncoding);

            writeToClient(result, new BufferedOutputStream(clientSocket.getOutputStream()));
            samplerCreator.postProcessSampler(sampler, result);
        } catch (JMeterException jme) {
            // ignored, already processed
        } catch (UnknownHostException uhe) {
            log.warn("{} Server Not Found.", port, uhe);
            writeErrorToClient(HttpReplyHdr.formServerNotFound());
            result = generateErrorResult(result, request, uhe); // Generate result (if nec.) and populate it
        } catch (IllegalArgumentException e) {
            log.error("{} Not implemented (probably used https)", port, e);
            writeErrorToClient(HttpReplyHdr.formNotImplemented("Probably used https instead of http. "
                    + "To record https requests, see "
                    + "<a href=\"http://jmeter.apache.org/usermanual/component_reference.html#HTTP(S)_Test_Script_Recorder\">"
                    + "HTTP(S) Test Script Recorder documentation</a>"));
            result = generateErrorResult(result, request, e); // Generate result (if nec.) and populate it
        } catch (Exception e) {
            log.error("{} Exception when processing sample", port, e);
            writeErrorToClient(HttpReplyHdr.formInternalError());
            result = generateErrorResult(result, request, e); // Generate result (if nec.) and populate it
        } finally {
            if(sampler != null && isDebug) {
                log.debug("{} Will deliver sample {}", port, sampler.getName());
            }
            /*
             * We don't want to store any cookies in the generated test plan
             */
            if (headers != null) {
                headers.removeHeaderNamed(HTTPConstants.HEADER_COOKIE);// Always remove cookies
                // See https://bz.apache.org/bugzilla/show_bug.cgi?id=25430
                // HEADER_AUTHORIZATION won't be removed, it will be used
                // for creating Authorization Manager
                // Remove additional headers
                for(String hdr : HEADERS_TO_REMOVE){
                    headers.removeHeaderNamed(hdr);
                }
            }
            if(result != null) // deliverSampler allows sampler to be null, but result must not be null
            {
                List<TestElement> children = new ArrayList<>();
                if(captureHttpHeaders) {
                    children.add(headers);
                }
                if(samplerCreator != null) {
                    children.addAll(samplerCreator.createChildren(sampler, result));
                } 
                target.deliverSampler(sampler,
                         children
                                .toArray(new TestElement[children.size()]),
                        result);
            }
            try {
                clientSocket.close();
            } catch (Exception e) {
                log.error("{} Failed to close client socket", port, e);
            }
            if(sampler != null) {
                sampler.threadFinished(); // Needed for HTTPSampler2
            }
            JMeterContextService.getContext().setRecording(false);
        }
    }

    /**
     * Get SSL connection from hashmap, creating it if necessary.
     *
     * @param host
     * @return a ssl socket factory, or null if keystore could not be opened/processed
     */
    private SSLSocketFactory getSSLSocketFactory(String host) {
        if (keyStore == null) {
            log.error("{} No keystore available, cannot record SSL", port);
            return null;
        }
        final String hashAlias;
        final String keyAlias;
        switch(ProxyControl.KEYSTORE_MODE) {
        case DYNAMIC_KEYSTORE:
            try {
                keyStore = target.getKeyStore(); // pick up any recent changes from other threads
                String alias = getDomainMatch(keyStore, host);
                if (alias == null) {
                    hashAlias = host;
                    keyAlias = host;
                    keyStore = target.updateKeyStore(port, keyAlias);
                } else if (alias.equals(host)) { // the host has a key already
                    hashAlias = host;
                    keyAlias = host;
                } else { // the host matches a domain; use its key
                    hashAlias = alias;
                    keyAlias = alias;
                }
            } catch (IOException | GeneralSecurityException e) {
                log.error("{} Problem with keystore", port, e);
                return null;
            }
            break;
        case JMETER_KEYSTORE:
            hashAlias = keyAlias = ProxyControl.JMETER_SERVER_ALIAS;
            break;
        case USER_KEYSTORE:
            hashAlias = keyAlias = ProxyControl.CERT_ALIAS;
            break;
        default:
            throw new IllegalStateException("Impossible case: " + ProxyControl.KEYSTORE_MODE);
        }
        synchronized (HOST2SSL_SOCK_FAC) {
            final SSLSocketFactory sslSocketFactory = HOST2SSL_SOCK_FAC.get(hashAlias);
            if (sslSocketFactory != null) {
                log.debug("{} Good, already in map, host={} using alias {}", port, host, hashAlias);
                return sslSocketFactory;
            }
            try {
                SSLContext sslcontext = SSLContext.getInstance(SSLCONTEXT_PROTOCOL);
                sslcontext.init(getWrappedKeyManagers(keyAlias), null, null);
                SSLSocketFactory sslFactory = sslcontext.getSocketFactory();
                HOST2SSL_SOCK_FAC.put(hashAlias, sslFactory);
                log.info("{} KeyStore for SSL loaded OK and put host '{}' in map with key ({})", port, host, hashAlias);
                return sslFactory;
            } catch (GeneralSecurityException e) {
                log.error("{} Problem with SSL certificate", port, e);
            } catch (IOException e) {
                log.error("{} Problem with keystore", port, e);
            }
            return null;
        }
    }

    /**
     * Get matching alias for a host from keyStore that may contain domain aliases.
     * Assumes domains must have at least 2 parts (apache.org);
     * does not check if TLD requires more (google.co.uk).
     * Note that DNS wildcards only apply to a single level, i.e.
     * podling.incubator.apache.org matches *.incubator.apache.org
     * but does not match *.apache.org
     *
     * @param keyStore the KeyStore to search
     * @param host the hostname to match
     * @return the keystore entry or {@code null} if no match found
     * @throws KeyStoreException 
     */
    private String getDomainMatch(KeyStore keyStore, String host) throws KeyStoreException {
        if (keyStore.containsAlias(host)) {
            return host;
        }
        String[] parts = host.split("\\."); // get the component parts
        // Assume domains must have at least 2 parts, e.g. apache.org
        // Replace the first part with "*" 
        StringBuilder sb = new StringBuilder("*"); // $NON-NLS-1$
        for(int j = 1; j < parts.length ; j++) { // Skip the first part
            sb.append('.');
            sb.append(parts[j]);
        }
        String alias = sb.toString();
        if (keyStore.containsAlias(alias)) {
            return alias;
        }
        return null;
    }

    /**
     * Return the key managers, wrapped to return a specific alias
     */
    private KeyManager[] getWrappedKeyManagers(final String keyAlias)
            throws GeneralSecurityException, IOException {
        if (!keyStore.containsAlias(keyAlias)) {
            throw new IOException("Keystore does not contain alias " + keyAlias);
        }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KEYMANAGERFACTORY);
        kmf.init(keyStore, keyPassword.toCharArray());
        final KeyManager[] keyManagers = kmf.getKeyManagers();
        // Check if alias is suitable here, rather than waiting for connection to fail
        final int keyManagerCount = keyManagers.length;
        final KeyManager[] wrappedKeyManagers = new KeyManager[keyManagerCount];
        for (int i =0; i < keyManagerCount; i++) {
            wrappedKeyManagers[i] = new ServerAliasKeyManager(keyManagers[i], keyAlias);
        }
        return wrappedKeyManagers;
    }

    /**
     * Negotiate a SSL connection.
     *
     * @param sock socket in
     * @param host
     * @return a new client socket over ssl
     * @throws IOException if negotiation failed
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
                    log.debug("{} SSL transaction ok with cipher: {}", port, secureSocket.getSession().getCipherSuite());
                }
                return secureSocket;
            } catch (IOException e) {
                log.error("{} Error in SSL socket negotiation: ", port, e);
                throw e;
            }
        } else {
            log.warn("{} Unable to negotiate SSL transaction, no keystore?", port);
            throw new IOException("Unable to negotiate SSL transaction, no keystore?");
        }
    }

    private SampleResult generateErrorResult(SampleResult result, HttpRequestHdr request, Exception e) {
        return generateErrorResult(result, request, e, "");
    }

    private static SampleResult generateErrorResult(SampleResult result, HttpRequestHdr request, Exception e, String msg) {
        if (result == null) {
            result = new SampleResult();
            ByteArrayOutputStream text = new ByteArrayOutputStream(200);
            e.printStackTrace(new PrintStream(text)); // NOSONAR we store the Stacktrace in the result
            result.setResponseData(text.toByteArray());
            result.setSamplerData(request.getFirstLine());
            result.setSampleLabel(request.getUrl());
        }
        result.setSuccessful(false);
        result.setResponseMessage(e.getMessage()+msg);
        return result;
    }

    /**
     * Write output to the output stream, then flush and close the stream.
     *
     * @param res
     *            the SampleResult to write
     * @param out
     *            the output stream to write to
     * @throws IOException
     *             if an IOException occurs while writing
     */
    private void writeToClient(SampleResult res, OutputStream out) throws IOException {
        try {
            String responseHeaders = messageResponseHeaders(res);
            out.write(responseHeaders.getBytes(SampleResult.DEFAULT_HTTP_ENCODING));
            out.write(CRLF_BYTES);
            out.write(res.getResponseData());
            out.flush();
            log.debug("{} Done writing to client", port);
        } catch (IOException e) {
            log.error("", e);
            throw e;
        } finally {
            try {
                out.close();
            } catch (Exception ex) {
                log.warn("{} Error while closing socket", port, ex);
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
     *
     * @return updated headers to be sent to client
     */
    private String messageResponseHeaders(SampleResult res) {
        String headers = res.getResponseHeaders();
        String[] headerLines = headers.split(NEW_LINE, 0); // drop empty trailing content
        int contentLengthIndex = -1;
        boolean fixContentLength = false;
        for (int i = 0; i < headerLines.length; i++) {
            String line = headerLines[i];
            String[] parts = line.split(":\\s+", 2); // $NON-NLS-1$
            if (parts.length == 2) {
                if (HTTPConstants.TRANSFER_ENCODING.equalsIgnoreCase(parts[0])) {
                    headerLines[i] = null; // We don't want this passed on to browser
                    continue;
                }
                if (HTTPConstants.HEADER_CONTENT_ENCODING.equalsIgnoreCase(parts[0])
                    && (HTTPConstants.ENCODING_GZIP.equalsIgnoreCase(parts[1])
                            || HTTPConstants.ENCODING_DEFLATE.equalsIgnoreCase(parts[1])
                            // TODO BROTLI not supported by HC4, so no uncompression would occur, add it once available
                            // || HTTPConstants.ENCODING_BROTLI.equalsIgnoreCase(parts[1]) 
                            )
                ){
                    headerLines[i] = null; // We don't want this passed on to browser
                    fixContentLength = true;
                    continue;
                }
                if (HTTPConstants.HEADER_CONTENT_LENGTH.equalsIgnoreCase(parts[0])){
                    contentLengthIndex = i;
                }
            }
        }
        if (fixContentLength && contentLengthIndex>=0){// Fix the content length
            headerLines[contentLengthIndex] =
                    HTTPConstants.HEADER_CONTENT_LENGTH + ": " + res.getResponseData().length;
        }
        StringBuilder sb = new StringBuilder(headers.length());
        for (String line : headerLines) {
            if (line != null) {
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
            log.warn("{} Exception while writing error", port, e);
        }
    }

    /**
     * Add the page encoding of the sample result to the Map with page encodings
     *
     * @param result the sample result to check
     * @return the page encoding found for the sample result, or null
     */
    private String addPageEncoding(SampleResult result) {
        String pageEncoding = null;
        try {
            pageEncoding = ConversionUtils.getEncodingFromContentType(result.getContentType());
        } catch(IllegalCharsetNameException ex) {
            log.warn("Unsupported charset detected in contentType:'{}', will continue processing with default charset", 
                    result.getContentType(), ex);
        }
        if (pageEncoding != null) {
            String urlWithoutQuery = getUrlWithoutQuery(result.getURL());
            pageEncodings.put(urlWithoutQuery, pageEncoding);
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
            log.debug("{} Unable to parse response, could not find any form character set encodings", port);
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
