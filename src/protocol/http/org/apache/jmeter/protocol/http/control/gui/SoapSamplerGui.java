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
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.protocol.http.sampler.SoapSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;

public class SoapSamplerGui extends AbstractSamplerGui {
    private static final long serialVersionUID = 240L;

    private JLabeledTextField urlField;
    private JLabeledTextField soapAction;
    private JCheckBox sendSoapAction;
    private JCheckBox useKeepAlive;
    private JLabeledTextArea soapXml;

    private FilePanel soapXmlFile = new FilePanel();

    public SoapSamplerGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return "soap_sampler_title"; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        SoapSampler sampler = new SoapSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement s) {
        this.configureTestElement(s);
        if (s instanceof SoapSampler) {
            SoapSampler sampler = (SoapSampler) s;
            sampler.setURLData(urlField.getText());
            sampler.setXmlData(soapXml.getText());
            sampler.setXmlFile(soapXmlFile.getFilename());
            sampler.setSOAPAction(soapAction.getText());
            sampler.setSendSOAPAction(sendSoapAction.isSelected());
            sampler.setUseKeepAlive(useKeepAlive.isSelected());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        urlField.setText(""); //$NON-NLS-1$
        soapAction.setText(""); //$NON-NLS-1$
        soapXml.setText(""); //$NON-NLS-1$
        sendSoapAction.setSelected(true);
        soapXmlFile.setFilename(""); //$NON-NLS-1$
        useKeepAlive.setSelected(false);
    }

    private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        urlField = new JLabeledTextField(JMeterUtils.getResString("url"), 10); //$NON-NLS-1$
        soapXml = new JLabeledTextArea(JMeterUtils.getResString("soap_data_title")); //$NON-NLS-1$
        soapAction = new JLabeledTextField("", 10); //$NON-NLS-1$
        sendSoapAction = new JCheckBox(JMeterUtils.getResString("soap_send_action"), true); //$NON-NLS-1$
        useKeepAlive = new JCheckBox(JMeterUtils.getResString("use_keepalive")); // $NON-NLS-1$

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel soapActionPanel = new JPanel();
        soapActionPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        soapActionPanel.add(urlField, c);
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.gridy = 1;
        c.weightx = 0;
        soapActionPanel.add(sendSoapAction, c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        soapActionPanel.add(soapAction, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.gridy = 2;
        c.gridx = 0;
        soapActionPanel.add(useKeepAlive, c);

        mainPanel.add(soapActionPanel, BorderLayout.NORTH);
        mainPanel.add(soapXml, BorderLayout.CENTER);
        mainPanel.add(soapXmlFile, BorderLayout.SOUTH);

        sendSoapAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                soapAction.setEnabled(sendSoapAction.isSelected());
            }
            });

        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        SoapSampler sampler = (SoapSampler) el;
        urlField.setText(sampler.getURLData());
        sendSoapAction.setSelected(sampler.getSendSOAPAction());
        soapAction.setText(sampler.getSOAPAction());
        soapXml.setText(sampler.getXmlData());
        soapXmlFile.setFilename(sampler.getXmlFile());
        useKeepAlive.setSelected(sampler.getUseKeepAlive());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
}
