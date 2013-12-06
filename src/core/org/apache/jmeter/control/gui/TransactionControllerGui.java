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

import javax.swing.JCheckBox;

import org.apache.jmeter.control.TransactionController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * A Transaction controller component.
 *
 */
public class TransactionControllerGui extends AbstractControllerGui {

    private static final long serialVersionUID = 240L;

    private JCheckBox parent; // If selected, then generate parent sample, otherwise as per original controller

    private JCheckBox includeTimers; // if selected, add duration of timers to total runtime

    /**
     * Create a new TransactionControllerGui instance.
     */
    public TransactionControllerGui() {
        init();
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        TransactionController lc = new TransactionController();
        lc.setIncludeTimers(false); // change default for new test elements
        configureTestElement(lc);
        return lc;
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        parent.setSelected(((TransactionController) el).isParent());
        includeTimers.setSelected(((TransactionController) el).isIncludeTimers());
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement el) {
        configureTestElement(el);
        ((TransactionController) el).setParent(parent.isSelected());
        TransactionController tc = ((TransactionController) el);
        tc.setParent(parent.isSelected());
        tc.setIncludeTimers(includeTimers.isSelected());
    }

    @Override
    public String getLabelResource() {
        return "transaction_controller_title"; // $NON-NLS-1$
    }

    /**
     * Initialize the GUI components and layout for this component.
     */
    private void init() {
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
        setBorder(makeBorder());
        add(makeTitlePanel());
        parent = new JCheckBox(JMeterUtils.getResString("transaction_controller_parent")); // $NON-NLS-1$
        add(parent);
        includeTimers = new JCheckBox(JMeterUtils.getResString("transaction_controller_include_timers"), true); // $NON-NLS-1$
        add(includeTimers);
    }
}
