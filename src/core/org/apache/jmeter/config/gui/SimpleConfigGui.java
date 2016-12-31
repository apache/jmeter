/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.jmeter.config.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.Data;
import org.apache.jorphan.gui.GuiUtils;

/**
 * Default config gui for Configuration Element.
 */
public class SimpleConfigGui extends AbstractConfigGui implements ActionListener {
    /* This class created for enhancement Bug ID 9101. */

    private static final long serialVersionUID = 240L;

    // TODO: This class looks a lot like ArgumentsPanel. What exactly is the
    // difference? Could they be combined?
    // Note: it seems that this class is not actually used ...

    /** The table of configuration parameters. */
    private JTable table;

    /** The model for the parameter table. */
    private PowerTableModel tableModel;

    /** A button for removing parameters from the table. */
    private JButton delete;

    /** Command for adding a row to the table. */
    private static final String ADD = "add";

    /** Command for removing a row from the table. */
    private static final String DELETE = "delete";

    /**
     * Boolean indicating whether or not this component should display its name.
     * If true, this is a standalone component. If false, this component is
     * intended to be used as a subpanel for another component.
     */
    private final boolean displayName;

    /** The resource names of the columns in the table. */
    private static final String COLUMN_NAMES_0 = "name"; // $NON-NLS-1$

    private static final String COLUMN_NAMES_1 = "value"; // $NON-NLS-1$

    /**
     * Create a new standalone SimpleConfigGui.
     */
    public SimpleConfigGui() {
        this(true);
    }

    /**
     * Create a new SimpleConfigGui as either a standalone or an embedded
     * component.
     *
     * @param displayName
     *            indicates whether or not this component should display its
     *            name. If true, this is a standalone component. If false, this
     *            component is intended to be used as a subpanel for another
     *            component.
     */
    public SimpleConfigGui(boolean displayName) {
        this.displayName = displayName;
        init();
    }

    @Override
    public String getLabelResource() {
        return "simple_config_element"; // $NON-NLS-1$
    }

    /**
     * A newly created component can be initialized with the contents of a Test
     * Element object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     * <p>
     * This implementation retrieves all key/value pairs from the TestElement
     * object and sets these values in the GUI.
     *
     * @param el
     *            the TestElement to configure
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        tableModel.clearData();
        PropertyIterator iter = el.propertyIterator();
        while (iter.hasNext()) {
            JMeterProperty prop = iter.next();
            tableModel.addRow(new Object[] { prop.getName(), prop.getStringValue() });
        }
        checkDeleteStatus();
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        TestElement el = new ConfigTestElement();
        modifyTestElement(el);
        return el;
    }

    /**
     * Get all of the values from the GUI component and set them in the
     * TestElement.
     *
     * @param el
     *            the TestElement to modify
     */
    @Override
    public void modifyTestElement(TestElement el) {
        GuiUtils.stopTableEditing(table);
        Data model = tableModel.getData();
        model.reset();
        while (model.next()) {
            el.setProperty(new StringProperty((String) model.getColumnValue(COLUMN_NAMES_0), (String) model
                    .getColumnValue(COLUMN_NAMES_1)));
        }
        super.configureTestElement(el);
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 10));

        if (displayName) {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        add(createTablePanel(), BorderLayout.CENTER);
        // Force the table to be at least 70 pixels high
        add(Box.createVerticalStrut(70), BorderLayout.WEST);
        add(createButtonPanel(), BorderLayout.SOUTH);
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
     * Create a GUI panel containing the table of configuration parameters.
     *
     * @return a GUI panel containing the parameter table
     */
    private Component createTablePanel() {
        tableModel = new PowerTableModel(
                new String[] { COLUMN_NAMES_0, COLUMN_NAMES_1 },
                new Class[] { String.class, String.class });

        table = new JTable(tableModel);
        JMeterUtils.applyHiDPI(table);
        table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return makeScrollPane(table);
    }

    /**
     * Create a panel containing the add and delete buttons.
     *
     * @return a GUI panel containing the buttons
     */
    private JPanel createButtonPanel() {
        /** A button for adding new parameters to the table. */
        JButton add = new JButton(JMeterUtils.getResString("add")); //$NON-NLS-1$
        add.setActionCommand(ADD);
        add.addActionListener(this);
        add.setEnabled(true);

        delete = new JButton(JMeterUtils.getResString("delete")); // $NON-NLS-1$
        delete.setActionCommand(DELETE);
        delete.addActionListener(this);

        checkDeleteStatus();

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(add);
        buttonPanel.add(delete);
        return buttonPanel;
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
     * Add a new argument row to the table.
     */
    protected void addArgument() {
        // If a table cell is being edited, we should accept the current value
        // and stop the editing before adding a new row.
        GuiUtils.stopTableEditing(table);

        tableModel.addNewRow();
        tableModel.fireTableDataChanged();

        // Enable DELETE (which may already be enabled, but it won't hurt)
        delete.setEnabled(true);

        // Highlight (select) the appropriate row.
        int rowToSelect = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(rowToSelect, rowToSelect);
    }

    /**
     * Stop any editing that is currently being done on the table. This will
     * save any changes that have already been made.
     */
    protected void stopTableEditing() {
        GuiUtils.stopTableEditing(table);
    }
    
    /**
     * Remove the currently selected argument from the table.
     */
    protected void deleteArgument() {
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
            } else {
                // Table still contains one or more rows, so highlight (select)
                // the appropriate one.
                int rowToSelect = rowSelected;

                if (rowSelected >= tableModel.getRowCount()) {
                    rowToSelect = rowSelected - 1;
                }

                table.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        }
    }
}
