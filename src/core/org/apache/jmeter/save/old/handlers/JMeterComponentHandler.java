package org.apache.jmeter.save.old.handlers;

import java.io.Writer;

import org.apache.jmeter.gui.JMeterComponentModel;
import org.apache.jmeter.save.old.Saveable;
import org.apache.jmeter.save.old.xml.TagHandler;
import org.apache.jmeter.testelement.TestElement;
import org.xml.sax.Attributes;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class JMeterComponentHandler extends TagHandler
{
	TestElement model;

	public JMeterComponentHandler()
	{
	}

	public void save(Saveable saveObject, Writer out) throws java.io.IOException
	{
		JMeterComponentModel save = (JMeterComponentModel)saveObject;
		out.write("<JMeterComponent type=\"");
		out.write(JMeterHandler.convertToXML(save.getClass().getName()));
		out.write("\" name=\"");
		out.write(JMeterHandler.convertToXML(save.getName()));
		out.write("\"/>");
	}

	public void setAtts(Attributes atts) throws java.lang.Exception
	{
		model = (TestElement)Class.forName(JMeterHandler.getComponentConversion(atts.getValue("type"))).newInstance();
		model.setProperty(TestElement.NAME,atts.getValue("name"));
		model.setProperty(TestElement.GUI_CLASS,JMeterHandler.getGuiClass(atts.getValue("type")));
	}

	public String getPrimaryTagName()
	{
		return "JMeterComponent";
	}

	public Object getModel()
	{
		return model;
	}
}