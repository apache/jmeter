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

package org.apache.jmeter.modifiers.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.modifiers.UserParameters;
import org.apache.jmeter.processor.gui.AbstractPreProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GUIMenuSortOrder(5)
public class UserParametersGui extends AbstractPreProcessorGui {

    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(UserParametersGui.class);

    private static final String NAME_COL_RESOURCE = "name"; // $NON-NLS-1$
    private static final String USER_COL_RESOURCE = "user"; // $NON-NLS-1$
    private static final String UNDERSCORE = "_"; // $NON-NLS-1$

    private JTable paramTable;

    private PowerTableModel tableModel;

    private int numUserColumns = 1;

    private JButton addParameterButton;
    private JButton addUserButton;
    private JButton deleteRowButton;
    private JButton deleteColumnButton;
    private JButton moveRowUpButton;
    private JButton moveRowDownButton;

    private JCheckBox perIterationCheck;

    private JPanel paramPanel;

    public UserParametersGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "user_parameters_title"; // $NON-NLS-1$
    }

    @Override
    public void configure(TestElement el) {
        initTableModel();
        paramTable.setModel(tableModel);
        UserParameters params = (UserParameters) el;
        CollectionProperty names = params.getNames();
        CollectionProperty threadValues = params.getThreadLists();
        tableModel.setColumnData(0, (List<?>) names.getObjectValue());
        PropertyIterator iter = threadValues.iterator();
        if (iter.hasNext()) {
            tableModel.setColumnData(1, (List<?>) iter.next().getObjectValue());
        }
        int count = 2;
        while (iter.hasNext()) {
            String colName = getUserColName(count);
            tableModel.addNewColumn(colName, String.class);
            tableModel.setColumnData(count, (List<?>) iter.next().getObjectValue());
            count++;
        }
        setColumnWidths();
        perIterationCheck.setSelected(params.isPerIteration());
        super.configure(el);
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        UserParameters params = new UserParameters();
        modifyTestElement(params);
        return params;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement params) {
        GuiUtils.stopTableEditing(paramTable);
        UserParameters userParams = (UserParameters) params;
        userParams.setNames(new CollectionProperty(UserParameters.NAMES, tableModel.getColumnData(NAME_COL_RESOURCE)));
        CollectionProperty threadLists = new CollectionProperty(UserParameters.THREAD_VALUES, new ArrayList<>());
        log.debug("making threadlists from gui");
        for (int col = 1; col < tableModel.getColumnCount(); col++) {
            threadLists.addItem(tableModel.getColumnData(getUserColName(col)));
            if (log.isDebugEnabled()) {
                log.debug("Adding column to threadlist: {}", tableModel.getColumnData(getUserColName(col)));
                log.debug("Threadlists now = {}", threadLists);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("In the end, threadlists = {}", threadLists);
        }
        userParams.setThreadLists(threadLists);
        userParams.setPerIteration(perIterationCheck.isSelected());
        super.configureTestElement(params);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        initTableModel();
        paramTable.setModel(tableModel);
        HeaderAsPropertyRenderer defaultRenderer = new HeaderAsPropertyRenderer(){
            private static final long serialVersionUID = 240L;

            @Override
            protected String getText(Object value, int row, int column) {
                if (column >= 1){ // Don't process the NAME column
                    String val = value.toString();
                    if (val.startsWith(USER_COL_RESOURCE+UNDERSCORE)){
                        return JMeterUtils.getResString(USER_COL_RESOURCE)+val.substring(val.indexOf(UNDERSCORE));
                    }
                }
                return super.getText(value, row, column);
            }
        };
        paramTable.getTableHeader().setDefaultRenderer(defaultRenderer);
        perIterationCheck.setSelected(false);
    }

    private String getUserColName(int user){
        return USER_COL_RESOURCE+UNDERSCORE+user;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setBorder(makeBorder());
        setLayout(new BorderLayout());
        JPanel vertPanel = new VerticalPanel();
        vertPanel.add(makeTitlePanel());

        perIterationCheck = new JCheckBox(JMeterUtils.getResString("update_per_iter"), true); // $NON-NLS-1$
        Box perIterationPanel = Box.createHorizontalBox();
        perIterationPanel.add(perIterationCheck);
        perIterationPanel.add(Box.createHorizontalGlue());
        vertPanel.add(perIterationPanel);
        add(vertPanel, BorderLayout.NORTH);

        add(makeParameterPanel(), BorderLayout.CENTER);
    }

    private JPanel makeParameterPanel() {
        JLabel tableLabel = new JLabel(JMeterUtils.getResString("user_parameters_table")); // $NON-NLS-1$
        initTableModel();
        paramTable = new JTable(tableModel);
        paramTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        paramTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JMeterUtils.applyHiDPI(paramTable);

        paramPanel = new JPanel(new BorderLayout());
        paramPanel.add(tableLabel, BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(paramTable);
        scroll.setPreferredSize(scroll.getMinimumSize());
        paramPanel.add(scroll, BorderLayout.CENTER);
        paramPanel.add(makeButtonPanel(), BorderLayout.SOUTH);
        return paramPanel;
    }

    protected void initTableModel() {
        tableModel = new PowerTableModel(new String[] { NAME_COL_RESOURCE, // $NON-NLS-1$
                getUserColName(numUserColumns) }, new Class[] { String.class, String.class });
    }

    private JPanel makeButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2));
        addParameterButton = new JButton(JMeterUtils.getResString("add_parameter")); // $NON-NLS-1$
        addUserButton = new JButton(JMeterUtils.getResString("add_user")); // $NON-NLS-1$
        deleteRowButton = new JButton(JMeterUtils.getResString("delete_parameter")); // $NON-NLS-1$
        deleteColumnButton = new JButton(JMeterUtils.getResString("delete_user")); // $NON-NLS-1$
        moveRowUpButton = new JButton(JMeterUtils.getResString("up")); // $NON-NLS-1$
        moveRowDownButton = new JButton(JMeterUtils.getResString("down")); // $NON-NLS-1$
        buttonPanel.add(addParameterButton);
        buttonPanel.add(deleteRowButton);
        buttonPanel.add(moveRowUpButton);
        buttonPanel.add(addUserButton);
        buttonPanel.add(deleteColumnButton);
        buttonPanel.add(moveRowDownButton);
        addParameterButton.addActionListener(new AddParamAction());
        addUserButton.addActionListener(new AddUserAction());
        deleteRowButton.addActionListener(new DeleteRowAction());
        deleteColumnButton.addActionListener(new DeleteColumnAction());
        moveRowUpButton.addActionListener(new MoveRowUpAction());
        moveRowDownButton.addActionListener(new MoveRowDownAction());
        return buttonPanel;
    }

    /**
     * Set Column size
     */
    private void setColumnWidths() {
        int margin = 10;
        int minwidth = 150;

        JTableHeader tableHeader = paramTable.getTableHeader();
        FontMetrics headerFontMetrics = tableHeader.getFontMetrics(tableHeader.getFont());

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            int headerWidth = headerFontMetrics.stringWidth(paramTable.getColumnName(i));
            int maxWidth = getMaximalRequiredColumnWidth(i, headerWidth);

            paramTable.getColumnModel().getColumn(i).setPreferredWidth(Math.max(maxWidth + margin, minwidth));
        }
    }

    /**
     * Compute max width between width of the largest column at columnIndex and headerWidth
     * @param columnIndex Column index
     * @param headerWidth Header width based on Font
     */
    private int getMaximalRequiredColumnWidth(int columnIndex, int headerWidth) {
        int maxWidth = headerWidth;

        TableColumn column = paramTable.getColumnModel().getColumn(columnIndex);

        TableCellRenderer cellRenderer = column.getCellRenderer();

        if(cellRenderer == null) {
            cellRenderer = new DefaultTableCellRenderer();
        }

        for(int row = 0; row < paramTable.getModel().getRowCount(); row++) {
            Component rendererComponent = cellRenderer.getTableCellRendererComponent(paramTable,
                paramTable.getModel().getValueAt(row, columnIndex),
                false,
                false,
                row,
                columnIndex);

            double valueWidth = rendererComponent.getPreferredSize().getWidth();

            maxWidth = (int) Math.max(maxWidth, valueWidth);
        }

        return maxWidth;
    }

    private class AddParamAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            GuiUtils.stopTableEditing(paramTable);

            tableModel.addNewRow();
            tableModel.fireTableDataChanged();

            // Enable DELETE (which may already be enabled, but it won't hurt)
            deleteRowButton.setEnabled(true);

            // Highlight (select) the appropriate row.
            int rowToSelect = tableModel.getRowCount() - 1;
            paramTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        }
    }

    private class AddUserAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {

            GuiUtils.stopTableEditing(paramTable);

            tableModel.addNewColumn(getUserColName(tableModel.getColumnCount()), String.class);
            tableModel.fireTableDataChanged();

            setColumnWidths();
            // Enable DELETE (which may already be enabled, but it won't hurt)
            deleteColumnButton.setEnabled(true);

            // Highlight (select) the appropriate row.
            int colToSelect = tableModel.getColumnCount() - 1;
            paramTable.setColumnSelectionInterval(colToSelect, colToSelect);
        }
    }

    private class DeleteRowAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            GuiUtils.cancelEditing(paramTable);

            int[] rowsSelected = paramTable.getSelectedRows();
            if (rowsSelected.length > 0) {
                for (int i = rowsSelected.length - 1; i >= 0; i--) {
                    tableModel.removeRow(rowsSelected[i]);
                }
                tableModel.fireTableDataChanged();

                // Disable DELETE if there are no rows in the table to delete.
                if (tableModel.getRowCount() == 0) {
                    deleteRowButton.setEnabled(false);
                }
            } else {
                if(tableModel.getRowCount()>0) {
                    tableModel.removeRow(0);
                    tableModel.fireTableDataChanged();
                }
            }
        }
    }

    private class DeleteColumnAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            GuiUtils.cancelEditing(paramTable);

            int colSelected = paramTable.getSelectedColumn();
            if (colSelected == 0 || colSelected == 1) {
                JOptionPane.showMessageDialog(null,
                        JMeterUtils.getResString("column_delete_disallowed"), // $NON-NLS-1$
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (colSelected >= 0) {
                tableModel.removeColumn(colSelected);
                tableModel.fireTableDataChanged();

                // Disable DELETE if there are no rows in the table to delete.
                if (tableModel.getColumnCount() == 0) {
                    deleteColumnButton.setEnabled(false);
                }

                // Table still contains one or more rows, so highlight (select)
                // the appropriate one.
                else {

                    if (colSelected >= tableModel.getColumnCount()) {
                        colSelected = colSelected - 1;
                    }

                    paramTable.setColumnSelectionInterval(colSelected, colSelected);
                }
                setColumnWidths();
            }
        }
    }

    private class MoveRowUpAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int[] rowsSelected = paramTable.getSelectedRows();
            GuiUtils.stopTableEditing(paramTable);

            if (rowsSelected.length > 0 && rowsSelected[0] > 0) {
                for (int rowSelected : rowsSelected) {
                    tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected - 1);
                }

                for (int rowSelected : rowsSelected) {
                    paramTable.addRowSelectionInterval(rowSelected - 1, rowSelected - 1);
                }
            }
        }
    }

    private class MoveRowDownAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int[] rowsSelected = paramTable.getSelectedRows();
            GuiUtils.stopTableEditing(paramTable);

            if (rowsSelected.length > 0 && rowsSelected[rowsSelected.length - 1] < paramTable.getRowCount() - 1) {
                for (int i = rowsSelected.length - 1; i >= 0; i--) {
                    int rowSelected = rowsSelected[i];
                    tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected + 1);
                }
                for (int rowSelected : rowsSelected) {
                    paramTable.addRowSelectionInterval(rowSelected + 1, rowSelected + 1);
                }
            }
        }
    }
}
