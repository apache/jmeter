package org.apache.jmeter.gui.action;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.apache.jmeter.util.LoggingManager;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class GlobalMouseListener extends MouseAdapter
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(LoggingManager.GUI);
	
	public void mousePressed(MouseEvent e)
	{
		log.debug("global mouse event");
	}
}
