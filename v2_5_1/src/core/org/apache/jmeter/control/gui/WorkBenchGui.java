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

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.apache.jmeter.gui.AbstractJMeterGuiComponent;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.util.JMeterUtils;

/**
 * JMeter GUI component representing a work bench where users can make
 * preparations for the test plan.
 *
 */
public class WorkBenchGui extends AbstractJMeterGuiComponent {
    private static final long serialVersionUID = 240L;

    /**
     * Create a new WorkbenchGui.
     */
    public WorkBenchGui() {
        super();
        init();
    }

    /**
     * This is the list of menu categories this gui component will be available
     * under. This implementation returns null, since the WorkBench appears at
     * the top level of the tree and cannot be added elsewhere.
     *
     * @return a Collection of Strings, where each element is one of the
     *         constants defined in MenuFactory
     */
    public Collection<String> getMenuCategories() {
        return null;
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    public TestElement createTestElement() {
        WorkBench wb = new WorkBench();
        modifyTestElement(wb);
        return wb;
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    public void modifyTestElement(TestElement wb) {
        super.configureTestElement(wb);
    }

    /**
     * When a user right-clicks on the component in the test tree, or selects
     * the edit menu when the component is selected, the component will be asked
     * to return a JPopupMenu that provides all the options available to the
     * user from this component.
     * <p>
     * The WorkBench returns a popup menu allowing you to add anything.
     *
     * @return a JPopupMenu appropriate for the component.
     */
    public JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenu addMenu = MenuFactory.makeMenus(new String[] {
                MenuFactory.NON_TEST_ELEMENTS,
                MenuFactory.CONTROLLERS,
                MenuFactory.CONFIG_ELEMENTS,
                MenuFactory.TIMERS,
                MenuFactory.PRE_PROCESSORS,
                MenuFactory.SAMPLERS,
                MenuFactory.POST_PROCESSORS,
                MenuFactory.ASSERTIONS,
                MenuFactory.LISTENERS,
                },
                JMeterUtils.getResString("add"), // $NON-NLS-1$
                ActionNames.ADD);
        menu.add(addMenu);
        MenuFactory.addPasteResetMenu(menu);
        MenuFactory.addFileMenu(menu);
        return menu;
    }

    public String getLabelResource() {
        return "workbench_title"; // $NON-NLS-1$
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
    }
}
