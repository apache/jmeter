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

package org.apache.jmeter.protocol.tcp.config.gui;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.ServerPanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.tcp.sampler.TCPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

public class TCPConfigGui extends AbstractConfigGui {

    private static final long serialVersionUID = 240L;

    private ServerPanel serverPanel;
    
    private JLabeledTextField classname;

    private JCheckBox reUseConnection;

    // NOTUSED yet private JTextField filename;

    private JCheckBox setNoDelay;

    private JTextArea requestData;

    private boolean displayName = true;

    public TCPConfigGui() {
        this(true);
    }

    public TCPConfigGui(boolean displayName) {
        this.displayName = displayName;
        init();
    }

    public String getLabelResource() {
        return "tcp_config_title"; // $NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        // N.B. this will be a config element, so we cannot use the getXXX() methods
        classname.setText(element.getPropertyAsString(TCPSampler.CLASSNAME));
        serverPanel.setServer(element.getPropertyAsString(TCPSampler.SERVER));
        // Default to original behaviour, i.e. re-use connection
        reUseConnection.setSelected(element.getPropertyAsBoolean(TCPSampler.RE_USE_CONNECTION,true));
        serverPanel.setPort(element.getPropertyAsString(TCPSampler.PORT));
        // filename.setText(element.getPropertyAsString(TCPSampler.FILENAME));
        serverPanel.setResponseTimeout(element.getPropertyAsString(TCPSampler.TIMEOUT));
        serverPanel.setConnectTimeout(element.getPropertyAsString(TCPSampler.TIMEOUT_CONNECT));
        setNoDelay.setSelected(element.getPropertyAsBoolean(TCPSampler.NODELAY));
        requestData.setText(element.getPropertyAsString(TCPSampler.REQUEST));
    }

    public TestElement createTestElement() {
        ConfigTestElement element = new ConfigTestElement();
        modifyTestElement(element);
        return element;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        // N.B. this will be a config element, so we cannot use the setXXX() methods
        element.setProperty(TCPSampler.CLASSNAME, classname.getText(), "");
        element.setProperty(TCPSampler.SERVER, serverPanel.getServer());
        element.setProperty(TCPSampler.RE_USE_CONNECTION, reUseConnection.isSelected());
        element.setProperty(TCPSampler.PORT, serverPanel.getPort());
        // element.setProperty(TCPSampler.FILENAME, filename.getText());
        element.setProperty(TCPSampler.NODELAY, setNoDelay.isSelected());
        element.setProperty(TCPSampler.TIMEOUT, serverPanel.getResponseTimeout());
        element.setProperty(TCPSampler.TIMEOUT_CONNECT, serverPanel.getConnectTimeout(),"");
        element.setProperty(TCPSampler.REQUEST, requestData.getText());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        serverPanel.clear();
        classname.setText(""); //$NON-NLS-1$
        requestData.setText(""); //$NON-NLS-1$
        reUseConnection.setSelected(true);
        setNoDelay.setSelected(false);
    }


    private JPanel createNoDelayPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("tcp_nodelay")); // $NON-NLS-1$

        setNoDelay = new JCheckBox();
        label.setLabelFor(setNoDelay);

        JPanel nodelayPanel = new JPanel(new BorderLayout(5, 0));
        nodelayPanel.add(label, BorderLayout.WEST);
        nodelayPanel.add(setNoDelay, BorderLayout.CENTER);
        return nodelayPanel;
    }

    private JPanel createClosePortPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("reuseconnection")); //$NON-NLS-1$

        reUseConnection = new JCheckBox("", true);
        label.setLabelFor(reUseConnection);

        JPanel closePortPanel = new JPanel(new BorderLayout(5, 0));
        closePortPanel.add(label, BorderLayout.WEST);
        closePortPanel.add(reUseConnection, BorderLayout.CENTER);
        return closePortPanel;
    }

    private JPanel createRequestPanel() {
        JLabel reqLabel = new JLabel(JMeterUtils.getResString("tcp_request_data")); // $NON-NLS-1$
        requestData = new JTextArea(3, 0);
        requestData.setLineWrap(true);
        reqLabel.setLabelFor(requestData);

        JPanel reqDataPanel = new JPanel(new BorderLayout(5, 0));
        reqDataPanel.add(reqLabel, BorderLayout.WEST);
        reqDataPanel.add(requestData, BorderLayout.CENTER);
        return reqDataPanel;
    }

    // private JPanel createFilenamePanel()//Not used yet
    // {
    //
    // JLabel label = new JLabel(JMeterUtils.getResString("file_to_retrieve")); // $NON-NLS-1$
    //
    // filename = new JTextField(10);
    // filename.setName(FILENAME);
    // label.setLabelFor(filename);
    //
    // JPanel filenamePanel = new JPanel(new BorderLayout(5, 0));
    // filenamePanel.add(label, BorderLayout.WEST);
    // filenamePanel.add(filename, BorderLayout.CENTER);
    // return filenamePanel;
    // }

    private void init() {
        setLayout(new BorderLayout(0, 5));

        serverPanel = new ServerPanel();
        
        if (displayName) {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        VerticalPanel mainPanel = new VerticalPanel();
        classname = new JLabeledTextField(JMeterUtils.getResString("tcp_classname"));
        mainPanel.add(classname);
        mainPanel.add(serverPanel);
        
        HorizontalPanel optionsPanel = new HorizontalPanel();
        optionsPanel.add(createClosePortPanel());
        optionsPanel.add(createNoDelayPanel());
        mainPanel.add(optionsPanel);
        mainPanel.add(createRequestPanel());

        // mainPanel.add(createFilenamePanel());
        add(mainPanel, BorderLayout.CENTER);
    }
}
