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
package org.apache.jmeter.protocol.http.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Handles input for determining if authentication services are required for a
 * Sampler. It also understands how to get AuthManagers for the files that the
 * user selects.
 *
 * @author    
 * @version   $Revision$  Last updated: $Date$
 */
public class AuthPanel extends AbstractConfigGui implements ActionListener
{
    transient private static Logger log = LoggingManager.getLoggerForClass();

    private static final String ADD_COMMAND = "Add";
    private static final String DELETE_COMMAND = "Delete";
    private static final String LOAD_COMMAND = "Load";
    private static final String SAVE_COMMAND = "Save";

    private InnerTableModel tableModel;

    /**
     * A table to show the authentication information.
     */
    private JTable authTable;

    private JButton addButton;
    private JButton deleteButton;
    private JButton loadButton;
    private JButton saveButton;

    /**
     * Default Constructor.
     */
    public AuthPanel()
    {
        tableModel = new InnerTableModel();
        init();
    }

    public TestElement createTestElement()
    {
        AuthManager authMan = tableModel.manager;
        configureTestElement(authMan);
        return (TestElement) authMan.clone();
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement el)
    {
        el.clear();
        el.addTestElement((TestElement) tableModel.manager.clone());
        configureTestElement(el);
    }

    public void configure(TestElement el)
    {
        super.configure(el);
        tableModel.manager.clear();
        tableModel.manager.addTestElement((AuthManager) el.clone());
    }

    public String getStaticLabel()
    {
        return JMeterUtils.getResString("auth_manager_title");
    }

    /**
     * Shows the main authentication panel for this object.
     */
    public void init()
    {
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
        add(createAuthTablePanel(), BorderLayout.CENTER);
    }

    public void actionPerformed(ActionEvent e)
    {
        String action = e.getActionCommand();

        if (action.equals(DELETE_COMMAND))
        {
            if (tableModel.getRowCount() > 0)
            {
                // If a table cell is being edited, we must cancel the editing
                // before deleting the row.
                if (authTable.isEditing())
                {
                    TableCellEditor cellEditor =
                        authTable.getCellEditor(
                            authTable.getEditingRow(),
                            authTable.getEditingColumn());
                    cellEditor.cancelCellEditing();
                }

                int rowSelected = authTable.getSelectedRow();

                if (rowSelected != -1)
                {
                    tableModel.removeRow(rowSelected);
                    tableModel.fireTableDataChanged();

                    // Disable the DELETE and SAVE buttons if no rows remaining
                    // after delete.
                    if (tableModel.getRowCount() == 0)
                    {
                        deleteButton.setEnabled(false);
                        saveButton.setEnabled(false);
                    }

                    // Table still contains one or more rows, so highlight
                    // (select) the appropriate one.
                    else
                    {
                        int rowToSelect = rowSelected;

                        if (rowSelected >= tableModel.getRowCount())
                        {
                            rowToSelect = rowSelected - 1;
                        }

                        authTable.setRowSelectionInterval(
                            rowToSelect,
                            rowToSelect);
                    }
                }
            }
        }
        else if (action.equals(ADD_COMMAND))
        {
            // If a table cell is being edited, we should accept the current
            // value and stop the editing before adding a new row.
            if (authTable.isEditing())
            {
                TableCellEditor cellEditor =
                    authTable.getCellEditor(
                        authTable.getEditingRow(),
                        authTable.getEditingColumn());
                cellEditor.stopCellEditing();
            }

            tableModel.addNewRow();
            tableModel.fireTableDataChanged();

            // Enable the DELETE and SAVE buttons if they are currently
            // disabled.
            if (!deleteButton.isEnabled())
            {
                deleteButton.setEnabled(true);
            }
            if (!saveButton.isEnabled())
            {
                saveButton.setEnabled(true);
            }

            // Highlight (select) the appropriate row.
            int rowToSelect = tableModel.getRowCount() - 1;
            authTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        }
        else if (action.equals(LOAD_COMMAND))
        {
            try
            {
                File tmp = FileDialoger.promptToOpenFile().getSelectedFile();
                if (tmp != null)
                {
                    tableModel.manager.addFile(tmp.getAbsolutePath());
                    tableModel.fireTableDataChanged();

                    if (tableModel.getRowCount() > 0)
                    {
                        deleteButton.setEnabled(true);
                        saveButton.setEnabled(true);
                    }
                }
            }
            catch (IOException ex)
            {
                log.error("", ex);
            }
            catch (NullPointerException err)
            {
            }
        }
        else if (action.equals(SAVE_COMMAND))
        {
            try
            {
                File tmp =
                    FileDialoger.promptToSaveFile(null).getSelectedFile();
                if (tmp != null)
                {
                    tableModel.manager.save(tmp.getAbsolutePath());
                }
            }
            catch (IOException ex)
            {
                log.error("", ex);
            }
            catch (NullPointerException err)
            {
            }
        }
    }

    public JPanel createAuthTablePanel()
    {
        // create the JTable that holds auth per row
        authTable = new JTable(tableModel);
        authTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        authTable.setPreferredScrollableViewportSize(new Dimension(100, 70));

        TableColumn passwordColumn = authTable.getColumnModel().getColumn(2);
        passwordColumn.setCellEditor(
            new DefaultCellEditor(new JPasswordField()));
        passwordColumn.setCellRenderer(new PasswordCellRenderer());

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("auths_stored")));
        panel.add(new JScrollPane(authTable));
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JButton createButton(
        String resName,
        char mnemonic,
        String command,
        boolean enabled)
    {
        JButton button = new JButton(JMeterUtils.getResString(resName));
        button.setMnemonic(mnemonic);
        button.setActionCommand(command);
        button.setEnabled(enabled);
        button.addActionListener(this);
        return button;
    }

    private JPanel createButtonPanel()
    {
        boolean tableEmpty = (tableModel.getRowCount() == 0);

        addButton = createButton("add", 'A', ADD_COMMAND, true);
        deleteButton = createButton("delete", 'D', DELETE_COMMAND, !tableEmpty);
        loadButton = createButton("load", 'L', LOAD_COMMAND, true);
        saveButton = createButton("save", 'S', SAVE_COMMAND, !tableEmpty);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        return buttonPanel;
    }

    /**
     * @version   $Revision$
     */
    private class InnerTableModel extends AbstractTableModel
    {
        AuthManager manager;

        public InnerTableModel(AuthManager man)
        {
            manager = man;
        }

        public InnerTableModel()
        {
            manager = new AuthManager();
        }

        public void removeRow(int row)
        {
            manager.remove(row);
        }

        public void addNewRow()
        {
            manager.addAuth();
        }

        public boolean isCellEditable(int row, int column)
        {
            // all table cells are editable
            return true;
        }

        public Class getColumnClass(int column)
        {
            return getValueAt(0, column).getClass();
        }

        /**
         * Required by table model interface.
         */
        public int getRowCount()
        {
            return manager.getAuthObjects().size();
        }

        /**
         * Required by table model interface.
         */
        public int getColumnCount()
        {
            return manager.getColumnCount();
        }

        /**
         * Required by table model interface.
         */
        public String getColumnName(int column)
        {
            return manager.getColumnName(column);
        }

        /**
         * Required by table model interface.
         */
        public Object getValueAt(int row, int column)
        {
            Authorization auth = manager.getAuthObjectAt(row);

            if (column == 0)
            {
                return auth.getURL();
            }
            else if (column == 1)
            {
                return auth.getUser();
            }
            else if (column == 2)
            {
                return auth.getPass();
            }
            return null;
        }

        public void setValueAt(Object value, int row, int column)
        {
            Authorization auth = manager.getAuthObjectAt(row);
            log.debug("Setting auth value: " + value);
            if (column == 0)
            {
                auth.setURL((String) value);
            }
            else if (column == 1)
            {
                auth.setUser((String) value);
            }
            else if (column == 2)
            {
                auth.setPass((String) value);
            }
        }
    }

    /**
     * @version   $Revision$
     */
    private class PasswordCellRenderer
        extends JPasswordField
        implements TableCellRenderer
    {
        private Border myBorder;

        public PasswordCellRenderer()
        {
            super();
            myBorder = new EmptyBorder(1, 2, 1, 2);
            setOpaque(true);
            setBorder(myBorder);
        }

        public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column)
        {
            setText((String) value);

            setBackground(
                isSelected
                    && !hasFocus
                        ? table.getSelectionBackground()
                        : table.getBackground());
            setForeground(
                isSelected
                    && !hasFocus
                        ? table.getSelectionForeground()
                        : table.getForeground());

            setFont(table.getFont());

            return this;
        }
    }
}
