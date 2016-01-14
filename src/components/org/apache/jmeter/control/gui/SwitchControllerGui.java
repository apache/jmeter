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

package org.apache.jmeter.control.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.control.SwitchController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class SwitchControllerGui extends AbstractControllerGui {
    private static final long serialVersionUID = 240L;

    private static final String SWITCH_LABEL = "switch_controller_label"; // $NON-NLS-1$

    private JTextField switchValue;

    public SwitchControllerGui() {
        init();
    }

    @Override
    public TestElement createTestElement() {
        SwitchController ic = new SwitchController();
        modifyTestElement(ic);
        return ic;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement ic) {
        configureTestElement(ic);
        ((SwitchController) ic).setSelection(switchValue.getText());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        switchValue.setText(""); // $NON-NLS-1$
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        switchValue.setText(((SwitchController) el).getSelection());
    }

    @Override
    public String getLabelResource() {
        return "switch_controller_title"; // $NON-NLS-1$
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createSwitchPanel(), BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createSwitchPanel() {
        JPanel switchPanel = new JPanel(new BorderLayout(5, 0));
        JLabel selectionLabel = new JLabel(JMeterUtils.getResString(SWITCH_LABEL));
        switchValue = new JTextField(""); // $NON-NLS-1$
        selectionLabel.setLabelFor(switchValue);
        switchPanel.add(selectionLabel, BorderLayout.WEST);
        switchPanel.add(switchValue, BorderLayout.CENTER);
        return switchPanel;
    }
}
