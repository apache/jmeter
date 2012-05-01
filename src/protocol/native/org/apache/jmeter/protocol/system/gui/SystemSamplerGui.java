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

package org.apache.jmeter.protocol.system.gui;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.system.SystemSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

/**
 * GUI for {@link SystemSampler}
 */
public class SystemSamplerGui extends AbstractSamplerGui implements ItemListener {

    /**
     * 
     */
    private static final long serialVersionUID = -2413845772703695934L;
    
    private JCheckBox checkReturnCode;
    private JLabeledTextField desiredReturnCode;
    private JLabeledTextField directory;
    private JLabeledTextField command;
    private ArgumentsPanel argsPanel;
    private ArgumentsPanel envPanel;
    
    /**
     * Constructor for JavaTestSamplerGui
     */
    public SystemSamplerGui() {
        super();
        init();
    }

    public String getLabelResource() {
        return "system_sampler_title";
    }

    public String getStaticLabel() {
        return JMeterUtils.getResString(getLabelResource());
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
       
        JPanel panelb = new VerticalPanel();
        panelb.add(makeReturnCodePanel());
        panelb.add(Box.createVerticalStrut(5));
        panelb.add(makeCommandPanel(), BorderLayout.CENTER);
        
        add(panelb, BorderLayout.CENTER);
    }

    /* Implements JMeterGuiComponent.createTestElement() */
    public TestElement createTestElement() {
        SystemSampler sampler = new SystemSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    public void modifyTestElement(TestElement sampler) {
        super.configureTestElement(sampler);
        sampler.setProperty(SystemSampler.CHECK_RETURN_CODE, Boolean.toString(checkReturnCode.isSelected()));
        if(checkReturnCode.isSelected()) {
            sampler.setProperty(SystemSampler.EXPECTED_RETURN_CODE, desiredReturnCode.getText());
        } else {
            sampler.setProperty(SystemSampler.EXPECTED_RETURN_CODE, "");
        }
        sampler.setProperty(SystemSampler.COMMAND, command.getText());
        ((SystemSampler)sampler).setArguments((Arguments)argsPanel.createTestElement());
        ((SystemSampler)sampler).setEnvironmentVariables((Arguments)envPanel.createTestElement());
        sampler.setProperty(SystemSampler.DIRECTORY, directory.getText());
    }

    /* Overrides AbstractJMeterGuiComponent.configure(TestElement) */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        checkReturnCode.setSelected(el.getPropertyAsBoolean(SystemSampler.CHECK_RETURN_CODE));
        desiredReturnCode.setText(el.getPropertyAsString(SystemSampler.EXPECTED_RETURN_CODE));
        desiredReturnCode.setEnabled(checkReturnCode.isSelected());
        command.setText(el.getPropertyAsString(SystemSampler.COMMAND));
        argsPanel.configure((Arguments)el.getProperty(SystemSampler.ARGUMENTS).getObjectValue());
        envPanel.configure((Arguments)el.getProperty(SystemSampler.ENVIRONMENT).getObjectValue());
        directory.setText(el.getPropertyAsString(SystemSampler.DIRECTORY));
    }

    /**
     * @return JPanel return code config
     */
    private JPanel makeReturnCodePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("return_code_config_box_title")));
        checkReturnCode = new JCheckBox(JMeterUtils.getResString("check_return_code_title"));
        checkReturnCode.addItemListener(this);
        desiredReturnCode = new JLabeledTextField(JMeterUtils.getResString("expected_return_code_title"));
        desiredReturnCode.setSize(desiredReturnCode.getSize().height, 30);
        panel.add(checkReturnCode);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(desiredReturnCode);
        checkReturnCode.setSelected(true);
        return panel;
    }
    
    /**
     * @return JPanel Command + directory
     */
    private JPanel makeCommandPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("command_config_box_title")));
        
        JPanel cmdPanel = new JPanel();
        cmdPanel.setLayout(new BoxLayout(cmdPanel, BoxLayout.X_AXIS));
        
        directory = new JLabeledTextField(JMeterUtils.getResString("directory_field_title"));
        cmdPanel.add(directory);
        cmdPanel.add(Box.createHorizontalStrut(5));
        command = new JLabeledTextField(JMeterUtils.getResString("command_field_title"));
        cmdPanel.add(command);
        panel.add(cmdPanel, BorderLayout.NORTH);
        panel.add(makeArgumentsPanel(), BorderLayout.CENTER);
        panel.add(makeEnvironmentPanel(), BorderLayout.SOUTH);
        return panel;
    }
    
    /**
     * @return JPanel Arguments Panel
     */
    private JPanel makeArgumentsPanel() {
        argsPanel = new ArgumentsPanel(JMeterUtils.getResString("arguments_panel_title"), null, true, false , 
                new ObjectTableModel(new String[] { ArgumentsPanel.COLUMN_RESOURCE_NAMES_1 },
                        Argument.class,
                        new Functor[] {
                        new Functor("getValue") },  // $NON-NLS-1$
                        new Functor[] {
                        new Functor("setValue") }, // $NON-NLS-1$
                        new Class[] {String.class }));
        return argsPanel;
    }
    
    /**
     * @return JPanel Environment Panel
     */
    private JPanel makeEnvironmentPanel() {
        envPanel = new ArgumentsPanel(JMeterUtils.getResString("environment_panel_title"));
        return envPanel;
    }

    /**
     * @see org.apache.jmeter.gui.AbstractJMeterGuiComponent#clearGui()
     */
    @Override
    public void clearGui() {
        super.clearGui();
        directory.setText("");
        command.setText("");
        argsPanel.clearGui();
        envPanel.clearGui();
        desiredReturnCode.setText("");
        checkReturnCode.setSelected(false);
        desiredReturnCode.setEnabled(false);
    }

    public void itemStateChanged(ItemEvent e) {
        if(e.getSource()==checkReturnCode) {
            desiredReturnCode.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
        }
    }
}