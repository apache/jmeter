package org.apache.jmeter.gui.action;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version $Revision$
 */
public class GlobalMouseListener extends MouseAdapter
{
    transient private static Logger log =
        LoggingManager.getLoggerForClass();

    public void mousePressed(MouseEvent e)
    {
        log.debug("global mouse event");
    }
}
