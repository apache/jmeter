/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.jmeter.protocol.ldap.config.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ObjectTableModel;
//import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
//import org.apache.log.Logger;

/**
 * A GUI panel allowing the user to enter name-value argument pairs. These
 * arguments (or parameters) are usually used to provide configuration values
 * for some other component.
 * 
 * author Dolf Smits(Dolf.Smits@Siemens.com) created Aug 09 2003 11:00 AM
 * company Siemens Netherlands N.V..
 * 
 * Based on the work of:
 * 
 * author Michael Stover
 */

public class LDAPArgumentsPanel extends AbstractConfigGui implements ActionListener {
	/** Logging. */
	//private static final Logger log = LoggingManager.getLoggerForClass();

	/** The title label for this component. */
	private JLabel tableLabel;

	/** The table containing the list of arguments. */
	private transient JTable table;

	/** The model for the arguments table. */
	protected transient ObjectTableModel tableModel;

	/** A button for adding new arguments to the table. */
	private JButton add;

	/** A button for removing arguments from the table. */
	private JButton delete;

	/** Command for adding a row to the table. */
	private static final String ADD = "add";

	/** Command for removing a row from the table. */
	private static final String DELETE = "delete";

	private static final String[] COLUMN_NAMES = { JMeterUtils.getResString("attribute"),
			JMeterUtils.getResString("value"), JMeterUtils.getResString("opcode"), JMeterUtils.getResString("metadata") };

	/**
	 * Create a new LDAPArgumentsPanel, using the default title.
	 */
	public LDAPArgumentsPanel() {
		this(JMeterUtils.getResString("paramtable"));
	}

	/**
	 * Create a new LDAPArgumentsPanel, using the specified title.
	 * 
	 * @param label
	 *            the title of the component
	 */
	public LDAPArgumentsPanel(String label) {
		tableLabel = new JLabel(label);
		init();
	}

	/**
	 * This is the list of menu categories this gui component will be available
	 * under. The LDAPArgumentsPanel is not intended to be used as a standalone
	 * component, so this inplementation returns null.
	 * 
	 * @return a Collection of Strings, where each element is one of the
	 *         constants defined in MenuFactory
	 */
	public Collection getMenuCategories() {
		return null;
	}

	public String getStaticLabel() {
		return ""; // This is not an independently displayable item
	}

	public String getLabelResource() {
		return "unused";// TODO use constant
	}

	/* Implements JMeterGUIComponent.createTestElement() */
	public TestElement createTestElement() {
		LDAPArguments args = new LDAPArguments();
		modifyTestElement(args);
		// TODO: Why do we clone the return value? This is the only reference
		// to it (right?) so we shouldn't need a separate copy.
		return (TestElement) args.clone();
	}

	/* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
	public void modifyTestElement(TestElement args) {
		stopTableEditing();
		Iterator modelData = tableModel.iterator();
		LDAPArguments arguments = null;
		if (args instanceof LDAPArguments) {
			arguments = (LDAPArguments) args;
			arguments.clear();
			while (modelData.hasNext()) {
				LDAPArgument arg = (LDAPArgument) modelData.next();
				arg.setMetaData("=");
				arguments.addArgument(arg);
			}
		}
		this.configureTestElement(args);
	}

	/**
	 * A newly created component can be initialized with the contents of a Test
	 * Element object by calling this method. The component is responsible for
	 * querying the Test Element object for the relevant information to display
	 * in its GUI.
	 * 
	 * @param el
	 *            the TestElement to configure
	 */
	public void configure(TestElement el) {
		super.configure(el);
		if (el instanceof LDAPArguments) {
			tableModel.clearData();
			PropertyIterator iter = ((LDAPArguments) el).iterator();
			while (iter.hasNext()) {
				LDAPArgument arg = (LDAPArgument) iter.next().getObjectValue();
				tableModel.addRow(arg);
			}
		}
		checkDeleteStatus();
	}

	/**
	 * Get the table used to enter arguments.
	 * 
	 * @return the table used to enter arguments
	 */
	protected JTable getTable() {
		return table;
	}

	/**
	 * Get the title label for this component.
	 * 
	 * @return the title label displayed with the table
	 */
	protected JLabel getTableLabel() {
		return tableLabel;
	}

	/**
	 * Get the button used to delete rows from the table.
	 * 
	 * @return the button used to delete rows from the table
	 */
	protected JButton getDeleteButton() {
		return delete;
	}

	/**
	 * Get the button used to add rows to the table.
	 * 
	 * @return the button used to add rows to the table
	 */
	protected JButton getAddButton() {
		return add;
	}

	/**
	 * Enable or disable the delete button depending on whether or not there is
	 * a row to be deleted.
	 */
	protected void checkDeleteStatus() {
		// Disable DELETE if there are no rows in the table to delete.
		if (tableModel.getRowCount() == 0) {
			delete.setEnabled(false);
		} else {
			delete.setEnabled(true);
		}
	}

	/**
	 * Clear all rows from the table. T.Elanjchezhiyan(chezhiyan@siptech.co.in)
	 */
	public void clear() {
		tableModel.clearData();
	}

	/**
	 * Invoked when an action occurs. This implementation supports the add and
	 * delete buttons.
	 * 
	 * @param e
	 *            the event that has occurred
	 */
	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();
		if (action.equals(DELETE)) {
			deleteArgument();
		} else if (action.equals(ADD)) {
			addArgument();
		}
	}

	/**
	 * Remove the currently selected argument from the table.
	 */
	protected void deleteArgument() {
		// If a table cell is being edited, we must cancel the editing before
		// deleting the row
		if (table.isEditing()) {
			TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
			cellEditor.cancelCellEditing();
		}

		int rowSelected = table.getSelectedRow();
		if (rowSelected >= 0) {
			tableModel.removeRow(rowSelected);
			tableModel.fireTableDataChanged();

			// Disable DELETE if there are no rows in the table to delete.
			if (tableModel.getRowCount() == 0) {
				delete.setEnabled(false);
			}

			// Table still contains one or more rows, so highlight (select)
			// the appropriate one.
			else {
				int rowToSelect = rowSelected;

				if (rowSelected >= tableModel.getRowCount()) {
					rowToSelect = rowSelected - 1;
				}

				table.setRowSelectionInterval(rowToSelect, rowToSelect);
			}
		}
	}

	/**
	 * Add a new argument row to the table.
	 */
	protected void addArgument() {
		// If a table cell is being edited, we should accept the current value
		// and stop the editing before adding a new row.
		stopTableEditing();

		tableModel.addRow(makeNewLDAPArgument());

		// Enable DELETE (which may already be enabled, but it won't hurt)
		delete.setEnabled(true);

		// Highlight (select) the appropriate row.
		int rowToSelect = tableModel.getRowCount() - 1;
		table.setRowSelectionInterval(rowToSelect, rowToSelect);
	}

	/**
	 * Create a new LDAPArgument object.
	 * 
	 * @return a new LDAPArgument object
	 */
	protected Object makeNewLDAPArgument() {
		return new LDAPArgument("", "", "");
	}

	/**
	 * Stop any editing that is currently being done on the table. This will
	 * save any changes that have already been made.
	 */
	private void stopTableEditing() {
		if (table.isEditing()) {
			TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
			cellEditor.stopCellEditing();
		}
	}

	/**
	 * Initialize the table model used for the arguments table.
	 */
	protected void initializeTableModel() {
		tableModel = new ObjectTableModel(new String[] { COLUMN_NAMES[0], COLUMN_NAMES[1], COLUMN_NAMES[2] },
				LDAPArgument.class,
				new Functor[] { new Functor("getName"), new Functor("getValue"), new Functor("getOpcode") },
				new Functor[] { new Functor("setName"), new Functor("setValue"), new Functor("setOpcode") },
				new Class[] { String.class, String.class, String.class });
	}

	public static boolean testFunctors(){
		LDAPArgumentsPanel instance = new LDAPArgumentsPanel();
		instance.initializeTableModel();
		return instance.tableModel.checkFunctors(null,instance.getClass());
	}
	
	/*
	 * protected void initializeTableModel() { tableModel = new
	 * ObjectTableModel( new String[] { ArgumentsPanel.COLUMN_NAMES_0,
	 * ArgumentsPanel.COLUMN_NAMES_1, ENCODE_OR_NOT, INCLUDE_EQUALS }, new
	 * Functor[] { new Functor("getName"), new Functor("getValue"), new
	 * Functor("isAlwaysEncoded"), new Functor("isUseEquals") }, new Functor[] {
	 * new Functor("setName"), new Functor("setValue"), new
	 * Functor("setAlwaysEncoded"), new Functor("setUseEquals") }, new Class[] {
	 * String.class, String.class, Boolean.class, Boolean.class }); }
	 */
	/**
	 * Resize the table columns to appropriate widths.
	 * 
	 * @param table
	 *            the table to resize columns for
	 */
	protected void sizeColumns(JTable _table) {
	}

	/**
	 * Create the main GUI panel which contains the argument table.
	 * 
	 * @return the main GUI panel
	 */
	private Component makeMainPanel() {
		initializeTableModel();
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		return makeScrollPane(table);
	}

	/**
	 * Create a panel containing the title label for the table.
	 * 
	 * @return a panel containing the title label
	 */
	protected Component makeLabelPanel() {
		JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		labelPanel.add(tableLabel);
		return labelPanel;
	}

	/**
	 * Create a panel containing the add and delete buttons.
	 * 
	 * @return a GUI panel containing the buttons
	 */
	private JPanel makeButtonPanel() {
		add = new JButton(JMeterUtils.getResString("add"));
		add.setActionCommand(ADD);
		add.setEnabled(true);

		delete = new JButton(JMeterUtils.getResString("delete"));
		delete.setActionCommand(DELETE);

		checkDeleteStatus();

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
		add.addActionListener(this);
		delete.addActionListener(this);
		buttonPanel.add(add);
		buttonPanel.add(delete);
		return buttonPanel;
	}

	/**
	 * Initialize the components and layout of this component.
	 */
	private void init() {
		setLayout(new BorderLayout());

		add(makeLabelPanel(), BorderLayout.NORTH);
		add(makeMainPanel(), BorderLayout.CENTER);
		// Force a minimum table height of 70 pixels
		add(Box.createVerticalStrut(70), BorderLayout.WEST);
		add(makeButtonPanel(), BorderLayout.SOUTH);

		table.revalidate();
		sizeColumns(table);
	}
}
