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

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.control.CriticalSectionController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The user interface for a controller which specifies that its subcomponents
 * should be executed while a condition holds. This component can be used
 * standalone or embedded into some other component.
 *
 * @since 2.12
 */
public class CriticalSectionControllerGui extends AbstractControllerGui {

    /**
     *
     */
    private static final long serialVersionUID = 7177285850634344095L;

    /**
     * A field allowing the user to specify the lock name
     */
    private JTextField tfLockName;

    /**
     * Boolean indicating whether or not this component should display its name.
     * If true, this is a standalone component. If false, this component is
     * intended to be used as a subpanel for another component.
     */
    private boolean displayName = true;

    /**
     * Create a new CriticalSection Panel as a standalone component.
     */
    public CriticalSectionControllerGui() {
        this(true);
    }

    /**
     * Create a new CriticalSectionPanel as either a standalone or an embedded
     * component.
     *
     * @param displayName
     *            indicates whether or not this component should display its
     *            name. If true, this is a standalone component. If false, this
     *            component is intended to be used as a subpanel for another
     *            component.
     */
    public CriticalSectionControllerGui(boolean displayName) {
        this.displayName = displayName;
        init();
    }

    /**
     * A newly created component can be initialized with the contents of a Test
     * Element object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param element
     *            the TestElement to configure
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof CriticalSectionController) {
            CriticalSectionController controller = (CriticalSectionController) element;
            tfLockName.setText(controller.getLockName());
        }

    }

    /**
     * Implements JMeterGUIComponent.createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        CriticalSectionController controller = new CriticalSectionController();
        modifyTestElement(controller);
        return controller;
    }

    /**
     * Implements JMeterGUIComponent.modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement controller) {
        configureTestElement(controller);
        if (controller instanceof CriticalSectionController) {
            CriticalSectionController csController = (CriticalSectionController) controller;
            csController.setLockName(tfLockName.getText());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        tfLockName.setText("global_lock"); // $NON-NLS-1$
    }

    @Override
    public String getLabelResource() {
        return "critical_section_controller_title"; // $NON-NLS-1$
    }

    /**
     * Initialize the GUI components and layout for this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        // Standalone
        if (displayName) {
            setLayout(new BorderLayout(0, 5));
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(createCriticalSectionPanel(), BorderLayout.NORTH);
            add(mainPanel, BorderLayout.CENTER);

        } else {
            // Embedded
            setLayout(new BorderLayout());
            add(createCriticalSectionPanel(), BorderLayout.NORTH);
        }
    }

    /**
     * Create a GUI panel containing the lockName
     *
     * @return a GUI panel containing the lock name components
     */
    private JPanel createCriticalSectionPanel() {
        JPanel conditionPanel = new JPanel(new BorderLayout(5, 0));

        // Condition LABEL
        JLabel conditionLabel = new JLabel(
                JMeterUtils.getResString("critical_section_controller_label")); // $NON-NLS-1$
        conditionPanel.add(conditionLabel, BorderLayout.WEST);

        // TEXT FIELD
        tfLockName = new JTextField(""); // $NON-NLS-1$
        conditionLabel.setLabelFor(tfLockName);
        conditionPanel.add(tfLockName, BorderLayout.CENTER);

        conditionPanel
                .add(Box.createHorizontalStrut(conditionLabel
                        .getPreferredSize().width
                        + tfLockName.getPreferredSize().width),
                        BorderLayout.NORTH);

        return conditionPanel;
    }
}
