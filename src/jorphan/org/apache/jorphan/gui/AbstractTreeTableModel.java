//$Header$
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

import javax.swing.event.TableModelListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.TreeNode;

import org.apache.jorphan.reflect.Functor;

/**
 * @author Peter Lin
 *
 */
public abstract class AbstractTreeTableModel extends DefaultTableModel implements TreeTableModel {

    protected TreeNode rootNode = null;
    protected EventListenerList listener = new EventListenerList();

    protected transient ArrayList objects = new ArrayList();

    protected transient List headers = new ArrayList();

    protected transient ArrayList classes = new ArrayList();

    protected transient ArrayList readFunctors = new ArrayList();

    protected transient ArrayList writeFunctors = new ArrayList();

    public AbstractTreeTableModel(TreeNode root) {
        this.rootNode = root;
    }
    
    public AbstractTreeTableModel(TreeNode root, boolean editable) {
        this.rootNode = root;
    }

    public AbstractTreeTableModel(String[] headers, 
            Functor[] readFunctors, 
            Functor[] writeFunctors, 
            Class[] editorClasses) {
        this.headers.addAll(Arrays.asList(headers));
        this.classes.addAll(Arrays.asList(editorClasses));
        this.readFunctors = new ArrayList(Arrays.asList(readFunctors));
        this.writeFunctors = new ArrayList(Arrays.asList(writeFunctors));
    }
    
    /**
     * The root node for the TreeTable
     * @return
     */
    public Object getRootNode() {
        return this.rootNode;
    }
    
	/* (non-Javadoc)
	 * @see org.apache.jorphan.gui.TreeTableModel#getValueAt(java.lang.Object, int)
	 */
	public Object getValueAt(Object node, int col) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.jorphan.gui.TreeTableModel#isCellEditable(java.lang.Object, int)
	 */
	public boolean isCellEditable(Object node, int col) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.jorphan.gui.TreeTableModel#setValueAt(java.lang.Object, java.lang.Object, int)
	 */
	public void setValueAt(Object val, Object node, int column) {
	}

    /**
     * The implementation is exactly the same as ObjectTableModel.getColumnCount.
     */
    public int getColumnCount() {
        return headers.size();
    }

    /**
     * The implementation is exactly the same as ObjectTableModel.getRowCount.
     */
    public int getRowCount() {
        if (objects == null) {
            return 0;
        }
        return objects.size();
    }

    /**
     * By default the abstract class returns true. It is up to subclasses
     * to override the implementation.
     */
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

    public Class getColumnClass(int arg0) {
        return (Class) classes.get(arg0);
    }

    /**
     * Subclasses need to implement the logic for the method and
     * return the value at the specific cell.
     */
	public Object getValueAt(int rowIndex, int columnIndex) {
        return null;
    }

    /**
     * 
     */
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        
    }

    /**
     * 
     */
	public String getColumnName(int columnIndex) {
        return (String) headers.get(columnIndex);
    }

    public int getChildCount(Object parent) {
        return 0;
    }
    
    public Object getChild(Object parent, int index) {
        return null;
    }
    
    /**
     * the implementation checks if the Object is a treenode. If it is,
     * it returns isLeaf(), otherwise it returns false.
     * @param node
     * @return
     */
    public boolean isLeaf(Object node) {
        if (node instanceof TreeNode) {
            return ((TreeNode)node).isLeaf();
        } else {
            return false;
        }
    }
    
    /**
     * 
     */
	public void addTableModelListener(TableModelListener l) {
        this.listener.add(TableModelListener.class,l);
	}

    /**
     * 
     */
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
