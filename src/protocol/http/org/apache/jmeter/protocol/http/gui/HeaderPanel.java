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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.VerticalLayout;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * Allows the user to specify if she needs HTTP header services, and give
 * parameters for this service.
 *
 *@author    $Author$
 *@created   $Date$
 *@version   $Revision$
 ***************************************/
public class HeaderPanel extends AbstractConfigGui implements ActionListener,FocusListener
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.http");
	InnerTableModel tableModel;

	/****************************************
	 * A table to show the authentication information
	 ***************************************/
	JTable headerTable;

	JButton addButton;
	JButton deleteButton;
	JButton loadButton;
	JButton saveButton;
	JPanel headerManagerPanel;

	/****************************************
	 * Default constructor
	 ***************************************/
	public HeaderPanel()
	{
		tableModel = new InnerTableModel();
		init();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public TestElement createTestElement()
	{
		HeaderManager headerManager = tableModel.manager;
		configureTestElement(headerManager);
		return (TestElement)headerManager.clone();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param el  !ToDo (Parameter description)
	 ***************************************/
	public void configure(TestElement el)
	{
		super.configure(el);
		tableModel.manager = (HeaderManager)el;
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("header_manager_title");
	}

	/****************************************
	 * Gets a HeaderManager to manage the file that is currently selected. Null is
	 * returned if no file is currently selected. Null will also be returned if
	 * there is a problem reading the file while getting the HeaderManager.
	 *
	 *@returns   A HeaderManager for the current file, or null public HeaderManager
	 *      getHeaderMgr() { return manager; }
	 ***************************************/

	/****************************************
	 * Shows the main header configuration panel
	 ***************************************/
	public void init()
	{
		// set the layout of the control panel
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		headerManagerPanel = new JPanel();

		Border margin = new EmptyBorder(10, 10, 5, 10);
		headerManagerPanel.setBorder(margin);

		headerManagerPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("header_manager_title"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		headerManagerPanel.add(panelTitleLabel);

		headerManagerPanel.add(getNamePanel());

		JPanel headerTablePanel = createHeaderTablePanel();
		headerManagerPanel.add(headerTablePanel);

		this.add(headerManagerPanel);
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
				if(headerTable.isEditing())
				{
					TableCellEditor cellEditor = headerTable.getCellEditor(headerTable.getEditingRow(), headerTable.getEditingColumn());
					cellEditor.cancelCellEditing();
				}

				int rowSelected = headerTable.getSelectedRow();

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

						headerTable.setRowSelectionInterval(rowToSelect, rowToSelect);
					}
				}
			}
		}
		else if(action.equals("Add"))
		{
			// If a table cell is being edited, we should accept the current value
			// and stop the editing before adding a new row.
			if(headerTable.isEditing())
			{
				TableCellEditor cellEditor = headerTable.getCellEditor(headerTable.getEditingRow(), headerTable.getEditingColumn());
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
			headerTable.setRowSelectionInterval(rowToSelect, rowToSelect);
		}
		else if(action.equals("Load"))
		{
			try
			{
				File tmp = FileDialoger.promptToOpenFile().getSelectedFile();
				if(tmp != null)
				{
					tableModel.manager.addFile(tmp.getAbsolutePath());
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
					tableModel.manager.save(tmp.getAbsolutePath());
				}
			}
			catch(IOException ex)
			{
				log.error("",ex);
			}
			catch(NullPointerException err){}
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public JPanel createHeaderTablePanel()
	{
		Border margin = new EmptyBorder(5, 10, 10, 10);

		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new VerticalLayout(0, VerticalLayout.CENTER));
		tempPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("headers_stored")), margin));

		// create the JTable that holds header per row
		headerTable = new JTable(tableModel);
		headerTable.addFocusListener(this);
		headerTable.setCellSelectionEnabled(true);
		headerTable.setRowSelectionAllowed(true);
		headerTable.setColumnSelectionAllowed(false);
		headerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// create a JScrollPane and place the header JTable inside it
		JScrollPane scroller = new JScrollPane(headerTable);
		headerTable.setPreferredScrollableViewportSize(new Dimension(520, 150));
		JTableHeader tableHeader = headerTable.getTableHeader();
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

	/****************************************
	 * Updates a header record
	 *
	 *@author    $Author$
	 *@created   $Date$
	 *@version   $Revision$
	 ***************************************/
	class HeaderUpdater implements ActionListener
	{
		JTextField nameField = new JTextField(20);
		JTextField valueField = new JTextField(20);
		JButton ok = new JButton("Ok");
		JButton cancel = new JButton("Cancel");
		int index;

		JDialog updateDialog;

		/****************************************
		 * !ToDo (Constructor description)
		 ***************************************/
		public HeaderUpdater() { }

		/****************************************
		 * returns the contructed panel containing the header record
		 *
		 *@return   !ToDo (Return description)
		 ***************************************/
		public JPanel getPanel()
		{
			JPanel main = new JPanel();
			GridBagLayout g = new GridBagLayout();

			main.setLayout(g);
			GridBagConstraints c = new GridBagConstraints();
			c.fill = c.BOTH;
			c.gridwidth = 1;
			c.gridheight = 1;
			JLabel nameLabel = new JLabel(JMeterUtils.getResString("name") + ":");
			c.gridx = 1;
			c.gridy = 1;
			g.setConstraints(nameLabel, c);
			main.add(nameLabel);
			JLabel valueLabel = new JLabel(JMeterUtils.getResString("value") + ":");
			c.gridx = 1;
			c.gridy = 2;
			g.setConstraints(valueLabel, c);
			main.add(valueLabel);

			c.gridx = 2;
			c.gridy = 1;
			g.setConstraints(nameField, c);
			main.add(nameField);
			c.gridx = 2;
			c.gridy = 2;
			g.setConstraints(valueField, c);
			main.add(valueField);

			JPanel buttons = new JPanel();
			ok.setPreferredSize(cancel.getPreferredSize());
			buttons.add(ok);
			buttons.add(cancel);
			c.gridwidth = 2;
			c.gridx = 1;
			c.gridy = 7;
			g.setConstraints(buttons, c);
			main.add(buttons);

			return main;
		}

		/****************************************
		 * !ToDo (Method description)
		 *
		 *@param e  !ToDo (Parameter description)
		 ***************************************/
		public void actionPerformed(ActionEvent e)
		{
			String command = e.getActionCommand();
			boolean valid = true;
			index = -1;
			if(command.equals("Edit"))
			{
				index = headerTable.getSelectedRow();
				if(index < 0)
				{
					valid = false;
				}
				else
				{
					Header c = tableModel.manager.get(index);
					nameField = new JTextField(c.getName(), 20);
					valueField = new JTextField(c.getValue(), 20);
					ok = new JButton("Ok");
					cancel = new JButton("Cancel");
				}
			}
			else if(command.equals("Add"))
			{
				nameField = new JTextField(20);
				valueField = new JTextField(20);
				ok = new JButton("Ok");
				cancel = new JButton("Cancel");
			}
			if(valid)
			{
				if(updateDialog != null)
				{
					updateDialog.dispose();
				}
				updateDialog = new JDialog();
				updateDialog.setSize(350, 300);

				ok.addActionListener(
					new ActionListener()
					{
						/****************************************
						 * !ToDo (Method description)
						 *
						 *@param ev  !ToDo (Parameter description)
						 ***************************************/
						public void actionPerformed(ActionEvent ev)
						{
							int i = index;
							Header c = new Header();
							if(i >= 0)
							{
								c = tableModel.manager.get(index);
							}
							c.setName(nameField.getText());
							c.setValue(valueField.getText());
							if(i < 0)
							{
								tableModel.manager.add(c);
							}
							tableModel.fireTableDataChanged();
							updateDialog.dispose();
						}
					});
				cancel.addActionListener(
					new ActionListener()
					{
						/****************************************
						 * !ToDo (Method description)
						 *
						 *@param ev  !ToDo (Parameter description)
						 ***************************************/
						public void actionPerformed(ActionEvent ev)
						{
							updateDialog.dispose();
						}
					});
				updateDialog.getContentPane().add(getPanel());
				updateDialog.show();
			}
		}
	}
	
	public void focusGained(FocusEvent e)
	{
	}
	
	public void focusLost(FocusEvent e)
	{
		try {
			headerTable.getCellEditor().stopCellEditing();
		} catch (RuntimeException err) {
		}
	}

	/****************************************
	 * !ToDo (Class description)
	 *
	 *@author    $Author$
	 *@created   $Date$
	 *@version   $Revision$
	 ***************************************/
	private class InnerTableModel extends AbstractTableModel
	{
		HeaderManager manager;

		/****************************************
		 * !ToDo (Constructor description)
		 *
		 *@param man  !ToDo (Parameter description)
		 ***************************************/
		public InnerTableModel(HeaderManager man)
		{
			manager = man;
		}

		/****************************************
		 * !ToDo (Constructor description)
		 ***************************************/
		public InnerTableModel()
		{
			manager = new HeaderManager();
		}

		/****************************************
		 * !ToDo (Method description)
		 *
		 *@param row  !ToDo (Parameter description)
		 ***************************************/
		public void removeRow(int row)
		{
			manager.remove(row);
		}

		/****************************************
		 * !ToDo
		 ***************************************/
		public void addNewRow()
		{
			manager.add();
		}

		/****************************************
		 * !ToDoo (Method description)
		 *
		 *@param row     !ToDo (Parameter description)
		 *@param column  !ToDo (Parameter description)
		 *@return        !ToDo (Return description)
		 ***************************************/
		public boolean isCellEditable(int row, int column)
		{
			// all table cells are editable
			return true;
		}

		/****************************************
		 * !ToDoo (Method description)
		 *
		 *@param column  !ToDo (Parameter description)
		 *@return        !ToDo (Return description)
		 ***************************************/
		public Class getColumnClass(int column)
		{
			return getValueAt(0, column).getClass();
		}

		/****************************************
		 * required by table model interface
		 *
		 *@return   !ToDo (Return description)
		 ***************************************/
		public int getRowCount()
		{
			return manager.getHeaders().size();
		}

		/****************************************
		 * required by table model interface
		 *
		 *@return   !ToDo (Return description)
		 ***************************************/
		public int getColumnCount()
		{
			return manager.getColumnCount();
		}

		/****************************************
		 * required by table model interface
		 *
		 *@param column  !ToDo (Parameter description)
		 *@return        !ToDo (Return description)
		 ***************************************/
		public String getColumnName(int column)
		{
			return manager.getColumnName(column);
		}

		/****************************************
		 * required by table model interface
		 *
		 *@param row     !ToDo (Parameter description)
		 *@param column  !ToDo (Parameter description)
		 *@return        !ToDo (Return description)
		 ***************************************/
		public Object getValueAt(int row, int column)
		{
			Header head = manager.getHeader(row);
			if(column == 0)
			{
				return head.getName();
			}
			else if(column == 1)
			{
				return head.getValue();
			}
			return null;
		}

		/****************************************
		 * !ToDo (Method description)
		 *
		 *@param value   !ToDo (Parameter description)
		 *@param row     !ToDo (Parameter description)
		 *@param column  !ToDo (Parameter description)
		 ***************************************/
		public void setValueAt(Object value, int row, int column)
		{
			Header header = manager.getHeader(row);

			if(column == 0)
			{
				header.setName((String)value);
			}
			else if(column == 1)
			{
				header.setValue((String)value);
			}
		}

	}
}
