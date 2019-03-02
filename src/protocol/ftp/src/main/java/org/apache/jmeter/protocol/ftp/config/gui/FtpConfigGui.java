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

package org.apache.jmeter.protocol.ftp.config.gui;

import java.awt.BorderLayout;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.ftp.sampler.FTPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class FtpConfigGui extends AbstractConfigGui {

    private static final long serialVersionUID = 240L;

    private JTextField server;

    private JTextField port;

    private JTextField remoteFile;

    private JTextField localFile;

    private JTextArea inputData;

    private JCheckBox binaryMode;

    private JCheckBox saveResponseData;

    private boolean displayName = true;

    private JRadioButton getBox;

    private JRadioButton putBox;

    public FtpConfigGui() {
        this(true);
    }

    public FtpConfigGui(boolean displayName) {
        this.displayName = displayName;
        init();
    }

    @Override
    public String getLabelResource() {
        return "ftp_sample_title"; // $NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element); // TODO - should this be done for embedded usage?
        // Note: the element is a ConfigTestElement when used standalone, so we cannot use FTPSampler access methods
        server.setText(element.getPropertyAsString(FTPSampler.SERVER));
        port.setText(element.getPropertyAsString(FTPSampler.PORT));
        remoteFile.setText(element.getPropertyAsString(FTPSampler.REMOTE_FILENAME));
        localFile.setText(element.getPropertyAsString(FTPSampler.LOCAL_FILENAME));
        inputData.setText(element.getPropertyAsString(FTPSampler.INPUT_DATA));
        binaryMode.setSelected(element.getPropertyAsBoolean(FTPSampler.BINARY_MODE, false));
        saveResponseData.setSelected(element.getPropertyAsBoolean(FTPSampler.SAVE_RESPONSE, false));
        final boolean uploading = element.getPropertyAsBoolean(FTPSampler.UPLOAD_FILE,false);
        if (uploading){
            putBox.setSelected(true);
        } else {
            getBox.setSelected(true);
        }
    }

    @Override
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
    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        // Note: the element is a ConfigTestElement, so cannot use FTPSampler access methods
        element.setProperty(FTPSampler.SERVER,server.getText());
        element.setProperty(FTPSampler.PORT,port.getText());
        element.setProperty(FTPSampler.REMOTE_FILENAME,remoteFile.getText());
        element.setProperty(FTPSampler.LOCAL_FILENAME,localFile.getText());
        element.setProperty(FTPSampler.INPUT_DATA,inputData.getText());
        element.setProperty(FTPSampler.BINARY_MODE,binaryMode.isSelected());
        element.setProperty(FTPSampler.SAVE_RESPONSE, saveResponseData.isSelected());
        element.setProperty(FTPSampler.UPLOAD_FILE,putBox.isSelected());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        server.setText(""); //$NON-NLS-1$
        port.setText(""); //$NON-NLS-1$
        remoteFile.setText(""); //$NON-NLS-1$
        localFile.setText(""); //$NON-NLS-1$
        inputData.setText(""); //$NON-NLS-1$
        binaryMode.setSelected(false);
        saveResponseData.setSelected(false);
        getBox.setSelected(true);
        putBox.setSelected(false);
    }

    private JPanel createServerPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("server")); //$NON-NLS-1$

        server = new JTextField(10);
        label.setLabelFor(server);

        JPanel serverPanel = new JPanel(new BorderLayout(5, 0));
        serverPanel.add(label, BorderLayout.WEST);
        serverPanel.add(server, BorderLayout.CENTER);
        return serverPanel;
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

    private JPanel createLocalFilenamePanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("ftp_local_file")); //$NON-NLS-1$

        localFile = new JTextField(10);
        label.setLabelFor(localFile);

        JPanel filenamePanel = new JPanel(new BorderLayout(5, 0));
        filenamePanel.add(label, BorderLayout.WEST);
        filenamePanel.add(localFile, BorderLayout.CENTER);
        return filenamePanel;
    }

    private JPanel createLocalFileContentsPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("ftp_local_file_contents")); //$NON-NLS-1$

        inputData = new JTextArea();
        label.setLabelFor(inputData);

        JPanel contentsPanel = new JPanel(new BorderLayout(5, 0));
        contentsPanel.add(label, BorderLayout.WEST);
        contentsPanel.add(inputData, BorderLayout.CENTER);
        return contentsPanel;
    }

    private JPanel createRemoteFilenamePanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("ftp_remote_file")); //$NON-NLS-1$

        remoteFile = new JTextField(10);
        label.setLabelFor(remoteFile);

        JPanel filenamePanel = new JPanel(new BorderLayout(5, 0));
        filenamePanel.add(label, BorderLayout.WEST);
        filenamePanel.add(remoteFile, BorderLayout.CENTER);
        return filenamePanel;
    }

    private JPanel createOptionsPanel(){

        ButtonGroup group = new ButtonGroup();

        getBox = new JRadioButton(JMeterUtils.getResString("ftp_get")); //$NON-NLS-1$
        group.add(getBox);
        getBox.setSelected(true);

        putBox = new JRadioButton(JMeterUtils.getResString("ftp_put")); //$NON-NLS-1$
        group.add(putBox);

        binaryMode = new JCheckBox(JMeterUtils.getResString("ftp_binary_mode")); //$NON-NLS-1$
        saveResponseData = new JCheckBox(JMeterUtils.getResString("ftp_save_response_data")); //$NON-NLS-1$


        JPanel optionsPanel = new HorizontalPanel();
        optionsPanel.add(getBox);
        optionsPanel.add(putBox);
        optionsPanel.add(binaryMode);
        optionsPanel.add(saveResponseData);
        return optionsPanel;
    }
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));

        if (displayName) {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        // MAIN PANEL
        VerticalPanel mainPanel = new VerticalPanel();
        JPanel serverPanel = new HorizontalPanel();
        serverPanel.add(createServerPanel(), BorderLayout.CENTER);
        serverPanel.add(getPortPanel(), BorderLayout.EAST);
        mainPanel.add(serverPanel);
        mainPanel.add(createRemoteFilenamePanel());
        mainPanel.add(createLocalFilenamePanel());
        mainPanel.add(createLocalFileContentsPanel());
        mainPanel.add(createOptionsPanel());

        add(mainPanel, BorderLayout.CENTER);
    }
}
