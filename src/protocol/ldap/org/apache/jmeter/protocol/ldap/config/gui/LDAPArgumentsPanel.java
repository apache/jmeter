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

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

/**
 * A GUI panel allowing the user to enter name-value argument pairs. These
 * arguments (or parameters) are usually used to provide configuration values
 * for some other component.
 *
 */
public class LDAPArgumentsPanel extends AbstractConfigGui implements ActionListener {

    private static final long serialVersionUID = 240L;

    /** The title label for this component. */
    private JLabel tableLabel;

    /** The table containing the list of arguments. */
    private transient JTable table;

    /** The model for the arguments table. */
    // needs to be accessible from test code
    transient ObjectTableModel tableModel; // Only contains LDAPArgument entries

    /** A button for removing arguments from the table. */
    private JButton delete;

    /** Command for adding a row to the table. */
    private static final String ADD = "add"; //$NON-NLS-1$

    /** Command for removing a row from the table. */
    private static final String DELETE = "delete"; //$NON-NLS-1$

    private static final String[] COLUMN_NAMES = {
            "attribute", //$NON-NLS-1$
            "value",  //$NON-NLS-1$
            "opcode",  //$NON-NLS-1$
            "metadata" }; //$NON-NLS-1$

    /**
     * Create a new LDAPArgumentsPanel, using the default title.
     */
    public LDAPArgumentsPanel() {
        this(JMeterUtils.getResString("paramtable")); //$NON-NLS-1$
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
     * component, so this implementation returns null.
     *
     * @return a Collection of Strings, where each element is one of the
     *         constants defined in MenuFactory
     */
    @Override
    public Collection<String> getMenuCategories() {
        return null;
    }

    @Override
    public String getLabelResource() {
        return "ldapext_sample_title"; // $NON-NLS-1$
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        LDAPArguments args = new LDAPArguments();
        modifyTestElement(args);
        // TODO: Why do we clone the return value? This is the only reference
        // to it (right?) so we shouldn't need a separate copy.
        return (TestElement) args.clone();
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement args) {
        GuiUtils.stopTableEditing(table);
        if (args instanceof LDAPArguments) {
            LDAPArguments arguments = (LDAPArguments) args;
            arguments.clear();
            @SuppressWarnings("unchecked") // Only contains LDAPArgument entries
            Iterator<LDAPArgument> modelData = (Iterator<LDAPArgument>) tableModel.iterator();
            while (modelData.hasNext()) {
                LDAPArgument arg = modelData.next();
                arg.setMetaData("=");
                arguments.addArgument(arg);
            }
        }
        super.configureTestElement(args);
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
    @Override
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
     * Enable or disable the delete button depending on whether or not there is
     * a row to be deleted.
     */
    private void checkDeleteStatus() {
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
    @Override
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
    private void deleteArgument() {
        // If a table cell is being edited, we must cancel the editing before
        // deleting the row
        GuiUtils.cancelEditing(table);

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
    private void addArgument() {
        // If a table cell is being edited, we should accept the current value
        // and stop the editing before adding a new row.
        GuiUtils.stopTableEditing(table);

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
    private LDAPArgument makeNewLDAPArgument() {
        return new LDAPArgument("", "", "");
    }

    /**
     * Initialize the table model used for the arguments table.
     */
    private void initializeTableModel() {
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

    /**
     * Create the main GUI panel which contains the argument table.
     *
     * @return the main GUI panel
     */
    private Component makeMainPanel() {
        initializeTableModel();
        table = new JTable(tableModel);
        JMeterUtils.applyHiDPI(table);
        table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return makeScrollPane(table);
    }

    /**
     * Create a panel containing the title label for the table.
     *
     * @return a panel containing the title label
     */
    private Component makeLabelPanel() {
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
        /* A button for adding new arguments to the table. */
        JButton add = new JButton(JMeterUtils.getResString("add")); //$NON-NLS-1$
        add.setActionCommand(ADD);
        add.setEnabled(true);

        delete = new JButton(JMeterUtils.getResString("delete")); //$NON-NLS-1$
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
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());

        add(makeLabelPanel(), BorderLayout.NORTH);
        add(makeMainPanel(), BorderLayout.CENTER);
        // Force a minimum table height of 70 pixels
        add(Box.createVerticalStrut(70), BorderLayout.WEST);
        add(makeButtonPanel(), BorderLayout.SOUTH);

        table.revalidate();
    }
}
