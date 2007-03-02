package org.apache.jorphan.gui;

/**
 * Renders a rate in a JTable.
 * 
 * The output is in units appropriate to its dimension:
 * <p>
 * The number is represented in one of:
 * - requests/second
 * - requests/minute
 * - requests/hour.
 * <p>
 * Examples: "34.2/sec" "0.1/sec" "43.0/hour" "15.9/min"
 */
public class RateRenderer extends NumberRenderer{

	public RateRenderer(String format) {
		super(format);
	}
	
	public void setValue(Object value) {
		if (value == null || ! (value instanceof Double)) {
			setText("#N/A"); // TODO: should this just call super()?
			return;
		}
		double rate = ((Double) value).doubleValue();
		if (rate == Double.MAX_VALUE){
			setText("#N/A"); // TODO: should this just call super()?
			return;
		}
		
	    String unit = "sec";

	    if (rate < 1.0) {
	        rate *= 60.0;
	        unit = "min";
	    }
	    if (rate < 1.0) {
	        rate *= 60.0;
	        unit = "hour";
	    }			
	    setText(formatter.format(rate) + "/" + unit);
	}
}