// $Header$
/*
 * Copyright 2003-2005 The Apache Software Foundation.
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
 * 
*/

package org.apache.jmeter.protocol.http.sampler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Random;
import java.util.Hashtable;

import javax.mail.MessagingException;
import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.InputSource;

import org.apache.jorphan.io.TextFile;
import org.apache.jorphan.logging.LoggingManager;

import org.apache.jmeter.gui.JMeterFileFilter;
import org.apache.jmeter.protocol.http.util.DOMPool;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Logger;
import org.apache.soap.Envelope;
import org.apache.soap.messaging.Message;
import org.apache.soap.transport.SOAPTransport;
import org.apache.soap.transport.http.SOAPHTTPConnection;
import org.apache.soap.util.xml.XMLParserUtils;
import org.apache.soap.SOAPException;
import org.w3c.dom.Document;

/**
 * Sampler to handle Web Service requests. It uses Apache SOAP drivers to
 * perform the XML generation, connection, SOAP encoding and other SOAP
 * functions.
 * <p>
 * Created on:  Jun 26, 2003
 * 
 * @version $Revision$
 */
public class WebServiceSampler extends HTTPSamplerBase
{
    private static Logger log = LoggingManager.getLoggerForClass();

    public static final String XML_DATA = "HTTPSamper.xml_data";
    public static final String SOAP_ACTION = "Soap.Action";
    public static final String XML_DATA_FILE =
        "WebServiceSampler.xml_data_file";
    public static final String XML_PATH_LOC = "WebServiceSampler.xml_path_loc";
    public static final String MEMORY_CACHE = "WebServiceSampler.memory_cache";
    public static final String READ_RESPONSE =
        "WebServiceSampler.read_response";
    public static final String USE_PROXY = "WebServiceSampler.use_proxy";
    public static final String PROXY_HOST = "WebServiceSampler.proxy_host";
    public static final String PROXY_PORT = "WebServiceSampler.proxy_port";
    public static final String WSDL_URL = "WebserviceSampler.wsdl_url";

    /**
     * size of File[] array
     */
    private int FILE_COUNT = -1;

    /**
     * List of files that have .xml extension
     */
    private File[] FILE_LIST = null;

    /**
     * Random class for generating random
     * numbers.
     */
    private Random RANDOM = new Random();

	private String FILE_CONTENTS = null;
	
    /**
     * Set the path where XML messages are stored for random selection.
     */
    public void setXmlPathLoc(String path)
    {
        setProperty(XML_PATH_LOC, path);
    }

    /**
     * Get the path where XML messages are stored. this is the directory where
     * JMeter will randomly select a file.
     */
    public String getXmlPathLoc()
    {
        return getPropertyAsString(XML_PATH_LOC);
    }

    /**
     * it's kinda obvious, but we state it anyways.  Set the xml file with a
     * string path.
     * @param filename
     */
    public void setXmlFile(String filename)
    {
        setProperty(XML_DATA_FILE, filename);
    }

    /**
     * Get the file location of the xml file.
     * @return String file path.
     */
    public String getXmlFile()
    {
        return getPropertyAsString(XML_DATA_FILE);
    }

    /**
     * Method uses jorphan TextFile class to load the contents of the file. I
     * wonder if we should cache the DOM Document to save on parsing the
     * message. Parsing XML is CPU intensive, so it could restrict the number
     * of threads a test plan can run effectively. To cache the documents, it
     * may be good to have an external class to provide caching that is
     * efficient. We could just use a HashMap, but for large tests, it will be
     * slow. Ideally, the cache would be indexed, so that large tests will
     * run efficiently.
     * @return String contents of the file
     */
    private File retrieveRuntimeXmlData()
    {
        String file = getRandomFileName();
        if (file.length() > 0)
        {
        	if (this.getReadResponse()){
        		TextFile tfile = new TextFile(file);
        		FILE_CONTENTS = tfile.getText();
        	}
        	return new File(file);
        } else {
        	return null;
        }
    }

    /**
     * Method is used internally to check if a random file should be used for
     * the message. Messages must be valid. This is one way to load test with
     * different messages. The limitation of this approach is parsing XML takes
     * CPU resources, so it could affect JMeter GUI responsiveness.
     * @return String filename
     */
    protected String getRandomFileName()
    {
        if (this.getXmlPathLoc() != null)
        {
            File src = new File(this.getXmlPathLoc());
            if (src.isDirectory() && src.list() != null)
            {
                FILE_LIST =
                    src.listFiles(
                        new JMeterFileFilter(new String[] { ".xml" }));
                this.FILE_COUNT = FILE_LIST.length;
                File one = FILE_LIST[RANDOM.nextInt(FILE_COUNT)];
                // return the absolutePath of the file
                return one.getAbsolutePath();
            }
            else
            {
                return getXmlFile();
            }
        }
        else
        {
            return getXmlFile();
        }
    }

    /**
     * Set the XML data.
     * @param data
     */
    public void setXmlData(String data)
    {
        setProperty(XML_DATA, data);
    }

    /**
     * Get the XML data as a string.
     * @return String data
     */
    public String getXmlData()
    {
        return getPropertyAsString(XML_DATA);
    }

    /**
     * Set the soap action which should be in the form of an URN.
     * @param data
     */
    public void setSoapAction(String data)
    {
        setProperty(SOAP_ACTION, data);
    }

    /**
     * Return the soap action string.
     * @return String soap action
     */
    public String getSoapAction()
    {
        return getPropertyAsString(SOAP_ACTION);
    }

    /**
     * Set the memory cache.
     * @param cache
     */
    public void setMemoryCache(boolean cache)
    {
        setProperty(MEMORY_CACHE, String.valueOf(cache));
    }

    /**
     * Get the memory cache.
     * @return boolean cache
     */
    public boolean getMemoryCache()
    {
        return getPropertyAsBoolean(MEMORY_CACHE);
    }

    /**
     * Set whether the sampler should read the response or not.
     * @param read
     */
    public void setReadResponse(boolean read)
    {
        setProperty(READ_RESPONSE, String.valueOf(read));
    }

    /**
     * Return whether or not to read the response.
     * @return boolean
     */
    public boolean getReadResponse()
    {
        return this.getPropertyAsBoolean(READ_RESPONSE);
    }

	/**
	 * Set whether or not to use a proxy
	 * @param proxy
	 */
	public void setUseProxy(boolean proxy){
		setProperty(USE_PROXY, String.valueOf(proxy));
	}

	/**
	 * Return whether or not to use proxy
	 * @return true if should use proxy
	 */	
	public boolean getUseProxy(){
		return this.getPropertyAsBoolean(USE_PROXY);
	}

	/**
	 * Set the proxy hostname
	 * @param host
	 */	
	public void setProxyHost(String host){
		setProperty(PROXY_HOST, host);
	}

	/**
	 * Return the proxy hostname
	 * @return the proxy hostname
	 */	
	public String getProxyHost(){
		this.checkProxy();
		return this.getPropertyAsString(PROXY_HOST);
	}

	/**
	 * Set the proxy port
	 * @param port
	 */	
	public void setProxyPort(String port){
		setProperty(PROXY_PORT, port);
	}

	/**
	 * Return the proxy port
	 * @return the proxy port
	 */	
	public int getProxyPort(){
		this.checkProxy();
		return this.getPropertyAsInt(PROXY_PORT);
	}
	
	/**
	 * 
	 * @param url
	 */
	public void setWsdlURL(String url){
		this.setProperty(WSDL_URL,url);
	}
	
	/**
	 * method returns the WSDL URL
	 * @return
	 */
	public String getWsdlURL(){
		return getPropertyAsString(WSDL_URL);
	}
	
	/**
	 * The method will check to see if JMeter was started
	 * in NonGui mode. If it was, it will try to pick up
	 * the proxy host and port values if they were passed
	 * to JMeter.java.
	 */
	public void checkProxy(){
		if (System.getProperty("JMeter.NonGui") != null &&
			System.getProperty("JMeter.NonGui").equals("true")){
				this.setUseProxy(true);
				// we check to see if the proxy host and port are set
				String port = this.getPropertyAsString(PROXY_PORT);
				String host = this.getPropertyAsString(PROXY_HOST);
				if (host == null || host.length() == 0){
					// it's not set, lets check if the user passed
					// proxy host and port from command line
					if (System.getProperty("http.proxyHost") != null){
						host = System.getProperty("http.proxyHost");
						this.setProxyHost(host);
					}
				}
				if (port == null || port.length() == 0){
					// it's not set, lets check if the user passed
					// proxy host and port from command line
					if (System.getProperty("http.proxyPort") != null){
						port = System.getProperty("http.proxyPort");
						this.setProxyPort(port);
					}
				}
			}
	}
	
    /**
     * This method uses Apache soap util to create the proper DOM elements.
     * @return Element
     */
    public org.w3c.dom.Element createDocument()
    {
        if (getPropertyAsBoolean(MEMORY_CACHE))
        {
            String next = this.getRandomFileName();
            if (DOMPool.getDocument(next) != null)
            {
                return DOMPool.getDocument(next).getDocumentElement();
            }
            else
            {
                return openDocument(next).getDocumentElement();
            }
        }
        else
        {
            return openDocument(null).getDocumentElement();
        }
    }

    /**
     * Open the file and create a Document.
     * @param key
     * @return Document
     */
    protected Document openDocument(String key)
    {
	    /*
	     * Consider using Apache commons pool to create a pool of document builders
	     * or make sure XMLParserUtils creates builders efficiently.
	     */
	    DocumentBuilder XDB = XMLParserUtils.getXMLDocBuilder();

		Document doc = null;
    	// if either a file or path location is given,
    	// get the file object.
    	if (getXmlFile().length() > 0 || getXmlPathLoc().length() > 0){
    		try {
				doc = XDB.parse(new FileInputStream(retrieveRuntimeXmlData()));
    		} catch (Exception e){
    			// there should be a file, if not fail silently
                log.debug(e.getMessage());
    		}
    	} else {
    		FILE_CONTENTS = getXmlData();
			if (FILE_CONTENTS != null && FILE_CONTENTS.length() > 0)
			{
				try
				{
					doc = XDB.parse(
						new InputSource(new StringReader(FILE_CONTENTS)));
				}
				catch (Exception ex)
				{
					log.debug(ex.getMessage());
				}
			}
    	}
		if (this.getPropertyAsBoolean(MEMORY_CACHE))
		{
			DOMPool.putDocument(key, doc);
		}
		return doc;
    }

	/*
	 *  Required to satisfy HTTPSamplerBase
	 *  Should not be called, as we override sample()
	 */
	
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
	public SampleResult sample() {
		SampleResult RESULT = new SampleResult();
        try
        {
			org.w3c.dom.Element rdoc = createDocument();
            Envelope msgEnv = Envelope.unmarshall(rdoc);
            // create a new message
            Message msg = new Message();
            RESULT.setURL(this.getUrl());
            RESULT.setSampleLabel(this.getUrl().toString());
            RESULT.sampleStart();
			SOAPHTTPConnection spconn = null;
			// if a blank HeaderManager exists, try to
			// get the SOAPHTTPConnection. After the first
			// request, there should be a connection object
			// stored with the cookie header info.
			if (this.getHeaderManager() != null &&
				this.getHeaderManager().getSOAPHeader() != null) {
				spconn = (SOAPHTTPConnection)this.getHeaderManager().
					getSOAPHeader();
			} else {
				spconn = new SOAPHTTPConnection();
			} 
			// check the proxy
			String phost = "";
			int pport = 0;
			// if use proxy is set, we try to pick up the
			// proxy host and port from either the text
			// fields or from JMeterUtil if they were passed
			// from command line
			if (this.getUseProxy()){
				if (this.getProxyHost().length() > 0 &&	this.getProxyPort() > 0){
					phost = this.getProxyHost();
					pport = this.getProxyPort();
				} else {
					if (System.getProperty("http.proxyHost") != null ||
						System.getProperty("http.proxyPort") != null){
							phost = System.getProperty("http.proxyHost");
							pport = Integer.parseInt(
								System.getProperty("http.proxyPort"));
						}
				}
				// if for some reason the host is blank and the port is
				// zero, the sampler will fail silently
				if (phost.length() > 0 && pport > 0){
					spconn.setProxyHost(phost);
					spconn.setProxyPort(pport);
				}
			}
			// by default we maintain the session.	
			spconn.setMaintainSession(true);
			msg.setSOAPTransport(spconn);
            msg.send(this.getUrl(), this.getSoapAction(), msgEnv);

			if (this.getHeaderManager() != null){
				this.getHeaderManager().setSOAPHeader(spconn);
			}

            SOAPTransport st = msg.getSOAPTransport();
            RESULT.setDataType(SampleResult.TEXT);
            BufferedReader br = null;
            // check to see if SOAPTransport is not nul and receive is
            // also not null. hopefully this will improve the error
            // reporting. 5/13/05 peter lin
            if (st != null && st.receive() != null){
                br = st.receive();
                if (this.getPropertyAsBoolean(READ_RESPONSE))
                {
                    StringBuffer buf = new StringBuffer();
                    String line;
                    while ((line = br.readLine()) != null)
                    {
                        buf.append(line);
                    }
                    RESULT.sampleEnd();
                    // set the response
                    RESULT.setResponseData(buf.toString().getBytes());
                }
                else
                {
                    // by not reading the response
                    // for real, it improves the
                    // performance on slow clients
                    br.read();
                    RESULT.sampleEnd();
                    RESULT.setResponseData(
                        JMeterUtils
                            .getResString("read_response_message")
                            .getBytes());
                }
                RESULT.setSuccessful(true);
                RESULT.setResponseCode("200");
                RESULT.setResponseHeaders(this.convertSoapHeaders(st.getHeaders()));
            } else {
                RESULT.setSuccessful(false);
                RESULT.setResponseData(
                        st.getResponseSOAPContext().getContentType().getBytes());
                RESULT.setResponseCode("000");
                RESULT.setResponseHeaders("error");
            }
            // 1-22-04 updated the sampler so that when read
            // response is set, it also sets SamplerData with
            // the XML message, so users can see what was
            // sent. if read response is not checked, it will
            // not set sampler data with the request message.
            // peter lin.
            RESULT.setSamplerData(
                getUrl().getProtocol()
                    + "://"
                    + getUrl().getHost()
                    + "/"
                    + getUrl().getFile()
                    + "\n"
                    + FILE_CONTENTS);
            RESULT.setDataEncoding(
                st.getResponseSOAPContext().getContentType());
            // setting this is just a formality, since
            // soap will return a descriptive error
            // message, soap errors within the response
            // are preferred.
            if (br != null){
                br.close();
            }
            msg = null;
            st = null;
            // reponse code doesn't really apply, since
            // the soap driver doesn't provide a
            // response code
        }
        catch (SOAPException exception){
            log.debug(exception.getMessage());
            RESULT.setSuccessful(false);
        }
        catch (MalformedURLException exception){
            // keep this debug, since a bad URL, means the
            // soap driver can't get to it anyways
            log.debug(exception.getMessage());
        }
        catch (IOException exception){
            // if the Webservice is unable or the stream
            // is null for some reason we can continue
            log.warn(exception.getMessage());
        }
        catch (MessagingException exception){
            // keep this one debug, since it means soap isn't
            // able to parse the document, so it can't continue
            // anyways
            log.debug(exception.getMessage());
        }
		return RESULT;
    }

    /**
     * We override this to prevent the wrong encoding and provide no
     * implementation. We want to reuse the other parts of HTTPSampler, but not
     * the connection. The connection is handled by the Apache SOAP driver.
     */
    public void addEncodedArgument(String name, String value, String metaData)
    {
    }
    
    public String convertSoapHeaders(Hashtable ht){
    	Enumeration en = ht.keys();
    	StringBuffer buf = new StringBuffer();
    	while (en.hasMoreElements()){
    		Object key = en.nextElement();
    		buf.append((String)key + "=" +
    		(String)ht.get(key) + "\n");
    	}
    	return buf.toString();
    }
}
