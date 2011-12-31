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

package org.apache.jmeter.protocol.java.control.gui;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.jmeter.protocol.java.sampler.BSFSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class BSFSamplerGui extends AbstractSamplerGui {
    private static final long serialVersionUID = 240L;

    private JTextArea scriptField;

    private JComboBox langField;// Language

    private JTextField filename;// script file name (if present)

    private JTextField parameters;// parameters to pass to script file (or script)

    public BSFSamplerGui() {
        init();
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        BSFSampler sampler = (BSFSampler) element;
        scriptField.setText(sampler.getScript());
        langField.setSelectedItem(sampler.getScriptLanguage());
        filename.setText(sampler.getFilename());
        parameters.setText(sampler.getParameters());
    }

    public TestElement createTestElement() {
        BSFSampler sampler = new BSFSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement te) {
        te.clear();
        this.configureTestElement(te);
        BSFSampler sampler = (BSFSampler) te;
        sampler.setFilename(filename.getText());
        sampler.setScriptLanguage((String) langField.getSelectedItem());
        sampler.setParameters(parameters.getText());
        sampler.setScript(scriptField.getText());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        scriptField.setText(""); //$NON-NLS-1$
        langField.setSelectedIndex(0);
        filename.setText(""); //$NON-NLS-1$
        parameters.setText(""); //$NON-NLS-1$
    }

    public String getLabelResource() {
        return "bsf_sampler_title"; // $NON-NLS-1$
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createLanguagePanel());
        box.add(createFilenamePanel());
        box.add(createParameterPanel());
        add(box, BorderLayout.NORTH);

        JPanel panel = createScriptPanel();
        add(panel, BorderLayout.CENTER);
        // Don't let the input field shrink too much
        add(Box.createVerticalStrut(panel.getPreferredSize().height), BorderLayout.WEST);
    }

    private JPanel createParameterPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("bsf_script_parameters")); // $NON-NLS-1$

        parameters = new JTextField(10);
        label.setLabelFor(parameters);

        JPanel parameterPanel = new JPanel(new BorderLayout(5, 0));
        parameterPanel.add(label, BorderLayout.WEST);
        parameterPanel.add(parameters, BorderLayout.CENTER);
        return parameterPanel;
    }

    private JPanel createFilenamePanel()// TODO ought to be a FileChooser ...
    {
        JLabel label = new JLabel(JMeterUtils.getResString("bsf_script_file")); // $NON-NLS-1$

        filename = new JTextField(10);
        label.setLabelFor(filename);

        JPanel filenamePanel = new JPanel(new BorderLayout(5, 0));
        filenamePanel.add(label, BorderLayout.WEST);
        filenamePanel.add(filename, BorderLayout.CENTER);
        return filenamePanel;
    }

    private JPanel createLanguagePanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("bsf_script_language")); // $NON-NLS-1$

        Properties p = JMeterUtils.loadProperties("org/apache/bsf/Languages.properties"); // $NON-NLS-1$
        // We have added Jexl in BSFSampler.
        p.put("jexl", ""); // $NON-NLS-1$
        Set<Object> keySet = p.keySet();
        // TODO - perhaps weed out ones which don't exist?
        String [] items = keySet.toArray(new String[]{});
        Arrays.sort(items);

        langField = new JComboBox(items);
        langField.setEditable(true);
        label.setLabelFor(langField);

        JPanel langPanel = new JPanel(new BorderLayout(5, 0));
        langPanel.add(label, BorderLayout.WEST);
        langPanel.add(langField, BorderLayout.CENTER);

        return langPanel;
    }

    private JPanel createScriptPanel() {
        scriptField = new JTextArea();
        scriptField.setRows(4);
        scriptField.setLineWrap(true);
        scriptField.setWrapStyleWord(true);

        JLabel label = new JLabel(JMeterUtils.getResString("bsf_script")); // $NON-NLS-1$
        label.setLabelFor(scriptField);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(scriptField), BorderLayout.CENTER);
        return panel;
    }
}
