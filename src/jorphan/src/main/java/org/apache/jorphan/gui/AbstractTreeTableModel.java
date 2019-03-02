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

package org.apache.jorphan.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeNode;

import org.apache.jorphan.reflect.Functor;

public abstract class AbstractTreeTableModel extends DefaultTableModel implements TreeTableModel {

    private static final long serialVersionUID = 240L;

    protected final TreeNode rootNode;
    protected final EventListenerList listener = new EventListenerList();

    protected final transient List<Object> objects = new ArrayList<>();

    protected final transient List<String> headers = new ArrayList<>();

    protected final transient List<Class<?>> classes = new ArrayList<>();

    protected final transient List<Functor> readFunctors;

    protected final transient List<Functor> writeFunctors;

    public AbstractTreeTableModel(TreeNode root) {
        this.rootNode = root;
        readFunctors = new ArrayList<>();
        writeFunctors = new ArrayList<>();
    }

    public AbstractTreeTableModel(String[] headers,
            Functor[] readFunctors,
            Functor[] writeFunctors,
            Class<?>[] editorClasses) {
        this.rootNode = null;
        this.headers.addAll(Arrays.asList(headers));
        this.classes.addAll(Arrays.asList(editorClasses));
        this.readFunctors = new ArrayList<>(Arrays.asList(readFunctors));
        this.writeFunctors = new ArrayList<>(Arrays.asList(writeFunctors));
    }

    /**
     * The root node for the TreeTable
     * @return the root node
     */
    public Object getRootNode() {
        return this.rootNode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(Object node, int col) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(Object node, int col) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object val, Object node, int column) {
    }

    /**
     * The implementation is exactly the same as ObjectTableModel.getColumnCount.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int getColumnCount() {
        return headers.size();
    }

    /**
     * The implementation is exactly the same as ObjectTableModel.getRowCount.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public int getRowCount() {
        return objects.size();
    }

    /**
     * By default the abstract class returns true. It is up to subclasses
     * to override the implementation.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getColumnClass(int arg0) {
        return classes.get(arg0);
    }

    /**
     * Subclasses need to implement the logic for the method and
     * return the value at the specific cell.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getColumnName(int columnIndex) {
        return headers.get(columnIndex);
    }

    public int getChildCount(Object parent) {
        return 0;
    }

    public Object getChild(Object parent, int index) {
        return null;
    }

    /**
     * the implementation checks if the Object is a treenode. If it is, it
     * returns {@link TreeNode#isLeaf() isLeaf()}, otherwise it returns
     * <code>false</code>.
     *
     * @param node
     *            object to be checked for {@link TreeNode#isLeaf() isLeaf()}
     * @return <code>true</code> if object is a leaf node, <code>false</code>
     *         otherwise
     */
    public boolean isLeaf(Object node) {
        if (node instanceof TreeNode) {
            return ((TreeNode)node).isLeaf();
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addTableModelListener(TableModelListener l) {
        this.listener.add(TableModelListener.class,l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeTableModelListener(TableModelListener l) {
        this.listener.remove(TableModelListener.class,l);
    }

    public void nodeStructureChanged(TreeNode node) {

    }

    public void fireTreeNodesChanged(TreeNode source,
            Object[] path,
            int[] indexes,
            Object[] children) {

    }

    public void clearData() {
        int size = getRowCount();
        objects.clear();
        super.fireTableRowsDeleted(0, size);
    }
}
