/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.config.gui;

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

import junit.framework.TestCase;

import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A GUI panel allowing the user to enter name-value argument pairs.  These
 * arguments (or parameters) are usually used to provide configuration values
 * for some other component.
 * 
 * @author    Michael Stover
 * @version   $Revision$
 */
public class ArgumentsPanel
    extends AbstractConfigGui
    implements  ActionListener
{
    /** Logging. */
    private static transient Logger log =LoggingManager.getLoggerForClass();
        
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

    public static final String[] COLUMN_NAMES =
        {
            JMeterUtils.getResString("name"),
            JMeterUtils.getResString("value"),
            JMeterUtils.getResString("metadata")
        };

    /**
     * Create a new ArgumentsPanel, using the default title. 
     */
    public ArgumentsPanel()
    {
        this(JMeterUtils.getResString("paramtable"));
    }

    /**
     * Create a new ArgumentsPanel, using the specified title.
     * 
     * @param label the title of the component
     */
    public ArgumentsPanel(String label)
    {
        tableLabel = new JLabel(label);
        init();
    }

    /**
     * This is the list of menu categories this gui component will be available
     * under. The ArgumentsPanel is not intended to be used as a standalone
     * component, so this inplementation returns null.
     *
     * @return   a Collection of Strings, where each element is one of the
     *           constants defined in MenuFactory
     */
    public Collection getMenuCategories()
    {
        return null;
    }

    /* Implements JMeterGUIComponent.getStaticLabel() */
    public String getStaticLabel()
    {
        return "Argument List";
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    public TestElement createTestElement()
    {
        Arguments args = new Arguments();
        modifyTestElement(args);
        // TODO: Why do we clone the return value? This is the only reference
        // to it (right?) so we shouldn't need a separate copy.
        return (TestElement) args.clone();
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    public void modifyTestElement(TestElement args)
    {
        stopTableEditing();
        Iterator modelData = tableModel.iterator();
        Arguments arguments = null;
        if (args instanceof Arguments)
        {
            arguments = (Arguments) args;
            arguments.clear();
            while (modelData.hasNext())
            {
                Argument arg = (Argument) modelData.next();
                arg.setMetaData("=");
                arguments.addArgument(arg);
            }
        }
        this.configureTestElement(args);
    }

    /**
     * A newly created component can be initialized with the contents of
     * a Test Element object by calling this method.  The component is
     * responsible for querying the Test Element object for the
     * relevant information to display in its GUI.
     *
     * @param el the TestElement to configure 
     */
     public void configure(TestElement el)
    {
        super.configure(el);
        if (el instanceof Arguments)
        {
            tableModel.clearData();
            PropertyIterator iter = ((Arguments) el).iterator();
            while (iter.hasNext())
            {
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
    protected JTable getTable()
    {
        return table;
    }

    /**
     * Get the title label for this component.
     * 
     * @return the title label displayed with the table
     */
    protected JLabel getTableLabel()
    {
        return tableLabel;
    }

    /**
     * Get the button used to delete rows from the table.
     * 
     * @return the button used to delete rows from the table
     */
    protected JButton getDeleteButton()
    {
        return delete;
    }

    /**
     * Get the button used to add rows to the table.
     * 
     * @return the button used to add rows to the table
     */
    protected JButton getAddButton()
    {
        return add;
    }

    /**
     * Enable or disable the delete button depending on whether or not there
     * is a row to be deleted.
     */
    protected void checkDeleteStatus()
    {
        // Disable DELETE if there are no rows in the table to delete.
        if (tableModel.getRowCount() == 0)
        {
            delete.setEnabled(false);
        }
        else
        {
            delete.setEnabled(true);
        }
    }

    /**
     * Clear all rows from the table.
     * T.Elanjchezhiyan(chezhiyan@siptech.co.in)
     */
    public void clear()
    {
        tableModel.clearData();
    }

    /**
     * Invoked when an action occurs.  This implementation supports the add
     * and delete buttons.
     * 
     * @param e the event that has occurred
     */
    public void actionPerformed(ActionEvent e)
    {
        String action = e.getActionCommand();
        if (action.equals(DELETE))
        {
            deleteArgument();
        }
        else if (action.equals(ADD))
        {
            addArgument();
        }
    }

    /**
     * Remove the currently selected argument from the table.
     */
    protected void deleteArgument()
    {
        // If a table cell is being edited, we must cancel the editing before
        // deleting the row
        if (table.isEditing())
        {
            TableCellEditor cellEditor = table.getCellEditor(
                    table.getEditingRow(),
                    table.getEditingColumn());
            cellEditor.cancelCellEditing();
        }

        int rowSelected = table.getSelectedRow();
        if (rowSelected >= 0)
        {
            tableModel.removeRow(rowSelected);
            tableModel.fireTableDataChanged();

            // Disable DELETE if there are no rows in the table to delete.
            if (tableModel.getRowCount() == 0)
            {
                delete.setEnabled(false);
            }

            // Table still contains one or more rows, so highlight (select)
            // the appropriate one.
            else
            {
                int rowToSelect = rowSelected;

                if (rowSelected >= tableModel.getRowCount())
                {
                    rowToSelect = rowSelected - 1;
                }

                table.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        }
    }

    /**
     * Add a new argument row to the table.
     */
    protected void addArgument()
    {
        // If a table cell is being edited, we should accept the current value
        // and stop the editing before adding a new row.
        stopTableEditing();

        tableModel.addRow(makeNewArgument());

        // Enable DELETE (which may already be enabled, but it won't hurt)
        delete.setEnabled(true);

        // Highlight (select) the appropriate row.
        int rowToSelect = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(rowToSelect, rowToSelect);
    }

    /**
     * Create a new Argument object.
     * @return a new Argument object
     */
    protected Object makeNewArgument()
    {
        return new Argument("", "");
    }

    /**
     * Stop any editing that is currently being done on the table.  This will
     * save any changes that have already been made.
     */
    private void stopTableEditing()
    {
        if (table.isEditing())
        {
            TableCellEditor cellEditor =
                table.getCellEditor(
                    table.getEditingRow(),
                    table.getEditingColumn());
            cellEditor.stopCellEditing();
        }
    }

    /**
     * Initialize the table model used for the arguments table.
     */
    protected void initializeTableModel()
    {
        tableModel =
            new ObjectTableModel(
                new String[] {
                    COLUMN_NAMES[0],
                    COLUMN_NAMES[1] },
                new String[] { "name", "value" },
                new Class[] { String.class, String.class },
                new Class[] { String.class, String.class },
                new Argument());
    }

    /**
     * Resize the table columns to appropriate widths.
     * @param table the table to resize columns for
     */
    protected void sizeColumns(JTable table)
    {
    }

    /**
     * Create the main GUI panel which contains the argument table.
     * 
     * @return the main GUI panel
     */
    private Component makeMainPanel()
    {
        initializeTableModel();
        table = new JTable(tableModel);
        //table.addFocusListener(this);
        // use default editor/renderer to fix bug #16058
        //    TextAreaTableCellEditor editor = new TextAreaTableCellEditor();
        //    table.setDefaultEditor(String.class, editor);
        //    editor.addCellEditorListener(this);
        //    TextAreaCellRenderer renderer = new TextAreaCellRenderer();
        //    table.setRowHeight(renderer.getPreferredHeight());
        //    table.setDefaultRenderer(String.class,renderer);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return makeScrollPane(table);
    }

    /**
     * Create a panel containing the title label for the table.
     * 
     * @return a panel containing the title label
     */
    protected Component makeLabelPanel()
    {
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        labelPanel.add(tableLabel);
        return labelPanel;
    }

    /**
     * Create a panel containing the add and delete buttons.
     * 
     * @return a GUI panel containing the buttons
     */
    private JPanel makeButtonPanel()
    {
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
    private void init()
    {
        setLayout(new BorderLayout());

        add(makeLabelPanel(), BorderLayout.NORTH);
        add(makeMainPanel(), BorderLayout.CENTER);
        // Force a minimum table height of 70 pixels
        add(Box.createVerticalStrut(70), BorderLayout.WEST);
        add(makeButtonPanel(), BorderLayout.SOUTH);

        table.revalidate();
        sizeColumns(table);
    }


    /**
     * Tests for the ArgumentsPanel component.
     */
    public static class Test extends TestCase
    {
        /**
         * Create a new test.
         * @param name the name of the test
         */
        public Test(String name)
        {
            super(name);
        }

        /**
         * Test that adding an argument to the table results in an appropriate
         * TestElement being created.
         * 
         * @throws Exception if an exception occurred during the test
         */
        public void testArgumentCreation() throws Exception
        {
            ArgumentsPanel gui = new ArgumentsPanel();
            gui.tableModel.addRow(new Argument());
            gui.tableModel.setValueAt("howdy", 0, 0);
            gui.tableModel.addRow(new Argument());
            gui.tableModel.setValueAt("doody", 0, 1);

            assertEquals(
                "=",
                ((Argument) ((Arguments) gui.createTestElement())
                    .getArguments()
                    .get(0)
                    .getObjectValue())
                    .getMetaData());
        }
    }
}
