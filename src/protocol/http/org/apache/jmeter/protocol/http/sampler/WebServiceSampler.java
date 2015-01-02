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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.JMeter;
import org.apache.jmeter.gui.JMeterFileFilter;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.DOMPool;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.io.TextFile;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.soap.Envelope;
import org.apache.soap.SOAPException;
import org.apache.soap.rpc.SOAPContext;
import org.apache.soap.transport.http.SOAPHTTPConnection;
import org.apache.soap.util.xml.XMLParserUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Sampler to handle Web Service requests. It uses Apache SOAP drivers to
 * perform the XML generation, connection, SOAP encoding and other SOAP
 * functions.
 * <p>
 * Created on: Jun 26, 2003
 *
 */
public class WebServiceSampler extends HTTPSamplerBase  {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final long serialVersionUID = 240L;

    //+ JMX file attribute names - do not change!
    private static final String XML_DATA = "HTTPSamper.xml_data"; //$NON-NLS-1$

    private static final String SOAP_ACTION = "Soap.Action"; //$NON-NLS-1$

    private static final String XML_DATA_FILE = "WebServiceSampler.xml_data_file"; //$NON-NLS-1$

    private static final String XML_PATH_LOC = "WebServiceSampler.xml_path_loc"; //$NON-NLS-1$

    private static final String MEMORY_CACHE = "WebServiceSampler.memory_cache"; //$NON-NLS-1$

    private static final String MAINTAIN_SESSION = "WebServiceSampler.maintain_session"; //$NON-NLS-1$
    
    private static final String READ_RESPONSE = "WebServiceSampler.read_response"; //$NON-NLS-1$

    private static final String USE_PROXY = "WebServiceSampler.use_proxy"; //$NON-NLS-1$

    private static final String PROXY_HOST = "WebServiceSampler.proxy_host"; //$NON-NLS-1$

    private static final String PROXY_PORT = "WebServiceSampler.proxy_port"; //$NON-NLS-1$

    private static final String WSDL_URL = "WebserviceSampler.wsdl_url"; //$NON-NLS-1$

    private static final String TIMEOUT = "WebserviceSampler.timeout"; //$NON-NLS-1$
    //- JMX file attribute names - do not change!

    private static final String PROXY_USER =
        JMeterUtils.getPropDefault(JMeter.HTTP_PROXY_USER,""); // $NON-NLS-1$

    private static final String PROXY_PASS =
        JMeterUtils.getPropDefault(JMeter.HTTP_PROXY_PASS,""); // $NON-NLS-1$

    private static final String ENCODING = "UTF-8"; // $NON-NLS-1$ TODO should this be a variable?

    public static final boolean MAINTAIN_SESSION_DEFAULT = true;

    /*
     * Random class for generating random numbers.
     */
    private final Random RANDOM = new Random();

    private String fileContents = null;

    /**
     * Set the path where XML messages are stored for random selection.
     *
     * @param path where XML messages are stored
     */
    public void setXmlPathLoc(String path) {
        setProperty(XML_PATH_LOC, path);
    }

    /**
     * Get the path where XML messages are stored. This is the directory where
     * JMeter will randomly select a file.
     *
     * @return path where XML messages are stored
     */
    public String getXmlPathLoc() {
        return getPropertyAsString(XML_PATH_LOC);
    }

    /**
     * it's kinda obvious, but we state it anyways. Set the xml file with a
     * string path.
     *
     * @param filename path to xml file
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

    /**
     * Method is used internally to check if a random file should be used for
     * the message. Messages must be valid. This is one way to load test with
     * different messages. The limitation of this approach is parsing XML takes
     * CPU resources, so it could affect JMeter GUI responsiveness.
     *
     * @return String filename
     */
    protected String getRandomFileName() {
        if (this.getXmlPathLoc() != null) {
            File src = new File(this.getXmlPathLoc());
            if (src.isDirectory() && src.list() != null) {
                File [] fileList = src.listFiles(new JMeterFileFilter(new String[] { ".xml" }, false));
                File one = fileList[RANDOM.nextInt(fileList.length)];
                // return the absolutePath of the file
                return one.getAbsolutePath();
            }
            return getXmlFile();
        }
        return getXmlFile();
    }

    /**
     * Set the XML data.
     *
     * @param data xml data
     */
    public void setXmlData(String data) {
        setProperty(XML_DATA, data);
    }

    /**
     * Get the XML data as a string.
     *
     * @return String data
     */
    public String getXmlData() {
        return getPropertyAsString(XML_DATA);
    }

    /**
     * Set the soap action which should be in the form of an URN.
     *
     * @param data soap action
     */
    public void setSoapAction(String data) {
        setProperty(SOAP_ACTION, data);
    }

    /**
     * Return the soap action string.
     *
     * @return String soap action
     */
    public String getSoapAction() {
        return getPropertyAsString(SOAP_ACTION);
    }

    /**
     * Set the maintain session option.
     *
     * @param maintainSession flag whether to maintain a session
     */
    public void setMaintainSession(boolean maintainSession) {
        setProperty(MAINTAIN_SESSION, maintainSession, MAINTAIN_SESSION_DEFAULT);
    }

    /**
     * Get the maintain session option.
     *
     * @return flag whether to maintain a session
     */
    public boolean getMaintainSession() {
        return getPropertyAsBoolean(MAINTAIN_SESSION, MAINTAIN_SESSION_DEFAULT);
    }
    
    /**
     * Set the memory cache.
     *
     * @param cache flag whether to use the memory cache
     */
    public void setMemoryCache(boolean cache) {
        setProperty(MEMORY_CACHE, String.valueOf(cache));
    }

    /**
     * Get the memory cache.
     *
     * @return flag whether to use the memory cache
     */
    public boolean getMemoryCache() {
        return getPropertyAsBoolean(MEMORY_CACHE);
    }

    /**
     * Set whether the sampler should read the response or not.
     *
     * @param read
     *            flag whether the response should be read
     */
    public void setReadResponse(boolean read) {
        setProperty(READ_RESPONSE, String.valueOf(read));
    }

    /**
     * Return whether or not to read the response.
     *
     * @return flag whether the response should be read
     */
    public boolean getReadResponse() {
        return this.getPropertyAsBoolean(READ_RESPONSE);
    }

    /**
     * Set whether or not to use a proxy
     *
     * @param proxy flag whether to use a proxy
     */
    public void setUseProxy(boolean proxy) {
        setProperty(USE_PROXY, String.valueOf(proxy));
    }

    /**
     * Return whether or not to use proxy
     *
     * @return <code>true</code> if a proxy should be used
     */
    public boolean getUseProxy() {
        return this.getPropertyAsBoolean(USE_PROXY);
    }

    /**
     * Set the proxy hostname
     *
     * @param host the hostname of the proxy
     */
    public void setProxyHost(String host) {
        setProperty(PROXY_HOST, host);
    }

    /**
     * Return the proxy hostname
     *
     * @return the proxy hostname
     */
    @Override
    public String getProxyHost() {
        this.checkProxy();
        return this.getPropertyAsString(PROXY_HOST);
    }

    /**
     * Set the proxy port
     *
     * @param port the port of the proxy
     */
    public void setProxyPort(String port) {
        setProperty(PROXY_PORT, port);
    }

    /**
     * Return the proxy port
     *
     * @return the proxy port
     */
    public int getProxyPort() {
        this.checkProxy();
        return this.getPropertyAsInt(PROXY_PORT);
    }

    /**
     *
     * @param url the URL of the WSDL
     */
    public void setWsdlURL(String url) {
        this.setProperty(WSDL_URL, url);
    }

    /**
     * method returns the WSDL URL
     *
     * @return the WSDL URL
     */
    public String getWsdlURL() {
        return getPropertyAsString(WSDL_URL);
    }

    /*
     * The method will check to see if JMeter was started in NonGui mode. If it
     * was, it will try to pick up the proxy host and port values if they were
     * passed to JMeter.java.
     */
    private void checkProxy() {
        if (JMeter.isNonGUI()) {
            this.setUseProxy(true);
            // we check to see if the proxy host and port are set
            String port = this.getPropertyAsString(PROXY_PORT);
            String host = this.getPropertyAsString(PROXY_HOST);
            if (host == null || host.length() == 0) {
                // it's not set, lets check if the user passed
                // proxy host and port from command line
                host = System.getProperty("http.proxyHost");
                if (host != null) {
                    this.setProxyHost(host);
                }
            }
            if (port == null || port.length() == 0) {
                // it's not set, lets check if the user passed
                // proxy host and port from command line
                port = System.getProperty("http.proxyPort");
                if (port != null) {
                    this.setProxyPort(port);
                }
            }
        }
    }

    /*
     * This method uses Apache soap util to create the proper DOM elements.
     *
     * @return Element
     */
    private org.w3c.dom.Element createDocument() throws SAXException, IOException {
        Document doc = null;
        String next = this.getRandomFileName();//get filename or ""

        /* Note that the filename is also used as a key to the pool (if used)
        ** Documents provided in the testplan are not currently pooled, as they may change
        *  between samples.
        */

        if (next.length() > 0 && getMemoryCache()) {
            doc = DOMPool.getDocument(next);
            if (doc == null){
                doc = openDocument(next);
                if (doc != null) {// we created the document
                    DOMPool.putDocument(next, doc);
                }
            }
        } else { // Must be local content - or not using pool
            doc = openDocument(next);
        }

        if (doc == null) {
            return null;
        }
        return doc.getDocumentElement();
    }

    /**
     * Open the file and create a Document.
     *
     * @param file - input filename or empty if using data from tesplan
     * @return Document
     * @throws IOException
     * @throws SAXException
     */
    private Document openDocument(String file) throws SAXException, IOException {
        /*
         * Consider using Apache commons pool to create a pool of document
         * builders or make sure XMLParserUtils creates builders efficiently.
         */
        DocumentBuilder XDB = XMLParserUtils.getXMLDocBuilder();
        XDB.setErrorHandler(null);//Suppress messages to stdout

        Document doc = null;
        // if either a file or path location is given,
        // get the file object.
        if (file.length() > 0) {// we have a file
            if (this.getReadResponse()) {
                TextFile tfile = new TextFile(file);
                fileContents = tfile.getText();
            }
            InputStream fileInputStream = null;
            try {
                fileInputStream = new BufferedInputStream(new FileInputStream(file));
                doc = XDB.parse(fileInputStream);
            } finally {
                JOrphanUtils.closeQuietly(fileInputStream);
            }
        } else {// must be a "here" document
            fileContents = getXmlData();
            if (fileContents != null && fileContents.length() > 0) {
                doc = XDB.parse(new InputSource(new StringReader(fileContents)));
            } else {
                log.warn("No post data provided!");
            }
        }
        return doc;
    }

    /*
     * Required to satisfy HTTPSamplerBase Should not be called, as we override
     * sample()
     */

    @Override
    protected HTTPSampleResult sample(URL u, String s, boolean b, int i) {
        throw new RuntimeException("Not implemented - should not be called");
    }

    /**
     * Sample the URL using Apache SOAP driver. Implementation note for myself
     * and those that are curious. Current logic marks the end after the
     * response has been read. If read response is set to false, the buffered
     * reader will read, but do nothing with it. Essentially, the stream from
     * the server goes into the ether.
     */
    @Override
    public SampleResult sample() {
        SampleResult result = new SampleResult();
        result.setSuccessful(false); // Assume it will fail
        result.setResponseCode("000"); // ditto $NON-NLS-1$
        result.setSampleLabel(getName());
        try {
            result.setURL(this.getUrl());
            org.w3c.dom.Element rdoc = createDocument();
            if (rdoc == null) {
                throw new SOAPException("Could not create document", null);
            }
            // set the response defaults
            result.setDataEncoding(ENCODING);
            result.setContentType("text/xml"); // $NON-NLS-1$
            result.setDataType(SampleResult.TEXT);
            result.setSamplerData(fileContents);// WARNING - could be large

            Envelope msgEnv = Envelope.unmarshall(rdoc);
            result.sampleStart();
            SOAPHTTPConnection spconn = null;
            // if a blank HeaderManager exists, try to
            // get the SOAPHTTPConnection. After the first
            // request, there should be a connection object
            // stored with the cookie header info.
            if (this.getHeaderManager() != null && this.getHeaderManager().getSOAPHeader() != null) {
                spconn = (SOAPHTTPConnection) this.getHeaderManager().getSOAPHeader();
            } else {
                spconn = new SOAPHTTPConnection();
            }
            spconn.setTimeout(getTimeoutAsInt());

            // set the auth. thanks to KiYun Roe for contributing the patch
            // I cleaned up the patch slightly. 5-26-05
            if (getAuthManager() != null) {
                if (getAuthManager().getAuthForURL(getUrl()) != null) {
                    AuthManager authmanager = getAuthManager();
                    Authorization auth = authmanager.getAuthForURL(getUrl());
                    spconn.setUserName(auth.getUser());
                    spconn.setPassword(auth.getPass());
                } else {
                    log.warn("the URL for the auth was null." + " Username and password not set");
                }
            }
            // check the proxy
            String phost = "";
            int pport = 0;
            // if use proxy is set, we try to pick up the
            // proxy host and port from either the text
            // fields or from JMeterUtil if they were passed
            // from command line
            if (this.getUseProxy()) {
                if (this.getProxyHost().length() > 0 && this.getProxyPort() > 0) {
                    phost = this.getProxyHost();
                    pport = this.getProxyPort();
                } else {
                    if (System.getProperty("http.proxyHost") != null || System.getProperty("http.proxyPort") != null) {
                        phost = System.getProperty("http.proxyHost");
                        pport = Integer.parseInt(System.getProperty("http.proxyPort"));
                    }
                }
                // if for some reason the host is blank and the port is
                // zero, the sampler will fail silently
                if (phost.length() > 0 && pport > 0) {
                    spconn.setProxyHost(phost);
                    spconn.setProxyPort(pport);
                    if (PROXY_USER.length()>0 && PROXY_PASS.length()>0){
                        spconn.setProxyUserName(PROXY_USER);
                        spconn.setProxyPassword(PROXY_PASS);
                    }
                }
            }
            
            HeaderManager headerManager = this.getHeaderManager();
            Hashtable<String,String> reqHeaders = null;
            if(headerManager != null) {
                int size = headerManager.getHeaders().size();
                reqHeaders = new Hashtable<String, String>(size);
                for (int i = 0; i < size; i++) {
                    Header header = headerManager.get(i);
                    reqHeaders.put(header.getName(), header.getValue());
                }         
            }
            spconn.setMaintainSession(getMaintainSession());
            spconn.send(this.getUrl(), this.getSoapAction(), reqHeaders, msgEnv, 
                    null, new SOAPContext());

            @SuppressWarnings("unchecked") // API uses raw types
            final Map<String, String> headers = spconn.getHeaders();
            result.setResponseHeaders(convertSoapHeaders(headers));

            if (this.getHeaderManager() != null) {
                this.getHeaderManager().setSOAPHeader(spconn);
            }

            BufferedReader br = null;
            if (spconn.receive() != null) {
                br = spconn.receive();
                SOAPContext sc = spconn.getResponseSOAPContext();
                // Set details from the actual response
                // Needs to be done before response can be stored
                final String contentType = sc.getContentType();
                result.setContentType(contentType);
                result.setEncodingAndType(contentType);
                int length=0;
                if (getReadResponse()) {
                    StringWriter sw = new StringWriter();
                    length=IOUtils.copy(br, sw);
                    result.sampleEnd();
                    result.setResponseData(sw.toString().getBytes(result.getDataEncodingWithDefault()));
                } else {
                    // by not reading the response
                    // for real, it improves the
                    // performance on slow clients
                    length=br.read();
                    result.sampleEnd();
                    result.setResponseData(JMeterUtils.getResString("read_response_message"), null); //$NON-NLS-1$
                }
                // It is not possible to access the actual HTTP response code, so we assume no data means failure
                if (length > 0){
                    result.setSuccessful(true);
                    result.setResponseCodeOK();
                    result.setResponseMessageOK();
                } else {
                    result.setSuccessful(false);
                    result.setResponseCode("999");
                    result.setResponseMessage("Empty response");
                }
            } else {
                result.sampleEnd();
                result.setSuccessful(false);
                final String contentType = spconn.getResponseSOAPContext().getContentType();
                result.setContentType(contentType);
                result.setEncodingAndType(contentType);
                result.setResponseData(spconn.getResponseSOAPContext().toString().getBytes(result.getDataEncodingWithDefault()));
            }
            if (br != null) {
                br.close();
            }
            // reponse code doesn't really apply, since
            // the soap driver doesn't provide a
            // response code
        } catch (IllegalArgumentException exception){
            String message = exception.getMessage();
            log.warn(message);
            result.setResponseMessage(message);
        } catch (SAXException exception) {
            log.warn(exception.toString());
            result.setResponseMessage(exception.getMessage());
        } catch (SOAPException exception) {
            log.warn(exception.toString());
            result.setResponseMessage(exception.getMessage());
        } catch (MalformedURLException exception) {
            String message = exception.getMessage();
            log.warn(message);
            result.setResponseMessage(message);
        } catch (IOException exception) {
            String message = exception.getMessage();
            log.warn(message);
            result.setResponseMessage(message);
        } catch (NoClassDefFoundError error){
            log.error("Missing class: ",error);
            result.setResponseMessage(error.toString());
        } catch (Exception exception) {
            if ("javax.mail.MessagingException".equals(exception.getClass().getName())){
                log.warn(exception.toString());
                result.setResponseMessage(exception.getMessage());
            } else {
                log.error("Problem processing the SOAP request", exception);
                result.setResponseMessage(exception.toString());
            }
        } finally {
            // Make sure the sample start time and sample end time are recorded
            // in order not to confuse the statistics calculation methods: if
            //  an error occurs and an exception is thrown it is possible that
            // the result.sampleStart() or result.sampleEnd() won't be called
            if (result.getStartTime() == 0)
            {
                result.sampleStart();
            }
            if (result.getEndTime() == 0)
            {
                result.sampleEnd();
            }
        }
        return result;
    }

    /**
     * We override this to prevent the wrong encoding and provide no
     * implementation. We want to reuse the other parts of HTTPSampler, but not
     * the connection. The connection is handled by the Apache SOAP driver.
     */
    @Override
    public void addEncodedArgument(String name, String value, String metaData) {
    }

    public String convertSoapHeaders(Map<String, String> ht) {
        StringBuilder buf = new StringBuilder();
        for (Entry<String, String> entry : ht.entrySet()) {
            buf.append(entry.getKey()).append("=").append(entry.getValue()).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return buf.toString();
    }

//    /**
//     * Process headerLines
//     * @param en enumeration of Strings
//     * @return String containing the lines
//     */
//    private String convertSoapHeaders(Enumeration en) {
//        StringBuilder buf = new StringBuilder(100);
//        while (en.hasMoreElements()) {
//            buf.append(en.nextElement()).append("\n"); //$NON-NLS-1$
//        }
//        return buf.toString();
//    }

    public String getTimeout() {
        return getPropertyAsString(TIMEOUT);
    }

    public int getTimeoutAsInt() {
        return getPropertyAsInt(TIMEOUT);
    }

    public void setTimeout(String text) {
        setProperty(TIMEOUT, text);
    }
    
    /* (non-Javadoc)
     * @see org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase#testEnded()
     */
    @Override
    public void testEnded() {
        DOMPool.clear();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase#testEnded(java.lang.String)
     */
    @Override
    public void testEnded(String host) {
        testEnded();
    }

}
