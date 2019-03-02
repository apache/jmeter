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
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.apache.jmeter.control.TestFragmentController;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * This defines a simple Test Fragment GUI that can be used instead of a Thread Group
 * to allow for a non-execution part of the Test Plan that can be saved and referenced
 * by a Module or Include Controller.
 */

public class TestFragmentControllerGui extends AbstractControllerGui {

    private static final long serialVersionUID = 240L;

    public TestFragmentControllerGui() {
        init();
    }

    /**
     * Implements JMeterGUIComponent.createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        TestFragmentController controller = new TestFragmentController();
        setEnabled(false);
        modifyTestElement(controller);
        return controller;
    }

    /**
     * Implements JMeterGUIComponent.modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement controller) {
        configureTestElement(controller);
    }

    @Override
    public String getLabelResource() {
        return "test_fragment_title"; // $NON-NLS-1$
    }

    /**
     * Initialize the GUI components and layout for this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
    }

    /**
     * Over-ride this so that we add ourselves to the Test Fragment Category instead.
     */
    @Override
    public Collection<String> getMenuCategories() {
        return Arrays.asList(MenuFactory.FRAGMENTS);
    }

    /**
     * Redefined to remove change parent and inserrt parent menu
     * @see org.apache.jmeter.control.gui.AbstractControllerGui#createPopupMenu()
     */
    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu pop = new JPopupMenu();        
        JMenu addMenu = new JMenu(JMeterUtils.getResString("add")); // $NON-NLS-1$
        addMenu.add(MenuFactory.makeMenu(MenuFactory.CONTROLLERS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.CONFIG_ELEMENTS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.TIMERS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.PRE_PROCESSORS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.SAMPLERS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.POST_PROCESSORS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.ASSERTIONS, ActionNames.ADD));
        addMenu.add(MenuFactory.makeMenu(MenuFactory.LISTENERS, ActionNames.ADD));

        pop.add(addMenu);
        pop.add(MenuFactory.makeMenuItemRes("add_think_times",// $NON-NLS-1$
                ActionNames.ADD_THINK_TIME_BETWEEN_EACH_STEP));

        pop.add(MenuFactory.makeMenuItemRes("apply_naming",// $NON-NLS-1$
                ActionNames.APPLY_NAMING_CONVENTION));
        
        MenuFactory.addEditMenu(pop, true);
        MenuFactory.addFileMenu(pop);
        return pop;
    }
}
