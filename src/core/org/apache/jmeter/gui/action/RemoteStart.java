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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.jmeter.engine.ClientJMeterEngine;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;
import org.apache.jorphan.collections.HashTree;

/****************************************
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class RemoteStart extends AbstractAction
{
	transient private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.gui");
	private Map remoteEngines = new HashMap();

	private static Set commands = new HashSet();
	static
	{
		commands.add("remote_start");
		commands.add("remote_stop");
	}

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public RemoteStart() { }

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void doAction(ActionEvent e)
	{
		String name = ((Component)e.getSource()).getName();
		String action = e.getActionCommand();
		if(action.equals("remote_stop"))
		{
			GuiPackage.getInstance().getMainFrame().showStoppingMessage(name);
			JMeterEngine engine = (JMeterEngine)remoteEngines.get(name);
			engine.stopTest();
		}
		else if(action.equals("remote_start"))
		{
			JMeterEngine engine = (JMeterEngine)remoteEngines.get(name);
			if(engine == null)
			{
				try
				{
					engine = new ClientJMeterEngine(name);
					remoteEngines.put(name, engine);
				}
				catch(Exception ex)
				{
					log.error("",ex);
					JMeterUtils.reportErrorToUser("Bad call to remote host");
					return;
				}
			}
			else
			{
				engine.reset();
			}
			startEngine(engine, name);
		}
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Set getActionNames()
	{
		return commands;
	}

	/****************************************
	 * Description of the Method
	 *
	 *@param engine  !ToDo (Parameter description)
	 *@param host    !ToDo (Parameter description)
	 ***************************************/
	private void startEngine(JMeterEngine engine, String host)
	{
		GuiPackage gui = GuiPackage.getInstance();
		HashTree testTree = gui.getTreeModel().getTestPlan();
		convertSubTree(testTree);
		testTree.add(testTree.getArray()[0],gui.getMainFrame());
		engine.configure(testTree);
		try {
			engine.runTest();
		} catch(JMeterEngineException e) {
			JOptionPane.showMessageDialog(gui.getMainFrame(),e.getMessage(),
					JMeterUtils.getResString("Error Occurred"),JOptionPane.ERROR_MESSAGE);
		}
	}
}
