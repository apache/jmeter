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

package org.apache.jmeter.protocol.java.control.gui;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.jmeter.gui.JBooleanPropertyEditor;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.FilePanelEntry;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.protocol.java.sampler.BeanShellSampler;
import org.apache.jmeter.protocol.java.sampler.BeanShellSamplerSchema;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

@TestElementMetadata(labelResource = "bsh_sampler_title")
public class BeanShellSamplerGui extends AbstractSamplerGui {

    private static final long serialVersionUID = 240L;

    // reset the bsh.Interpreter before each execution
    private final JBooleanPropertyEditor resetInterpreter =
            new JBooleanPropertyEditor(
                    BeanShellSamplerSchema.INSTANCE.getResetInterpreter(),
                    JMeterUtils.getResString("bsh_script_reset_interpreter"));

    private final FilePanelEntry filename = new FilePanelEntry(JMeterUtils.getResString("bsh_script_file"),".bsh"); // script file name (if present)

    private JTextField parameters;// parameters to pass to script file (or script)

    private JSyntaxTextArea scriptField;// script area

    public BeanShellSamplerGui() {
        init();
    }

    @Override
    public void configure(TestElement element) {
        scriptField.setInitialText(element.get(BeanShellSamplerSchema.INSTANCE.getScript()));
        scriptField.setCaretPosition(0);
        filename.setFilename(element.get(BeanShellSamplerSchema.INSTANCE.getFilename()));
        parameters.setText(element.get(BeanShellSamplerSchema.INSTANCE.getParameters()));
        resetInterpreter.updateUi(element);
        super.configure(element);
    }

    @Override
    public TestElement createTestElement() {
        BeanShellSampler sampler = new BeanShellSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement te) {
        te.clear();
        super.configureTestElement(te);
        te.set(BeanShellSamplerSchema.INSTANCE.getScript(), scriptField.getText());
        te.set(BeanShellSamplerSchema.INSTANCE.getFilename(), filename.getFilename());
        te.set(BeanShellSamplerSchema.INSTANCE.getParameters(), parameters.getText());
        resetInterpreter.updateElement(te);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        filename.setFilename(""); //$NON-NLS-1$
        parameters.setText(""); //$NON-NLS-1$
        scriptField.setInitialText(""); //$NON-NLS-1$
        resetInterpreter.reset();
    }

    @Override
    public String getLabelResource() {
        return "bsh_sampler_title"; // $NON-NLS-1$
    }

    private JPanel createFilenamePanel()
    {
        JPanel filenamePanel = new JPanel(new BorderLayout());
        filenamePanel.add(filename, BorderLayout.CENTER);

        return filenamePanel;
    }

    private JPanel createParameterPanel() {
        JLabel label = new JLabel(JMeterUtils.getResString("bsh_script_parameters")); // $NON-NLS-1$

        parameters = new JTextField(10);
        parameters.setName(BeanShellSamplerSchema.INSTANCE.getParameters().getName());
        label.setLabelFor(parameters);

        JPanel parameterPanel = new JPanel(new BorderLayout(5, 0));
        parameterPanel.add(label, BorderLayout.WEST);
        parameterPanel.add(parameters, BorderLayout.CENTER);
        return parameterPanel;
    }

    private JPanel createResetPanel() {
        JPanel resetInterpreterPanel = new JPanel(new BorderLayout());
        resetInterpreterPanel.add(resetInterpreter, BorderLayout.WEST);
        return resetInterpreterPanel;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createResetPanel());
        box.add(createParameterPanel());
        box.add(createFilenamePanel());
        add(box, BorderLayout.NORTH);

        JPanel panel = createScriptPanel();
        add(panel, BorderLayout.CENTER);
        // Don't let the input field shrink too much
        add(Box.createVerticalStrut(panel.getPreferredSize().height), BorderLayout.WEST);
    }

    private JPanel createScriptPanel() {
        scriptField = JSyntaxTextArea.getInstance(20, 20);

        JLabel label = new JLabel(JMeterUtils.getResString("bsh_script")); // $NON-NLS-1$
        label.setLabelFor(scriptField);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(JTextScrollPane.getInstance(scriptField), BorderLayout.CENTER);

        JTextArea explain = new JTextArea(JMeterUtils.getResString("bsh_script_variables")); //$NON-NLS-1$
        explain.setLineWrap(true);
        explain.setEditable(false);
        explain.setBackground(this.getBackground());
        panel.add(explain, BorderLayout.SOUTH);

        return panel;
    }
}
