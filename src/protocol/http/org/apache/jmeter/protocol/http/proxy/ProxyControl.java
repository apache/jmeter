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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.conn.ssl.AbstractVerifier;
import org.apache.jmeter.assertions.Assertion;
import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.assertions.gui.AssertionGui;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigElement;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.control.gui.TransactionControllerGui;
import org.apache.jmeter.engine.util.ValueReplacer;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.processor.PostProcessor;
import org.apache.jmeter.processor.PreProcessor;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.AuthManager.Mechanism;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.protocol.http.gui.AuthPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleListener;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBeanHelper;
import org.apache.jmeter.testelement.NonTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.timers.Timer;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.Visualizer;
import org.apache.jorphan.exec.KeyToolUtils;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.oro.text.MalformedCachePatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class handles storing of generated samples, etc
 * For unit tests, see TestProxyControl
 */
public class ProxyControl extends GenericController implements NonTestElement {

    private static final Logger log = LoggerFactory.getLogger(ProxyControl.class);

    private static final long serialVersionUID = 240L;

    private static final String ASSERTION_GUI = AssertionGui.class.getName();


    private static final String TRANSACTION_CONTROLLER_GUI = TransactionControllerGui.class.getName();

    private static final String LOGIC_CONTROLLER_GUI = LogicControllerGui.class.getName();

    private static final String AUTH_PANEL = AuthPanel.class.getName();

    private static final String AUTH_MANAGER = AuthManager.class.getName();

    public static final int DEFAULT_PORT = 8888;

    // and as a string
    public static final String DEFAULT_PORT_S =
        Integer.toString(DEFAULT_PORT);// Used by GUI

    //+ JMX file attributes
    private static final String PORT = "ProxyControlGui.port"; // $NON-NLS-1$

    private static final String DOMAINS = "ProxyControlGui.domains"; // $NON-NLS-1$

    private static final String EXCLUDE_LIST = "ProxyControlGui.exclude_list"; // $NON-NLS-1$

    private static final String INCLUDE_LIST = "ProxyControlGui.include_list"; // $NON-NLS-1$

    private static final String CAPTURE_HTTP_HEADERS = "ProxyControlGui.capture_http_headers"; // $NON-NLS-1$

    private static final String ADD_ASSERTIONS = "ProxyControlGui.add_assertion"; // $NON-NLS-1$

    private static final String GROUPING_MODE = "ProxyControlGui.grouping_mode"; // $NON-NLS-1$

    private static final String SAMPLER_TYPE_NAME = "ProxyControlGui.sampler_type_name"; // $NON-NLS-1$

    private static final String SAMPLER_REDIRECT_AUTOMATICALLY = "ProxyControlGui.sampler_redirect_automatically"; // $NON-NLS-1$

    private static final String SAMPLER_FOLLOW_REDIRECTS = "ProxyControlGui.sampler_follow_redirects"; // $NON-NLS-1$

    private static final String USE_KEEPALIVE = "ProxyControlGui.use_keepalive"; // $NON-NLS-1$

    private static final String SAMPLER_DOWNLOAD_IMAGES = "ProxyControlGui.sampler_download_images"; // $NON-NLS-1$

    private static final String HTTP_SAMPLER_NAMING_MODE = "ProxyControlGui.proxy_http_sampler_naming_mode"; // $NON-NLS-1$

    private static final String PREFIX_HTTP_SAMPLER_NAME = "ProxyControlGui.proxy_prefix_http_sampler_name"; // $NON-NLS-1$

    private static final String PROXY_PAUSE_HTTP_SAMPLER = "ProxyControlGui.proxy_pause_http_sampler"; // $NON-NLS-1$

    private static final String DEFAULT_ENCODING_PROPERTY = "ProxyControlGui.default_encoding"; // $NON-NLS-1$

    private static final String REGEX_MATCH = "ProxyControlGui.regex_match"; // $NON-NLS-1$

    private static final String CONTENT_TYPE_EXCLUDE = "ProxyControlGui.content_type_exclude"; // $NON-NLS-1$

    private static final String CONTENT_TYPE_INCLUDE = "ProxyControlGui.content_type_include"; // $NON-NLS-1$

    private static final String NOTIFY_CHILD_SAMPLER_LISTENERS_FILTERED = "ProxyControlGui.notify_child_sl_filtered"; // $NON-NLS-1$

    private static final String BEARER_AUTH = "Bearer";

    private static final String BASIC_AUTH = "Basic"; // $NON-NLS-1$

    private static final String DIGEST_AUTH = "Digest"; // $NON-NLS-1$

    //- JMX file attributes

    // Must agree with the order of entries in the drop-down
    // created in ProxyControlGui.createGroupingPanel()
    private static final int GROUPING_ADD_SEPARATORS = 1;
    private static final int GROUPING_IN_SIMPLE_CONTROLLERS = 2;
    private static final int GROUPING_STORE_FIRST_ONLY = 3;
    private static final int GROUPING_IN_TRANSACTION_CONTROLLERS = 4;


    // Original numeric order (we now use strings)
    private static final String SAMPLER_TYPE_HTTP_SAMPLER_JAVA = "0";
    private static final String SAMPLER_TYPE_HTTP_SAMPLER_HC3_1 = "1";
    private static final String SAMPLER_TYPE_HTTP_SAMPLER_HC4 = "2";

    private long sampleGap; // $NON-NLS-1$
    // Detect if user has pressed a new link

    // for ssl connection
    private static final String KEYSTORE_TYPE =
        JMeterUtils.getPropDefault("proxy.cert.type", "JKS"); // $NON-NLS-1$ $NON-NLS-2$

    // Proxy configuration SSL
    private static final String CERT_DIRECTORY =
        JMeterUtils.getPropDefault("proxy.cert.directory", JMeterUtils.getJMeterBinDir()); // $NON-NLS-1$

    private static final String CERT_FILE_DEFAULT = "proxyserver.jks";// $NON-NLS-1$

    private static final String CERT_FILE =
        JMeterUtils.getPropDefault("proxy.cert.file", CERT_FILE_DEFAULT); // $NON-NLS-1$

    private static final File CERT_PATH = new File(CERT_DIRECTORY, CERT_FILE);

    private static final String CERT_PATH_ABS = CERT_PATH.getAbsolutePath();

    private static final String DEFAULT_PASSWORD = "password"; // $NON-NLS-1$ NOSONAR only default password, if user has not defined one

    /**
     * Keys for user preferences
     */
    private static final String USER_PASSWORD_KEY = "proxy_cert_password"; // NOSONAR not a hardcoded password

    /**
     * Note: Windows user preferences are stored relative to: HKEY_CURRENT_USER\Software\JavaSoft\Prefs
     */
    private static final Preferences PREFERENCES = Preferences.userNodeForPackage(ProxyControl.class);

    /**
     * Whether to use dynamic key generation (if supported)
     */
    private static final boolean USE_DYNAMIC_KEYS = JMeterUtils.getPropDefault("proxy.cert.dynamic_keys", true); // $NON-NLS-1$

    // The alias to be used if dynamic host names are not possible
    static final String JMETER_SERVER_ALIAS = ":jmeter:"; // $NON-NLS-1$

    public static final int CERT_VALIDITY = JMeterUtils.getPropDefault("proxy.cert.validity", 7); // $NON-NLS-1$

    // If this is defined, it is assumed to be the alias of a user-supplied certificate; overrides dynamic mode
    static final String CERT_ALIAS = JMeterUtils.getProperty("proxy.cert.alias"); // $NON-NLS-1$

    public enum KeystoreMode {
        USER_KEYSTORE,   // user-provided keystore
        JMETER_KEYSTORE, // keystore generated by JMeter; single entry
        DYNAMIC_KEYSTORE, // keystore generated by JMeter; dynamic entries
        NONE             // cannot use keystore
    }

    static final KeystoreMode KEYSTORE_MODE;

    static {
        if (CERT_ALIAS != null) {
            KEYSTORE_MODE = KeystoreMode.USER_KEYSTORE;
            log.info(
                    "HTTP(S) Test Script Recorder will use the keystore '{}' with the alias: '{}'",
                    CERT_PATH_ABS, CERT_ALIAS);
        } else {
            if (!KeyToolUtils.haveKeytool()) {
                KEYSTORE_MODE = KeystoreMode.NONE;
            } else if (USE_DYNAMIC_KEYS) {
                KEYSTORE_MODE = KeystoreMode.DYNAMIC_KEYSTORE;
                log.info(
                        "HTTP(S) Test Script Recorder SSL Proxy will use keys that support embedded 3rd party resources in file {}",
                        CERT_PATH_ABS);
            } else {
                KEYSTORE_MODE = KeystoreMode.JMETER_KEYSTORE;
                log.warn(
                        "HTTP(S) Test Script Recorder SSL Proxy will use keys that may not work for embedded resources in file {}",
                        CERT_PATH_ABS);
            }
        }
    }

    /**
     * Whether to use the redirect disabling feature (can be switched off if it does not work)
     */
    private static final boolean ATTEMPT_REDIRECT_DISABLING =
            JMeterUtils.getPropDefault("proxy.redirect.disabling", true); // $NON-NLS-1$

    /**
     * Although this field is mutable, it is only accessed within the synchronized method deliverSampler()
     */
    private static String LAST_REDIRECT = null;

    /*
     * TODO this assumes that the redirected response will always immediately follow the original response.
     * This may not always be true.
     * Is there a better way to do this?
     */
    private transient Daemon server;

    private long lastTime = 0;// When was the last sample seen?

    private transient KeyStore keyStore;

    private volatile boolean addAssertions = false;

    private volatile int groupingMode = 0;

    private volatile boolean samplerRedirectAutomatically = false;

    private volatile boolean samplerFollowRedirects = false;

    private volatile boolean useKeepAlive = false;

    private volatile boolean samplerDownloadImages = false;

    private volatile boolean notifyChildSamplerListenersOfFilteredSamples = true;

    private volatile boolean regexMatch = false;// Should we match using regexes?

    private Set<Class<?>> addableInterfaces = new HashSet<>(
            Arrays.asList(Visualizer.class, ConfigElement.class,
                    Assertion.class, Timer.class, PreProcessor.class,
                    PostProcessor.class, SampleListener.class));

    /**
     * Tree node where the samples should be stored.
     * <p>
     * This property is not persistent.
     */
    private JMeterTreeNode target;

    private String storePassword;

    private String keyPassword;

    private JMeterTreeModel nonGuiTreeModel;

    public ProxyControl() {
        setPort(DEFAULT_PORT);
        setExcludeList(new HashSet<>());
        setIncludeList(new HashSet<>());
        setCaptureHttpHeaders(true); // maintain original behaviour
    }

    /**
     * Set a {@link JMeterTreeModel} to be used by the ProxyControl, when used
     * in a non-GUI environment, where the {@link JMeterTreeModel} can't be
     * acquired through {@link GuiPackage#getTreeModel()}
     *
     * @param treeModel
     *            the {@link JMeterTreeModel} to be used, or {@code null} when
     *            the GUI model should be used
     */
    public void setNonGuiTreeModel(JMeterTreeModel treeModel) {
        this.nonGuiTreeModel = treeModel;
    }

    public void setPort(int port) {
        this.setProperty(new IntegerProperty(PORT, port));
    }

    public void setPort(String port) {
        setProperty(PORT, port);
    }

    public void setSslDomains(String domains) {
        setProperty(DOMAINS, domains, "");
    }

    public String getSslDomains() {
        return getPropertyAsString(DOMAINS,"");
    }

    public void setCaptureHttpHeaders(boolean capture) {
        setProperty(new BooleanProperty(CAPTURE_HTTP_HEADERS, capture));
    }

    public void setGroupingMode(int grouping) {
        this.groupingMode = grouping;
        setProperty(new IntegerProperty(GROUPING_MODE, grouping));
    }

    public void setAssertions(boolean b) {
        addAssertions = b;
        setProperty(new BooleanProperty(ADD_ASSERTIONS, b));
    }

    public void setSamplerTypeName(String samplerTypeName) {
        setProperty(new StringProperty(SAMPLER_TYPE_NAME, samplerTypeName));
    }
    public void setSamplerRedirectAutomatically(boolean b) {
        samplerRedirectAutomatically = b;
        setProperty(new BooleanProperty(SAMPLER_REDIRECT_AUTOMATICALLY, b));
    }

    public void setSamplerFollowRedirects(boolean b) {
        samplerFollowRedirects = b;
        setProperty(new BooleanProperty(SAMPLER_FOLLOW_REDIRECTS, b));
    }

    /**
     * @param b flag whether keep alive should be used
     */
    public void setUseKeepAlive(boolean b) {
        useKeepAlive = b;
        setProperty(new BooleanProperty(USE_KEEPALIVE, b));
    }

    public void setSamplerDownloadImages(boolean b) {
        samplerDownloadImages = b;
        setProperty(new BooleanProperty(SAMPLER_DOWNLOAD_IMAGES, b));
    }

    public void setHTTPSampleNamingMode(int httpNamingMode) {
        setProperty(new IntegerProperty(HTTP_SAMPLER_NAMING_MODE, httpNamingMode));
    }

    public String getDefaultEncoding() {
        return getPropertyAsString(DEFAULT_ENCODING_PROPERTY, StandardCharsets.UTF_8.name());
    }
    public void setDefaultEncoding(String defaultEncoding) {
        setProperty(DEFAULT_ENCODING_PROPERTY, defaultEncoding);
    }

    public void setPrefixHTTPSampleName(String prefixHTTPSampleName) {
        setProperty(PREFIX_HTTP_SAMPLER_NAME, prefixHTTPSampleName);
    }

    public void setProxyPauseHTTPSample(String proxyPauseHTTPSample) {
        setProperty(PROXY_PAUSE_HTTP_SAMPLER, proxyPauseHTTPSample);
    }

    public void setNotifyChildSamplerListenerOfFilteredSamplers(boolean b) {
        notifyChildSamplerListenersOfFilteredSamples = b;
        setProperty(new BooleanProperty(NOTIFY_CHILD_SAMPLER_LISTENERS_FILTERED, b));
    }

    public void setIncludeList(Collection<String> list) {
        setProperty(new CollectionProperty(INCLUDE_LIST, new HashSet<>(list)));
    }

    public void setExcludeList(Collection<String> list) {
        setProperty(new CollectionProperty(EXCLUDE_LIST, new HashSet<>(list)));
    }

    /**
     * @param b flag whether regex matching should be used
     */
    public void setRegexMatch(boolean b) {
        regexMatch = b;
        setProperty(new BooleanProperty(REGEX_MATCH, b));
    }

    public void setContentTypeExclude(String contentTypeExclude) {
        setProperty(new StringProperty(CONTENT_TYPE_EXCLUDE, contentTypeExclude));
    }

    public void setContentTypeInclude(String contentTypeInclude) {
        setProperty(new StringProperty(CONTENT_TYPE_INCLUDE, contentTypeInclude));
    }

    public boolean getAssertions() {
        return getPropertyAsBoolean(ADD_ASSERTIONS);
    }

    public int getGroupingMode() {
        return getPropertyAsInt(GROUPING_MODE);
    }

    public int getPort() {
        return getPropertyAsInt(PORT);
    }

    public String getPortString() {
        return getPropertyAsString(PORT);
    }

    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

    public boolean getCaptureHttpHeaders() {
        return getPropertyAsBoolean(CAPTURE_HTTP_HEADERS);
    }

    public String getSamplerTypeName() {
        // Convert the old numeric types - just in case someone wants to reload the workbench
        String type = getPropertyAsString(SAMPLER_TYPE_NAME);
        if (SAMPLER_TYPE_HTTP_SAMPLER_JAVA.equals(type)){
            type = HTTPSamplerFactory.IMPL_JAVA;
        } else if (SAMPLER_TYPE_HTTP_SAMPLER_HC3_1.equals(type)){
            type = HTTPSamplerFactory.IMPL_HTTP_CLIENT4;
        } else if (SAMPLER_TYPE_HTTP_SAMPLER_HC4.equals(type)){
            type = HTTPSamplerFactory.IMPL_HTTP_CLIENT4;
        }
        return type;
    }

    public boolean getSamplerRedirectAutomatically() {
        return getPropertyAsBoolean(SAMPLER_REDIRECT_AUTOMATICALLY, false);
    }

    public boolean getSamplerFollowRedirects() {
        return getPropertyAsBoolean(SAMPLER_FOLLOW_REDIRECTS, true);
    }

    public boolean getUseKeepalive() {
        return getPropertyAsBoolean(USE_KEEPALIVE, true);
    }

    public boolean getSamplerDownloadImages() {
        return getPropertyAsBoolean(SAMPLER_DOWNLOAD_IMAGES, false);
    }

    public int getHTTPSampleNamingMode() {
        return getPropertyAsInt(HTTP_SAMPLER_NAMING_MODE);
    }

    public String getPrefixHTTPSampleName() {
        return getPropertyAsString(PREFIX_HTTP_SAMPLER_NAME);
    }

    public String getProxyPauseHTTPSample() {
        return getPropertyAsString(PROXY_PAUSE_HTTP_SAMPLER);
    }

    public boolean getNotifyChildSamplerListenerOfFilteredSamplers() {
        return getPropertyAsBoolean(NOTIFY_CHILD_SAMPLER_LISTENERS_FILTERED, true);
    }

    public boolean getRegexMatch() {
        return getPropertyAsBoolean(REGEX_MATCH, false);
    }

    public String getContentTypeExclude() {
        return getPropertyAsString(CONTENT_TYPE_EXCLUDE);
    }

    public String getContentTypeInclude() {
        return getPropertyAsString(CONTENT_TYPE_INCLUDE);
    }

    /**
     * @return the {@link JMeterTreeModel} used when run in non-GUI mode, or {@code null} when run in GUI mode
     */
    public JMeterTreeModel getNonGuiTreeModel() {
        return nonGuiTreeModel;
    }

    public void addConfigElement(ConfigElement config) {
        // NOOP
    }

    public void startProxy() throws IOException {
        try {
            initKeyStore();
        } catch (GeneralSecurityException e) {
            log.error("Could not initialise key store", e);
            throw new IOException("Could not create keystore", e);
        } catch (IOException e) { // make sure we log the error
            log.error("Could not initialise key store", e);
            throw e;
        }
        notifyTestListenersOfStart();
        try {
            server = new Daemon(getPort(), this);
            if (getProxyPauseHTTPSample().isEmpty()) {
                sampleGap = JMeterUtils.getPropDefault("proxy.pause", 5000);
            } else {
                sampleGap = Long.parseLong(getProxyPauseHTTPSample().trim());
            }
            server.start();
            if (GuiPackage.getInstance() != null) {
                GuiPackage.getInstance().register(server);
            }
        } catch (IOException e) {
            log.error("Could not create HTTP(S) Test Script Recorder Proxy daemon", e);
            throw e;
        }
    }

    public void addExcludedPattern(String pattern) {
        getExcludePatterns().addItem(pattern);
    }

    public CollectionProperty getExcludePatterns() {
        return (CollectionProperty) getProperty(EXCLUDE_LIST);
    }

    public void addIncludedPattern(String pattern) {
        getIncludePatterns().addItem(pattern);
    }

    public CollectionProperty getIncludePatterns() {
        return (CollectionProperty) getProperty(INCLUDE_LIST);
    }

    public void clearExcludedPatterns() {
        getExcludePatterns().clear();
    }

    public void clearIncludedPatterns() {
        getIncludePatterns().clear();
    }

    /**
     * @return the target controller node
     */
    public JMeterTreeNode getTarget() {
        return target;
    }

    /**
     * Sets the target node where the samples generated by the proxy have to be
     * stored.
     *
     * @param target target node to store generated samples
     */
    public void setTarget(JMeterTreeNode target) {
        this.target = target;
    }

    /**
     * Receives the recorded sampler from the proxy server for placing in the
     * test tree; this is skipped if the sampler is null (e.g. for recording SSL errors)
     * Always sends the result to any registered sample listeners.
     *
     * @param sampler the sampler, may be null
     * @param testElements the test elements to be added (e.g. header manager) under the Sampler
     * @param result the sample result, not null
     * TODO param serverResponse to be added to allow saving of the
     * server's response while recording.
     */
    public synchronized void deliverSampler(final HTTPSamplerBase sampler, final TestElement[] testElements, final SampleResult result) {
        boolean notifySampleListeners = true;
        if (sampler != null) {
            if (ATTEMPT_REDIRECT_DISABLING && (samplerRedirectAutomatically || samplerFollowRedirects)
                    && result instanceof HTTPSampleResult) {
                final HTTPSampleResult httpSampleResult = (HTTPSampleResult) result;
                final String urlAsString = httpSampleResult.getUrlAsString();
                if (urlAsString.equals(LAST_REDIRECT)) { // the url matches the last redirect
                    sampler.setEnabled(false);
                    sampler.setComment("Detected a redirect from the previous sample");
                } else { // this is not the result of a redirect
                    LAST_REDIRECT = null; // so break the chain
                }
                if (httpSampleResult.isRedirect()) { // Save Location so resulting sample can be disabled
                    if (LAST_REDIRECT == null) {
                        sampler.setComment("Detected the start of a redirect chain");
                    }
                    LAST_REDIRECT = httpSampleResult.getRedirectLocation();
                } else {
                    LAST_REDIRECT = null;
                }
            }
            if (filterContentType(result) && filterUrl(sampler)) {
                JMeterTreeNode myTarget = findTargetControllerNode();
                @SuppressWarnings("unchecked") // OK, because find only returns correct element types
                Collection<ConfigTestElement> defaultConfigurations = (Collection<ConfigTestElement>) findApplicableElements(
                        myTarget, ConfigTestElement.class, false);
                @SuppressWarnings("unchecked") // OK, because find only returns correct element types
                Collection<Arguments> userDefinedVariables = (Collection<Arguments>) findApplicableElements(
                        myTarget, Arguments.class, true);

                removeValuesFromSampler(sampler, defaultConfigurations);
                replaceValues(sampler, testElements, userDefinedVariables);
                sampler.setAutoRedirects(samplerRedirectAutomatically);
                sampler.setFollowRedirects(samplerFollowRedirects);
                sampler.setUseKeepAlive(useKeepAlive);
                sampler.setImageParser(samplerDownloadImages);
                Authorization authorization = createAuthorization(testElements, sampler);
                if (authorization != null) {
                    setAuthorization(authorization, myTarget);
                }
                placeSampler(sampler, testElements, myTarget);
            } else {
                if(log.isDebugEnabled()) {
                    log.debug(
                            "Sample excluded based on url or content-type: {} - {}",
                            result.getUrlAsString(), result.getContentType());
                }
                notifySampleListeners = notifyChildSamplerListenersOfFilteredSamples;
                result.setSampleLabel("["+result.getSampleLabel()+"]");
            }
        }
        if(notifySampleListeners) {
            // SampleEvent is not passed JMeterVariables, because they don't make sense for Proxy Recording
            notifySampleListeners(new SampleEvent(result, "WorkBench"));
        } else {
            log.debug(
                    "Sample not delivered to Child Sampler Listener based on url or content-type: {} - {}",
                    result.getUrlAsString(), result.getContentType());
        }
    }

    /**
     * Detect Header manager in subConfigs,
     * Find(if any) Authorization header
     * Construct Authentication object
     * Removes Authorization if present
     *
     * @param subConfigs {@link TestElement}[]
     * @param sampler {@link HTTPSamplerBase}
     * @return {@link Authorization}
     */
    private Authorization createAuthorization(final TestElement[] testElements, HTTPSamplerBase sampler) {
        Header authHeader;
        Authorization authorization = null;
        // Iterate over subconfig elements searching for HeaderManager
        for (TestElement te : testElements) {
            if (te instanceof HeaderManager) {
                @SuppressWarnings("unchecked") // headers should only contain the correct classes
                List<TestElementProperty> headers = (ArrayList<TestElementProperty>) ((HeaderManager) te).getHeaders().getObjectValue();
                for (Iterator<?> iterator = headers.iterator(); iterator.hasNext();) {
                    TestElementProperty tep = (TestElementProperty) iterator
                            .next();
                    if (tep.getName().equals(HTTPConstants.HEADER_AUTHORIZATION)) {
                        //Construct Authorization object from HEADER_AUTHORIZATION
                        authHeader = (Header) tep.getObjectValue();
                        String headerValue = authHeader.getValue().trim();
                        String[] authHeaderContent = headerValue.split(" ");//$NON-NLS-1$
                        String authType;
                        String authCredentialsBase64;
                        if(authHeaderContent.length>=2) {
                            authType = authHeaderContent[0];
                            // if HEADER_AUTHORIZATION contains "Basic"
                            // then set Mechanism.BASIC_DIGEST, otherwise Mechanism.KERBEROS
                            Mechanism mechanism;
                            switch (authType) {
                                case BEARER_AUTH:
                                    // This one will need to be correlated manually by user
                                    return null;
                                case DIGEST_AUTH:
                                    mechanism = Mechanism.DIGEST;
                                    break;
                                case BASIC_AUTH:
                                    mechanism = Mechanism.BASIC;
                                    break;
                                default:
                                    mechanism = Mechanism.KERBEROS;
                                    break;
                            }
                            authCredentialsBase64 = authHeaderContent[1];
                            authorization=new Authorization();
                            try {
                                authorization.setURL(sampler.getUrl().toExternalForm());
                            } catch (MalformedURLException e) {
                                log.error("Error filling url on authorization, message: {}", e.getMessage(), e);
                                authorization.setURL("${AUTH_BASE_URL}");//$NON-NLS-1$
                            }
                            authorization.setMechanism(mechanism);
                            if(BASIC_AUTH.equals(authType)) {
                                String authCred= new String(Base64.decodeBase64(authCredentialsBase64));
                                String[] loginPassword = authCred.split(":"); //$NON-NLS-1$
                                if(loginPassword.length == 2) {
                                    authorization.setUser(loginPassword[0]);
                                    authorization.setPass(loginPassword[1]);
                                } else {
                                    log.error("Error parsing BASIC Auth authorization header:'{}', decoded value:'{}'",
                                            authCredentialsBase64, authCred);
                                    // we keep initial header
                                    return null;
                                }
                            } else {
                                // Digest or Kerberos
                                authorization.setUser("${AUTH_LOGIN}");//$NON-NLS-1$
                                authorization.setPass("${AUTH_PASSWORD}");//$NON-NLS-1$
                            }
                        }
                        // remove HEADER_AUTHORIZATION from HeaderManager
                        // because it's useless after creating Authorization object
                        iterator.remove();
                        break;
                    }
                }
            }
        }
        return authorization;
    }

    public void stopProxy() {
        if (server != null) {
            server.stopServer();
            if (GuiPackage.getInstance() != null) {
                GuiPackage.getInstance().unregister(server);
            }
            try {
                server.join(1000); // wait for server to stop
            } catch (InterruptedException e) {
                //NOOP
                Thread.currentThread().interrupt();
            }
            notifyTestListenersOfEnd();
            server = null;
        }
    }

    public String[] getCertificateDetails() {
        if (isDynamicMode()) {
            try {
                X509Certificate caCert = (X509Certificate) keyStore.getCertificate(KeyToolUtils.getRootCAalias());
                if (caCert == null) {
                    return new String[]{"Could not find certificate"};
                }
                return new String[]
                        {
                        caCert.getSubjectX500Principal().toString(),
                        "Fingerprint(SHA1): " + JOrphanUtils.baToHexString(DigestUtils.sha1(caCert.getEncoded()), ' '),
                        "Created: "+ caCert.getNotBefore().toString()
                        };
            } catch (GeneralSecurityException e) {
                log.error("Problem reading root CA from keystore", e);
                return new String[] { "Problem with root certificate", e.getMessage() };
            }
        }
        return new String[0]; // should not happen
    }
    // Package protected to allow test case access
    boolean filterUrl(HTTPSamplerBase sampler) {
        String domain = sampler.getDomain();
        if (domain == null || domain.length() == 0) {
            return false;
        }

        String url = generateMatchUrl(sampler);
        CollectionProperty includePatterns = getIncludePatterns();
        if (includePatterns.size() > 0 && !matchesPatterns(url, includePatterns)) {
            return false;
        }

        CollectionProperty excludePatterns = getExcludePatterns();
        if (excludePatterns.size() > 0 && matchesPatterns(url, excludePatterns)) {
            return false;
        }

        return true;
    }

    // Package protected to allow test case access
    /**
     * Filter the response based on the content type.
     * If no include nor exclude filter is specified, the result will be included
     *
     * @param result the sample result to check
     * @return <code>true</code> means result will be kept
     */
    boolean filterContentType(SampleResult result) {
        String includeExp = getContentTypeInclude();
        String excludeExp = getContentTypeExclude();
        // If no expressions are specified, we let the sample pass
        if((includeExp == null || includeExp.length() == 0) &&
                (excludeExp == null || excludeExp.length() == 0)
                )
        {
            return true;
        }

        // Check that we have a content type
        String sampleContentType = result.getContentType();
        if(sampleContentType == null || sampleContentType.length() == 0) {
            if(log.isDebugEnabled()) {
                log.debug("No Content-type found for : {}", result.getUrlAsString());
            }

            return true;
        }

        if(log.isDebugEnabled()) {
            log.debug("Content-type to filter : {}", sampleContentType);
        }

        // Check if the include pattern is matched
        boolean matched = testPattern(includeExp, sampleContentType, true);
        if(!matched) {
            return false;
        }

        // Check if the exclude pattern is matched
        matched = testPattern(excludeExp, sampleContentType, false);
        if(!matched) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if matching pattern was different from expectedToMatch
     * @param expression Expression to match
     * @param sampleContentType
     * @return boolean true if Matching expression
     */
    private boolean testPattern(String expression, String sampleContentType, boolean expectedToMatch) {
        if(expression != null && expression.length() > 0) {
            if(log.isDebugEnabled()) {
                log.debug(
                        "Testing Expression : {} on sampleContentType: {}, expected to match: {}",
                        expression, sampleContentType, expectedToMatch);
            }

            Pattern pattern = null;
            try {
                pattern = JMeterUtils.getPatternCache().getPattern(expression, Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
                if(JMeterUtils.getMatcher().contains(sampleContentType, pattern) != expectedToMatch) {
                    return false;
                }
            } catch (MalformedCachePatternException e) {
                log.warn("Skipped invalid content pattern: {}", expression, e);
            }
        }
        return true;
    }

    /**
     * Find if there is any AuthManager in JMeterTreeModel
     * If there is no one, create and add it to tree
     * Add authorization object to AuthManager
     * @param authorization {@link Authorization}
     * @param target {@link JMeterTreeNode}
     */
    private void setAuthorization(Authorization authorization, JMeterTreeNode target) {
        JMeterTreeModel jmeterTreeModel = getJmeterTreeModel();
        List<JMeterTreeNode> authManagerNodes = jmeterTreeModel.getNodesOfType(AuthManager.class);
        if (authManagerNodes.isEmpty()) {
            try {
                log.debug("Creating HTTP Authentication manager for authorization: {}", authorization);
                AuthManager authManager = newAuthorizationManager(authorization);
                jmeterTreeModel.addComponent(authManager, target);
            } catch (IllegalUserActionException e) {
                log.error("Failed to add Authorization Manager to target node: {}", target.getName(), e);
            }
        } else{
            AuthManager authManager=(AuthManager)authManagerNodes.get(0).getTestElement();
            authManager.addAuth(authorization);
        }
    }

    private JMeterTreeModel getJmeterTreeModel() {
        if (this.nonGuiTreeModel == null) {
            return GuiPackage.getInstance().getTreeModel();
        }
        return this.nonGuiTreeModel;
    }

    /**
     * Helper method to add a Response Assertion
     * Called from AWT Event thread
     */
    private void addAssertion(JMeterTreeModel model, JMeterTreeNode node) throws IllegalUserActionException {
        ResponseAssertion ra = new ResponseAssertion();
        ra.setProperty(TestElement.GUI_CLASS, ASSERTION_GUI);
        ra.setName(JMeterUtils.getResString("assertion_title")); // $NON-NLS-1$
        ra.setTestFieldResponseData();
        model.addComponent(ra, node);
    }

    /**
     * Construct AuthManager
     * @param authorization
     * @return AuthManager
     * @throws IllegalUserActionException
     */
    private AuthManager newAuthorizationManager(Authorization authorization) throws IllegalUserActionException {
        AuthManager authManager = new AuthManager();
        authManager.setProperty(TestElement.GUI_CLASS, AUTH_PANEL);
        authManager.setProperty(TestElement.TEST_CLASS, AUTH_MANAGER);
        authManager.setName("HTTP Authorization Manager");
        authManager.addAuth(authorization);
        return authManager;
    }

    /**
     * Helper method to add a Divider
     * Called from Application Thread that needs to update GUI (JMeterTreeModel)
     */
    private void addDivider(final JMeterTreeModel model, final JMeterTreeNode node) {
        final GenericController sc = new GenericController();
        sc.setProperty(TestElement.GUI_CLASS, LOGIC_CONTROLLER_GUI);
        sc.setName("-------------------"); // $NON-NLS-1$
        safelyAddComponent(model, node, sc);
    }

    private void safelyAddComponent(
            final JMeterTreeModel model,
            final JMeterTreeNode node,
            final GenericController controller) {
        JMeterUtils.runSafe(true, () -> {
            try {
                model.addComponent(controller, node);
            } catch (IllegalUserActionException e) {
                log.error("Program error", e);
                throw new Error(e);
            }
        });
    }

    /**
     * Helper method to add a Simple Controller to contain the samplers.
     * Called from Application Thread that needs to update GUI (JMeterTreeModel)
     * @param model
     *            Test component tree model
     * @param node
     *            Node in the tree where we will add the Controller
     * @param name
     *            A name for the Controller
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    private void addSimpleController(final JMeterTreeModel model, final JMeterTreeNode node, String name)
            throws InterruptedException, InvocationTargetException {
        final GenericController sc = new GenericController();
        sc.setProperty(TestElement.GUI_CLASS, LOGIC_CONTROLLER_GUI);
        sc.setName(name);
        safelyAddComponent(model, node, sc);
    }

    /**
     * Helper method to add a Transaction Controller to contain the samplers.
     * Called from Application Thread that needs to update GUI (JMeterTreeModel)
     * @param model
     *            Test component tree model
     * @param node
     *            Node in the tree where we will add the Controller
     * @param name
     *            A name for the Controller
     * @throws InvocationTargetException
     * @throws InterruptedException
     */
    private void addTransactionController(final JMeterTreeModel model, final JMeterTreeNode node, String name)
            throws InterruptedException, InvocationTargetException {
        final TransactionController sc = new TransactionController();
        sc.setIncludeTimers(false);
        sc.setProperty(TestElement.GUI_CLASS, TRANSACTION_CONTROLLER_GUI);
        sc.setName(name);
        safelyAddComponent(model, node, sc);
    }
    /**
     * Helper method to replicate any timers found within the Proxy Controller
     * into the provided sampler, while replacing any occurrences of string _T_
     * in the timer's configuration with the provided deltaT.
     * Called from AWT Event thread
     * @param model
     *            Test component tree model
     * @param node
     *            Sampler node in where we will add the timers
     * @param deltaT
     *            Time interval from the previous request
     */
    private void addTimers(JMeterTreeModel model, JMeterTreeNode node, long deltaT) {
        TestPlan variables = new TestPlan();
        variables.addParameter("T", Long.toString(deltaT)); // $NON-NLS-1$
        ValueReplacer replacer = new ValueReplacer(variables);
        JMeterTreeNode mySelf = model.getNodeOf(this);
        if(mySelf != null) {
            Enumeration<?> children = mySelf.children();
            while (children.hasMoreElements()) {
                JMeterTreeNode templateNode = (JMeterTreeNode)children.nextElement();
                if (templateNode.isEnabled()) {
                    TestElement template = templateNode.getTestElement();
                    if (template instanceof Timer) {
                        TestElement timer = (TestElement) template.clone();
                        try {
                            timer.setComment("Recorded:"+Long.toString(deltaT)+"ms");
                            replacer.undoReverseReplace(timer);
                            model.addComponent(timer, node);
                        } catch (InvalidVariableException
                                | IllegalUserActionException e) {
                            // Not 100% sure, but I believe this can't happen, so
                            // I'll log and throw an error:
                            log.error("Program error adding timers", e);
                            throw new Error(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * Finds the first enabled node of a given type in the tree.
     *
     * @param type
     *            class of the node to be found
     *
     * @return the first node of the given type in the test component tree, or
     *         <code>null</code> if none was found.
     */
    private JMeterTreeNode findFirstNodeOfType(Class<?> type) {
        JMeterTreeModel treeModel = getJmeterTreeModel();
        List<JMeterTreeNode> nodes = treeModel.getNodesOfType(type);
        for (JMeterTreeNode node : nodes) {
            if (node.isEnabled()) {
                return node;
            }
        }
        return null;
    }

    /**
     * Finds the controller where samplers have to be stored, that is:
     * <ul>
     * <li>The controller specified by the <code>target</code> property.
     * <li>If none was specified, the first RecordingController in the tree.
     * <li>If none is found, the first AbstractThreadGroup in the tree.
     * </ul>
     *
     * @return the tree node for the controller where the proxy must store the
     *         generated samplers.
     */
    public JMeterTreeNode findTargetControllerNode() {
        JMeterTreeNode myTarget = getTarget();
        if (myTarget != null) {
            return myTarget;
        }
        myTarget = findFirstNodeOfType(RecordingController.class);
        if (myTarget != null) {
            return myTarget;
        }
        myTarget = findFirstNodeOfType(AbstractThreadGroup.class);
        if (myTarget != null) {
            return myTarget;
        }
        log.error("Program error: test script recording target not found.");
        return null;
    }

    /**
     * Finds all configuration objects of the given class applicable to the
     * recorded samplers, that is:
     * <ul>
     * <li>All such elements directly within the HTTP(S) Test Script Recorder (these have
     * the highest priority).
     * <li>All such elements directly within the target controller (higher
     * priority) or directly within any containing controller (lower priority),
     * including the Test Plan itself (lowest priority).
     * </ul>
     *
     * @param myTarget
     *            tree node for the recording target controller.
     * @param myClass
     *            Class of the elements to be found.
     * @param ascending
     *            true if returned elements should be ordered in ascending
     *            priority, false if they should be in descending priority.
     *
     * @return a collection of applicable objects of the given class.
     */
    // TODO - could be converted to generic class?
    private Collection<?> findApplicableElements(JMeterTreeNode myTarget, Class<? extends TestElement> myClass, boolean ascending) {
        JMeterTreeModel treeModel = getJmeterTreeModel();
        LinkedList<TestElement> elements = new LinkedList<>();

        // Look for elements directly within the HTTP proxy:
        JMeterTreeNode node = treeModel.getNodeOf(this);
        if(node != null) {
            Enumeration<?> kids = node.children();
            while (kids.hasMoreElements()) {
                JMeterTreeNode subNode = (JMeterTreeNode) kids.nextElement();
                if (subNode.isEnabled()) {
                    TestElement element = (TestElement) subNode.getUserObject();
                    if (myClass.isInstance(element)) {
                        if (ascending) {
                            elements.addFirst(element);
                        } else {
                            elements.add(element);
                        }
                    }
                }
            }
        }
        // Look for arguments elements in the target controller or higher up:
        for (JMeterTreeNode controller = myTarget; controller != null; controller = (JMeterTreeNode) controller
                .getParent()) {
            Enumeration<?> kids = controller.children();
            while (kids.hasMoreElements()) {
                JMeterTreeNode subNode = (JMeterTreeNode) kids.nextElement();
                if (subNode.isEnabled()) {
                    TestElement element = (TestElement) subNode.getUserObject();
                    if (myClass.isInstance(element)) {
                        log.debug("Applicable: {}", element.getName());
                        if (ascending) {
                            elements.addFirst(element);
                        } else {
                            elements.add(element);
                        }
                    }

                    // Special case for the TestPlan's Arguments sub-element:
                    if (element instanceof TestPlan) {
                        TestPlan tp = (TestPlan) element;
                        Arguments args = tp.getArguments();
                        if (myClass.isInstance(args)) {
                            if (ascending) {
                                elements.addFirst(args);
                            } else {
                                elements.add(args);
                            }
                        }
                    }
                }
            }
        }

        return elements;
    }

    private void placeSampler(final HTTPSamplerBase sampler, final TestElement[] testElements,
            JMeterTreeNode myTarget) {
        try {
            final JMeterTreeModel treeModel = getJmeterTreeModel();

            boolean firstInBatch = false;
            long now = System.currentTimeMillis();
            long deltaT = now - lastTime;
            int cachedGroupingMode = groupingMode;
            if (deltaT > sampleGap) {
                if (!myTarget.isLeaf() && cachedGroupingMode == GROUPING_ADD_SEPARATORS) {
                    addDivider(treeModel, myTarget);
                }
                if (cachedGroupingMode == GROUPING_IN_SIMPLE_CONTROLLERS) {
                    addSimpleController(treeModel, myTarget, sampler.getName());
                }
                if (cachedGroupingMode == GROUPING_IN_TRANSACTION_CONTROLLERS) {
                    addTransactionController(treeModel, myTarget, sampler.getName());
                }
                firstInBatch = true;// Remember this was first in its batch
            }
            if (lastTime == 0) {
                deltaT = 0; // Decent value for timers
            }
            lastTime = now;

            if (cachedGroupingMode == GROUPING_STORE_FIRST_ONLY) {
                if (!firstInBatch) {
                    return; // Huh! don't store this one!
                }

                // If we're not storing subsequent samplers, we'll need the
                // first sampler to do all the work...:
                sampler.setFollowRedirects(true);
                sampler.setImageParser(true);
            }

            if (cachedGroupingMode == GROUPING_IN_SIMPLE_CONTROLLERS ||
                    cachedGroupingMode == GROUPING_IN_TRANSACTION_CONTROLLERS) {
                // Find the last controller in the target to store the
                // sampler there:
                for (int i = myTarget.getChildCount() - 1; i >= 0; i--) {
                    JMeterTreeNode c = (JMeterTreeNode) myTarget.getChildAt(i);
                    if (c.getTestElement() instanceof GenericController) {
                        myTarget = c;
                        break;
                    }
                }
            }
            final long deltaTFinal = deltaT;
            final boolean firstInBatchFinal = firstInBatch;
            final JMeterTreeNode myTargetFinal = myTarget;
            JMeterUtils.runSafe(true, () -> {
                try {
                    final JMeterTreeNode newNode = treeModel.addComponent(sampler, myTargetFinal);
                    if (firstInBatchFinal) {
                        if (addAssertions) {
                            addAssertion(treeModel, newNode);
                        }
                        addTimers(treeModel, newNode, deltaTFinal);
                    }

                    if (testElements != null) {
                        for (TestElement testElement: testElements) {
                            if (isAddableTestElement(testElement)) {
                                treeModel.addComponent(testElement, newNode);
                            }
                        }
                    }
                } catch (IllegalUserActionException e) {
                    log.error("Error placing sampler", e);
                    JMeterUtils.reportErrorToUser(e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Error placing sampler", e);
            JMeterUtils.reportErrorToUser(e.getMessage());
        }
    }

    /**
     * @param testElement {@link TestElement} to add
     * @return true if testElement can be added to Sampler
     */
    private boolean isAddableTestElement(
            TestElement testElement) {
        if (hasCorrectInterface(testElement, addableInterfaces)) {
            if (testElement.getProperty(TestElement.GUI_CLASS) != null) {
                return true;
            } else {
                log.error(
                        "Cannot add element that lacks the {} property as testElement: {}",
                        TestElement.GUI_CLASS, testElement);
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean hasCorrectInterface(Object obj, Set<Class<?>> klasses) {
        for (Class<?> klass: klasses) {
            if (klass != null && klass.isInstance(obj)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Remove from the sampler all values which match the one provided by the
     * first configuration in the given collection which provides a value for
     * that property.
     *
     * @param sampler
     *            Sampler to remove values from.
     * @param configurations
     *            ConfigTestElements in descending priority.
     */
    private void removeValuesFromSampler(HTTPSamplerBase sampler, Collection<ConfigTestElement> configurations) {
        for (PropertyIterator props = sampler.propertyIterator(); props.hasNext();) {
            JMeterProperty prop = props.next();
            String name = prop.getName();
            String value = prop.getStringValue();

            // There's a few properties which are excluded from this processing:
            if (name.equals(TestElement.ENABLED) || name.equals(TestElement.GUI_CLASS) || name.equals(TestElement.NAME)
                    || name.equals(TestElement.TEST_CLASS)) {
                continue; // go on with next property.
            }

            for (ConfigTestElement config : configurations) {
                String configValue = config.getPropertyAsString(name);

                if (configValue != null && configValue.length() > 0) {
                    if (configValue.equals(value)) {
                        sampler.setProperty(name, ""); // $NON-NLS-1$
                    }
                    // Property was found in a config element. Whether or not
                    // it matched the value in the sampler, we're done with
                    // this property -- don't look at lower-priority configs:
                    break;
                }
            }
        }
    }

    private String generateMatchUrl(HTTPSamplerBase sampler) {
        StringBuilder buf = new StringBuilder(sampler.getDomain());
        buf.append(':'); // $NON-NLS-1$
        buf.append(sampler.getPort());
        buf.append(sampler.getPath());
        if (sampler.getQueryString().length() > 0) {
            buf.append('?'); // $NON-NLS-1$
            buf.append(sampler.getQueryString());
        }
        return buf.toString();
    }

    private boolean matchesPatterns(String url, CollectionProperty patterns) {
        for (JMeterProperty jMeterProperty : patterns) {
            String item = jMeterProperty.getStringValue();
            try {
                Pattern pattern = JMeterUtils.getPatternCache().getPattern(
                        item, Perl5Compiler.READ_ONLY_MASK | Perl5Compiler.SINGLELINE_MASK);
                if (JMeterUtils.getMatcher().matches(url, pattern)) {
                    return true;
                }
            } catch (MalformedCachePatternException e) {
                log.warn("Skipped invalid pattern: {}", item, e);
            }
        }
        return false;
    }

    /**
     * Scan all test elements passed in for values matching the value of any of
     * the variables in any of the variable-holding elements in the collection.
     *
     * @param sampler
     *            A TestElement to replace values on
     * @param configs
     *            More TestElements to replace values on
     * @param variables
     *            Collection of Arguments to use to do the replacement, ordered
     *            by ascending priority.
     */
    private void replaceValues(TestElement sampler, TestElement[] configs, Collection<Arguments> variables) {
        // Build the replacer from all the variables in the collection:
        ValueReplacer replacer = new ValueReplacer();
        for (Arguments variable : variables) {
            final Map<String, String> map = variable.getArgumentsAsMap();
            // Drop any empty values (Bug 45199)
            map.values().removeIf(""::equals);
            replacer.addVariables(map);
        }

        try {
            boolean cachedRegexpMatch = regexMatch;
            replacer.reverseReplace(sampler, cachedRegexpMatch);
            for (TestElement config : configs) {
                if (config != null) {
                    replacer.reverseReplace(config, cachedRegexpMatch);
                }
            }
        } catch (InvalidVariableException e) {
            log.warn("Invalid variables included for replacement into recorded sample", e);
        }
    }

    /**
     * This will notify sample listeners directly within the Proxy of the
     * sampling that just occurred -- so that we have a means to record the
     * server's responses as we go.
     *
     * @param event
     *            sampling event to be delivered
     */
    private void notifySampleListeners(SampleEvent event) {
        JMeterTreeModel treeModel = getJmeterTreeModel();
        JMeterTreeNode myNode = treeModel.getNodeOf(this);
        if(myNode != null) {
            Enumeration<?> kids = myNode.children();
            while (kids.hasMoreElements()) {
                JMeterTreeNode subNode = (JMeterTreeNode)kids.nextElement();
                if (subNode.isEnabled()) {
                    TestElement testElement = subNode.getTestElement();
                    if (testElement instanceof SampleListener) {
                        ((SampleListener) testElement).sampleOccurred(event);
                    }
                }
            }
        }
    }

    /**
     * This will notify test listeners directly within the Proxy that the 'test'
     * (here meaning the proxy recording) has started.
     */
    private void notifyTestListenersOfStart() {
        JMeterTreeModel treeModel = getJmeterTreeModel();
        JMeterTreeNode myNode = treeModel.getNodeOf(this);
        if(myNode != null) {
            Enumeration<?> kids = myNode.children();
            while (kids.hasMoreElements()) {
                JMeterTreeNode subNode = (JMeterTreeNode)kids.nextElement();
                if (subNode.isEnabled()) {
                    TestElement testElement = subNode.getTestElement();
                    if (testElement instanceof TestStateListener) {
                        TestBeanHelper.prepare(testElement);
                        ((TestStateListener) testElement).testStarted();
                    }
                }
            }
        }
    }

    /**
     * This will notify test listeners directly within the Proxy that the 'test'
     * (here meaning the proxy recording) has ended.
     */
    private void notifyTestListenersOfEnd() {
        JMeterTreeModel treeModel = getJmeterTreeModel();
        JMeterTreeNode myNode = treeModel.getNodeOf(this);
        if(myNode != null) {
            Enumeration<?> kids = myNode.children();
            while (kids.hasMoreElements()) {
                JMeterTreeNode subNode = (JMeterTreeNode)kids.nextElement();
                if (subNode.isEnabled()) {
                    TestElement testElement = subNode.getTestElement();
                    if (testElement instanceof TestStateListener) { // TL - TE
                        ((TestStateListener) testElement).testEnded();
                    }
                }
            }
        }
    }

    @Override
    public boolean canRemove() {
        return null == server;
    }

    private void initKeyStore() throws IOException, GeneralSecurityException {
        switch(KEYSTORE_MODE) {
        case DYNAMIC_KEYSTORE:
            storePassword = getPassword();
            keyPassword = getPassword();
            initDynamicKeyStore();
            break;
        case JMETER_KEYSTORE:
            storePassword = getPassword();
            keyPassword = getPassword();
            initJMeterKeyStore();
            break;
        case USER_KEYSTORE:
            storePassword = JMeterUtils.getPropDefault("proxy.cert.keystorepass", DEFAULT_PASSWORD); // $NON-NLS-1$
            keyPassword = JMeterUtils.getPropDefault("proxy.cert.keypassword", DEFAULT_PASSWORD); // $NON-NLS-1$
            log.info("HTTP(S) Test Script Recorder will use the keystore '{}' with the alias: '{}'", CERT_PATH_ABS, CERT_ALIAS);
            initUserKeyStore();
            break;
        case NONE:
            throw new IOException("Cannot find keytool application and no keystore was provided");
        default:
            throw new IllegalStateException("Impossible case: " + KEYSTORE_MODE);
        }
    }

    /**
     * Initialise the user-provided keystore
     */
    private void initUserKeyStore() {
        try {
            keyStore = getKeyStore(storePassword.toCharArray());
            X509Certificate  caCert = (X509Certificate) keyStore.getCertificate(CERT_ALIAS);
            if (caCert == null) {
                log.error("Could not find key with alias {}", CERT_ALIAS);
                keyStore = null;
            } else {
                caCert.checkValidity(new Date(System.currentTimeMillis()+DateUtils.MILLIS_PER_DAY));
            }
        } catch (Exception e) {
            keyStore = null;
            log.error(
                    "Could not open keystore or certificate is not valid {} {}",
                    CERT_PATH_ABS, e.getMessage(), e);
        }
    }

    /**
     * Initialise the dynamic domain keystore
     */
    private void initDynamicKeyStore() throws IOException, GeneralSecurityException {
        if (storePassword  != null) { // Assume we have already created the store
            try {
                keyStore = getKeyStore(storePassword.toCharArray());
                for(String alias : KeyToolUtils.getCAaliases()) {
                    X509Certificate  caCert = (X509Certificate) keyStore.getCertificate(alias);
                    if (caCert == null) {
                        keyStore = null; // no CA key - probably the wrong store type.
                        break; // cannot continue
                    } else {
                        caCert.checkValidity(new Date(System.currentTimeMillis()+DateUtils.MILLIS_PER_DAY));
                        log.info("Valid alias found for {}", alias);
                    }
                }
            } catch (IOException e) { // store is faulty, we need to recreate it
                keyStore = null; // if cert is not valid, flag up to recreate it
                if (e.getCause() instanceof UnrecoverableKeyException) {
                    log.warn(
                            "Could not read key store {}; cause: {}, a new one will be created, ensure you install it in browser",
                            e.getMessage(), e.getCause().getMessage(), e);
                } else {
                    log.warn(
                            "Could not open/read key store {}, a new one will be created, ensure you install it in browser",
                            e.getMessage(), e); // message includes the file name
                }
            } catch (CertificateExpiredException e) {
                keyStore = null; // if cert is not valid, flag up to recreate it
                log.warn(
                        "Existing ROOT Certificate has expired, a new one will be created, ensure you install it in browser, message: {}",
                        e.getMessage(), e);
            } catch (CertificateNotYetValidException e) {
                keyStore = null; // if cert is not valid, flag up to recreate it
                log.warn(
                        "Existing ROOT Certificate is not yet valid, a new one will be created, ensure you install it in browser, message: {}",
                        e.getMessage(), e);
            } catch (GeneralSecurityException e) {
                keyStore = null; // if cert is not valid, flag up to recreate it
                log.warn(
                        "Problem reading key store, a new one will be created, ensure you install it in browser, message: {}",
                        e.getMessage(), e);
            }
        }
        if (keyStore == null) { // no existing file or not valid
            storePassword = RandomStringUtils.randomAlphanumeric(20); // Alphanum to avoid issues with command-line quoting
            keyPassword = storePassword; // we use same password for both
            setPassword(storePassword);
            log.info(
                    "Creating HTTP(S) Test Script Recorder Root CA in {}, ensure you install certificate in your Browser for recording",
                    CERT_PATH_ABS);
            KeyToolUtils.generateProxyCA(CERT_PATH, storePassword, CERT_VALIDITY);
            log.info("Created keystore in {}", CERT_PATH_ABS);
            keyStore = getKeyStore(storePassword.toCharArray()); // This should now work
        }
        final String sslDomains = getSslDomains().trim();
        if (sslDomains.length() > 0) {
            final String[] domains = sslDomains.split(",");
            // The subject may be either a host or a domain
            for(String subject : domains) {
                if (isValid(subject)) {
                    if (!keyStore.containsAlias(subject)) {
                        log.info("Creating entry {} in {}", subject, CERT_PATH_ABS);
                        KeyToolUtils.generateHostCert(CERT_PATH, storePassword, subject, CERT_VALIDITY);
                        keyStore = getKeyStore(storePassword.toCharArray()); // reload to pick up new aliases
                        // reloading is very quick compared with creating an entry currently
                    }
                } else {
                    log.warn("Attempt to create an invalid domain certificate: {}", subject);
                }
            }
        }
    }

    private boolean isValid(String subject) {
        String[] parts = subject.split("\\.");
        return !parts[0].endsWith("*") // not a wildcard
                || parts.length >= 3
                && AbstractVerifier.acceptableCountryWildcard(subject);
    }

    // This should only be called for a specific host
    KeyStore updateKeyStore(String port, String host) throws IOException, GeneralSecurityException {
        synchronized(CERT_PATH) { // ensure Proxy threads cannot interfere with each other
            if (!keyStore.containsAlias(host)) {
                log.info("{} Creating entry {} in {}", port, host, CERT_PATH_ABS);
                KeyToolUtils.generateHostCert(CERT_PATH, storePassword, host, CERT_VALIDITY);
            }
            keyStore = getKeyStore(storePassword.toCharArray()); // reload after adding alias
        }
        return keyStore;
    }

    /**
     * Initialise the single key JMeter keystore (original behaviour)
     */
    private void initJMeterKeyStore() throws IOException, GeneralSecurityException {
        if (storePassword  != null) { // Assume we have already created the store
            try {
                keyStore = getKeyStore(storePassword.toCharArray());
                X509Certificate  caCert = (X509Certificate) keyStore.getCertificate(JMETER_SERVER_ALIAS);
                caCert.checkValidity(new Date(System.currentTimeMillis()+DateUtils.MILLIS_PER_DAY));
            } catch (Exception e) { // store is faulty, we need to recreate it
                keyStore = null; // if cert is not valid, flag up to recreate it
                log.warn(
                        "Could not open expected file or certificate is not valid {} {}",
                        CERT_PATH_ABS, e.getMessage(), e);
            }
        }
        if (keyStore == null) { // no existing file or not valid
            storePassword = RandomStringUtils.randomAlphanumeric(20); // Alphanum to avoid issues with command-line quoting
            keyPassword = storePassword; // we use same password for both
            setPassword(storePassword);
            log.info("Generating standard keypair in {}", CERT_PATH_ABS);
            if(!CERT_PATH.delete()){ // safer to start afresh
                log.warn(
                        "Could not delete {}, this could create issues, stop jmeter, ensure file is deleted and restart again",
                        CERT_PATH.getAbsolutePath());
            }
            KeyToolUtils.genkeypair(CERT_PATH, JMETER_SERVER_ALIAS, storePassword, CERT_VALIDITY, null, null);
            keyStore = getKeyStore(storePassword.toCharArray()); // This should now work
        }
    }

    private KeyStore getKeyStore(char[] password) throws GeneralSecurityException, IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(CERT_PATH))) {
            log.debug("Opened Keystore file: {}", CERT_PATH_ABS);
            KeyStore ks = KeyStore.getInstance(KEYSTORE_TYPE);
            ks.load(in, password);
            log.debug("Loaded Keystore file: {}", CERT_PATH_ABS);
            return ks;
        }
    }

    private String getPassword() {
        return PREFERENCES.get(USER_PASSWORD_KEY, null);
    }

    private void setPassword(String password) {
        PREFERENCES.put(USER_PASSWORD_KEY, password);
    }

    // the keystore for use by the Proxy
    KeyStore getKeyStore() {
        return keyStore;
    }

    String getKeyPassword() {
        return keyPassword;
    }

    public static boolean isDynamicMode() {
        return KEYSTORE_MODE == KeystoreMode.DYNAMIC_KEYSTORE;
    }

}
