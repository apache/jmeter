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

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.control.ForeachController;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The user interface for a foreach controller which specifies that its
 * subcomponents should be executed some number of times in a loop. This
 * component can be used standalone or embedded into some other component.
 */

public class ForeachControlPanel extends AbstractControllerGui {

    private static final long serialVersionUID = 240L;

    /**
     * A field allowing the user to specify the input variable the controller
     * should loop.
     */
    private JTextField inputVal;

    /**
     * A field allowing the user to specify the indice start of the loop
     */
    private JTextField startIndex;

    /**
     * A field allowing the user to specify the indice end of the loop
     */
    private JTextField endIndex;

    /**
     * A field allowing the user to specify output variable the controller
     * should return.
     */
    private JTextField returnVal;

    // Should we add the "_" separator?
    private JCheckBox useSeparator;

    /**
     * Boolean indicating whether or not this component should display its name.
     * If true, this is a standalone component. If false, this component is
     * intended to be used as a subpanel for another component.
     */
    private boolean displayName = true;

    /** The name of the infinite checkbox component. */
    private static final String INPUTVAL = "Input Field"; // $NON-NLS-1$

    /** The name of the loops field component. */
    private static final String RETURNVAL = "Return Field"; // $NON-NLS-1$

    /** The name of the start index field component. */
    private static final String START_INDEX = "Start Index Field"; // $NON-NLS-1$

    /** The name of the end index field component. */
    private static final String END_INDEX = "End Index Field"; // $NON-NLS-1$
    /**
     * Create a new LoopControlPanel as a standalone component.
     */
    public ForeachControlPanel() {
        this(true);
    }

    /**
     * Create a new LoopControlPanel as either a standalone or an embedded
     * component.
     *
     * @param displayName
     *            indicates whether or not this component should display its
     *            name. If true, this is a standalone component. If false, this
     *            component is intended to be used as a subpanel for another
     *            component.
     */
    public ForeachControlPanel(boolean displayName) {
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
        inputVal.setText(((ForeachController) element).getInputValString());
        startIndex.setText(((ForeachController) element).getStartIndexAsString());
        endIndex.setText(((ForeachController) element).getEndIndexAsString());
        returnVal.setText(((ForeachController) element).getReturnValString());
        useSeparator.setSelected(((ForeachController) element).getUseSeparator());
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        ForeachController lc = new ForeachController();
        modifyTestElement(lc);
        return lc;
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement lc) {
        configureTestElement(lc);
        if (lc instanceof ForeachController) {
            ForeachController fec = (ForeachController) lc;
            fec.setInputVal(inputVal.getText());
            fec.setStartIndex(startIndex.getText());
            fec.setEndIndex(endIndex.getText());
            fec.setReturnVal(returnVal.getText());
            fec.setUseSeparator(useSeparator.isSelected());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        inputVal.setText(""); // $NON-NLS-1$
        startIndex.setText(""); // $NON-NLS-1$
        endIndex.setText(""); // $NON-NLS-1$
        returnVal.setText(""); // $NON-NLS-1$
        useSeparator.setSelected(true);
    }


    @Override
    public String getLabelResource() {
        return "foreach_controller_title"; // $NON-NLS-1$
    }

    /**
     * Initialize the GUI components and layout for this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        // The Loop Controller panel can be displayed standalone or inside
        // another panel. For standalone, we want to display the TITLE, NAME,
        // etc. (everything). However, if we want to display it within another
        // panel, we just display the Loop Count fields (not the TITLE and
        // NAME).

        // Standalone
        if (displayName) {
            setLayout(new BorderLayout(0, 5));
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.add(createLoopCountPanel(), BorderLayout.NORTH);
            add(mainPanel, BorderLayout.CENTER);
        } else {
            // Embedded
            setLayout(new BorderLayout());
            add(createLoopCountPanel(), BorderLayout.NORTH);
        }
    }

    /**
     * Create a GUI panel containing the components related to the number of
     * loops which should be executed.
     *
     * @return a GUI panel containing the loop count components
     */
    private JPanel createLoopCountPanel() {
        VerticalPanel loopPanel = new VerticalPanel();

        // LOOP LABEL
        JLabel inputValLabel = new JLabel(JMeterUtils.getResString("foreach_input")); // $NON-NLS-1$
        JLabel startIndexLabel = new JLabel(JMeterUtils.getResString("foreach_start_index")); // $NON-NLS-1$
        JLabel endIndexLabel = new JLabel(JMeterUtils.getResString("foreach_end_index")); // $NON-NLS-1$
        JLabel returnValLabel = new JLabel(JMeterUtils.getResString("foreach_output")); // $NON-NLS-1$

        // TEXT FIELD
        JPanel inputValSubPanel = new JPanel(new BorderLayout(5, 0));
        inputVal = new JTextField("", 5); // $NON-NLS-1$
        inputVal.setName(INPUTVAL);
        inputValLabel.setLabelFor(inputVal);
        inputValSubPanel.add(inputValLabel, BorderLayout.WEST);
        inputValSubPanel.add(inputVal, BorderLayout.CENTER);

        // TEXT FIELD
        JPanel startIndexSubPanel = new JPanel(new BorderLayout(5, 0));
        startIndex = new JTextField("", 5); // $NON-NLS-1$
        startIndex.setName(START_INDEX);
        startIndexLabel.setLabelFor(startIndex);
        startIndexSubPanel.add(startIndexLabel, BorderLayout.WEST);
        startIndexSubPanel.add(startIndex, BorderLayout.CENTER);

        // TEXT FIELD
        JPanel endIndexSubPanel = new JPanel(new BorderLayout(5, 0));
        endIndex = new JTextField("", 5); // $NON-NLS-1$
        endIndex.setName(END_INDEX);
        endIndexLabel.setLabelFor(endIndex);
        endIndexSubPanel.add(endIndexLabel, BorderLayout.WEST);
        endIndexSubPanel.add(endIndex, BorderLayout.CENTER);

        // TEXT FIELD
        JPanel returnValSubPanel = new JPanel(new BorderLayout(5, 0));
        returnVal = new JTextField("", 5); // $NON-NLS-1$
        returnVal.setName(RETURNVAL);
        returnValLabel.setLabelFor(returnVal);
        returnValSubPanel.add(returnValLabel, BorderLayout.WEST);
        returnValSubPanel.add(returnVal, BorderLayout.CENTER);

        // Checkbox
        useSeparator = new JCheckBox(JMeterUtils.getResString("foreach_use_separator"), true); // $NON-NLS-1$
        loopPanel.add(inputValSubPanel);
        loopPanel.add(startIndexSubPanel);
        loopPanel.add(endIndexSubPanel);
        loopPanel.add(returnValSubPanel);
        loopPanel.add(useSeparator);

        return loopPanel;
    }
}
