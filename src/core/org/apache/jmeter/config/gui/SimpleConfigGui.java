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
import javax.swing.JScrollPane;
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

/************************************************************
 *  Title: JMeter Description :Default config gui for Configuration Element
 *
 *@author     T.Elanjchezhiyan (chezhiyan@siptech.co.in)
 *@created    March 21 2003
 *@version    Bug Enhancement Bug ID-9101
 ***********************************************************/

public class SimpleConfigGui extends AbstractConfigGui implements ActionListener
{
    JTable table;
    JButton add;
    JButton delete;
    protected PowerTableModel tableModel;
    public static final String TVALUE="SimpleConfigGui.tvalue";
    private boolean displayName = true;
    private static String ADD = "add";
    private static String DELETE = "delete";


    public static String[] COLUMN_NAMES = {
        JMeterUtils.getResString("name"),
        JMeterUtils.getResString("value"),
        JMeterUtils.getResString("metadata")
    };

    /****************************************
     * Constructor for the SimpleConfigGui object
     ***************************************/
    public SimpleConfigGui()
    {
        this(true);
    }

    /****************************************
     * Constructor for the SimpleConfigGui object
     *
     *@param displayName  Description of Parameter
     ***************************************/
    public SimpleConfigGui(boolean displayName)
    {
        this.displayName = displayName;
        init();
    }

    public String getStaticLabel()
    {
        return JMeterUtils.getResString("Simple Config Element");
    }

    /**************************************************************
     *Retrive all the key value pair from the TestElement object
     *and set  back (all the value in)to the component in the GUI.
     **************************************************************/
    public  void configure(TestElement el){
        super.configure(el);
        tableModel.clearData();
        PropertyIterator iter=el.propertyIterator();
        while(iter.hasNext())
        {
            JMeterProperty prop = iter.next();
            tableModel.addRow(new Object[]{prop.getName(),prop.getStringValue()});
        }
        checkDeleteStatus();
    }
    
    public TestElement createTestElement()
    {
        TestElement el = new ConfigTestElement();
        modifyTestElement(el);
        return el;
    }

    /*************************************************************
     *Get all the value from the component in the GUI and
     *configure with the TestElement object
     ************************************************************/

    public void modifyTestElement(TestElement el) {
        Data model = tableModel.getData();
        model.reset();
        while(model.next())
        {
            el.setProperty(new StringProperty((String)model.getColumnValue(COLUMN_NAMES[0]),
                                (String)model.getColumnValue(COLUMN_NAMES[1])));
        }
        super.configureTestElement(el);
    }


    private void init()
    {
        setLayout(new BorderLayout(0, 5));
        
        if (displayName) {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        add(createTablePanel(), BorderLayout.CENTER);
        // Force the table to be at least 70 pixels high
        add(Box.createVerticalStrut(70), BorderLayout.WEST);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }



    public void actionPerformed(ActionEvent e)
    {
        String action = e.getActionCommand();
        if(action.equals(DELETE))
        {
            deleteArgument();
        }
        else if(action.equals(ADD))
        {
            addArgument();
        }
    }

    private Component createTablePanel() {
        tableModel = new PowerTableModel(new String[]{COLUMN_NAMES[0],COLUMN_NAMES[1]},
                                         new Class[]{String.class,String.class});
            
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return new JScrollPane(table);
    }

    private JPanel createButtonPanel() {
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
    protected void checkDeleteStatus() {
        // Disable DELETE if there are no rows in the table to delete.
        if(tableModel.getRowCount() == 0)
        {
            delete.setEnabled(false);
        }
        else
        {
            delete.setEnabled(true);
        }
    }

    protected void addArgument() {
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

    protected void stopTableEditing()
    {
        if(table.isEditing())
        {
            TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
            cellEditor.stopCellEditing();
        }
    }

    protected void deleteArgument() {
        // If a table cell is being edited, we must cancel the editing before
        // deleting the row
        if(table.isEditing())
        {
            TableCellEditor cellEditor = table.getCellEditor(table.getEditingRow(), table.getEditingColumn());
            cellEditor.cancelCellEditing();
        }
                
        int rowSelected = table.getSelectedRow();


        if(rowSelected >= 0)
        {

            //removeProperty(tableModel.getValueAt(table.getSelectedRow(),0).toString());
            tableModel.removeRow(rowSelected);
            tableModel.fireTableDataChanged();

            // Disable DELETE if there are no rows in the table to delete.
            if(tableModel.getRowCount() == 0)
            {
                delete.setEnabled(false);
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
                
                table.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        }
    }

    
}
