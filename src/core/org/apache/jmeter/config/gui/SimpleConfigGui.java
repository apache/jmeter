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

package org.apache.jmeter.config.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.Data;
import org.apache.jorphan.gui.layout.VerticalLayout;

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
        Iterator iter=el.getPropertyNames().iterator();
        while(iter.hasNext())
        {
            String str = (String)iter.next();
            tableModel.addRow(new Object[]{str,el.getProperty(str)});
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
            el.setProperty((String)model.getColumnValue(COLUMN_NAMES[0]),
                                model.getColumnValue(COLUMN_NAMES[1]));
        }
        super.configureTestElement(el);
    }


    private void init()
    {
        this.setLayout(new BorderLayout(0, 0));
        this.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        if(displayName)
        {
            this.add(makeTitlePanel(),BorderLayout.NORTH);
            
        }
        this.createTablePanel();
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

    protected void createTablePanel()
    {
        tableModel = new PowerTableModel(new String[]{COLUMN_NAMES[0],COLUMN_NAMES[1]},
                                         new Class[]{String.class,String.class});
            
        JPanel tPanel=new JPanel();
        tPanel.setLayout(new VerticalLayout (1, VerticalLayout.CENTER));
        table = new JTable(tableModel);
        table.setEnabled(true);
        // table.addFocusListener(this);
        table.setCellSelectionEnabled(true);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scroller = new JScrollPane(table);
        Dimension tableDim = scroller.getPreferredSize();
        tableDim.height = 300;
        scroller.setPreferredSize(tableDim);
        scroller.setColumnHeaderView(table.getTableHeader());

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

        this.add(scroller, BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

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
