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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.Data;

/**
 * Default config gui for Configuration Element.
 *
 * @author     T.Elanjchezhiyan (chezhiyan@siptech.co.in)
 * @version    $Revision$
 */
public class SimpleConfigGui extends AbstractConfigGui implements ActionListener
{
    /* This class created for enhancement Bug ID 9101. */
    
    // TODO: This class looks a lot like ArgumentsPanel.  What exactly is the
    // difference?  Could they be combined?
    
    /** The table of configuration parameters. */
    private JTable table;
    
    /** The model for the parameter table. */
    private PowerTableModel tableModel;

    /** A button for adding new parameters to the table. */
    private JButton add;
    
    /** A button for removing parameters from the table. */
    private JButton delete;
    
    /** Command for adding a row to the table. */ 
    private static final String ADD = "add";

    /** Command for removing a row from the table. */ 
    private static final String DELETE = "delete";

    /**
     * Boolean indicating whether or not this component should display its
     * name. If true, this is a standalone component. If false, this component
     * is intended to be used as a subpanel for another component.
     */
    private boolean displayName = true;

    /** The names of the columns in the table. */
    public static final String[] COLUMN_NAMES = {
        JMeterUtils.getResString("name"),
        JMeterUtils.getResString("value"),
        JMeterUtils.getResString("metadata")
    };

    /**
     * Create a new standalone SimpleConfigGui.
     */
    public SimpleConfigGui()
    {
        this(true);
    }

    /**
     * Create a new SimpleConfigGui as either a standalone or an embedded
     * component.
     *
     * @param displayName  indicates whether or not this component should
     *                     display its name.  If true, this is a standalone
     *                     component.  If false, this component is intended
     *                     to be used as a subpanel for another component.
     */
    public SimpleConfigGui(boolean displayName)
    {
        this.displayName = displayName;
        init();
    }

    /* Implements JMeterGUIComponent.getStaticLabel() */
    public String getStaticLabel()
    {
        return JMeterUtils.getResString("Simple Config Element");
    }

    /**
     * A newly created component can be initialized with the contents of
     * a Test Element object by calling this method.  The component is
     * responsible for querying the Test Element object for the
     * relevant information to display in its GUI.
     * <p>
     * This implementation retrieves all key/value pairs from the TestElement
     * object and sets these values in the GUI.
     * 
     * @param el the TestElement to configure 
     */
    public  void configure(TestElement el)
    {
        super.configure(el);
        tableModel.clearData();
        PropertyIterator iter = el.propertyIterator();
        while (iter.hasNext())
        {
            JMeterProperty prop = iter.next();
            tableModel.addRow(
                new Object[] { prop.getName(), prop.getStringValue()});
        }
        checkDeleteStatus();
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    public TestElement createTestElement()
    {
        TestElement el = new ConfigTestElement();
        modifyTestElement(el);
        return el;
    }

    /**
     * Get all of the values from the GUI component and set them in the
     * TestElement.
     * 
     * @param el the TestElement to modify
     */
    public void modifyTestElement(TestElement el)
    {
        Data model = tableModel.getData();
        model.reset();
        while (model.next())
        {
            el.setProperty(
                new StringProperty(
                    (String) model.getColumnValue(COLUMN_NAMES[0]),
                    (String) model.getColumnValue(COLUMN_NAMES[1])));
        }
        super.configureTestElement(el);
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init()
    {
        setLayout(new BorderLayout(0, 10));
        
        if (displayName)
        {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        add(createTablePanel(), BorderLayout.CENTER);
        // Force the table to be at least 70 pixels high
        add(Box.createVerticalStrut(70), BorderLayout.WEST);
        add(createButtonPanel(), BorderLayout.SOUTH);
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
     * Create a GUI panel containing the table of configuration parameters.
     *  
     * @return a GUI panel containing the parameter table
     */
    private Component createTablePanel()
    {
        tableModel =
            new PowerTableModel(
                new String[] { COLUMN_NAMES[0], COLUMN_NAMES[1] },
                new Class[] { String.class, String.class });
            
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return makeScrollPane(table);
    }

    /**
     * Create a panel containing the add and delete buttons.
     * 
     * @return a GUI panel containing the buttons
     */
    private JPanel createButtonPanel()
    {
        add = new JButton(JMeterUtils.getResString("add"));
        add.setActionCommand(ADD);
        add.addActionListener(this);
        add.setEnabled(true);
        
        delete = new JButton(JMeterUtils.getResString("delete"));
        delete.setActionCommand(DELETE);
        delete.addActionListener(this);
        
        checkDeleteStatus();
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(add);
        buttonPanel.add(delete);
        return buttonPanel;
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
     * Add a new argument row to the table.
     */
    protected void addArgument()
    {
        // If a table cell is being edited, we should accept the current value
        // and stop the editing before adding a new row.
        stopTableEditing();
                
        tableModel.addNewRow();
        tableModel.fireTableDataChanged();
                
        // Enable DELETE (which may already be enabled, but it won't hurt)
        delete.setEnabled(true);
                
        // Highlight (select) the appropriate row.
        int rowToSelect = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(rowToSelect, rowToSelect);
    }

    /**
     * Stop any editing that is currently being done on the table.  This will
     * save any changes that have already been made.
     */
    protected void stopTableEditing()
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
     * Remove the currently selected argument from the table.
     */
    protected void deleteArgument()
    {
        // If a table cell is being edited, we must cancel the editing before
        // deleting the row
        if (table.isEditing())
        {
            TableCellEditor cellEditor =
                table.getCellEditor(
                    table.getEditingRow(),
                    table.getEditingColumn());
            cellEditor.cancelCellEditing();
        }
                
        int rowSelected = table.getSelectedRow();


        if (rowSelected >= 0)
        {

            //removeProperty(tableModel.getValueAt (
            //    table.getSelectedRow(),0).toString());
            tableModel.removeRow(rowSelected);
            tableModel.fireTableDataChanged();

            // Disable DELETE if there are no rows in the table to delete.
            if (tableModel.getRowCount() == 0)
            {
                delete.setEnabled(false);
            }
            else
            {
                // Table still contains one or more rows, so highlight (select)
                // the appropriate one.
                int rowToSelect = rowSelected;
                
                if (rowSelected >= tableModel.getRowCount())
                {
                    rowToSelect = rowSelected - 1;
                }
                
                table.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        }
    }
}
