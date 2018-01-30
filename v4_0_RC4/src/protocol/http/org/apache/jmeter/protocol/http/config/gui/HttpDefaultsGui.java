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

package org.apache.jmeter.protocol.http.config.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * GUI for Http Request defaults
 */
@GUIMenuSortOrder(5)
public class HttpDefaultsGui extends AbstractConfigGui {

    private static final long serialVersionUID = 241L;

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    private UrlConfigGui urlConfigGui;
    private JCheckBox retrieveEmbeddedResources;
    private JCheckBox concurrentDwn;
    private JTextField concurrentPool;
    private JCheckBox useMD5;
    private JLabeledTextField embeddedRE; // regular expression used to match against embedded resource URLs
    private JTextField sourceIpAddr; // does not apply to Java implementation
    private JComboBox<String> sourceIpType = new JComboBox<>(HTTPSamplerBase.getSourceTypeList());
    private JTextField proxyHost;
    private JTextField proxyPort;
    private JTextField proxyUser;
    private JPasswordField proxyPass;
    private JComboBox<String> httpImplementation = new JComboBox<>(HTTPSamplerFactory.getImplementations());
    private JTextField connectTimeOut;
    private JTextField responseTimeOut;

    public HttpDefaultsGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "url_config_title"; // $NON-NLS-1$
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        ConfigTestElement config = new ConfigTestElement();
        modifyTestElement(config);
        return config;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement config) {
        ConfigTestElement cfg = (ConfigTestElement ) config;
        ConfigTestElement el = (ConfigTestElement) urlConfigGui.createTestElement();
        cfg.clear(); 
        cfg.addConfigElement(el);
        super.configureTestElement(config);
        if (retrieveEmbeddedResources.isSelected()) {
            config.setProperty(new BooleanProperty(HTTPSamplerBase.IMAGE_PARSER, true));
        } else {
            config.removeProperty(HTTPSamplerBase.IMAGE_PARSER);
        }
        enableConcurrentDwn(retrieveEmbeddedResources.isSelected());
        if (concurrentDwn.isSelected()) {
            config.setProperty(new BooleanProperty(HTTPSamplerBase.CONCURRENT_DWN, true));
        } else {
            // The default is false, so we can remove the property to simplify JMX files
            // This also allows HTTPDefaults to work for this checkbox
            config.removeProperty(HTTPSamplerBase.CONCURRENT_DWN);
        }
        if(!StringUtils.isEmpty(concurrentPool.getText())) {
            config.setProperty(new StringProperty(HTTPSamplerBase.CONCURRENT_POOL,
                    concurrentPool.getText()));
        } else {
            config.setProperty(new StringProperty(HTTPSamplerBase.CONCURRENT_POOL,
                    String.valueOf(HTTPSamplerBase.CONCURRENT_POOL_SIZE)));
        }
        if(useMD5.isSelected()) {
            config.setProperty(new BooleanProperty(HTTPSamplerBase.MD5, true));
        } else {
            config.removeProperty(HTTPSamplerBase.MD5);
        }
        if (!StringUtils.isEmpty(embeddedRE.getText())) {
            config.setProperty(new StringProperty(HTTPSamplerBase.EMBEDDED_URL_RE,
                    embeddedRE.getText()));
        } else {
            config.removeProperty(HTTPSamplerBase.EMBEDDED_URL_RE);
        }
        
        if(!StringUtils.isEmpty(sourceIpAddr.getText())) {
            config.setProperty(new StringProperty(HTTPSamplerBase.IP_SOURCE,
                    sourceIpAddr.getText()));
            config.setProperty(new IntegerProperty(HTTPSamplerBase.IP_SOURCE_TYPE,
                    sourceIpType.getSelectedIndex()));
        } else {
            config.removeProperty(HTTPSamplerBase.IP_SOURCE);
            config.removeProperty(HTTPSamplerBase.IP_SOURCE_TYPE);
        }

        config.setProperty(HTTPSamplerBase.PROXYHOST, proxyHost.getText(),"");
        config.setProperty(HTTPSamplerBase.PROXYPORT, proxyPort.getText(),"");
        config.setProperty(HTTPSamplerBase.PROXYUSER, proxyUser.getText(),"");
        config.setProperty(HTTPSamplerBase.PROXYPASS, String.valueOf(proxyPass.getPassword()),"");
        config.setProperty(HTTPSamplerBase.IMPLEMENTATION, httpImplementation.getSelectedItem().toString(),"");
        config.setProperty(HTTPSamplerBase.CONNECT_TIMEOUT, connectTimeOut.getText());
        config.setProperty(HTTPSamplerBase.RESPONSE_TIMEOUT, responseTimeOut.getText());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
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
        embeddedRE.setText(""); // $NON-NLS-1$
        sourceIpAddr.setText(""); // $NON-NLS-1$
        sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
        proxyHost.setText(""); // $NON-NLS-1$
        proxyPort.setText(""); // $NON-NLS-1$
        proxyUser.setText(""); // $NON-NLS-1$
        proxyPass.setText(""); // $NON-NLS-1$
        httpImplementation.setSelectedItem(""); // $NON-NLS-1$
        connectTimeOut.setText(""); // $NON-NLS-1$
        responseTimeOut.setText(""); // $NON-NLS-1$
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        AbstractTestElement samplerBase = (AbstractTestElement) el;
        urlConfigGui.configure(el);
        retrieveEmbeddedResources.setSelected(samplerBase.getPropertyAsBoolean(HTTPSamplerBase.IMAGE_PARSER));
        concurrentDwn.setSelected(samplerBase.getPropertyAsBoolean(HTTPSamplerBase.CONCURRENT_DWN));
        concurrentPool.setText(samplerBase.getPropertyAsString(HTTPSamplerBase.CONCURRENT_POOL));
        useMD5.setSelected(samplerBase.getPropertyAsBoolean(HTTPSamplerBase.MD5, false));
        embeddedRE.setText(samplerBase.getPropertyAsString(HTTPSamplerBase.EMBEDDED_URL_RE, ""));//$NON-NLS-1$
        sourceIpAddr.setText(samplerBase.getPropertyAsString(HTTPSamplerBase.IP_SOURCE)); //$NON-NLS-1$
        sourceIpType.setSelectedIndex(
                samplerBase.getPropertyAsInt(HTTPSamplerBase.IP_SOURCE_TYPE,
                        HTTPSamplerBase.SOURCE_TYPE_DEFAULT));

        proxyHost.setText(samplerBase.getPropertyAsString(HTTPSamplerBase.PROXYHOST));
        proxyPort.setText(samplerBase.getPropertyAsString(HTTPSamplerBase.PROXYPORT));
        proxyUser.setText(samplerBase.getPropertyAsString(HTTPSamplerBase.PROXYUSER));
        proxyPass.setText(samplerBase.getPropertyAsString(HTTPSamplerBase.PROXYPASS));
        httpImplementation.setSelectedItem(samplerBase.getPropertyAsString(HTTPSamplerBase.IMPLEMENTATION));
        connectTimeOut.setText(samplerBase.getPropertyAsString(HTTPSamplerBase.CONNECT_TIMEOUT));
        responseTimeOut.setText(samplerBase.getPropertyAsString(HTTPSamplerBase.RESPONSE_TIMEOUT));
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        // URL CONFIG
        urlConfigGui = new UrlConfigGui(false, true, false);

        // HTTP request options
        JPanel httpOptions = new HorizontalPanel();
        httpOptions.add(getImplementationPanel());
        httpOptions.add(getTimeOutPanel());
        // AdvancedPanel (embedded resources, source address and optional tasks)
        JPanel advancedPanel = new VerticalPanel();
        advancedPanel.add(httpOptions);
        advancedPanel.add(createEmbeddedRsrcPanel());
        advancedPanel.add(createSourceAddrPanel());
        advancedPanel.add(getProxyServerPanel());
        advancedPanel.add(createOptionalTasksPanel());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_basic"), urlConfigGui);
        tabbedPane.add(JMeterUtils
                .getResString("web_testing_advanced"), advancedPanel);

        JPanel emptyPanel = new JPanel();
        emptyPanel.setMaximumSize(new Dimension());

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);        
        add(emptyPanel, BorderLayout.SOUTH);
    }
    
    private JPanel getTimeOutPanel() {
        JPanel timeOut = new HorizontalPanel();
        timeOut.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
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
        concurrentPool = new JTextField(2); // 2 columns size
        concurrentPool.setMinimumSize(new Dimension(10, (int) concurrentPool.getPreferredSize().getHeight()));
        concurrentPool.setMaximumSize(new Dimension(30, (int) concurrentPool.getPreferredSize().getHeight()));

        final JPanel embeddedRsrcPanel = new HorizontalPanel();
        embeddedRsrcPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_retrieve_title"))); // $NON-NLS-1$
        embeddedRsrcPanel.add(retrieveEmbeddedResources);
        embeddedRsrcPanel.add(concurrentDwn);
        embeddedRsrcPanel.add(concurrentPool);
        
        // Embedded URL match regex
        embeddedRE = new JLabeledTextField(JMeterUtils.getResString("web_testing_embedded_url_pattern"),20); // $NON-NLS-1$
        embeddedRsrcPanel.add(embeddedRE); 
        
        return embeddedRsrcPanel;
    }
    
    protected JPanel createSourceAddrPanel() {
        final JPanel sourceAddrPanel = new HorizontalPanel();
        sourceAddrPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("web_testing_source_ip"))); // $NON-NLS-1$

        sourceIpType.setSelectedIndex(HTTPSamplerBase.SourceType.HOSTNAME.ordinal()); //default: IP/Hostname
        sourceAddrPanel.add(sourceIpType);

        sourceIpAddr = new JTextField();
        sourceAddrPanel.add(sourceIpAddr);
        return sourceAddrPanel;
    }
    
    protected JPanel createOptionalTasksPanel() {
        // OPTIONAL TASKS
        final JPanel checkBoxPanel = new VerticalPanel();
        checkBoxPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("optional_tasks"))); // $NON-NLS-1$

        // Use MD5
        useMD5 = new JCheckBox(JMeterUtils.getResString("response_save_as_md5")); // $NON-NLS-1$

        checkBoxPanel.add(useMD5);

        return checkBoxPanel;
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
    
    private void enableConcurrentDwn(final boolean enable) {
        if (enable) {
            concurrentDwn.setEnabled(true);
            embeddedRE.setEnabled(true);
            if (concurrentDwn.isSelected()) {
                concurrentPool.setEnabled(true);
            }
        } else {
            concurrentDwn.setEnabled(false);
            concurrentPool.setEnabled(false);
            embeddedRE.setEnabled(false);
        }
    }
    
    /**
     * Create a panel containing the implementation details
     *
     * @return the panel
     */
    protected final JPanel getImplementationPanel(){
        JPanel implPanel = new HorizontalPanel();
        implPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_server_client"))); // $NON-NLS-1$
        implPanel.add(new JLabel(JMeterUtils.getResString("http_implementation"))); // $NON-NLS-1$
        httpImplementation.addItem("");// $NON-NLS-1$
        implPanel.add(httpImplementation);
        return implPanel;
    }

    /**
     * Create a panel containing the proxy server details
     *
     * @return the panel
     */
    protected final JPanel getProxyServerPanel(){
        JPanel proxyServer = new HorizontalPanel();
        proxyServer.add(getProxyHostPanel(), BorderLayout.CENTER);
        proxyServer.add(getProxyPortPanel(), BorderLayout.EAST);

        JPanel proxyLogin = new HorizontalPanel();
        proxyLogin.add(getProxyUserPanel());
        proxyLogin.add(getProxyPassPanel());

        JPanel proxyServerPanel = new HorizontalPanel();
        proxyServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_proxy_server_title"))); // $NON-NLS-1$
        proxyServerPanel.add(proxyServer);
        proxyServerPanel.add(proxyLogin);

        return proxyServerPanel;
    }

    private JPanel getProxyHostPanel() {
        proxyHost = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_domain")); // $NON-NLS-1$
        label.setLabelFor(proxyHost);
        label.setFont(FONT_SMALL);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyHost, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel getProxyPortPanel() {
        proxyPort = new JTextField(10);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_port")); // $NON-NLS-1$
        label.setLabelFor(proxyPort);
        label.setFont(FONT_SMALL);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyPort, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getProxyUserPanel() {
        proxyUser = new JTextField(5);

        JLabel label = new JLabel(JMeterUtils.getResString("username")); // $NON-NLS-1$
        label.setLabelFor(proxyUser);
        label.setFont(FONT_SMALL);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyUser, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getProxyPassPanel() {
        proxyPass = new JPasswordField(5);

        JLabel label = new JLabel(JMeterUtils.getResString("password")); // $NON-NLS-1$
        label.setLabelFor(proxyPass);
        label.setFont(FONT_SMALL);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyPass, BorderLayout.CENTER);
        return panel;
    }
}
