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

package org.apache.jmeter.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import org.apache.jmeter.util.JMeterUtils;

/*************************
 *  This class allows the user to input argument value pairs. In addition, the
 *  value may be a list of values.
 *
 *@author     $Author$
 *@created    $Date$
 *@version    $Version: 1.0 10/14/1998$
 ************************/

public class ListDialog extends JPanel implements ActionListener, KeyListener
{

	JList input;

	JLabel value;
	JLabel name;
	JTextField nameField;

	JTextField valueField;

	JButton ok, cancel;

	Vector listData;

	public ListDialog()
	{
	}

	/*************************
	 *  Constructor takes a label for instructions to the user, and a list of items
	 *  to add to selectable list.
	 *
	 *@param  listener  ActionListener for this dialog object.
	 ************************/
	public ListDialog(ActionListener listener)
	{
		listData = new Vector();
		name = new JLabel(JMeterUtils.getResString("name"));
		value = new JLabel(JMeterUtils.getResString("add_value"));
		nameField = new JTextField(10);
		valueField = new JTextField(10);
		valueField.addKeyListener(this);
		GridBagLayout gridBag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridBag);
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = c.EAST;
		c.weightx = 0;
		add(name, c.clone());
		c.weightx = 1;
		c.fill = c.HORIZONTAL;
		c.gridx++;
		c.anchor = c.WEST;
		add(nameField, c.clone());
		c.weightx = 0;
		c.fill = c.NONE;
		c.gridy++;
		c.gridx--;
		c.anchor = c.EAST;
		add(value, c.clone());
		c.weightx = 1;
		c.fill = c.HORIZONTAL;
		c.gridx++;
		c.anchor = c.WEST;
		add(valueField, c.clone());
		input = new JList();
		int x = 0;
		c.fill = c.BOTH;
		c.weightx = c.weighty = 1;
		c.gridy++;
		c.gridx--;
		c.gridwidth = 2;
		add(new JScrollPane(input), c.clone());
		c.gridwidth = 1;
		c.weightx = .5;
		c.gridy++;
		c.weighty = 0;
		c.fill = c.NONE;
		c.anchor = c.EAST;
		ok = new JButton("OK");
		cancel = new JButton("Cancel");
		ok.setPreferredSize(cancel.getPreferredSize());
		ok.addActionListener(listener);
		ok.addActionListener(this);
		cancel.addActionListener(listener);
		add(ok, c.clone());
		c.gridx = 1;
		c.anchor = c.WEST;
		add(cancel, c);
	}
	//end of Method


	/*************************
	 *  Set the items to select.
	 *
	 *@param  items  The new Items value
	 ************************/
	public void setItems(String[] items)
	{

		input.removeAll();

		listData = new Vector(items.length);

		for (int x = 0; x < items.length; listData.add(items[x++]))
		{
			;
		}

		input.setListData(listData);

	}

	/*************************
	 *  Sets the Visible attribute of the ListDialog object
	 *
	 *@param  set  The new Visible value
	 ************************/
	public void setVisible(boolean set)
	{
		super.setVisible(set);
		nameField.requestFocus();
	}

	/*************************
	 *  Sets the Name attribute of the ListDialog object
	 *
	 *@param  label  The new Name value
	 ************************/
	public void setName(String label)
	{
		nameField.setText(label);
	}

	/*************************
	 *  Gets the Name attribute of the ListDialog object
	 *
	 *@return    The Name value
	 ************************/
	public String getName()
	{
		return nameField.getText();
	}


	/*************************
	 *  This method returns the item selected by the user.
	 *
	 *@return    List item selected by the user.
	 ************************/
	public String[] getItems()
	{
		return (String[]) listData.toArray(new String[0]);
	}
	//end of Method


	/*************************
	 *  Satisfies the ActionListener interface requirements. Catches the event
	 *  generated when the user makes a choice and makes the window invisible and
	 *  returns control to the parent frame.
	 *
	 *@param  e  ItemEvent object.
	 ************************/
	public void actionPerformed(ActionEvent e)
	{
		if (valueFieldFull())
		{
			addValueTextToList();
		}
	}
	//end of Method

	/*************************
	 *  Description of the Method
	 *
	 *@param  event  Description of Parameter
	 ************************/
	public void keyPressed(KeyEvent event)
	{
	}

	/*************************
	 *  Description of the Method
	 *
	 *@param  event  Description of Parameter
	 ************************/
	public void keyReleased(KeyEvent event)
	{
	}

	/*************************
	 *  Description of the Method
	 *
	 *@param  event  Description of Parameter
	 ************************/
	public void keyTyped(KeyEvent event)
	{
		if (event.getKeyChar() == '\n' && valueFieldFull())
		{
			addValueTextToList();
		}
	}

	private boolean valueFieldFull()
	{
		String text = valueField.getText();
		return text != null && !text.equals("");
	}

	private void addValueTextToList()
	{
		listData.add(valueField.getText());
		input.setListData(listData);
		valueField.setText("");
	}
}
//end of Method

