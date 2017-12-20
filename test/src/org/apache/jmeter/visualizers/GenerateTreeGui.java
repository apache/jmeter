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

package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Workbench test element to create a test plan containing samples of each test element
 * (apart from Threads and Test Fragment).
 * <p>
 * The user creates a Thread Group, and the elements are created as child elements of
 * Simple Controllers.
 * <p>
 * Note: the code currently runs on all versions of JMeter back to 2.2.
 * Beware of making changes that rely on more recent APIs.
 */
public class GenerateTreeGui extends AbstractConfigGui
        implements ActionListener, UnsharedComponent {

    private static final long serialVersionUID = 1L;

    private JButton generateButton = new JButton("Generate");

    public GenerateTreeGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "test_plan"; // $NON-NLS-1$
    }

    @Override
    public String getStaticLabel() {
        return "Test Generator"; // $NON-NLS-1$
    }

    @Override
    public String getDocAnchor() {
        return super.getDocAnchor();
    }

    @Override
    public Collection<String> getMenuCategories() {
        return Arrays.asList(MenuFactory.NON_TEST_ELEMENTS);
    }

    @Override
    public void actionPerformed(ActionEvent action) {
        GuiPackage guiPackage = GuiPackage.getInstance();
        JMeterTreeModel treeModel = guiPackage.getTreeModel();
        JMeterTreeNode myTarget = findFirstNodeOfType(ThreadGroup.class, treeModel);
        if (myTarget == null) {
            JMeterUtils.reportErrorToUser("Cannot find Thread Group");
            return;
        }

        addElements(MenuFactory.CONTROLLERS,     "Controllers",     guiPackage, treeModel, myTarget);
        addElements(MenuFactory.SAMPLERS,        "Samplers",        guiPackage, treeModel, myTarget);
        addElements(MenuFactory.TIMERS,          "Timers",          guiPackage, treeModel, myTarget);
        addElements(MenuFactory.ASSERTIONS,      "Assertions",      guiPackage, treeModel, myTarget);
        addElements(MenuFactory.PRE_PROCESSORS,  "Pre Processors",  guiPackage, treeModel, myTarget);
        addElements(MenuFactory.POST_PROCESSORS, "Post Processors", guiPackage, treeModel, myTarget);
        addElements(MenuFactory.CONFIG_ELEMENTS, "Config Elements", guiPackage, treeModel, myTarget);
        addElements(MenuFactory.LISTENERS,       "Listeners",       guiPackage, treeModel, myTarget);
    }

    private void addElements(
            String menuKey, String title, GuiPackage guiPackage,
            JMeterTreeModel treeModel, JMeterTreeNode myTarget) {

        myTarget = addSimpleController(treeModel, myTarget, title);
        JPopupMenu jp = MenuFactory.makeMenu(menuKey, "").getPopupMenu();
        for (Component comp : jp.getComponents()) {
            JMenuItem item = (JMenuItem) comp;
            try {
                TestElement testElement = guiPackage.createTestElement(item.getName());
                addToTree(treeModel, myTarget, testElement);
            } catch (Exception e) {
                addSimpleController(treeModel, myTarget, item.getName()+" "+e.getMessage());
            }
        }
    }

    @Override
    public TestElement createTestElement() {
        TestElement el = new ConfigTestElement();
        modifyTestElement(el);
        return el;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
    }

    /**
     * Create a panel containing the title label for the table.
     *
     * @return a panel containing the title label
     */
    private Component makeLabelPanel() {
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ButtonGroup bg = new ButtonGroup();
        bg.add(generateButton);
        generateButton.addActionListener(this);
        labelPanel.add(generateButton);
        return labelPanel;
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(makeLabelPanel(), BorderLayout.NORTH);
        // Force a minimum table height of 70 pixels
        p.add(Box.createVerticalStrut(70), BorderLayout.WEST);
        add(p, BorderLayout.CENTER);
    }

    /**
     * Helper method to add a Simple Controller to contain the elements.
     * Called from Application Thread that needs to update GUI (JMeterTreeModel)
     *
     * @param model Test component tree model
     * @param node  Node in the tree where we will add the Controller
     * @param name  A name for the Controller
     * @return the new node
     */
    private JMeterTreeNode addSimpleController(JMeterTreeModel model, JMeterTreeNode node, String name) {
        final TestElement sc = new GenericController();
        sc.setProperty(TestElement.GUI_CLASS, LOGIC_CONTROLLER_GUI);
        sc.setProperty(TestElement.NAME, name); // Use old style
        return addToTree(model, node, sc);
    }

    private static class RunGUI implements Runnable {
        private final JMeterTreeModel model;
        private final JMeterTreeNode node;
        private final TestElement testElement;
        RunGUI(JMeterTreeModel model, JMeterTreeNode node, TestElement testElement) {
            super();
            this.model = model;
            this.node = node;
            this.testElement = testElement;
        }

        volatile JMeterTreeNode newNode;

        @Override
        public void run() {
            try {
                newNode = model.addComponent(testElement, node);
            } catch (IllegalUserActionException e) {
                 throw new Error(e);
            }
        }
    }

    private JMeterTreeNode addToTree(final JMeterTreeModel model,
            final JMeterTreeNode node, final TestElement sc) {
        RunGUI runnable = new RunGUI(model, node, sc);
        if(SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (InterruptedException | InvocationTargetException e) {
                throw new Error(e);
            }
        }
        return runnable.newNode;
    }

    private static final String LOGIC_CONTROLLER_GUI = LogicControllerGui.class.getName();

    /**
     * Finds the first enabled node of a given type in the tree.
     *
     * @param type      class of the node to be found
     * @param treeModel the tree to search in
     * @return the first node of the given type in the test component tree, or
     * <code>null</code> if none was found.
     */
    private JMeterTreeNode findFirstNodeOfType(Class<?> type, JMeterTreeModel treeModel) {
        return treeModel.getNodesOfType(type).stream()
                .filter(JMeterTreeNode::isEnabled)
                .findFirst()
                .orElse(null);
    }
}
