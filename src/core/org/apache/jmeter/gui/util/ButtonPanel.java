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
 */package org.apache.jmeter.gui.util;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.apache.jmeter.util.JMeterUtils;

/****************************************
 * Title: Apache JMeter Description: Copyright: Copyright (c) 2000 Company:
 * Apache Foundation
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class ButtonPanel extends JPanel
{

	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	public final static int ADD_BUTTON = 1;
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	public final static int EDIT_BUTTON = 2;
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	public final static int DELETE_BUTTON = 3;
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	public final static int LOAD_BUTTON = 4;
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	public final static int SAVE_BUTTON = 5;

	private JButton add, delete, edit, load, save;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public ButtonPanel()
	{
		init();
	}

	/****************************************
	 * !ToDo
	 *
	 *@param button    !ToDo
	 *@param listener  !ToDo
	 ***************************************/
	public void addButtonListener(int button, ActionListener listener)
	{
		switch (button)
		{
			case ADD_BUTTON:
				add.addActionListener(listener);
				break;
			case EDIT_BUTTON:
				edit.addActionListener(listener);
				break;
			case DELETE_BUTTON:
				delete.addActionListener(listener);
				break;
			case LOAD_BUTTON:
				load.addActionListener(listener);
				break;
			case SAVE_BUTTON:
				save.addActionListener(listener);
				break;
		}
	}

	private void initButtonMap() { }

	private void init()
	{
		add = new JButton(JMeterUtils.getResString("add"));
		add.setActionCommand("Add");
		edit = new JButton(JMeterUtils.getResString("edit"));
		edit.setActionCommand("Edit");
		delete = new JButton(JMeterUtils.getResString("delete"));
		delete.setActionCommand("Delete");
		load = new JButton(JMeterUtils.getResString("load"));
		load.setActionCommand("Load");
		save = new JButton(JMeterUtils.getResString("save"));
		save.setActionCommand("Save");
		Dimension d = delete.getPreferredSize();
		add.setPreferredSize(d);
		edit.setPreferredSize(d);
		//close.setPreferredSize(d);
		load.setPreferredSize(d);
		save.setPreferredSize(d);
		GridBagLayout g = new GridBagLayout();
		this.setLayout(g);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.gridx = 1;
		c.gridy = 1;
		g.setConstraints(add, c);
		this.add(add);
		c.gridx = 2;
		c.gridy = 1;
		g.setConstraints(edit, c);
		this.add(edit);
		c.gridx = 3;
		c.gridy = 1;
		g.setConstraints(delete, c);
		this.add(delete);
		/*
		 * c.gridx = 1;
		 * c.gridy = 2;
		 * g.setConstraints(close, c);
		 * panel.add(close);
		 */
		c.gridx = 2;
		c.gridy = 2;
		g.setConstraints(load, c);
		this.add(load);
		c.gridx = 3;
		c.gridy = 2;
		g.setConstraints(save, c);
		this.add(save);
	}
}
