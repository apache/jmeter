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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;

/**
 * Basic URL / HTTP Request configuration:
 * - host and port
 * - connect and response timeouts
 * - path, method, encoding, parameters
 * - redirects & keepalive
 */
public class UrlConfigGui extends JPanel implements ChangeListener {

    private static final long serialVersionUID = 240L;

    private static final int TAB_PARAMETERS = 0;
    
    private static final int TAB_RAW_BODY = 1;
    
    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 12);

    private HTTPArgumentsPanel argsPanel;

    private JTextField domain;

    private JTextField port;

    private JTextField proxyHost;

    private JTextField proxyPort;

    private JTextField proxyUser;

    private JPasswordField proxyPass;

    private JTextField connectTimeOut;

    private JTextField responseTimeOut;

    private JTextField protocol;

    private JTextField contentEncoding;

    private JTextField path;

    private JCheckBox followRedirects;

    private JCheckBox autoRedirects;

    private JCheckBox useKeepAlive;

    private JCheckBox useMultipartForPost;

    private JCheckBox useBrowserCompatibleMultipartMode;

    private JLabeledChoice method;
    
    private JLabeledChoice httpImplementation;

    private final boolean notConfigOnly;
    // set this false to suppress some items for use in HTTP Request defaults
    
    private final boolean showImplementation; // Set false for AJP

    // Body data
    private JSyntaxTextArea postBodyContent;

    // Tabbed pane that contains parameters and raw body
    private ValidationTabbedPane postContentTabbedPane;

    private boolean showRawBodyPane;

    public UrlConfigGui() {
        this(true);
    }

    /**
     * @param showSamplerFields
     */
    public UrlConfigGui(boolean showSamplerFields) {
        this(showSamplerFields, true, true);
    }

    /**
     * @param showSamplerFields
     * @param showImplementation Show HTTP Implementation
     * @param showRawBodyPane 
     */
    public UrlConfigGui(boolean showSamplerFields, boolean showImplementation, boolean showRawBodyPane) {
        notConfigOnly=showSamplerFields;
        this.showImplementation = showImplementation;
        this.showRawBodyPane = showRawBodyPane;
        init();
    }

    public void clear() {
        domain.setText(""); // $NON-NLS-1$
        if (notConfigOnly){
            followRedirects.setSelected(true);
            autoRedirects.setSelected(false);
            method.setText(HTTPSamplerBase.DEFAULT_METHOD);
            useKeepAlive.setSelected(true);
            useMultipartForPost.setSelected(false);
            useBrowserCompatibleMultipartMode.setSelected(HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT);
        }
        if (showImplementation) {
            httpImplementation.setText(""); // $NON-NLS-1$
        }
        path.setText(""); // $NON-NLS-1$
        port.setText(""); // $NON-NLS-1$
        proxyHost.setText(""); // $NON-NLS-1$
        proxyPort.setText(""); // $NON-NLS-1$
        proxyUser.setText(""); // $NON-NLS-1$
        proxyPass.setText(""); // $NON-NLS-1$
        connectTimeOut.setText(""); // $NON-NLS-1$
        responseTimeOut.setText(""); // $NON-NLS-1$
        protocol.setText(""); // $NON-NLS-1$
        contentEncoding.setText(""); // $NON-NLS-1$
        argsPanel.clear();
        if(showRawBodyPane) {
            postBodyContent.setInitialText("");// $NON-NLS-1$
        }
        postContentTabbedPane.setSelectedIndex(TAB_PARAMETERS, false);
    }

    public TestElement createTestElement() {
        ConfigTestElement element = new ConfigTestElement();

        element.setName(this.getName());
        element.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
        element.setProperty(TestElement.TEST_CLASS, element.getClass().getName());
        modifyTestElement(element);
        return element;
    }

    /**
     * Save the GUI values in the sampler.
     *
     * @param element
     */
    public void modifyTestElement(TestElement element) {
        boolean useRaw = postContentTabbedPane.getSelectedIndex()==TAB_RAW_BODY;
        Arguments args;
        if(useRaw) {
            args = new Arguments();
            String text = postBodyContent.getText();
            /*
             * Textfield uses \n (LF) to delimit lines; we need to send CRLF.
             * Rather than change the way that arguments are processed by the
             * samplers for raw data, it is easier to fix the data.
             * On retrival, CRLF is converted back to LF for storage in the text field.
             * See
             */
            HTTPArgument arg = new HTTPArgument("", text.replaceAll("\n","\r\n"), false);
            arg.setAlwaysEncoded(false);
            args.addArgument(arg);
        } else {
            args = (Arguments) argsPanel.createTestElement();
            HTTPArgument.convertArgumentsToHTTP(args);
        }
        element.setProperty(HTTPSamplerBase.POST_BODY_RAW, useRaw, HTTPSamplerBase.POST_BODY_RAW_DEFAULT);
        element.setProperty(new TestElementProperty(HTTPSamplerBase.ARGUMENTS, args));
        element.setProperty(HTTPSamplerBase.DOMAIN, domain.getText());
        element.setProperty(HTTPSamplerBase.PORT, port.getText());
        element.setProperty(HTTPSamplerBase.PROXYHOST, proxyHost.getText(),"");
        element.setProperty(HTTPSamplerBase.PROXYPORT, proxyPort.getText(),"");
        element.setProperty(HTTPSamplerBase.PROXYUSER, proxyUser.getText(),"");
        element.setProperty(HTTPSamplerBase.PROXYPASS, String.valueOf(proxyPass.getPassword()),"");
        element.setProperty(HTTPSamplerBase.CONNECT_TIMEOUT, connectTimeOut.getText());
        element.setProperty(HTTPSamplerBase.RESPONSE_TIMEOUT, responseTimeOut.getText());
        element.setProperty(HTTPSamplerBase.PROTOCOL, protocol.getText());
        element.setProperty(HTTPSamplerBase.CONTENT_ENCODING, contentEncoding.getText());
        element.setProperty(HTTPSamplerBase.PATH, path.getText());
        if (notConfigOnly){
            element.setProperty(HTTPSamplerBase.METHOD, method.getText());
            element.setProperty(new BooleanProperty(HTTPSamplerBase.FOLLOW_REDIRECTS, followRedirects.isSelected()));
            element.setProperty(new BooleanProperty(HTTPSamplerBase.AUTO_REDIRECTS, autoRedirects.isSelected()));
            element.setProperty(new BooleanProperty(HTTPSamplerBase.USE_KEEPALIVE, useKeepAlive.isSelected()));
            element.setProperty(new BooleanProperty(HTTPSamplerBase.DO_MULTIPART_POST, useMultipartForPost.isSelected()));
            element.setProperty(HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART, useBrowserCompatibleMultipartMode.isSelected(),HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT);
        }
        if (showImplementation) {
            element.setProperty(HTTPSamplerBase.IMPLEMENTATION, httpImplementation.getText(),"");
        }
    }

    // FIXME FACTOR WITH HTTPHC4Impl, HTTPHC3Impl
    // Just append all the parameter values, and use that as the post body
    /**
     * Compute body data from arguments
     * @param arguments {@link Arguments}
     * @return {@link String}
     */
    private static final String computePostBody(Arguments arguments) {
        return computePostBody(arguments, false);
    }

    /**
     * Compute body data from arguments
     * @param arguments {@link Arguments}
     * @param crlfToLF whether to convert CRLF to LF
     * @return {@link String}
     */
    private static final String computePostBody(Arguments arguments, boolean crlfToLF) {
        StringBuilder postBody = new StringBuilder();
        PropertyIterator args = arguments.iterator();
        while (args.hasNext()) {
            HTTPArgument arg = (HTTPArgument) args.next().getObjectValue();
            String value = arg.getValue();
            if (crlfToLF) {
                value=value.replaceAll("\r\n", "\n"); // See modifyTestElement
            }
            postBody.append(value);
        }
        return postBody.toString();
    }

    /**
     * Set the text, etc. in the UI.
     *
     * @param el
     *            contains the data to be displayed
     */
    public void configure(TestElement el) {
        setName(el.getName());
        Arguments arguments = (Arguments) el.getProperty(HTTPSamplerBase.ARGUMENTS).getObjectValue();

        boolean useRaw = el.getPropertyAsBoolean(HTTPSamplerBase.POST_BODY_RAW, HTTPSamplerBase.POST_BODY_RAW_DEFAULT);
        if(useRaw) {
            String postBody = computePostBody(arguments, true); // Convert CRLF to CR, see modifyTestElement
            postBodyContent.setInitialText(postBody); 
            postBodyContent.setCaretPosition(0);
            postContentTabbedPane.setSelectedIndex(TAB_RAW_BODY, false);
        } else {
            argsPanel.configure(arguments);
            postContentTabbedPane.setSelectedIndex(TAB_PARAMETERS, false);
        }

        domain.setText(el.getPropertyAsString(HTTPSamplerBase.DOMAIN));

        String portString = el.getPropertyAsString(HTTPSamplerBase.PORT);

        // Only display the port number if it is meaningfully specified
        if (portString.equals(HTTPSamplerBase.UNSPECIFIED_PORT_AS_STRING)) {
            port.setText(""); // $NON-NLS-1$
        } else {
            port.setText(portString);
        }
        proxyHost.setText(el.getPropertyAsString(HTTPSamplerBase.PROXYHOST));
        proxyPort.setText(el.getPropertyAsString(HTTPSamplerBase.PROXYPORT));
        proxyUser.setText(el.getPropertyAsString(HTTPSamplerBase.PROXYUSER));
        proxyPass.setText(el.getPropertyAsString(HTTPSamplerBase.PROXYPASS));
        connectTimeOut.setText(el.getPropertyAsString(HTTPSamplerBase.CONNECT_TIMEOUT));
        responseTimeOut.setText(el.getPropertyAsString(HTTPSamplerBase.RESPONSE_TIMEOUT));
        protocol.setText(el.getPropertyAsString(HTTPSamplerBase.PROTOCOL));
        contentEncoding.setText(el.getPropertyAsString(HTTPSamplerBase.CONTENT_ENCODING));
        path.setText(el.getPropertyAsString(HTTPSamplerBase.PATH));
        if (notConfigOnly){
            method.setText(el.getPropertyAsString(HTTPSamplerBase.METHOD));
            followRedirects.setSelected(el.getPropertyAsBoolean(HTTPSamplerBase.FOLLOW_REDIRECTS));
            autoRedirects.setSelected(el.getPropertyAsBoolean(HTTPSamplerBase.AUTO_REDIRECTS));
            useKeepAlive.setSelected(el.getPropertyAsBoolean(HTTPSamplerBase.USE_KEEPALIVE));
            useMultipartForPost.setSelected(el.getPropertyAsBoolean(HTTPSamplerBase.DO_MULTIPART_POST));
            useBrowserCompatibleMultipartMode.setSelected(el.getPropertyAsBoolean(
                    HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART, HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT));
        }
        if (showImplementation) {
            httpImplementation.setText(el.getPropertyAsString(HTTPSamplerBase.IMPLEMENTATION));
        }
    }

    private void init() {// called from ctor, so must not be overridable
        this.setLayout(new BorderLayout());

        // WEB REQUEST PANEL
        JPanel webRequestPanel = new JPanel();
        webRequestPanel.setLayout(new BorderLayout());
        webRequestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_request"))); // $NON-NLS-1$

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(getProtocolAndMethodPanel());
        northPanel.add(getPathPanel());

        webRequestPanel.add(northPanel, BorderLayout.NORTH);
        webRequestPanel.add(getParameterPanel(), BorderLayout.CENTER);

        this.add(getWebServerTimeoutPanel(), BorderLayout.NORTH);
        this.add(webRequestPanel, BorderLayout.CENTER);
        this.add(getProxyServerPanel(), BorderLayout.SOUTH); 
    }

    /**
     * Create a panel containing the webserver (domain+port) and timeouts (connect+request).
     *
     * @return the panel
     */
    protected final JPanel getWebServerTimeoutPanel() {
        // WEB SERVER PANEL
        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_server"))); // $NON-NLS-1$
        final JPanel domainPanel = getDomainPanel();
        final JPanel portPanel = getPortPanel();
        webServerPanel.add(domainPanel, BorderLayout.CENTER);
        webServerPanel.add(portPanel, BorderLayout.EAST);

        JPanel timeOut = new HorizontalPanel();
        timeOut.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_server_timeout_title"))); // $NON-NLS-1$
        final JPanel connPanel = getConnectTimeOutPanel();
        final JPanel reqPanel = getResponseTimeOutPanel();
        timeOut.add(connPanel);
        timeOut.add(reqPanel);

        JPanel webServerTimeoutPanel = new VerticalPanel();
        webServerTimeoutPanel.add(webServerPanel, BorderLayout.CENTER);
        webServerTimeoutPanel.add(timeOut, BorderLayout.EAST);

        JPanel bigPanel = new VerticalPanel();
        bigPanel.add(webServerTimeoutPanel);
        return bigPanel;
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

    private JPanel getPortPanel() {
        port = new JTextField(4);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_port")); // $NON-NLS-1$
        label.setLabelFor(port);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(port, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getProxyPortPanel() {
        proxyPort = new JTextField(4);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_port")); // $NON-NLS-1$
        label.setLabelFor(proxyPort);
        label.setFont(FONT_SMALL);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(proxyPort, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getConnectTimeOutPanel() {
        connectTimeOut = new JTextField(4);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_timeout_connect")); // $NON-NLS-1$
        label.setLabelFor(connectTimeOut);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(connectTimeOut, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getResponseTimeOutPanel() {
        responseTimeOut = new JTextField(4);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_timeout_response")); // $NON-NLS-1$
        label.setLabelFor(responseTimeOut);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(responseTimeOut, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getDomainPanel() {
        domain = new JTextField(20);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_domain")); // $NON-NLS-1$
        label.setLabelFor(domain);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(domain, BorderLayout.CENTER);
        return panel;
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

    /**
     * This method defines the Panel for the HTTP path, 'Follow Redirects'
     * 'Use KeepAlive', and 'Use multipart for HTTP POST' elements.
     *
     * @return JPanel The Panel for the path, 'Follow Redirects' and 'Use
     *         KeepAlive' elements.
     */
    protected Component getPathPanel() {
        path = new JTextField(15);

        JLabel label = new JLabel(JMeterUtils.getResString("path")); //$NON-NLS-1$
        label.setLabelFor(path);

        if (notConfigOnly){
            followRedirects = new JCheckBox(JMeterUtils.getResString("follow_redirects")); // $NON-NLS-1$
            followRedirects.setFont(null);
            followRedirects.setSelected(true);
            followRedirects.addChangeListener(this);

            autoRedirects = new JCheckBox(JMeterUtils.getResString("follow_redirects_auto")); //$NON-NLS-1$
            autoRedirects.setFont(null);
            autoRedirects.addChangeListener(this);
            autoRedirects.setSelected(false);// Default changed in 2.3 and again in 2.4

            useKeepAlive = new JCheckBox(JMeterUtils.getResString("use_keepalive")); // $NON-NLS-1$
            useKeepAlive.setFont(null);
            useKeepAlive.setSelected(true);

            useMultipartForPost = new JCheckBox(JMeterUtils.getResString("use_multipart_for_http_post")); // $NON-NLS-1$
            useMultipartForPost.setFont(null);
            useMultipartForPost.setSelected(false);

            useBrowserCompatibleMultipartMode = new JCheckBox(JMeterUtils.getResString("use_multipart_mode_browser")); // $NON-NLS-1$
            useBrowserCompatibleMultipartMode.setFont(null);
            useBrowserCompatibleMultipartMode.setSelected(HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT);

        }

        JPanel pathPanel = new HorizontalPanel();
        pathPanel.add(label);
        pathPanel.add(path);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(pathPanel);
        if (notConfigOnly){
            JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            optionPanel.setFont(FONT_SMALL); // all sub-components with setFont(null) inherit this font
            optionPanel.add(autoRedirects);
            optionPanel.add(followRedirects);
            optionPanel.add(useKeepAlive);
            optionPanel.add(useMultipartForPost);
            optionPanel.add(useBrowserCompatibleMultipartMode);
            optionPanel.setMinimumSize(optionPanel.getPreferredSize());
            panel.add(optionPanel);
        }

        return panel;
    }

    protected JPanel getProtocolAndMethodPanel() {

        // Implementation
        
        if (showImplementation) {
            httpImplementation = new JLabeledChoice(JMeterUtils.getResString("http_implementation"), // $NON-NLS-1$
                    HTTPSamplerFactory.getImplementations());
            httpImplementation.addValue("");
        }
        // PROTOCOL
        protocol = new JTextField(4);
        JLabel protocolLabel = new JLabel(JMeterUtils.getResString("protocol")); // $NON-NLS-1$
        protocolLabel.setLabelFor(protocol);        
        
        // CONTENT_ENCODING
        contentEncoding = new JTextField(10);
        JLabel contentEncodingLabel = new JLabel(JMeterUtils.getResString("content_encoding")); // $NON-NLS-1$
        contentEncodingLabel.setLabelFor(contentEncoding);

        if (notConfigOnly){
            method = new JLabeledChoice(JMeterUtils.getResString("method"), // $NON-NLS-1$
                    HTTPSamplerBase.getValidMethodsAsArray());
        }

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        if (showImplementation) {
            panel.add(httpImplementation);
        }
        panel.add(protocolLabel);
        panel.add(protocol);
        panel.add(Box.createHorizontalStrut(5));

        if (notConfigOnly){
            panel.add(method);
        }
        panel.setMinimumSize(panel.getPreferredSize());
        panel.add(Box.createHorizontalStrut(5));

        panel.add(contentEncodingLabel);
        panel.add(contentEncoding);
        panel.setMinimumSize(panel.getPreferredSize());
        return panel;
    }

    protected JTabbedPane getParameterPanel() {
        postContentTabbedPane = new ValidationTabbedPane();
        argsPanel = new HTTPArgumentsPanel();
        postContentTabbedPane.add(JMeterUtils.getResString("post_as_parameters"), argsPanel);// $NON-NLS-1$
        if(showRawBodyPane) {
            postBodyContent = new JSyntaxTextArea(30, 50);// $NON-NLS-1$
            postContentTabbedPane.add(JMeterUtils.getResString("post_body"), new JTextScrollPane(postBodyContent));// $NON-NLS-1$
        }
        return postContentTabbedPane;
    }

    /**
     * 
     */
    class ValidationTabbedPane extends JTabbedPane{

        /**
         * 
         */
        private static final long serialVersionUID = 7014311238367882880L;

        /* (non-Javadoc)
         * @see javax.swing.JTabbedPane#setSelectedIndex(int)
         */
        @Override
        public void setSelectedIndex(int index) {
            setSelectedIndex(index, true);
        }
        /**
         * Apply some check rules if check is true
         */
        public void setSelectedIndex(int index, boolean check) {
            int oldSelectedIndex = getSelectedIndex();
            if(!check || oldSelectedIndex==-1) {
                super.setSelectedIndex(index);
            }
            else if(index != this.getSelectedIndex())
            {
                if(noData(getSelectedIndex())) {
                    // If there is no data, then switching between Parameters and Raw should be
                    // allowed with no further user interaction.
                    argsPanel.clear();
                    postBodyContent.setInitialText("");
                    super.setSelectedIndex(index);
                }
                else { 
                    if(oldSelectedIndex == TAB_RAW_BODY) {
                        // If RAW data and Parameters match we allow switching
                        if(postBodyContent.getText().equals(computePostBody((Arguments)argsPanel.createTestElement()).trim())) {
                            super.setSelectedIndex(index);
                        }
                        else {
                            // If there is data in the Raw panel, then the user should be 
                            // prevented from switching (that would be easy to track).
                            JOptionPane.showConfirmDialog(this,
                                    JMeterUtils.getResString("web_cannot_switch_tab"), // $NON-NLS-1$
                                    JMeterUtils.getResString("warning"), // $NON-NLS-1$
                                    JOptionPane.DEFAULT_OPTION, 
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                    else {
                        // If the Parameter data can be converted (i.e. no names), we 
                        // warn the user that the Parameter data will be lost.
                        if(canConvertParameters()) {
                            Object[] options = {
                                    JMeterUtils.getResString("confirm"), // $NON-NLS-1$
                                    JMeterUtils.getResString("cancel")}; // $NON-NLS-1$
                            int n = JOptionPane.showOptionDialog(this,
                                JMeterUtils.getResString("web_parameters_lost_message"), // $NON-NLS-1$
                                JMeterUtils.getResString("warning"), // $NON-NLS-1$
                                JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.QUESTION_MESSAGE,
                                null,
                                options,
                                options[1]);
                            if(n == JOptionPane.YES_OPTION) {
                                convertParametersToRaw();
                                super.setSelectedIndex(index);
                            }
                            else{
                                return;
                            }
                        }
                        else {
                            // If the Parameter data cannot be converted to Raw, then the user should be
                            // prevented from doing so raise an error dialog
                            JOptionPane.showConfirmDialog(this,
                                    JMeterUtils.getResString("web_cannot_convert_parameters_to_raw"), // $NON-NLS-1$
                                    JMeterUtils.getResString("warning"), // $NON-NLS-1$
                                    JOptionPane.DEFAULT_OPTION, 
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
            }
        }   
    }
    // autoRedirects and followRedirects cannot both be selected
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == autoRedirects){
            if (autoRedirects.isSelected()) {
                followRedirects.setSelected(false);
            }
        }
        if (e.getSource() == followRedirects){
            if (followRedirects.isSelected()) {
                autoRedirects.setSelected(false);
            }
        }
    }


    /**
     * Convert Parameters to Raw Body
     */
    void convertParametersToRaw() {
        postBodyContent.setInitialText(computePostBody((Arguments)argsPanel.createTestElement()));
        postBodyContent.setCaretPosition(0);
    }

    /**
     * 
     * @return true if no argument has a name
     */
    boolean canConvertParameters() {
        Arguments arguments = (Arguments)argsPanel.createTestElement();
        for (int i = 0; i < arguments.getArgumentCount(); i++) {
            if(!StringUtils.isEmpty(arguments.getArgument(i).getName())) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return true if neither Parameters tab nor Raw Body tab contain data
     */
    boolean noData(int oldSelectedIndex) {
        if(oldSelectedIndex == TAB_RAW_BODY) {
            return StringUtils.isEmpty(postBodyContent.getText().trim());
        }
        else {
            Arguments element = (Arguments)argsPanel.createTestElement();
            return StringUtils.isEmpty(computePostBody(element));
        }
    }
}
