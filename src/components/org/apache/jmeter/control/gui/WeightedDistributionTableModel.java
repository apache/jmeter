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

package org.apache.jmeter.control.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import org.apache.jmeter.control.WeightedDistributionController;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.util.JMeterUtils;

class WeightedDistributionTableModel extends PowerTableModel {
    private static final long serialVersionUID = -600418978315572279L;
    protected static final int ENABLED_COLUMN = 0;
    protected static final int ELEMENT_NAME_COLUMN = 1;
    protected static final int WEIGHT_COLUMN = 2;
    protected static final int PERCENT_COLUMN = 3;
    protected static final int HIDDEN_CHILD_NODE_IDX_COLUMN = 4;
    
    protected static final int HIDDEN_COLUMN_WIDTH = 0;
    protected static final int NUMERIC_COLUMN_WIDTH = 125;
    
    protected static final String[] COLUMN_NAMES = { "Enabled", "Element Name",
            String.format("Weight (%d-%d)",
                    WeightedDistributionController.MIN_WEIGHT,
                    WeightedDistributionController.MAX_WEIGHT),
            "Percentage", null };
    
    @SuppressWarnings("rawtypes")
    protected static final Class[] COLUMN_CLASSES = { Boolean.class,
            String.class, Integer.class, Float.class, Integer.class };

    static JTable buildWeightedDistributionTable() {
        TableModel tableModel = new WeightedDistributionTableModel();
        JTable table = new JTable(tableModel);
        Font defaultFont = table.getTableHeader().getFont();
        table.getTableHeader()
                .setFont(new Font("Bold", Font.BOLD, defaultFont.getSize()));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(ENABLED_COLUMN)
                .setPreferredWidth(NUMERIC_COLUMN_WIDTH);
        table.getColumnModel().getColumn(ENABLED_COLUMN)
                .setMaxWidth(NUMERIC_COLUMN_WIDTH);
        table.getColumnModel().getColumn(ENABLED_COLUMN).setResizable(false);
        table.getColumnModel().getColumn(WEIGHT_COLUMN)
                .setPreferredWidth(NUMERIC_COLUMN_WIDTH);
        table.getColumnModel().getColumn(WEIGHT_COLUMN)
                .setMaxWidth(NUMERIC_COLUMN_WIDTH);
        table.getColumnModel().getColumn(WEIGHT_COLUMN).setResizable(false);
        table.getColumnModel().getColumn(WEIGHT_COLUMN)
                .setCellEditor(new WeightedDistributionIntegerCellEditor());
        table.getColumnModel().getColumn(PERCENT_COLUMN)
                .setPreferredWidth(NUMERIC_COLUMN_WIDTH);
        table.getColumnModel().getColumn(PERCENT_COLUMN)
                .setMaxWidth(NUMERIC_COLUMN_WIDTH);
        table.getColumnModel().getColumn(PERCENT_COLUMN).setResizable(false);
        table.getColumnModel().getColumn(PERCENT_COLUMN).setCellRenderer(
                new WeightedDistributionIneditablePercentageRenderer());
        table.getColumnModel().getColumn(HIDDEN_CHILD_NODE_IDX_COLUMN)
                .setMinWidth(HIDDEN_COLUMN_WIDTH);
        table.getColumnModel().getColumn(HIDDEN_CHILD_NODE_IDX_COLUMN)
                .setMaxWidth(HIDDEN_COLUMN_WIDTH);
        table.getColumnModel().getColumn(HIDDEN_CHILD_NODE_IDX_COLUMN)
                .setResizable(false);
        table.getModel().addTableModelListener(
                new WeightedDistributionTableModelListener());

        return table;
    }

    public WeightedDistributionTableModel() {
        super(COLUMN_NAMES, COLUMN_CLASSES);
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return column != PERCENT_COLUMN
                && column != HIDDEN_CHILD_NODE_IDX_COLUMN;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        super.setValueAt(aValue, row, column);
        fireTableCellUpdated(row, column);
    }

}

class WeightedDistributionTableModelListener implements TableModelListener {

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getSource() instanceof WeightedDistributionTableModel
                && e.getType() == TableModelEvent.UPDATE) {
            switch (e.getColumn()) {
            case WeightedDistributionTableModel.ENABLED_COLUMN:
                handleEnabledChange(e);
                break;
            case WeightedDistributionTableModel.ELEMENT_NAME_COLUMN:
                handleElementNameChange(e);
                break;
            case WeightedDistributionTableModel.WEIGHT_COLUMN:
                handleWeightChange(e);
                break;
            default:
                break;
            }
        }
    }

    private void handleEnabledChange(TableModelEvent e) {
        if (WeightedDistributionControllerGui
                .isCurrentElementWeightedDistributionController()) {
            WeightedDistributionTableModel firingModel = (WeightedDistributionTableModel) e
                    .getSource();
            Object[] rowData = firingModel.getRowData(e.getFirstRow());
            boolean isEnabled = (boolean) rowData[WeightedDistributionTableModel.ENABLED_COLUMN];
            WeightedDistributionController wdc = (WeightedDistributionController) GuiPackage
                    .getInstance().getCurrentElement();
            ((JMeterTreeNode) wdc.getNode().getChildAt(
                    (int) rowData[WeightedDistributionTableModel.HIDDEN_CHILD_NODE_IDX_COLUMN]))
                            .setEnabled(isEnabled);
            updateProbabilityColumn(firingModel);
            GuiPackage.getInstance().getMainFrame().repaint();
        }
    }

    private void handleElementNameChange(TableModelEvent e) {
        WeightedDistributionTableModel firingModel = (WeightedDistributionTableModel) e
                .getSource();
        Object[] rowData = firingModel.getRowData(e.getFirstRow());
        ((JMeterTreeNode) GuiPackage.getInstance().getCurrentNode().getChildAt(
                (int) rowData[WeightedDistributionTableModel.HIDDEN_CHILD_NODE_IDX_COLUMN]))
                        .setName(
                                (String) rowData[WeightedDistributionTableModel.ELEMENT_NAME_COLUMN]);
        GuiPackage.getInstance().getMainFrame().repaint();
    }

    private void handleWeightChange(TableModelEvent e) {
        if (WeightedDistributionControllerGui
                .isCurrentElementWeightedDistributionController()) {
            WeightedDistributionTableModel firingModel = (WeightedDistributionTableModel) e
                    .getSource();
            Object[] rowData = firingModel.getRowData(e.getFirstRow());
            int weight = (int) rowData[WeightedDistributionTableModel.WEIGHT_COLUMN];
            WeightedDistributionController wdc = (WeightedDistributionController) GuiPackage
                    .getInstance().getCurrentElement();
            ((JMeterTreeNode) wdc.getNode().getChildAt(
                    (int) rowData[WeightedDistributionTableModel.HIDDEN_CHILD_NODE_IDX_COLUMN]))
                            .getTestElement()
                            .setProperty(WeightedDistributionController.WEIGHT,
                                    weight);
            updateProbabilityColumn(firingModel);
            GuiPackage.getInstance().getMainFrame().repaint();
        }
    }

    @SuppressWarnings("unchecked")
    private void updateProbabilityColumn(
            WeightedDistributionTableModel firingModel) {
        WeightedDistributionController wdc = (WeightedDistributionController) GuiPackage
                .getInstance().getCurrentElement();
        wdc.resetCumulativeProbability();
        List<Integer> weightData = (List<Integer>) firingModel.getColumnData(
                WeightedDistributionTableModel.COLUMN_NAMES[WeightedDistributionTableModel.WEIGHT_COLUMN]);
        List<Boolean> enabledData = (List<Boolean>) firingModel.getColumnData(
                WeightedDistributionTableModel.COLUMN_NAMES[WeightedDistributionTableModel.ENABLED_COLUMN]);
        List<Float> probabilityData = new ArrayList<Float>(weightData.size());
        for (int i = 0; i < weightData.size(); i++) {
            if (enabledData.get(i)) {
                probabilityData
                        .add(wdc.calculateProbability(weightData.get(i)));
            } else {
                probabilityData.add(wdc.calculateProbability(0));
            }
        }
        firingModel.setColumnData(WeightedDistributionTableModel.PERCENT_COLUMN,
                probabilityData);
    }
}

@SuppressWarnings("serial")
class WeightedDistributionIneditablePercentageRenderer
        extends DefaultTableCellRenderer {
    private static final String FORMAT = "###.####%";
    private final DecimalFormat formatter = new DecimalFormat(FORMAT);

    public WeightedDistributionIneditablePercentageRenderer() {
        super();
        setBackground(Color.LIGHT_GRAY);
        setHorizontalAlignment(RIGHT);
    }

    public void setValue(Object value) {
        setText((value == null) ? "" : formatter.format(value));
    }
}

@SuppressWarnings("serial")
class WeightedDistributionIntegerCellEditor extends DefaultCellEditor {

    JFormattedTextField ftf;
    NumberFormat intFormat = NumberFormat.getIntegerInstance();
    private boolean DEBUG = false;
    private static final String ERRMSG = String.format(
            "Valid values are integers between %d - %d",
            WeightedDistributionController.MIN_WEIGHT,
            WeightedDistributionController.MAX_WEIGHT);

    public WeightedDistributionIntegerCellEditor() {
        super(new JFormattedTextField());
        ftf = (JFormattedTextField) getComponent();
        NumberFormatter intFormatter = new NumberFormatter(intFormat);
        intFormatter.setMinimum(WeightedDistributionController.MIN_WEIGHT);
        intFormatter.setMaximum(WeightedDistributionController.MAX_WEIGHT);
        ftf.setFormatterFactory(new DefaultFormatterFactory(intFormatter));
        ftf.setValue(0);
        ftf.setHorizontalAlignment(JTextField.TRAILING);
        ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);

        // React when the user presses Enter while the editor is
        // active. (Tab is handled as specified by
        // JFormattedTextField's focusLostBehavior property.)
        ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                "check");
        ftf.getActionMap().put("check", new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!ftf.isEditValid()) { // The text is invalid.
                    JMeterUtils.reportErrorToUser(ERRMSG);
                    ftf.postActionEvent();
                    /*
                     * if (userSaysRevert()) { //reverted ftf.postActionEvent();
                     * //inform the editor }
                     */
                } else {
                    try { // The text is valid,
                        ftf.commitEdit(); // so use it.
                        ftf.postActionEvent(); // stop editing
                    } catch (java.text.ParseException exc) {
                        // TODO: something
                    }
                }
            }
        });
    }

    // Override to invoke setValue on the formatted text field.
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected, int row, int column) {
        JFormattedTextField ftf = (JFormattedTextField) super.getTableCellEditorComponent(
                table, value, isSelected, row, column);
        ftf.setValue(value);
        return ftf;
    }

    // Override to ensure that the value remains an Integer.
    public Object getCellEditorValue() {
        JFormattedTextField ftf = (JFormattedTextField) getComponent();
        Object o = ftf.getValue();
        if (o instanceof Integer) {
            return o;
        } else if (o instanceof Number) {
            return new Integer(((Number) o).intValue());
        } else {
            if (DEBUG) {
                System.out.println("getCellEditorValue: o isn't a Number");
            }
            try {
                return intFormat.parseObject(o.toString());
            } catch (ParseException exc) {
                System.err.println("getCellEditorValue: can't parse o: " + o);
                return null;
            }
        }
    }

    // Override to check whether the edit is valid,
    // setting the value if it is and complaining if
    // it isn't. If it's OK for the editor to go
    // away, we need to invoke the superclass's version
    // of this method so that everything gets cleaned up.
    public boolean stopCellEditing() {
        JFormattedTextField ftf = (JFormattedTextField) getComponent();
        if (ftf.isEditValid()) {
            try {
                ftf.commitEdit();
            } catch (java.text.ParseException exc) {
            }

        } else { // text is invalid
            JMeterUtils.reportErrorToUser(ERRMSG);
            return false;
            /*
             * if (!userSaysRevert()) { //user wants to edit return false;
             * //don't let the editor go away }
             */
        }
        return super.stopCellEditing();
    }
}
