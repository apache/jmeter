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

package org.apache.jmeter.protocol.http.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.protocol.http.control.AuthManager;
import org.apache.jmeter.protocol.http.control.AuthManager.Mechanism;
import org.apache.jmeter.protocol.http.control.Authorization;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * Handles input for determining if authentication services are required for a
 * Sampler. It also understands how to get AuthManagers for the files that the
 * user selects.
 */
public class AuthPanel extends AbstractConfigGui implements ActionListener {
    private static final long serialVersionUID = -378312656300713635L;

    private static final Logger log = LoggerFactory.getLogger(AuthPanel.class);

    private static final String ADD_COMMAND = "Add"; //$NON-NLS-1$

    private static final String DELETE_COMMAND = "Delete"; //$NON-NLS-1$

    private static final String LOAD_COMMAND = "Load"; //$NON-NLS-1$

    private static final String SAVE_COMMAND = "Save"; //$NON-NLS-1$

    private InnerTableModel tableModel;

    private JCheckBox clearEachIteration;

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
    public AuthPanel() {
        tableModel = new InnerTableModel();
        init();
    }

    @Override
    public TestElement createTestElement() {
        AuthManager authMan = tableModel.manager;
        configureTestElement(authMan);
        authMan.setClearEachIteration(clearEachIteration.isSelected());
        return (TestElement) authMan.clone();
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement el) {
        GuiUtils.stopTableEditing(authTable);
        AuthManager authManager = (AuthManager) el;
        authManager.clear();
        authManager.addTestElement((TestElement) tableModel.manager.clone());
        authManager.setClearEachIteration(clearEachIteration.isSelected());
        configureTestElement(el);
    }

    /**
     * Implements JMeterGUIComponent.clear
     */
    @Override
    public void clearGui() {
        super.clearGui();

        tableModel.clearData();
        deleteButton.setEnabled(false);
        saveButton.setEnabled(false);
        clearEachIteration.setSelected(false);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        tableModel.manager.clear();
        tableModel.manager.addTestElement((AuthManager) el.clone());
        clearEachIteration.setSelected(((AuthManager) el).getClearEachIteration());
        checkButtonsStatus();
    }

    @Override
    public String getLabelResource() {
        return "auth_manager_title"; //$NON-NLS-1$
    }

    /**
     * Shows the main authentication panel for this object.
     */
    private void init() {// called from ctor, so must not be overridable
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        northPanel.add(makeTitlePanel());

        JPanel optionsPane = new JPanel();
        optionsPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("auth_manager_options"))); // $NON-NLS-1$
        optionsPane.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        clearEachIteration = 
                new JCheckBox(JMeterUtils.getResString("auth_manager_clear_per_iter"), false); //$NON-NLS-1$
        optionsPane.add(clearEachIteration);
        northPanel.add(optionsPane);
        add(northPanel, BorderLayout.NORTH);

        
        add(createAuthTablePanel(), BorderLayout.CENTER);
    }

    /**
     * Remove the currently selected argument from the table.
     */
    protected void deleteRows() {
        // If a table cell is being edited, we must cancel the editing
        // before deleting the row.
        GuiUtils.cancelEditing(authTable);

        int[] rowsSelected = authTable.getSelectedRows();
        int anchorSelection = authTable.getSelectionModel().getAnchorSelectionIndex();
        authTable.clearSelection();
        if (rowsSelected.length > 0) {
            for (int i = rowsSelected.length - 1; i >= 0; i--) {
                tableModel.removeRow(rowsSelected[i]);
            }
            tableModel.fireTableDataChanged();

            // Table still contains one or more rows, so highlight (select)
            // the appropriate one.
            if (tableModel.getRowCount() > 0) {
                if (anchorSelection >= tableModel.getRowCount()) {
                    anchorSelection = tableModel.getRowCount() - 1;
                }
                authTable.setRowSelectionInterval(anchorSelection, anchorSelection);
            }

            checkButtonsStatus();
        }
    }


    private void checkButtonsStatus() {
        // Disable DELETE if there are no rows in the table to delete.
        if (tableModel.getRowCount() == 0) {
            deleteButton.setEnabled(false);
            saveButton.setEnabled(false);
        } else {
            deleteButton.setEnabled(true);
            saveButton.setEnabled(true);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals(DELETE_COMMAND)) {
            deleteRows();
        }
        else if (action.equals(ADD_COMMAND)) {
            // If a table cell is being edited, we should accept the current
            // value and stop the editing before adding a new row.
            GuiUtils.stopTableEditing(authTable);

            tableModel.addNewRow();
            tableModel.fireTableDataChanged();

            checkButtonsStatus();

            // Highlight (select) the appropriate row.
            int rowToSelect = tableModel.getRowCount() - 1;
            authTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        } else if (action.equals(LOAD_COMMAND)) {
            try {
                final String [] _txt={".txt"}; //$NON-NLS-1$
                final JFileChooser dialog = FileDialoger.promptToOpenFile(_txt);
                if (dialog != null) {
                    tableModel.manager.addFile(dialog.getSelectedFile().getAbsolutePath());
                    tableModel.fireTableDataChanged();

                    checkButtonsStatus();
                }
            } catch (IOException ex) {
                log.error("Error loading auth data", ex);
            }
        } else if (action.equals(SAVE_COMMAND)) {
            try {
                final JFileChooser chooser = FileDialoger.promptToSaveFile("auth.txt"); //$NON-NLS-1$
                if (chooser != null) {
                    tableModel.manager.save(chooser.getSelectedFile().getAbsolutePath());
                }
            } catch (IOException ex) {
                JMeterUtils.reportErrorToUser(ex.getMessage(), "Error saving auth data");
            }
        }
    }

    public JPanel createAuthTablePanel() {
        // create the JTable that holds auth per row
        authTable = new JTable(tableModel);
        JMeterUtils.applyHiDPI(authTable);
        authTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        authTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        authTable.setPreferredScrollableViewportSize(new Dimension(100, 70));

        TableColumn passwordColumn = authTable.getColumnModel().getColumn(AuthManager.COL_PASSWORD);
        passwordColumn.setCellRenderer(new PasswordCellRenderer());
        
        TableColumn mechanismColumn = authTable.getColumnModel().getColumn(AuthManager.COL_MECHANISM);
        mechanismColumn.setCellEditor(new MechanismCellEditor());

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("auths_stored"))); //$NON-NLS-1$
        panel.add(new JScrollPane(authTable));
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
        boolean tableEmpty = tableModel.getRowCount() == 0;

        addButton = createButton("add", 'A', ADD_COMMAND, true); //$NON-NLS-1$
        deleteButton = createButton("delete", 'D', DELETE_COMMAND, !tableEmpty); //$NON-NLS-1$
        loadButton = createButton("load", 'L', LOAD_COMMAND, true); //$NON-NLS-1$
        saveButton = createButton("save", 'S', SAVE_COMMAND, !tableEmpty); //$NON-NLS-1$

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        return buttonPanel;
    }

    private static class InnerTableModel extends AbstractTableModel {
        private static final long serialVersionUID = 4638155137475747946L;
        final AuthManager manager;

        public InnerTableModel() {
            manager = new AuthManager();
        }

        public void clearData() {
            manager.clear();
            fireTableDataChanged();
        }

        public void removeRow(int row) {
            manager.remove(row);
        }

        public void addNewRow() {
            manager.addAuth();
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            // all table cells are editable
            return true;
        }

        @Override
        public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }

        /**
         * Required by table model interface.
         */
        @Override
        public int getRowCount() {
            return manager.getAuthObjects().size();
        }

        /**
         * Required by table model interface.
         */
        @Override
        public int getColumnCount() {
            return manager.getColumnCount();
        }

        /**
         * Required by table model interface.
         */
        @Override
        public String getColumnName(int column) {
            return manager.getColumnName(column);
        }

        /**
         * Required by table model interface.
         */
        @Override
        public Object getValueAt(int row, int column) {
            Authorization auth = manager.getAuthObjectAt(row);

            switch (column){
                case AuthManager.COL_URL:
                    return auth.getURL();
                case AuthManager.COL_USERNAME:
                    return auth.getUser();
                case AuthManager.COL_PASSWORD:
                    return auth.getPass();
                case AuthManager.COL_DOMAIN:
                    return auth.getDomain();
                case AuthManager.COL_REALM:
                    return auth.getRealm();
                case AuthManager.COL_MECHANISM:
                    return auth.getMechanism();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            Authorization auth = manager.getAuthObjectAt(row);
            log.debug("Setting auth value: {}", value);
            switch (column){
                case AuthManager.COL_URL:
                    auth.setURL((String) value);
                    break;
                case AuthManager.COL_USERNAME:
                    auth.setUser((String) value);
                    break;
                case AuthManager.COL_PASSWORD:
                    auth.setPass((String) value);
                    break;
                case AuthManager.COL_DOMAIN:
                    auth.setDomain((String) value);
                    break;
                case AuthManager.COL_REALM:
                    auth.setRealm((String) value);
                    break;
                case AuthManager.COL_MECHANISM:
                    auth.setMechanism((Mechanism) value);
                    break;
                default:
                    break;
            }
        }
    }
    
    private static class MechanismCellEditor extends DefaultCellEditor {

        private static final long serialVersionUID = 6085773573067229265L;
        
        public MechanismCellEditor() {
            super(new JComboBox<>(Mechanism.values()));
        }
    }

    private static class PasswordCellRenderer extends JPasswordField implements TableCellRenderer {
        private static final long serialVersionUID = 5169856333827579927L;
        private Border myBorder;

        public PasswordCellRenderer() {
            super();
            myBorder = new EmptyBorder(1, 2, 1, 2);
            setOpaque(true);
            setBorder(myBorder);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            setText((String) value);

            setBackground(isSelected && !hasFocus ? table.getSelectionBackground() : table.getBackground());
            setForeground(isSelected && !hasFocus ? table.getSelectionForeground() : table.getForeground());

            setFont(table.getFont());

            return this;
        }
    }
}
