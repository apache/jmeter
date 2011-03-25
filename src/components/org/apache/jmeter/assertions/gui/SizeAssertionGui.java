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

package org.apache.jmeter.assertions.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.jmeter.assertions.SizeAssertion;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * GUI for {@link SizeAssertion}
 */
public class SizeAssertionGui extends AbstractAssertionGui implements ActionListener {

    private static final long serialVersionUID = 240L;

    private JTextField size;

    private JRadioButton equalButton, notequalButton, greaterthanButton, lessthanButton, greaterthanequalButton,
            lessthanequalButton;

    private int execState; // store the operator

    public SizeAssertionGui() {
        init();
    }

    public String getLabelResource() {
        return "size_assertion_title"; //$NON-NLS-1$
    }

    public TestElement createTestElement() {
        SizeAssertion el = new SizeAssertion();
        modifyTestElement(el);
        return el;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement el) {
        configureTestElement(el);
        SizeAssertion assertion = (SizeAssertion) el;
        assertion.setAllowedSize(size.getText());
        assertion.setCompOper(getState());
        saveScopeSettings(assertion);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        size.setText(""); //$NON-NLS-1$
        equalButton.setSelected(true);
        notequalButton.setSelected(false);
        greaterthanButton.setSelected(false);
        lessthanButton.setSelected(false);
        greaterthanequalButton.setSelected(false);
        lessthanequalButton.setSelected(false);
        execState = SizeAssertion.EQUAL;
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        SizeAssertion assertion = (SizeAssertion) el;
        size.setText(assertion.getAllowedSize());
        setState(assertion.getCompOper());
        showScopeSettings(assertion);
    }

    /**
     * Set the state of the radio Button
     */
    public void setState(int state) {
        if (state == SizeAssertion.EQUAL) {
            equalButton.setSelected(true);
            execState = state;
        } else if (state == SizeAssertion.NOTEQUAL) {
            notequalButton.setSelected(true);
            execState = state;
        } else if (state == SizeAssertion.GREATERTHAN) {
            greaterthanButton.setSelected(true);
            execState = state;
        } else if (state == SizeAssertion.LESSTHAN) {
            lessthanButton.setSelected(true);
            execState = state;
        } else if (state == SizeAssertion.GREATERTHANEQUAL) {
            greaterthanequalButton.setSelected(true);
            execState = state;
        } else if (state == SizeAssertion.LESSTHANEQUAL) {
            lessthanequalButton.setSelected(true);
            execState = state;
        }
    }

    /**
     * Get the state of the radio Button
     */
    public int getState() {
        return execState;
    }

    private void init() {
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
        setBorder(makeBorder());

        add(makeTitlePanel());

        add(createScopePanel(true));

        // USER_INPUT
        JPanel sizePanel = new JPanel();
        sizePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("size_assertion_size_test"))); //$NON-NLS-1$

        sizePanel.add(new JLabel(JMeterUtils.getResString("size_assertion_label"))); //$NON-NLS-1$
        size = new JTextField(12);
        sizePanel.add(size);

        sizePanel.add(createComparatorButtonPanel());

        add(sizePanel);
    }

    private Box createComparatorButtonPanel() {
        ButtonGroup group = new ButtonGroup();

        equalButton = createComparatorButton("=", SizeAssertion.EQUAL, group); //$NON-NLS-1$
        notequalButton = createComparatorButton("!=", SizeAssertion.NOTEQUAL, group); //$NON-NLS-1$
        greaterthanButton = createComparatorButton(">", SizeAssertion.GREATERTHAN, group); //$NON-NLS-1$
        lessthanButton = createComparatorButton("<", SizeAssertion.LESSTHAN, group); //$NON-NLS-1$
        greaterthanequalButton = createComparatorButton(">=", SizeAssertion.GREATERTHANEQUAL, group); //$NON-NLS-1$
        lessthanequalButton = createComparatorButton("<=", SizeAssertion.LESSTHANEQUAL, group); //$NON-NLS-1$

        equalButton.setSelected(true);
        execState = Integer.parseInt(equalButton.getActionCommand());

        // Put the check boxes in a column in a panel
        Box checkPanel = Box.createVerticalBox();
        JLabel compareLabel = new JLabel(JMeterUtils.getResString("size_assertion_comparator_label")); //$NON-NLS-1$
        checkPanel.add(compareLabel);
        checkPanel.add(equalButton);
        checkPanel.add(notequalButton);
        checkPanel.add(greaterthanButton);
        checkPanel.add(lessthanButton);
        checkPanel.add(greaterthanequalButton);
        checkPanel.add(lessthanequalButton);
        return checkPanel;
    }

    private JRadioButton createComparatorButton(String name, int value, ButtonGroup group) {
        JRadioButton button = new JRadioButton(name);
        button.setActionCommand(String.valueOf(value));
        button.addActionListener(this);
        group.add(button);
        return button;
    }

    public void actionPerformed(ActionEvent e) {
        int comparator = Integer.parseInt(e.getActionCommand());
        execState = comparator;
    }
}
