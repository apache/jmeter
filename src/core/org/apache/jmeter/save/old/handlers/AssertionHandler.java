package org.apache.jmeter.save.old.handlers;

import java.io.IOException;
import java.io.Writer;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.save.old.SaveHandler;
import org.apache.jmeter.save.old.Saveable;
import org.apache.jmeter.save.old.xml.TagHandler;
import org.apache.jmeter.testelement.TestElement;
import org.xml.sax.Attributes;

/**
 * Title:        Jakarta-JMeter
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Apache
 * @author Michael Stover
 * @version 1.0
 */

public class AssertionHandler extends TagHandler implements SaveHandler
{

	ResponseAssertion model;

	public AssertionHandler() {
	}

	public String getPrimaryTagName()
	{
		return "assertion";
	}

	public void save(Saveable objectToSave, Writer out) throws IOException {
		ResponseAssertion saved = (ResponseAssertion)objectToSave;
		out.write("<");
		out.write(getPrimaryTagName());
		out.write(" name=\"");
		out.write(saved.getName());
		out.write("\" class=\"");
		out.write(saved.getClass().getName());
		out.write("\" testType=\"");
		out.write(""+saved.getTestType());
		out.write("\" testField=\"");
		out.write(saved.getTestField());
		out.write("\">");
		writeTestStrings(saved,out);
		out.write("\n</");
		out.write(getPrimaryTagName());
		out.write(">");
	}

	public Object getModel()
	{
		return model;
	}

	public void setAtts(Attributes atts) throws ClassNotFoundException,IllegalAccessException,InstantiationException
	{
		try {
			model = (ResponseAssertion)Class.forName(JMeterHandler.getComponentConversion(atts.getValue("class"))).newInstance();
			model.setName(atts.getValue("name"));
			model.setTestType(Integer.parseInt(atts.getValue("testType")));
			model.setTestField(JMeterHandler.convertProperty(atts.getValue("testField")));
			model.setProperty(TestElement.GUI_CLASS,JMeterHandler.getGuiClass(atts.getValue("class")));
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	public void testString(String data)
	{
		model.addTestString(data);
	}

	private void writeTestStrings(ResponseAssertion saved, Writer out) throws IOException
	{
		/*Iterator iter = saved.getTestStringList().iterator();
		while (iter.hasNext())
		{
			out.write("\n<testString>");
			out.write(JMeterHandler.convertToXML(iter.next().toString()));
			out.write("</testString>");
		}*/
	}
}