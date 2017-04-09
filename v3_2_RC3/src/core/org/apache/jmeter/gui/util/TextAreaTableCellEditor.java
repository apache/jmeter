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

package org.apache.jmeter.gui.util;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreeCellEditor;

public class TextAreaTableCellEditor extends AbstractCellEditor implements TableCellEditor, TreeCellEditor {
    private static final long serialVersionUID = 240L;

    //
    // Instance Variables
    //

    /** The Swing component being edited. */
    protected JTextArea editorComponent;

    /**
     * The delegate class which handles all methods sent from the
     * <code>CellEditor</code>.
     */
    protected EditorDelegate delegate;

    /**
     * An integer specifying the number of clicks needed to start editing. Even
     * if <code>clickCountToStart</code> is defined as zero, it will not
     * initiate until a click occurs.
     */
    protected int clickCountToStart = 1;

    //
    // Constructors
    //

    /**
     * Constructs a <code>TableCellEditor</code> that uses a text field.
     */
    public TextAreaTableCellEditor() {
        editorComponent = new JTextArea();
        editorComponent.setRows(3);
        this.clickCountToStart = 2;
        delegate = new EditorDelegate() {
            private static final long serialVersionUID = 240L;

            @Override
            public void setValue(Object value) {
                editorComponent.setText((value != null) ? value.toString() : "");
            }

            @Override
            public Object getCellEditorValue() {
                return editorComponent.getText();
            }
        };
        editorComponent.addFocusListener(delegate);
    }

    /**
     * Returns a reference to the editor component.
     *
     * @return the editor <code>Component</code>
     */
    public Component getComponent() {
        return editorComponent;
    }

    //
    // Modifying
    //

    /**
     * Specifies the number of clicks needed to start editing.
     *
     * @param count
     *            an int specifying the number of clicks needed to start editing
     * @see #getClickCountToStart
     */
    public void setClickCountToStart(int count) {
        clickCountToStart = count;
    }

    /**
     * Returns the number of clicks needed to start editing.
     *
     * @return the number of clicks needed to start editing
     */
    public int getClickCountToStart() {
        return clickCountToStart;
    }

    //
    // Override the implementations of the superclass, forwarding all methods
    // from the CellEditor interface to our delegate.
    //

    /**
     * Forwards the message from the <code>CellEditor</code> to the
     * <code>delegate</code>.
     *
     * @see EditorDelegate#getCellEditorValue
     */
    @Override
    public Object getCellEditorValue() {
        return delegate.getCellEditorValue();
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the
     * <code>delegate</code>.
     *
     * @see EditorDelegate#isCellEditable(EventObject)
     */
    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return delegate.isCellEditable(anEvent);
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the
     * <code>delegate</code>.
     *
     * @see EditorDelegate#shouldSelectCell(EventObject)
     */
    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return delegate.shouldSelectCell(anEvent);
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the
     * <code>delegate</code>.
     *
     * @see EditorDelegate#stopCellEditing
     */
    @Override
    public boolean stopCellEditing() {
        return delegate.stopCellEditing();
    }

    /**
     * Forwards the message from the <code>CellEditor</code> to the
     * <code>delegate</code>.
     *
     * @see EditorDelegate#cancelCellEditing
     */
    @Override
    public void cancelCellEditing() {
        delegate.cancelCellEditing();
    }

    //
    // Implementing the TreeCellEditor Interface
    //

    /** Implements the <code>TreeCellEditor</code> interface. */
    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded,
            boolean leaf, int row) {
        String stringValue = tree.convertValueToText(value, isSelected, expanded, leaf, row, false);

        delegate.setValue(stringValue);
        return new JScrollPane(editorComponent);
    }

    //
    // Implementing the CellEditor Interface
    //
    /** Implements the <code>TableCellEditor</code> interface. */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        delegate.setValue(value);
        return new JScrollPane(editorComponent);
    }

    //
    // Protected EditorDelegate class
    //

    /**
     * The protected <code>EditorDelegate</code> class.
     */
    protected class EditorDelegate implements FocusListener, Serializable {
        private static final long serialVersionUID = 240L;

        /** The value of this cell. */
        protected Object value;

        /**
         * Returns the value of this cell.
         *
         * @return the value of this cell
         */
        public Object getCellEditorValue() {
            return value;
        }

        /**
         * Sets the value of this cell.
         *
         * @param value
         *            the new value of this cell
         */
        public void setValue(Object value) {
            this.value = value;
        }

        /**
         * Returns true if <code>anEvent</code> is <b>not</b> a
         * <code>MouseEvent</code>. Otherwise, it returns true if the
         * necessary number of clicks have occurred, and returns false
         * otherwise.
         *
         * @param anEvent
         *            the event
         * @return true if cell is ready for editing, false otherwise
         * @see #setClickCountToStart(int)
         * @see #shouldSelectCell
         */
        public boolean isCellEditable(EventObject anEvent) {
            if (anEvent instanceof MouseEvent) {
                return ((MouseEvent) anEvent).getClickCount() >= clickCountToStart;
            }
            return true;
        }

        /**
         * Returns true to indicate that the editing cell may be selected.
         *
         * @param anEvent
         *            the event
         * @return true
         * @see #isCellEditable
         */
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        /**
         * Returns true to indicate that editing has begun.
         *
         * @param anEvent
         *            the event
         * @return always <code>true</code>
         */
        public boolean startCellEditing(EventObject anEvent) {
            return true;
        }

        /**
         * Stops editing and returns true to indicate that editing has stopped.
         * This method calls <code>fireEditingStopped</code>.
         *
         * @return true
         */
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }

        /**
         * Cancels editing. This method calls <code>fireEditingCanceled</code>.
         */
        public void cancelCellEditing() {
            fireEditingCanceled();
        }

        /**
         * When an action is performed, editing is ended.
         *
         * @param e
         *            the action event
         * @see #stopCellEditing
         */
        public void actionPerformed(ActionEvent e) {
            TextAreaTableCellEditor.this.stopCellEditing();
        }

        /**
         * When an item's state changes, editing is ended.
         *
         * @param e
         *            the action event
         * @see #stopCellEditing
         */
        public void itemStateChanged(ItemEvent e) {
            TextAreaTableCellEditor.this.stopCellEditing();
        }

        @Override
        public void focusLost(FocusEvent ev) {
            TextAreaTableCellEditor.this.stopCellEditing();
        }

        @Override
        public void focusGained(FocusEvent ev) {
        }
    }
}
