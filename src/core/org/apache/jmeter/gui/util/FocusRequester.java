package org.apache.jmeter.gui.util;
import java.awt.Component;

import javax.swing.SwingUtilities;

import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 * Note: This helper class appeared in JavaWorld in June 2001
 * (http://www.javaworld.com) and was written by Michael Daconta.
 *
 *@author    Kevin Hammond
 *@created   xxxx
 *@version   xxxx
 ***************************************/

public class FocusRequester implements Runnable
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.gui");
	private Component comp;

	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param comp  !ToDo (Parameter description)
	 ***************************************/
	public FocusRequester(Component comp)
	{
		this.comp = comp;
		try
		{
			SwingUtilities.invokeLater(this);
		}
		catch(Exception e)
		{
			log.error("",e);
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	public void run()
	{
		comp.requestFocus();
	}
}
