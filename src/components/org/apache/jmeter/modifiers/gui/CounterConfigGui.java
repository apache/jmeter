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

package org.apache.jmeter.modifiers.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.CheckBoxPanel;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
import org.apache.jorphan.gui.layout.VerticalLayout;

public class CounterConfigGui extends AbstractConfigGui implements ActionListener {
    private static final long serialVersionUID = 240L;

    private JLabeledTextField startField;
    private JLabeledTextField incrField;
    private JLabeledTextField endField;
    private JLabeledTextField varNameField;
    private JLabeledTextField formatField;
    private JCheckBox resetCounterOnEachThreadGroupIteration;

    private JCheckBox perUserField;

    public CounterConfigGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "counter_config_title";//$NON-NLS-1$
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        CounterConfig config = new CounterConfig();
        modifyTestElement(config);
        return config;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement c) {
        if (c instanceof CounterConfig) {
            CounterConfig config = (CounterConfig) c;
            config.setStart(startField.getText());
            config.setEnd(endField.getText());
            config.setIncrement(incrField.getText());
            config.setVarName(varNameField.getText());
            config.setFormat(formatField.getText());
            config.setIsPerUser(perUserField.isSelected());
            config.setResetOnThreadGroupIteration(resetCounterOnEachThreadGroupIteration.isEnabled()
                    && resetCounterOnEachThreadGroupIteration.isSelected());
        }
        super.configureTestElement(c);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        startField.setText(""); //$NON-NLS-1$
        incrField.setText(""); //$NON-NLS-1$
        endField.setText(""); //$NON-NLS-1$
        varNameField.setText(""); //$NON-NLS-1$
        formatField.setText(""); //$NON-NLS-1$
        perUserField.setSelected(false);
        resetCounterOnEachThreadGroupIteration.setEnabled(false);
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        CounterConfig config = (CounterConfig) element;
        startField.setText(config.getStartAsString());
        endField.setText(config.getEndAsString());
        incrField.setText(config.getIncrementAsString());
        formatField.setText(config.getFormat());
        varNameField.setText(config.getVarName());
        perUserField.setSelected(config.isPerUser());
        if(config.isPerUser()) {
            resetCounterOnEachThreadGroupIteration.setEnabled(true);
            resetCounterOnEachThreadGroupIteration.setSelected(config.isResetOnThreadGroupIteration());
        } else {
            resetCounterOnEachThreadGroupIteration.setEnabled(false);
        }
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setBorder(makeBorder());
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH));

        startField = new JLabeledTextField(JMeterUtils.getResString("start_value"));//$NON-NLS-1$
        incrField = new JLabeledTextField(JMeterUtils.getResString("increment"));//$NON-NLS-1$
        endField = new JLabeledTextField(JMeterUtils.getResString("max_value"));//$NON-NLS-1$
        varNameField = new JLabeledTextField(JMeterUtils.getResString("var_name"));//$NON-NLS-1$
        formatField = new JLabeledTextField(JMeterUtils.getResString("format"));//$NON-NLS-1$
        perUserField = new JCheckBox(JMeterUtils.getResString("counter_per_user"));//$NON-NLS-1$
        resetCounterOnEachThreadGroupIteration = new JCheckBox(JMeterUtils.getResString("counter_reset_per_tg_iteration"));//$NON-NLS-1$
        add(makeTitlePanel());
        add(startField);
        add(incrField);
        add(endField);
        add(formatField);
        add(varNameField);
        add(CheckBoxPanel.wrap(perUserField));
        add(CheckBoxPanel.wrap(resetCounterOnEachThreadGroupIteration));

        perUserField.addActionListener(this);
    }

    /**
     * Disable/Enable resetCounterOnEachThreadGroupIteration when perUserField is disabled / enabled
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == perUserField) {
            resetCounterOnEachThreadGroupIteration.setEnabled(perUserField.isSelected());
        }
    }
}
