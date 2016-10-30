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

package org.apache.jmeter.control.gui.wdc;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.jmeter.control.WeightedDistributionController;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;

/**
 * The Class WeightedDistributionTableModel. Provides Support for the control
 * panel table
 * 
 * NOTE:  This class was intended to be a internal class, but the requirement by
 * JMeterTest to instantiate all Serializable classes breaks unless this is public
 */
public class WeightedDistributionTableModel extends PowerTableModel {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -600418978315572279L;

    /**
     * The Constant ENABLED_COLUMN. Represents the column number of "Enabled"
     */
    protected static final int ENABLED_COLUMN = 0;

    /**
     * The Constant ELEMENT_NAME_COLUMN. Represents the column number of
     * "Element Name"
     */
    protected static final int ELEMENT_NAME_COLUMN = 1;

    /** The Constant WEIGHT_COLUMN. Represents the column number of "Weight" */
    protected static final int WEIGHT_COLUMN = 2;

    /**
     * The Constant EVAL_WEIGHT_COLUMN. Represents the column number of
     * "Evaluates To"
     */
    protected static final int EVAL_WEIGHT_COLUMN = 3;

    /**
     * The Constant PROBABILITY_COLUMN. Represents the column number of
     * "Est. Prob."
     */
    protected static final int PROBABILITY_COLUMN = 4;

    /**
     * The Constant HIDDEN_CHILD_NODE_IDX_COLUMN. Represents the column number
     * of a hidden column containing the index number of the child element
     * represented by this row
     */
    protected static final int HIDDEN_CHILD_NODE_IDX_COLUMN = 5;

    /**
     * The Constant HIDDEN_COLUMN_WIDTH. Represents the width of a hidden column
     */
    protected static final int HIDDEN_COLUMN_WIDTH = 0;

    /**
     * The Constant NUMERIC_COLUMN_WIDTH. Represents the width of a fixed width,
     * uneditable column
     */
    protected static final int FIXED_COLUMN_WIDTH = 100;

    /**
     * The Constant COLUMN_NAMES. Array of the property keys for the display
     * names for the various columns
     */
    protected static final String[] COLUMN_NAMES = {
            "weighted_distribution_controller_table_enabled",
            "weighted_distribution_controller_table_element_name",
            "weighted_distribution_controller_table_weight",
            "weighted_distribution_controller_table_eval_weight",
            "weighted_distribution_controller_table_probability", null };

    /**
     * The Constant INEDITABLE_COLUMNS. This is the set of columns that are not
     * editable
     */
    protected static final int[] INEDITABLE_COLUMNS = { EVAL_WEIGHT_COLUMN,
            PROBABILITY_COLUMN, HIDDEN_CHILD_NODE_IDX_COLUMN };

    /**
     * The Constant COLUMN_CLASSES. These are the classes that correspond to the
     * columns
     */
    @SuppressWarnings("rawtypes")
    protected static final Class[] COLUMN_CLASSES = { Boolean.class,
            String.class, String.class, String.class, Float.class,
            Integer.class };

    /**
     * Builds the weighted distribution table.
     *
     * @return the j table
     */
    static JTable buildWeightedDistributionTable() {

        TableModel tableModel = new WeightedDistributionTableModel();
        JTable table = new JTable(tableModel);

        // Header config
        table.getTableHeader()
                .setDefaultRenderer(new HeaderAsPropertyRenderer());
        Font defaultFont = table.getTableHeader().getFont();
        table.getTableHeader()
                .setFont(new Font("Bold", Font.BOLD, defaultFont.getSize()));

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Column config
        table.getColumnModel().getColumn(ENABLED_COLUMN)
                .setPreferredWidth(FIXED_COLUMN_WIDTH);
        table.getColumnModel().getColumn(ENABLED_COLUMN)
                .setMaxWidth(FIXED_COLUMN_WIDTH);
        table.getColumnModel().getColumn(ENABLED_COLUMN).setResizable(false);
        table.getColumnModel().getColumn(EVAL_WEIGHT_COLUMN).setCellRenderer(
                new WeightedDistributionIneditableEvaluatedWeightRenderer());
        table.getColumnModel().getColumn(PROBABILITY_COLUMN)
                .setPreferredWidth(FIXED_COLUMN_WIDTH);
        table.getColumnModel().getColumn(PROBABILITY_COLUMN)
                .setMaxWidth(FIXED_COLUMN_WIDTH);
        table.getColumnModel().getColumn(PROBABILITY_COLUMN)
                .setResizable(false);
        table.getColumnModel().getColumn(PROBABILITY_COLUMN).setCellRenderer(
                new WeightedDistributionIneditableProbabilityRenderer());
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

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.jmeter.gui.util.PowerTableModel#isCellEditable(int, int)
     */
    @Override
    public boolean isCellEditable(int row, int column) {
        return !ArrayUtils.contains(INEDITABLE_COLUMNS, column);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.apache.jmeter.gui.util.PowerTableModel#setValueAt(java.lang.Object,
     * int, int)
     */
    @Override
    public void setValueAt(Object aValue, int row, int column) {
        super.setValueAt(aValue, row, column);
        fireTableCellUpdated(row, column);
    }

}

/**
 * This class listens for changes to the editable columns and updates the table
 * or associated child test elements
 */
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
        WeightedDistributionController wdc = WeightedDistributionControllerGui
                .getCurrentWeightedDistributionController();
        if (wdc != null) {
            WeightedDistributionTableModel firingModel = (WeightedDistributionTableModel) e
                    .getSource();

            // get values from the row that was changed
            Object[] rowData = firingModel.getRowData(e.getFirstRow());
            int childIdx = (int) rowData[WeightedDistributionTableModel.HIDDEN_CHILD_NODE_IDX_COLUMN];
            boolean isEnabled = (boolean) rowData[WeightedDistributionTableModel.ENABLED_COLUMN];

            // get the associated child test element and set enabled to match
            // table
            wdc.getChildNode(childIdx).setEnabled(isEnabled);

            // Update the probability column to add/remove this element
            updateProbabilityColumn(firingModel);

            GuiPackage.getInstance().getMainFrame().repaint();
        }
    }

    private void handleElementNameChange(TableModelEvent e) {
        WeightedDistributionController wdc = WeightedDistributionControllerGui
                .getCurrentWeightedDistributionController();
        if (wdc != null) {
            WeightedDistributionTableModel firingModel = (WeightedDistributionTableModel) e
                    .getSource();

            // get values from the row that was changed
            Object[] rowData = firingModel.getRowData(e.getFirstRow());
            int childIdx = (int) rowData[WeightedDistributionTableModel.HIDDEN_CHILD_NODE_IDX_COLUMN];
            String elementName = (String) rowData[WeightedDistributionTableModel.ELEMENT_NAME_COLUMN];

            // set the name of the associated child element
            wdc.getChildNode(childIdx).setName(elementName);

            GuiPackage.getInstance().getMainFrame().repaint();
        }
    }

    private void handleWeightChange(TableModelEvent e) {
        WeightedDistributionController wdc = WeightedDistributionControllerGui
                .getCurrentWeightedDistributionController();
        if (wdc != null) {
            WeightedDistributionTableModel firingModel = (WeightedDistributionTableModel) e
                    .getSource();

            // get values from the row that was changed
            Object[] rowData = firingModel.getRowData(e.getFirstRow());
            int childIdx = (int) rowData[WeightedDistributionTableModel.HIDDEN_CHILD_NODE_IDX_COLUMN];
            String weight = (String) rowData[WeightedDistributionTableModel.WEIGHT_COLUMN];

            // update the weight property
            TestElement testElem = wdc.getChildTestElement(childIdx);
            testElem.setProperty(WeightedDistributionController.WEIGHT, weight);

            // update the evaluated weight column
            TestElement evalTestElem = wdc.evaluateTestElement(testElem);
            firingModel.setValueAt(
                    evalTestElem.getPropertyAsString(
                            WeightedDistributionController.WEIGHT),
                    e.getFirstRow(),
                    WeightedDistributionTableModel.EVAL_WEIGHT_COLUMN);

            // Update the probability column to reflect the new weight
            updateProbabilityColumn(firingModel);

            GuiPackage.getInstance().getMainFrame().repaint();
        }
    }

    @SuppressWarnings("unchecked")
    private void updateProbabilityColumn(
            WeightedDistributionTableModel firingModel) {
        WeightedDistributionController wdc = WeightedDistributionControllerGui
                .getCurrentWeightedDistributionController();
        if (wdc != null) {
            // get the data necessary for updating the probabilities
            List<String> weightData = (List<String>) firingModel.getColumnData(
                    WeightedDistributionTableModel.COLUMN_NAMES[WeightedDistributionTableModel.EVAL_WEIGHT_COLUMN]);
            List<Boolean> enabledData = (List<Boolean>) firingModel
                    .getColumnData(
                            WeightedDistributionTableModel.COLUMN_NAMES[WeightedDistributionTableModel.ENABLED_COLUMN]);

            List<Float> probabilityData = new ArrayList<Float>(
                    weightData.size());

            wdc.resetCumulativeProbability();

            for (int i = 0; i < enabledData.size(); i++) {
                // only determine values of enabled child elements
                if (enabledData.get(i)) {
                    // determine if the weight evaluates as an integer
                    // we cannot use the same approach as in
                    // WeightedDistributionController.configure(TestElement el)
                    // because we are using the raw data from the table rather
                    // than TestElement objects this is for performance reasons
                    String evalWeightStr = weightData.get(i);
                    int evalWeightInt;
                    // if value cannot be parsed as int, weight = 0
                    try {
                        evalWeightInt = Integer.parseInt(evalWeightStr);
                    } catch (NumberFormatException nfe) {
                        evalWeightInt = 0;
                    }
                    probabilityData
                            .add(wdc.calculateProbability(evalWeightInt));
                } else {
                    // disabled child elements have weight = 0
                    probabilityData.add(wdc.calculateProbability(0));
                }
            }
            firingModel.setColumnData(
                    WeightedDistributionTableModel.PROBABILITY_COLUMN,
                    probabilityData);
        }
    }
}
