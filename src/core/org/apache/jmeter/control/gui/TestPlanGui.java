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
import java.awt.Container;
import java.util.Collection;

import javax.swing.JCheckBox;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.util.FileListPanel;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.gui.ThreadGroupGui;
import org.apache.jmeter.util.JMeterUtils;

/**
 * JMeter GUI component representing the test plan which will be executed when
 * the test is run.
 * 
 * @version $Revision$ Last Updated: $Date$
 */
public class TestPlanGui extends AbstractJMeterGuiComponent {

	/**
	 * A checkbox allowing the user to specify whether or not JMeter should do
	 * functional testing.
	 */
	private JCheckBox functionalMode;

	private JCheckBox serializedMode;

	/** A panel allowing the user to define variables. */
	private ArgumentsPanel argsPanel;

    FileListPanel browseJar = null;

    /**
	 * Create a new TestPlanGui.
	 */
	public TestPlanGui() {
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
	public JPopupMenu createPopupMenu() {
		JPopupMenu pop = new JPopupMenu();
		JMenu addMenu = new JMenu(JMeterUtils.getResString("Add"));
		addMenu.add(MenuFactory.makeMenuItem(new ThreadGroupGui().getStaticLabel(), ThreadGroupGui.class.getName(),
				"Add"));
		addMenu.add(MenuFactory.makeMenu(MenuFactory.LISTENERS, "Add"));
		addMenu.add(MenuFactory.makeMenu(MenuFactory.CONFIG_ELEMENTS, "Add"));
		addMenu.add(MenuFactory.makeMenu(MenuFactory.ASSERTIONS, "Add"));
		addMenu.add(MenuFactory.makeMenu(MenuFactory.PRE_PROCESSORS, "Add"));
		addMenu.add(MenuFactory.makeMenu(MenuFactory.POST_PROCESSORS, "Add"));
		addMenu.add(MenuFactory.makeMenu(MenuFactory.TIMERS, "Add"));
		pop.add(addMenu);
		MenuFactory.addFileMenu(pop);
		return pop;
	}

	/* Implements JMeterGUIComponent.createTestElement() */
	public TestElement createTestElement() {
		TestPlan tp = new TestPlan();
		modifyTestElement(tp);
		return tp;
	}

	/* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
	public void modifyTestElement(TestElement plan) {
		super.configureTestElement(plan);
		if (plan instanceof TestPlan) {
			TestPlan tp = (TestPlan) plan;
			tp.setFunctionalMode(functionalMode.isSelected());
			tp.setSerialized(serializedMode.isSelected());
			tp.setUserDefinedVariables((Arguments) argsPanel.createTestElement());
            tp.setTestPlanClasspathArray(browseJar.getFiles());
		}
	}

	public String getLabelResource() {
		return "test_plan";
	}

	/**
	 * This is the list of menu categories this gui component will be available
	 * under. This implementation returns null, since the TestPlan appears at
	 * the top level of the tree and cannot be added elsewhere.
	 * 
	 * @return a Collection of Strings, where each element is one of the
	 *         constants defined in MenuFactory
	 */
	public Collection getMenuCategories() {
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
	public void configure(TestElement el) {
		super.configure(el);
		functionalMode.setSelected(((AbstractTestElement) el).getPropertyAsBoolean(TestPlan.FUNCTIONAL_MODE));

		serializedMode.setSelected(((AbstractTestElement) el).getPropertyAsBoolean(TestPlan.SERIALIZE_THREADGROUPS));

		if (el.getProperty(TestPlan.USER_DEFINED_VARIABLES) != null) {
			argsPanel.configure((Arguments) el.getProperty(TestPlan.USER_DEFINED_VARIABLES).getObjectValue());
		}
        browseJar.setFiles( ((TestPlan)el).getTestPlanClasspathArray() );
	}

	/**
	 * Create a panel allowing the user to define variables for the test.
	 * 
	 * @return a panel for user-defined variables
	 */
	private JPanel createVariablePanel() {
		argsPanel = new ArgumentsPanel(JMeterUtils.getResString("user_defined_variables"));

		return argsPanel;
	}

    protected Container createClassPathPanel() {
        browseJar = new FileListPanel(JMeterUtils.getResString("test_plan_classpath_browse"), ".jar");
        return browseJar;
    }

	/**
	 * Initialize the components and layout of this component.
	 */
	private void init() {
		setLayout(new BorderLayout(10, 10));
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);

		add(createVariablePanel(), BorderLayout.CENTER);

		VerticalPanel southPanel = new VerticalPanel();
		serializedMode = new JCheckBox(JMeterUtils.getResString("testplan.serialized"));
		southPanel.add(serializedMode);
		functionalMode = new JCheckBox(JMeterUtils.getResString("functional_mode"));
		southPanel.add(functionalMode);
		JTextArea explain = new JTextArea(JMeterUtils.getResString("functional_mode_explanation"));
		explain.setEditable(false);
		explain.setBackground(this.getBackground());
		southPanel.add(explain);
        southPanel.add(createClassPathPanel());

		add(southPanel, BorderLayout.SOUTH);
	}
}
