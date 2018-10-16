package org.apache.jorphan.gui;

/**
 * Renders a min or max value and hides the extrema as they
 * are used for initialization of the values and will likely be interpreted as
 * random values.
 * <p>
 * {@link Long#MIN_VALUE} and {@link Long#MAX_VALUE} will be displayed as
 * {@code #N/A}.
 *
 */
public class MinMaxLongRenderer extends NumberRenderer { // NOSONAR

    private static final long serialVersionUID = 1L;

    public MinMaxLongRenderer(String format) {
        super(format);
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof Long) {
            long longValue = ((Long) value).longValue();
            if (!(longValue == Long.MAX_VALUE || longValue == Long.MIN_VALUE)) {
                setText(formatter.format(longValue));
                return;
            }
        }
        setText("#N/A");
    }
}
