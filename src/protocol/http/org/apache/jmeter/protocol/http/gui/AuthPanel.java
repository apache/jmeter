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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.VerticalLayout;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Hierarchy;
import org.apache.log.Logger;

/****************************************
 * Handles input for determining if authentication services are required for a
 * Sampler. It also understands how to get AuthManagers for the files that the
 * user selects.
 *
 *@author    $Author$
 *@created   $Date$
 *@version   $Revision$
 ***************************************/
public class AuthPanel extends AbstractConfigGui implements ActionListener
{
	private static Logger log = Hierarchy.getDefaultHierarchy().getLoggerFor(
			"jmeter.protocol.http");
	InnerTableModel tableModel;

	/****************************************
	 * A table to show the authentication information
	 ***************************************/
	JTable authTable;
	JButton addButton;
	JButton deleteButton;
	JButton loadButton;
	JButton saveButton;
	JPanel authManagerPanel;

	/****************************************
	 * Default Constructor
	 ***************************************/
	public AuthPanel()
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
		AuthManager authMan = tableModel.manager;
		configureTestElement(authMan);
		return (TestElement)authMan.clone();
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param el  !ToDo (Parameter description)
	 ***************************************/
	public void configure(TestElement el)
	{
		super.configure(el);
		tableModel.manager = (AuthManager)el;
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("auth_manager_title");
	}

	/****************************************
	 * Shows the main authentication panel for this object
	 ***************************************/
	public void init()
	{
		// set the layout of the control panel
		this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		authManagerPanel = new JPanel();

		Border margin = new EmptyBorder(10, 10, 5, 10);
		authManagerPanel.setBorder(margin);

		authManagerPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

		JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("auth_manager_title"));
		Font curFont = panelTitleLabel.getFont();
		int curFontSize = curFont.getSize();
		curFontSize += 4;
		panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
		authManagerPanel.add(panelTitleLabel);

		authManagerPanel.add(getNamePanel());

		JPanel authTablePanel = createAuthTablePanel();
		authManagerPanel.add(authTablePanel);

		this.add(authManagerPanel);
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
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
				if(authTable.isEditing())
				{
					TableCellEditor cellEditor = authTable.getCellEditor(authTable.getEditingRow(), authTable.getEditingColumn());
					cellEditor.cancelCellEditing();
				}

				int rowSelected = authTable.getSelectedRow();

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

						authTable.setRowSelectionInterval(rowToSelect, rowToSelect);
					}
				}
			}
		}
		else if(action.equals("Add"))
		{
			// If a table cell is being edited, we should accept the current value
			// and stop the editing before adding a new row.
			if(authTable.isEditing())
			{
				TableCellEditor cellEditor = authTable.getCellEditor(authTable.getEditingRow(), authTable.getEditingColumn());
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
			authTable.setRowSelectionInterval(rowToSelect, rowToSelect);
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
	public JPanel createAuthTablePanel()
	{
		Border margin = new EmptyBorder(5, 10, 10, 10);

		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new VerticalLayout(0, VerticalLayout.CENTER));
		tempPanel.setBorder(new CompoundBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("auths_stored")), margin));

		// create the JTable that holds auth per row
		authTable = new JTable(tableModel);
		authTable.setCellSelectionEnabled(true);
		authTable.setRowSelectionAllowed(true);
		authTable.setColumnSelectionAllowed(false);
		authTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		TableColumn passwordColumn = authTable.getColumnModel().getColumn(2);
		passwordColumn.setCellEditor(new DefaultCellEditor(new JPasswordField()));
		passwordColumn.setCellRenderer(new PasswordCellRenderer());

		// create a JScrollPane and place the auth JTable inside it
		JScrollPane scroller = new JScrollPane(authTable);
		authTable.setPreferredScrollableViewportSize(new Dimension(520, 150));
		JTableHeader tableHeader = authTable.getTableHeader();
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
	 * !ToDo (Class description)
	 *
	 *@author    $Author$
	 *@created   $Date$
	 *@version   $Revision$
	 ***************************************/
	private class InnerTableModel extends AbstractTableModel
	{
		AuthManager manager;

		/****************************************
		 * !ToDo (Constructor description)
		 *
		 *@param man  !ToDo (Parameter description)
		 ***************************************/
		public InnerTableModel(AuthManager man)
		{
			manager = man;
		}

		/****************************************
		 * !ToDo (Constructor description)
		 ***************************************/
		public InnerTableModel()
		{
			manager = new AuthManager();
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
			manager.addAuth();
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
			return manager.getAuthObjects().size();
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
			Authorization auth = manager.getAuthObjectAt(row);

			if(column == 0)
			{
				return auth.getURL();
			}
			else if(column == 1)
			{
				return auth.getUser();
			}
			else if(column == 2)
			{
				return auth.getPass();
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
			Authorization auth = manager.getAuthObjectAt(row);

			if(column == 0)
			{
				auth.setURL((String)value);
			}
			else if(column == 1)
			{
				auth.setUser((String)value);
			}
			else if(column == 2)
			{
				auth.setPass((String)value);
			}
		}
	}

	/****************************************
	 * !ToDo (Class description)
	 *
	 *@author    $Author$
	 *@created   $Date$
	 *@version   $Revision$
	 ***************************************/
	private class PasswordCellRenderer extends JPasswordField implements TableCellRenderer
	{
		private Border myBorder;

		/****************************************
		 * !ToDo (Constructor description)
		 ***************************************/
		public PasswordCellRenderer()
		{
			super();
			myBorder = new EmptyBorder(1, 2, 1, 2);
			setOpaque(true);
			setBorder(myBorder);
		}

		/****************************************
		 * !ToDoo (Method description)
		 *
		 *@param table       !ToDo (Parameter description)
		 *@param value       !ToDo (Parameter description)
		 *@param isSelected  !ToDo (Parameter description)
		 *@param hasFocus    !ToDo (Parameter description)
		 *@param row         !ToDo (Parameter description)
		 *@param column      !ToDo (Parameter description)
		 *@return            !ToDo (Return description)
		 ***************************************/
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus,
				int row, int column)
		{
			setText((String)value);

			setBackground(isSelected && !hasFocus ?
					table.getSelectionBackground() : table.getBackground());
			setForeground(isSelected && !hasFocus ?
					table.getSelectionForeground() : table.getForeground());

			setFont(table.getFont());

			return this;
		}
	}
}
