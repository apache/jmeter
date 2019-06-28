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
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.gui.HTTPFileArgsPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * Basic URL / HTTP Request configuration:
 * <ul>
 * <li>host and port</li>
 * <li>connect and response timeouts</li>
 * <li>path, method, encoding, parameters</li>
 * <li>redirects and keepalive</li>
 * </ul>
 */
public class UrlConfigGui extends JPanel implements ChangeListener {

    private static final long serialVersionUID = 240L;

    private static final int TAB_PARAMETERS = 0;

    private int tabRawBodyIndex = 1;

    private int tabFileUploadIndex = 2;

    private static final Font FONT_DEFAULT = UIManager.getDefaults().getFont("TextField.font");

    private static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, (int) Math.round(FONT_DEFAULT.getSize() * 0.8));

    private HTTPArgumentsPanel argsPanel;

    private HTTPFileArgsPanel filesPanel;

    private JLabeledTextField domain;

    private JLabeledTextField port;

    private JLabeledTextField protocol;

    private JLabeledTextField contentEncoding;

    private JLabeledTextField path;

    private JCheckBox followRedirects;

    private JCheckBox autoRedirects;

    private JCheckBox useKeepAlive;

    private JCheckBox useMultipart;

    private JCheckBox useBrowserCompatibleMultipartMode;

    private JLabeledChoice method;

    // set this false to suppress some items for use in HTTP Request defaults
    private final boolean notConfigOnly;

    // Body data
    private JSyntaxTextArea postBodyContent;

    // Tabbed pane that contains parameters and raw body
    private ValidationTabbedPane postContentTabbedPane;

    private boolean showRawBodyPane;
    private boolean showFileUploadPane;

    /**
     * Constructor which is setup to show HTTP implementation, raw body pane and
     * sampler fields.
     */
    public UrlConfigGui() {
        this(true);
    }

    /**
     * Constructor which is setup to show HTTP implementation and raw body pane.
     *
     * @param showSamplerFields
     *            flag whether sampler fields should be shown.
     */
    public UrlConfigGui(boolean showSamplerFields) {
        this(showSamplerFields, true);
    }

    /**
     * @param showSamplerFields
     *            flag whether sampler fields should be shown
     * @param showRawBodyPane
     *            flag whether the raw body pane should be shown
     */
    public UrlConfigGui(boolean showSamplerFields, boolean showRawBodyPane) {
        this(showSamplerFields, showRawBodyPane, false);
    }

    /**
     * @param showSamplerFields
     *            flag whether sampler fields should be shown
     * @param showRawBodyPane
     *            flag whether the raw body pane should be shown
     * @param showFileUploadPane flag whether the file upload pane should be shown
     */
    public UrlConfigGui(boolean showSamplerFields, boolean showRawBodyPane, boolean showFileUploadPane) {
        this.notConfigOnly = showSamplerFields;
        this.showRawBodyPane = showRawBodyPane;
        this.showFileUploadPane = showFileUploadPane;
        init();
    }

    public void clear() {
        domain.setText(""); // $NON-NLS-1$
        if (notConfigOnly){
            followRedirects.setSelected(true);
            autoRedirects.setSelected(false);
            method.setText(HTTPSamplerBase.DEFAULT_METHOD);
            useKeepAlive.setSelected(true);
            useMultipart.setSelected(false);
            useBrowserCompatibleMultipartMode.setSelected(HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT);
        }
        path.setText(""); // $NON-NLS-1$
        port.setText(""); // $NON-NLS-1$
        protocol.setText(""); // $NON-NLS-1$
        contentEncoding.setText(""); // $NON-NLS-1$
        argsPanel.clear();
        if(showFileUploadPane) {
            filesPanel.clear();
        }
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
     * @param element {@link TestElement} to modify
     */
    public void modifyTestElement(TestElement element) {
        boolean useRaw = !postBodyContent.getText().isEmpty();
        Arguments args;
        if(useRaw) {
            args = new Arguments();
            String text = postBodyContent.getText();
            /*
             * Textfield uses \n (LF) to delimit lines; we need to send CRLF.
             * Rather than change the way that arguments are processed by the
             * samplers for raw data, it is easier to fix the data.
             * On retrieval, CRLF is converted back to LF for storage in the text field.
             * See
             */
            HTTPArgument arg = new HTTPArgument("", text.replaceAll("\n","\r\n"), false);
            arg.setAlwaysEncoded(false);
            args.addArgument(arg);
        } else {
            args = (Arguments) argsPanel.createTestElement();
            HTTPArgument.convertArgumentsToHTTP(args);
        }
        if(showFileUploadPane) {
            filesPanel.modifyTestElement(element);
        }
        element.setProperty(HTTPSamplerBase.POST_BODY_RAW, useRaw, HTTPSamplerBase.POST_BODY_RAW_DEFAULT);
        element.setProperty(new TestElementProperty(HTTPSamplerBase.ARGUMENTS, args));
        element.setProperty(HTTPSamplerBase.DOMAIN, domain.getText());
        element.setProperty(HTTPSamplerBase.PORT, port.getText());
        element.setProperty(HTTPSamplerBase.PROTOCOL, protocol.getText());
        element.setProperty(HTTPSamplerBase.CONTENT_ENCODING, contentEncoding.getText());
        element.setProperty(HTTPSamplerBase.PATH, path.getText());
        if (notConfigOnly){
            element.setProperty(HTTPSamplerBase.METHOD, method.getText());
            element.setProperty(new BooleanProperty(HTTPSamplerBase.FOLLOW_REDIRECTS, followRedirects.isSelected()));
            element.setProperty(new BooleanProperty(HTTPSamplerBase.AUTO_REDIRECTS, autoRedirects.isSelected()));
            element.setProperty(new BooleanProperty(HTTPSamplerBase.USE_KEEPALIVE, useKeepAlive.isSelected()));
            element.setProperty(new BooleanProperty(HTTPSamplerBase.DO_MULTIPART_POST, useMultipart.isSelected()));
            element.setProperty(HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART,
                    useBrowserCompatibleMultipartMode.isSelected(),
                    HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT);
        }
    }

    // Just append all the parameter values, and use that as the post body
    /**
     * Compute body data from arguments
     * @param arguments {@link Arguments}
     * @return {@link String}
     */
    private static String computePostBody(Arguments arguments) {
        return computePostBody(arguments, false);
    }

    /**
     * Compute body data from arguments
     * @param arguments {@link Arguments}
     * @param crlfToLF whether to convert CRLF to LF
     * @return {@link String}
     */
    private static String computePostBody(Arguments arguments, boolean crlfToLF) {
        StringBuilder postBody = new StringBuilder();
        for (JMeterProperty argument : arguments) {
            HTTPArgument arg = (HTTPArgument) argument.getObjectValue();
            String value = arg.getValue();
            if (crlfToLF) {
                value = value.replaceAll("\r\n", "\n"); // See modifyTestElement
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
            argsPanel.clear();
            postContentTabbedPane.setSelectedIndex(tabRawBodyIndex, false);
        } else {
            postBodyContent.setInitialText("");
            argsPanel.configure(arguments);
            postContentTabbedPane.setSelectedIndex(TAB_PARAMETERS, false);
        }
        if(showFileUploadPane) {
            filesPanel.configure(el);
        }

        domain.setText(el.getPropertyAsString(HTTPSamplerBase.DOMAIN));

        String portString = el.getPropertyAsString(HTTPSamplerBase.PORT);

        // Only display the port number if it is meaningfully specified
        if (portString.equals(HTTPSamplerBase.UNSPECIFIED_PORT_AS_STRING)) {
            port.setText(""); // $NON-NLS-1$
        } else {
            port.setText(portString);
        }
        protocol.setText(el.getPropertyAsString(HTTPSamplerBase.PROTOCOL));
        contentEncoding.setText(el.getPropertyAsString(HTTPSamplerBase.CONTENT_ENCODING));
        path.setText(el.getPropertyAsString(HTTPSamplerBase.PATH));
        if (notConfigOnly){
            method.setText(el.getPropertyAsString(HTTPSamplerBase.METHOD));
            followRedirects.setSelected(el.getPropertyAsBoolean(HTTPSamplerBase.FOLLOW_REDIRECTS));
            autoRedirects.setSelected(el.getPropertyAsBoolean(HTTPSamplerBase.AUTO_REDIRECTS));
            useKeepAlive.setSelected(el.getPropertyAsBoolean(HTTPSamplerBase.USE_KEEPALIVE));
            useMultipart.setSelected(el.getPropertyAsBoolean(HTTPSamplerBase.DO_MULTIPART_POST));
            useBrowserCompatibleMultipartMode.setSelected(el.getPropertyAsBoolean(
                    HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART, HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT));
        }
    }

    private void init() {// called from ctor, so must not be overridable
        this.setLayout(new BorderLayout());

        // WEB REQUEST PANEL
        JPanel webRequestPanel = new JPanel();
        webRequestPanel.setLayout(new BorderLayout());
        webRequestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_request"))); // $NON-NLS-1$

        webRequestPanel.add(getPathPanel(), BorderLayout.NORTH);
        webRequestPanel.add(getParameterPanel(), BorderLayout.CENTER);

        this.add(getWebServerPanel(), BorderLayout.NORTH);
        this.add(webRequestPanel, BorderLayout.CENTER);
    }

    /**
     * Create a panel containing the webserver (domain+port) and scheme.
     *
     * @return the panel
     */
    protected final JPanel getWebServerPanel() {
        // PROTOCOL
        protocol = new JLabeledTextField(JMeterUtils.getResString("protocol"), 4); // $NON-NLS-1$
        port = new JLabeledTextField(JMeterUtils.getResString("web_server_port"), 7); // $NON-NLS-1$
        domain = new JLabeledTextField(JMeterUtils.getResString("web_server_domain"), 40); // $NON-NLS-1$

        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_server"))); // $NON-NLS-1$
        webServerPanel.add(protocol);
        webServerPanel.add(domain);
        webServerPanel.add(port);
        return webServerPanel;
    }


    /**
     * This method defines the Panel for:
     *  the HTTP path, Method and Content Encoding
     *  'Follow Redirects', 'Use KeepAlive', and 'Use multipart for HTTP POST' elements.
     *
     * @return JPanel The Panel for the path, 'Follow Redirects' and 'Use
     *         KeepAlive' elements.
     */
    protected Component getPathPanel() {
        path = new JLabeledTextField(JMeterUtils.getResString("path"), 80); //$NON-NLS-1$
        // CONTENT_ENCODING
        contentEncoding = new JLabeledTextField(JMeterUtils.getResString("content_encoding"), 7); // $NON-NLS-1$

        if (notConfigOnly){
            method = new JLabeledChoice(JMeterUtils.getResString("method"), // $NON-NLS-1$
                    HTTPSamplerBase.getValidMethodsAsArray(), true, false);
            method.addChangeListener(this);
        }

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

            useMultipart = new JCheckBox(JMeterUtils.getResString("use_multipart_for_http_post")); // $NON-NLS-1$
            useMultipart.setFont(null);
            useMultipart.setSelected(false);

            useBrowserCompatibleMultipartMode = new JCheckBox(JMeterUtils.getResString("use_multipart_mode_browser")); // $NON-NLS-1$
            useBrowserCompatibleMultipartMode.setFont(null);
            useBrowserCompatibleMultipartMode.setSelected(HTTPSamplerBase.BROWSER_COMPATIBLE_MULTIPART_MODE_DEFAULT);

        }

        JPanel pathPanel =  new HorizontalPanel();
        if (notConfigOnly){
            pathPanel.add(method);
        }
        pathPanel.add(path);
        pathPanel.add(contentEncoding);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(pathPanel);
        if (notConfigOnly){
            JPanel optionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            optionPanel.setFont(FONT_SMALL); // all sub-components with setFont(null) inherit this font
            optionPanel.add(autoRedirects);
            optionPanel.add(followRedirects);
            optionPanel.add(useKeepAlive);
            optionPanel.add(useMultipart);
            optionPanel.add(useBrowserCompatibleMultipartMode);
            optionPanel.setMinimumSize(optionPanel.getPreferredSize());
            panel.add(optionPanel);
        }

        return panel;
    }

    protected JTabbedPane getParameterPanel() {
        postContentTabbedPane = new ValidationTabbedPane();
        argsPanel = new HTTPArgumentsPanel();
        postContentTabbedPane.add(JMeterUtils.getResString("post_as_parameters"), argsPanel);// $NON-NLS-1$

        int indx = TAB_PARAMETERS;
        if(showRawBodyPane) {
            tabRawBodyIndex = ++indx;
            postBodyContent = JSyntaxTextArea.getInstance(30, 50);// $NON-NLS-1$
            postContentTabbedPane.add(JMeterUtils.getResString("post_body"), JTextScrollPane.getInstance(postBodyContent));// $NON-NLS-1$
        }

        if(showFileUploadPane) {
            tabFileUploadIndex = ++indx;
            filesPanel = new HTTPFileArgsPanel();
            postContentTabbedPane.add(JMeterUtils.getResString("post_files_upload"), filesPanel);
        }
        return postContentTabbedPane;
    }

    /**
     *
     */
    class ValidationTabbedPane extends JTabbedPane {

        /**
         *
         */
        private static final long serialVersionUID = 7014311238367882880L;


        @Override
        public void setSelectedIndex(int index) {
            setSelectedIndex(index, true);
        }

        /**
         * Apply some check rules if check is true
         *
         * @param index
         *            index to select
         * @param check
         *            flag whether to perform checks before setting the selected
         *            index
         */
        public void setSelectedIndex(int index, boolean check) {
            int oldSelectedIndex = this.getSelectedIndex();
            if(!check || oldSelectedIndex == -1) {
                super.setSelectedIndex(index);
            } else if(index == tabFileUploadIndex) { // We're going to File, no problem
                super.setSelectedIndex(index);
            }
            // We're moving to Raw or Parameters
            else if(index != oldSelectedIndex) {
                // If the Parameter data can be converted (i.e. no names)
                // we switch
                if(index == tabRawBodyIndex) {
                    if(canSwitchToRawBodyPane()) {
                        convertParametersToRaw();
                        super.setSelectedIndex(index);
                    } else {
                        super.setSelectedIndex(TAB_PARAMETERS);
                    }
                }
                else {
                    // If the Parameter data cannot be converted to Raw, then the user should be
                    // prevented from doing so raise an error dialog
                    if(canSwitchToParametersTab()) {
                        super.setSelectedIndex(index);
                    } else {
                        super.setSelectedIndex(tabRawBodyIndex);
                    }
                }
            }
        }

        /**
         * @return false if one argument has a name
         */
        private boolean canSwitchToRawBodyPane() {
            Arguments arguments = (Arguments) argsPanel.createTestElement();
            for (int i = 0; i < arguments.getArgumentCount(); i++) {
                if(!StringUtils.isEmpty(arguments.getArgument(i).getName())) {
                    return false;
                }
            }
            return true;
        }

        /**
         * @return true if postBodyContent is empty
         */
        private boolean canSwitchToParametersTab() {
            return postBodyContent.getText().isEmpty();
        }
    }

    // autoRedirects and followRedirects cannot both be selected
    @Override
    public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
        if (source == autoRedirects && autoRedirects.isSelected()) {
            followRedirects.setSelected(false);
        }
        else if (source == followRedirects && followRedirects.isSelected()) {
            autoRedirects.setSelected(false);
        }
    }


    /**
     * Convert Parameters to Raw Body
     */
    void convertParametersToRaw() {
        if(postBodyContent.getText().isEmpty()) {
            postBodyContent.setInitialText(computePostBody((Arguments)argsPanel.createTestElement()));
            postBodyContent.setCaretPosition(0);
        }
    }
}
