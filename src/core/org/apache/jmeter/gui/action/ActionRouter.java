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
import java.awt.event.ActionListener;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.jorphan.reflect.ClassFinder;

/**
 *  Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author     Michael Stover
 *@created    February 10, 2001
 *@version    1.0
 */

public class ActionRouter implements ActionListener
{
	private Map commands = new HashMap();
	private static ActionRouter router;
	private static AddToTree add = new AddToTree();
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.gui");


	private ActionRouter()
	{
	}

	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of Parameter
	 */
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			Set commandObjects = (Set)commands.get(e.getActionCommand());
			Iterator iter = commandObjects.iterator();
			while(iter.hasNext())
			{
				try
				{
					((Command)iter.next()).doAction(e);
				}
				catch(Exception err)
				{
					log.error("",err);
				}
			}
		}
		catch (NullPointerException er)
		{
			log.error("",er);
			JMeterUtils.reportErrorToUser("Sorry, this feature (" + e.getActionCommand() +
					") not yet implemented");
		}
	}
	
	public Set getAction(String actionName)
	{
		Set set = new HashSet();
		Set commandObjects = (Set)commands.get(actionName);
		Iterator iter = commandObjects.iterator();
		while(iter.hasNext())
		{
			try
			{
				set.add(iter.next());
			}
			catch(Exception err)
			{
				log.error("",err);
			}
		}
		return set;
	}
	
	public Command getAction(String actionName,String className)
	{
		Set commandObjects = (Set)commands.get(actionName);
		Iterator iter = commandObjects.iterator();
		while(iter.hasNext())
		{
			try
			{
				Command com = (Command)iter.next();
				if(com.getClass().getName().equals(className))
				{
					return com;
				}
			}
			catch(Exception err)
			{
				log.error("",err);
			}
		}
		return null;
	}

	private void populateCommandMap()
	{
		List listClasses;
		Command command;
		Iterator iterClasses;
		Class commandClass;
		try
		{
			listClasses = ClassFinder.findClassesThatExtend(
					JMeterUtils.getSearchPaths(),
					new Class[]{Class.forName("org.apache.jmeter.gui.action.Command")});
			commands = new HashMap(listClasses.size());
			if (listClasses.size() == 0)
			{
				log.warn("!!!!!Uh-oh, didn't find any action handlers!!!!!");
			}
			iterClasses = listClasses.iterator();
			while (iterClasses.hasNext())
			{
				String strClassName = (String) iterClasses.next();
				commandClass = Class.forName(strClassName);
				if (!Modifier.isAbstract(commandClass.getModifiers()))
				{
					command = (Command) commandClass.newInstance();
					Iterator iter = command.getActionNames().iterator();
					while (iter.hasNext())
					{
						String commandName = (String)iter.next();
						Set commandObjects = (Set)commands.get(commandName);
						if(commandObjects == null)
						{
							commandObjects = new HashSet();
							commands.put(commandName,commandObjects);
						}
						commandObjects.add(command);
					}
				}
			}
		}
		catch (Exception e)
		{
			log.error("exception finding action handlers",e);
		}
	}

	/**
	 *  Gets the Instance attribute of the ActionRouter class
	 *
	 *@return    The Instance value
	 */
	public static ActionRouter getInstance()
	{
		if (router == null)
		{
			router = new ActionRouter();
			router.populateCommandMap();
		}
		return router;
	}
}
