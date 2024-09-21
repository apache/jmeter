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

package org.apache.jmeter.protocol.http.control.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.JBooleanPropertyEditor;
import org.apache.jmeter.gui.JTextComponentBinding;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBaseSchema;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JEditableCheckBox;
import org.apache.jorphan.gui.JFactory;

import net.miginfocom.swing.MigLayout;

/**
 * HTTP Sampler GUI
 */
@GUIMenuSortOrder(1)
@TestElementMetadata(labelResource = "web_testing_title")
public class HttpTestSampleGui extends AbstractSamplerGui {

    private static final long serialVersionUID = 242L;

    private UrlConfigGui urlConfigGui;
    private final JBooleanPropertyEditor retrieveEmbeddedResources = new JBooleanPropertyEditor(
            HTTPSamplerBaseSchema.INSTANCE.getRetrieveEmbeddedResources(),
            JMeterUtils.getResString("web_testing_retrieve_images"));
    private final JBooleanPropertyEditor concurrentDwn = new JBooleanPropertyEditor(
            HTTPSamplerBaseSchema.INSTANCE.getConcurrentDownload(),
            JMeterUtils.getResString("web_testing_concurrent_download"));
    private JTextField concurrentPool;
    private final JBooleanPropertyEditor useMD5 = new JBooleanPropertyEditor(
            HTTPSamplerBaseSchema.INSTANCE.getStoreAsMD5(),
            JMeterUtils.getResString("response_save_as_md5")); // $NON-NLS-1$
    private JTextField embeddedAllowRE; // regular expression used to match against embedded resource URLs to allow
    private JTextField embeddedExcludeRE; // regular expression used to match against embedded resource URLs to exclude
    private JTextField sourceIpAddr; // does not apply to Java implementation
    private final JComboBox<String> sourceIpType = new JComboBox<>(HTTPSamplerBase.getSourceTypeList());
    private JTextField proxyScheme;
    private JTextField proxyHost;
    private JTextField proxyPort;
    private JTextField proxyUser;
    private JPasswordField proxyPass;
    private final JComboBox<String> httpImplementation = new JComboBox<>(HTTPSamplerFactory.getImplementations());
    private JTextField connectTimeOut;
    private JTextField responseTimeOut;

    private final boolean isAJP;

    public HttpTestSampleGui() {
        this(false);
    }

    // For use by AJP
    protected HttpTestSampleGui(boolean ajp) {
        isAJP = ajp;
        init();
        HTTPSamplerBaseSchema schema = HTTPSamplerBaseSchema.INSTANCE;
        bindingGroup.addAll(
                Arrays.asList(
                        retrieveEmbeddedResources,
                        concurrentDwn,
                        new JTextComponentBinding(concurrentPool, schema.getConcurrentDownloadPoolSize()),
                        useMD5,
                        new JTextComponentBinding(embeddedAllowRE, schema.getEmbeddedUrlAllowRegex()),
                        new JTextComponentBinding(embeddedExcludeRE, schema.getEmbeddedUrlExcludeRegex())
                )
        );
        if (!isAJP) {
            bindingGroup.addAll(
                    Arrays.asList(
                            new JTextComponentBinding(sourceIpAddr, schema.getIpSource()),
                            // TODO: sourceIpType
                            new JTextComponentBinding(proxyScheme, schema.getProxy().getScheme()),
                            new JTextComponentBinding(proxyHost, schema.getProxy().getHost()),
                            new JTextComponentBinding(proxyPort, schema.getProxy().getPort()),
                            new JTextComponentBinding(proxyUser, schema.getProxy().getUsername()),
                            new JTextComponentBinding(proxyPass, schema.getProxy().getPassword()),
                            // TODO: httpImplementation
                            new JTextComponentBinding(connectTimeOut, schema.getConnectTimeout()),
                            new JTextComponentBinding(responseTimeOut, schema.getResponseTimeout())
                    )
            );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) element;
        HTTPSamplerBaseSchema httpSchema = HTTPSamplerBaseSchema.INSTANCE;
        urlConfigGui.configure(element);
        if (!isAJP) {
            sourceIpType.setSelectedIndex(samplerBase.getIpSourceType());
            httpImplementation.setSelectedItem(samplerBase.getString(httpSchema.getImplementation()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement makeTestElement() {
        return new HTTPSamplerProxy();
    }

    @Override
    public void assignDefaultValues(TestElement element) {
        super.assignDefaultValues(element);
        HTTPSamplerBaseSchema schema = HTTPSamplerBaseSchema.INSTANCE;
        // It probably does not make much sense overriding HTTP method with HTTP Request Defaults, so we set it here
        element.set(schema.getMethod(), HTTPConstants.GET);
        element.set(schema.getFollowRedirects(), true);
        element.set(schema.getUseKeepalive(), true);
        urlConfigGui.assignDefaultValues(element);
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        super.modifyTestElement(sampler);
        urlConfigGui.modifyTestElement(sampler);
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) sampler;
        HTTPSamplerBaseSchema httpSchema = samplerBase.getSchema();
        enableConcurrentDwn();
        if (!isAJP) {
            if (!StringUtils.isEmpty(sourceIpAddr.getText())) {
                samplerBase.set(httpSchema.getIpSourceType(), sourceIpType.getSelectedIndex());
            } else {
                samplerBase.removeProperty(httpSchema.getIpSourceType());
            }
            String selectedImplementation = String.valueOf(httpImplementation.getSelectedItem());
            samplerBase.set(httpSchema.getImplementation(), StringUtils.defaultIfBlank(selectedImplementation, null));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return "web_testing_title"; // $NON-NLS-1$
    }

    private void init() {// called from ctor, so must not be overridable
        setLayout(new BorderLayout(0, 5));
        setBorder(BorderFactory.createEmptyBorder());

        JTabbedPane tabbedPane = createTabbedConfigPane();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBorder(makeBorder());
        wrapper.add(makeTitlePanel(), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, wrapper, tabbedPane);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setOneTouchExpandable(true);
        add(splitPane);
    }

    /**
     * Create the parameters configuration tabstrip which includes the Basic tab ({@link UrlConfigGui})
     * and the Advanced tab by default.
     * @return the parameters configuration tabstrip which includes the Basic tab ({@link UrlConfigGui})
     *         and the Advanced tab by default
     */
    protected JTabbedPane createTabbedConfigPane() {
        final JTabbedPane tabbedPane = new JTabbedPane();

        // URL CONFIG
        urlConfigGui = createUrlConfigGui();

        tabbedPane.add(JMeterUtils
                .getResString("web_testing_basic"), urlConfigGui);

        // AdvancedPanel (embedded resources, source address and optional tasks)
        final JPanel advancedPanel = createAdvancedConfigPanel();
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_advanced"), advancedPanel);

        return tabbedPane;
    }

    /**
     * Create a {@link UrlConfigGui} which is used as the Basic tab in the parameters configuration tabstrip.
     * @return a {@link UrlConfigGui} which is used as the Basic tab
     */
    protected UrlConfigGui createUrlConfigGui() {
        final UrlConfigGui configGui = new UrlConfigGui(true, true, true);
        configGui.setBorder(makeBorder());
        return configGui;
    }

    private JPanel createAdvancedConfigPanel() {
        // HTTP request options
        JPanel httpOptions = new HorizontalPanel();
        httpOptions.add(getImplementationPanel());
        httpOptions.add(getTimeOutPanel());

        // AdvancedPanel (embedded resources, source address and optional tasks)
        JPanel advancedPanel = new VerticalPanel();
        advancedPanel.setBorder(makeBorder());
        if (!isAJP) {
            advancedPanel.add(httpOptions);
        }
        advancedPanel.add(createEmbeddedRsrcPanel());
        if (!isAJP) {
            advancedPanel.add(createSourceAddrPanel());
            advancedPanel.add(getProxyServerPanel());
        }

        advancedPanel.add(createOptionalTasksPanel());
        return advancedPanel;
    }

    private JPanel getTimeOutPanel() {
        JPanel timeOut = new HorizontalPanel();
        timeOut.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("web_server_timeout_title"))); // $NON-NLS-1$
        final JPanel connPanel = getConnectTimeOutPanel();
        final JPanel reqPanel = getResponseTimeOutPanel();
        timeOut.add(connPanel);
        timeOut.add(reqPanel);
        return timeOut;
    }

    private JPanel getConnectTimeOutPanel() {
        connectTimeOut = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_timeout_connect")); // $NON-NLS-1$
        label.setLabelFor(connectTimeOut);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(connectTimeOut, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getResponseTimeOutPanel() {
        responseTimeOut = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_timeout_response")); // $NON-NLS-1$
        label.setLabelFor(responseTimeOut);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(responseTimeOut, BorderLayout.CENTER);

        return panel;
    }

    protected JPanel createEmbeddedRsrcPanel() {
        // retrieve Embedded resources
        // add a listener to activate or not concurrent dwn.
        retrieveEmbeddedResources.addPropertyChangeListener(
                JEditableCheckBox.VALUE_PROPERTY,
                ev -> enableConcurrentDwn());
        // Download concurrent resources
        concurrentDwn.addPropertyChangeListener(
                JEditableCheckBox.VALUE_PROPERTY,
                ev -> enableConcurrentDwn());
        concurrentPool = new JTextField(2); // 2 column size
        concurrentPool.setMinimumSize(new Dimension(10, (int) concurrentPool.getPreferredSize().getHeight()));
        concurrentPool.setMaximumSize(new Dimension(60, (int) concurrentPool.getPreferredSize().getHeight()));

        final JPanel embeddedRsrcPanel = new JPanel(new MigLayout());
        embeddedRsrcPanel.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("web_testing_retrieve_title"))); // $NON-NLS-1$
        embeddedRsrcPanel.add(retrieveEmbeddedResources);
        embeddedRsrcPanel.add(concurrentDwn);
        embeddedRsrcPanel.add(concurrentPool, "wrap");

        // Embedded URL match regex
        embeddedAllowRE = addTextFieldWithLabel(embeddedRsrcPanel, JMeterUtils.getResString("web_testing_embedded_url_pattern")); // $NON-NLS-1$

        // Embedded URL to not match regex
        embeddedExcludeRE = addTextFieldWithLabel(embeddedRsrcPanel, JMeterUtils.getResString("web_testing_embedded_url_exclude_pattern")); // $NON-NLS-1$

        return embeddedRsrcPanel;
    }

    private static JTextField addTextFieldWithLabel(JPanel panel, String labelText) {
        JLabel label = new JLabel(labelText); // $NON-NLS-1$
        JTextField field = new JTextField(100);
        label.setLabelFor(field);
        panel.add(label);
        panel.add(field, "span");
        return field;
    }

    /**
     * Create a panel containing the implementation details
     *
     * @return the panel
     */
    protected final JPanel getImplementationPanel(){
        JPanel implPanel = new HorizontalPanel();
        implPanel.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("web_server_client"))); // $NON-NLS-1$
        implPanel.add(new JLabel(JMeterUtils.getResString("http_implementation"))); // $NON-NLS-1$
        httpImplementation.addItem("");// $NON-NLS-1$
        implPanel.add(httpImplementation);
        return implPanel;
    }

    protected JPanel createOptionalTasksPanel() {
        // OPTIONAL TASKS
        final JPanel checkBoxPanel = new VerticalPanel();
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("optional_tasks"))); // $NON-NLS-1$

        checkBoxPanel.add(useMD5);

        return checkBoxPanel;
    }

    protected JPanel createSourceAddrPanel() {
        final JPanel sourceAddrPanel = new HorizontalPanel();
        sourceAddrPanel.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("web_testing_source_ip"))); // $NON-NLS-1$

        // Add a new field source ip address (for HC implementations only)
        sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
        sourceAddrPanel.add(sourceIpType);

        sourceIpAddr = new JTextField();
        sourceAddrPanel.add(sourceIpAddr);
        return sourceAddrPanel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public void clearGui() {
        super.clearGui();
        urlConfigGui.clear();
    }

    private void enableConcurrentDwn() {
        boolean enable = !JEditableCheckBox.Value.of(false).equals(retrieveEmbeddedResources.getValue());
        concurrentDwn.setEnabled(enable);
        embeddedAllowRE.setEnabled(enable);
        embeddedExcludeRE.setEnabled(enable);
        // Allow editing the pool size if "download concurrently" checkbox is set or has expression
        concurrentPool.setEnabled(enable && !concurrentDwn.getValue().equals(JEditableCheckBox.Value.of(false)));
    }


    /**
     * Create a panel containing the proxy server details
     *
     * @return the panel
     */
    protected final JPanel getProxyServerPanel(){
        JPanel proxyServer = new HorizontalPanel();
        proxyServer.add(getProxySchemePanel(), BorderLayout.WEST);
        proxyServer.add(getProxyHostPanel(), BorderLayout.CENTER);
        proxyServer.add(getProxyPortPanel(), BorderLayout.EAST);

        JPanel proxyLogin = new HorizontalPanel();
        proxyLogin.add(getProxyUserPanel());
        proxyLogin.add(getProxyPassPanel());

        JPanel proxyServerPanel = new HorizontalPanel();
        proxyServerPanel.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("web_proxy_server_title"))); // $NON-NLS-1$
        proxyServerPanel.add(proxyServer);
        proxyServerPanel.add(proxyLogin);

        return proxyServerPanel;
    }

    private JPanel getProxySchemePanel() {
        proxyScheme = new JTextField(5);

        JLabel label = new JLabel(JMeterUtils.getResString("web_proxy_scheme")); // $NON-NLS-1$
        label.setLabelFor(proxyScheme);
        JFactory.small(label);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyScheme, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getProxyHostPanel() {
        proxyHost = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_domain")); // $NON-NLS-1$
        label.setLabelFor(proxyHost);
        JFactory.small(label);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyHost, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getProxyPortPanel() {
        proxyPort = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_port")); // $NON-NLS-1$
        label.setLabelFor(proxyPort);
        JFactory.small(label);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyPort, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getProxyUserPanel() {
        proxyUser = new JTextField(5);

        JLabel label = new JLabel(JMeterUtils.getResString("username")); // $NON-NLS-1$
        label.setLabelFor(proxyUser);
        JFactory.small(label);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyUser, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getProxyPassPanel() {
        proxyPass = new JPasswordField(5);

        JLabel label = new JLabel(JMeterUtils.getResString("password")); // $NON-NLS-1$
        label.setLabelFor(proxyPass);
        JFactory.small(label);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyPass, BorderLayout.CENTER);
        return panel;
    }
}
