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
 */
package org.apache.jmeter.protocol.http.sampler;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.parser.HTMLParseException;
import org.apache.jmeter.protocol.http.parser.HTMLParser;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Common constants and methods for HTTP samplers
 * 
 */
public abstract class HTTPSamplerBase extends AbstractSampler implements TestListener {

    private static final Logger log = LoggingManager.getLoggerForClass();

	public static final int DEFAULT_HTTPS_PORT = 443;

	public static final int    DEFAULT_HTTP_PORT = 80;
    public static final String DEFAULT_HTTP_PORT_STRING = "80"; // $NON-NLS-1$

	public final static String ARGUMENTS = "HTTPsampler.Arguments"; // $NON-NLS-1$

	public final static String AUTH_MANAGER = "HTTPSampler.auth_manager"; // $NON-NLS-1$

	public final static String COOKIE_MANAGER = "HTTPSampler.cookie_manager"; // $NON-NLS-1$

	public final static String HEADER_MANAGER = "HTTPSampler.header_manager"; // $NON-NLS-1$

	public final static String MIMETYPE = "HTTPSampler.mimetype"; // $NON-NLS-1$

	public final static String DOMAIN = "HTTPSampler.domain"; // $NON-NLS-1$

	public final static String PORT = "HTTPSampler.port"; // $NON-NLS-1$

	public final static String METHOD = "HTTPSampler.method"; // $NON-NLS-1$

	public final static String CONTENT_ENCODING = "HTTPSampler.contentEncoding"; // $NON-NLS-1$

    public final static String IMPLEMENTATION = "HTTPSampler.implementation"; // $NON-NLS-1$

    public final static String PATH = "HTTPSampler.path"; // $NON-NLS-1$

	public final static String FOLLOW_REDIRECTS = "HTTPSampler.follow_redirects"; // $NON-NLS-1$

	public final static String AUTO_REDIRECTS = "HTTPSampler.auto_redirects"; // $NON-NLS-1$

	public final static String PROTOCOL = "HTTPSampler.protocol"; // $NON-NLS-1$

    public static final String PROTOCOL_HTTP = "http"; // $NON-NLS-1$

    public static final String PROTOCOL_HTTPS = "https"; // $NON-NLS-1$

    private static final String PROTOCOL_FILE = "file"; // $NON-NLS-1$

    public final static String DEFAULT_PROTOCOL = PROTOCOL_HTTP;

	public final static String URL = "HTTPSampler.URL"; // $NON-NLS-1$

    public final static String HEAD = "HEAD"; // $NON-NLS-1$
    
	public final static String POST = "POST"; // $NON-NLS-1$

    public final static String PUT = "PUT"; // $NON-NLS-1$

	public final static String GET = "GET"; // $NON-NLS-1$

    public final static String OPTIONS = "OPTIONS"; // $NON-NLS-1$
    public final static String TRACE = "TRACE"; // $NON-NLS-1$
    public final static String DELETE = "DELETE"; // $NON-NLS-1$

    public final static String DEFAULT_METHOD = "GET"; // $NON-NLS-1$
    // Supported methods:
    private final static String [] METHODS = {
        DEFAULT_METHOD, // i.e. GET
        POST,
        HEAD,
        PUT,
        OPTIONS,
        TRACE,
        DELETE,
        };
    
    public final static List METHODLIST = Collections.unmodifiableList(Arrays.asList(METHODS));

    
	public final static String USE_KEEPALIVE = "HTTPSampler.use_keepalive"; // $NON-NLS-1$

	public final static String FILE_NAME = "HTTPSampler.FILE_NAME"; // $NON-NLS-1$
    
    public final static String DO_MULTIPART_POST = "HTTPSampler.DO_MULTIPART_POST"; // $NON-NLS-1$

    /* Shown as Parameter Name on the GUI */
	public final static String FILE_FIELD = "HTTPSampler.FILE_FIELD"; // $NON-NLS-1$

	public final static String FILE_DATA = "HTTPSampler.FILE_DATA"; // $NON-NLS-1$

	public final static String FILE_MIMETYPE = "HTTPSampler.FILE_MIMETYPE"; // $NON-NLS-1$

	public final static String CONTENT_TYPE = "HTTPSampler.CONTENT_TYPE"; // $NON-NLS-1$

	public final static String NORMAL_FORM = "normal_form"; // $NON-NLS-1$

	public final static String MULTIPART_FORM = "multipart_form"; // $NON-NLS-1$

	// public final static String ENCODED_PATH= "HTTPSampler.encoded_path";
	
	// IMAGE_PARSER now really means EMBEDDED_PARSER
	public final static String IMAGE_PARSER = "HTTPSampler.image_parser"; // $NON-NLS-1$
	
	// Embedded URLs must match this RE (if provided)
	public final static String EMBEDDED_URL_RE = "HTTPSampler.embedded_url_re"; // $NON-NLS-1$

	public final static String MONITOR = "HTTPSampler.monitor"; // $NON-NLS-1$

	/** A number to indicate that the port has not been set. * */
	public static final int UNSPECIFIED_PORT = 0;
    public static final String UNSPECIFIED_PORT_AS_STRING = "0";

	protected final static String NON_HTTP_RESPONSE_CODE = "Non HTTP response code";

	protected final static String NON_HTTP_RESPONSE_MESSAGE = "Non HTTP response message";

    private static final String ARG_VAL_SEP = "="; // $NON-NLS-1$

    private static final String QRY_SEP = "&"; // $NON-NLS-1$

    private static final String QRY_PFX = "?"; // $NON-NLS-1$

    protected static final int MAX_REDIRECTS = JMeterUtils.getPropDefault("httpsampler.max_redirects", 5); // $NON-NLS-1$

    protected static final int MAX_FRAME_DEPTH = JMeterUtils.getPropDefault("httpsampler.max_frame_depth", 5); // $NON-NLS-1$

    protected static final String HEADER_AUTHORIZATION = "Authorization"; // $NON-NLS-1$

    protected static final String HEADER_COOKIE = "Cookie"; // $NON-NLS-1$

    protected static final String HEADER_CONNECTION = "Connection"; // $NON-NLS-1$

    protected static final String CONNECTION_CLOSE = "close"; // $NON-NLS-1$

    protected static final String KEEP_ALIVE = "keep-alive"; // $NON-NLS-1$

    protected static final String TRANSFER_ENCODING = "transfer-encoding";

    protected static final String HEADER_CONTENT_ENCODING = "content-encoding";

    protected static final String HTTP_1_1 = "HTTP/1.1"; // $NON-NLS-1$

    protected static final String HEADER_SET_COOKIE = "set-cookie"; // $NON-NLS-1$

    protected static final String ENCODING_GZIP = "gzip"; // $NON-NLS-1$

    protected static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition"; // $NON-NLS-1$

    protected static final String HEADER_CONTENT_TYPE = "Content-Type"; // $NON-NLS-1$

    protected static final String HEADER_CONTENT_LENGTH = "Content-Length"; // $NON-NLS-1$

    protected static final String HEADER_LOCATION = "Location"; // $NON-NLS-1$

	public static final String APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded"; // $NON-NLS-1$
    
    public static final String MULTIPART_FORM_DATA = "multipart/form-data"; // $NON-NLS-1$
    
    // Derive the mapping of content types to parsers
    private static Map parsersForType = new HashMap();
    // Not synch, but it is not modified after creation
    
    private static final String RESPONSE_PARSERS= // list of parsers
        JMeterUtils.getProperty("HTTPResponse.parsers");//$NON-NLS-1$

    static{
        String []parsers = JOrphanUtils.split(RESPONSE_PARSERS, " " , true);// returns empty array for null
        for (int i=0;i<parsers.length;i++){
            final String parser = parsers[i];
            String classname=JMeterUtils.getProperty(parser+".className");//$NON-NLS-1$
            if (classname == null){
                log.info("Cannot find .className property for "+parser+", using default");
                classname="";
            }
            String typelist=JMeterUtils.getProperty(parser+".types");//$NON-NLS-1$
            if (typelist != null){
                String []types=JOrphanUtils.split(typelist, " " , true);
                for (int j=0;j<types.length;j++){
                    final String type = types[j];
                    log.info("Parser for "+type+" is "+classname);
                    parsersForType.put(type,classname);
                }
            } else {
                log.warn("Cannot find .types property for "+parser);
            }
        }
        if (parsers.length==0){ // revert to previous behaviour
        	parsersForType.put("text/html", ""); //$NON-NLS-1$ //$NON-NLS-2$
        	log.info("No response parsers defined: text/html only will be scanned for embedded resources");
        }
    }
    
    ////////////////////// Variables //////////////////////
    
    private boolean dynamicPath = false;// Set false if spaces are already encoded

    ////////////////////// Code ///////////////////////////
    
    public HTTPSamplerBase() {
		setArguments(new Arguments());
	}

    /**
     * The name parameter to be applied to the file
     */
	public void setFileField(String value) {
		setProperty(FILE_FIELD, value);
	}

    /**
     * The name parameter to be applied to the file
     */
	public String getFileField() {
		return getPropertyAsString(FILE_FIELD);
	}

    /**
     * The actual name of the file to POST
     */
	public void setFilename(String value) {
		setProperty(FILE_NAME, value);
	}

    /**
     * The actual name of the file to POST
     */
	public String getFilename() {
		return getPropertyAsString(FILE_NAME);
	}

	/**
	 * Determine if the file should be sent as the entire Post body,
	 * i.e. without any additional wrapping
	 * 
	 * @return true if specified file is to be sent as the body,
	 * i.e. FileField is blank
	 */
	public boolean getSendFileAsPostBody() {
        // If no file field is specified, the file is sent as post body
		return getFileField().length()== 0 && getFilename().length() > 0;
	}
    
    /**
     * Determine if none of the parameters have a name, and if that
     * is the case, it means that the parameter values should be sent
     * as the post body
     * 
     * @return true if none of the parameters have a name specified
     */
    public boolean getSendParameterValuesAsPostBody() {
        boolean noArgumentsHasName = true;
        PropertyIterator args = getArguments().iterator();
        while (args.hasNext()) {
            HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
            if(arg.getName() != null && arg.getName().length() > 0) {
                noArgumentsHasName = false;
                break;
            }
        }
        return noArgumentsHasName;
    }

    /**
     * Determine if we should use multipart/form-data or 
     * application/x-www-form-urlencoded for the post
     * 
     * @return true if multipart/form-data should be used
     */
    public boolean getUseMultipartForPost(){
        // We use multipart if we have been told so, or files are present
        // and the files should not be send as the post body
        if(getDoMultipartPost() || (hasUploadableFiles() && !getSendFileAsPostBody())) {
            return true;
        }
        else {
            return false;
        }
    }

	public void setProtocol(String value) {
		setProperty(PROTOCOL, value.toLowerCase());
	}

	public String getProtocol() {
		String protocol = getPropertyAsString(PROTOCOL);
		if (protocol == null || protocol.length() == 0 ) {
			return DEFAULT_PROTOCOL;
		}
		return protocol;
	}

	/**
	 * Sets the Path attribute of the UrlConfig object Also calls parseArguments
	 * to extract and store any query arguments
	 * 
	 * @param path
	 *            The new Path value
	 */
	public void setPath(String path) {
		if (GET.equals(getMethod())) {
			int index = path.indexOf(QRY_PFX);
			if (index > -1) {
				setProperty(PATH, path.substring(0, index));
				parseArguments(path.substring(index + 1));
			} else {
				setProperty(PATH, path);
			}
		} else {
			setProperty(PATH, path);
		}
	}

	public String getPath() {
		String p = getPropertyAsString(PATH);
		if (dynamicPath) {
			return encodeSpaces(p);
		}
		return p;
	}

	public void setFollowRedirects(boolean value) {
		setProperty(new BooleanProperty(FOLLOW_REDIRECTS, value));
	}

	public boolean getFollowRedirects() {
		return getPropertyAsBoolean(FOLLOW_REDIRECTS);
	}

    public void setAutoRedirects(boolean value) {
        setProperty(new BooleanProperty(AUTO_REDIRECTS, value));
    }

    public boolean getAutoRedirects() {
        return getPropertyAsBoolean(AUTO_REDIRECTS);
    }

	public void setMethod(String value) {
		setProperty(METHOD, value);
	}

	public String getMethod() {
		return getPropertyAsString(METHOD);
	}

	public void setContentEncoding(String value) {
		setProperty(CONTENT_ENCODING, value);
	}

	public String getContentEncoding() {
		return getPropertyAsString(CONTENT_ENCODING);
	}

	public void setUseKeepAlive(boolean value) {
		setProperty(new BooleanProperty(USE_KEEPALIVE, value));
	}

	public boolean getUseKeepAlive() {
		return getPropertyAsBoolean(USE_KEEPALIVE);
	}

    public void setDoMultipartPost(boolean value) {
        setProperty(new BooleanProperty(DO_MULTIPART_POST, value));
    }

    public boolean getDoMultipartPost() {
    	// TODO - Maybe provide a setting in the properties file
    	// to control the default value for this property
        return getPropertyAsBoolean(DO_MULTIPART_POST, false);
    }

	public void setMonitor(String value) {
		this.setProperty(MONITOR, value);
	}

    public void setMonitor(boolean truth) {
        this.setProperty(MONITOR, truth);
    }

	public String getMonitor() {
		return this.getPropertyAsString(MONITOR);
	}

	public boolean isMonitor() {
		return this.getPropertyAsBoolean(MONITOR);
	}

    public void setImplementation(String value) {
        this.setProperty(IMPLEMENTATION, value);
    }

    public String getImplementation() {
        return this.getPropertyAsString(IMPLEMENTATION);
    }

    /**
     * Add an argument which has already been encoded
     */
    public void addEncodedArgument(String name, String value) {
        this.addEncodedArgument(name, value, ARG_VAL_SEP);
    }

	public void addEncodedArgument(String name, String value, String metaData) {
        if (log.isDebugEnabled()){
		    log.debug("adding argument: name: " + name + " value: " + value + " metaData: " + metaData);
        }

        
		HTTPArgument arg = new HTTPArgument(name, value, metaData, true);

		if (arg.getName().equals(arg.getEncodedName()) && arg.getValue().equals(arg.getEncodedValue())) {
			arg.setAlwaysEncoded(false);
		}
		this.getArguments().addArgument(arg);
	}
    
    public void addNonEncodedArgument(String name, String value, String metadata) {
        HTTPArgument arg = new HTTPArgument(name, value, metadata, false);
        arg.setAlwaysEncoded(false);
        this.getArguments().addArgument(arg);
    }
    
	public void addArgument(String name, String value) {
		this.getArguments().addArgument(new HTTPArgument(name, value));
	}

	public void addArgument(String name, String value, String metadata) {
		this.getArguments().addArgument(new HTTPArgument(name, value, metadata));
	}

	public void addTestElement(TestElement el) {
		if (el instanceof CookieManager) {
			setCookieManager((CookieManager) el);
		} else if (el instanceof HeaderManager) {
			setHeaderManager((HeaderManager) el);
		} else if (el instanceof AuthManager) {
			setAuthManager((AuthManager) el);
		} else {
			super.addTestElement(el);
		}
	}

	public void setPort(int value) {
		setProperty(new IntegerProperty(PORT, value));
	}

    public static int getDefaultPort(String protocol,int port){
        if (port==-1){
            return 
                protocol.equalsIgnoreCase(PROTOCOL_HTTP)  ? DEFAULT_HTTP_PORT :
                protocol.equalsIgnoreCase(PROTOCOL_HTTPS) ? DEFAULT_HTTPS_PORT :
                    port;
        }
        return port;
    }

    /**
     * Tell whether the default port for the specified protocol is used
     * 
     * @return true if the default port number for the protocol is used, false otherwise
     */
    public boolean isProtocolDefaultPort() {
    	final int port = getPropertyAsInt(PORT);
        final String protocol = getProtocol();
		if (port == UNSPECIFIED_PORT || 
                (PROTOCOL_HTTP.equalsIgnoreCase(protocol) && port == DEFAULT_HTTP_PORT) ||
                (PROTOCOL_HTTPS.equalsIgnoreCase(protocol) && port == DEFAULT_HTTPS_PORT)) {
            return true;
        }
        else {
            return false;
        }
    }
    
	public int getPort() {
		int port = getPropertyAsInt(PORT);
		if (port == UNSPECIFIED_PORT) {
			String prot = getProtocol();
            if (PROTOCOL_HTTPS.equalsIgnoreCase(prot)) {
				return DEFAULT_HTTPS_PORT;
			}
            if (!PROTOCOL_HTTP.equalsIgnoreCase(prot)) {
                log.warn("Unexpected protocol: "+prot);
                // TODO - should this return something else?
            }
			return DEFAULT_HTTP_PORT;
		}
		return port;
	}

	public void setDomain(String value) {
		setProperty(DOMAIN, value);
	}

	public String getDomain() {
		return getPropertyAsString(DOMAIN);
	}

	public void setArguments(Arguments value) {
		setProperty(new TestElementProperty(ARGUMENTS, value));
	}

	public Arguments getArguments() {
		return (Arguments) getProperty(ARGUMENTS).getObjectValue();
	}

	public void setAuthManager(AuthManager value) {
		AuthManager mgr = getAuthManager();
		if (mgr != null) {
			log.warn("Existing Manager " + mgr.getName() + " superseded by " + value.getName());
		}
		setProperty(new TestElementProperty(AUTH_MANAGER, value));
	}

	public AuthManager getAuthManager() {
		return (AuthManager) getProperty(AUTH_MANAGER).getObjectValue();
	}

	public void setHeaderManager(HeaderManager value) {
		HeaderManager mgr = getHeaderManager();
		if (mgr != null) {
			log.warn("Existing Manager " + mgr.getName() + " superseded by " + value.getName());
		}
		setProperty(new TestElementProperty(HEADER_MANAGER, value));
	}

	public HeaderManager getHeaderManager() {
		return (HeaderManager) getProperty(HEADER_MANAGER).getObjectValue();
	}

	public void setCookieManager(CookieManager value) {
		CookieManager mgr = getCookieManager();
		if (mgr != null) {
			log.warn("Existing Manager " + mgr.getName() + " superseded by " + value.getName());
		}
		setProperty(new TestElementProperty(COOKIE_MANAGER, value));
	}

	public CookieManager getCookieManager() {
		return (CookieManager) getProperty(COOKIE_MANAGER).getObjectValue();
	}

	public void setMimetype(String value) {
		setProperty(MIMETYPE, value);
	}

	public String getMimetype() {
		return getPropertyAsString(MIMETYPE);
	}

	public boolean isImageParser() {
		return getPropertyAsBoolean(IMAGE_PARSER);
	}

	public void setImageParser(boolean parseImages) {
		setProperty(new BooleanProperty(IMAGE_PARSER, parseImages));
	}

	/**
	 * Get the regular expression URLs must match.
	 * 
	 * @return regular expression (or empty) string
	 */
	public String getEmbeddedUrlRE() {
		return getPropertyAsString(EMBEDDED_URL_RE,"");
	}

	public void setEmbeddedUrlRE(String regex) {
		setProperty(new StringProperty(EMBEDDED_URL_RE, regex));
	}

	/**
	 * Obtain a result that will help inform the user that an error has occured
	 * during sampling, and how long it took to detect the error.
	 * 
	 * @param e
	 *            Exception representing the error.
	 * @param current
	 *            SampleResult
	 * @return a sampling result useful to inform the user about the exception.
	 */
	protected HTTPSampleResult errorResult(Throwable e, HTTPSampleResult res) {
		res.setSampleLabel("Error");
		res.setDataType(HTTPSampleResult.TEXT);
		ByteArrayOutputStream text = new ByteArrayOutputStream(200);
		e.printStackTrace(new PrintStream(text));
		res.setResponseData(text.toByteArray());
		res.setResponseCode(NON_HTTP_RESPONSE_CODE+": "+e.getClass().getName());
		res.setResponseMessage(NON_HTTP_RESPONSE_MESSAGE+": "+e.getMessage());
		res.setSuccessful(false);
		res.setMonitor(this.isMonitor());
		return res;
	}

	/**
	 * Get the URL, built from its component parts.
	 * 
	 * @return The URL to be requested by this sampler.
	 * @throws MalformedURLException
	 */
	public URL getUrl() throws MalformedURLException {
		StringBuffer pathAndQuery = new StringBuffer(100);
		String path = this.getPath();
        if (!path.startsWith("/")){ // $NON-NLS-1$
            pathAndQuery.append("/"); // $NON-NLS-1$
        }
        pathAndQuery.append(path);

        // Add the query string if it is a HTTP GET request
        if(GET.equals(getMethod())) {
            // Get the query string encoded in specified encoding
            // If no encoding is specified by user, we will get it
            // encoded in UTF-8, which is what the HTTP spec says
            String queryString = getQueryString(getContentEncoding());
            if(queryString.length() > 0) {
                if (path.indexOf(QRY_PFX) > -1) {
                    pathAndQuery.append(QRY_SEP);
                } else {
                    pathAndQuery.append(QRY_PFX);
                }
                pathAndQuery.append(queryString);
            }
		}
        // If default port for protocol is used, we do not include port in URL
        if(isProtocolDefaultPort()) {
			return new URL(getProtocol(), getDomain(), pathAndQuery.toString());
		}
        else {
            return new URL(getProtocol(), getDomain(), getPort(), pathAndQuery.toString());
        }
	}

    /**
     * Gets the QueryString attribute of the UrlConfig object, using
     * UTF-8 to encode the URL
     * 
     * @return the QueryString value
     */
    public String getQueryString() {
        // We use the encoding which should be used according to the HTTP spec, which is UTF-8
        return getQueryString(EncoderCache.URL_ARGUMENT_ENCODING);
    }
    
	/**
	 * Gets the QueryString attribute of the UrlConfig object, using the
	 * specified encoding to encode the parameter values put into the URL
	 * 
	 * @param contentEncoding the encoding to use for encoding parameter values
	 * @return the QueryString value
	 */
	public String getQueryString(String contentEncoding) {
 		// Check if the sampler has a specified content encoding        
 		if(contentEncoding == null || contentEncoding.trim().length() == 0) {
 			// We use the encoding which should be used according to the HTTP spec, which is UTF-8
 			contentEncoding = EncoderCache.URL_ARGUMENT_ENCODING;
 		}
		StringBuffer buf = new StringBuffer();
		PropertyIterator iter = getArguments().iterator();
		boolean first = true;
		while (iter.hasNext()) {
			HTTPArgument item = null;
            /*
             * N.B. Revision 323346 introduced the ClassCast check, but then used iter.next()
             * to fetch the item to be cast, thus skipping the element that did not cast.
             * Reverted to work more like the original code, but with the check in place.
             * Added a warning message so can track whether it is necessary
             */
			Object objectValue = iter.next().getObjectValue();
            try {
				item = (HTTPArgument) objectValue;
			} catch (ClassCastException e) {
                log.warn("Unexpected argument type: "+objectValue.getClass().getName());
				item = new HTTPArgument((Argument) objectValue);
			}
			if (!first) {
				buf.append(QRY_SEP);
			} else {
				first = false;
			}
			buf.append(item.getEncodedName());
			if (item.getMetaData() == null) {
				buf.append(ARG_VAL_SEP);
			} else {
				buf.append(item.getMetaData());
			}
            
			// Encode the parameter value in the specified content encoding
			try {
			    buf.append(item.getEncodedValue(contentEncoding));
			}
			catch(UnsupportedEncodingException e) {
			    log.warn("Unable to encode parameter in encoding " + contentEncoding + ", parameter value not included in query string");
			}
		}
		return buf.toString();
	}

	// Mark Walsh 2002-08-03, modified to also parse a parameter name value
	// string, where string contains only the parameter name and no equal sign.
	/**
	 * This method allows a proxy server to send over the raw text from a
	 * browser's output stream to be parsed and stored correctly into the
	 * UrlConfig object.
	 * 
	 * For each name found, addArgument() is called
	 * 
	 * @param queryString -
	 *            the query string
	 * 
	 */
	public void parseArguments(String queryString) {
		String[] args = JOrphanUtils.split(queryString, QRY_SEP);
		for (int i = 0; i < args.length; i++) {
			// need to handle four cases: 
            // - string contains name=value
			// - string contains name=
			// - string contains name
			// - empty string
            
			String metaData; // records the existance of an equal sign
            String name;
            String value;
            int length = args[i].length();
            int endOfNameIndex = args[i].indexOf(ARG_VAL_SEP);
            if (endOfNameIndex != -1) {// is there a separator?
				// case of name=value, name=
				metaData = ARG_VAL_SEP;
                name = args[i].substring(0, endOfNameIndex);
                value = args[i].substring(endOfNameIndex + 1, length);
			} else {
				metaData = "";
                name=args[i];
                value="";
			}
			if (name.length() > 0) {
                // The browser has already done the encoding, so save the values as is 
                HTTPArgument arg = new HTTPArgument(name, value, metaData, false);
                // and make sure they stay that way:
                arg.setAlwaysEncoded(false);
                // Note that URL.encode()/decode() do not follow RFC3986 entirely
				this.getArguments().addArgument(arg);
				// TODO: this leaves the arguments in encoded form, which may be difficult to read
                // if we can find proper coding methods, this could be tidied up 
            }
		}
	}

	public String toString() {
		try {
			return this.getUrl().toString() + ((POST.equals(getMethod())) ? "\nQuery Data: " + getQueryString() : "");
		} catch (MalformedURLException e) {
			return "";
		}
	}

	/**
	 * Do a sampling and return its results.
	 * 
	 * @param e
	 *            <code>Entry</code> to be sampled
	 * @return results of the sampling
	 */
	public SampleResult sample(Entry e) {
		return sample();
	}

	/**
	 * Perform a sample, and return the results
	 * 
	 * @return results of the sampling
	 */
	public SampleResult sample() {
		SampleResult res = null;
		try {
			if (PROTOCOL_FILE.equalsIgnoreCase(getProtocol())){
				res = fileSample(new URI(PROTOCOL_FILE,getPath(),null));
			} else {
			    res = sample(getUrl(), getMethod(), false, 0);
			}
			res.setSampleLabel(getName());
			return res;
		} catch (MalformedURLException e) {
			return errorResult(e, new HTTPSampleResult());
		} catch (IOException e) {
			return errorResult(e, new HTTPSampleResult());
		} catch (URISyntaxException e) {
			return errorResult(e, new HTTPSampleResult());
		}
	}

	private HTTPSampleResult fileSample(URI uri) throws IOException {

		String urlStr = uri.toString();

		
		HTTPSampleResult res = new HTTPSampleResult();
		res.setMonitor(isMonitor());
		res.setHTTPMethod(GET); // Dummy
		res.setURL(new URL(urlStr));
		res.setSampleLabel(urlStr);
		FileReader reader = null;
		res.sampleStart();
		try {
			byte[] responseData;
			StringBuffer ctb=new StringBuffer("text/html"); // $NON-NLS-1$
			reader = new FileReader(getPath());
			String contentEncoding = getContentEncoding();
			if (contentEncoding.length() == 0) {
				responseData = IOUtils.toByteArray(reader);
			} else {
				ctb.append("; charset="); // $NON-NLS-1$
				ctb.append(contentEncoding);
				responseData = IOUtils.toByteArray(reader,contentEncoding);				
			}
			res.sampleEnd();			
			res.setResponseData(responseData);
		    res.setResponseCodeOK();
			res.setResponseMessage(""); // TODO - what should this be?
			res.setSuccessful(true);
			String ct = ctb.toString();
			res.setContentType(ct);
            res.setEncodingAndType(ct);
		} finally {
			IOUtils.closeQuietly(reader);
		}

		//res.setResponseHeaders("");

		return res;
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
	protected abstract HTTPSampleResult sample(URL u, 
            String method, boolean areFollowingRedirect, int depth);

	/**
	 * Download the resources of an HTML page.
	 * <p>
	 * If createContainerResult is true, the returned result will contain one
	 * subsample for each request issued, including the original one that was
	 * passed in. It will otherwise look exactly like that original one.
	 * <p>
	 * If createContainerResult is false, one subsample will be added to the
	 * provided result for each requests issued.
	 * 
	 * @param res
	 *            result of the initial request - must contain an HTML response
	 * @param container
	 *            for storing the results
	 * @param frameDepth
	 *            Depth of this target in the frame structure. Used only to
	 *            prevent infinite recursion.
	 * @return "Container" result with one subsample per request issued
	 */
	protected HTTPSampleResult downloadPageResources(HTTPSampleResult res, HTTPSampleResult container, int frameDepth) {
		Iterator urls = null;
		try {
			final byte[] responseData = res.getResponseData();
            if (responseData.length > 0){  // Bug 39205
                String parserName = getParserClass(res);
                if(parserName != null)
                {
    				final HTMLParser parser =
                        parserName.length() > 0 ? // we have a name
                        HTMLParser.getParser(parserName) 
                        : 
                        HTMLParser.getParser(); // we don't; use the default parser
                    urls = parser.getEmbeddedResourceURLs(responseData, res.getURL());
    			} 
            }
		} catch (HTMLParseException e) {
			// Don't break the world just because this failed:
			res.addSubResult(errorResult(e, res));
			res.setSuccessful(false);
		}

		// Iterate through the URLs and download each image:
		if (urls != null && urls.hasNext()) {
			if (container == null) {
				res = new HTTPSampleResult(res);
			} else {
				res = container;
			}

			// Get the URL matcher
			String re=getEmbeddedUrlRE();
			Perl5Matcher localMatcher = null;
			Pattern pattern = null;
			if (re.length()>0){
				try {
				    pattern = JMeterUtils.getPattern(re);
					localMatcher = JMeterUtils.getMatcher();// don't fetch unless pattern compiles
				} catch (MalformedCachePatternException e) {
					log.warn("Ignoring embedded URL match string: "+e.getMessage());
				}
			}
			while (urls.hasNext()) {
				Object binURL = urls.next();
				try {
					URL url = (URL) binURL;
                    String urlstr = url.toString();
                    String urlStrEnc=encodeSpaces(urlstr);
                    if (!urlstr.equals(urlStrEnc)){// There were some spaces in the URL
                        try {
                            url = new URL(urlStrEnc);
                        } catch (MalformedURLException e) {
                            res.addSubResult(errorResult(new Exception(urlStrEnc + " is not a correct URI"), res));
                            res.setSuccessful(false);
                            continue;
                        }
                    }
                    if (pattern != null && !localMatcher.matches(urlStrEnc, pattern)) {
                    	continue; // we have a pattern and the URL does not match, so skip it
                    }
                    HTTPSampleResult binRes = sample(url, GET, false, frameDepth + 1);
					res.addSubResult(binRes);
					res.setSuccessful(res.isSuccessful() && binRes.isSuccessful());
				} catch (ClassCastException e) {
					res.addSubResult(errorResult(new Exception(binURL + " is not a correct URI"), res));
					res.setSuccessful(false);
					continue;
                }
			}
		}
		return res;
	}

    /*
     * @param res HTTPSampleResult to check
     * @return parser class name (may be "") or null if entry does not exist
     */
    private String getParserClass(HTTPSampleResult res) {
        final String ct = res.getMediaType();
        return (String)parsersForType.get(ct);
    }

    // TODO: make static?
	protected String encodeSpaces(String path) {
        return JOrphanUtils.replaceAllChars(path, ' ', "%20"); // $NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testEnded()
	 */
	public void testEnded() {
		dynamicPath = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testEnded(java.lang.String)
	 */
	public void testEnded(String host) {
		testEnded();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testIterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
	 */
	public void testIterationStart(LoopIterationEvent event) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testStarted()
	 */
	public void testStarted() {
		JMeterProperty pathP = getProperty(PATH);
		log.debug("path property is a " + pathP.getClass().getName());
		log.debug("path beginning value = " + pathP.getStringValue());
		if (pathP instanceof StringProperty && !"".equals(pathP.getStringValue())) {
			log.debug("Encoding spaces in path");
			pathP.setObjectValue(encodeSpaces(pathP.getStringValue()));
            dynamicPath = false;
		} else {
			log.debug("setting dynamic path to true");
			dynamicPath = true;
		}
		log.debug("path ending value = " + pathP.getStringValue());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.testelement.TestListener#testStarted(java.lang.String)
	 */
	public void testStarted(String host) {
		testStarted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	public Object clone() {
		HTTPSamplerBase base = (HTTPSamplerBase) super.clone();
		base.dynamicPath = dynamicPath;
		return base;
	}

	/**
	 * Iteratively download the redirect targets of a redirect response.
	 * <p>
	 * The returned result will contain one subsample for each request issued,
	 * including the original one that was passed in. It will be an
	 * HTTPSampleResult that should mostly look as if the final destination of
	 * the redirect chain had been obtained in a single shot.
	 * 
	 * @param res
	 *            result of the initial request - must be a redirect response
	 * @param frameDepth
	 *            Depth of this target in the frame structure. Used only to
	 *            prevent infinite recursion.
	 * @return "Container" result with one subsample per request issued
	 */
	protected HTTPSampleResult followRedirects(HTTPSampleResult res, int frameDepth) {
		HTTPSampleResult totalRes = new HTTPSampleResult(res);
		HTTPSampleResult lastRes = res;

		int redirect;
		for (redirect = 0; redirect < MAX_REDIRECTS; redirect++) {
			boolean invalidRedirectUrl = false;
			// Browsers seem to tolerate Location headers with spaces,
			// replacing them automatically with %20. We want to emulate
			// this behaviour.
			String location = encodeSpaces(lastRes.getRedirectLocation());
			try {
				lastRes = sample(new URL(lastRes.getURL(), location), GET, true, frameDepth);
			} catch (MalformedURLException e) {
				lastRes = errorResult(e, lastRes);
				// The redirect URL we got was not a valid URL
				invalidRedirectUrl = true;
			}
			if (lastRes.getSubResults() != null && lastRes.getSubResults().length > 0) {
				SampleResult[] subs = lastRes.getSubResults();
				for (int i = 0; i < subs.length; i++) {
					totalRes.addSubResult(subs[i]);
				}
			} else {
				// Only add sample if it is a sample of valid url redirect, i.e. that
				// we have actually sampled the URL
				if(!invalidRedirectUrl) {
					totalRes.addSubResult(lastRes);
				}
			}

			if (!lastRes.isRedirect()) {
				break;
			}
		}
		if (redirect >= MAX_REDIRECTS) {
			lastRes = errorResult(new IOException("Exceeeded maximum number of redirects: " + MAX_REDIRECTS), lastRes);
			totalRes.addSubResult(lastRes);
		}

		// Now populate the any totalRes fields that need to
		// come from lastRes:
		totalRes.setSampleLabel(totalRes.getSampleLabel() + "->" + lastRes.getSampleLabel());
		// The following three can be discussed: should they be from the
		// first request or from the final one? I chose to do it this way
		// because that's what browsers do: they show the final URL of the
		// redirect chain in the location field.
		totalRes.setURL(lastRes.getURL());
		totalRes.setHTTPMethod(lastRes.getHTTPMethod());
		totalRes.setQueryString(lastRes.getQueryString());
		totalRes.setRequestHeaders(lastRes.getRequestHeaders());

		totalRes.setResponseData(lastRes.getResponseData());
		totalRes.setResponseCode(lastRes.getResponseCode());
		totalRes.setSuccessful(lastRes.isSuccessful());
		totalRes.setResponseMessage(lastRes.getResponseMessage());
		totalRes.setDataType(lastRes.getDataType());
		totalRes.setResponseHeaders(lastRes.getResponseHeaders());
        totalRes.setContentType(lastRes.getContentType());
        totalRes.setDataEncoding(lastRes.getDataEncoding());
		return totalRes;
	}

	/**
	 * Follow redirects and download page resources if appropriate. this works,
	 * but the container stuff here is what's doing it. followRedirects() is
	 * actually doing the work to make sure we have only one container to make
	 * this work more naturally, I think this method - sample() - needs to take
	 * an HTTPSamplerResult container parameter instead of a
	 * boolean:areFollowingRedirect.
	 * 
	 * @param areFollowingRedirect
	 * @param frameDepth
	 * @param res
	 * @return
	 */
	protected HTTPSampleResult resultProcessing(boolean areFollowingRedirect, int frameDepth, HTTPSampleResult res) {
		boolean wasRedirected = false;
		if (!areFollowingRedirect) {
			if (res.isRedirect()) {
				log.debug("Location set to - " + res.getRedirectLocation());

				if (getFollowRedirects()) {
					res = followRedirects(res, frameDepth);
					areFollowingRedirect = true;
					wasRedirected = true;
				}
			}
		}
		if (isImageParser() && (HTTPSampleResult.TEXT).equals(res.getDataType()) && res.isSuccessful()) {
			if (frameDepth > MAX_FRAME_DEPTH) {
				res.addSubResult(errorResult(new Exception("Maximum frame/iframe nesting depth exceeded."), res));
			} else {
				// If we followed redirects, we already have a container:
				HTTPSampleResult container = (HTTPSampleResult) (areFollowingRedirect ? res.getParent() : res);

				// Only download page resources if we were not redirected.
				// If we were redirected, the page resources have already been
				// downloaded for the sample made for the redirected url
				if(!wasRedirected) {
					res = downloadPageResources(res, container, frameDepth);
				}
			}
		}
		return res;
	}
    
    /**
     * Determine if the HTTP status code is successful or not
     * i.e. in range 200 to 399 inclusive
     * 
     * @return whether in range 200-399 or not
     */
    protected boolean isSuccessCode(int code){
        return (code >= 200 && code <= 399);
    }

    protected static String encodeBackSlashes(String value) {
    	StringBuffer newValue = new StringBuffer();
    	for (int i = 0; i < value.length(); i++) {
    		char charAt = value.charAt(i);
            if (charAt == '\\') { // $NON-NLS-1$
    			newValue.append("\\\\"); // $NON-NLS-1$
    		} else {
    			newValue.append(charAt);
    		}
    	}
    	return newValue.toString();
    }

    /**
     * Method to tell if the request has any files to be uploaded
     */
    protected boolean hasUploadableFiles() {
        return getFilename() != null && getFilename().length() > 0;        
    }

    public static String[] getValidMethodsAsArray(){
        return (String[]) METHODLIST.toArray(new String[0]);
    }

    public static boolean isSecure(String protocol){
        return PROTOCOL_HTTPS.equalsIgnoreCase(protocol);
    }
    
    public static boolean isSecure(URL url){
        return isSecure(url.getProtocol());
    }
}

