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
