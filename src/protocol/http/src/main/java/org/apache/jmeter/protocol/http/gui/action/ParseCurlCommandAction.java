/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.gui.action;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.TreePath;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.KeystoreConfig;
import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.ReplaceableController;
import org.apache.jmeter.control.gui.TestPlanGui;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.AbstractAction;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.Command;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.control.StaticHost;
import org.apache.jmeter.protocol.http.control.gui.HttpTestSampleGui;
import org.apache.jmeter.protocol.http.curl.ArgumentHolder;
import org.apache.jmeter.protocol.http.curl.BasicCurlParser;
import org.apache.jmeter.protocol.http.curl.BasicCurlParser.Request;
import org.apache.jmeter.protocol.http.curl.FileArgumentHolder;
import org.apache.jmeter.protocol.http.gui.AuthPanel;
import org.apache.jmeter.protocol.http.gui.CookiePanel;
import org.apache.jmeter.protocol.http.gui.DNSCachePanel;
import org.apache.jmeter.protocol.http.gui.HeaderPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.services.FileServer;
import org.apache.jmeter.testbeans.gui.TestBeanGUI;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.threads.AbstractThreadGroupSchema;
import org.apache.jmeter.threads.ThreadGroup;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.ViewResultsFullVisualizer;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.JMeterUIDefaults;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.google.auto.service.AutoService;

/**
 * Opens a popup where user can enter a cURL command line and create a test plan
 * from it
 *
 * @since 5.1
 */
@AutoService({
        Command.class,
        MenuCreator.class
})
public class ParseCurlCommandAction extends AbstractAction implements MenuCreator, ActionListener { // NOSONAR
    private static final Logger LOGGER = LoggerFactory.getLogger(ParseCurlCommandAction.class);
    private static final String ACCEPT_ENCODING = "Accept-Encoding";
    private static final Set<String> commands = new HashSet<>();
    public static final String IMPORT_CURL = "import_curl";
    private static final String CREATE_REQUEST = "CREATE_REQUEST";
    private static final String CERT = "cert";
    /** A panel allowing results to be saved. */
    private FilePanel filePanel = null;
    static {
        commands.add(IMPORT_CURL);
    }
    private JSyntaxTextArea cURLCommandTA;
    private JLabel statusText;
    private JCheckBox uploadCookiesCheckBox;
    private final Tika tika = createTika();

    private Tika createTika() {
        try {
            return new Tika(new TikaConfig(this.getClass().getClassLoader()
                    .getResourceAsStream("org/apache/jmeter/protocol/http/gui/action/tika-config.xml")));
        } catch (TikaException | IOException | SAXException e) {
            return new Tika();
        }
    }

    public ParseCurlCommandAction() {
        super();
    }

    @Override
    public void doAction(ActionEvent e) {
        showInputDialog(e);
    }

    /**
     * Show popup where user can import cURL command
     *
     * @param event {@link ActionEvent}
     */
    private void showInputDialog(ActionEvent event) {
        EscapeDialog messageDialog = new EscapeDialog(
                getParentFrame(event),
                JMeterUtils.getResString("curl_import"), //$NON-NLS-1$
                false);
        Container contentPane = messageDialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        statusText = new JLabel("",JLabel.CENTER);
        statusText.setForeground(UIManager.getColor(JMeterUIDefaults.LABEL_ERROR_FOREGROUND));
        contentPane.add(statusText, BorderLayout.NORTH);
        cURLCommandTA = JSyntaxTextArea.getInstance(20, 80, false);
        cURLCommandTA.setCaretPosition(0);
        contentPane.add(JTextScrollPane.getInstance(cURLCommandTA), BorderLayout.CENTER);
        JPanel optionPanel = new JPanel(new BorderLayout(3, 1));
        filePanel = new FilePanel(JMeterUtils.getResString("curl_import_from_file")); // $NON-NLS-1$
        optionPanel.add(filePanel,BorderLayout.CENTER);
        uploadCookiesCheckBox =  new JCheckBox(JMeterUtils.getResString("curl_add_cookie_header_to_cookiemanager"), false);
        optionPanel.add(uploadCookiesCheckBox,BorderLayout.NORTH);
        JButton button = new JButton(JMeterUtils.getResString("curl_create_request"));
        button.setActionCommand(CREATE_REQUEST);
        button.addActionListener(this);
        button.setPreferredSize(new Dimension(50, 50));
        optionPanel.add(button,BorderLayout.SOUTH);
        contentPane.add(optionPanel, BorderLayout.SOUTH);
        messageDialog.pack();
        ComponentUtil.centerComponentInComponent(GuiPackage.getInstance().getMainFrame(), messageDialog);
        SwingUtilities.invokeLater(() -> messageDialog.setVisible(true));
    }

    /**
     * Finds the first enabled node of a given type in the tree.
     *
     * @param type class of the node to be found
     * @return the first node of the given type in the test component tree, or
     *         <code>null</code> if none was found.
     */
    private static JMeterTreeNode findFirstNodeOfType(Class<?> type) {
        JMeterTreeModel treeModel = GuiPackage.getInstance().getTreeModel();
        return treeModel.getNodesOfType(type).stream().filter(JMeterTreeNode::isEnabled).findFirst().orElse(null);
    }

    private static DNSCacheManager findNodeOfTypeDnsCacheManagerByType(boolean isCustom) {
        JMeterTreeModel treeModel = GuiPackage.getInstance().getTreeModel();
        List<JMeterTreeNode> res = treeModel.getNodesOfType(DNSCacheManager.class);
        for (JMeterTreeNode jm : res) {
            DNSCacheManager dnsCacheManager = (DNSCacheManager) jm.getTestElement();
            if (dnsCacheManager.isCustomResolver() == isCustom) {
                return dnsCacheManager;
            }
        }
        return null;
    }

    private void createTestPlan(ActionEvent e, Request request, String statusText)
            throws MalformedURLException, IllegalUserActionException {
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.CLOSE));
        GuiPackage guiPackage = GuiPackage.getInstance();
        guiPackage.clearTestPlan();
        FileServer.getFileServer().setScriptName(null);
        ThreadGroup threadGroup = new ThreadGroup();
        threadGroup.setProperty(TestElement.GUI_CLASS, ThreadGroupGui.class.getName());
        threadGroup.setProperty(TestElement.NAME, "Thread Group");
        threadGroup.set(AbstractThreadGroupSchema.INSTANCE.getNumThreads(), "${__P(threads,10)}");
        threadGroup.setProperty(ThreadGroup.RAMP_TIME,"${__P(rampup,30)}");
        threadGroup.setScheduler(true);
        threadGroup.setProperty(ThreadGroup.DURATION,"${__P(duration,3600)}");
        threadGroup.setDelay(5);
        LoopController loopCtrl = new LoopController();
        loopCtrl.setLoops("${__P(iterations,-1)}");
        loopCtrl.setContinueForever(false);
        threadGroup.setSamplerController(loopCtrl);
        TestPlan testPlan = new TestPlan();
        testPlan.setProperty(TestElement.NAME, "Test Plan");
        testPlan.setProperty(TestElement.GUI_CLASS, TestPlanGui.class.getName());
        testPlan.setComment("You can run me using: jmeter -Jthreads=<Number of threads> -Jrampup=<rampup in seconds> -Jduration=<duration in seconds> "
                + "-Jiterations=<Number of iterations, -1 means infinite> -e -o <report output folder>");
        HashTree tree = new HashTree();
        HashTree testPlanHT = tree.add(testPlan);
        HashTree threadGroupHT = testPlanHT.add(threadGroup);
        createHttpRequest(request, threadGroupHT,statusText);
        if (!request.getAuthorization().getUser().isEmpty()) {
            AuthManager authManager = new AuthManager();
            createAuthManager(request, authManager);
            threadGroupHT.add(authManager);
        }
        if (!request.getDnsServers().isEmpty()) {
            DNSCacheManager dnsCacheManager = new DNSCacheManager();
            createDnsServer(request, dnsCacheManager);
            threadGroupHT.add(dnsCacheManager);
        }
        if (request.getDnsResolver()!=null) {
            DNSCacheManager dnsCacheManager = new DNSCacheManager();
            createDnsResolver(request, dnsCacheManager);
            threadGroupHT.add(dnsCacheManager);
        }
        CookieManager cookieManager = new CookieManager();
        createCookieManager(cookieManager, request);
        threadGroupHT.add(cookieManager);
        ResultCollector resultCollector = new ResultCollector();
        resultCollector.setProperty(TestElement.NAME, "View Results Tree");
        resultCollector.setProperty(TestElement.GUI_CLASS, ViewResultsFullVisualizer.class.getName());
        tree.add(tree.getArray()[0], resultCollector);
        final HashTree newTree = guiPackage.addSubTree(tree);
        guiPackage.updateCurrentGui();
        guiPackage.getMainFrame().getTree()
                .setSelectionPath(new TreePath(((JMeterTreeNode) newTree.getArray()[0]).getPath()));
        final HashTree subTree = guiPackage.getCurrentSubTree();
        // Send different event whether we are merging a test plan into another test
        // plan,
        // or loading a testplan from scratch
        ActionEvent actionEvent = new ActionEvent(subTree.get(subTree.getArray()[subTree.size() - 1]), e.getID(),
                ActionNames.SUB_TREE_LOADED);
        ActionRouter.getInstance().actionPerformed(actionEvent);
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.EXPAND_ALL));
    }

    private HTTPSamplerProxy createHttpRequest(Request request, HashTree parentHT, String commentText) throws MalformedURLException {
        HTTPSamplerProxy httpSampler = createSampler(request,commentText);
        HashTree samplerHT = parentHT.add(httpSampler);
        samplerHT.add(httpSampler.getHeaderManager());
        if (CERT.equals(request.getCaCert())) {
            samplerHT.add(httpSampler.getKeystoreConfig());
        }
        return httpSampler;
    }

    /**
     * @param request    {@link Request}
     * @param commentText
     * @return {@link HTTPSamplerProxy}
     * @throws MalformedURLException
     */
    private HTTPSamplerProxy createSampler(Request request, String commentText) throws MalformedURLException {
        HTTPSamplerProxy httpSampler = (HTTPSamplerProxy) HTTPSamplerFactory
                .newInstance(HTTPSamplerFactory.DEFAULT_CLASSNAME);
        httpSampler.setProperty(TestElement.GUI_CLASS, HttpTestSampleGui.class.getName());
        httpSampler.setProperty(TestElement.NAME, "HTTP Request");
        if (!commentText.isEmpty()) {
            httpSampler.setProperty(TestElement.COMMENTS,commentText); // NOSONAR
        } else {
            httpSampler.setProperty(TestElement.COMMENTS, getDefaultComment());
        } // NOSONAR
        URL url = new URL(request.getUrl());
        httpSampler.setProtocol(url.getProtocol());
        if (url.getPort() != -1) {
            httpSampler.setPort(url.getPort());
        }
        String path = url.getPath();
        if (StringUtils.isNotEmpty(url.getQuery())) {
            path += "?" + url.getQuery();
        }
        httpSampler.setPath(path);
        httpSampler.setDomain(url.getHost());
        httpSampler.setUseKeepAlive(request.isKeepAlive());
        httpSampler.setFollowRedirects(true);
        httpSampler.setMethod(request.getMethod());
        HeaderManager headerManager = createHeaderManager(request);
        httpSampler.addTestElement(headerManager);
        configureTimeout(request, httpSampler);
        createProxyServer(request, httpSampler);
        if (request.getInterfaceName() != null) {
            httpSampler.setIpSourceType(1);
            httpSampler.setIpSource(request.getInterfaceName());
        }
        if (!"GET".equals(request.getMethod()) && request.getPostData() != null) {
            Arguments arguments = new Arguments();
            httpSampler.setArguments(arguments);
            httpSampler.addNonEncodedArgument("", request.getPostData(), "");
        }
        if (!request.getFormData().isEmpty() || !request.getFormStringData().isEmpty()) {
            setFormData(request, httpSampler);
            httpSampler.setDoMultipart(true);
        }
        if (CERT.equals(request.getCaCert())) {
            KeystoreConfig keystoreConfig = createKeystoreConfiguration();
            httpSampler.addTestElement(keystoreConfig);
        }
        return httpSampler;
    }

    private static void configureTimeout(Request request, HTTPSamplerProxy httpSampler) {
        double connectTimeout = request.getConnectTimeout();
        double maxTime = request.getMaxTime();
        if (connectTimeout >= 0) {
            httpSampler.setConnectTimeout(String.valueOf((int) request.getConnectTimeout()));
            if (maxTime >= 0) {
                maxTime = maxTime - connectTimeout;
            }
        }
        if (maxTime >= 0) {
            httpSampler.setResponseTimeout(String.valueOf((int) maxTime));
        }
    }

    /**
     *
     * @param request {@link Request}
     * @return {@link HeaderManager} element
     */
    private static HeaderManager createHeaderManager(Request request) {
        HeaderManager headerManager = new HeaderManager();
        headerManager.setProperty(TestElement.GUI_CLASS, HeaderPanel.class.getName());
        headerManager.setProperty(TestElement.NAME, "HTTP HeaderManager");
        headerManager.setProperty(TestElement.COMMENTS, getDefaultComment());
        boolean hasAcceptEncoding = false;
        for (Pair<String, String> header : request.getHeaders()) {
            String key = header.getKey();
            hasAcceptEncoding = hasAcceptEncoding || key.equalsIgnoreCase(ACCEPT_ENCODING);
            headerManager.getHeaders().addItem(new Header(key, header.getValue()));
        }
        if (!hasAcceptEncoding && request.isCompressed()) {
            headerManager.getHeaders().addItem(new Header(ACCEPT_ENCODING, "gzip, deflate"));
        }
        return headerManager;
    }

    /**
     * Configures a given Cookie Manager.
     *
     * @param cookieManager the manager to configure
     * @param request to copy information about cookies from
     */
    private void createCookieManager(CookieManager cookieManager, Request request) {
        cookieManager.setProperty(TestElement.GUI_CLASS, CookiePanel.class.getName());
        cookieManager.setProperty(TestElement.NAME, "HTTP CookieManager");
        cookieManager.setProperty(TestElement.COMMENTS, getDefaultComment());
        if (!request.getCookies(request.getUrl()).isEmpty()) {
            for (Cookie c : request.getCookies(request.getUrl())) {
                cookieManager.getCookies().addItem(c);
            }
        }
        if (!request.getCookieInHeaders(request.getUrl()).isEmpty() && uploadCookiesCheckBox.isSelected()) {
            for (Cookie c : request.getCookieInHeaders(request.getUrl())) {
                cookieManager.getCookies().addItem(c);
            }
        }
        if (!request.getFilepathCookie().isEmpty()) {
            String pathfileCookie=request.getFilepathCookie();
            File file = new File(pathfileCookie);
            if (file.isFile() && file.exists()) {
                try {
                    cookieManager.addFile(pathfileCookie);
                } catch (IOException e) {
                    LOGGER.error("Failed to read from File {}", pathfileCookie, e);
                    throw new IllegalArgumentException("Failed to read from File " + pathfileCookie);
                }
            } else {
                LOGGER.error("File {} doesn't exist", pathfileCookie);
                throw new IllegalArgumentException("File " + pathfileCookie + " doesn't exist");
            }
        }
    }

    private static KeystoreConfig createKeystoreConfiguration() {
        KeystoreConfig keystoreConfig = new KeystoreConfig();
        keystoreConfig.setProperty(TestElement.GUI_CLASS, TestBeanGUI.class.getName());
        keystoreConfig.setProperty(TestElement.NAME, "Keystore Configuration");
        keystoreConfig.setProperty(TestElement.COMMENTS, getDefaultComment());
        return keystoreConfig;
    }

    /**
     * Create Authorization manager
     *
     * @param request {@link Request}
     */
    private static void createAuthManager(Request request, AuthManager authManager) {
        Authorization auth = request.getAuthorization();
        authManager.setProperty(TestElement.GUI_CLASS, AuthPanel.class.getName());
        authManager.setProperty(TestElement.NAME, "HTTP AuthorizationManager");
        authManager.setProperty(TestElement.COMMENTS, getDefaultComment());
        authManager.getAuthObjects().addItem(auth);
    }

    /**
     * Whether to update Authorization Manager in http request
     *
     * @param request     {@link Request}
     * @param authManager {@link AuthManager} element
     * @return whether to update Authorization Manager in http request
     */
    private static boolean canAddAuthManagerInHttpRequest(Request request, AuthManager authManager) {
        Authorization auth = request.getAuthorization();
        for (int i = 0; i < authManager.getAuthObjects().size(); i++) {
            if (!authManager.getAuthObjectAt(i).getUser().equals(auth.getUser())
                    || !authManager.getAuthObjectAt(i).getPass().equals(auth.getPass())
                    || authManager.getAuthObjectAt(i).getMechanism() != auth.getMechanism()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether to update Authorization Manager in Thread Group
     *
     * @param request     {@link Request}
     * @param authManager {@link AuthManager} element
     * @return whether to update Authorization Manager in Thread Group
     */
    private static boolean canUpdateAuthManagerInThreadGroup(Request request, AuthManager authManager) {
        Authorization auth = request.getAuthorization();
        for (int i = 0; i < authManager.getAuthObjects().size(); i++) {
            if (auth.getURL().equals(authManager.getAuthObjectAt(i).getURL())) {
                return false;
            }
        }
        return true;
    }

    private static void createDnsServer(Request request, DNSCacheManager dnsCacheManager) {
        Set<String> dnsServers = request.getDnsServers();
        dnsCacheManager.setProperty(TestElement.GUI_CLASS, DNSCachePanel.class.getName());
        dnsCacheManager.setProperty(TestElement.NAME, "DNS Cache Manager");
        dnsCacheManager.setProperty(TestElement.COMMENTS, getDefaultComment());
        dnsCacheManager.getServers().clear();
        for (String dnsServer : dnsServers) {
            dnsCacheManager.addServer(dnsServer);
        }
    }

    private static boolean canAddDnsServerInHttpRequest(Request request, DNSCacheManager dnsCacheManager) {
        Set<String> currentDnsServers =new HashSet<>();
        Set<String> newDnsServers = request.getDnsServers();
        for (int i = 0; i < dnsCacheManager.getServers().size(); i++) {
            currentDnsServers.add(dnsCacheManager.getServers().get(i).getStringValue());
        }
        return !(newDnsServers.size() == currentDnsServers.size() && newDnsServers.containsAll(currentDnsServers));
    }

    private static void createDnsResolver(Request request, DNSCacheManager dnsCacheManager) {
        dnsCacheManager.setProperty(TestElement.GUI_CLASS, DNSCachePanel.class.getName());
        dnsCacheManager.setProperty(TestElement.NAME, "DNS Cache Manager");
        dnsCacheManager.setCustomResolver(true);
        dnsCacheManager.getHosts().clear();
        String[]resolveParameters=request.getDnsResolver().split(":");
        String port=resolveParameters[1];
        if (!"443".equals(port)
                && !"80".equals(port)
                && !"*".equals(port)) {
            dnsCacheManager.setProperty(TestElement.COMMENTS,
                    "Custom DNS resolver doesn't support port "+port);
        }
        else {
            dnsCacheManager.setProperty(TestElement.COMMENTS, getDefaultComment());
        }
        dnsCacheManager.addHost(resolveParameters[0], resolveParameters[2]);
    }

    @SuppressWarnings("JavaTimeDefaultTimeZone")
    private static String getDefaultComment() {
        return "Created from cURL on " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    }

    private static boolean canAddDnsResolverInHttpRequest(Request request, DNSCacheManager dnsCacheManager) {
        if (dnsCacheManager.getHosts().size() != 1) {
            return true;
        } else {
            String[] resolveParameters = request.getDnsResolver().split(":");
            String host = resolveParameters[0];
            String address = resolveParameters[2];
            StaticHost statichost = (StaticHost) dnsCacheManager.getHosts().get(0).getObjectValue();
            if (statichost.getAddress().equals(address) && statichost.getName().equals(host)) {
                return false;
            }
        }
        return true;
    }

    private void setFormData(Request request, HTTPSamplerProxy httpSampler) {
        if (request.getPostData() != null) {
            throw new IllegalArgumentException("--form and --data can't appear in the same command");
        }
        List<HTTPFileArg> httpFileArgs = new ArrayList<>();
        for (Pair<String, String> entry : request.getFormStringData()) {
            String formName = entry.getKey();
            String formValue = entry.getValue();
            httpSampler.addNonEncodedArgument(formName, formValue, "");
        }
        for (Pair<String, ArgumentHolder> entry : request.getFormData()) {
            String formName = entry.getKey();
            ArgumentHolder formValueObject = entry.getValue();
            String formValue = formValueObject.getName();
            if (formValueObject instanceof FileArgumentHolder) {
                String contentType;
                if (formValueObject.hasContenType()) {
                    contentType = formValueObject.getContentType();
                } else {
                    try {
                        final File contentFile = new File(formValue);
                        if (contentFile.canRead()) {
                            contentType = tika.detect(contentFile);
                        } else {
                            LOGGER.info("Can not read file {}, so guessing contentType by extension.", formValue);
                            contentType = tika.detect(formValue);
                        }
                    } catch (IOException e) {
                        LOGGER.info(
                                "Could not detect contentType for file {} by content, so falling back to detection by filename",
                                formValue);
                        contentType = tika.detect(formValue);
                    }
                }
                httpFileArgs.add(new HTTPFileArg(formValue, formName, contentType));
            } else {
                if (formValueObject.hasContenType()) {
                    httpSampler.addNonEncodedArgument(formName, formValue, "", formValueObject.getContentType());
                } else {
                    httpSampler.addNonEncodedArgument(formName, formValue, "");
                }
            }
        }
        if (!httpFileArgs.isEmpty()) {
            httpSampler.setHTTPFiles(httpFileArgs.toArray(new HTTPFileArg[httpFileArgs.size()]));
        }
    }

    private static void createProxyServer(Request request, HTTPSamplerProxy httpSampler) {
        Map<String, String> proxyServer = request.getProxyServer();
        for (Map.Entry<String, String> proxyPara : proxyServer.entrySet()) {
            String key = proxyPara.getKey();
            switch (key) {
            case "servername":
                httpSampler.setProxyHost(proxyPara.getValue());
                break;
            case "port":
                httpSampler.setProxyPortInt(proxyPara.getValue());
                break;
            case "scheme":
                httpSampler.setProxyScheme(proxyPara.getValue());
                break;
            case "username":
                httpSampler.setProxyUser(proxyPara.getValue());
                break;
            case "password":
                httpSampler.setProxyPass(proxyPara.getValue());
                break;
            default:
                break;
            }
        }
    }

    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if (location == MENU_LOCATION.TOOLS) {
            // Use the action name as resource key because the action name is used by JMeterMenuBar too when changing languages.
            JMenuItem menuItemIC = new JMenuItem(JMeterUtils.getResString(IMPORT_CURL), KeyEvent.VK_UNDEFINED);
            menuItemIC.setName(IMPORT_CURL);
            menuItemIC.setActionCommand(IMPORT_CURL);
            menuItemIC.setAccelerator(null);
            menuItemIC.addActionListener(ActionRouter.getInstance());
            return new JMenuItem[] { menuItemIC };
        }
        return new JMenuItem[0];
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        statusText.setText("");
        statusText.setForeground(Color.GREEN);
        boolean isReadFromFile = false;
        if (e.getActionCommand().equals(CREATE_REQUEST)) {
            List<String> commandsList = null;
            try {
                if (!filePanel.getFilename().trim().isEmpty() && cURLCommandTA.getText().trim().isEmpty()) {
                    commandsList = readFromFile(filePanel.getFilename().trim());
                    isReadFromFile = true;
                } else if (filePanel.getFilename().trim().isEmpty() && !cURLCommandTA.getText().trim().isEmpty()) {
                    commandsList = readFromTextPanel(cURLCommandTA.getText().trim());
                } else {
                    throw new IllegalArgumentException(
                            "Error creating tast plan ,Please select one between reading file and directly fill in the panel");
                }
                List<Request> requests = parseCommands(isReadFromFile, commandsList);
                for (int i=0;i<requests.size();i++) {
                    BasicCurlParser.Request request = requests.get(i);
                    try {
                        String commentText = createCommentText(request);
                        GuiPackage guiPackage = GuiPackage.getInstance();
                        guiPackage.updateCurrentNode();
                        JMeterTreeNode treeNode = findFirstNodeOfType(AbstractThreadGroup.class);
                        if (treeNode == null) {
                            LOGGER.info("No AbstractThreadGroup found, potentially empty plan, creating a new plan");
                            createTestPlan(e, request, commentText);
                        } else {
                            JMeterTreeNode currentNode = guiPackage.getCurrentNode();
                            Object userObject = currentNode.getUserObject();
                            if (userObject instanceof Controller && !(userObject instanceof ReplaceableController)) {
                                LOGGER.info("Newly created element will be placed under current selected node {}",
                                        currentNode.getName());
                                addToTestPlan(currentNode, request, commentText);
                            } else {
                                LOGGER.info(
                                        "Newly created element will be placed under first AbstractThreadGroup node {}",
                                        treeNode.getName());
                                addToTestPlan(treeNode, request, commentText);
                            }
                        }
                        statusText.setText(JMeterUtils.getResString("curl_create_success"));
                    }
                    catch (Exception ex) {
                        LOGGER.error("Error creating test plan from cURL command:{}, error:{}", commandsList.get(i),
                                ex.getMessage(), ex);
                        statusText.setText(
                                MessageFormat.format(JMeterUtils.getResString("curl_create_failure"), ex.getMessage()));
                        statusText.setForeground(UIManager.getColor(JMeterUIDefaults.LABEL_ERROR_FOREGROUND));
                        break;
                    }
                }
            } catch (Exception ex) {
                LOGGER.error("Error creating test plan from cURL command list:{}", commandsList, ex);
                statusText.setText(
                        MessageFormat.format(JMeterUtils.getResString("curl_create_failure"), ex.getMessage()));
                statusText.setForeground(UIManager.getColor(JMeterUIDefaults.LABEL_ERROR_FOREGROUND));
            }
        }
    }


    public List<Request>  parseCommands(boolean isReadFromFile, List<String> commandsList) {
        List<Request> requests = new ArrayList<>();
        BasicCurlParser basicCurlParser = new BasicCurlParser();
        for (int i = 0; i < commandsList.size(); i++) {
            try {
                BasicCurlParser.Request q = basicCurlParser.parse(commandsList.get(i));
                requests.add(q);
                LOGGER.info("Parsed CURL command {} into {}", commandsList.get(i), q);
            } catch (IllegalArgumentException ie) {
                if (isReadFromFile) {
                    int line = i + 1;
                    LOGGER.error("Error creating test plan from line {} of file, command:{}, error:{}", line,
                            commandsList.get(i), ie.getMessage(), ie);
                    throw new IllegalArgumentException(
                            "Error creating tast plan from file in line " + line + ", see log file");
                } else {
                    LOGGER.error("Error creating test plan from cURL command:{}, error:{}", commandsList.get(i),
                            ie.getMessage(), ie);
                    throw ie;
                }
            }
        }
        return requests;
    }

    private void addToTestPlan(final JMeterTreeNode currentNode, Request request,String statusText) throws MalformedURLException {
        final HTTPSamplerProxy sampler = createSampler(request,statusText);
        JMeterTreeModel treeModel = GuiPackage.getInstance().getTreeModel();
        JMeterUtils.runSafe(true, () -> {
            try {
                boolean canAddAuthManagerInHttpRequest = false;
                boolean canAddDnsServer=false;
                boolean canAddDnsResolver=false;
                if (!request.getAuthorization().getUser().isEmpty()) {
                    JMeterTreeNode jMeterTreeNodeAuth = findFirstNodeOfType(AuthManager.class);
                    if (jMeterTreeNodeAuth == null) {
                        AuthManager authManager = new AuthManager();
                        createAuthManager(request, authManager);
                        treeModel.addComponent(authManager, currentNode);
                    } else {
                        AuthManager authManager = (AuthManager) jMeterTreeNodeAuth.getTestElement();
                        if (canUpdateAuthManagerInThreadGroup(request, authManager)) {
                            createAuthManager(request, authManager);
                        } else {
                            canAddAuthManagerInHttpRequest = canAddAuthManagerInHttpRequest(request, authManager);
                        }
                    }
                }
                if (!request.getDnsServers().isEmpty()) {
                    DNSCacheManager dnsCacheManager = findNodeOfTypeDnsCacheManagerByType(false);
                    if ( dnsCacheManager == null) {
                        dnsCacheManager=new DNSCacheManager();
                        createDnsServer(request, dnsCacheManager);
                        treeModel.addComponent(dnsCacheManager, currentNode);
                    } else {
                        canAddDnsServer=canAddDnsServerInHttpRequest(request, dnsCacheManager);
                    }
                }
                if (request.getDnsResolver()!=null) {
                    DNSCacheManager dnsCacheManager = findNodeOfTypeDnsCacheManagerByType(true);
                    if (dnsCacheManager == null) {
                        dnsCacheManager=new DNSCacheManager();
                        createDnsResolver(request, dnsCacheManager);
                        treeModel.addComponent(dnsCacheManager, currentNode);
                    } else {
                        canAddDnsResolver=canAddDnsResolverInHttpRequest(request, dnsCacheManager);
                    }
                }
                if (!request.getCookies(request.getUrl()).isEmpty() || !request.getFilepathCookie().isEmpty()
                        || (!request.getCookieInHeaders(request.getUrl()).isEmpty()&&uploadCookiesCheckBox.isSelected())) {
                    JMeterTreeNode jMeterTreeNodeCookie = findFirstNodeOfType(CookieManager.class);
                    if (jMeterTreeNodeCookie == null) {
                        CookieManager cookieManager = new CookieManager();
                        createCookieManager(cookieManager, request);
                        treeModel.addComponent(cookieManager, currentNode);
                    } else {
                        CookieManager cookieManager = (CookieManager) jMeterTreeNodeCookie.getTestElement();
                        createCookieManager(cookieManager, request);
                    }
                }
                HeaderManager headerManager = sampler.getHeaderManager();
                KeystoreConfig keystoreConfig = sampler.getKeystoreConfig();
                final JMeterTreeNode newNode = treeModel.addComponent(sampler, currentNode);
                treeModel.addComponent(headerManager, newNode);
                if (CERT.equals(request.getCaCert())) {
                    treeModel.addComponent(keystoreConfig, newNode);
                }
                if (canAddAuthManagerInHttpRequest) {
                    AuthManager authManager = new AuthManager();
                    createAuthManager(request, authManager);
                    treeModel.addComponent(authManager, newNode);
                }
                if (canAddDnsServer) {
                    DNSCacheManager dnsCacheManager=new DNSCacheManager();
                    createDnsServer(request, dnsCacheManager);
                    treeModel.addComponent(dnsCacheManager, newNode);
                }
                if (canAddDnsResolver) {
                    DNSCacheManager dnsCacheManager=new DNSCacheManager();
                    createDnsResolver(request, dnsCacheManager);
                    treeModel.addComponent(dnsCacheManager, newNode);
                }
            } catch (IllegalUserActionException ex) {
                LOGGER.error("Error placing sampler", ex);
                JMeterUtils.reportErrorToUser(ex.getMessage());
            }
        });
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }

    @Override
    public JMenu[] getTopLevelMenus() {
        return new JMenu[0];
    }

    @Override
    public boolean localeChanged(MenuElement menu) {
        return false;
    }

    @Override
    public void localeChanged() {
        // NOOP
    }

    public List<String> readFromFile(String pathname) throws IOException {
        String encoding = StandardCharsets.UTF_8.name();
        File file = new File(pathname);
        return FileUtils.readLines(file, encoding);
    }

    public List<String> readFromTextPanel(String commands) {
        String[] cs = commands.split("curl");
        List<String> s = new ArrayList<>();
        for (int i = 1; i < cs.length; i++) {
            s.add("curl " + cs[i].trim());
        }
        return s;
    }

    public String createCommentText(Request request) {
        StringBuilder commentText = new StringBuilder();
        if (!request.getOptionsIgnored().isEmpty()) {
            for (String s : request.getOptionsIgnored()) {
                commentText.append("--"+s + " ");
            }
            commentText.append("ignoring; ");
        }
        if (!request.getOptionsInProperties().isEmpty()) {
            for (String s : request.getOptionsInProperties()) {
                commentText.append(s + " ");
            }
            commentText.append("configure in jmeter.properties ");
        }
        if (request.getLimitRate() != 0) {
            commentText.append(
                    "Please configure the limit rate in 'httpclient.socket.http.cps' of 'jmeter.properties(374 line), the value is "
                            + request.getLimitRate() + ";");
        }
        if (!request.getOptionsNoSupport().isEmpty()) {
            for (String s : request.getOptionsNoSupport()) {
                commentText.append("--"+s + " ");
            }
            commentText.append("not supported; ");
        }
        if (request.getNoproxy()!=null) {
            commentText.append("Please configure noproxy list in terminal and restart JMeter. ");
            commentText.append("Look: https://jmeter.apache.org/usermanual/get-started.html#proxy_server");
        }
        if (!request.getCaCert().isEmpty()) {
            commentText.append("Please configure the SSL file with CA certificates in 'SSL configuration' of 'system.properties(49 line)'. ");
            commentText.append("Look: https://jmeter.apache.org/usermanual/properties_reference.html#ssl_config");
        }
        return commentText.toString();
    }
}
