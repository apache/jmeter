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

package org.apache.jmeter.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import org.apache.jmeter.gui.action.UndoCommand;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class serves storing Test Tree state and navigating through it
 * to give the undo/redo ability for test plan changes
 * 
 * @since 2.12
 */
public class UndoHistory implements TreeModelListener, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Interface to be implemented by components interested in UndoHistory
     */
    public interface HistoryListener {
        void notifyChangeInHistory(UndoHistory history);
    }

    /**
     * Avoid storing too many elements
     *
     * @param <T> Class that should be held in this container
     */
    private static class LimitedArrayList<T> extends ArrayList<T> {
        /**
         *
         */
        private static final long serialVersionUID = -6574380490156356507L;
        private int limit;

        public LimitedArrayList(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean add(T item) {
            if (this.size() + 1 > limit) {
                this.remove(0);
            }
            return super.add(item);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(UndoHistory.class);

    /**
     * temporary storage for GUI tree expansion state
     */
    private ArrayList<Integer> savedExpanded = new ArrayList<>();

    /**
     * temporary storage for GUI tree selected row
     */
    private int savedSelected = 0;

    private static final int INITIAL_POS = -1;
    private int position = INITIAL_POS;

    private static final int HISTORY_SIZE = JMeterUtils.getPropDefault("undo.history.size", 0);

    private List<UndoHistoryItem> history = new LimitedArrayList<>(HISTORY_SIZE);

    /**
     * flag to prevent recursive actions
     */
    private boolean working = false;

    /**
     * History listeners
     */
    private List<HistoryListener> listeners = new ArrayList<>();

    public UndoHistory() {
    }

    /**
     * Clears the undo history
     */
    public void clear() {
        if (working) {
            return;
        }
        log.debug("Clearing undo history");
        history.clear();
        position = INITIAL_POS;
        notifyListeners();
    }

    /**
     * Add tree model copy to the history
     * <p>
     * This method relies on the rule that the record in history made AFTER
     * change has been made to test plan
     *
     * @param treeModel JMeterTreeModel
     * @param comment   String
     */
    public void add(JMeterTreeModel treeModel, String comment) {
        if(!isEnabled()) {
            log.debug("undo.history.size is set to 0, undo/redo feature is disabled");
            return;
        }

        // don't add element if we are in the middle of undo/redo or a big loading
        if (working) {
            log.debug("Not adding history because of noop");
            return;
        }

        JMeterTreeNode root = (JMeterTreeNode) treeModel.getRoot();
        if (root.getChildCount() < 1) {
            log.debug("Not adding history because of no children");
            return;
        }

        String name = root.getName();

        log.debug("Adding history element {}: {}", name, comment);

        working = true;
        // get test plan tree
        HashTree tree = treeModel.getCurrentSubTree((JMeterTreeNode) treeModel.getRoot());
        // first clone to not convert original tree
        tree = (HashTree) tree.getTree(tree.getArray()[0]).clone();

        position++;
        while (history.size() > position) {
            if (log.isDebugEnabled()) {
                log.debug("Removing further record, position: {}, size: {}", position, history.size());
            }
            history.remove(history.size() - 1);
        }

        // cloning is required because we need to immute stored data
        HashTree copy = UndoCommand.convertAndCloneSubTree(tree);

        history.add(new UndoHistoryItem(copy, comment));

        log.debug("Added history element, position: {}, size: {}", position, history.size());
        working = false;
        notifyListeners();
    }

    /**
     * Goes through undo history, changing GUI
     *
     * @param offset        the direction to go to, usually -1 for undo or 1 for redo
     * @param acceptorModel TreeModel to accept the changes
     */
    public void moveInHistory(int offset, JMeterTreeModel acceptorModel) {
        log.debug("Moving history from position {} with step {}, size is {}", position, offset, history.size());
        if (offset < 0 && !canUndo()) {
            log.warn("Can't undo, we're already on the last record");
            return;
        }

        if (offset > 0 && !canRedo()) {
            log.warn("Can't redo, we're already on the first record");
            return;
        }

        if (history.isEmpty()) {
            log.warn("Can't proceed, the history is empty");
            return;
        }

        position += offset;

        final GuiPackage guiInstance = GuiPackage.getInstance();

        // save tree expansion and selection state before changing the tree
        saveTreeState(guiInstance);

        // load the tree
        loadHistoricalTree(acceptorModel, guiInstance);

        // load tree UI state
        restoreTreeState(guiInstance);

        if (log.isDebugEnabled()) {
            log.debug("Current position {}, size is {}", position, history.size());
        }

        // refresh the all ui
        guiInstance.updateCurrentGui();
        guiInstance.getMainFrame().repaint();
        notifyListeners();
    }

    /**
     * Load the undo item into acceptorModel tree
     *
     * @param acceptorModel tree to accept the data
     * @param guiInstance {@link GuiPackage} to be used
     */
    private void loadHistoricalTree(JMeterTreeModel acceptorModel, GuiPackage guiInstance) {
        HashTree newModel = history.get(position).getTree();
        acceptorModel.removeTreeModelListener(this);
        working = true;
        try {
            guiInstance.getTreeModel().clearTestPlan();
            guiInstance.addSubTree(newModel);
        } catch (Exception ex) {
            log.error("Failed to load from history", ex);
        }
        acceptorModel.addTreeModelListener(this);
        working = false;
    }

    /**
     * @return true if remaining items
     */
    public boolean canRedo() {
        return position < history.size() - 1;
    }

    /**
     * @return true if not at first element
     */
    public boolean canUndo() {
        return position > INITIAL_POS + 1;
    }

    /**
     * Record the changes in the node as the undo step
     *
     * @param tme {@link TreeModelEvent} with event details
     */
    @Override
    public void treeNodesChanged(TreeModelEvent tme) {
        String name = ((JMeterTreeNode) tme.getTreePath().getLastPathComponent()).getName();
        log.debug("Nodes changed {}", name);
        final JMeterTreeModel sender = (JMeterTreeModel) tme.getSource();
        add(sender, "Node changed " + name);
    }

    /**
     * Record adding nodes as the undo step
     *
     * @param tme {@link TreeModelEvent} with event details
     */
    @Override
    public void treeNodesInserted(TreeModelEvent tme) {
        String name = ((JMeterTreeNode) tme.getTreePath().getLastPathComponent()).getName();
        log.debug("Nodes inserted {}", name);
        final JMeterTreeModel sender = (JMeterTreeModel) tme.getSource();
        add(sender, "Add " + name);
    }

    /**
     * Record deleting nodes as the undo step
     *
     * @param tme {@link TreeModelEvent} with event details
     */
    @Override
    public void treeNodesRemoved(TreeModelEvent tme) {
        String name = ((JMeterTreeNode) tme.getTreePath().getLastPathComponent()).getName();
        log.debug("Nodes removed: {}", name);
        add((JMeterTreeModel) tme.getSource(), "Remove " + name);
    }

    /**
     * Record some other change
     *
     * @param tme {@link TreeModelEvent} with event details
     */
    @Override
    public void treeStructureChanged(TreeModelEvent tme) {
        log.debug("Nodes struct changed");
        add((JMeterTreeModel) tme.getSource(), "Complex Change");
    }

    /**
     * Save tree expanded and selected state
     *
     * @param guiPackage {@link GuiPackage} to be used
     */
    private void saveTreeState(GuiPackage guiPackage) {
        savedExpanded.clear();

        MainFrame mainframe = guiPackage.getMainFrame();
        if (mainframe != null) {
            final JTree tree = mainframe.getTree();
            savedSelected = tree.getMinSelectionRow();

            for (int rowN = 0; rowN < tree.getRowCount(); rowN++) {
                if (tree.isExpanded(rowN)) {
                    savedExpanded.add(Integer.valueOf(rowN));
                }
            }
        }
    }

    /**
     * Restore tree expanded and selected state
     *
     * @param guiInstance GuiPackage to be used
     */
    private void restoreTreeState(GuiPackage guiInstance) {
        final JTree tree = guiInstance.getMainFrame().getTree();

        if (savedExpanded.size() > 0) {
            for (int rowN : savedExpanded) {
                tree.expandRow(rowN);
            }
        } else {
            tree.expandRow(0);
        }
        tree.setSelectionRow(savedSelected);
    }
    
    /**
     * @return true if history is enabled
     */
    public static boolean isEnabled() {
        return HISTORY_SIZE > 0;
    }
    
    /**
     * Register HistoryListener 
     * @param listener to add to our listeners
     */
    public void registerHistoryListener(HistoryListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Notify listener
     */
    private void notifyListeners() {
        for (HistoryListener listener : listeners) {
            listener.notifyChangeInHistory(this);
        }
    }

}
