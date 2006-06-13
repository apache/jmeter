// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.jmeter.protocol.http.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Allows the user to specify if she needs HTTP header services, and give
 * parameters for this service.
 * 
 * @version $Revision$
 */
public class HeaderPanel extends AbstractConfigGui implements ActionListener, FocusListener {
	transient private static Logger log = LoggingManager.getLoggerForClass();

	private static final String ADD_COMMAND = "Add";

	private static final String DELETE_COMMAND = "Delete";

	private static final String LOAD_COMMAND = "Load";

	private static final String SAVE_COMMAND = "Save";

	private InnerTableModel tableModel;

	/**
	 * A table to show the authentication information.
	 */
	private JTable headerTable;

	private JButton addButton;

	private JButton deleteButton;

	private JButton loadButton;

	private JButton saveButton;

	/**
	 * Default constructor.
	 */
	public HeaderPanel() {
		tableModel = new InnerTableModel();
		init();
	}

	public TestElement createTestElement() {
		HeaderManager headerManager = tableModel.manager;
		configureTestElement(headerManager);
		return (TestElement) headerManager.clone();
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement el) {
		el.clear();
		el.addTestElement(tableModel.manager);
		configureTestElement(el);
	}

	public void configure(TestElement el) {
		tableModel.manager.clear();
		super.configure(el);
		tableModel.manager.addTestElement(el);
		boolean hasRows = tableModel.getRowCount() > 0;
		deleteButton.setEnabled(hasRows);
		saveButton.setEnabled(hasRows);

	}

	public String getLabelResource() {
		return "header_manager_title";
	}

	/**
	 * Gets a HeaderManager to manage the file that is currently selected. Null
	 * is returned if no file is currently selected. Null will also be returned
	 * if there is a problem reading the file while getting the HeaderManager.
	 * 
	 * @returns a HeaderManager for the current file, or null
	 * 
	 * public HeaderManager getHeaderMgr() { return manager; }
	 */

	/**
	 * Shows the main header configuration panel.
	 */
	public void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
		add(createHeaderTablePanel(), BorderLayout.CENTER);
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("Delete")) {
			if (tableModel.getRowCount() > 0) {
				// If a table cell is being edited, we must cancel the editing
				// before deleting the row.
				if (headerTable.isEditing()) {
					TableCellEditor cellEditor = headerTable.getCellEditor(headerTable.getEditingRow(), headerTable
							.getEditingColumn());
					cellEditor.cancelCellEditing();
				}

				int rowSelected = headerTable.getSelectedRow();

				if (rowSelected != -1) {
					tableModel.removeRow(rowSelected);
					tableModel.fireTableDataChanged();

					// Disable the DELETE and SAVE buttons if no rows remaining
					// after delete
					if (tableModel.getRowCount() == 0) {
						deleteButton.setEnabled(false);
						saveButton.setEnabled(false);
					}

					// Table still contains one or more rows, so highlight
					// (select) the appropriate one.
					else {
						int rowToSelect = rowSelected;

						if (rowSelected >= tableModel.getRowCount()) {
							rowToSelect = rowSelected - 1;
						}

						headerTable.setRowSelectionInterval(rowToSelect, rowToSelect);
					}
				}
			}
		} else if (action.equals("Add")) {
			// If a table cell is being edited, we should accept the current
			// value and stop the editing before adding a new row.
			if (headerTable.isEditing()) {
				TableCellEditor cellEditor = headerTable.getCellEditor(headerTable.getEditingRow(), headerTable
						.getEditingColumn());
				cellEditor.stopCellEditing();
			}

			tableModel.addNewRow();
			tableModel.fireTableDataChanged();

			// Enable the DELETE and SAVE buttons if they are currently
			// disabled.
			if (!deleteButton.isEnabled()) {
				deleteButton.setEnabled(true);
			}
			if (!saveButton.isEnabled()) {
				saveButton.setEnabled(true);
			}

			// Highlight (select) the appropriate row.
			int rowToSelect = tableModel.getRowCount() - 1;
			headerTable.setRowSelectionInterval(rowToSelect, rowToSelect);
		} else if (action.equals("Load")) {
			try {
				File tmp = FileDialoger.promptToOpenFile().getSelectedFile();
				if (tmp != null) {
					tableModel.manager.addFile(tmp.getAbsolutePath());
					tableModel.fireTableDataChanged();

					if (tableModel.getRowCount() > 0) {
						deleteButton.setEnabled(true);
						saveButton.setEnabled(true);
					}
				}
			} catch (IOException ex) {
				log.error("", ex);
			} catch (NullPointerException err) {
			}
		} else if (action.equals("Save")) {
			try {
				File tmp = FileDialoger.promptToSaveFile(null).getSelectedFile();
				if (tmp != null) {
					tableModel.manager.save(tmp.getAbsolutePath());
				}
			} catch (IOException ex) {
				log.error("", ex);
			} catch (NullPointerException err) {
			}
		}
	}

	public JPanel createHeaderTablePanel() {
		// create the JTable that holds header per row
		headerTable = new JTable(tableModel);
		headerTable.addFocusListener(this);
		headerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		headerTable.setPreferredScrollableViewportSize(new Dimension(100, 70));

		JPanel panel = new JPanel(new BorderLayout(0, 5));
		panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("headers_stored")));
		panel.add(new JScrollPane(headerTable), BorderLayout.CENTER);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);
		return panel;
	}

	private JButton createButton(String resName, char mnemonic, String command, boolean enabled) {
		JButton button = new JButton(JMeterUtils.getResString(resName));
		button.setMnemonic(mnemonic);
		button.setActionCommand(command);
		button.setEnabled(enabled);
		button.addActionListener(this);
		return button;
	}

	private JPanel createButtonPanel() {
		boolean tableEmpty = (tableModel.getRowCount() == 0);

		addButton = createButton("add", 'A', ADD_COMMAND, true);
		deleteButton = createButton("delete", 'D', DELETE_COMMAND, !tableEmpty);
		loadButton = createButton("load", 'L', LOAD_COMMAND, true);
		saveButton = createButton("save", 'S', SAVE_COMMAND, !tableEmpty);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(loadButton);
		buttonPanel.add(saveButton);
		return buttonPanel;
	}

	/**
	 * Updates a header record.
	 * 
	 * @version $Revision$
	 */
	class HeaderUpdater implements ActionListener {
		JTextField nameField = new JTextField(20);

		JTextField valueField = new JTextField(20);

		JButton ok = new JButton("Ok");

		JButton cancel = new JButton("Cancel");

		int index;

		JDialog updateDialog;

		public HeaderUpdater() {
		}

		/**
		 * Returns the contructed panel containing the header record.
		 */
		public JPanel getPanel() {
			JPanel main = new JPanel();
			GridBagLayout g = new GridBagLayout();

			main.setLayout(g);
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
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

		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			boolean valid = true;
			index = -1;
			if (command.equals("Edit")) {
				index = headerTable.getSelectedRow();
				if (index < 0) {
					valid = false;
				} else {
					Header c = tableModel.manager.get(index);
					nameField = new JTextField(c.getName(), 20);
					valueField = new JTextField(c.getValue(), 20);
					ok = new JButton("Ok");
					cancel = new JButton("Cancel");
				}
			} else if (command.equals("Add")) {
				nameField = new JTextField(20);
				valueField = new JTextField(20);
				ok = new JButton("Ok");
				cancel = new JButton("Cancel");
			}
			if (valid) {
				if (updateDialog != null) {
					updateDialog.dispose();
				}
				updateDialog = new JDialog();
				updateDialog.setSize(350, 300);

				ok.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						int i = index;
						Header c = new Header();
						if (i >= 0) {
							c = tableModel.manager.get(index);
						}
						c.setName(nameField.getText());
						c.setValue(valueField.getText());
						if (i < 0) {
							tableModel.manager.add(c);
						}
						tableModel.fireTableDataChanged();
						updateDialog.dispose();
					}
				});
				cancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ev) {
						updateDialog.dispose();
					}
				});
				updateDialog.getContentPane().add(getPanel());
				updateDialog.show();
			}
		}
	}

	public void focusGained(FocusEvent e) {
	}

	public void focusLost(FocusEvent e) {
		try {
			headerTable.getCellEditor().stopCellEditing();
		} catch (RuntimeException err) {
		}
	}

	/**
	 * @version $Revision$
	 */
	private static class InnerTableModel extends AbstractTableModel {
		HeaderManager manager;

		public InnerTableModel(HeaderManager man) {
			manager = man;
		}

		public InnerTableModel() {
			manager = new HeaderManager();
		}

		public void removeRow(int row) {
			manager.remove(row);
		}

		public void addNewRow() {
			manager.add();
		}

		public boolean isCellEditable(int row, int column) {
			// all table cells are editable
			return true;
		}

		public Class getColumnClass(int column) {
			return getValueAt(0, column).getClass();
		}

		public int getRowCount() {
			return manager.getHeaders().size();
		}

		/**
		 * Required by table model interface.
		 */
		public int getColumnCount() {
			return manager.getColumnCount();
		}

		/**
		 * Required by table model interface.
		 */
		public String getColumnName(int column) {
			return manager.getColumnName(column);
		}

		/**
		 * Required by table model interface.
		 */
		public Object getValueAt(int row, int column) {
			Header head = manager.getHeader(row);
			if (column == 0) {
				return head.getName();
			} else if (column == 1) {
				return head.getValue();
			}
			return null;
		}

		/**
		 * Required by table model interface.
		 */
		public void setValueAt(Object value, int row, int column) {
			Header header = manager.getHeader(row);

			if (column == 0) {
				header.setName((String) value);
			} else if (column == 1) {
				header.setValue((String) value);
			}
		}

	}
}
