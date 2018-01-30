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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.jmeter.control.Controller;
import org.apache.jmeter.control.ModuleController;
import org.apache.jmeter.control.TestFragmentController;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.MenuFactory;
import org.apache.jmeter.gui.util.MenuInfo;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.threads.AbstractThreadGroup;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * ModuleControllerGui provides UI for configuring ModuleController element.
 * It contains filtered copy of test plan tree (called Module to run tree) 
 * in which target element can be specified by selecting node.
 * Allowed types of elements in Module to run tree:
 * - TestPlan - cannot be referenced
 * - AbstractThreadGroup - cannot be referenced
 * - Controller (except ModuleController)
 * - TestFragmentController
 *
 */
@GUIMenuSortOrder(MenuInfo.SORT_ORDER_DEFAULT+2)
public class ModuleControllerGui extends AbstractControllerGui implements ActionListener {
    private static final long serialVersionUID = -4195441608252523573L;

    private static final String SEPARATOR = " > ";

    private static final TreeNode[] EMPTY_TREE_NODES = new TreeNode[0];
    
    private JMeterTreeNode selected = null;
    
    /**
     * Model of a Module to run tree. It is a copy of a test plan tree. 
     * User object of each element is set to a correlated JMeterTreeNode element of a test plan tree. 
     */
    private final DefaultTreeModel moduleToRunTreeModel;
    
    /**
     * Module to run tree. Allows to select a module to be executed. Many ModuleControllers can reference
     * the same element. 
     */
    private final JTree moduleToRunTreeNodes;

    /**
     * Used in case if referenced element does not exist in test plan tree (after removing it for example)
     */
    private final JLabel warningLabel;

    /**
     * Helps navigating test plan
     */
    private JButton expandButton;

    /**
     * Use this to warn about no selectable controller
     */
    private boolean hasAtLeastOneController;

    /**
     * Initializes the gui panel for the ModuleController instance.
     */
    public ModuleControllerGui() {
        moduleToRunTreeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        moduleToRunTreeNodes = new JTree(moduleToRunTreeModel);
        moduleToRunTreeNodes.setCellRenderer(new ModuleControllerCellRenderer());
        
        // this custom TreeSelectionModel forbid the selection of some test elements (test plan, thread group, etc..)
        TreeSelectionModel tsm =  new DefaultTreeSelectionModel() {

            private static final long serialVersionUID = 4062816201792954617L;

            private boolean isSelectedPathAllowed(DefaultMutableTreeNode lastSelected) {
                JMeterTreeNode tn = null;
                if (lastSelected != null && lastSelected.getUserObject() instanceof JMeterTreeNode) {
                    tn = (JMeterTreeNode) lastSelected.getUserObject();
                }
                return tn != null && isTestElementAllowed(tn.getTestElement());
            }
            
            @Override
            public void setSelectionPath(TreePath path) {
                DefaultMutableTreeNode lastSelected = (DefaultMutableTreeNode) path.getLastPathComponent();
                
                if (isSelectedPathAllowed(lastSelected)) {
                    super.setSelectionPath(path);
                }
            }

            @Override
            public void setSelectionPaths(TreePath[] pPaths) {
                DefaultMutableTreeNode lastSelected = (DefaultMutableTreeNode) pPaths[pPaths.length-1].getLastPathComponent();
                if(isSelectedPathAllowed(lastSelected)) {
                    super.setSelectionPaths(pPaths);
                }
            }

            @Override
            public void addSelectionPath(TreePath path) {
                DefaultMutableTreeNode lastSelected = (DefaultMutableTreeNode) path.getLastPathComponent();
                if(isSelectedPathAllowed(lastSelected)) {
                    super.addSelectionPath(path);
                }
            }

            @Override
            public void addSelectionPaths(TreePath[] paths) {
                DefaultMutableTreeNode lastSelected = (DefaultMutableTreeNode) paths[paths.length-1].getLastPathComponent();
                if(isSelectedPathAllowed(lastSelected)) {
                    super.addSelectionPaths(paths);
                }
            }
        };
        tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        moduleToRunTreeNodes.setSelectionModel(tsm);
        
        ImageIcon image = JMeterUtils.getImage("warning.png");
        warningLabel = new JLabel("", image, SwingConstants.LEFT); // $NON-NLS-1$
        warningLabel.setForeground(Color.RED);
        Font font = warningLabel.getFont();
        warningLabel.setFont(new Font(font.getFontName(), Font.BOLD, (int)(font.getSize()*1.1)));
        warningLabel.setVisible(false);
        
        init();
        
        moduleToRunTreeNodes.addTreeSelectionListener(
                evt -> {
                    warningLabel.setVisible(false);
                    expandButton.setEnabled(true);    
                });
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
            warningLabel.setVisible(true);
            expandButton.setEnabled(false);
        } else {
            warningLabel.setVisible(false);
            expandButton.setEnabled(true);
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
                buf.append(SEPARATOR); // $NON-NLS-1$
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
        JMeterTreeNode tn = null;
        DefaultMutableTreeNode lastSelected =
                (DefaultMutableTreeNode) this.moduleToRunTreeNodes.getLastSelectedPathComponent();
        if (lastSelected != null && lastSelected.getUserObject() instanceof JMeterTreeNode) {
            tn = (JMeterTreeNode) lastSelected.getUserObject();
        }
        if (tn != null) {
            selected = tn;
            // prevent from selecting thread group or test plan elements
            if (isTestElementAllowed(selected.getTestElement())) {
                ((ModuleController) element).setSelectedNode(selected);
            }
        }
    }
    
    // check if a given test element can be selected as the target of a module controller
    private static boolean isTestElementAllowed(TestElement testElement) {
        return testElement != null
                && !(testElement instanceof AbstractThreadGroup)
                && !(testElement instanceof TestPlan);

    }

    /** {@inheritDoc}} */
    @Override
    public void clearGui() {
        super.clearGui();
        selected = null;
        hasAtLeastOneController = false;
    }

    /** {@inheritDoc}} */
    @Override
    public JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();
        MenuFactory.addEditMenu(menu, true);
        MenuFactory.addFileMenu(menu);
        return menu;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new VerticalLayout(5, VerticalLayout.BOTH, VerticalLayout.TOP));
        setBorder(makeBorder());
        add(makeTitlePanel());

        JPanel modulesPanel = new JPanel();
        
        expandButton = new JButton(JMeterUtils.getResString("find_target_element")); //$NON-NLS-1$
        expandButton.addActionListener(this);
        modulesPanel.add(expandButton);
        modulesPanel.setLayout(new BoxLayout(modulesPanel, BoxLayout.Y_AXIS));
        modulesPanel.add(Box.createRigidArea(new Dimension(0,5)));
        
        JLabel nodesLabel = new JLabel(JMeterUtils.getResString("module_controller_module_to_run")); // $NON-NLS-1$
        modulesPanel.add(nodesLabel);
        modulesPanel.add(warningLabel);
        add(modulesPanel);
        
        JPanel treePanel = new JPanel();
        treePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        treePanel.add(moduleToRunTreeNodes);
        add(treePanel);
    }

    /**
     * Recursively traverse module to run tree in order to find JMeterTreeNode element given by testPlanPath
     * in a DefaultMutableTreeNode tree
     * 
     * @param level - current search level
     * @param testPlanPath - path of a test plan tree element
     * @param parent - module to run tree parent element
     * 
     * @return path of a found element 
     */
    private TreeNode[] findPathInTreeModel(
            int level, TreeNode[] testPlanPath, DefaultMutableTreeNode parent)
    {
        if (level >= testPlanPath.length) {
            return EMPTY_TREE_NODES;
        }
        int childCount = parent.getChildCount();
        JMeterTreeNode searchedTreeNode = 
                (JMeterTreeNode) testPlanPath[level];

        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            JMeterTreeNode childUserObj = (JMeterTreeNode) child.getUserObject();

            if (childUserObj.equals(searchedTreeNode)) {
                if (level == (testPlanPath.length - 1)) {
                    return child.getPath();
                } else {
                    return findPathInTreeModel(level + 1, testPlanPath, child);
                }
            }
        }
        return EMPTY_TREE_NODES;
    }
    
    /**
     * Expand module to run tree to selected JMeterTreeNode and set selection path to it
     * @param selected - referenced module to run
     */
    private void focusSelectedOnTree(JMeterTreeNode selected)
    {
        TreeNode[] path = selected.getPath();
        TreeNode[] filteredPath = new TreeNode[path.length-1];
        
        //ignore first element of path - WorkBench, (why WorkBench is appearing in the path ???)
        for (int i = 1; i < path.length; i++){
            filteredPath[i-1] = path[i];
        }
        
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) moduleToRunTreeNodes.getModel().getRoot();
        //treepath of test plan tree and module to run tree cannot be compared directly - moduleToRunTreeModel.getPathToRoot()
        //custom method for finding an JMeterTreeNode element in DefaultMutableTreeNode have to be used 
        TreeNode[] dmtnPath = this.findPathInTreeModel(1, filteredPath, root);
        if (dmtnPath.length > 0) {
            TreePath treePath = new TreePath(dmtnPath);
            moduleToRunTreeNodes.setSelectionPath(treePath);
            moduleToRunTreeNodes.scrollPathToVisible(treePath);
        }
    }

    private void reinitialize() {
        ((DefaultMutableTreeNode) moduleToRunTreeModel.getRoot()).removeAllChildren();

        GuiPackage gp = GuiPackage.getInstance();
        JMeterTreeNode root;
        if (gp != null) {
            root = (JMeterTreeNode) GuiPackage.getInstance().getTreeModel().getRoot();
            buildTreeNodeModel(root, 0, null);
            moduleToRunTreeModel.nodeStructureChanged((TreeNode) moduleToRunTreeModel.getRoot());
        }
        if (selected != null) {            
            //expand Module to run tree to selected node and set selection path to it
            this.focusSelectedOnTree(selected);
        }
        if(!hasAtLeastOneController) {
            warningLabel.setText(JMeterUtils.getResString("module_controller_warning_no_controller"));
            warningLabel.setVisible(true);
        }
    }

    /**
     * Recursively build module to run tree. Only 4 types of elements are allowed to be added:
     * - All controllers except ModuleController
     * - TestPlan
     * - TestFragmentController
     * - AbstractThreadGroup
     * 
     * @param node - element of test plan tree
     * @param level - level of element in a tree
     * @param parent
     */
    private void buildTreeNodeModel(JMeterTreeNode node, int level,
            DefaultMutableTreeNode parent) {
        if (node != null) {
            for (int i = 0; i < node.getChildCount(); i++) {
                JMeterTreeNode cur = (JMeterTreeNode) node.getChildAt(i);
                TestElement te = cur.getTestElement();
                if (te instanceof TestFragmentController
                        || te instanceof AbstractThreadGroup
                        || (te instanceof Controller
                                && !(te instanceof ModuleController) && level > 0)) {
                    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(cur);
                    parent.add(newNode);
                    buildTreeNodeModel(cur, level + 1, newNode);
                    hasAtLeastOneController = 
                            hasAtLeastOneController || (
                                    te instanceof Controller && 
                                    !(te instanceof ModuleController || te instanceof AbstractThreadGroup));
                } else if (te instanceof TestPlan) {
                    ((DefaultMutableTreeNode) moduleToRunTreeModel.getRoot())
                            .setUserObject(cur);
                    buildTreeNodeModel(cur, level,
                            (DefaultMutableTreeNode) moduleToRunTreeModel.getRoot());
                }
            }
        }
    }

    /**
     * Implementation of Expand button - moves focus to a test plan tree element referenced by 
     * selected element in Module to run tree
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == expandButton) {
            JMeterTreeNode tn = null;
            DefaultMutableTreeNode selected = (DefaultMutableTreeNode) 
                    this.moduleToRunTreeNodes.getLastSelectedPathComponent(); 
            if (selected != null && selected.getUserObject() instanceof JMeterTreeNode){
                tn = (JMeterTreeNode) selected.getUserObject();
            }
            if (tn != null){
                TreePath treePath = new TreePath(tn.getPath());
                //changing selection in a test plan tree
                GuiPackage.getInstance().getTreeListener().getJTree()
                        .setSelectionPath(treePath);
                //expanding tree to make referenced element visible in test plan tree
                GuiPackage.getInstance().getTreeListener().getJTree()
                        .scrollPathToVisible(treePath);
            }
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
    
    /**
     * Renderer class for printing "module to run" tree 
     */
    private static class ModuleControllerCellRenderer extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 1129098620102526299L;
        
        /**
         * @see javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
         */
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                boolean leaf, int row, boolean hasFocus) {
            JMeterTreeNode node = (JMeterTreeNode)((DefaultMutableTreeNode) value).getUserObject();
            if (node != null){
                super.getTreeCellRendererComponent(tree, node.getName(), selected, expanded, leaf, row,
                        hasFocus);
                //print same icon as in test plan tree
                boolean enabled = node.isEnabled();
                ImageIcon icon = node.getIcon(enabled);
                if (icon != null) {
                    if (enabled) {
                        setIcon(icon);
                    } else {
                        setDisabledIcon(icon);
                    }
                }
                else if (!enabled) { // i.e. no disabled icon found
                    // Must therefore set the enabled icon so there is at least some  icon
                    icon = node.getIcon();
                    if (icon != null) {
                        setIcon(icon);
                    }
                }
            }
            return this;
        }
    }
}
