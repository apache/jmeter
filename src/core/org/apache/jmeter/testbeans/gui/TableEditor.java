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

package org.apache.jmeter.testbeans.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.CellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.gui.ClearGui;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table editor for TestBean GUI properties.
 * Currently only works for:
 * <ul>
 * <li>property type Collection of {@link String}s, where there is a single header entry</li>
 * </ul>
 */
public class TableEditor extends PropertyEditorSupport implements FocusListener,TestBeanPropertyEditor,TableModelListener, ClearGui {
    private static final Logger log = LoggerFactory.getLogger(TableEditor.class);

    /** 
     * attribute name for class name of a table row;
     * value must be java.lang.String, or a class which supports set and get/is methods for the property name.
     */
    public static final String CLASSNAME = "tableObject.classname"; // $NON-NLS-1$

    /** 
     * attribute name for table headers, value must be a String array.
     * If {@link #CLASSNAME} is java.lang.String, there must be only a single entry.
     */
    public static final String HEADERS = "table.headers"; // $NON-NLS-1$

    /** attribute name for property names within the {@link #CLASSNAME}, value must be String array */
    public static final String OBJECT_PROPERTIES = "tableObject.properties"; // $NON-NLS-1$

    private JTable table;
    private ObjectTableModel model;
    private Class<?> clazz;
    private PropertyDescriptor descriptor;
    private final JButton addButton;
    private final JButton clipButton;
    private final JButton removeButton;
    private final JButton clearButton;
    private final JButton upButton;
    private final JButton downButton;

    public TableEditor() {
        addButton = new JButton(JMeterUtils.getResString("add")); // $NON-NLS-1$
        addButton.addActionListener(new AddListener());
        clipButton = new JButton(JMeterUtils.getResString("add_from_clipboard")); // $NON-NLS-1$
        clipButton.addActionListener(new ClipListener());
        removeButton = new JButton(JMeterUtils.getResString("remove")); // $NON-NLS-1$
        removeButton.addActionListener(new RemoveListener());
        clearButton = new JButton(JMeterUtils.getResString("clear")); // $NON-NLS-1$
        clearButton.addActionListener(new ClearListener());
        upButton = new JButton(JMeterUtils.getResString("up")); // $NON-NLS-1$
        upButton.addActionListener(new UpListener());
        downButton = new JButton(JMeterUtils.getResString("down")); // $NON-NLS-1$
        downButton.addActionListener(new DownListener());
    }

    @Override
    public String getAsText() {
        return null;
    }

    @Override
    public Component getCustomEditor() {
        JComponent pane = makePanel();
        pane.doLayout();
        pane.validate();
        return pane;
    }

    private JComponent makePanel() {
        JPanel p = new JPanel(new BorderLayout());
        JScrollPane scroller = new JScrollPane(table);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        scroller.setMinimumSize(new Dimension(100, 70));
        scroller.setPreferredSize(scroller.getMinimumSize());
        p.add(scroller,BorderLayout.CENTER);
        JPanel south = new JPanel();
        south.add(addButton);
        south.add(clipButton);
        removeButton.setEnabled(false);
        south.add(removeButton);
        clearButton.setEnabled(false);
        south.add(clearButton);
        upButton.setEnabled(false);
        south.add(upButton);
        downButton.setEnabled(false);
        south.add(downButton);
        p.add(south,BorderLayout.SOUTH);
        return p;
    }

    @Override
    public Object getValue() {
        return model.getObjectList();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        //not interested in this method.
    }

    @Override
    public void setValue(Object value) {
        if(value != null) {
            model.setRows(convertCollection((Collection<?>)value));
        } else {
            model.clearData();
        }
        
        if(model.getRowCount()>0) {
            removeButton.setEnabled(true);
            clearButton.setEnabled(true);
        } else {
            removeButton.setEnabled(false);
            clearButton.setEnabled(false);
        }
        
        if(model.getRowCount()>1) {
            upButton.setEnabled(true);
            downButton.setEnabled(true);
        } else {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }
        
        this.firePropertyChange();
    }

    private Collection<Object> convertCollection(Collection<?> values) {
        List<Object> l = new LinkedList<>();
        for(Object obj : values) {
            if(obj instanceof TestElementProperty) {
                l.add(((TestElementProperty)obj).getElement());
            } else {
                l.add(obj);
            }
        }
        return l;
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    /**
     * For the table editor, the CLASSNAME attribute must simply be the name of the class of object it will hold
     * where each row holds one object.
     */
    @Override
    public void setDescriptor(PropertyDescriptor descriptor) {
        this.descriptor = descriptor;
        String value = (String)descriptor.getValue(CLASSNAME);
        if (value == null) {
            throw new RuntimeException("The Table Editor requires the CLASSNAME attribute be set - the name of the object to represent a row");
        }
        try {
            clazz = Class.forName(value);
            initializeModel();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find the CLASSNAME class "+ value, e);
        }
    }

    void initializeModel()
    {
        Object hdrs = descriptor.getValue(HEADERS);
        if (!(hdrs instanceof String[])) {
            throw new RuntimeException("attribute HEADERS must be a String array");
        }
        if (clazz == String.class) {
            model = new ObjectTableModel((String[]) hdrs, new Functor[0], new Functor[0], new Class[]{String.class});
        } else {
            Object value = descriptor.getValue(OBJECT_PROPERTIES);
            if (!(value instanceof String[])) {
                throw new RuntimeException("attribute OBJECT_PROPERTIES must be a String array");
            }
            List<String> props = Arrays.stream((String[]) value)
                    .map(StringUtils::capitalize)
                    .collect(Collectors.toList());
            Functor[] writers = createWriters(props);
            Functor[] readers = createReaders(clazz, props);
            Class<?>[] editors = getArgsForWriter(clazz, props);
            model = new ObjectTableModel((String[]) hdrs, readers, writers, editors);
        }
        model.addTableModelListener(this);
        table = new JTable(model);
        JMeterUtils.applyHiDPI(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addFocusListener(this);
    }

    private Functor[] createWriters(List<String> propNames) {
        return propNames.stream()
                .map(propName -> "set" + propName) // $NON-NLS-1$
                .map(Functor::new)
                .toArray(Functor[]::new);
    }

    private Functor[] createReaders(Class<?> c, List<String> propNames) {
        List<String> methodNames = Arrays.stream(c.getMethods())
                .map(Method::getName)
                .collect(Collectors.toList());
        return propNames.stream()
                .map(name -> methodNames.contains("get" + name) ? "get" + name : "is" + name)
                .map(Functor::new)
                .toArray(Functor[]::new);
    }

    private Class<?>[] getArgsForWriter(Class<?> c, List<String> propNames) {
        return propNames.stream()
                .map(propName -> Arrays.stream(c.getMethods())
                        .filter(m -> m.getName().equals("set" + propName)) // $NON-NLS-1$
                        .map(m -> m.getParameterTypes()[0])
                        .findFirst()
                        .orElse(null))
                .toArray(Class<?>[]::new);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        this.firePropertyChange();
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        final int editingRow = table.getEditingRow();
        final int editingColumn = table.getEditingColumn();
        CellEditor ce = null;
        if (editingRow != -1 && editingColumn != -1) {
            ce = table.getCellEditor(editingRow,editingColumn);
        }
        Component editor = table.getEditorComponent();
        if(ce != null && (editor == null || editor != e.getOppositeComponent())) {
            ce.stopCellEditing();
        } else if(editor != null) {
            editor.addFocusListener(this);
        }
        this.firePropertyChange();
    }

    private class AddListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                model.addRow(clazz.newInstance());
                
                removeButton.setEnabled(true);
                clearButton.setEnabled(true);
            } catch(Exception err) {
                log.error("The class type given to TableEditor was not instantiable.", err);
            }
        }
    }
    
    private class ClipListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String clipboardContent = GuiUtils.getPastedText();
                if(clipboardContent == null) {
                    return;
                }

                String[] clipboardLines = clipboardContent.split("\n"); // $NON-NLS-1$
                for (String clipboardLine : clipboardLines) {
                    String[] columns = clipboardLine.split("\t"); // $NON-NLS-1$

                    model.addRow(clazz.newInstance());
                    
                    for (int i=0; i < columns.length; i++) {
                        model.setValueAt(columns[i], model.getRowCount() - 1, i);
                    }
                }

                if(model.getRowCount()>1) {
                    upButton.setEnabled(true);
                    downButton.setEnabled(true);
                } else {
                    upButton.setEnabled(false);
                    downButton.setEnabled(false);
                }
            } catch (Exception err) {
                log.error("The class type given to TableEditor was not instantiable.", err);
            }
        }
    }

    private class RemoveListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int[] rows = table.getSelectedRows();
            for(int i=0; i<rows.length; i++){
              model.removeRow(rows[i]-i);
            }
            
            if(model.getRowCount()>1) {
                upButton.setEnabled(true);
                downButton.setEnabled(true);
            } else {
                upButton.setEnabled(false);
                downButton.setEnabled(false);
            }
        }
    }

    private class ClearListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            model.clearData();
            
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }
    }
    
    private class UpListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            GuiUtils.cancelEditing(table);

            int[] rowsSelected = table.getSelectedRows();
            if (rowsSelected.length > 0 && rowsSelected[0] > 0) {
                table.clearSelection();
                for (int rowSelected : rowsSelected) {
                    model.moveRow(rowSelected, rowSelected + 1, rowSelected - 1);
                }
                for (int rowSelected : rowsSelected) {
                    table.addRowSelectionInterval(rowSelected - 1, rowSelected - 1);
                }
            }            
        }
    }
    
    private class DownListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            GuiUtils.cancelEditing(table);
            
            int[] rowsSelected = table.getSelectedRows();
            if (rowsSelected.length > 0 && rowsSelected[rowsSelected.length - 1] < table.getRowCount() - 1) {
                table.clearSelection();
                for (int i = rowsSelected.length - 1; i >= 0; i--) {
                    int rowSelected = rowsSelected[i];
                    model.moveRow(rowSelected, rowSelected + 1, rowSelected + 1);
                }
                for (int rowSelected : rowsSelected) {
                    table.addRowSelectionInterval(rowSelected + 1, rowSelected + 1);
                }
            }
        }
    }

    @Override
    public void clearGui() {
        this.model.clearData();
    }
    
}
