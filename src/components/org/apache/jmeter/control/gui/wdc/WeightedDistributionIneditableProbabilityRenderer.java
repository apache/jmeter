package org.apache.jmeter.control.gui.wdc;

import java.awt.Color;
import java.text.DecimalFormat;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * This class renders the Probability column with a grey background with a
 * nicely formatted percentage string
 * 
 * NOTE:  This class was intended to be a internal class, but the requirement by
 * JMeterTest to instantiate all Serializable classes breaks unless this is public
 */
@SuppressWarnings("serial")
public class WeightedDistributionIneditableProbabilityRenderer
        extends DefaultTableCellRenderer {
    private static final String FORMAT = "###.####%";
    private final DecimalFormat formatter = new DecimalFormat(FORMAT);

    public WeightedDistributionIneditableProbabilityRenderer() {
        super();
        setBackground(Color.LIGHT_GRAY);
        setHorizontalAlignment(RIGHT);
    }

    public void setValue(Object value) {
        setText((value == null) ? "" : formatter.format(value));
    }
}
