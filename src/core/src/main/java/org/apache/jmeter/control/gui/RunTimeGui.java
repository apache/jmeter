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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.control.RunTime;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The user interface for a controller which specifies that its subcomponents
 * should be executed some number of seconds in a loop. This component can be
 * used standalone or embedded into some other component.
 *
 */

public class RunTimeGui extends AbstractControllerGui implements ActionListener {
    private static final long serialVersionUID = 240L;

    /**
     * A field allowing the user to specify the number of seconds the controller
     * should loop.
     */
    private JTextField seconds;

    /**
     * Boolean indicating whether or not this component should display its name.
     * If true, this is a standalone component. If false, this component is
     * intended to be used as a subpanel for another component.
     */
    private boolean displayName = true;

    /**
     * Create a new LoopControlPanel as a standalone component.
     */
    public RunTimeGui() {
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
    public RunTimeGui(boolean displayName) {
        this.displayName = displayName;
        init();
        setState(1);
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
        if (element instanceof RunTime) {
            setState(((RunTime) element).getRuntimeString());
        } else {
            setState(1);
        }
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        RunTime lc = new RunTime();
        modifyTestElement(lc);
        return lc;
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement lc) {
        configureTestElement(lc);
        if (lc instanceof RunTime) {
            if (seconds.getText().length() > 0) {
                ((RunTime) lc).setRuntime(seconds.getText());
            } else {
                ((RunTime) lc).setRuntime(0);
            }
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        seconds.setText("1"); // $NON-NLS-1$
    }

    /**
     * Invoked when an action occurs. This implementation assumes that the
     * target component is the infinite seconds checkbox.
     *
     * @param event
     *            the event that has occurred
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        seconds.setEnabled(true);
    }

    @Override
    public String getLabelResource() {
        return "runtime_controller_title"; // $NON-NLS-1$
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
     * seconds which should be executed.
     *
     * @return a GUI panel containing the loop count components
     */
    private JPanel createLoopCountPanel() {
        JPanel loopPanel = new JPanel(new BorderLayout(5, 0));

        // SECONDS LABEL
        JLabel secondsLabel = new JLabel(JMeterUtils.getResString("runtime_seconds")); // $NON-NLS-1$
        loopPanel.add(secondsLabel, BorderLayout.WEST);

        JPanel loopSubPanel = new JPanel(new BorderLayout(5, 0));

        // TEXT FIELD
        seconds = new JTextField("1", 5); // $NON-NLS-1$
        secondsLabel.setLabelFor(seconds);
        loopSubPanel.add(seconds, BorderLayout.CENTER);

        loopPanel.add(loopSubPanel, BorderLayout.CENTER);

        loopPanel.add(Box.createHorizontalStrut(secondsLabel.getPreferredSize().width
                + seconds.getPreferredSize().width), BorderLayout.NORTH);

        return loopPanel;
    }

    /**
     * Set the number of seconds which should be reflected in the GUI. The
     * secsCount parameter should contain the String representation of an
     * integer. This integer will be treated as the number of seconds. If this
     * integer is less than 0, the number of seconds will be assumed to be
     * infinity.
     *
     * @param secsCount
     *            the String representation of the number of seconds
     */
    private void setState(String secsCount) {
        seconds.setText(secsCount);
        seconds.setEnabled(true);
    }

    /**
     * Set the number of seconds which should be reflected in the GUI. If the
     * secsCount is less than 0, the number of seconds will be assumed to be
     * infinity.
     *
     * @param secsCount
     *            the number of seconds
     */
    private void setState(long secsCount) {
        seconds.setEnabled(true);
        seconds.setText(Long.toString(secsCount));
    }
}
