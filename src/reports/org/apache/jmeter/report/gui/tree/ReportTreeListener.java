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

package org.apache.jmeter.report.gui.tree;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.jmeter.control.gui.ReportGui;
import org.apache.jmeter.gui.ReportGuiPackage;
import org.apache.jmeter.gui.ReportMainFrame;
import org.apache.jmeter.report.gui.action.ReportDragNDrop;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public class ReportTreeListener implements TreeSelectionListener, MouseListener, KeyListener, MouseMotionListener {
    private static final Logger log = LoggingManager.getLoggerForClass();

    // Container endWindow;
    // JPopupMenu pop;
    private TreePath currentPath;

    private ActionListener actionHandler;

    private ReportTreeModel model;

    private JTree tree;

    private boolean dragging = false;

    private ReportTreeNode[] draggedNodes;

    private JLabel dragIcon = new JLabel(JMeterUtils.getImage("leafnode.gif"));

    /**
     * Constructor for the JMeterTreeListener object.
     */
    public ReportTreeListener(ReportTreeModel model) {
        this.model = model;
        dragIcon.validate();
        dragIcon.setVisible(true);
    }

    public ReportTreeListener() {
        dragIcon.validate();
        dragIcon.setVisible(true);
    }

    public void setModel(ReportTreeModel m) {
        model = m;
    }

    /**
     * Sets the ActionHandler attribute of the JMeterTreeListener object.
     *
     * @param ah
     *            the new ActionHandler value
     */
    public void setActionHandler(ActionListener ah) {
        actionHandler = ah;
    }

    /**
     * Sets the JTree attribute of the JMeterTreeListener object.
     *
     * @param tree
     *            the new JTree value
     */
    public void setJTree(JTree tree) {
        this.tree = tree;
    }

    /**
     * Sets the EndWindow attribute of the JMeterTreeListener object.
     *
     * @param window
     *            the new EndWindow value
     */
    public void setEndWindow(Container window) {
        // endWindow = window;
    }

    /**
     * Gets the JTree attribute of the JMeterTreeListener object.
     *
     * @return tree the current JTree value.
     */
    public JTree getJTree() {
        return tree;
    }

    /**
     * Gets the CurrentNode attribute of the JMeterTreeListener object.
     *
     * @return the CurrentNode value
     */
    public ReportTreeNode getCurrentNode() {
        if (currentPath != null) {
            if (currentPath.getLastPathComponent() != null) {
                return (ReportTreeNode) currentPath.getLastPathComponent();
            } else {
                return (ReportTreeNode) currentPath.getParentPath().getLastPathComponent();
            }
        } else {
            return (ReportTreeNode) model.getRoot();
        }
    }

    public ReportTreeNode[] getSelectedNodes() {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths == null) {
            return new ReportTreeNode[] { getCurrentNode() };
        }
        ReportTreeNode[] nodes = new ReportTreeNode[paths.length];
        for (int i = 0; i < paths.length; i++) {
            nodes[i] = (ReportTreeNode) paths[i].getLastPathComponent();
        }

        return nodes;
    }

    public TreePath removedSelectedNode() {
        currentPath = currentPath.getParentPath();
        return currentPath;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        log.debug("value changed, updating currentPath");
        currentPath = e.getNewLeadSelectionPath();
        actionHandler.actionPerformed(new ActionEvent(this, 3333, "edit"));
    }

    @Override
    public void mouseClicked(MouseEvent ev) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (dragging && isValidDragAction(draggedNodes, getCurrentNode())) {
            dragging = false;
            JPopupMenu dragNdrop = new JPopupMenu();
            JMenuItem item = new JMenuItem(JMeterUtils.getResString("insert_before")); // $NON-NLS-1$
            item.addActionListener(actionHandler);
            item.setActionCommand(ReportDragNDrop.INSERT_BEFORE);
            dragNdrop.add(item);
            item = new JMenuItem(JMeterUtils.getResString("insert_after")); // $NON-NLS-1$
            item.addActionListener(actionHandler);
            item.setActionCommand(ReportDragNDrop.INSERT_AFTER);
            dragNdrop.add(item);
            item = new JMenuItem(JMeterUtils.getResString("add_as_child")); // $NON-NLS-1$
            item.addActionListener(actionHandler);
            item.setActionCommand(ReportDragNDrop.ADD);
            dragNdrop.add(item);
            dragNdrop.addSeparator();
            item = new JMenuItem(JMeterUtils.getResString("cancel")); // $NON-NLS-1$
            dragNdrop.add(item);
            displayPopUp(e, dragNdrop);
        } else {
            ReportGuiPackage.getInstance().getMainFrame().repaint();
        }
        dragging = false;
    }

    public ReportTreeNode[] getDraggedNodes() {
        return draggedNodes;
    }

    /**
     * Tests if the node is being dragged into one of it's own sub-nodes, or
     * into itself.
     */
    private boolean isValidDragAction(ReportTreeNode[] source, ReportTreeNode dest) {
        boolean isValid = true;
        TreeNode[] path = dest.getPath();
        for (int i = 0; i < path.length; i++) {
            if (contains(source, path[i])) {
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    private void changeSelectionIfDragging(MouseEvent e) {
        if (dragging) {
            ReportGuiPackage.getInstance().getMainFrame().drawDraggedComponent(dragIcon, e.getX(), e.getY());
            if (tree.getPathForLocation(e.getX(), e.getY()) != null) {
                currentPath = tree.getPathForLocation(e.getX(), e.getY());
                if (!contains(draggedNodes, getCurrentNode())) {
                    tree.setSelectionPath(currentPath);
                }
            }
        }
    }

    private boolean contains(Object[] container, Object item) {
        for (int i = 0; i < container.length; i++) {
            if (container[i] == item) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Get the Main Frame.
        ReportMainFrame mainFrame = ReportGuiPackage.getInstance().getMainFrame();
        // Close any Main Menu that is open
        mainFrame.closeMenu();
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        if (tree.getPathForLocation(e.getX(), e.getY()) != null) {
            log.debug("mouse pressed, updating currentPath");
            currentPath = tree.getPathForLocation(e.getX(), e.getY());
        }
        if (selRow != -1) {
            // updateMainMenu(((JMeterGUIComponent)
            // getCurrentNode().getUserObject()).createPopupMenu());
            if (isRightClick(e)) {
                if (tree.getSelectionCount() < 2) {
                    tree.setSelectionPath(currentPath);
                }
                if (getCurrentNode() != null) {
                    log.debug("About to display pop-up");
                    displayPopUp(e);
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (!dragging) {
            dragging = true;
            draggedNodes = getSelectedNodes();
            if (draggedNodes[0].getUserObject() instanceof ReportGui) {
                dragging = false;
            }

        }
        changeSelectionIfDragging(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent ev) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    private boolean isRightClick(MouseEvent e) {
        return e.isPopupTrigger() || (InputEvent.BUTTON2_MASK & e.getModifiers()) > 0 || (InputEvent.BUTTON3_MASK == e.getModifiers());
    }

    /*
     * NOTUSED private void updateMainMenu(JPopupMenu menu) { try { MainFrame
     * mainFrame = GuiPackage.getInstance().getMainFrame();
     * mainFrame.setEditMenu(menu); } catch (NullPointerException e) {
     * log.error("Null pointer: JMeterTreeListener.updateMenuItem()", e);
     * log.error("", e); } }
     */

    private void displayPopUp(MouseEvent e) {
        JPopupMenu pop = getCurrentNode().createPopupMenu();
        ReportGuiPackage.getInstance().displayPopUp(e, pop);
    }

    private void displayPopUp(MouseEvent e, JPopupMenu popup) {
        log.warn("Shouldn't be here");
        if (popup != null) {
            popup.pack();
            popup.show(tree, e.getX(), e.getY());
            popup.setVisible(true);
            popup.requestFocusInWindow();
        }
    }
}
