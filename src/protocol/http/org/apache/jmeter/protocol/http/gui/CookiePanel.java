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
package org.apache.jmeter.protocol.http.gui;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.gui.util.VerticalLayout;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * Allows the user to specify if she needs cookie services, and give parameters
 * for this service.
 *
 *@author    $Author$
 *@created   $Date$
 *@version   $Revision$
 ***************************************/
public class CookiePanel extends AbstractConfigGui implements ActionListener
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.http");
	private final static int columnCount = 6;
	private final static String[] columnNames = {
		 JMeterUtils.getResString("name"),
		 JMeterUtils.getResString("value"),
		 JMeterUtils.getResString("domain"),
		 JMeterUtils.getResString("path"),
		 JMeterUtils.getResString("secure"),
		 JMeterUtils.getResString("expiration"),
	};
	
	JTable cookieTable;
	JTextField nicknameField;
	JButton addButton;
	JButton deleteButton;
	JButton loadButton;
	JButton saveButton;
	JPanel cookieManagerPanel;
	JTextPane nicknameTextPane;
	JTextArea nicknameText;
	PowerTableModel tableModel;

	/****************************************
	 * Default constructor
	 ***************************************/
	public CookiePanel()
	{
		tableModel = new PowerTableModel(columnNames,new Class[]{String.class,
				String.class,String.class,String.class,Boolean.class,Long.class});
		init();
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("cookie_manager_title");
	}

	/****************************************
	 * Description of the Method
	 *
	 *@param e  Description of Parameter
	 ***************************************/
	public void actionPerformed(ActionEvent e)
	{
		String action = e.getActionCommand();

		if(action.equals("Delete"))
		{
			if(tableModel.getRowCount() > 0)
			{
				// If a table cell is being edited, we must cancel the editing before
				// deleting the row
				if(cookieTable.isEditing())
				{
					TableCellEditor cellEditor = cookieTable.getCellEditor(cookieTable.getEditingRow(), cookieTable.getEditingColumn());
					cellEditor.cancelCellEditing();
				}

				int rowSelected = cookieTable.getSelectedRow();

				if(rowSelected != -1)
				{
					tableModel.removeRow(rowSelected);
					tableModel.fireTableDataChanged();

					// Disable the DELETE and SAVE buttons if no rows remaining after delete
					if(tableModel.getRowCount() == 0)
					{
						deleteButton.setEnabled(false);
						saveButton.setEnabled(false);
					}

					// Table still contains one or more rows, so highlight (select)
					// the appropriate one.
					else
					{
						int rowToSelect = rowSelected;

						if(rowSelected >= tableModel.getRowCount())
						{
							rowToSelect = rowSelected - 1;
						}

						cookieTable.setRowSelectionInterval(rowToSelect, rowToSelect);
					}
				}
			}
		}
		else if(action.equals("Add"))
		{
			// If a table cell is being edited, we should accept the current value
			// and stop the editing before adding a new row.
			if(cookieTable.isEditing())
			{
				TableCellEditor cellEditor = cookieTable.getCellEditor(cookieTable.getEditingRow(), cookieTable.getEditingColumn());
				cellEditor.stopCellEditing();
			}

			tableModel.addNewRow();
			tableModel.fireTableDataChanged();

			// Enable the DELETE and SAVE buttons if they are currently disabled.
			if(!deleteButton.isEnabled())
			{
				deleteButton.setEnabled(true);
			}
			if(!saveButton.isEnabled())
			{
				saveButton.setEnabled(true);
			}

			// Highlight (select) the appropriate row.
			int rowToSelect = tableModel.getRowCount() - 1;
			cookieTable.setRowSelectionInterval(rowToSelect, rowToSelect);
		}
		else if(action.equals("Load"))
		{
			try
			{
				File tmp = FileDialoger.promptToOpenFile().getSelectedFile();
				if(tmp != null)
				{
					CookieManager manager = new CookieManager();
					manager.addFile(tmp.getAbsolutePath());
					Cookie cookie = manager.get(0);
					addCookieToTable(cookie);
					tableModel.fireTableDataChanged();

					if(tableModel.getRowCount() > 0)
					{
						deleteButton.setEnabled(true);
						saveButton.setEnabled(true);
					}
				}
			}
			catch(IOException ex)
			{
				log.error("",ex);
			}
			catch(NullPointerException err){}
		}
		else if(action.equals("Save"))
		{
			try
			{
				File tmp = FileDialoger.promptToSaveFile(null).getSelectedFile();
				if(tmp != null)
				{
					createCookieManager().save(tmp.getAbsolutePath());
				}
			}
			catch(IOException ex)
			{
				log.error("",ex);
			}
			catch(NullPointerException err){}
		}
	}

	private void addCookieToTable(Cookie cookie) {
		tableModel.addRow(new Object[]{cookie.getName(),cookie.getValue(),
				cookie.getDomain(),cookie.getPath(),
				new Boolean(cookie.getSecure()),
				new Long(cookie.getExpires())});
	}
	
	private CookieManager createCookieManager()
	{
		CookieManager cookieManager = new CookieManager();
		for(int i = 0;i < tableModel.getRowCount();i++)
		{
			Cookie cookie = createCookie(tableModel.getRowData(i));
			cookieManager.add(cookie);
		}
		return cookieManager;
	}
	
	private Cookie createCookie(Object[] rowData)
	{
		Cookie cookie = new Cookie((String)rowData[0],(String)rowData[1],(String)rowData[2],
				(String)rowData[3],((Boolean)rowData[4]).booleanValue(),
				((Long)rowData[5]).longValue());
		return cookie;
	}
	
	private void populateTable(CookieManager manager)
	{
		Iterator iter = manager.getCookies().iterator();
		while(iter.hasNext())
		{
			addCookieToTable((Cookie)iter.next());
		}
	}
	

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public TestElement createTestElement()
	{
		CookieManager cookieManager = createCookieManager();
		configureTestElement(cookieManager);
		return cookieManager;
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param el  !ToDo (Parameter description)
	 ***************************************/
	public void configure(TestElement el)
	{
		super.configure(el);
		populateTable((CookieManager)el);
	}

	/****************************************
	 * Shows the main cookie configuration panel
	 ***************************************/
	public void init()
	{
		// set the layout of the control panel
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		cookieManagerPanel = new JPanel();

		Border margin = new EmptyBorder(10, 10, 5, 10);
		cookieManagerPanel.setBorder(margin);

		cookieManagerPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("cookie_manager_title"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		cookieManagerPanel.add(panelTitleLabel);

		cookieManagerPanel.add(getNamePanel());

		JPanel cookieTablePanel = createCookieTablePanel();
		cookieManagerPanel.add(cookieTablePanel);

		this.add(cookieManagerPanel);
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public JPanel createCookieTablePanel()
	{
		Border margin = new EmptyBorder(5, 10, 10, 10);

		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new VerticalLayout(0, VerticalLayout.CENTER));
		tempPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("cookies_stored")), margin));

		// create the JTable that holds one cookie per row
		cookieTable = new JTable(tableModel);
		cookieTable.setCellSelectionEnabled(true);
		cookieTable.setRowSelectionAllowed(true);
		cookieTable.setColumnSelectionAllowed(false);
		cookieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// create a JScrollPane and place the cookie JTable inside it
		JScrollPane scroller = new JScrollPane(cookieTable);
		cookieTable.setPreferredScrollableViewportSize(new Dimension(520, 150));
		JTableHeader tableHeader = cookieTable.getTableHeader();
		scroller.setColumnHeaderView(tableHeader);

		tempPanel.add(scroller);

		// ADD button
		addButton = new JButton(JMeterUtils.getResString("add"));
		addButton.setMnemonic('A');
		addButton.setActionCommand("Add");
		addButton.addActionListener(this);

		// DELETE button
		deleteButton = new JButton(JMeterUtils.getResString("delete"));

		if(tableModel.getRowCount() == 0)
		{
			deleteButton.setEnabled(false);
		}
		else
		{
			deleteButton.setEnabled(true);
		}

		deleteButton.setMnemonic('D');
		deleteButton.setActionCommand("Delete");
		deleteButton.addActionListener(this);

		// LOAD button
		loadButton = new JButton(JMeterUtils.getResString("load"));
		loadButton.setMnemonic('L');
		loadButton.setActionCommand("Load");
		loadButton.addActionListener(this);

		// SAVE button
		saveButton = new JButton(JMeterUtils.getResString("save"));

		if(tableModel.getRowCount() == 0)
		{
			saveButton.setEnabled(false);
		}
		else
		{
			saveButton.setEnabled(true);
		}

		saveButton.setMnemonic('S');
		saveButton.setActionCommand("Save");
		saveButton.addActionListener(this);

		// Button Panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(loadButton);
		buttonPanel.add(saveButton);

		tempPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		tempPanel.add(buttonPanel);

		return tempPanel;
	}

	
}
