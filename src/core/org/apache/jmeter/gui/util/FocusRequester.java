package org.apache.jmeter.gui.util;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

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
			e.printStackTrace();
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
