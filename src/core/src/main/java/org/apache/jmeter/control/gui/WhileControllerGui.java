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

package org.apache.jmeter.control.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.apache.jmeter.control.WhileController;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import net.miginfocom.swing.MigLayout;

@GUIMenuSortOrder(4)
@TestElementMetadata(labelResource = "while_controller_title")
public class WhileControllerGui extends AbstractControllerGui {

    private static final long serialVersionUID = 240L;

    /**
     * A field allowing the user to specify the condition (not yet used).
     */
    private JSyntaxTextArea theCondition;

    /** The name of the condition field component. */
    private static final String CONDITION = "While_Condition"; // $NON-NLS-1$

    /**
     * Create a new LoopControlPanel as a standalone component.
     */
    public WhileControllerGui() {
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
        if (element instanceof WhileController whileController) {
            theCondition.setText(whileController.getCondition());
        }

    }

    /**
     * Implements JMeterGUIComponent.createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        WhileController controller = new WhileController();
        modifyTestElement(controller);
        return controller;
    }

    /**
     * Implements JMeterGUIComponent.modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement controller) {
        configureTestElement(controller);
        if (controller instanceof WhileController whileController) {
            if (!theCondition.getText().isEmpty()) {
                whileController.setCondition(theCondition.getText());
            } else {
                whileController.setCondition(""); // $NON-NLS-1$
            }
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        theCondition.setText(""); // $NON-NLS-1$
    }

    @Override
    public String getLabelResource() {
        return "while_controller_title"; // $NON-NLS-1$
    }

    /**
     * Initialize the GUI components and layout for this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        add(createConditionPanel(), BorderLayout.CENTER);
    }

    /**
     * Create a GUI panel containing the condition. TODO make use of the field
     *
     * @return a GUI panel containing the condition components
     */
    private JPanel createConditionPanel() {
        JPanel conditionPanel = new JPanel(new MigLayout("fillx, wrap 2, insets 0", "[][fill,grow]"));

        // Condition
        // This means exit if last sample failed
        theCondition = JSyntaxTextArea.getInstance(5, 50);
        JTextScrollPane theConditionJSP = JTextScrollPane.getInstance(theCondition);
        conditionPanel.add(JMeterUtils.labelFor(theConditionJSP, "while_controller_label"), "top");
        theCondition.setName(CONDITION);
        conditionPanel.add(theConditionJSP, "push, grow");

        return conditionPanel;
    }
}
