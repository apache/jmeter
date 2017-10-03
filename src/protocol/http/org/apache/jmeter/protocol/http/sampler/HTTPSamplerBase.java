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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.gui.Replaceable;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.CacheManager;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.parser.BaseParser;
import org.apache.jmeter.protocol.http.parser.LinkExtractorParseException;
import org.apache.jmeter.protocol.http.parser.LinkExtractorParser;
import org.apache.jmeter.protocol.http.sampler.ResourcesDownloader.AsynSamplerResultHolder;
import org.apache.jmeter.protocol.http.util.ConversionUtils;
import org.apache.jmeter.protocol.http.util.DirectAccessByteArrayOutputStream;
import org.apache.jmeter.protocol.http.util.EncoderCache;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.HTTPConstantsInterface;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.protocol.http.util.HTTPFileArgs;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestIterationListener;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common constants and methods for HTTP samplers
 *
 */
public abstract class HTTPSamplerBase extends AbstractSampler
    implements TestStateListener, TestIterationListener, ThreadListener, HTTPConstantsInterface,
        Replaceable {

    private static final long serialVersionUID = 242L;

    private static final Logger log = LoggerFactory.getLogger(HTTPSamplerBase.class);

    private static final Set<String> APPLIABLE_CONFIG_CLASSES = new HashSet<>(
            Arrays.asList(
                    "org.apache.jmeter.config.gui.LoginConfigGui",
                    "org.apache.jmeter.protocol.http.config.gui.HttpDefaultsGui",
                    "org.apache.jmeter.config.gui.SimpleConfigGui",
                    "org.apache.jmeter.protocol.http.gui.HeaderPanel",
                    "org.apache.jmeter.protocol.http.control.DNSCacheManager",
                    "org.apache.jmeter.protocol.http.gui.DNSCachePanel",
                    "org.apache.jmeter.protocol.http.gui.AuthPanel",
                    "org.apache.jmeter.protocol.http.gui.CacheManagerGui",
                    "org.apache.jmeter.protocol.http.gui.CookiePanel"
            ));

    //+ JMX names - do not change
    public static final String ARGUMENTS = "HTTPsampler.Arguments"; // $NON-NLS-1$

    public static final String AUTH_MANAGER = "HTTPSampler.auth_manager"; // $NON-NLS-1$

    public static final String COOKIE_MANAGER = "HTTPSampler.cookie_manager"; // $NON-NLS-1$

    public static final String CACHE_MANAGER = "HTTPSampler.cache_manager"; // $NON-NLS-1$

    public static final String HEADER_MANAGER = "HTTPSampler.header_manager"; // $NON-NLS-1$

    public static final String DNS_CACHE_MANAGER = "HTTPSampler.dns_cache_manager"; // $NON-NLS-1$

    public static final String DOMAIN = "HTTPSampler.domain"; // $NON-NLS-1$

    public static final String PORT = "HTTPSampler.port"; // $NON-NLS-1$

    public static final String PROXYHOST = "HTTPSampler.proxyHost"; // $NON-NLS-1$

    public static final String PROXYPORT = "HTTPSampler.proxyPort"; // $NON-NLS-1$

    public static final String PROXYUSER = "HTTPSampler.proxyUser"; // $NON-NLS-1$

    public static final String PROXYPASS = "HTTPSampler.proxyPass"; // $NON-NLS-1$

    public static final String CONNECT_TIMEOUT = "HTTPSampler.connect_timeout"; // $NON-NLS-1$

    public static final String RESPONSE_TIMEOUT = "HTTPSampler.response_timeout"; // $NON-NLS-1$

    public static final String METHOD = "HTTPSampler.method"; // $NON-NLS-1$

    /** This is the encoding used for the content, i.e. the charset name, not the header "Content-Encoding" */
    public static final String CONTENT_ENCODING = "HTTPSampler.contentEncoding"; // $NON-NLS-1$

    public static final String IMPLEMENTATION = "HTTPSampler.implementation"; // $NON-NLS-1$

    public static final String PATH = "HTTPSampler.path"; // $NON-NLS-1$

    public static final String FOLLOW_REDIRECTS = "HTTPSampler.follow_redirects"; // $NON-NLS-1$

    public static final String AUTO_REDIRECTS = "HTTPSampler.auto_redirects"; // $NON-NLS-1$

    public static final String PROTOCOL = "HTTPSampler.protocol"; // $NON-NLS-1$

    static final String PROTOCOL_FILE = "file"; // $NON-NLS-1$

    private static final String DEFAULT_PROTOCOL = HTTPConstants.PROTOCOL_HTTP;

    public static final String URL = "HTTPSampler.URL"; // $NON-NLS-1$

    /**
     * IP source to use - does not apply to Java HTTP implementation currently
     */
    public static final String IP_SOURCE = "HTTPSampler.ipSource"; // $NON-NLS-1$

    public static final String IP_SOURCE_TYPE = "HTTPSampler.ipSourceType"; // $NON-NLS-1$

    public static final String USE_KEEPALIVE = "HTTPSampler.use_keepalive"; // $NON-NLS-1$

    public static final String DO_MULTIPART_POST = "HTTPSampler.DO_MULTIPART_POST"; // $NON-NLS-1$

    public static final String BROWSER_COMPATIBLE_MULTIPART  = "HTTPSampler.BROWSER_COMPATIBLE_MULTIPART"; // $NON-NLS-1$

    public static final String CONCURRENT_DWN = "HTTPSampler.concurrentDwn"; // $NON-NLS-1$

    public static final String CONCURRENT_POOL = "HTTPSampler.concurrentPool"; // $NON-NLS-1$

    public static final int CONCURRENT_POOL_SIZE = 6; // Default concurrent pool size for download embedded resources

    private static final String CONCURRENT_POOL_DEFAULT = Integer.toString(CONCURRENT_POOL_SIZE); // default for concurrent pool

    private static final String USER_AGENT = "User-Agent"; // $NON-NLS-1$

    //- JMX names

    public static final boolean BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT = false; // The default setting to be used (i.e. historic)

    private static final int MAX_BYTES_TO_STORE_PER_REQUEST =
            JMeterUtils.getPropDefault("httpsampler.max_bytes_to_store_per_request", 0); // $NON-NLS-1$ // default value: 0 don't truncate

    private static final int MAX_BUFFER_SIZE = 
            JMeterUtils.getPropDefault("httpsampler.max_buffer_size", 65 * 1024); // $NON-NLS-1$

    private static final boolean IGNORE_FAILED_EMBEDDED_RESOURCES =
            JMeterUtils.getPropDefault("httpsampler.ignore_failed_embedded_resources", false); // $NON-NLS-1$ // default value: false

    private static final boolean IGNORE_EMBEDDED_RESOURCES_DATA =
            JMeterUtils.getPropDefault("httpsampler.embedded_resources_use_md5", false); // $NON-NLS-1$ // default value: false

    public static final int SOURCE_TYPE_DEFAULT = HTTPSamplerBase.SourceType.HOSTNAME.ordinal();

    public static final String DEFAULT_METHOD = HTTPConstants.GET; // $NON-NLS-1$

    private static final List<String> METHODLIST;
    static {
        List<String> defaultMethods = new ArrayList<>(Arrays.asList(
            DEFAULT_METHOD, // i.e. GET
            HTTPConstants.POST,
            HTTPConstants.HEAD,
            HTTPConstants.PUT,
            HTTPConstants.OPTIONS,
            HTTPConstants.TRACE,
            HTTPConstants.DELETE,
            HTTPConstants.PATCH,
            HTTPConstants.PROPFIND,
            HTTPConstants.PROPPATCH,
            HTTPConstants.MKCOL,
            HTTPConstants.COPY,
            HTTPConstants.MOVE,
            HTTPConstants.LOCK,
            HTTPConstants.UNLOCK,
            HTTPConstants.REPORT,
            HTTPConstants.MKCALENDAR,
            HTTPConstants.SEARCH
        ));
        String userDefinedMethods = JMeterUtils.getPropDefault(
                "httpsampler.user_defined_methods", "");
        if (StringUtils.isNotBlank(userDefinedMethods)) {
            defaultMethods.addAll(Arrays.asList(userDefinedMethods.split("\\s*,\\s*")));
        }
        METHODLIST = Collections.unmodifiableList(defaultMethods);
    }

    // @see mergeFileProperties
    // Must be private, as the file list needs special handling
    private static final String FILE_ARGS = "HTTPsampler.Files"; // $NON-NLS-1$
    // MIMETYPE is kept for backward compatibility with old test plans
    private static final String MIMETYPE = "HTTPSampler.mimetype"; // $NON-NLS-1$
    // FILE_NAME is kept for backward compatibility with old test plans
    private static final String FILE_NAME = "HTTPSampler.FILE_NAME"; // $NON-NLS-1$
    /* Shown as Parameter Name on the GUI */
    // FILE_FIELD is kept for backward compatibility with old test plans
    private static final String FILE_FIELD = "HTTPSampler.FILE_FIELD"; // $NON-NLS-1$

    public static final String CONTENT_TYPE = "HTTPSampler.CONTENT_TYPE"; // $NON-NLS-1$

    // IMAGE_PARSER now really means EMBEDDED_PARSER
    public static final String IMAGE_PARSER = "HTTPSampler.image_parser"; // $NON-NLS-1$

    // Embedded URLs must match this RE (if provided)
    public static final String EMBEDDED_URL_RE = "HTTPSampler.embedded_url_re"; // $NON-NLS-1$

    public static final String MONITOR = "HTTPSampler.monitor"; // $NON-NLS-1$

    // Store MD5 hash instead of storing response
    public static final String MD5 = "HTTPSampler.md5"; // $NON-NLS-1$

    /** A number to indicate that the port has not been set. */
    public static final int UNSPECIFIED_PORT = 0;
    public static final String UNSPECIFIED_PORT_AS_STRING = "0"; // $NON-NLS-1$

    /** If the port is not present in a URL, getPort() returns -1 */
    public static final int URL_UNSPECIFIED_PORT = -1;
    public static final String URL_UNSPECIFIED_PORT_AS_STRING = "-1"; // $NON-NLS-1$

    protected static final String NON_HTTP_RESPONSE_CODE = "Non HTTP response code";

    protected static final String NON_HTTP_RESPONSE_MESSAGE = "Non HTTP response message";

    public static final String POST_BODY_RAW = "HTTPSampler.postBodyRaw";

    public static final boolean POST_BODY_RAW_DEFAULT = false;

    private static final String ARG_VAL_SEP = "="; // $NON-NLS-1$

    private static final String QRY_SEP = "&"; // $NON-NLS-1$

    private static final String QRY_PFX = "?"; // $NON-NLS-1$

    protected static final int MAX_REDIRECTS = JMeterUtils.getPropDefault("httpsampler.max_redirects", 20); // $NON-NLS-1$

    protected static final int MAX_FRAME_DEPTH = JMeterUtils.getPropDefault("httpsampler.max_frame_depth", 5); // $NON-NLS-1$

    // Derive the mapping of content types to parsers
    private static final Map<String, String> PARSERS_FOR_CONTENT_TYPE = new HashMap<>();
    // Not synch, but it is not modified after creation

    private static final String RESPONSE_PARSERS = // list of parsers
            JMeterUtils.getProperty("HTTPResponse.parsers");//$NON-NLS-1$
    
    // Bug 49083
    /** Whether to remove '/pathsegment/..' from redirects; default true */
    private static final boolean REMOVESLASHDOTDOT =
            JMeterUtils.getPropDefault("httpsampler.redirect.removeslashdotdot", true);
    
    private static final String HTTP_PREFIX = HTTPConstants.PROTOCOL_HTTP+"://"; // $NON-NLS-1$
    private static final String HTTPS_PREFIX = HTTPConstants.PROTOCOL_HTTPS+"://"; // $NON-NLS-1$

    // Bug 51939
    private static final boolean SEPARATE_CONTAINER =
            JMeterUtils.getPropDefault("httpsampler.separate.container", true); // $NON-NLS-1$

    static {
        String[] parsers = JOrphanUtils.split(RESPONSE_PARSERS, " " , true);// returns empty array for null
        for (final String parser : parsers) {
            String classname = JMeterUtils.getProperty(parser + ".className");//$NON-NLS-1$
            if (classname == null) {
                log.error("Cannot find .className property for " + parser+", ensure you set property:'" + parser + ".className'");
                continue;
            }
            String typeList = JMeterUtils.getProperty(parser + ".types");//$NON-NLS-1$
            if (typeList != null) {
                String[] types = JOrphanUtils.split(typeList, " ", true);
                for (final String type : types) {
                    log.info("Parser for " + type + " is " + classname);
                    PARSERS_FOR_CONTENT_TYPE.put(type, classname);
                }
            } else {
                log.warn("Cannot find .types property for " + parser
                        + ", as a consequence parser will not be used, to make it usable, define property:'"
                        + parser + ".types'");
            }
        }
    }
    
    ////////////////////// Code ///////////////////////////

    public HTTPSamplerBase() {
        setArguments(new Arguments());
    }

    public enum SourceType {
        HOSTNAME("web_testing_source_ip_hostname"), //$NON-NLS-1$
        DEVICE("web_testing_source_ip_device"), //$NON-NLS-1$
        DEVICE_IPV4("web_testing_source_ip_device_ipv4"), //$NON-NLS-1$
        DEVICE_IPV6("web_testing_source_ip_device_ipv6"); //$NON-NLS-1$

        public final String propertyName;
        SourceType(String propertyName) {
            this.propertyName = propertyName;
        }
    }

    // Use for ComboBox Source Address Type. Preserve order (specially with localization)
    public static String[] getSourceTypeList() {
        final SourceType[] types = SourceType.values();
        final String[] displayStrings = new String[types.length];
        for(int i = 0; i < types.length; i++) {
            displayStrings[i] = JMeterUtils.getResString(types[i].propertyName);
        }
        return displayStrings;
    }
    /**
     * Determine if the file should be sent as the entire Content body,
     * i.e. without any additional wrapping.
     *
     * @return true if specified file is to be sent as the body,
     * i.e. there is a single file entry which has a non-empty path and
     * an empty Parameter name.
     */
    public boolean getSendFileAsPostBody() {
        // If there is one file with no parameter name, the file will
        // be sent as post body.
        HTTPFileArg[] files = getHTTPFiles();
        return (files.length == 1)
                && (files[0].getPath().length() > 0)
                && (files[0].getParamName().length() == 0);
    }

    /**
     * Determine if none of the parameters have a name, and if that is the case,
     * it means that the parameter values should be sent as the entity body
     *
     * @return {@code true} if there are parameters and none of these have a
     *         name specified, or {@link HTTPSamplerBase#getPostBodyRaw()} returns
     *         {@code true}
     */
    public boolean getSendParameterValuesAsPostBody() {
        if (getPostBodyRaw()) {
            return true;
        } else {
            boolean hasArguments = false;
            for (JMeterProperty jMeterProperty : getArguments()) {
                hasArguments = true;
                HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
                if (arg.getName() != null && arg.getName().length() > 0) {
                    return false;
                }
            }
            return hasArguments;
        }
    }

    /**
     * Determine if we should use multipart/form-data or
     * application/x-www-form-urlencoded for the post
     *
     * @return true if multipart/form-data should be used and method is POST
     */
    public boolean getUseMultipartForPost() {
        // We use multipart if we have been told so, or files are present
        // and the files should not be send as the post body
        HTTPFileArg[] files = getHTTPFiles();
        return HTTPConstants.POST.equals(getMethod())
                && (getDoMultipartPost() || (files.length > 0 && !getSendFileAsPostBody()));
    }

    public void setProtocol(String value) {
        setProperty(PROTOCOL, value.toLowerCase(java.util.Locale.ENGLISH));
    }

    /**
     * Gets the protocol, with default.
     *
     * @return the protocol
     */
    public String getProtocol() {
        String protocol = getPropertyAsString(PROTOCOL);
        if (protocol == null || protocol.length() == 0) {
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
        // We know that URL arguments should always be encoded in UTF-8 according to spec
        setPath(path, EncoderCache.URL_ARGUMENT_ENCODING);
    }

    /**
     * Sets the PATH property; if the request is a GET or DELETE (and the path
     * does not start with http[s]://) it also calls {@link #parseArguments(String, String)}
     * to extract and store any query arguments.
     *
     * @param path
     *            The new Path value
     * @param contentEncoding
     *            The encoding used for the querystring parameter values
     */
    public void setPath(String path, String contentEncoding) {
        boolean fullUrl = path.startsWith(HTTP_PREFIX) || path.startsWith(HTTPS_PREFIX);
        String method = getMethod();
        boolean getOrDelete = HTTPConstants.GET.equals(method) || HTTPConstants.DELETE.equals(method);
        if (!fullUrl && getOrDelete) {
            int index = path.indexOf(QRY_PFX);
            if (index > -1) {
                setProperty(PATH, path.substring(0, index));
                // Parse the arguments in querystring, assuming specified encoding for values
                parseArguments(path.substring(index + 1), contentEncoding);
            } else {
                setProperty(PATH, path);
            }
        } else {
            setProperty(PATH, path);
        }
    }

    public String getPath() {
        String p = getPropertyAsString(PATH);
        return encodeSpaces(p);
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

    /**
     * Sets the value of the encoding to be used for the content.
     *
     * @param charsetName the name of the encoding to be used
     */
    public void setContentEncoding(String charsetName) {
        setProperty(CONTENT_ENCODING, charsetName);
    }

    /**
     *
     * @return the encoding of the content, i.e. its charset name
     */
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
        return getPropertyAsBoolean(DO_MULTIPART_POST, false);
    }

    public void setDoBrowserCompatibleMultipart(boolean value) {
        setProperty(BROWSER_COMPATIBLE_MULTIPART, value, BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT);
    }

    public boolean getDoBrowserCompatibleMultipart() {
        return getPropertyAsBoolean(BROWSER_COMPATIBLE_MULTIPART, BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT);
    }

    public void setMonitor(String value) {
        this.setProperty(MONITOR, value);
    }

    public void setMonitor(boolean truth) {
        this.setProperty(MONITOR, truth);
    }

    /**
     * @return boolean 
     * @deprecated since 3.2 always returns false
     */
    @Deprecated
    public String getMonitor() {
        return "false";
    }

    /**
     * @return boolean
     * @deprecated since 3.2 always returns false
     */
    @Deprecated
    public boolean isMonitor() {
        return false;
    }
    
    public void setImplementation(String value) {
        this.setProperty(IMPLEMENTATION, value);
    }

    public String getImplementation() {
        return this.getPropertyAsString(IMPLEMENTATION);
    }

    public boolean useMD5() {
        return this.getPropertyAsBoolean(MD5, false);
    }

    public void setMD5(boolean truth) {
        this.setProperty(MD5, truth, false);
    }

    /**
     * Add an argument which has already been encoded
     *
     * @param name name of the argument
     * @param value value of the argument
     */
    public void addEncodedArgument(String name, String value) {
        this.addEncodedArgument(name, value, ARG_VAL_SEP);
    }

    /**
     * Creates an HTTPArgument and adds it to the current set {@link #getArguments()} of arguments.
     *
     * @param name - the parameter name
     * @param value - the parameter value
     * @param metaData - normally just '='
     * @param contentEncoding - the encoding, may be null
     */
    public void addEncodedArgument(String name, String value, String metaData, String contentEncoding) {
        if (log.isDebugEnabled()) {
            log.debug("adding argument: name: " + name + " value: " + value + " metaData: " + metaData + " contentEncoding: " + contentEncoding);
        }

        HTTPArgument arg;
        final boolean nonEmptyEncoding = !StringUtils.isEmpty(contentEncoding);
        if (nonEmptyEncoding) {
            arg = new HTTPArgument(name, value, metaData, true, contentEncoding);
        } else {
            arg = new HTTPArgument(name, value, metaData, true);
        }

        // Check if there are any difference between name and value and their encoded name and value
        String valueEncoded;
        if (nonEmptyEncoding) {
            try {
                valueEncoded = arg.getEncodedValue(contentEncoding);
            } catch (UnsupportedEncodingException e) { // NOSONAR 
                log.warn("Unable to get encoded value using encoding " + contentEncoding);
                valueEncoded = arg.getEncodedValue();
            }
        } else {
            valueEncoded = arg.getEncodedValue();
        }
        // If there is no difference, we mark it as not needing encoding
        if (arg.getName().equals(arg.getEncodedName())
                && arg.getValue().equals(valueEncoded)) {
            arg.setAlwaysEncoded(false);
        }
        this.getArguments().addArgument(arg);
    }

    public void addEncodedArgument(String name, String value, String metaData) {
        this.addEncodedArgument(name, value, metaData, null);
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

    public boolean hasArguments() {
        return getArguments().getArgumentCount() > 0;
    }

    @Override
    public void addTestElement(TestElement el) {
        if (el instanceof CookieManager) {
            setCookieManager((CookieManager) el);
        } else if (el instanceof CacheManager) {
            setCacheManager((CacheManager) el);
        } else if (el instanceof HeaderManager) {
            setHeaderManager((HeaderManager) el);
        } else if (el instanceof AuthManager) {
            setAuthManager((AuthManager) el);
        } else if (el instanceof DNSCacheManager) {
            setDNSResolver((DNSCacheManager) el);
        } else {
            super.addTestElement(el);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Clears the Header Manager property so subsequent loops don't keep merging more elements
     */
    @Override
    public void clearTestElementChildren() {
        removeProperty(HEADER_MANAGER);
    }

    public void setPort(int value) {
        setProperty(new IntegerProperty(PORT, value));
    }

    /**
     * Get the port number for a URL, applying defaults if necessary.
     * (Called by CookieManager.)
     * @param protocol from {@link URL#getProtocol()}
     * @param port number from {@link URL#getPort()}
     * @return the default port for the protocol
     */
    public static int getDefaultPort(String protocol, int port) {
        if (port == URL_UNSPECIFIED_PORT) {
            if (protocol.equalsIgnoreCase(HTTPConstants.PROTOCOL_HTTP)) {
                return HTTPConstants.DEFAULT_HTTP_PORT;
            } else if (protocol.equalsIgnoreCase(HTTPConstants.PROTOCOL_HTTPS)) {
                return HTTPConstants.DEFAULT_HTTPS_PORT;
            }
        }
        return port;
    }

    /**
     * Get the port number from the port string, allowing for trailing blanks.
     *
     * @return port number or UNSPECIFIED_PORT (== 0)
     */
    public int getPortIfSpecified() {
        String portAsString = getPropertyAsString(PORT);
        if(portAsString == null || portAsString.isEmpty()) {
            return UNSPECIFIED_PORT;
        }
        
        try {
            return Integer.parseInt(portAsString.trim());
        } catch (NumberFormatException e) {
            return UNSPECIFIED_PORT;
        }
    }

    /**
     * Tell whether the default port for the specified protocol is used
     *
     * @return true if the default port number for the protocol is used, false otherwise
     */
    public boolean isProtocolDefaultPort() {
        final int port = getPortIfSpecified();
        final String protocol = getProtocol();
        boolean isDefaultHTTPPort = HTTPConstants.PROTOCOL_HTTP
                .equalsIgnoreCase(protocol)
                && port == HTTPConstants.DEFAULT_HTTP_PORT;
        boolean isDefaultHTTPSPort = HTTPConstants.PROTOCOL_HTTPS
                .equalsIgnoreCase(protocol)
                && port == HTTPConstants.DEFAULT_HTTPS_PORT;
        return port == UNSPECIFIED_PORT ||
                isDefaultHTTPPort ||
                isDefaultHTTPSPort;
    }

    /**
     * Get the port; apply the default for the protocol if necessary.
     *
     * @return the port number, with default applied if required.
     */
    public int getPort() {
        final int port = getPortIfSpecified();
        if (port == UNSPECIFIED_PORT) {
            String prot = getProtocol();
            if (HTTPConstants.PROTOCOL_HTTPS.equalsIgnoreCase(prot)) {
                return HTTPConstants.DEFAULT_HTTPS_PORT;
            }
            if (!HTTPConstants.PROTOCOL_HTTP.equalsIgnoreCase(prot)) {
                log.warn("Unexpected protocol: " + prot);
                // TODO - should this return something else?
            }
            return HTTPConstants.DEFAULT_HTTP_PORT;
        }
        return port;
    }

    public void setDomain(String value) {
        setProperty(DOMAIN, value);
    }

    public String getDomain() {
        return getPropertyAsString(DOMAIN);
    }

    public void setConnectTimeout(String value) {
        setProperty(CONNECT_TIMEOUT, value, "");
    }

    public int getConnectTimeout() {
        return getPropertyAsInt(CONNECT_TIMEOUT, 0);
    }

    public void setResponseTimeout(String value) {
        setProperty(RESPONSE_TIMEOUT, value, "");
    }

    public int getResponseTimeout() {
        return getPropertyAsInt(RESPONSE_TIMEOUT, 0);
    }

    public String getProxyHost() {
        return getPropertyAsString(PROXYHOST);
    }

    public int getProxyPortInt() {
        return getPropertyAsInt(PROXYPORT, 0);
    }

    public String getProxyUser() {
        return getPropertyAsString(PROXYUSER);
    }

    public String getProxyPass() {
        return getPropertyAsString(PROXYPASS);
    }

    // gets called from ctor, so has to be final
    public final void setArguments(Arguments value) {
        setProperty(new TestElementProperty(ARGUMENTS, value));
    }

    public Arguments getArguments() {
        return (Arguments) getProperty(ARGUMENTS).getObjectValue();
    }

    /**
     * @param value Boolean that indicates body will be sent as is
     */
    public void setPostBodyRaw(boolean value) {
        setProperty(POST_BODY_RAW, value, POST_BODY_RAW_DEFAULT);
    }

    /**
     * @return boolean that indicates body will be sent as is
     */
    public boolean getPostBodyRaw() {
        return getPropertyAsBoolean(POST_BODY_RAW, POST_BODY_RAW_DEFAULT);
    }

    public void setAuthManager(AuthManager value) {
        AuthManager mgr = getAuthManager();
        if (mgr != null) {
            if(log.isWarnEnabled()) {
                log.warn("Existing AuthManager {} superseded by {}", mgr.getName(), value.getName());
            }
        }
        setProperty(new TestElementProperty(AUTH_MANAGER, value));
    }

    public AuthManager getAuthManager() {
        return (AuthManager) getProperty(AUTH_MANAGER).getObjectValue();
    }

    public void setHeaderManager(final HeaderManager value) {
        HeaderManager mgr = getHeaderManager();
        HeaderManager lValue = value;
        if (mgr != null) {
            lValue = mgr.merge(value);
            if (log.isDebugEnabled()) {
                log.debug("Existing HeaderManager '" + mgr.getName() + "' merged with '" + lValue.getName() + "'");
                for (int i = 0; i < lValue.getHeaders().size(); i++) {
                    log.debug("    " + lValue.getHeader(i).getName() + "=" + lValue.getHeader(i).getValue());
                }
            }
        }
        setProperty(new TestElementProperty(HEADER_MANAGER, lValue));
    }

    public HeaderManager getHeaderManager() {
        return (HeaderManager) getProperty(HEADER_MANAGER).getObjectValue();
    }

    // private method to allow AsyncSample to reset the value without performing checks
    private void setCookieManagerProperty(CookieManager value) {
        setProperty(new TestElementProperty(COOKIE_MANAGER, value));
    }

    public void setCookieManager(CookieManager value) {
        CookieManager mgr = getCookieManager();
        if (mgr != null) {
            if(log.isWarnEnabled()) {
                log.warn("Existing CookieManager {} superseded by {}", mgr.getName(), value.getName());
            }
        }
        setCookieManagerProperty(value);
    }

    public CookieManager getCookieManager() {
        return (CookieManager) getProperty(COOKIE_MANAGER).getObjectValue();
    }

    // private method to allow AsyncSample to reset the value without performing checks
    private void setCacheManagerProperty(CacheManager value) {
        setProperty(new TestElementProperty(CACHE_MANAGER, value));
    }

    public void setCacheManager(CacheManager value) {
        CacheManager mgr = getCacheManager();
        if (mgr != null) {
            if(log.isWarnEnabled()) {
                log.warn("Existing CacheManager {} superseded by {}", mgr.getName(), value.getName());
            }
        }
        setCacheManagerProperty(value);
    }

    public CacheManager getCacheManager() {
        return (CacheManager) getProperty(CACHE_MANAGER).getObjectValue();
    }

    public DNSCacheManager getDNSResolver() {
        return (DNSCacheManager) getProperty(DNS_CACHE_MANAGER).getObjectValue();
    }

    public void setDNSResolver(DNSCacheManager cacheManager) {
        DNSCacheManager mgr = getDNSResolver();
        if (mgr != null) {
            if(log.isWarnEnabled()) {
                log.warn("Existing DNSCacheManager {} superseded by {}", mgr.getName(), cacheManager.getName());
            }
        }
        setProperty(new TestElementProperty(DNS_CACHE_MANAGER, cacheManager));
    }

    public boolean isImageParser() {
        return getPropertyAsBoolean(IMAGE_PARSER, false);
    }

    public void setImageParser(boolean parseImages) {
        setProperty(IMAGE_PARSER, parseImages, false);
    }

    /**
     * Get the regular expression URLs must match.
     *
     * @return regular expression (or empty) string
     */
    public String getEmbeddedUrlRE() {
        return getPropertyAsString(EMBEDDED_URL_RE, "");
    }

    public void setEmbeddedUrlRE(String regex) {
        setProperty(new StringProperty(EMBEDDED_URL_RE, regex));
    }

    /**
     * Populates the provided HTTPSampleResult with details from the Exception.
     * Does not create a new instance, so should not be used directly to add a subsample.
     *
     * @param e
     *            Exception representing the error.
     * @param res
     *            SampleResult to be modified
     * @return the modified sampling result containing details of the Exception.
     */
    protected HTTPSampleResult errorResult(Throwable e, HTTPSampleResult res) {
        res.setSampleLabel(res.getSampleLabel());
        res.setDataType(SampleResult.TEXT);
        ByteArrayOutputStream text = new ByteArrayOutputStream(200);
        e.printStackTrace(new PrintStream(text)); // NOSONAR Stacktrace will be used in the response
        res.setResponseData(text.toByteArray());
        res.setResponseCode(NON_HTTP_RESPONSE_CODE+": " + e.getClass().getName());
        res.setResponseMessage(NON_HTTP_RESPONSE_MESSAGE+": " + e.getMessage());
        res.setSuccessful(false);
        return res;
    }


    /**
     * Get the URL, built from its component parts.
     *
     * <p>
     * As a special case, if the path starts with "http[s]://",
     * then the path is assumed to be the entire URL.
     * </p>
     *
     * @return The URL to be requested by this sampler.
     * @throws MalformedURLException if url is malformed
     */
    public URL getUrl() throws MalformedURLException {
        StringBuilder pathAndQuery = new StringBuilder(100);
        String path = this.getPath();
        // Hack to allow entire URL to be provided in host field
        if (path.startsWith(HTTP_PREFIX)
                || path.startsWith(HTTPS_PREFIX)) {
            return new URL(path);
        }
        String domain = getDomain();
        String protocol = getProtocol();
        String method = getMethod();
        if (PROTOCOL_FILE.equalsIgnoreCase(protocol)) {
            domain = null; // allow use of relative file URLs
        } else {
            // HTTP URLs must be absolute, allow file to be relative
            if (!path.startsWith("/")) { // $NON-NLS-1$
                pathAndQuery.append('/'); // $NON-NLS-1$
            }
        }
        pathAndQuery.append(path);

        // Add the query string if it is a HTTP GET or DELETE request
        if (HTTPConstants.GET.equals(method) 
                || HTTPConstants.DELETE.equals(method)
                || HTTPConstants.OPTIONS.equals(method)) {
            // Get the query string encoded in specified encoding
            // If no encoding is specified by user, we will get it
            // encoded in UTF-8, which is what the HTTP spec says
            String queryString = getQueryString(getContentEncoding());
            if (queryString.length() > 0) {
                if (path.contains(QRY_PFX)) {// Already contains a prefix
                    pathAndQuery.append(QRY_SEP);
                } else {
                    pathAndQuery.append(QRY_PFX);
                }
                pathAndQuery.append(queryString);
            }
        }
        // If default port for protocol is used, we do not include port in URL
        if (isProtocolDefaultPort()) {
            return new URL(protocol, domain, pathAndQuery.toString());
        }
        return new URL(protocol, domain, getPort(), pathAndQuery.toString());
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
    public String getQueryString(final String contentEncoding) {
        
        CollectionProperty arguments = getArguments().getArguments();
        // Optimisation : avoid building useless objects if empty arguments
        if(arguments.size() == 0) {
            return "";
        }
        String lContentEncoding = contentEncoding;
        // Check if the sampler has a specified content encoding
        if (JOrphanUtils.isBlank(lContentEncoding)) {
            // We use the encoding which should be used according to the HTTP spec, which is UTF-8
            lContentEncoding = EncoderCache.URL_ARGUMENT_ENCODING;
        }
        
        StringBuilder buf = new StringBuilder(arguments.size() * 15);
        PropertyIterator iter = arguments.iterator();
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
            } catch (ClassCastException e) { // NOSONAR
                log.warn("Unexpected argument type: " + objectValue.getClass().getName() +" cannot be cast to HTTPArgument");
                item = new HTTPArgument((Argument) objectValue);
            }
            final String encodedName = item.getEncodedName();
            if (encodedName.length() == 0) {
                continue; // Skip parameters with a blank name (allows use of optional variables in parameter lists)
            }
            if (!first) {
                buf.append(QRY_SEP);
            } else {
                first = false;
            }
            buf.append(encodedName);
            if (item.getMetaData() == null) {
                buf.append(ARG_VAL_SEP);
            } else {
                buf.append(item.getMetaData());
            }

            // Encode the parameter value in the specified content encoding
            try {
                buf.append(item.getEncodedValue(lContentEncoding));
            } catch(UnsupportedEncodingException e) { // NOSONAR
                log.warn("Unable to encode parameter in encoding " + lContentEncoding + ", parameter value not included in query string");
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
     *            the query string, might be the post body of a http post request.
     * @param contentEncoding -
     *            the content encoding of the query string;
     *            if non-null then it is used to decode the
     */
    public void parseArguments(String queryString, String contentEncoding) {
        String[] args = JOrphanUtils.split(queryString, QRY_SEP);
        final boolean isDebug = log.isDebugEnabled();
        for (String arg : args) {
            if (isDebug) {
                log.debug("Arg: " + arg);
            }
            // need to handle four cases:
            // - string contains name=value
            // - string contains name=
            // - string contains name
            // - empty string

            String metaData; // records the existence of an equal sign
            String name;
            String value;
            int length = arg.length();
            int endOfNameIndex = arg.indexOf(ARG_VAL_SEP);
            if (endOfNameIndex != -1) {// is there a separator?
                // case of name=value, name=
                metaData = ARG_VAL_SEP;
                name = arg.substring(0, endOfNameIndex);
                value = arg.substring(endOfNameIndex + 1, length);
            } else {
                metaData = "";
                name = arg;
                value = "";
            }
            if (name.length() > 0) {
                if (isDebug) {
                    log.debug("Name: " + name + " Value: " + value + " Metadata: " + metaData);
                }
                // If we know the encoding, we can decode the argument value,
                // to make it easier to read for the user
                if (!StringUtils.isEmpty(contentEncoding)) {
                    addEncodedArgument(name, value, metaData, contentEncoding);
                } else {
                    // If we do not know the encoding, we just use the encoded value
                    // The browser has already done the encoding, so save the values as is
                    addNonEncodedArgument(name, value, metaData);
                }
            }
        }
    }

    public void parseArguments(String queryString) {
        // We do not know the content encoding of the query string
        parseArguments(queryString, null);
    }

    @Override
    public String toString() {
        try {
            StringBuilder stringBuffer = new StringBuilder();
            stringBuffer.append(this.getUrl().toString());
            // Append body if it is a post or put
            String method = getMethod();
            if (HTTPConstants.POST.equals(method) || HTTPConstants.PUT.equals(method)) {
                stringBuffer.append("\nQuery Data: ");
                stringBuffer.append(getQueryString());
            }
            return stringBuffer.toString();
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
    @Override
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
            res = sample(getUrl(), getMethod(), false, 0);
            if (res != null) {
                res.setSampleLabel(getName());
            }
            return res;
        } catch (Exception e) {
            return errorResult(e, new HTTPSampleResult());
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
     * @param u
     *            URL to sample
     * @param method
     *            HTTP method: GET, POST,...
     * @param areFollowingRedirect
     *            whether we're getting a redirect target
     * @param depth
     *            Depth of this target in the frame structure. Used only to
     *            prevent infinite recursion.
     * @return results of the sampling, can be null if u is in CacheManager
     */
    protected abstract HTTPSampleResult sample(URL u,
            String method, boolean areFollowingRedirect, int depth);

    /**
     * Download the resources of an HTML page.
     *
     * @param pRes
     *            result of the initial request - must contain an HTML response
     * @param container
     *            for storing the results, if any
     * @param frameDepth
     *            Depth of this target in the frame structure. Used only to
     *            prevent infinite recursion.
     * @return res if no resources exist, otherwise the "Container" result with one subsample per request issued
     */
    protected HTTPSampleResult downloadPageResources(final HTTPSampleResult pRes, final HTTPSampleResult container, final int frameDepth) {
        HTTPSampleResult res = pRes;
        Iterator<URL> urls = null;
        try {
            final byte[] responseData = res.getResponseData();
            if (responseData.length > 0) {  // Bug 39205
                final LinkExtractorParser parser = getParser(res);
                if (parser != null) {
                    String userAgent = getUserAgent(res);
                    urls = parser.getEmbeddedResourceURLs(userAgent, responseData, res.getURL(), res.getDataEncodingWithDefault());
                }
            }
        } catch (LinkExtractorParseException e) {
            // Don't break the world just because this failed:
            res.addSubResult(errorResult(e, new HTTPSampleResult(res)));
            setParentSampleSuccess(res, false);
        }
        HTTPSampleResult lContainer = container;
        // Iterate through the URLs and download each image:
        if (urls != null && urls.hasNext()) {
            if (lContainer == null) {
                lContainer = new HTTPSampleResult(res);
                lContainer.addRawSubResult(res);
            }
            res = lContainer;

            // Get the URL matcher
            String re = getEmbeddedUrlRE();
            Perl5Matcher localMatcher = null;
            Pattern pattern = null;
            if (re.length() > 0) {
                try {
                    pattern = JMeterUtils.getPattern(re);
                    localMatcher = JMeterUtils.getMatcher();// don't fetch unless pattern compiles
                } catch (MalformedCachePatternException e) { // NOSONAR
                    log.warn("Ignoring embedded URL match string: " + e.getMessage());
                }
            }

            // For concurrent get resources
            final List<Callable<AsynSamplerResultHolder>> list = new ArrayList<>();

            int maxConcurrentDownloads = CONCURRENT_POOL_SIZE; // init with default value
            boolean isConcurrentDwn = isConcurrentDwn();
            if (isConcurrentDwn) {
                try {
                    maxConcurrentDownloads = Integer.parseInt(getConcurrentPool());
                } catch (NumberFormatException nfe) {
                    log.warn("Concurrent download resources selected, "// $NON-NLS-1$
                            + "but pool size value is bad. Use default value");// $NON-NLS-1$
                }

                // if the user choose a number of parallel downloads of 1
                // no need to use another thread, do the sample on the current thread
                if (maxConcurrentDownloads == 1) {
                    log.warn("Number of parallel downloads set to 1, (sampler name=" + getName()+")");
                    isConcurrentDwn = false;
                }
            }

            while (urls.hasNext()) {
                Object binURL = urls.next(); // See catch clause below
                try {
                    URL url = (URL) binURL;
                    if (url == null) {
                        log.warn("Null URL detected (should not happen)");
                    } else {
                        try {
                            url = escapeIllegalURLCharacters(url);
                        } catch (Exception e) { // NOSONAR
                            res.addSubResult(errorResult(new Exception(url.toString() + " is not a correct URI", e), new HTTPSampleResult(res)));
                            setParentSampleSuccess(res, false);
                            continue;
                        }
                        // I don't think localMatcher can be null here, but check just in case
                        if (pattern != null && localMatcher != null && !localMatcher.matches(url.toString(), pattern)) {
                            continue; // we have a pattern and the URL does not match, so skip it
                        }
                        try {
                            url = url.toURI().normalize().toURL();
                        } catch (MalformedURLException | URISyntaxException e) {
                            res.addSubResult(errorResult(new Exception(url.toString() + " URI can not be normalized", e), new HTTPSampleResult(res)));
                            setParentSampleSuccess(res, false);
                            continue;
                        }

                        if (isConcurrentDwn) {
                            // if concurrent download emb. resources, add to a list for async gets later
                            list.add(new ASyncSample(url, HTTPConstants.GET, false, frameDepth + 1, getCookieManager(), this));
                        } else {
                            // default: serial download embedded resources
                            HTTPSampleResult binRes = sample(url, HTTPConstants.GET, false, frameDepth + 1);
                            res.addSubResult(binRes);
                            setParentSampleSuccess(res, res.isSuccessful() && (binRes == null || binRes.isSuccessful()));
                        }
                    }
                } catch (ClassCastException e) { // NOSONAR
                    res.addSubResult(errorResult(new Exception(binURL + " is not a correct URI", e), new HTTPSampleResult(res)));
                    setParentSampleSuccess(res, false);
                }
            }

            // IF for download concurrent embedded resources
            if (isConcurrentDwn && !list.isEmpty()) {

                ResourcesDownloader resourcesDownloader = ResourcesDownloader.getInstance();

                try {
                    // sample all resources
                    final List<Future<AsynSamplerResultHolder>> retExec =
                            resourcesDownloader.invokeAllAndAwaitTermination(maxConcurrentDownloads, list);
                    CookieManager cookieManager = getCookieManager();
                    // add result to main sampleResult
                    for (Future<AsynSamplerResultHolder> future : retExec) {
                        // this call will not block as the futures return by invokeAllAndAwaitTermination
                        //   are either done or cancelled
                        AsynSamplerResultHolder binRes = future.get();
                        if (cookieManager != null) {
                            CollectionProperty cookies = binRes.getCookies();
                            for (JMeterProperty jMeterProperty : cookies) {
                                Cookie cookie = (Cookie) jMeterProperty.getObjectValue();
                                cookieManager.add(cookie);
                            }
                        }
                        res.addSubResult(binRes.getResult());
                        setParentSampleSuccess(res, res.isSuccessful() && (binRes.getResult() != null ? binRes.getResult().isSuccessful():true));
                    }
                } catch (InterruptedException ie) {
                    log.warn("Interrupted fetching embedded resources", ie); // $NON-NLS-1$
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ee) {
                    log.warn("Execution issue when fetching embedded resources", ee); // $NON-NLS-1$
                }
            }
        }
        return res;
    }

    /**
     * Gets parser from {@link HTTPSampleResult#getMediaType()}.
     * Returns null if no parser defined for it
     * @param res {@link HTTPSampleResult}
     * @return {@link LinkExtractorParser}
     * @throws LinkExtractorParseException
     */
    private LinkExtractorParser getParser(HTTPSampleResult res)
            throws LinkExtractorParseException {
        String parserClassName =
                PARSERS_FOR_CONTENT_TYPE.get(res.getMediaType());
        if (!StringUtils.isEmpty(parserClassName)) {
            return BaseParser.getParser(parserClassName);
        }
        return null;
    }
    
    /**
     * @param url URL to escape
     * @return escaped url
     */
    private URL escapeIllegalURLCharacters(java.net.URL url) {
        if (url == null || "file".equals(url.getProtocol())) {
            return url;
        }
        try {
            return ConversionUtils.sanitizeUrl(url).toURL();
        } catch (Exception e1) { // NOSONAR
            log.error("Error escaping URL:'" + url + "', message:" + e1.getMessage());
            return url;
        }
    }

    /**
     * Extract User-Agent header value
     * @param sampleResult HTTPSampleResult
     * @return User Agent part
     */
    private String getUserAgent(HTTPSampleResult sampleResult) {
        String res = sampleResult.getRequestHeaders();
        int index = res.indexOf(USER_AGENT);
        if (index >= 0) {
            // see HTTPHC3Impl#getConnectionHeaders
            // see HTTPHC4Impl#getConnectionHeaders
            // see HTTPJavaImpl#getConnectionHeaders
            //': ' is used by JMeter to fill-in requestHeaders, see getConnectionHeaders
            final String userAgentPrefix = USER_AGENT+": ";
            String userAgentHdr = res.substring(
                    index+userAgentPrefix.length(),
                    res.indexOf('\n',// '\n' is used by JMeter to fill-in requestHeaders, see getConnectionHeaders
                            index+userAgentPrefix.length()+1));
            return userAgentHdr.trim();
        } else {
            if (log.isInfoEnabled()) {
                log.info("No user agent extracted from requestHeaders:" + res);
            }
            return null;
        }
    }

    /**
     * Set parent successful attribute based on IGNORE_FAILED_EMBEDDED_RESOURCES parameter
     * @param res {@link HTTPSampleResult}
     * @param initialValue boolean
     */
    private void setParentSampleSuccess(HTTPSampleResult res, boolean initialValue) {
        if (!IGNORE_FAILED_EMBEDDED_RESOURCES) {
            res.setSuccessful(initialValue);
            if (!initialValue) {
                StringBuilder detailedMessage = new StringBuilder(80);
                detailedMessage.append("Embedded resource download error:"); //$NON-NLS-1$
                for (SampleResult subResult : res.getSubResults()) {
                    HTTPSampleResult httpSampleResult = (HTTPSampleResult) subResult;
                    if (!httpSampleResult.isSuccessful()) {
                        detailedMessage.append(httpSampleResult.getURL())
                                .append(" code:") //$NON-NLS-1$
                                .append(httpSampleResult.getResponseCode())
                                .append(" message:") //$NON-NLS-1$
                                .append(httpSampleResult.getResponseMessage())
                                .append(", "); //$NON-NLS-1$
                    }
                }
                res.setResponseMessage(detailedMessage.toString()); //$NON-NLS-1$
            }
        }
    }

    protected String encodeSpaces(String path) {
        return JOrphanUtils.replaceAllChars(path, ' ', "%20"); // $NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded() {
        if (isConcurrentDwn()) {
            ResourcesDownloader.getInstance().shrink();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testEnded(String host) {
        testEnded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testStarted(String host) {
        testStarted();
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
        totalRes.addRawSubResult(res);
        HTTPSampleResult lastRes = res;

        int redirect;
        for (redirect = 0; redirect < MAX_REDIRECTS; redirect++) {
            boolean invalidRedirectUrl = false;
            String location = lastRes.getRedirectLocation();
            if (log.isDebugEnabled()) {
                log.debug("Initial location: " + location);
            }
            if (REMOVESLASHDOTDOT) {
                location = ConversionUtils.removeSlashDotDot(location);
            }
            // Browsers seem to tolerate Location headers with spaces,
            // replacing them automatically with %20. We want to emulate
            // this behaviour.
            location = encodeSpaces(location);
            if (log.isDebugEnabled()) {
                log.debug("Location after /. and space transforms: " + location);
            }
            // Change all but HEAD into GET (Bug 55450)
            String method = lastRes.getHTTPMethod();
            method = computeMethodForRedirect(method);

            try {
                URL url = ConversionUtils.makeRelativeURL(lastRes.getURL(), location);
                url = ConversionUtils.sanitizeUrl(url).toURL();
                if (log.isDebugEnabled()) {
                    log.debug("Location as URL: " + url.toString());
                }
                HTTPSampleResult tempRes = sample(url, method, true, frameDepth);
                if (tempRes != null) {
                    lastRes = tempRes;
                } else {
                    // Last url was in cache so tempRes is null
                    break;
                }
            } catch (MalformedURLException | URISyntaxException e) {
                errorResult(e, lastRes);
                // The redirect URL we got was not a valid URL
                invalidRedirectUrl = true;
            }
            if (lastRes.getSubResults() != null && lastRes.getSubResults().length > 0) {
                SampleResult[] subs = lastRes.getSubResults();
                for (SampleResult sub : subs) {
                    totalRes.addSubResult(sub);
                }
            } else {
                // Only add sample if it is a sample of valid url redirect, i.e. that
                // we have actually sampled the URL
                if (!invalidRedirectUrl) {
                    totalRes.addSubResult(lastRes);
                }
            }

            if (!lastRes.isRedirect()) {
                break;
            }
        }
        if (redirect >= MAX_REDIRECTS) {
            lastRes = errorResult(new IOException("Exceeded maximum number of redirects: " + MAX_REDIRECTS), new HTTPSampleResult(lastRes));
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
        totalRes.setDataEncoding(lastRes.getDataEncodingNoDefault());
        return totalRes;
    }

    /**
     * See <a href="http://tools.ietf.org/html/rfc2616#section-10.3">RFC2616#section-10.3</a>
     * JMeter conforms currently to HttpClient 4.5.2 supported RFC
     * TODO Update when migrating to HttpClient 5.X using response code
     * @param initialMethod the initial HTTP Method
     * @return the new HTTP Method as per RFC
     */
    private String computeMethodForRedirect(String initialMethod) {
        if (!HTTPConstants.HEAD.equalsIgnoreCase(initialMethod)) {
            return HTTPConstants.GET;
        }
        return initialMethod;
    }

    /**
     * Follow redirects and download page resources if appropriate. this works,
     * but the container stuff here is what's doing it. followRedirects() is
     * actually doing the work to make sure we have only one container to make
     * this work more naturally, I think this method - sample() - needs to take
     * an HTTPSamplerResult container parameter instead of a
     * boolean:areFollowingRedirect.
     *
     * @param pAreFollowingRedirect flag whether we are getting a redirect target
     * @param frameDepth Depth of this target in the frame structure. Used only to prevent infinite recursion.
     * @param pRes sample result to process
     * @return the sample result
     */
    protected HTTPSampleResult resultProcessing(final boolean pAreFollowingRedirect, final int frameDepth, final HTTPSampleResult pRes) {
        boolean wasRedirected = false;
        boolean areFollowingRedirect = pAreFollowingRedirect;
        HTTPSampleResult res = pRes;
        if (!areFollowingRedirect && res.isRedirect()) {
            if(log.isDebugEnabled()) {
                log.debug("Location set to - " + res.getRedirectLocation());
            }

            if (getFollowRedirects()) {
                res = followRedirects(res, frameDepth);
                areFollowingRedirect = true;
                wasRedirected = true;
            }
        }
        
        if (res.isSuccessful() && SampleResult.TEXT.equals(res.getDataType()) && isImageParser() ) {
            if (frameDepth > MAX_FRAME_DEPTH) {
                HTTPSampleResult errSubResult = new HTTPSampleResult(res);
                errSubResult.removeSubResults();
                res.addSubResult(errorResult(new Exception("Maximum frame/iframe nesting depth exceeded."), errSubResult));
            } else {
                // Only download page resources if we were not redirected.
                // If we were redirected, the page resources have already been
                // downloaded for the sample made for the redirected url
                // otherwise, use null so the container is created if necessary unless
                // the flag is false, in which case revert to broken 2.1 behaviour
                // Bug 51939 -  https://bz.apache.org/bugzilla/show_bug.cgi?id=51939
                if (!wasRedirected) {
                    HTTPSampleResult container = (HTTPSampleResult) (
                            areFollowingRedirect ? res.getParent() : SEPARATE_CONTAINER ? null : res);
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
     * @param code status code to check
     * @return whether in range 200-399 or not
     */
    protected boolean isSuccessCode(int code) {
        return code >= 200 && code <= 399;
    }

    protected static String encodeBackSlashes(String value) {
        StringBuilder newValue = new StringBuilder();
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

    /*
     * Method to set files list to be uploaded.
     *
     * @param value
     *   HTTPFileArgs object that stores file list to be uploaded.
     */
    private void setHTTPFileArgs(HTTPFileArgs value) {
        if (value.getHTTPFileArgCount() > 0) {
            setProperty(new TestElementProperty(FILE_ARGS, value));
        } else {
            removeProperty(FILE_ARGS); // no point saving an empty list
        }
    }

    /*
     * Method to get files list to be uploaded.
     */
    private HTTPFileArgs getHTTPFileArgs() {
        return (HTTPFileArgs) getProperty(FILE_ARGS).getObjectValue();
    }

    /**
     * Get the collection of files as a list.
     * The list is built up from the filename/filefield/mimetype properties,
     * plus any additional entries saved in the FILE_ARGS property.
     *
     * If there are no valid file entries, then an empty list is returned.
     *
     * @return an array of file arguments (never null)
     */
    public HTTPFileArg[] getHTTPFiles() {
        final HTTPFileArgs fileArgs = getHTTPFileArgs();
        return fileArgs == null ? new HTTPFileArg[] {} : fileArgs.asArray();
    }

    public int getHTTPFileCount() {
        return getHTTPFiles().length;
    }
    /**
     * Saves the list of files.
     * The first file is saved in the Filename/field/mimetype properties.
     * Any additional files are saved in the FILE_ARGS array.
     *
     * @param files list of files to save
     */
    public void setHTTPFiles(HTTPFileArg[] files) {
        HTTPFileArgs fileArgs = new HTTPFileArgs();
        // Weed out the empty files
        if (files.length > 0) {
            for (HTTPFileArg file : files) {
                if (file.isNotEmpty()) {
                    fileArgs.addHTTPFileArg(file);
                }
            }
        }
        setHTTPFileArgs(fileArgs);
    }

    public static String[] getValidMethodsAsArray() {
        return METHODLIST.toArray(new String[METHODLIST.size()]);
    }

    public static boolean isSecure(String protocol) {
        return HTTPConstants.PROTOCOL_HTTPS.equalsIgnoreCase(protocol);
    }

    public static boolean isSecure(URL url) {
        return isSecure(url.getProtocol());
    }

    // Implement these here, to avoid re-implementing for sub-classes
    // (previously these were implemented in all TestElements)
    @Override
    public void threadStarted() {
    }

    @Override
    public void threadFinished() {
    }

    @Override
    public void testIterationStart(LoopIterationEvent event) {
        // NOOP to provide based empty impl and avoid breaking existing implementations
    }

    /**
     * Read response from the input stream, converting to MD5 digest if the useMD5 property is set.
     * <p>
     * For the MD5 case, the result byte count is set to the size of the original response.
     * <p>
     * Closes the inputStream
     *
     * @param sampleResult sample to store information about the response into
     * @param in input stream from which to read the response
     * @param length expected input length or zero
     * @return the response or the MD5 of the response
     * @throws IOException if reading the result fails
     */
    public byte[] readResponse(SampleResult sampleResult, InputStream in, long length) throws IOException {
        
        OutputStream w = null;
        try { // NOSONAR No try with resource as performance is critical here
            byte[] readBuffer = new byte[8192]; // 8kB is the (max) size to have the latency ('the first packet')
            int bufferSize = 32;// Enough for MD5

            MessageDigest md = null;
            boolean knownResponseLength = length > 0;// may also happen if long value > int.max
            if (useMD5()) {
                try {
                    md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
                } catch (NoSuchAlgorithmException e) {
                    log.error("Should not happen - could not find MD5 digest", e);
                }
            } else {
                if (!knownResponseLength) {
                    bufferSize = 4 * 1024;
                } else {
                    bufferSize = (int) Math.min(MAX_BUFFER_SIZE, length);
                }
            }
            
            
            int bytesReadInBuffer = 0;
            long totalBytes = 0;
            boolean first = true;
            boolean storeInBOS = true;
            while ((bytesReadInBuffer = in.read(readBuffer)) > -1) {
                if (first) {
                    sampleResult.latencyEnd();
                    first = false;
                    if(md == null) {
                        if(!knownResponseLength) {
                            w = new org.apache.commons.io.output.ByteArrayOutputStream(bufferSize);
                        }
                        else {
                            w = new DirectAccessByteArrayOutputStream(bufferSize);
                        }
                    }
                }
                
                if (md == null) {
                    if(storeInBOS) {
                        if(MAX_BYTES_TO_STORE_PER_REQUEST <= 0 ||
                                (totalBytes+bytesReadInBuffer<=MAX_BYTES_TO_STORE_PER_REQUEST) ||
                                JMeterContextService.getContext().isRecording()) {
                            w.write(readBuffer, 0, bytesReadInBuffer);
                        } else {
                            log.debug("Big response, truncating it to {} bytes", MAX_BYTES_TO_STORE_PER_REQUEST);
                            w.write(readBuffer, 0, (int)(MAX_BYTES_TO_STORE_PER_REQUEST-totalBytes));
                            storeInBOS = false;
                        }
                    }
                } else {
                    md.update(readBuffer, 0, bytesReadInBuffer);
                }
                totalBytes += bytesReadInBuffer;
            }
            
            if (first) { // Bug 46838 - if there was no data, still need to set latency
                sampleResult.latencyEnd();
                return new byte[0];
            }
            
            if (md == null) {
                return toByteArray(w);
            } else {
                byte[] md5Result = md.digest();
                sampleResult.setBytes(totalBytes);
                return JOrphanUtils.baToHexBytes(md5Result);                
            }
            
        } finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(w);
        }
    }

    /**
     * Optimized method to get byte array from {@link OutputStream}
     * @param w {@link OutputStream}
     * @return byte array
     */
    private byte[] toByteArray(OutputStream w) {
        if(w instanceof DirectAccessByteArrayOutputStream) {
            return ((DirectAccessByteArrayOutputStream) w).toByteArray();
        }
        
        if(w instanceof org.apache.commons.io.output.ByteArrayOutputStream) {
            return ((org.apache.commons.io.output.ByteArrayOutputStream) w).toByteArray();
        }
        
        log.warn("Unknown stream type " + w.getClass());
        
        return null;
    }

    /**
     * JMeter 2.3.1 and earlier only had fields for one file on the GUI:
     * <ul>
     *   <li>FILE_NAME</li>
     *   <li>FILE_FIELD</li>
     *   <li>MIMETYPE</li>
     * </ul>
     * These were stored in their own individual properties.
     * <p>
     * Version 2.3.3 introduced a list of files, each with their own path, name and mimetype.
     * <p>
     * In order to maintain backwards compatibility of test plans, the 3 original properties
     * were retained; additional file entries are stored in an HTTPFileArgs class.
     * The HTTPFileArgs class was only present if there is more than 1 file; this means that
     * such test plans are backward compatible.
     * <p>
     * Versions after 2.3.4 dispense with the original set of 3 properties.
     * Test plans that use them are converted to use a single HTTPFileArgs list.
     *
     * @see HTTPSamplerBaseConverter
     */
    void mergeFileProperties() {
        JMeterProperty fileName = getProperty(FILE_NAME);
        JMeterProperty paramName = getProperty(FILE_FIELD);
        JMeterProperty mimeType = getProperty(MIMETYPE);
        HTTPFileArg oldStyleFile = new HTTPFileArg(fileName, paramName, mimeType);

        HTTPFileArgs fileArgs = getHTTPFileArgs();

        HTTPFileArgs allFileArgs = new HTTPFileArgs();
        if (oldStyleFile.isNotEmpty()) { // OK, we have an old-style file definition
            allFileArgs.addHTTPFileArg(oldStyleFile); // save it
            // Now deal with any additional file arguments
            if (fileArgs != null) {
                HTTPFileArg[] infiles = fileArgs.asArray();
                for (HTTPFileArg infile : infiles) {
                    allFileArgs.addHTTPFileArg(infile);
                }
            }
        } else {
            if (fileArgs != null) { // for new test plans that don't have FILE/PARAM/MIME properties
                allFileArgs = fileArgs;
            }
        }
        // Updated the property lists
        setHTTPFileArgs(allFileArgs);
        removeProperty(FILE_FIELD);
        removeProperty(FILE_NAME);
        removeProperty(MIMETYPE);
    }

    /**
     * set IP source to use - does not apply to Java HTTP implementation currently
     *
     * @param value IP source to use
     */
    public void setIpSource(String value) {
        setProperty(IP_SOURCE, value, "");
    }

    /**
     * get IP source to use - does not apply to Java HTTP implementation currently
     *
     * @return IP source to use
     */
    public String getIpSource() {
        return getPropertyAsString(IP_SOURCE, "");
    }

    /**
     * set IP/address source type to use
     *
     * @param value type of the IP/address source
     */
    public void setIpSourceType(int value) {
        setProperty(IP_SOURCE_TYPE, value, SOURCE_TYPE_DEFAULT);
    }

    /**
     * get IP/address source type to use
     *
     * @return address source type
     */
    public int getIpSourceType() {
        return getPropertyAsInt(IP_SOURCE_TYPE, SOURCE_TYPE_DEFAULT);
    }

    /**
     * Return if used a concurrent thread pool to get embedded resources.
     *
     * @return true if used
     */
    public boolean isConcurrentDwn() {
        return getPropertyAsBoolean(CONCURRENT_DWN, false);
    }

    public void setConcurrentDwn(boolean concurrentDwn) {
        setProperty(CONCURRENT_DWN, concurrentDwn, false);
    }

    /**
     * Get the pool size for concurrent thread pool to get embedded resources.
     *
     * @return the pool size
     */
    public String getConcurrentPool() {
        return getPropertyAsString(CONCURRENT_POOL, CONCURRENT_POOL_DEFAULT);
    }

    public void setConcurrentPool(String poolSize) {
        setProperty(CONCURRENT_POOL, poolSize, CONCURRENT_POOL_DEFAULT);
    }


    /**
     * Callable class to sample asynchronously resources embedded
     *
     */
    private static class ASyncSample implements Callable<AsynSamplerResultHolder> {
        private final URL url;
        private final String method;
        private final boolean areFollowingRedirect;
        private final int depth;
        private final HTTPSamplerBase sampler;
        private final JMeterContext jmeterContextOfParentThread;

        ASyncSample(URL url, String method,
                boolean areFollowingRedirect, int depth,  CookieManager cookieManager, HTTPSamplerBase base) {
            this.url = url;
            this.method = method;
            this.areFollowingRedirect = areFollowingRedirect;
            this.depth = depth;
            this.sampler = (HTTPSamplerBase) base.clone();
            // We don't want to use CacheManager clone but the parent one, and CacheManager is Thread Safe
            CacheManager cacheManager = base.getCacheManager();
            if (cacheManager != null) {
                this.sampler.setCacheManagerProperty(cacheManager.createCacheManagerProxy());
            }

            if (cookieManager != null) {
                CookieManager clonedCookieManager = (CookieManager) cookieManager.clone();
                this.sampler.setCookieManagerProperty(clonedCookieManager);
            }
            this.sampler.setMD5(this.sampler.useMD5() || IGNORE_EMBEDDED_RESOURCES_DATA);
            this.jmeterContextOfParentThread = JMeterContextService.getContext();
        }

        @Override
        public AsynSamplerResultHolder call() {
            JMeterContextService.replaceContext(jmeterContextOfParentThread);
            HTTPSampleResult httpSampleResult = sampler.sample(url, method, areFollowingRedirect, depth);
            if (sampler.getCookieManager() != null) {
                CollectionProperty cookies = sampler.getCookieManager().getCookies();
                return new AsynSamplerResultHolder(httpSampleResult, cookies);
            } else {
                return new AsynSamplerResultHolder(httpSampleResult, new CollectionProperty());
            }
        }
    }

    /**
     * @see org.apache.jmeter.samplers.AbstractSampler#applies(org.apache.jmeter.config.ConfigTestElement)
     */
    @Override
    public boolean applies(ConfigTestElement configElement) {
        String guiClass = configElement.getProperty(TestElement.GUI_CLASS).getStringValue();
        return APPLIABLE_CONFIG_CLASSES.contains(guiClass);
    }

    /**
     * Replace by replaceBy in path and body (arguments) properties
     */
    @Override
    public int replace(String regex, String replaceBy, boolean caseSensitive) throws Exception {
        int totalReplaced = 0;
        for (JMeterProperty jMeterProperty : getArguments()) {
            HTTPArgument arg = (HTTPArgument) jMeterProperty.getObjectValue();
            String value = arg.getValue();
            if(!StringUtils.isEmpty(value)) {
                Object[] result = JOrphanUtils.replaceAllWithRegex(value, regex, replaceBy, caseSensitive);
                // check if there is anything to replace
                int nbReplaced = ((Integer)result[1]).intValue();
                if (nbReplaced>0) {
                    String replacedText = (String) result[0];
                    arg.setValue(replacedText);
                    totalReplaced += nbReplaced;
                }
            }
        }
        String value = getPath();
        if(!StringUtils.isEmpty(value)) {
            Object[] result = JOrphanUtils.replaceAllWithRegex(value, regex, replaceBy, caseSensitive);
            // check if there is anything to replace
            int nbReplaced = ((Integer)result[1]).intValue();
            if (nbReplaced>0) {
                String replacedText = (String) result[0];
                setPath(replacedText);
                totalReplaced += nbReplaced;
            }
        }

        if(!StringUtils.isEmpty(getDomain())) {
            Object[] result = JOrphanUtils.replaceAllWithRegex(getDomain(), regex, replaceBy, caseSensitive);            
            // check if there is anything to replace
            int nbReplaced = ((Integer)result[1]).intValue();
            if (nbReplaced>0) {
                String replacedText = (String) result[0];
                setDomain(replacedText);
                totalReplaced += nbReplaced;
            }
        }
        return totalReplaced;
    }
}
