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
import java.util.Iterator;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.save.old.Saveable;
import org.apache.jmeter.save.old.xml.TagHandler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.ThreadGroup;
import org.xml.sax.Attributes;

/************************************************************
 *  Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 *@author     Michael Stover
 *@created    $Date$
 *@version    1.0
 ***********************************************************/

public class ThreadGroupHandler extends TagHandler
{

	 private ThreadGroup group;

	 /************************************************************
	  *  !ToDo (Constructor description)
	  ***********************************************************/
	 public ThreadGroupHandler()
	 {
	 }

	 /************************************************************
	  *  !ToDo (Method description)
	  *
	  *@param  atts                     !ToDo (Parameter description)
	  *@exception  java.lang.Exception  !ToDo (Exception description)
	  ***********************************************************/
	 public void setAtts(Attributes atts) throws java.lang.Exception
	 {
		  group = new ThreadGroup();
		  group.setName(atts.getValue("name"));
		  group.setProperty(TestElement.GUI_CLASS,
				"org.apache.jmeter.threads.gui.ThreadGroupGui");
		  try
		  {
				group.setNumThreads(Integer.parseInt(atts.getValue("numThreads")));
		  }
		  catch(NumberFormatException err)
		  {
		  	group.setNumThreads(1);
		  }
		  try
		  {
				group.setRampUp(Integer.parseInt(atts.getValue("rampUp")));
		  }
		  catch (NumberFormatException ex)
		  {
				group.setRampUp(0);
		  }

	 }

	 /************************************************************
	  *  !ToDoo (Method description)
	  *
	  *@return    !ToDo (Return description)
	  ***********************************************************/
	 public String getPrimaryTagName()
	 {
		  return "ThreadGroup";
	 }

	 /************************************************************
	  *  !ToDoo (Method description)
	  *
	  *@return    !ToDo (Return description)
	  ***********************************************************/
	 public Object getModel()
	 {
		  return group;
	 }

	 /************************************************************
	  *  !ToDo (Method description)
	  *
	  *@param  s                        !ToDo (Parameter description)
	  *@param  out                      !ToDo (Parameter description)
	  *@exception  java.io.IOException  !ToDo (Exception description)
	  ***********************************************************/
	 public void save(Saveable s, Writer out) throws java.io.IOException
	 {
		  /*ThreadGroup save = (ThreadGroup)s;
		  out.write("<ThreadGroup name=\"");
		  out.write(JMeterHandler.convertToXML(save.getName()));
		  out.write("\" numThreads=\"");

		  out.write(String.valueOf(save.getNumThreads()));

		  out.write("\" rampUp=\"");
		  out.write(String.valueOf(save.getRampUp()));
		  out.write("\"");

		  out.write(">\n");
		  Collection controllers = new LinkedList();
		  controllers.add(save.getSamplerController());
		  JMeterHandler.writeControllers(controllers, out);
		  JMeterHandler.writeTimers(save.getTimers(), out);
		  JMeterHandler.writeListeners(save.getListeners(), out);
		  out.write("</ThreadGroup>\n");*/
	 }

	 /************************************************************
	  *  !ToDo (Method description)
	  *
	  *@param  childObj  !ToDo (Parameter description)
	  ***********************************************************/
	 public void notifySubElementEnded(Object childObj)
	 {
		  if (childObj instanceof LoopController)
		  {
				Iterator children = xmlParent.takeChildObjects(this).iterator();
				if (children.hasNext())
				{
					 group.setSamplerController((LoopController)((TagHandler)children.next()).getModel());
				}
		  }
	 }
}
