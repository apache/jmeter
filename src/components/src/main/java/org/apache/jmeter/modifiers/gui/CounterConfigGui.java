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

package org.apache.jmeter.modifiers.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.modifiers.CounterConfig;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import net.miginfocom.swing.MigLayout;

@TestElementMetadata(labelResource = "counter_config_title")
public class CounterConfigGui extends AbstractConfigGui implements ActionListener {
    private static final long serialVersionUID = 240L;

    private JTextField startField;
    private JTextField incrField;
    private JTextField endField;
    private JTextField varNameField;
    private JTextField formatField;
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
        setLayout(new BorderLayout());

        JPanel counterPanel = new JPanel(new MigLayout("fillx, wrap 2, insets 0", "[][fill,grow]"));

        startField = new JTextField(20);
        counterPanel.add(JMeterUtils.labelFor(startField, "start_value"));
        counterPanel.add(startField);

        incrField = new JTextField(20);
        counterPanel.add(JMeterUtils.labelFor(incrField, "increment"));
        counterPanel.add(incrField);

        endField = new JTextField(20);
        counterPanel.add(JMeterUtils.labelFor(endField, "max_value"));
        counterPanel.add(endField);

        formatField = new JTextField(20);
        counterPanel.add(JMeterUtils.labelFor(formatField, "format"));
        counterPanel.add(formatField);

        varNameField = new JTextField(20);
        counterPanel.add(JMeterUtils.labelFor(varNameField, "var_name"));
        counterPanel.add(varNameField);

        perUserField = new JCheckBox(JMeterUtils.getResString("counter_per_user"));//$NON-NLS-1$
        perUserField.addActionListener(this);
        counterPanel.add(perUserField, "span 2");

        resetCounterOnEachThreadGroupIteration = new JCheckBox(JMeterUtils.getResString("counter_reset_per_tg_iteration"));//$NON-NLS-1$
        counterPanel.add(resetCounterOnEachThreadGroupIteration, "span 2");

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(counterPanel, BorderLayout.CENTER);
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
