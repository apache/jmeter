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

package org.apache.jmeter.protocol.jms.control.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.protocol.jms.sampler.JMSProperties;
import org.apache.jmeter.protocol.jms.sampler.JMSProperty;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles input for Jms Properties
 * @since 2.11
 */
public class JMSPropertiesPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = -2893899384410289131L;

    private static final Logger log = LoggerFactory.getLogger(JMSPropertiesPanel.class);

    private static final String ADD_COMMAND = "Add"; //$NON-NLS-1$

    private static final String DELETE_COMMAND = "Delete"; //$NON-NLS-1$

    private static final int COL_NAME = 0;
    private static final int COL_VALUE = 1;
    private static final int COL_TYPE = 2;

    private InnerTableModel tableModel;

    private JTable jmsPropertiesTable;

    private JButton addButton;

    private JButton deleteButton;


    /**
     * Default Constructor.
     */
    public JMSPropertiesPanel() {
        tableModel = new InnerTableModel();
        init();
    }

    public TestElement createTestElement() {
        JMSProperties jmsProperties = tableModel.jmsProperties;
        return (TestElement) jmsProperties.clone();
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @param el
     *            the test element to modify
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement el) {
        GuiUtils.stopTableEditing(jmsPropertiesTable);
        JMSProperties jmsProperties = (JMSProperties) el;
        jmsProperties.clear();
        jmsProperties.addTestElement((TestElement) tableModel.jmsProperties.clone());
    }

    /**
     * Clear GUI
     */
    public void clearGui() {
        tableModel.clearData();
        deleteButton.setEnabled(false);
    }

    /**
     * Configures GUI from el
     * @param el {@link TestElement}
     */
    public void configure(TestElement el) {
        tableModel.jmsProperties.clear();
        tableModel.jmsProperties.addTestElement((JMSProperties) el.clone());
        if (tableModel.getRowCount() != 0) {
            deleteButton.setEnabled(true);
        }
    }

    /**
     * Shows the main properties panel for this object.
     */
    private void init() {// called from ctor, so must not be overridable
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        add(createPropertiesPanel(), BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals(DELETE_COMMAND)) {
            if (tableModel.getRowCount() > 0) {
                // If a table cell is being edited, we must cancel the editing
                // before deleting the row.
                GuiUtils.cancelEditing(jmsPropertiesTable);

                int rowSelected = jmsPropertiesTable.getSelectedRow();

                if (rowSelected != -1) {
                    tableModel.removeRow(rowSelected);
                    tableModel.fireTableDataChanged();

                    // Disable the DELETE and SAVE buttons if no rows remaining
                    // after delete.
                    if (tableModel.getRowCount() == 0) {
                        deleteButton.setEnabled(false);
                    }

                    // Table still contains one or more rows, so highlight
                    // (select) the appropriate one.
                    else {
                        int rowToSelect = rowSelected;

                        if (rowSelected >= tableModel.getRowCount()) {
                            rowToSelect = rowSelected - 1;
                        }

                        jmsPropertiesTable.setRowSelectionInterval(rowToSelect, rowToSelect);
                    }
                }
            }
        } else if (action.equals(ADD_COMMAND)) {
            // If a table cell is being edited, we should accept the current
            // value and stop the editing before adding a new row.
            GuiUtils.stopTableEditing(jmsPropertiesTable);

            tableModel.addNewRow();
            tableModel.fireTableDataChanged();

            // Enable the DELETE and SAVE buttons if they are currently
            // disabled.
            if (!deleteButton.isEnabled()) {
                deleteButton.setEnabled(true);
            }

            // Highlight (select) the appropriate row.
            int rowToSelect = tableModel.getRowCount() - 1;
            jmsPropertiesTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        } 
    }

    public JPanel createPropertiesPanel() {
        // create the JTable that holds JMSProperty per row
        jmsPropertiesTable = new JTable(tableModel);
        JMeterUtils.applyHiDPI(jmsPropertiesTable);
        jmsPropertiesTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        jmsPropertiesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jmsPropertiesTable.setPreferredScrollableViewportSize(new Dimension(100, 70));

        
        TableColumn mechanismColumn = jmsPropertiesTable.getColumnModel().getColumn(COL_TYPE);
        mechanismColumn.setCellEditor(new TypeCellEditor());

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("jms_props"))); //$NON-NLS-1$
        panel.add(new JScrollPane(jmsPropertiesTable));
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JButton createButton(String resName, char mnemonic, String command, boolean enabled) {
        JButton button = new JButton(JMeterUtils.getResString(resName));
        button.setMnemonic(mnemonic);
        button.setActionCommand(command);
        button.setEnabled(enabled);
        button.addActionListener(this);
        return button;
    }

    private JPanel createButtonPanel() {
        boolean tableEmpty = tableModel.getRowCount() == 0;

        addButton = createButton("add", 'A', ADD_COMMAND, true); //$NON-NLS-1$
        deleteButton = createButton("delete", 'D', DELETE_COMMAND, !tableEmpty); //$NON-NLS-1$
       
        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        return buttonPanel;
    }

    private static class InnerTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 4638155137475747946L;
        final JMSProperties jmsProperties;

        public InnerTableModel() {
            jmsProperties = new JMSProperties();
        }

        public void addNewRow() {
            jmsProperties.addJmsProperty(new JMSProperty("","",String.class.getName()));
        }

        public void clearData() {
            jmsProperties.clear();
            fireTableDataChanged();
        }

        public void removeRow(int row) {
            jmsProperties.removeJmsProperty(row);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            // all table cells are editable
            return true;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }

        /**
         * Required by table model interface.
         */
        @Override
        public int getRowCount() {
            return jmsProperties.getJmsPropertyCount();
        }

        /**
         * Required by table model interface.
         */
        @Override
        public int getColumnCount() {
            return 3;
        }

        /**
         * Required by table model interface.
         */
        @Override
        public String getColumnName(int column) {
            switch(column) {
                case COL_NAME:
                    return "name";
                case COL_VALUE:
                    return "value";
                case COL_TYPE:
                    return "jms_properties_type";
                default:
                    return null;
            }
        }

        /**
         * Required by table model interface.
         */
        @Override
        public Object getValueAt(int row, int column) {
            JMSProperty property = jmsProperties.getJmsProperty(row);

            switch (column){
                case COL_NAME:
                    return property.getName();
                case COL_VALUE:
                    return property.getValue();
                case COL_TYPE:
                    return property.getType();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            JMSProperty property = jmsProperties.getJmsProperty(row);
            log.debug("Setting jms property value: {}", value);
            switch (column){
                case COL_NAME:
                    property.setName((String)value);
                    break;
                case COL_VALUE:
                    property.setValue((String) value);
                    break;
                case COL_TYPE:
                    property.setType((String) value);
                    break;
                default:
                    break;
            }
        }
    }
    
    private static class TypeCellEditor extends DefaultCellEditor {

        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public TypeCellEditor() {
            super(new JComboBox<>(new Object[]{
                    Boolean.class.getName(),
                    Byte.class.getName(),
                    Short.class.getName(),
                    Integer.class.getName(),
                    Long.class.getName(),
                    Float.class.getName(),
                    Double.class.getName(),
                    String.class.getName()
            }));
        }
    }
}
