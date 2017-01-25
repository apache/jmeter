package org.apache.jmeter.gui.logging;

import java.util.EventObject;

public class LogEventObject extends EventObject {

    private static final long serialVersionUID = 1L;

    private String seralizedString;

    public LogEventObject(Object source, String seralizedString) {
        super(source);
        this.seralizedString = seralizedString;
    }

    @Override
    public String toString() {
        if (seralizedString != null) {
            return seralizedString;
        }

        return super.toString();
    }
}
