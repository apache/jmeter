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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.ModuleController;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.testelement.WorkBench;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * ModuleController Gui.
 *
 */
public class ModuleControllerGui extends AbstractControllerGui implements ActionListener
// implements UnsharedComponent
{

    private static final long serialVersionUID = 240L;

    private JMeterTreeNode selected = null;

    private final JComboBox nodes;

    private final DefaultComboBoxModel nodesModel;

    private final JLabel warningLabel;

    private JButton expandButton;

    /**
     * Initializes the gui panel for the ModuleController instance.
     */
    public ModuleControllerGui() {
        nodesModel = new DefaultComboBoxModel();
        nodes = new JComboBox(nodesModel);
        warningLabel = new JLabel(""); // $NON-NLS-1$
        init();
    }

    /** {@inheritDoc}} */
    @Override
    public String getLabelResource() {
        return "module_controller_title"; // $NON-NLS-1$
    }
    /** {@inheritDoc}} */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        ModuleController controller = (ModuleController) el;
        this.selected = controller.getSelectedNode();
        if (selected == null && controller.getNodePath() != null) {
            warningLabel.setText(JMeterUtils.getResString("module_controller_warning") // $NON-NLS-1$
                    + renderPath(controller.getNodePath()));
        } else {
            warningLabel.setText(""); // $NON-NLS-1$
        }
        reinitialize();
    }

    private String renderPath(Collection<?> path) {
        Iterator<?> iter = path.iterator();
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        while (iter.hasNext()) {
            if (first) {
                first = false;
                iter.next();
                continue;
            }
            buf.append(iter.next());
            if (iter.hasNext()) {
                buf.append(" > "); // $NON-NLS-1$
            }
        }
        return buf.toString();
    }

    /** {@inheritDoc}} */
    @Override
    public TestElement createTestElement() {
        ModuleController mc = new ModuleController();
        configureTestElement(mc);
        if (selected != null) {
            mc.setSelectedNode(selected);
        }
        return mc;
    }

    /** {@inheritDoc}} */
    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        TreeNodeWrapper tnw = (TreeNodeWrapper) nodesModel.getSelectedItem();
        if (tnw != null && tnw.getTreeNode() != null) {
            selected = tnw.getTreeNode();
            if (selected != null) {
                ((ModuleController) element).setSelectedNode(selected);
            }
        }
    }

    /** {@inheritDoc}} */
    @Override
    public void clearGui() {
        super.clearGui();

        nodes.setSelectedIndex(-1);
        selected = null;
    }


    /** {@inheritDoc}} */
    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        JMenu addMenu = MenuFactory.makeMenus(
                new String[] {
                        MenuFactory.CONFIG_ELEMENTS,
                        MenuFactory.ASSERTIONS,
                        MenuFactory.TIMERS,
                        MenuFactory.LISTENERS,
                },
                JMeterUtils.getResString("add"),  // $NON-NLS-1$
                ActionNames.ADD);
        menu.add(addMenu);
        MenuFactory.addEditMenu(menu, true);
        MenuFactory.addFileMenu(menu);
        return menu;
    }

    private void init() {
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
        setBorder(makeBorder());
        add(makeTitlePanel());

        // DROP-DOWN MENU
        JPanel modulesPanel = new JPanel();
        modulesPanel.setLayout(new BoxLayout(modulesPanel, BoxLayout.X_AXIS));
        JLabel nodesLabel = new JLabel(JMeterUtils.getResString("module_controller_module_to_run")); // $NON-NLS-1$
        modulesPanel.add(nodesLabel);
        modulesPanel.add(Box.createRigidArea(new Dimension(5,0)));
        nodesLabel.setLabelFor(nodes);
        reinitialize();
        modulesPanel.add(nodes);
        modulesPanel.add(warningLabel);

        modulesPanel.add(Box.createRigidArea(new Dimension(5,0)));
        expandButton = new JButton(JMeterUtils.getResString("expand")); //$NON-NLS-1$
        expandButton.addActionListener(this);
        modulesPanel.add(expandButton);
        add(modulesPanel);
    }

    private void reinitialize() {
        nodesModel.removeAllElements();
        GuiPackage gp = GuiPackage.getInstance();
        JMeterTreeNode root;
        if (gp != null) {
            root = (JMeterTreeNode) GuiPackage.getInstance().getTreeModel().getRoot();
            buildNodesModel(root, "", 0); // $NON-NLS-1$
        }
        if (selected != null) {
            TreeNodeWrapper current;
            for (int i = 0; i < nodesModel.getSize(); i++) {
                current = (TreeNodeWrapper) nodesModel.getElementAt(i);
                if (current.getTreeNode() != null && current.getTreeNode().equals(selected)) {
                    nodesModel.setSelectedItem(current);
                    break;
                }
            }
        }
    }

    private void buildNodesModel(JMeterTreeNode node, String parent_name, int level) {
        if (level == 0 && (parent_name == null || parent_name.length() == 0)) {
            nodesModel.addElement(new TreeNodeWrapper(null, "")); // $NON-NLS-1$
        }
        String separator = " > "; // $NON-NLS-1$
        if (node != null) {
            StringBuilder name = new StringBuilder();
            for (int i = 0; i < node.getChildCount(); i++) {
                name.setLength(0);
                JMeterTreeNode cur = (JMeterTreeNode) node.getChildAt(i);
                TestElement te = cur.getTestElement();
                if (te instanceof AbstractThreadGroup) {
                    name.append(parent_name);
                    name.append(cur.getName());
                    name.append(separator);
                    buildNodesModel(cur, name.toString(), level);
                } else if (te instanceof Controller && !(te instanceof ModuleController)) {
                    name.append(parent_name);
                    name.append(cur.getName());
                    TreeNodeWrapper tnw = new TreeNodeWrapper(cur, name.toString());
                    nodesModel.addElement(tnw);
                    name.append(separator);
                    buildNodesModel(cur, name.toString(), level + 1);
                } else if (te instanceof TestPlan || te instanceof WorkBench) {
                    name.append(cur.getName());
                    name.append(separator);
                    buildNodesModel(cur, name.toString(), 0);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==expandButton) {
            // reset previous result
            ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.SEARCH_RESET));
            JMeterTreeNode currentSelectedNode = null;
            TreeNodeWrapper tnw = (TreeNodeWrapper) nodesModel.getSelectedItem();
            if (tnw != null && tnw.getTreeNode() != null) {
                currentSelectedNode = tnw.getTreeNode();
            }
            if (currentSelectedNode != null) {
                expandToSelectNode(currentSelectedNode);
            }
            return;
        } 
    }

    /**
     * @param selected JMeterTreeNode tree node to expand
     */
    protected void expandToSelectNode(JMeterTreeNode selected) {
        GuiPackage guiInstance = GuiPackage.getInstance();
        JTree jTree = guiInstance.getMainFrame().getTree();
        jTree.expandPath(new TreePath(selected.getPath()));
        selected.setMarkedBySearch(true);
    }
}