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

package org.apache.jmeter.gui.tree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.MainFrame;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.action.KeyStrokes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JMeterTreeListener implements TreeSelectionListener, MouseListener, KeyListener {
    private static final Logger log = LoggerFactory.getLogger(JMeterTreeListener.class);

    private TreePath currentPath;

    private ActionListener actionHandler;

    private JMeterTreeModel model;

    private JTree tree;

    /**
     * Constructor for the JMeterTreeListener object.
     *
     * @param model
     *            The {@link JMeterTreeModel} for this listener
     */
    public JMeterTreeListener(JMeterTreeModel model) {
        this.model = model;
    }

    /**
     * Constructor for the {@link JMeterTreeListener} object
     */
    public JMeterTreeListener() {
    }

    /**
     * Set the {@link JMeterTreeModel} for this listener
     * @param m The {@link JMeterTreeModel} to be used
     */
    public void setModel(JMeterTreeModel m) {
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
    public JMeterTreeNode getCurrentNode() {
        if (currentPath != null) {
            if (currentPath.getLastPathComponent() != null) {
                return (JMeterTreeNode) currentPath.getLastPathComponent();
            }
            return (JMeterTreeNode) currentPath.getParentPath().getLastPathComponent();
        }
        return (JMeterTreeNode) model.getRoot();
    }

    public JMeterTreeNode[] getSelectedNodes() {
        TreePath[] paths = tree.getSelectionPaths();
        if (paths == null) {
            return new JMeterTreeNode[] { getCurrentNode() };
        }
        JMeterTreeNode[] nodes = new JMeterTreeNode[paths.length];
        for (int i = 0; i < paths.length; i++) {
            nodes[i] = (JMeterTreeNode) paths[i].getLastPathComponent();
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
        // Call requestFocusInWindow to ensure current component loses focus and
        // all values are correctly saved
        // see https://bz.apache.org/bugzilla/show_bug.cgi?id=55103
        // see https://bz.apache.org/bugzilla/show_bug.cgi?id=55459
        tree.requestFocusInWindow();
        actionHandler.actionPerformed(new ActionEvent(this, 3333, ActionNames.EDIT)); // $NON-NLS-1$
    }

    @Override
    public void mouseClicked(MouseEvent ev) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        GuiPackage.getInstance().getMainFrame().repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Get the Main Frame.
        MainFrame mainFrame = GuiPackage.getInstance().getMainFrame();
        // Close any Main Menu that is open
        mainFrame.closeMenu();
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        if (tree.getPathForLocation(e.getX(), e.getY()) != null) {
            log.debug("mouse pressed, updating currentPath");
            currentPath = tree.getPathForLocation(e.getX(), e.getY());
        }
        if (selRow != -1 && isRightClick(e)) {
            if (tree.getSelectionCount() < 2) {
                tree.setSelectionPath(currentPath);
            }
            log.debug("About to display pop-up");
            displayPopUp(e);
        }
    }

    @Override
    public void mouseExited(MouseEvent ev) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        String actionName = null;

        if (KeyStrokes.matches(e, KeyStrokes.COPY)) {
            actionName = ActionNames.COPY;
        } else if (KeyStrokes.matches(e, KeyStrokes.PASTE)) {
            actionName = ActionNames.PASTE;
        } else if (KeyStrokes.matches(e, KeyStrokes.CUT)) {
            actionName = ActionNames.CUT;
        } else if (KeyStrokes.matches(e, KeyStrokes.DUPLICATE)) {
            actionName = ActionNames.DUPLICATE;
        } else if (KeyStrokes.matches(e, KeyStrokes.ALT_UP_ARROW)) {
            actionName = ActionNames.MOVE_UP;
        } else if (KeyStrokes.matches(e, KeyStrokes.ALT_DOWN_ARROW)) {
            actionName = ActionNames.MOVE_DOWN;
        } else if (KeyStrokes.matches(e, KeyStrokes.ALT_LEFT_ARROW)) {
            actionName = ActionNames.MOVE_LEFT;
        } else if (KeyStrokes.matches(e, KeyStrokes.ALT_RIGHT_ARROW)) {
            actionName = ActionNames.MOVE_RIGHT;
        } else if (KeyStrokes.matches(e, KeyStrokes.SHIFT_LEFT_ARROW)
                || KeyStrokes.matches(e, KeyStrokes.COLLAPSE_ALL_SUBTRACT)) {
            actionName = ActionNames.COLLAPSE;
        } else if (KeyStrokes.matches(e, KeyStrokes.SHIFT_RIGHT_ARROW)
                || KeyStrokes.matches(e, KeyStrokes.EXPAND_ALL_SUBTRACT)) {
            actionName = ActionNames.EXPAND;
        }

        if (actionName != null) {
            final ActionRouter actionRouter = ActionRouter.getInstance();
            actionRouter.doActionNow(new ActionEvent(e.getSource(), e.getID(), actionName));
            e.consume();
        }
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

    private void displayPopUp(MouseEvent e) {
        JPopupMenu pop = getCurrentNode().createPopupMenu();
        GuiPackage.getInstance().displayPopUp(e, pop);
    }
}
