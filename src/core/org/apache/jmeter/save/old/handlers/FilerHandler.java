package org.apache.jmeter.save.old.handlers;

import java.io.Writer;

import org.apache.jmeter.reporters.ResultCollector;
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

public class FilerHandler extends TagHandler
{
	ResultCollector model;

	public FilerHandler()
	{
	}

	public void save(Saveable saveObject, Writer out) throws java.io.IOException
	{
 		/* protect ourselves from bad input.
		if ((saveObject == null) || (out == null))
		{
			// error!
		}
		else
		{
	 		Filer save = (Filer)saveObject;

			// validate filename
			String filename = save.getFile();
			if (filename == null)
			{
				System.err.println("Error. Missing filename in File Reporter.");
				filename = "";
			}

			out.write("<Filer type=\"");
			out.write(JMeterHandler.convertToXML(save.getClass().getName()));
			out.write("\" name=\"");
			out.write(JMeterHandler.convertToXML(save.getName()));
			out.write("\" verbose=\"");
			out.write(""+save.getVerbose());
			out.write("\" append=\"");
			out.write(""+save.getAppend());
			out.write("\" autoFlush=\"");
			out.write("" + save.getAutoFlush());
			out.write("\" viewSubmitData=\"");
			out.write("" + save.getViewSubmitData());
			out.write("\" file=\"");
			out.write(JMeterHandler.convertToXML(filename));
			out.write("\"/>");
		}*/
	}

	public void setAtts(Attributes atts) throws java.lang.Exception
	{
		model = new ResultCollector();
		model.setName(atts.getValue("name"));
		model.setFilename(atts.getValue("file"));
		model.setProperty(TestElement.GUI_CLASS,"org.apache.jmeter.visualizers.GraphVisualizer");

	}

	public String getPrimaryTagName()
	{
		return "Filer";
	}

	public Object getModel()
	{
		return model;
	}
}
