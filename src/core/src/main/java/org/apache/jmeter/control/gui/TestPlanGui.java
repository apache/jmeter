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
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.JBooleanPropertyEditor;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.util.FileListPanel;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.TestPlanSchema;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;

/**
 * JMeter GUI component representing the test plan which will be executed when
 * the test is run.
 */
@TestElementMetadata(labelResource = "test_plan", actionGroups = "")
public class TestPlanGui extends AbstractJMeterGuiComponent {

    private static final long serialVersionUID = 240L;

    /**
     * A checkbox allowing the user to specify whether JMeter should do
     * functional testing.
     */
    private final JBooleanPropertyEditor functionalMode =
            new JBooleanPropertyEditor(
                    TestPlanSchema.INSTANCE.getFunctionalMode(),
                    JMeterUtils.getResString("functional_mode"));

    private final JBooleanPropertyEditor serializedMode =
            new JBooleanPropertyEditor(
                    TestPlanSchema.INSTANCE.getSerializeThreadgroups(),
                    JMeterUtils.getResString("testplan.serialized"));

    private final JBooleanPropertyEditor tearDownOnShutdown =
            new JBooleanPropertyEditor(
                    TestPlanSchema.INSTANCE.getTearDownOnShutdown(),
                    JMeterUtils.getResString("teardown_on_shutdown"));

    /** A panel allowing the user to define variables. */
    private final ArgumentsPanel argsPanel;

    private final FileListPanel browseJar;

    /**
     * Create a new TestPlanGui.
     */
    public TestPlanGui() {
        browseJar = new FileListPanel(JMeterUtils.getResString("test_plan_classpath_browse"), ".jar"); // $NON-NLS-1$ $NON-NLS-2$
        argsPanel = new ArgumentsPanel(JMeterUtils.getResString("user_defined_variables")); // $NON-NLS-1$
        init();
        bindingGroup.addAll(
                Arrays.asList(
                        functionalMode,
                        serializedMode,
                        tearDownOnShutdown
                )
        );
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

    @Override
    public TestElement makeTestElement() {
        return new TestPlan();
    }

    @Override
    public void assignDefaultValues(TestElement element) {
        super.assignDefaultValues(element);
        TestPlan tp = (TestPlan) element;
        tp.setUserDefinedVariables((Arguments) argsPanel.createTestElement());
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement plan) {
        super.modifyTestElement(plan);
        if (plan instanceof TestPlan) {
            TestPlan tp = (TestPlan) plan;
            // TODO: set expression to TestPlan somehow
            tp.setUserDefinedVariables((Arguments) argsPanel.createTestElement());
            String[] files = browseJar.getFiles();
            if (files.length == 0) {
                // Remove property if it is empty
                tp.setTestPlanClasspath(null);
            } else {
                tp.setTestPlanClasspathArray(files);
            }
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
        JComponent explain = new JLabel(JMeterUtils.getResString("functional_mode_explanation")); // $NON-NLS-1$
        southPanel.add(explain);
        southPanel.add(browseJar);

        add(southPanel, BorderLayout.SOUTH);
    }

    @Override
    public void clearGui() {
        super.clearGui();
        argsPanel.clear();
        browseJar.clearFiles();
    }
}
