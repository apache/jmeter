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
import java.util.Set;

import javax.swing.JOptionPane;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.ListedHashTree;

/****************************************
 * Title: Apache JMeter Description: Copyright: Copyright (c) 2000 Company:
 * Apache Foundation
 *
 *@author    Michael Stover
 *@created   March 1, 2001
 *@version   1.0
 ***************************************/

public class Start extends AbstractAction
{
	private static Set commands = new HashSet();

	private StandardJMeterEngine engine;
	static
	{
		commands.add("start");
		commands.add("stop");
	}


	/****************************************
	 * Constructor for the Start object
	 ***************************************/
	public Start() { }


	/****************************************
	 * Gets the ActionNames attribute of the Start object
	 *
	 *@return   The ActionNames value
	 ***************************************/
	public Set getActionNames()
	{
		return commands;
	}


	/****************************************
	 * Description of the Method
	 *
	 *@param e  Description of Parameter
	 ***************************************/
	public void doAction(ActionEvent e)
	{
		if(e.getActionCommand().equals("start"))
		{
			startEngine();
		}
		else if(e.getActionCommand().equals("stop"))
		{
			GuiPackage.getInstance().getMainFrame().showStoppingMessage();
			engine.stopTest();
			engine = null;
		}
	}

	/****************************************
	 * Description of the Method
	 *
	 ***************************************/
	protected void startEngine()
	{
		GuiPackage gui = GuiPackage.getInstance();
		engine = new StandardJMeterEngine();
		ListedHashTree testTree = gui.getTreeModel().getTestPlan();
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
