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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

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

    private static final long serialVersionUID = 1L;
    
    /**
     * Interface to be implemented by components interested in UndoHistory
     */
    public interface HistoryListener {
        void notifyChangeInHistory(UndoHistory history);
    }

    private static final Logger log = LoggerFactory.getLogger(UndoHistory.class);

    private static final int HISTORY_SIZE = JMeterUtils.getPropDefault("undo.history.size", 0);

    /** flag to prevent recursive actions */
    private boolean working = false;

    /** History listeners */
    private List<HistoryListener> listeners = new ArrayList<>();

    private final UndoManager manager = new UndoManager();

    private final Deque<SimpleCompoundEdit> transactions = new ArrayDeque<>();

    private UndoHistoryItem lastKnownState = null;

    public UndoHistory() {
        manager.setLimit(HISTORY_SIZE);
    }

    /**
     * Clears the undo history
     */
    public void clear() {
        if (working) {
            return;
        }
        log.debug("Clearing undo history");
        manager.discardAllEdits();
        if (isTransaction()) {
            if(log.isWarnEnabled()) {
                log.warn("Clearing undo history with {} unfinished transactions", transactions.size());
            }
            transactions.clear();
        }
        lastKnownState = null;
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

        // cloning is required because we need to immute stored data
        HashTree copy = UndoCommand.convertAndCloneSubTree(tree);

        GuiPackage guiPackage = GuiPackage.getInstance();
        //or maybe a Boolean?
        boolean dirty = guiPackage != null ? guiPackage.isDirty() : false;
        addEdit(new UndoHistoryItem(copy, comment, TreeState.from(guiPackage), dirty));

        working = false;
    }

    public void undo() {
        if (!canUndo()) {
            log.warn("Can't undo, we're already on the last record");
            return;
        }
        manager.undo();
    }

    public void redo() {
        if (!canRedo()) {
            log.warn("Can't redo, we're already on the first record");
            return;
        }
        manager.redo();
    }

    private void reload(UndoHistoryItem z) {
        final GuiPackage guiInstance = GuiPackage.getInstance();
        JMeterTreeModel acceptorModel = guiInstance.getTreeModel();

        try {
            // load the tree
            loadHistoricalTree(acceptorModel, guiInstance, z.getTree());
        } finally {
            // load tree UI state
            z.getTreeState().restore(guiInstance);
            guiInstance.setDirty(z.isDirty());
        }
        setLastKnownState(z);

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
    private void loadHistoricalTree(JMeterTreeModel acceptorModel, GuiPackage guiInstance, HashTree newModel) {
        acceptorModel.removeTreeModelListener(this);
        working = true;
        try {
            guiInstance.getTreeModel().clearTestPlan();
            guiInstance.addSubTree(newModel);
        } catch (Exception ex) {
            log.error("Failed to load from history", ex);
        } finally {
            acceptorModel.addTreeModelListener(this);
            working = false;
        }
    }

    /**
     * @return true if remaining items
     */
    public boolean canRedo() {
        return manager.canRedo();
    }

    /**
     * @return true if not at first element
     */
    public boolean canUndo() {
        return manager.canUndo();
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

    private void addEdit(UndoHistoryItem item) {
        if (lastKnownState != null) {
            GlobalUndoableEdit edit = new GlobalUndoableEdit(item, lastKnownState, this::reload);
            addEdit(edit);
        } else {
            log.debug("Skipping undo since there is no previous known state");
        }
        lastKnownState = item;
    }

    private void addEdit(UndoableEdit edit) {
        if (isTransaction()) {
            transactions.peek().addEdit(edit);
            //XXX: Add sanity checks for transactions depth and number of edits?
        } else {
            manager.addEdit(edit);
            notifyListeners();
        }
    }

    void endUndoTransaction() {
        if(!isEnabled()) {
            return;
        }
        if (!isTransaction()) {
            log.error("Undo transaction ended without beginning", new Exception());
            return;
        }
        SimpleCompoundEdit edit = transactions.pop();
        edit.end();
        if (!edit.isEmpty()) {
            addEdit(edit);
        }
    }

    void beginUndoTransaction() {
        if (isEnabled()) {
            transactions.add(new SimpleCompoundEdit());
        }
    }

    boolean isTransaction() {
        return !transactions.isEmpty();
    }

    private void setLastKnownState(UndoHistoryItem previous) {
        this.lastKnownState = previous;
    }

}
