/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.jmeter.save.old.handlers;


import java.io.IOException;
import java.io.Writer;
import java.util.List;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.save.old.Saveable;
import org.apache.jmeter.save.old.xml.TagHandler;
import org.apache.jmeter.testelement.TestElement;
import org.xml.sax.Attributes;



/************************************************************
 *  Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author     Michael Stover
 *@created    June 9, 2001
 *@version    1.0
 ***********************************************************/


public class AbstractConfigElementHandler extends TagHandler
{

	protected TestElement config;

	protected String currentProperty;


	/************************************************************
	 *  Constructor for the AbstractConfigElementHandler object
	 ***********************************************************/
	public AbstractConfigElementHandler()
	{

	}


	/************************************************************
	 *  This is called when a tag is first encountered for this handler class to
	 *  handle. The attributes of the tag are passed, and the SaveHandler object is
	 *  expected to instantiate a new object.
	 *
	 *@param  atts           The new Atts value
	 *@exception  Exception  Description of Exception
	 ***********************************************************/

	public void setAtts(Attributes atts) throws Exception
	{

		config = (TestElement)Class.forName(JMeterHandler.getComponentConversion(atts.getValue("type"))).newInstance();
		config.setProperty(TestElement.TEST_CLASS,atts.getValue("type"));
		config.setProperty(TestElement.GUI_CLASS,JMeterHandler.getGuiClass(atts.getValue("type")));
		config.setProperty(TestElement.NAME,atts.getValue("name"));
	}


	/************************************************************
	 *  Returns the AbstractConfigElement object parsed from the XML. This method
	 *  is required to fulfill the SaveHandler interface. It is used by the XML
	 *  routines to gather all the saved objects.
	 *
	 *@return    The Model value
	 ***********************************************************/

	public Object getModel()
	{

		return config;
	}



	/************************************************************
	 *  Gets the tag name that will trigger the use of this object's TagHandler.
	 *
	 *@return    The PrimaryTagName value
	 ***********************************************************/

	public String getPrimaryTagName()
	{

		return "ConfigElement";
	}


	/************************************************************
	 *  Called by reflection when a &lt;property&gt; tag is encountered. Again, the
	 *  attributes are passed.
	 *
	 *@param  atts  Description of Parameter
	 ***********************************************************/

	public void property(Attributes atts)
	{

		currentProperty = atts.getValue("name");

	}


	/************************************************************
	 *  Called by reflection when text between the begin and end &lt;property&gt;
	 *  tag is encountered.
	 *
	 *@param  data  Description of Parameter
	 ***********************************************************/

	public void property(String data)
	{
		if(data != null && data.trim().length() > 0)
		{
			config.setProperty(JMeterHandler.convertProperty(currentProperty), data.trim());
			currentProperty = null;
		}
	}


	/************************************************************
	 *  Called by reflection when the &lt;property&gt; tag is ended.
	 ***********************************************************/

	public void propertyTagEnd()
	{
		List children = xmlParent.takeChildObjects(this);
		if(children.size() == 1)
		{
			config.setProperty(JMeterHandler.convertProperty(currentProperty), ((TagHandler)children.get(0)).getModel());
		}
	}


	/************************************************************
	 *  Tells the object to save itself to the given output stream.
	 *
	 *@param  obj              Description of Parameter
	 *@param  out              Description of Parameter
	 *@exception  IOException  Description of Exception
	 ***********************************************************/

	public void save(Saveable obj, Writer out) throws IOException
	{
		/*
		AbstractConfigElement saved = (AbstractConfigElement)obj;

		out.write("<ConfigElement type=\"");

		out.write(JMeterHandler.convertToXML(saved.getClass().getName()));

		out.write("\">\n");

		Iterator iter = saved.getPropertyNames().iterator();

		while(iter.hasNext())
		{

			String key = (String)iter.next();

			Object value = saved.getProperty(key);

			writeProperty(out, key, value);

		}

		out.write("</ConfigElement>");*/

	}


	/************************************************************
	 *  Routine to write each property to xml.
	 *
	 *@param  out              Description of Parameter
	 *@param  key              Description of Parameter
	 *@param  value            Description of Parameter
	 *@exception  IOException  Description of Exception
	 ***********************************************************/

	protected void writeProperty(Writer out, String key, Object value) throws IOException
	{

		out.write("<property name=\"");

		out.write(JMeterHandler.convertToXML(key));

		out.write("\">");

		JMeterHandler.writeObject(value, out);

		out.write("</property>\n");

	}

}
