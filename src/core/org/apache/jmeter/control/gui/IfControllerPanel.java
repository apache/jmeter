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
import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.control.IfController;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The user interface for a controller which specifies that its subcomponents
 * should be executed while a condition holds. This component can be used
 * standalone or embedded into some other component.
 *
 */
@GUIMenuSortOrder(1)
public class IfControllerPanel extends AbstractControllerGui implements ChangeListener {

    private static final long serialVersionUID = 240L;

    /**
     * Used to warn about performance penalty
     */
    private JLabel warningLabel;

    private JLabel conditionLabel;
    /**
     * A field allowing the user to specify the number of times the controller
     * should loop.
     */
    private JSyntaxTextArea theCondition;

    private JCheckBox useExpression;

    private JCheckBox evaluateAll;

    /**
     * Boolean indicating whether or not this component should display its name.
     * If true, this is a standalone component. If false, this component is
     * intended to be used as a subpanel for another component.
     */
    private boolean displayName = true;

    /**
     * Create a new LoopControlPanel as a standalone component.
     */
    public IfControllerPanel() {
        this(true);
    }

    /**
     * Create a new IfControllerPanel as either a standalone or an embedded
     * component.
     *
     * @param displayName
     *            indicates whether or not this component should display its
     *            name. If true, this is a standalone component. If false, this
     *            component is intended to be used as a subpanel for another
     *            component.
     */
    public IfControllerPanel(boolean displayName) {
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
        if (element instanceof IfController) {
            IfController ifController = (IfController) element;
            theCondition.setText(ifController.getCondition());
            evaluateAll.setSelected(ifController.isEvaluateAll());
            useExpression.setSelected(ifController.isUseExpression());
        }

    }

    /**
     * Implements JMeterGUIComponent.createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        IfController controller = new IfController();
        modifyTestElement(controller);
        return controller;
    }

    /**
     * Implements JMeterGUIComponent.modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement controller) {
        configureTestElement(controller);
        if (controller instanceof IfController) {
            IfController ifController = (IfController) controller;
            ifController.setCondition(theCondition.getText());
            ifController.setEvaluateAll(evaluateAll.isSelected());
            ifController.setUseExpression(useExpression.isSelected());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        useExpression.setSelected(true);
        theCondition.setText(""); // $NON-NLS-1$
        evaluateAll.setSelected(false);
    }

    @Override
    public String getLabelResource() {
        return "if_controller_title"; // $NON-NLS-1$
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
            mainPanel.add(createConditionPanel(), BorderLayout.NORTH);
            add(mainPanel, BorderLayout.CENTER);

        } else {
            // Embedded
            setLayout(new BorderLayout());
            add(createConditionPanel(), BorderLayout.NORTH);
        }
    }

    /**
     * Create a GUI panel containing the condition.
     *
     * @return a GUI panel containing the condition components
     */
    private JPanel createConditionPanel() {
        JPanel conditionPanel = new JPanel(new BorderLayout(5, 0));

        // Condition LABEL
        conditionLabel = new JLabel(JMeterUtils.getResString("if_controller_label")); // $NON-NLS-1$
        conditionPanel.add(conditionLabel, BorderLayout.WEST);
        ImageIcon image = JMeterUtils.getImage("warning.png");
        warningLabel = new JLabel(JMeterUtils.getResString("if_controller_warning"), image, SwingConstants.CENTER); // $NON-NLS-1$
        warningLabel.setForeground(Color.RED);
        Font font = warningLabel.getFont();
        warningLabel.setFont(new Font(font.getFontName(), Font.BOLD, (int)(font.getSize()*1.1)));

        // Condition
        theCondition = JSyntaxTextArea.getInstance(5, 50); // $NON-NLS-1$
        conditionLabel.setLabelFor(theCondition);
        conditionPanel.add(JTextScrollPane.getInstance(theCondition), BorderLayout.CENTER);
       
        conditionPanel.add(warningLabel, BorderLayout.NORTH);


        JPanel optionPanel = new JPanel();

        // Use expression instead of Javascript
        useExpression = new JCheckBox(JMeterUtils.getResString("if_controller_expression")); // $NON-NLS-1$
        useExpression.addChangeListener(this);
        optionPanel.add(useExpression);

        // Evaluate All checkbox
        evaluateAll = new JCheckBox(JMeterUtils.getResString("if_controller_evaluate_all")); // $NON-NLS-1$
        optionPanel.add(evaluateAll);

        conditionPanel.add(optionPanel,BorderLayout.SOUTH);
        return conditionPanel;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(e.getSource() == useExpression) {
            if(useExpression.isSelected()) {
                warningLabel.setForeground(Color.BLACK);
                conditionLabel.setText(JMeterUtils.getResString("if_controller_expression_label"));
            } else {
                warningLabel.setForeground(Color.RED);
                conditionLabel.setText(JMeterUtils.getResString("if_controller_label"));
            }
        }
    }
}
