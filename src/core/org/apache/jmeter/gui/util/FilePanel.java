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
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.event.*;
import org.apache.jmeter.util.JMeterUtils;

/**
 *  Title: Jakarta JMeter Description: Copyright: Copyright (c) 2002 Company:
 *  Apache Software Foundation
 *
 *@author     Michael Stover
 *@created    April 18, 2002
 *@version    1.0
 */

public class FilePanel extends JPanel implements ActionListener
{

	JTextField filename = new JTextField(30);
	JLabel label = new JLabel(JMeterUtils.getResString("file_visualizer_filename"));
	JButton browse = new JButton(JMeterUtils.getResString("browse"));
	List listeners = new LinkedList();
	
	/**
	 *  Constructor for the FilePanel object
	 */
	public FilePanel()
	{
		init();
	}

	/**
	 *  Constructor for the FilePanel object
	 */
	public FilePanel(ChangeListener l)
	{
		init();
		listeners.add(l);
	}
	
	public void addChangeListener(ChangeListener l)
	{
		listeners.add(l);
	}

	/**
	 *  Description of the Method
	 */
	private void init()
	{
		add(label);
		add(filename);
		filename.addActionListener(this);
		add(browse);
		browse.setActionCommand("browse");
		browse.addActionListener(this);
	}

	/**
	 *  Gets the filename attribute of the FilePanel object
	 *
	 *@return    The filename value
	 */
	public String getFilename()
	{
		return filename.getText();
	}

	/**
	 *  Sets the filename attribute of the FilePanel object
	 *
	 *@param  f  The new filename value
	 */
	public void setFilename(String f)
	{
		filename.setText(f);
	}
	
	private void fireFileChanged()
	{
		Iterator iter = listeners.iterator();
		while(iter.hasNext())
		{
			((ChangeListener)iter.next()).stateChanged(new ChangeEvent(this));
		}
	}
			

	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void actionPerformed(ActionEvent e)
	{
		if(e.getActionCommand().equals("browse"))
		{
			JFileChooser chooser = FileDialoger.promptToOpenFile(new String[]{".jtl"});
			if(chooser != null && chooser.getSelectedFile() != null)
			{
				filename.setText(chooser.getSelectedFile().getPath());
				fireFileChanged();
			}
		}
		else
		{
			fireFileChanged();
		}
	}
}
