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
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.ArrayUtils;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.WebServiceSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.apache.jmeter.protocol.http.util.WSDLHelper;
import org.apache.jmeter.protocol.http.control.AuthManager;

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

    private final JLabeledTextField protocol = new JLabeledTextField(JMeterUtils.getResString("protocol")); // $NON-NLS-1$

    private final JLabeledTextField port = new JLabeledTextField(JMeterUtils.getResString("web_server_port")); // $NON-NLS-1$

    private final JLabeledTextField path = new JLabeledTextField(JMeterUtils.getResString("path")); // $NON-NLS-1$

    private final JLabeledTextField soapAction = new JLabeledTextField(JMeterUtils.getResString("webservice_soap_action")); // $NON-NLS-1$

    private final JLabeledTextArea soapXml = new JLabeledTextArea(JMeterUtils.getResString("soap_data_title")); // $NON-NLS-1$

    private final JLabeledTextField wsdlField = new JLabeledTextField(JMeterUtils.getResString("wsdl_url")); // $NON-NLS-1$

    private final JButton wsdlButton = new JButton(JMeterUtils.getResString("load_wsdl")); // $NON-NLS-1$

    private final JButton selectButton = new JButton(JMeterUtils.getResString("configure_wsdl")); // $NON-NLS-1$

    private JLabeledChoice wsdlMethods = null;

    private transient WSDLHelper HELPER = null;

    private final FilePanel soapXmlFile = new FilePanel(JMeterUtils.getResString("get_xml_from_file"), ".xml"); // $NON-NLS-1$

    private final JLabeledTextField randomXmlFile = new JLabeledTextField(JMeterUtils.getResString("get_xml_from_random")); // $NON-NLS-1$

    private final JLabeledTextField connectTimeout = new JLabeledTextField(JMeterUtils.getResString("webservice_timeout")); // $NON-NLS-1$

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
    private JLabeledTextField proxyHost = new JLabeledTextField(JMeterUtils.getResString("webservice_proxy_host")); // $NON-NLS-1$

    /**
     * text field for the proxy port
     */
    private JLabeledTextField proxyPort = new JLabeledTextField(JMeterUtils.getResString("webservice_proxy_port")); // $NON-NLS-1$

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

    public String getLabelResource() {
        return "webservice_sampler_title"; // $NON-NLS-1$
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
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
    public void modifyTestElement(TestElement s) {
        WebServiceSampler sampler = (WebServiceSampler) s;
        this.configureTestElement(sampler);
        sampler.setDomain(domain.getText());
        sampler.setProperty(HTTPSamplerBase.PORT,port.getText());
        sampler.setProtocol(protocol.getText());
        sampler.setPath(path.getText());
        sampler.setWsdlURL(wsdlField.getText());
        sampler.setMethod(HTTPSamplerBase.POST);
        sampler.setSoapAction(soapAction.getText());
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
        Border margin = new EmptyBorder(10, 10, 5, 10);
        mainPanel.setBorder(margin);
        mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));

        // Button for browsing webservice wsdl
        JPanel wsdlEntry = new JPanel();
        mainPanel.add(wsdlEntry);
        wsdlEntry.add(wsdlField);
        wsdlEntry.add(wsdlButton);
        wsdlButton.addActionListener(this);

        // Web Methods
        JPanel listPanel = new JPanel();
        JLabel selectLabel = new JLabel(JMeterUtils.getResString("webservice_methods")); // $NON-NLS-1$
        wsdlMethods = new JLabeledChoice();
        mainPanel.add(listPanel);
        listPanel.add(selectLabel);
        listPanel.add(wsdlMethods);
        listPanel.add(selectButton);
        selectButton.addActionListener(this);

        mainPanel.add(protocol);
        mainPanel.add(domain);
        mainPanel.add(port);
        mainPanel.add(path);
        mainPanel.add(connectTimeout);
        mainPanel.add(soapAction);
        // OPTIONAL TASKS
        // we create a preferred size for the soap text area
        // the width is the same as the soap file browser
        Dimension pref = new Dimension(400, 200);
        soapXml.setPreferredSize(pref);
        mainPanel.add(soapXml);
        mainPanel.add(soapXmlFile);
        mainPanel.add(randomXmlFile);
        mainPanel.add(memCache);
        mainPanel.add(readResponse);
        readResponse.setToolTipText(readToolTip);

        // add the proxy elements
        mainPanel.add(useProxy);
        useProxy.addActionListener(this);
        useProxy.setToolTipText(proxyToolTip);
        mainPanel.add(proxyHost);
        mainPanel.add(proxyPort);

        this.add(mainPanel);
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
            fillWsdlMethods(wsdlField.getText(), true);
        }
        protocol.setText(sampler.getProtocol());
        domain.setText(sampler.getDomain());
        port.setText(sampler.getPropertyAsString(HTTPSamplerBase.PORT));
        path.setText(sampler.getPath());
        soapAction.setText(sampler.getSoapAction());
        soapXml.setText(sampler.getXmlData());
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
                fillWsdlMethods(wsdlText, false);
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
     */
    private void fillWsdlMethods(final String wsdlText, boolean silent) {
        String[] wsdlData = browseWSDL(wsdlText, silent);
        if (wsdlData != null) {
            wsdlMethods.setValues(wsdlData);
            wsdlMethods.repaint();
        }
    }

}
