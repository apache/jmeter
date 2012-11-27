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

package org.apache.jmeter.protocol.http.control.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.WebServiceSampler;
import org.apache.jmeter.protocol.http.util.HTTPConstants;
import org.apache.jmeter.protocol.http.util.WSDLHelper;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * This is the GUI for the webservice samplers. It extends AbstractSamplerGui
 * and is modeled after the SOAP sampler GUI. I've added instructional notes to
 * the GUI for instructional purposes. XML parsing is pretty heavy weight,
 * therefore the notes address those situations. <br>
 * Created on: Jun 26, 2003
 *
 */
public class WebServiceSamplerGui extends AbstractSamplerGui implements java.awt.event.ActionListener {

    private static final long serialVersionUID = 240L;

    private final JLabeledTextField domain = new JLabeledTextField(JMeterUtils.getResString("web_server_domain")); // $NON-NLS-1$

    private final JLabeledTextField protocol = new JLabeledTextField(JMeterUtils.getResString("protocol"), 4); // $NON-NLS-1$

    private final JLabeledTextField port = new JLabeledTextField(JMeterUtils.getResString("web_server_port"), 4); // $NON-NLS-1$

    private final JLabeledTextField path = new JLabeledTextField(JMeterUtils.getResString("path")); // $NON-NLS-1$

    private final JLabeledTextField soapAction = new JLabeledTextField(JMeterUtils.getResString("webservice_soap_action")); // $NON-NLS-1$

    /**
     * checkbox for Session maintenance.
     */
    private JCheckBox maintainSession = new JCheckBox(JMeterUtils.getResString("webservice_maintain_session"), true); // $NON-NLS-1$

    
    private JTextArea soapXml;

    private final JLabeledTextField wsdlField = new JLabeledTextField(JMeterUtils.getResString("wsdl_url")); // $NON-NLS-1$

    private final JButton wsdlButton = new JButton(JMeterUtils.getResString("load_wsdl")); // $NON-NLS-1$

    private final JButton selectButton = new JButton(JMeterUtils.getResString("configure_wsdl")); // $NON-NLS-1$

    private JLabeledChoice wsdlMethods = null;

    private transient WSDLHelper HELPER = null;

    private final FilePanel soapXmlFile = new FilePanel(JMeterUtils.getResString("get_xml_from_file"), ".xml"); // $NON-NLS-1$

    private final JLabeledTextField randomXmlFile = new JLabeledTextField(JMeterUtils.getResString("get_xml_from_random")); // $NON-NLS-1$

    private final JLabeledTextField connectTimeout = new JLabeledTextField(JMeterUtils.getResString("webservice_timeout"), 4); // $NON-NLS-1$

    /**
     * checkbox for memory cache.
     */
    private JCheckBox memCache = new JCheckBox(JMeterUtils.getResString("memory_cache"), true); // $NON-NLS-1$

    /**
     * checkbox for reading the response
     */
    private JCheckBox readResponse = new JCheckBox(JMeterUtils.getResString("read_soap_response")); // $NON-NLS-1$

    /**
     * checkbox for use proxy
     */
    private JCheckBox useProxy = new JCheckBox(JMeterUtils.getResString("webservice_use_proxy")); // $NON-NLS-1$

    /**
     * text field for the proxy host
     */
    private JTextField proxyHost;

    /**
     * text field for the proxy port
     */
    private JTextField proxyPort;

    /**
     * Text note about read response and its usage.
     */
    private String readToolTip = JMeterUtils.getResString("read_response_note") // $NON-NLS-1$
                                  + " " // $NON-NLS-1$
                                  + JMeterUtils.getResString("read_response_note2") // $NON-NLS-1$
                                  + " " // $NON-NLS-1$
                                  + JMeterUtils.getResString("read_response_note3"); // $NON-NLS-1$

    /**
     * Text note for proxy
     */
    private String proxyToolTip = JMeterUtils.getResString("webservice_proxy_note") // $NON-NLS-1$
                                  + " " // $NON-NLS-1$
                                  + JMeterUtils.getResString("webservice_proxy_note2") // $NON-NLS-1$
                                  + " " // $NON-NLS-1$
                                  + JMeterUtils.getResString("webservice_proxy_note3"); // $NON-NLS-1$
    public WebServiceSamplerGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return "webservice_sampler_title"; // $NON-NLS-1$
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        WebServiceSampler sampler = new WebServiceSampler();
        this.configureTestElement(sampler);
        this.modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement s) {
        WebServiceSampler sampler = (WebServiceSampler) s;
        this.configureTestElement(sampler);
        sampler.setDomain(domain.getText());
        sampler.setProperty(HTTPSamplerBase.PORT,port.getText());
        sampler.setProtocol(protocol.getText());
        sampler.setPath(path.getText());
        sampler.setWsdlURL(wsdlField.getText());
        sampler.setMethod(HTTPConstants.POST);
        sampler.setSoapAction(soapAction.getText());
        sampler.setMaintainSession(maintainSession.isSelected());
        sampler.setXmlData(soapXml.getText());
        sampler.setXmlFile(soapXmlFile.getFilename());
        sampler.setXmlPathLoc(randomXmlFile.getText());
        sampler.setTimeout(connectTimeout.getText());
        sampler.setMemoryCache(memCache.isSelected());
        sampler.setReadResponse(readResponse.isSelected());
        sampler.setUseProxy(useProxy.isSelected());
        sampler.setProxyHost(proxyHost.getText());
        sampler.setProxyPort(proxyPort.getText());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        wsdlMethods.setValues(new String[0]);
        domain.setText(""); //$NON-NLS-1$
        protocol.setText(""); //$NON-NLS-1$
        port.setText(""); //$NON-NLS-1$
        path.setText(""); //$NON-NLS-1$
        soapAction.setText(""); //$NON-NLS-1$
        maintainSession.setSelected(WebServiceSampler.MAINTAIN_SESSION_DEFAULT);
        soapXml.setText(""); //$NON-NLS-1$
        wsdlField.setText(""); //$NON-NLS-1$
        randomXmlFile.setText(""); //$NON-NLS-1$
        connectTimeout.setText(""); //$NON-NLS-1$
        proxyHost.setText(""); //$NON-NLS-1$
        proxyPort.setText(""); //$NON-NLS-1$
        memCache.setSelected(true);
        readResponse.setSelected(false);
        useProxy.setSelected(false);
        soapXmlFile.setFilename(""); //$NON-NLS-1$
    }

    /**
     * init() adds soapAction to the mainPanel. The class reuses logic from
     * SOAPSampler, since it is common.
     */
    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createMessagePanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);
        this.add(mainPanel);
    }

    private final JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        
        JPanel wsdlHelper = new JPanel();
        wsdlHelper.setLayout(new BoxLayout(wsdlHelper, BoxLayout.Y_AXIS));
        wsdlHelper.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("webservice_configuration_wizard"))); // $NON-NLS-1$

        // Button for browsing webservice wsdl
        JPanel wsdlEntry = new JPanel();
        wsdlEntry.setLayout(new BoxLayout(wsdlEntry, BoxLayout.X_AXIS));
        Border margin = new EmptyBorder(0, 5, 0, 5);
        wsdlEntry.setBorder(margin);
        wsdlHelper.add(wsdlEntry);
        wsdlEntry.add(wsdlField);
        wsdlEntry.add(wsdlButton);
        wsdlButton.addActionListener(this);

        // Web Methods
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel selectLabel = new JLabel(JMeterUtils.getResString("webservice_methods")); // $NON-NLS-1$
        wsdlMethods = new JLabeledChoice();
        wsdlHelper.add(listPanel);
        listPanel.add(selectLabel);
        listPanel.add(wsdlMethods);
        listPanel.add(selectButton);
        selectButton.addActionListener(this);

        topPanel.add(wsdlHelper);
        
        JPanel urlPane = new JPanel();
        urlPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        urlPane.add(protocol);
        urlPane.add(Box.createRigidArea(new Dimension(5,0)));
        urlPane.add(domain);
        urlPane.add(Box.createRigidArea(new Dimension(5,0)));
        urlPane.add(port);
        urlPane.add(Box.createRigidArea(new Dimension(5,0)));
        urlPane.add(connectTimeout);
        topPanel.add(urlPane);
        
        topPanel.add(createParametersPanel());
        
        return topPanel;
    }

    private final JPanel createParametersPanel() {
        JPanel paramsPanel = new JPanel();
        paramsPanel.setLayout(new BoxLayout(paramsPanel, BoxLayout.X_AXIS));
        paramsPanel.add(path);
        paramsPanel.add(Box.createHorizontalGlue());        
        paramsPanel.add(soapAction);
        paramsPanel.add(Box.createHorizontalGlue());        
        paramsPanel.add(maintainSession);
        return paramsPanel;
    }
    
    private final JPanel createMessagePanel() {
        JPanel msgPanel = new JPanel();
        msgPanel.setLayout(new BorderLayout(5, 0));
        msgPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("webservice_message_soap"))); // $NON-NLS-1$

        JPanel soapXmlPane = new JPanel();
        soapXmlPane.setLayout(new BorderLayout(5, 0));
        soapXmlPane.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("soap_data_title"))); // $NON-NLS-1$
        soapXmlPane.setPreferredSize(new Dimension(4, 4)); // Permit dynamic resize of TextArea
        soapXml = new JTextArea();
        soapXml.setLineWrap(true);
        soapXml.setWrapStyleWord(true);
        soapXml.setTabSize(4); // improve xml display
        soapXmlPane.add(new JScrollPane(soapXml), BorderLayout.CENTER);
        msgPanel.add(soapXmlPane, BorderLayout.CENTER);
        
        JPanel southPane = new JPanel();
        southPane.setLayout(new BoxLayout(southPane, BoxLayout.Y_AXIS));
        southPane.add(soapXmlFile);
        JPanel randomXmlPane = new JPanel();
        randomXmlPane.setLayout(new BorderLayout(5, 0));
        randomXmlPane.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("webservice_get_xml_from_random_title"))); // $NON-NLS-1$
        randomXmlPane.add(randomXmlFile, BorderLayout.CENTER);
        southPane.add(randomXmlPane);
        msgPanel.add(southPane, BorderLayout.SOUTH);
        return msgPanel;
    }
    
    private final JPanel createBottomPanel() {
        JPanel optionPane = new JPanel();
        optionPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("option"))); // $NON-NLS-1$
        optionPane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        JPanel ckboxPane = new HorizontalPanel();
        ckboxPane.add(memCache, BorderLayout.WEST);
        ckboxPane.add(readResponse, BorderLayout.CENTER);
        readResponse.setToolTipText(readToolTip);
        optionPane.add(ckboxPane);

        // add the proxy elements
        optionPane.add(getProxyServerPanel());
        return optionPane;
        
    }
    /**
     * Create a panel containing the proxy server details
     *
     * @return the panel
     */
    private final JPanel getProxyServerPanel(){
        JPanel proxyServer = new JPanel();
        proxyServer.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        proxyServer.add(useProxy);
        useProxy.addActionListener(this);
        useProxy.setToolTipText(proxyToolTip);
        proxyServer.add(Box.createRigidArea(new Dimension(5,0)));
        proxyServer.add(getProxyHostPanel());
        proxyServer.add(Box.createRigidArea(new Dimension(5,0)));
        proxyServer.add(getProxyPortPanel());
        return proxyServer;
    }
    
    private JPanel getProxyHostPanel() {
        proxyHost = new JTextField(12);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_domain")); // $NON-NLS-1$
        label.setLabelFor(proxyHost);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyHost, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel getProxyPortPanel() {
        proxyPort = new JTextField(4);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_port")); // $NON-NLS-1$
        label.setLabelFor(proxyPort);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyPort, BorderLayout.CENTER);

        return panel;
    }

    /**
     * the implementation loads the URL and the soap action for the request.
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        WebServiceSampler sampler = (WebServiceSampler) el;
        wsdlField.setText(sampler.getWsdlURL());
        final String wsdlText = wsdlField.getText();
        if (wsdlText != null && wsdlText.length() > 0) {
            fillWsdlMethods(wsdlField.getText(), true, sampler.getSoapAction());
        }
        protocol.setText(sampler.getProtocol());
        domain.setText(sampler.getDomain());
        port.setText(sampler.getPropertyAsString(HTTPSamplerBase.PORT));
        path.setText(sampler.getPath());
        soapAction.setText(sampler.getSoapAction());
        maintainSession.setSelected(sampler.getMaintainSession());
        soapXml.setText(sampler.getXmlData());
        soapXml.setCaretPosition(0); // go to 1st line
        soapXmlFile.setFilename(sampler.getXmlFile());
        randomXmlFile.setText(sampler.getXmlPathLoc());
        connectTimeout.setText(sampler.getTimeout());
        memCache.setSelected(sampler.getMemoryCache());
        readResponse.setSelected(sampler.getReadResponse());
        useProxy.setSelected(sampler.getUseProxy());
        if (sampler.getProxyHost().length() == 0) {
            proxyHost.setEnabled(false);
        } else {
            proxyHost.setText(sampler.getProxyHost());
        }
        if (sampler.getProxyPort() == 0) {
            proxyPort.setEnabled(false);
        } else {
            proxyPort.setText(String.valueOf(sampler.getProxyPort()));
        }
    }

    /**
     * configure the sampler from the WSDL. If the WSDL did not include service
     * node, it will use the original URL minus the querystring. That may not be
     * correct, so we should probably add a note. For Microsoft webservices it
     * will work, since that's how IIS works.
     */
    public void configureFromWSDL() {
        if (HELPER != null) {
            if(HELPER.getBinding() != null) {
                this.protocol.setText(HELPER.getProtocol());
                this.domain.setText(HELPER.getBindingHost());
                if (HELPER.getBindingPort() > 0) {
                    this.port.setText(String.valueOf(HELPER.getBindingPort()));
                } else {
                    this.port.setText("80"); // $NON-NLS-1$
                }
                this.path.setText(HELPER.getBindingPath());
            }
            this.soapAction.setText(HELPER.getSoapAction(this.wsdlMethods.getText()));
        }
    }

    /**
     * The method uses WSDLHelper to get the information from the WSDL. Since
     * the logic for getting the description is isolated to this method, we can
     * easily replace it with a different WSDL driver later on.
     *
     * @param url
     * @param silent 
     * @return array of web methods
     */
    public String[] browseWSDL(String url, boolean silent) {
        try {
            // We get the AuthManager and pass it to the WSDLHelper
            // once the sampler is updated to Axis, all of this stuff
            // should not be necessary. Now I just need to find the
            // time and motivation to do it.
            WebServiceSampler sampler = (WebServiceSampler) this.createTestElement();
            AuthManager manager = sampler.getAuthManager();
            HELPER = new WSDLHelper(url, manager);
            HELPER.parse();
            return HELPER.getWebMethods();
        } catch (Exception exception) {
            if (!silent) {
                JOptionPane.showConfirmDialog(this,
                        JMeterUtils.getResString("wsdl_helper_error") // $NON-NLS-1$
                        +"\n"+exception, // $NON-NLS-1$
                        JMeterUtils.getResString("warning"), // $NON-NLS-1$
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
    }

    /**
     * method from ActionListener
     *
     * @param event
     *            that occurred
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        final Object eventSource = event.getSource();
        if (eventSource == selectButton) {
            this.configureFromWSDL();
        } else if (eventSource == useProxy) {
            // if use proxy is checked, we enable
            // the text fields for the host and port
            boolean use = useProxy.isSelected();
            if (use) {
                proxyHost.setEnabled(true);
                proxyPort.setEnabled(true);
            } else {
                proxyHost.setEnabled(false);
                proxyPort.setEnabled(false);
            }
        } else if (eventSource == wsdlButton){
            final String wsdlText = wsdlField.getText();
            if (wsdlText != null && wsdlText.length() > 0) {
                fillWsdlMethods(wsdlText, false, null);
            } else {
                JOptionPane.showConfirmDialog(this,
                        JMeterUtils.getResString("wsdl_url_error"), // $NON-NLS-1$
                        JMeterUtils.getResString("warning"), // $NON-NLS-1$
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * @param wsdlText
     * @param silent
     * @param soapAction 
     */
    private void fillWsdlMethods(final String wsdlText, boolean silent, String soapAction) {
        String[] wsdlData = browseWSDL(wsdlText, silent);
        if (wsdlData != null) {
            wsdlMethods.setValues(wsdlData);
            if (HELPER != null && soapAction != null) {
                String selected = HELPER.getSoapActionName(soapAction);
                if (selected != null) {
                    wsdlMethods.setText(selected);
                }
            }
            wsdlMethods.repaint();
        }
    }

}
