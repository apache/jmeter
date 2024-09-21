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

package org.apache.jmeter.protocol.http.config.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.BindingGroup;
import org.apache.jmeter.gui.JBooleanPropertyEditor;
import org.apache.jmeter.gui.JCheckBoxBinding;
import org.apache.jmeter.gui.JLabeledFieldBinding;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.gui.HTTPFileArgsPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBaseSchema;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JFactory;
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

    /**
     * Default value settings for URL Configuration GUI elements.
     */
    private static final UrlConfigDefaults URL_CONFIG_DEFAULTS = new UrlConfigDefaults();

    private static final int TAB_PARAMETERS = 0;

    private int tabRawBodyIndex = 1;

    private int tabFileUploadIndex = 2;

    private HTTPArgumentsPanel argsPanel;

    private HTTPFileArgsPanel filesPanel;

    private JLabeledTextField domain;

    private JLabeledTextField port;

    private JLabeledTextField protocol;

    private JLabeledTextField contentEncoding;

    private JLabeledTextField path;

    private JCheckBox followRedirects;

    private JCheckBox autoRedirects;

    private JBooleanPropertyEditor useKeepAlive;

    private JBooleanPropertyEditor useMultipart;

    private JBooleanPropertyEditor useBrowserCompatibleMultipartMode;

    private JLabeledChoice method;

    // set this false to suppress some items for use in HTTP Request defaults
    private final boolean notConfigOnly;

    // Body data
    private JSyntaxTextArea postBodyContent;

    // Tabbed pane that contains parameters and raw body
    private AbstractValidationTabbedPane postContentTabbedPane;

    private final boolean showRawBodyPane;
    private final boolean showFileUploadPane;

    private final BindingGroup bindingGroup;

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
        HTTPSamplerBaseSchema schema = HTTPSamplerBaseSchema.INSTANCE;
        bindingGroup = new BindingGroup(
                Arrays.asList(
                        new JLabeledFieldBinding(domain, schema.getDomain()),
                        new JLabeledFieldBinding(port, schema.getPort()),
                        new JLabeledFieldBinding(protocol, schema.getProtocol()),
                        new JLabeledFieldBinding(contentEncoding, schema.getContentEncoding()),
                        new JLabeledFieldBinding(path, schema.getPath())
                )
        );
        if (notConfigOnly) {
            bindingGroup.addAll(
                    Arrays.asList(
                            new JCheckBoxBinding(followRedirects, schema.getFollowRedirects()),
                            new JCheckBoxBinding(autoRedirects, schema.getAutoRedirects()),
                            new JLabeledFieldBinding(method, schema.getMethod()),
                            useKeepAlive,
                            useMultipart,
                            useBrowserCompatibleMultipartMode
                    )
            );
        }
    }

    public void clear() {
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
        bindingGroup.updateElement(element);
        boolean useRaw = showRawBodyPane && !postBodyContent.getText().isEmpty();
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
            args = argsPanel.createTestElement();
            HTTPArgument.convertArgumentsToHTTP(args);
        }
        if(showFileUploadPane) {
            filesPanel.modifyTestElement(element);
        }
        HTTPSamplerBaseSchema httpSchema = HTTPSamplerBaseSchema.INSTANCE;
        // Treat "unset" checkbox as "property removal" for HTTP Request Defaults component
        // Regular sampler should save both true and false values
        element.set(httpSchema.getPostBodyRaw(), useRaw ? Boolean.TRUE : (notConfigOnly ? false : null));
        element.set(httpSchema.getArguments(), args);
    }

    public void assignDefaultValues(TestElement element) {
        HTTPSamplerBase httpSampler = (HTTPSamplerBase) element;
        httpSampler.setPostBodyRaw(false);
        httpSampler.setArguments(argsPanel.createTestElement());
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
        bindingGroup.updateUi(el);
        HTTPSamplerBaseSchema httpSchema = HTTPSamplerBaseSchema.INSTANCE;
        Arguments arguments = el.get(httpSchema.getArguments());

        if (showRawBodyPane) {
            boolean useRaw = el.get(httpSchema.getPostBodyRaw());
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
        }

        if(showFileUploadPane) {
            filesPanel.configure(el);
        }
    }

    private void init() {// called from ctor, so must not be overridable
        this.setLayout(new BorderLayout());

        // WEB REQUEST PANEL
        JPanel webRequestPanel = new JPanel();
        webRequestPanel.setLayout(new BorderLayout());
        webRequestPanel.setBorder(BorderFactory.createTitledBorder(
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
        webServerPanel.setBorder(BorderFactory.createTitledBorder(
                JMeterUtils.getResString("web_server"))); // $NON-NLS-1$
        webServerPanel.add(protocol);
        webServerPanel.add(domain);
        webServerPanel.add(port);
        return webServerPanel;
    }

    /**
     * Return the {@link UrlConfigDefaults} instance to be used when configuring the UI elements and default values.
     * @return the {@link UrlConfigDefaults} instance to be used when configuring the UI elements and default values
     */
    protected UrlConfigDefaults getUrlConfigDefaults() {
        return URL_CONFIG_DEFAULTS;
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
                    getUrlConfigDefaults().getValidMethods(), true, false);
            method.addChangeListener(this);
        }

        if (notConfigOnly){
            followRedirects = new JCheckBox(JMeterUtils.getResString("follow_redirects")); // $NON-NLS-1$
            JFactory.small(followRedirects);
            followRedirects.addChangeListener(this);
            followRedirects.setVisible(getUrlConfigDefaults().isFollowRedirectsVisible());

            autoRedirects = new JCheckBox(JMeterUtils.getResString("follow_redirects_auto")); //$NON-NLS-1$
            JFactory.small(autoRedirects);
            autoRedirects.addChangeListener(this);
            autoRedirects.setVisible(getUrlConfigDefaults().isAutoRedirectsVisible());

            useKeepAlive = new JBooleanPropertyEditor(
                    HTTPSamplerBaseSchema.INSTANCE.getUseKeepalive(),
                    JMeterUtils.getResString("use_keepalive"));
            JFactory.small(useKeepAlive);
            useKeepAlive.setVisible(getUrlConfigDefaults().isUseKeepAliveVisible());

            useMultipart = new JBooleanPropertyEditor(
                    HTTPSamplerBaseSchema.INSTANCE.getUseMultipartPost(),
                    JMeterUtils.getResString("use_multipart_for_http_post")); // $NON-NLS-1$
            JFactory.small(useMultipart);
            useMultipart.setVisible(getUrlConfigDefaults().isUseMultipartVisible());

            useBrowserCompatibleMultipartMode = new JBooleanPropertyEditor(
                    HTTPSamplerBaseSchema.INSTANCE.getUseBrowserCompatibleMultipart(),
                    JMeterUtils.getResString("use_multipart_mode_browser")); // $NON-NLS-1$
            JFactory.small(useBrowserCompatibleMultipartMode);
            useBrowserCompatibleMultipartMode.setVisible(getUrlConfigDefaults().isUseBrowserCompatibleMultipartModeVisible());
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
     * Create a new {@link Arguments} instance associated with the specific GUI used in this component.
     * @return a new {@link Arguments} instance associated with the specific GUI used in this component
     */
    protected Arguments createHTTPArgumentsTestElement() {
        return argsPanel.createTestElement();
    }

    class ValidationTabbedPane extends AbstractValidationTabbedPane {

        private static final long serialVersionUID = 7014311238367882881L;

        @Override
        protected int getValidatedTabIndex(int currentTabIndex, int newTabIndex) {
            if (newTabIndex == tabFileUploadIndex) { // We're going to File, no problem
                return newTabIndex;
            }

            // We're moving to Raw or Parameters
            if (newTabIndex != currentTabIndex) {
                // If the Parameter data can be converted (i.e. no names)
                // we switch
                if (newTabIndex == tabRawBodyIndex) {
                    if (canSwitchToRawBodyPane()) {
                        convertParametersToRaw();
                        return newTabIndex;
                    } else {
                        return TAB_PARAMETERS;
                    }
                }
                else {
                    // If the Parameter data cannot be converted to Raw, then the user should be
                    // prevented from doing so raise an error dialog
                    if (canSwitchToParametersTab()) {
                        return newTabIndex;
                    } else {
                        return tabRawBodyIndex;
                    }
                }
            }

            return newTabIndex;
        }

        /**
         * @return false if one argument has a name
         */
        private boolean canSwitchToRawBodyPane() {
            Arguments arguments = argsPanel.createTestElement();
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
            return showRawBodyPane && postBodyContent.getText().isEmpty();
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
        if (showRawBodyPane && postBodyContent.getText().isEmpty()) {
            postBodyContent.setInitialText(computePostBody(argsPanel.createTestElement()));
            postBodyContent.setCaretPosition(0);
        }
    }
}
