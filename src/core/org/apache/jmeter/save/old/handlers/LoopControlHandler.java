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

import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.save.old.Saveable;
import org.apache.jmeter.testelement.TestElement;
import org.xml.sax.Attributes;

/************************************************************
 *  Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author     Michael Stover
 *@created    $Date$
 *@version    1.0
 ***********************************************************/

public class LoopControlHandler extends GenericControllerHandler
{

	/************************************************************
	 *  !ToDo (Constructor description)
	 ***********************************************************/
	public LoopControlHandler()
	{
		super();
	}

	public String getPrimaryTagName()
	{
		return "LoopController";
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  atts                     !ToDo (Parameter description)
	 *@exception  java.lang.Exception  !ToDo (Exception description)
	 ***********************************************************/
	public void setAtts(Attributes atts) throws java.lang.Exception
	{
		controller = (LoopController)Class.forName(atts.getValue("type")).newInstance();
		controller.setProperty(TestElement.GUI_CLASS,JMeterHandler.getGuiClass(atts.getValue("type")));
		((LoopController)controller).setName(atts.getValue("name"));
		((LoopController)controller).setLoops(Integer.parseInt(atts.getValue("iterations")));
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  out                      !ToDo (Parameter description)
	 *@param  controller               !ToDo (Parameter description)
	 *@exception  java.io.IOException  !ToDo (Exception description)
	 ***********************************************************/
	protected void writeMainTag(Writer out, GenericController controller) throws java.io.IOException
	{
		out.write("<LoopController type=\"");
		out.write(JMeterHandler.convertToXML(controller.getClass().getName()));
		out.write("\" name=\"");
		out.write(JMeterHandler.convertToXML(controller.getName()));
		out.write("\" iterations=\"");
		out.write("" + ((LoopController)controller).getLoops());
		out.write("\">\n");
	}

	/**
	 *  Description of the Method
	 *
	 *@param  cont                     Description of Parameter
	 *@param  out                      Description of Parameter
	 *@exception  java.io.IOException  Description of Exception
	 */
	public void save(Saveable cont, Writer out) throws java.io.IOException
	{
		LoopController controller = (LoopController) cont;
		writeMainTag(out, controller);
		writeSubElements(controller, out);
		out.write("</LoopController>");
	}
}
