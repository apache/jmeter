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

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Assertion scope panel for Assertions so users can choose whether
 * to apply the assertion to the parent sample, the child samples or both.
 * 
 * This class is a helper class for the AbstractAssertionGui.
 *
 */
public class AssertionScopePanel extends JPanel {

    private JRadioButton parentButton;
    private JRadioButton childButton;
    private JRadioButton allButton;
    
    /**
     * Create a new NamePanel with the default name.
     */
    public AssertionScopePanel() {
        init();
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init() {
        setLayout(new BorderLayout(5, 0));
        setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("assertion_scope"))); //$NON-NLS-1$

        allButton = new JRadioButton(JMeterUtils.getResString("assertion_scope_all"));
        parentButton = new JRadioButton(JMeterUtils.getResString("assertion_scope_parent"));
        parentButton.setSelected(true);
        childButton = new JRadioButton(JMeterUtils.getResString("assertion_scope_children"));
        
        ButtonGroup group = new ButtonGroup();
        group.add(allButton);
        group.add(parentButton);
        group.add(childButton);
        JPanel buttonPanel = new HorizontalPanel();
        buttonPanel.add(parentButton);
        buttonPanel.add(childButton);
        buttonPanel.add(allButton);
        
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
        allButton.setSelected(true);
    }

    public void setScopeChildren() {
        childButton.setSelected(true);        
    }

    public void setScopeParent() {
        parentButton.setSelected(true);
    }

    public boolean isScopeParent() {
        return parentButton.isSelected();
    }

    public boolean isScopeChildren() {
        return childButton.isSelected();
    }
}
