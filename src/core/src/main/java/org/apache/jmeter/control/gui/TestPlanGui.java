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
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.util.FileListPanel;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;

/**
 * JMeter GUI component representing the test plan which will be executed when
 * the test is run.
 */
public class TestPlanGui extends AbstractJMeterGuiComponent {

    private static final long serialVersionUID = 240L;

    /**
     * A checkbox allowing the user to specify whether or not JMeter should do
     * functional testing.
     */
    private final JCheckBox functionalMode;

    private final JCheckBox serializedMode;

    private final JCheckBox tearDownOnShutdown;

    /** A panel allowing the user to define variables. */
    private final ArgumentsPanel argsPanel;

    private final FileListPanel browseJar;

    /**
     * Create a new TestPlanGui.
     */
    public TestPlanGui() {
        browseJar = new FileListPanel(JMeterUtils.getResString("test_plan_classpath_browse"), ".jar"); // $NON-NLS-1$ $NON-NLS-2$
        argsPanel = new ArgumentsPanel(JMeterUtils.getResString("user_defined_variables")); // $NON-NLS-1$
        serializedMode = new JCheckBox(JMeterUtils.getResString("testplan.serialized")); // $NON-NLS-1$
        functionalMode = new JCheckBox(JMeterUtils.getResString("functional_mode")); // $NON-NLS-1$
        tearDownOnShutdown = new JCheckBox(JMeterUtils.getResString("teardown_on_shutdown"), true); // $NON-NLS-1$
        init();
    }

    /**
     * When a user right-clicks on the component in the test tree, or selects
     * the edit menu when the component is selected, the component will be asked
     * to return a JPopupMenu that provides all the options available to the
     * user from this component.
     * <p>
     * The TestPlan will return a popup menu allowing you to add ThreadGroups,
     * Listeners, Configuration Elements, Assertions, PreProcessors,
     * PostProcessors, and Timers.
     *
     * @return a JPopupMenu appropriate for the component.
     */
    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu pop = new JPopupMenu();
        JMenu addMenu = new JMenu(JMeterUtils.getResString("add")); // $NON-NLS-1$
        addMenu.add(MenuFactory.makeMenu(MenuFactory.THREADS, ActionNames.ADD));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.CONFIG_ELEMENTS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.LISTENERS, ActionNames.ADD));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.TIMERS, ActionNames.ADD));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.PRE_PROCESSORS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.POST_PROCESSORS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.ASSERTIONS, ActionNames.ADD));
        addMenu.addSeparator();
        addMenu.add(MenuFactory.makeMenu(MenuFactory.FRAGMENTS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.NON_TEST_ELEMENTS, ActionNames.ADD));
        pop.add(addMenu);
        MenuFactory.addPasteResetMenu(pop);
        MenuFactory.addFileMenu(pop, false);
        return pop;
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        TestPlan tp = new TestPlan();
        modifyTestElement(tp);
        return tp;
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement plan) {
        super.configureTestElement(plan);
        if (plan instanceof TestPlan) {
            TestPlan tp = (TestPlan) plan;
            tp.setFunctionalMode(functionalMode.isSelected());
            tp.setTearDownOnShutdown(tearDownOnShutdown.isSelected());
            tp.setSerialized(serializedMode.isSelected());
            tp.setUserDefinedVariables((Arguments) argsPanel.createTestElement());
            tp.setTestPlanClasspathArray(browseJar.getFiles());
        }
    }

    @Override
    public String getLabelResource() {
        return "test_plan"; // $NON-NLS-1$
    }

    /**
     * This is the list of menu categories this gui component will be available
     * under. This implementation returns null, since the TestPlan appears at
     * the top level of the tree and cannot be added elsewhere.
     *
     * @return a Collection of Strings, where each element is one of the
     *         constants defined in MenuFactory
     */
    @Override
    public Collection<String> getMenuCategories() {
        return null;
    }

    /**
     * A newly created component can be initialized with the contents of a Test
     * Element object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param el
     *            the TestElement to configure
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof TestPlan) {
            TestPlan tp = (TestPlan) el;
            functionalMode.setSelected(tp.isFunctionalMode());
            serializedMode.setSelected(tp.isSerialized());
            tearDownOnShutdown.setSelected(tp.isTearDownOnShutdown());
            final JMeterProperty udv = tp.getUserDefinedVariablesAsProperty();
            if (udv != null) {
                argsPanel.configure((Arguments) udv.getObjectValue());
            }
            browseJar.setFiles(tp.getTestPlanClasspathArray());
        }
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(10, 10));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        add(argsPanel, BorderLayout.CENTER);

        VerticalPanel southPanel = new VerticalPanel();
        southPanel.add(serializedMode);
        southPanel.add(tearDownOnShutdown);
        southPanel.add(functionalMode);
        JTextArea explain = new JTextArea(JMeterUtils.getResString("functional_mode_explanation")); // $NON-NLS-1$
        explain.setEditable(false);
        explain.setBackground(this.getBackground());
        southPanel.add(explain);
        southPanel.add(browseJar);

        add(southPanel, BorderLayout.SOUTH);
    }

    @Override
    public void clearGui() {
        super.clearGui();
        functionalMode.setSelected(false);
        serializedMode.setSelected(false);
        tearDownOnShutdown.setSelected(true);
        argsPanel.clear();
        browseJar.clearFiles();
    }
}
