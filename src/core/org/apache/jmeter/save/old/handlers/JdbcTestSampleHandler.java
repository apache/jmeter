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

import java.io.Writer;
import java.util.List;

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.save.old.Saveable;
import org.apache.jmeter.save.old.xml.TagHandler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.xml.sax.Attributes;

/************************************************************
 *  Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 *@author     Michael Stover
 *@created    $Date$
 *@version    1.0
 ***********************************************************/

public class JdbcTestSampleHandler extends TagHandler
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.jdbc");
	
	JDBCSampler sampler;

	/************************************************************
	 *  !ToDo (Constructor description)
	 ***********************************************************/
	public JdbcTestSampleHandler()
	{
	}

	/************************************************************
	 *  Gets the PrimaryTagName attribute of the GenericControllerHandler object
	 *
	 *@return    The PrimaryTagName value
	 ***********************************************************/
	public String getPrimaryTagName()
	{
		return "JdbcTestSample";
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  childObj  !ToDo (Parameter description)
	 ***********************************************************/
	public void notifySubElementEnded(Object childObj)
	{
		log.debug(childObj.toString());
		if (this.getMethodName().endsWith("defaultDb"))
		{
			retrieveDefaultDbConfig();
		}
		if (this.getMethodName().endsWith("defaultPool"))
		{
			retrieveDefaultPoolConfig();
		}
		if (this.getMethodName().endsWith("defaultSql"))
		{
			retrieveDefaultSqlConfig();
		}
	}

	/************************************************************
	 *  Description of the Method
	 ***********************************************************/
	public void defaultDbTagEnd()
	{
		retrieveDefaultDbConfig();
	}

	/************************************************************
	 *  !ToDo (Method description)
	 ***********************************************************/
	public void defaultPoolTagEnd()
	{
		List children = xmlParent.takeChildObjects(this);
		if (children.size() == 1)
		{
			sampler.addTestElement((TestElement)((TagHandler)children.get(0)).getModel());
		}
	}
	
	public void setAtts(Attributes atts) throws java.lang.Exception
	{
		sampler = new JDBCSampler();
		sampler.setProperty(TestElement.GUI_CLASS,"org.apache.jmeter.protocol.jdbc.control.gui.JdbcTestSampleGui");
		sampler.setName(atts.getValue("name"));

	}
	
	public Object getModel()
	{
		return sampler;
	}

	/************************************************************
	 *  Description of the Method
	 *
	 *@param  cont                     Description of Parameter
	 *@param  out                      Description of Parameter
	 *@exception  java.io.IOException  Description of Exception
	 ***********************************************************/
	public void save(Saveable cont, Writer out) throws java.io.IOException
	{
		/*JdbcTestSample controller = (JdbcTestSample)cont;
		writeMainTag(out, controller);
		writeDefaultDb(out, controller);
		writeDefaultPool(out, controller);
		writeDefaultSql(out, controller);
		writeSubElements(controller, out);
		out.write("</JdbcTestSample>\n");*/
	}

	/************************************************************
	 *  Description of the Method
	 *
	 *@param  out                      Description of Parameter
	 *@param  controller               Description of Parameter
	 *@exception  java.io.IOException  Description of Exception
	 ***********************************************************/
	protected void writeMainTag(Writer out, GenericController controller) throws java.io.IOException
	{
		/*out.write("<JdbcTestSample type=\"");
		out.write(JMeterHandler.convertToXML(controller.getClass().getName()));
		out.write("\" name=\"");
		out.write(JMeterHandler.convertToXML(controller.getName()));
		out.write("\">\n");*/
	}

	/************************************************************
	 *  Description of the Method
	 *
	 *@param  out                      Description of Parameter
	 *@param  controller               Description of Parameter
	 *@exception  java.io.IOException  Description of Exception
	 ***********************************************************/
	protected void writeDefaultDb(Writer out)
			 throws java.io.IOException
	{
		/*out.write("<defaultDb>\n");
		JMeterHandler.writeObject(controller.getDefaultDbConfig(), out);
		out.write("</defaultDb>\n");*/
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  out                      !ToDo (Parameter description)
	 *@param  controller               !ToDo (Parameter description)
	 *@exception  java.io.IOException  !ToDo (Exception description)
	 ***********************************************************/
	protected void writeDefaultPool(Writer out)
			 throws java.io.IOException
	{
		/*out.write("<defaultPool>\n");
		JMeterHandler.writeObject(controller.getDefaultPoolConfig(), out);
		out.write("</defaultPool>\n");*/
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  out                      !ToDo (Parameter description)
	 *@param  controller               !ToDo (Parameter description)
	 *@exception  java.io.IOException  !ToDo (Exception description)
	 ***********************************************************/
	protected void writeDefaultSql(Writer out) throws java.io.IOException
	{
		/*out.write("<defaultSql>\n");
		JMeterHandler.writeObject(controller.getDefaultSqlConfig(), out);
		out.write("</defaultSql>\n");*/
	}

	private void retrieveDefaultDbConfig()
	{
		List children = xmlParent.takeChildObjects(this);
		if (children.size() == 1)
		{
			sampler.addTestElement((TestElement)((TagHandler)children.get(0)).getModel());
		}
	}

	private void retrieveDefaultPoolConfig()
	{
		List children = xmlParent.takeChildObjects(this);
		if (children.size() == 1)
		{
			sampler.addTestElement((TestElement)((TagHandler)children.get(0)).getModel());
		}
	}

	private void retrieveDefaultSqlConfig()
	{
		List children = xmlParent.takeChildObjects(this);
		if (children.size() == 1)
		{
			sampler.addTestElement((TestElement)((TagHandler)children.get(0)).getModel());
		}
	}
}
