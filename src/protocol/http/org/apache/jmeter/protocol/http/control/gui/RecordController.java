package org.apache.jmeter.protocol.http.control.gui;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.control.gui.LogicControllerGui;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class RecordController extends LogicControllerGui
{
	
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("record_controller_title");
	}
}
