package org.apache.jmeter.control.gui.wdc;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * This class renders the Evaluated Weight column and sets the background color
 * as green/gray/red
 * 
 * NOTE:  This class was intended to be a internal class, but the requirement by
 * JMeterTest to instantiate all Serializable classes breaks unless this is public
 */
@SuppressWarnings("serial")
public class WeightedDistributionIneditableEvaluatedWeightRenderer
        extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
                row, column);

        // get the evaluated weight and enabled/disabled
        String evalWeight = (String) table.getModel().getValueAt(row,
                WeightedDistributionTableModel.EVAL_WEIGHT_COLUMN);
        boolean isEnabled = (boolean) table.getModel().getValueAt(row,
                WeightedDistributionTableModel.ENABLED_COLUMN);

        int intValue = -1;
        try {
            intValue = Integer.parseInt(evalWeight);
        } catch (NumberFormatException nfe) {
            // Negative numbers and unable to parse as int both display red
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
