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
import java.util.Iterator;
import java.util.List;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.proxy.ProxyControl;
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


public class ProxyControlHandler extends AbstractConfigElementHandler
{

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
		config = new ProxyControl();
		config.setProperty(TestElement.GUI_CLASS,"org.apache.jmeter.protocol.http.proxy.gui.ProxyControlGui");
		config.setProperty(TestElement.NAME,atts.getValue("name"));
	}

	/************************************************************
	 *  Gets the tag name that will trigger the use of this object's TagHandler.
	 *
	 *@return    The PrimaryTagName value
	 ***********************************************************/
	public String getPrimaryTagName()
	{
		return "ProxyControl";
	}

	/************************************************************
	 *  Called by reflection when the &lt;property&gt; tag is ended.
	 ***********************************************************/
	public void propertyTagEnd()
	{
		List children = xmlParent.takeChildObjects(this);
		if(children.size() == 1) {
			// Include or Exclude list, encapsulated as a set of Arguments
			Object model = ((TagHandler)children.get(0)).getModel();
			if (model instanceof Arguments) {
				Arguments args = (Arguments) model;
				List list = null;
				ProxyControl proxy = (ProxyControl) config;
				if (currentProperty.equals("includes")) {
					list = proxy.getIncludePatterns();
				} else if (currentProperty.equals("excludes")) {
					list = proxy.getExcludePatterns();
				}
				if (list != null) {
					for (Iterator iter = args.iterator(); iter.hasNext(); ) {
						Argument arg = (Argument) iter.next();
						list.add(arg.getValue());
					}
				}
			}
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
		ProxyControl saved = (ProxyControl)obj;
		String tagName = getPrimaryTagName();
		out.write("<" + tagName);
		out.write(" type=\"");
		out.write(JMeterHandler.convertToXML(saved.getClass().getName()));
		out.write("\"");
		out.write(">\n");

		for (Iterator iter = saved.getPropertyNames().iterator(); iter.hasNext(); ) {
			String key = (String)iter.next();
			Object value = saved.getProperty(key);
			writeProperty(out, key, value);
		}

		writePatterns(saved.getIncludePatterns(), "includes", out);
		writePatterns(saved.getExcludePatterns(), "excludes", out);

		out.write("</" + tagName + ">");
	}

	private void writePatterns(List list, String name, Writer out) throws IOException {
		Arguments args = new Arguments();
		int i = 0;
		for (Iterator iter = list.iterator(); iter.hasNext(); ) {
			String pattern = (String) iter.next();
			args.addArgument("arg" + i++, pattern);
		}
		writeProperty(out, name, args);
	}
}
