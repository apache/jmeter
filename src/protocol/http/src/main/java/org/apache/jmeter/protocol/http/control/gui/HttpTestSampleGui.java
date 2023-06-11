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
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBaseSchema;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerProxy;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
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
    private JCheckBox retrieveEmbeddedResources;
    private JCheckBox concurrentDwn;
    private JTextField concurrentPool;
    private JCheckBox useMD5;
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
        isAJP = false;
        init();
    }

    // For use by AJP
    protected HttpTestSampleGui(boolean ajp) {
        isAJP = ajp;
        init();
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
        retrieveEmbeddedResources.setSelected(samplerBase.isImageParser());
        concurrentDwn.setSelected(samplerBase.isConcurrentDwn());
        concurrentPool.setText(samplerBase.getConcurrentPool());
        useMD5.setSelected(samplerBase.useMD5());
        embeddedAllowRE.setText(samplerBase.getEmbeddedUrlRE());
        embeddedExcludeRE.setText(samplerBase.getEmbededUrlExcludeRE());
        if (!isAJP) {
            sourceIpAddr.setText(samplerBase.getIpSource());
            sourceIpType.setSelectedIndex(samplerBase.getIpSourceType());

            proxyScheme.setText(samplerBase.getString(httpSchema.getProxy().getScheme()));
            proxyHost.setText(samplerBase.getString(httpSchema.getProxy().getHost()));
            proxyPort.setText(samplerBase.getString(httpSchema.getProxy().getPort()));
            proxyUser.setText(samplerBase.getString(httpSchema.getProxy().getUsername()));
            proxyPass.setText(samplerBase.getString(httpSchema.getProxy().getPassword()));
            httpImplementation.setSelectedItem(samplerBase.getString(httpSchema.getImplementation()));
            connectTimeOut.setText(samplerBase.getString(httpSchema.getConnectTimeout()));
            responseTimeOut.setText(samplerBase.getString(httpSchema.getResponseTimeout()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        HTTPSamplerBase sampler = new HTTPSamplerProxy();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        urlConfigGui.modifyTestElement(sampler);
        final HTTPSamplerBase samplerBase = (HTTPSamplerBase) sampler;
        HTTPSamplerBaseSchema httpSchema = samplerBase.getSchema();
        samplerBase.setImageParser(retrieveEmbeddedResources.isSelected());
        enableConcurrentDwn(retrieveEmbeddedResources.isSelected());
        samplerBase.setConcurrentDwn(concurrentDwn.isSelected());
        samplerBase.setConcurrentPool(concurrentPool.getText());
        samplerBase.setMD5(useMD5.isSelected());
        samplerBase.setEmbeddedUrlRE(embeddedAllowRE.getText());
        samplerBase.setEmbeddedUrlExcludeRE(embeddedExcludeRE.getText());
        if (!isAJP) {
            samplerBase.setIpSource(sourceIpAddr.getText());
            samplerBase.setIpSourceType(sourceIpType.getSelectedIndex());

            samplerBase.set(httpSchema.getProxy().getScheme(), proxyScheme.getText());
            samplerBase.set(httpSchema.getProxy().getHost(), proxyHost.getText());
            samplerBase.set(httpSchema.getProxy().getPort(), proxyPort.getText());
            samplerBase.set(httpSchema.getProxy().getUsername(), proxyUser.getText());
            samplerBase.set(httpSchema.getProxy().getPassword(), String.valueOf(proxyPass.getPassword()));
            samplerBase.set(httpSchema.getImplementation(), String.valueOf(httpImplementation.getSelectedItem()));
            samplerBase.set(httpSchema.getConnectTimeout(), connectTimeOut.getText());
            samplerBase.set(httpSchema.getResponseTimeout(), responseTimeOut.getText());
        }
        super.configureTestElement(sampler);
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
        retrieveEmbeddedResources = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images")); // $NON-NLS-1$
        // add a listener to activate or not concurrent dwn.
        retrieveEmbeddedResources.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) { enableConcurrentDwn(true); }
            else { enableConcurrentDwn(false); }
        });
        // Download concurrent resources
        concurrentDwn = new JCheckBox(JMeterUtils.getResString("web_testing_concurrent_download")); // $NON-NLS-1$
        concurrentDwn.addItemListener(e -> {
            if (retrieveEmbeddedResources.isSelected() && e.getStateChange() == ItemEvent.SELECTED) { concurrentPool.setEnabled(true); }
            else { concurrentPool.setEnabled(false); }
        });
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

        // Use MD5
        useMD5 = new JCheckBox(JMeterUtils.getResString("response_save_as_md5")); // $NON-NLS-1$
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();
        retrieveEmbeddedResources.setSelected(false);
        concurrentDwn.setSelected(false);
        concurrentPool.setText(String.valueOf(HTTPSamplerBase.CONCURRENT_POOL_SIZE));
        enableConcurrentDwn(false);
        useMD5.setSelected(false);
        urlConfigGui.clear();
        embeddedAllowRE.setText(""); // $NON-NLS-1$
        if (!isAJP) {
            sourceIpAddr.setText(""); // $NON-NLS-1$
            sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
            proxyScheme.setText(""); // $NON-NLS-1$
            proxyHost.setText(""); // $NON-NLS-1$
            proxyPort.setText(""); // $NON-NLS-1$
            proxyUser.setText(""); // $NON-NLS-1$
            proxyPass.setText(""); // $NON-NLS-1$
            httpImplementation.setSelectedItem(""); // $NON-NLS-1$
            connectTimeOut.setText(""); // $NON-NLS-1$
            responseTimeOut.setText(""); // $NON-NLS-1$
        }
    }

    private void enableConcurrentDwn(boolean enable) {
        concurrentDwn.setEnabled(enable);
        embeddedAllowRE.setEnabled(enable);
        embeddedExcludeRE.setEnabled(enable);
        concurrentPool.setEnabled(concurrentDwn.isSelected() && enable);
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
