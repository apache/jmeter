package org.apache.jmeter.gui.action;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Logger;
import org.jorphan.logging.LoggingManager;
/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class ChangeLanguage implements Command
{
	private static final Set commands = new HashSet();
	public final static String CHANGE_LANGUAGE = "change_language";
	private Logger log = LoggingManager.getLoggerFor(JMeterUtils.GUI);
	
	static
	{
		commands.add(CHANGE_LANGUAGE);
	}
	/**
	 * @see org.apache.jmeter.gui.action.Command#doAction(ActionEvent)
	 */
	public void doAction(ActionEvent e)
	{
		Locale loc = new Locale(((Component)e.getSource()).getName(),"");
		log.debug("Changing language to "+loc.getLanguage());
		JMeterUtils.reinitializeLocale(loc);
	}
	/**
	 * @see org.apache.jmeter.gui.action.Command#getActionNames()
	 */
	public Set getActionNames()
	{
		return commands;
	}
}
