package org.apache.jmeter.save.old.handlers;

import java.io.Writer;

import org.apache.jmeter.gui.JMeterComponentModel;
import org.apache.jmeter.save.old.Saveable;
import org.apache.jmeter.save.old.xml.TagHandler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.timers.Timer;
import org.xml.sax.Attributes;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class TimerHandler extends TagHandler
{
	Timer timer;

	public TimerHandler()
	{
	}

	public void save(Saveable parm1, Writer out) throws java.io.IOException
	{
		Timer timer = (Timer)parm1;
		out.write("<Timer type=\"");
		out.write(JMeterHandler.convertToXML(timer.getClass().getName()));
		out.write("\"");
		if(timer instanceof JMeterComponentModel)
		{
			out.write(" name=\"");
			out.write(JMeterHandler.convertToXML(((JMeterComponentModel)timer).getName()));
			out.write("\"");
		}
		out.write(">\n");
		out.write("<delay>");
		out.write(Long.toString(timer.getDelay()));
		out.write("</delay>\n");
		out.write("<range>");
		out.write(Double.toString(timer.getRange()));
		out.write("</range>\n");
		out.write("</Timer>\n");
	}

	public void setAtts(Attributes atts) throws java.lang.Exception
	{
		timer = (Timer)Class.forName(atts.getValue("type")).newInstance();
		((TestElement)timer).setProperty(TestElement.GUI_CLASS,JMeterHandler.getGuiClass(atts.getValue("type")));
		((TestElement)timer).setProperty(TestElement.NAME,atts.getValue("name"));
	}

	public void delay(String data)
	{
		try
		{
			timer.setDelay(Long.parseLong(data));
		}
		catch(NumberFormatException e)
		{
			timer.setDelay(0);
		}
	}

	public void range(String data)
	{
		try
		{
			timer.setRange(Double.parseDouble(data));
		}
		catch(NumberFormatException e)
		{
			timer.setRange(0);
		}
	}

	public String getPrimaryTagName()
	{
		return "Timer";
	}
	public Object getModel()
	{
		return timer;
	}
}