package org.apache.jmeter.protocol.http.control.gui;

import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.protocol.http.control.RecordingController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

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
	
	public TestElement createTestElement()
	{
		RecordingController con = new RecordingController();
		this.configureTestElement(con);
		return con;
	}
}
