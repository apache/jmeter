package org.apache.jmeter.gui.action;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Logger;
import org.apache.jorphan.logging.LoggingManager;
/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class GlobalMouseListener extends MouseAdapter
{
	transient private static Logger log = LoggingManager.getLoggerFor(JMeterUtils.GUI);
	
	public void mousePressed(MouseEvent e)
	{
		log.debug("global mouse event");
	}
}
