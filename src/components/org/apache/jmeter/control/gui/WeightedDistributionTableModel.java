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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.apache.jmeter.control.WeightedDistributionController;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;

/**
 * The Class WeightedDistributionTableModel. Provides Support for the GUI table
 */
class WeightedDistributionTableModel extends PowerTableModel {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -600418978315572279L;
    
    /** The Constant ENABLED_COLUMN. */
    protected static final int ENABLED_COLUMN = 0;
    
    /** The Constant ELEMENT_NAME_COLUMN. */
    protected static final int ELEMENT_NAME_COLUMN = 1;
    
    /** The Constant WEIGHT_COLUMN. */
    protected static final int WEIGHT_COLUMN = 2;
    
    /** The Constant EVAL_WEIGHT_COLUMN. */
    protected static final int EVAL_WEIGHT_COLUMN = 3;
    
    /** The Constant PERCENT_COLUMN. */
    protected static final int PERCENT_COLUMN = 4;
    
    /** The Constant HIDDEN_CHILD_NODE_IDX_COLUMN. */
    protected static final int HIDDEN_CHILD_NODE_IDX_COLUMN = 5;
    
    /** The Constant HIDDEN_COLUMN_WIDTH. */
    protected static final int HIDDEN_COLUMN_WIDTH = 0;
    
    /** The Constant NUMERIC_COLUMN_WIDTH. */
    protected static final int NUMERIC_COLUMN_WIDTH = 100;
    
    /** The Constant COLUMN_NAMES. */
    protected static final String[] COLUMN_NAMES = { "Enabled", "Element Name", "Weight", "Evaluates To", "Est. Prob.", null };
    
    /** The Constant INEDITABLE_COLUMNS. */
    protected static final int[] INEDITABLE_COLUMNS = { EVAL_WEIGHT_COLUMN, PERCENT_COLUMN, HIDDEN_CHILD_NODE_IDX_COLUMN };
    
    
    /** The Constant COLUMN_CLASSES. */
    @SuppressWarnings("rawtypes")
    protected static final Class[] COLUMN_CLASSES = { Boolean.class,
            String.class, String.class, String.class, Float.class, Integer.class };

    /**
     * Builds the weighted distribution table.
     *
     * @return the j table
     */
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
        table.getColumnModel().getColumn(EVAL_WEIGHT_COLUMN).setCellRenderer(new WeightedDistributionIneditableEvaluatedWeightRenderer());
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

    /**
     * Instantiates a new weighted distribution table model.
     */
    public WeightedDistributionTableModel() {
        super(COLUMN_NAMES, COLUMN_CLASSES);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.util.PowerTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return Arrays.binarySearch(INEDITABLE_COLUMNS, column) < 0;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.util.PowerTableModel#setValueAt(java.lang.Object, int, int)
     */
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
            String weight = (String) rowData[WeightedDistributionTableModel.WEIGHT_COLUMN];
            WeightedDistributionController wdc = (WeightedDistributionController) GuiPackage
                    .getInstance().getCurrentElement();
            TestElement testElem = ((JMeterTreeNode) wdc.getNode().getChildAt(
                    (int) rowData[WeightedDistributionTableModel.HIDDEN_CHILD_NODE_IDX_COLUMN]))
                    .getTestElement();
            testElem.setProperty(WeightedDistributionController.WEIGHT,
                    weight);
            TestElement evalTestElem = (TestElement) testElem.clone();
            
            
            try {
                GuiPackage.getInstance().getReplacer().replaceValues(evalTestElem);
            } catch (InvalidVariableException ive) {
                
            }
            evalTestElem.setRunningVersion(true);
            
            firingModel.setValueAt(evalTestElem.getPropertyAsString(WeightedDistributionController.WEIGHT), e.getFirstRow(), WeightedDistributionTableModel.EVAL_WEIGHT_COLUMN);

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
        List<String> weightData = (List<String>) firingModel.getColumnData(
                WeightedDistributionTableModel.COLUMN_NAMES[WeightedDistributionTableModel.EVAL_WEIGHT_COLUMN]);
        List<Boolean> enabledData = (List<Boolean>) firingModel.getColumnData(
                WeightedDistributionTableModel.COLUMN_NAMES[WeightedDistributionTableModel.ENABLED_COLUMN]);
        List<Float> probabilityData = new ArrayList<Float>(weightData.size());
        for (int i = 0; i < enabledData.size(); i++) {
            if (enabledData.get(i)) {
                String evalWeightStr = weightData.get(i);
                int evalWeightInt;
                try {
                    evalWeightInt = Integer.parseInt(evalWeightStr);
                } catch (NumberFormatException nfe) {
                    evalWeightInt = 0;
                }
                probabilityData
                        .add(wdc.calculateProbability(evalWeightInt));
            } else {
                probabilityData.add(wdc.calculateProbability(0));
            }
        }
        firingModel.setColumnData(WeightedDistributionTableModel.PERCENT_COLUMN,
                probabilityData);
    }
}

@SuppressWarnings("serial")
class WeightedDistributionIneditableEvaluatedWeightRenderer
        extends DefaultTableCellRenderer {
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        String evalWeight = (String)table.getModel().getValueAt(row, WeightedDistributionTableModel.EVAL_WEIGHT_COLUMN);
        boolean isEnabled = (boolean)table.getModel().getValueAt(row, WeightedDistributionTableModel.ENABLED_COLUMN);
        
        int intValue = -1;
        try {
            intValue = Integer.parseInt(evalWeight);
        } catch (NumberFormatException nfe) {
            // Negative numbers and unable to parse both display red
            intValue = -1;
        }
        
        if (intValue == 0 || !isEnabled) {
            setBackground(Color.LIGHT_GRAY);
            setForeground(Color.BLACK);
        } else if (intValue < 0) {
            setBackground(Color.RED);
            setForeground(Color.WHITE);
        } else {
            setBackground(Color.GREEN);
            setForeground(Color.BLACK);
        }
        
        return this;
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

