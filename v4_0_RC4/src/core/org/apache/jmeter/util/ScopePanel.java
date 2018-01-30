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

package org.apache.jmeter.util;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;

/**
 * Scope panel so users can choose whether
 * to apply the test element to the parent sample, the child samples or both.
 *
 */
public class ScopePanel extends JPanel implements ActionListener, FocusListener {

    private static final long serialVersionUID = 240L;

    private final JRadioButton parentButton;
    private final JRadioButton childButton;
    private final JRadioButton allButton;
    private final JRadioButton variableButton;
    private final JTextField variableName;

    public ScopePanel(){
        this(false);
    }

    public ScopePanel(boolean enableVariableButton) {
        this(enableVariableButton, true, true);
    }
    
    public ScopePanel(boolean enableVariableButton, boolean enableParentAndSubsamples, boolean enableSubsamplesOnly) {
        parentButton = new JRadioButton(JMeterUtils.getResString("sample_scope_parent")); //$NON-NLS-1$
        if(enableParentAndSubsamples) {
            allButton = new JRadioButton(JMeterUtils.getResString("sample_scope_all")); //$NON-NLS-1$
        } else {
            allButton = null;
        }
        if(enableSubsamplesOnly) {
            childButton = new JRadioButton(JMeterUtils.getResString("sample_scope_children")); //$NON-NLS-1$
        } else {
            childButton = null;
        }
        if (enableVariableButton) {
            variableButton = new JRadioButton(JMeterUtils.getResString("sample_scope_variable")); //$NON-NLS-1$
            variableName = new JTextField(10);
        } else {
            variableButton = null;
            variableName = null;
        }
        init();
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("sample_scope"))); //$NON-NLS-1$

        parentButton.setSelected(true);

        JPanel buttonPanel = new HorizontalPanel();
        ButtonGroup group = new ButtonGroup();
        if(allButton != null) {
            group.add(allButton);
            buttonPanel.add(allButton);
        }
        group.add(parentButton);
        buttonPanel.add(parentButton);
        if(childButton != null) {
            group.add(childButton);
            buttonPanel.add(childButton);
        }
        
        if (variableButton != null){
            variableButton.addActionListener(this);
            group.add(variableButton);
            buttonPanel.add(variableButton);
            buttonPanel.add(variableName);
            variableName.addFocusListener(this);
        }
        add(buttonPanel);
    }

    public void clearGui() {
        parentButton.setSelected(true);
    }

    public int getSelection(){
        if (parentButton.isSelected()){
            return 0;
        }
        return 1;
    }

    public void setScopeAll() {
        setScopeAll(false);
    }
    
    public void setScopeAll(boolean enableVariableButton) {
        allButton.setSelected(true);
        if (enableVariableButton) {
            variableName.setText(""); //$NON-NLS-1$
        }
    }

    public void setScopeChildren() {
        setScopeChildren(false);
    }

    public void setScopeChildren(boolean enableVariableButton) {
        childButton.setSelected(true);
        if (enableVariableButton) {
            variableName.setText(""); //$NON-NLS-1$
        }
    }

    public void setScopeParent() {
        setScopeParent(false);
    }

    public void setScopeParent(boolean enableVariableButton) {
        parentButton.setSelected(true);
        if (enableVariableButton) {
            variableName.setText(""); //$NON-NLS-1$
        }
    }

    public void setScopeVariable(String value){
        variableButton.setSelected(true);
        variableName.setText(value);
    }

    public boolean isScopeParent() {
        return parentButton.isSelected();
    }

    public boolean isScopeChildren() {
        return childButton != null && childButton.isSelected();
    }

    public boolean isScopeAll() {
        return allButton != null && allButton.isSelected();
    }

    public boolean isScopeVariable() {
        return variableButton != null && variableButton.isSelected();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        variableName.setEnabled(variableButton.isSelected());
    }

    public String getVariable() {
        return variableName.getText();
    }

    @Override
    public void focusGained(FocusEvent focusEvent) {
        variableButton.setSelected(focusEvent.getSource() == variableName);
    }

    @Override
    public void focusLost(FocusEvent focusEvent) {
        // NOOP
    }
}
