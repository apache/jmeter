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
package org.apache.jmeter.gui.action;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.samplers.Clearable;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/************************************************************
 *  Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author     Michael Stover
 *@created    $Date$
 *@version    1.0
 ***********************************************************/

public class Clear implements Command
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.gui");
	public final static String CLEAR = "action.clear";
	public final static String CLEAR_ALL = "action.clear_all";

	private static Set commands = new HashSet();

	/************************************************************
	 *  !ToDo (Constructor description)
	 ***********************************************************/
	public Clear()
	{
	}

	/************************************************************
	 *  !ToDoo (Method description)
	 *
	 *@return    !ToDo (Return description)
	 ***********************************************************/
	public Set getActionNames()
	{
		return commands;
	}

	/************************************************************
	 *  !ToDo (Method description)
	 *
	 *@param  e  !ToDo (Parameter description)
	 ***********************************************************/
	public void doAction(ActionEvent e)
	{
		GuiPackage guiPackage = GuiPackage.getInstance();
		if (e.getActionCommand().equals(CLEAR))
		{
			JMeterGUIComponent model = (JMeterGUIComponent)guiPackage.getTreeListener().getCurrentNode().getUserObject();
			try
			{
				((Clearable)model).clear();
			}
			catch (Throwable ex)
			{
				log.error("",ex);
			}
		}
		else
		{
			Iterator iter = guiPackage.getTreeModel().getNodesOfType(Clearable.class).iterator();
			while (iter.hasNext())
			{
				try
				{
					Clearable item = (Clearable)((JMeterTreeNode)iter.next()).getUserObject();
					item.clear();
				}
				catch (Exception ex)
				{
					log.error("",ex);
				}
			}
		}
	}
	static
	{
		commands.add(CLEAR);
		commands.add(CLEAR_ALL);
	}
}
