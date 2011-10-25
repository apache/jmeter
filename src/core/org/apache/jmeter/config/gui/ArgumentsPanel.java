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

package org.apache.jmeter.config.gui;

import java.awt.BorderLayout;
import java.awt.Color;
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

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

/**
 * A GUI panel allowing the user to enter name-value argument pairs. These
 * arguments (or parameters) are usually used to provide configuration values
 * for some other component.
 *
 */
public class ArgumentsPanel extends AbstractConfigGui implements ActionListener {

    private static final long serialVersionUID = 240L;

    /** The title label for this component. */
    private JLabel tableLabel;

    /** The table containing the list of arguments. */
    private transient JTable table;

    /** The model for the arguments table. */
    protected transient ObjectTableModel tableModel; // will only contain Argument or HTTPArgument

    /** A button for adding new arguments to the table. */
    private JButton add;

    /** A button for removing arguments from the table. */
    private JButton delete;

    /**
     * Added background support for reporting tool
     */
    private Color background;

    /**
     * Boolean indicating whether this component is a standalong component or it
     * is intended to be used as a subpanel for another component.
     */
    private final boolean standalone;

    /** Button to move a argument up*/
    private JButton up;

    /** Button to move a argument down*/
    private JButton down;

    private final boolean enableUpDown;

    /** Command for adding a row to the table. */
    private static final String ADD = "add"; // $NON-NLS-1$

    /** Command for removing a row from the table. */
    private static final String DELETE = "delete"; // $NON-NLS-1$

    /** Command for moving a row up in the table. */
    private static final String UP = "up"; // $NON-NLS-1$

    /** Command for moving a row down in the table. */
    private static final String DOWN = "down"; // $NON-NLS-1$

    public static final String COLUMN_RESOURCE_NAMES_0 = "name"; // $NON-NLS-1$

    public static final String COLUMN_RESOURCE_NAMES_1 = "value"; // $NON-NLS-1$

    public static final String COLUMN_RESOURCE_NAMES_2 = "description"; // $NON-NLS-1$

    /**
     * Create a new ArgumentsPanel as a standalone component.
     */
    public ArgumentsPanel() {
        this(JMeterUtils.getResString("user_defined_variables"),null, true, true);// $NON-NLS-1$
    }

    /**
     * Create a new ArgumentsPanel as an embedded component, using the specified
     * title.
     *
     * @param label
     *            the title for the component.
     */
    public ArgumentsPanel(String label) {
        this(label, null, true, false);
    }
    
    /**
     * Create a new ArgumentsPanel as an embedded component, using the specified
     * title.
     *
     * @param label
     *            the title for the component.
     * @param enableUpDown Add up/down buttons
     */
    public ArgumentsPanel(String label, boolean enableUpDown) {
        this(label, null, enableUpDown, false);
    }

    /**
     * Create a new ArgumentsPanel with a border and color background
     * @param label text for label
     * @param bkg background colour
     */
    public ArgumentsPanel(String label, Color bkg) {
        this(label, bkg, true, false);
    }
    
    /**
     * Create a new ArgumentsPanel with a border and color background
     * @param label text for label
     * @param bkg background colour
     * @param enableUpDown Add up/down buttons
     * @param standalone is standalone
     */
    public ArgumentsPanel(String label, Color bkg, boolean enableUpDown, boolean standalone) {
        tableLabel = new JLabel(label);
        this.enableUpDown = enableUpDown;
        this.background = bkg;
        this.standalone = standalone;
        init();
    }

    /**
     * This is the list of menu categories this gui component will be available
     * under.
     *
     * @return a Collection of Strings, where each element is one of the
     *         constants defined in MenuFactory
     */
    @Override
    public Collection<String> getMenuCategories() {
        if (standalone) {
            return super.getMenuCategories();
        }
        return null;
    }

    public String getLabelResource() {
        return "user_defined_variables"; // $NON-NLS-1$
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    public TestElement createTestElement() {
        Arguments args = new Arguments();
        modifyTestElement(args);
        return args;
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    public void modifyTestElement(TestElement args) {
        stopTableEditing();
        Arguments arguments = null;
        if (args instanceof Arguments) {
            arguments = (Arguments) args;
            arguments.clear();
            @SuppressWarnings("unchecked") // only contains Argument (or HTTPArgument)
            Iterator<Argument> modelData = (Iterator<Argument>) tableModel.iterator();
            while (modelData.hasNext()) {
                Argument arg = modelData.next();
                arg.setMetaData("="); // $NON-NLS-1$
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
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof Arguments) {
            tableModel.clearData();
            PropertyIterator iter = ((Arguments) el).iterator();
            while (iter.hasNext()) {
                Argument arg = (Argument) iter.next().getObjectValue();
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
        
        if(enableUpDown && tableModel.getRowCount()>1) {
            up.setEnabled(true);
            down.setEnabled(true);
        }
    }

    @Override
    public void clearGui(){
        super.clearGui();
        clear();
    }

    /**
     * Clear all rows from the table. T.Elanjchezhiyan(chezhiyan@siptech.co.in)
     */
    public void clear() {
        stopTableEditing();
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
        } else if (action.equals(UP)) {
            moveUp();
        } else if (action.equals(DOWN)) {
            moveDown();
        }
    }

    /**
     * Cancel cell editing if it is being edited
     */
    private void cancelEditing() {
        // If a table cell is being edited, we must cancel the editing before
        // deleting the row
        if (table.isEditing()) {
            TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
            cellEditor.cancelCellEditing();
        }
    }
    
    /**
     * Move a row down
     */
    private void moveDown() {
        cancelEditing();

        int rowSelected = table.getSelectedRow();
        if (rowSelected >=0 && rowSelected < table.getRowCount()-1) {
            tableModel.moveRow(rowSelected, rowSelected+1, rowSelected+1);
            table.setRowSelectionInterval(rowSelected+1, rowSelected+1);
        }
    }

    /**
     *  Move a row down
     */
    private void moveUp() {
        cancelEditing();

        int rowSelected = table.getSelectedRow();
        if (rowSelected > 0) {
            tableModel.moveRow(rowSelected, rowSelected+1, rowSelected-1);
            table.setRowSelectionInterval(rowSelected-1, rowSelected-1);
        } 
    }

    /**
     * Remove the currently selected argument from the table.
     */
    protected void deleteArgument() {
        cancelEditing();

        int rowSelected = table.getSelectedRow();
        if (rowSelected >= 0) {
            tableModel.removeRow(rowSelected);
            tableModel.fireTableDataChanged();

            // Disable DELETE if there are no rows in the table to delete.
            if (tableModel.getRowCount() == 0) {
                delete.setEnabled(false);
            }
            
            if(enableUpDown && tableModel.getRowCount()>1) {
                up.setEnabled(true);
                down.setEnabled(true);
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

        tableModel.addRow(makeNewArgument());

        // Enable DELETE (which may already be enabled, but it won't hurt)
        delete.setEnabled(true);
        if(enableUpDown && tableModel.getRowCount()>1) {
            up.setEnabled(true);
            down.setEnabled(true);
        }
        // Highlight (select) the appropriate row.
        int rowToSelect = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(rowToSelect, rowToSelect);
    }

    /**
     * Create a new Argument object.
     *
     * @return a new Argument object
     */
    protected Argument makeNewArgument() {
        return new Argument("", ""); // $NON-NLS-1$ // $NON-NLS-2$
    }

    /**
     * Stop any editing that is currently being done on the table. This will
     * save any changes that have already been made.
     */
    protected void stopTableEditing() {
        if (table.isEditing()) {
            TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
            cellEditor.stopCellEditing();
        }
    }

    /**
     * Initialize the table model used for the arguments table.
     */
    protected void initializeTableModel() {
        if(standalone) {
            tableModel = new ObjectTableModel(new String[] { COLUMN_RESOURCE_NAMES_0, COLUMN_RESOURCE_NAMES_1, COLUMN_RESOURCE_NAMES_2 },
                    Argument.class,
                    new Functor[] {
                    new Functor("getName"), // $NON-NLS-1$
                    new Functor("getValue"),  // $NON-NLS-1$
                    new Functor("getDescription") },  // $NON-NLS-1$
                    new Functor[] {
                    new Functor("setName"), // $NON-NLS-1$
                    new Functor("setValue"), // $NON-NLS-1$
                    new Functor("setDescription") },  // $NON-NLS-1$
                    new Class[] { String.class, String.class, String.class });
        } else {
        tableModel = new ObjectTableModel(new String[] { COLUMN_RESOURCE_NAMES_0, COLUMN_RESOURCE_NAMES_1 },
                Argument.class,
                new Functor[] {
                new Functor("getName"), // $NON-NLS-1$
                new Functor("getValue") },  // $NON-NLS-1$
                new Functor[] {
                new Functor("setName"), // $NON-NLS-1$
                new Functor("setValue") }, // $NON-NLS-1$
                new Class[] { String.class, String.class });
    	}
    }

    public static boolean testFunctors(){
        ArgumentsPanel instance = new ArgumentsPanel();
        instance.initializeTableModel();
        return instance.tableModel.checkFunctors(null,instance.getClass());
    }

    /**
     * Resize the table columns to appropriate widths.
     *
     * @param _table
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
        table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (this.background != null) {
            table.setBackground(this.background);
        }
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
        if (this.background != null) {
            labelPanel.setBackground(this.background);
        }
        return labelPanel;
    }

    /**
     * Create a panel containing the add and delete buttons.
     *
     * @return a GUI panel containing the buttons
     */
    private JPanel makeButtonPanel() {
        add = new JButton(JMeterUtils.getResString("add")); // $NON-NLS-1$
        add.setActionCommand(ADD);
        add.setEnabled(true);

        delete = new JButton(JMeterUtils.getResString("delete")); // $NON-NLS-1$
        delete.setActionCommand(DELETE);

        if(enableUpDown) {
            up = new JButton(JMeterUtils.getResString("up")); // $NON-NLS-1$
            up.setActionCommand(UP);
    
            down = new JButton(JMeterUtils.getResString("down")); // $NON-NLS-1$
            down.setActionCommand(DOWN);
        }
        checkDeleteStatus();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        if (this.background != null) {
            buttonPanel.setBackground(this.background);
        }
        add.addActionListener(this);
        delete.addActionListener(this);
        buttonPanel.add(add);
        buttonPanel.add(delete);
        if(enableUpDown) {
            up.addActionListener(this);
            down.addActionListener(this);
            buttonPanel.add(up);
            buttonPanel.add(down);
        }
        return buttonPanel;
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() {
        JPanel p = this;

        if (standalone) {
            setLayout(new BorderLayout(0, 5));
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
            p = new JPanel();
        }

        p.setLayout(new BorderLayout());

        p.add(makeLabelPanel(), BorderLayout.NORTH);
        p.add(makeMainPanel(), BorderLayout.CENTER);
        // Force a minimum table height of 70 pixels
        p.add(Box.createVerticalStrut(70), BorderLayout.WEST);
        p.add(makeButtonPanel(), BorderLayout.SOUTH);

        if (standalone) {
            add(p, BorderLayout.CENTER);
        }

        table.revalidate();
        sizeColumns(table);
    }
}
