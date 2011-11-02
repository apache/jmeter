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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.protocol.http.control.Cookie;
import org.apache.jmeter.protocol.http.control.CookieManager;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * This is the GUI for Cookie Manager
 *
 * Allows the user to specify if she needs cookie services, and give parameters
 * for this service.
 *
 */
public class CookiePanel extends AbstractConfigGui implements ActionListener {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    //++ Action command names
    private static final String ADD_COMMAND = "Add"; //$NON-NLS-1$

    private static final String DELETE_COMMAND = "Delete"; //$NON-NLS-1$

    private static final String LOAD_COMMAND = "Load"; //$NON-NLS-1$

    private static final String SAVE_COMMAND = "Save"; //$NON-NLS-1$
    //--

    private JTable cookieTable;

    private PowerTableModel tableModel;

    private JCheckBox clearEachIteration;

    private static final String[] COLUMN_RESOURCE_NAMES = {
        ("name"),   //$NON-NLS-1$
        ("value"),  //$NON-NLS-1$
        ("domain"), //$NON-NLS-1$
        ("path"),   //$NON-NLS-1$
        ("secure"), //$NON-NLS-1$
        // removed expiration because it's just an annoyance for static cookies
    };

    private static final Class<?>[] columnClasses = {
        String.class,
        String.class,
        String.class,
        String.class,
        Boolean.class, };

    private JButton addButton;

    private JButton deleteButton;

    private JButton loadButton;

    private JButton saveButton;

    /*
     * List of cookie policies.
     *
     * These are used both for the display, and for setting the policy
    */
    private final String[] policies = CookieManager.getCookieSpecs();

    private JLabeledChoice policy;

    /**
     * Default constructor.
     */
    public CookiePanel() {
        init();
    }

    public String getLabelResource() {
        return "cookie_manager_title"; //$NON-NLS-1$
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals(DELETE_COMMAND)) {
            if (tableModel.getRowCount() > 0) {
                // If a table cell is being edited, we must cancel the editing
                // before deleting the row.
                if (cookieTable.isEditing()) {
                    TableCellEditor cellEditor = cookieTable.getCellEditor(cookieTable.getEditingRow(),
                            cookieTable.getEditingColumn());
                    cellEditor.cancelCellEditing();
                }

                int rowSelected = cookieTable.getSelectedRow();

                if (rowSelected != -1) {
                    tableModel.removeRow(rowSelected);
                    tableModel.fireTableDataChanged();

                    // Disable the DELETE and SAVE buttons if no rows remaining
                    // after delete.
                    if (tableModel.getRowCount() == 0) {
                        deleteButton.setEnabled(false);
                        saveButton.setEnabled(false);
                    }

                    // Table still contains one or more rows, so highlight
                    // (select) the appropriate one.
                    else {
                        int rowToSelect = rowSelected;

                        if (rowSelected >= tableModel.getRowCount()) {
                            rowToSelect = rowSelected - 1;
                        }

                        cookieTable.setRowSelectionInterval(rowToSelect, rowToSelect);
                    }
                }
            }
        } else if (action.equals(ADD_COMMAND)) {
            // If a table cell is being edited, we should accept the current
            // value and stop the editing before adding a new row.
            if (cookieTable.isEditing()) {
                TableCellEditor cellEditor = cookieTable.getCellEditor(cookieTable.getEditingRow(),
                        cookieTable.getEditingColumn());
                cellEditor.stopCellEditing();
            }

            tableModel.addNewRow();
            tableModel.fireTableDataChanged();

            // Enable the DELETE and SAVE buttons if they are currently
            // disabled.
            if (!deleteButton.isEnabled()) {
                deleteButton.setEnabled(true);
            }
            if (!saveButton.isEnabled()) {
                saveButton.setEnabled(true);
            }

            // Highlight (select) the appropriate row.
            int rowToSelect = tableModel.getRowCount() - 1;
            cookieTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        } else if (action.equals(LOAD_COMMAND)) {
            try {
                final String [] _txt={".txt"}; //$NON-NLS-1$
                final JFileChooser chooser = FileDialoger.promptToOpenFile(_txt);
                if (chooser != null) {
                    CookieManager manager = new CookieManager();
                    manager.addFile(chooser.getSelectedFile().getAbsolutePath());
                    for (int i = 0; i < manager.getCookieCount() ; i++){
                        addCookieToTable(manager.get(i));
                    }
                    tableModel.fireTableDataChanged();

                    if (tableModel.getRowCount() > 0) {
                        deleteButton.setEnabled(true);
                        saveButton.setEnabled(true);
                    }
                }
            } catch (IOException ex) {
                log.error("", ex);
            }
        } else if (action.equals(SAVE_COMMAND)) {
            try {
                final JFileChooser chooser = FileDialoger.promptToSaveFile("cookies.txt"); //$NON-NLS-1$
                if (chooser != null) {
                    ((CookieManager) createTestElement()).save(chooser.getSelectedFile().getAbsolutePath());
                }
            } catch (IOException ex) {
                log.error("", ex);
            }
        }
    }

    private void addCookieToTable(Cookie cookie) {
        tableModel.addRow(new Object[] { cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.getPath(),
                Boolean.valueOf(cookie.getSecure()) });
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement cm) {
        if (cookieTable.isEditing()) {
            cookieTable.getCellEditor().stopCellEditing();
        }
        cm.clear();
        configureTestElement(cm);
        if (cm instanceof CookieManager) {
            CookieManager cookieManager = (CookieManager) cm;
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                Cookie cookie = createCookie(tableModel.getRowData(i));
                cookieManager.add(cookie);
            }
            cookieManager.setClearEachIteration(clearEachIteration.isSelected());
            cookieManager.setCookiePolicy(policy.getText());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        tableModel.clearData();
        clearEachIteration.setSelected(false);
        policy.setText(CookieManager.DEFAULT_POLICY);
        deleteButton.setEnabled(false);
        saveButton.setEnabled(false);
    }

    private Cookie createCookie(Object[] rowData) {
        Cookie cookie = new Cookie(
                (String) rowData[0],
                (String) rowData[1],
                (String) rowData[2],
                (String) rowData[3],
                ((Boolean) rowData[4]).booleanValue(),
                0); // Non-expiring
        return cookie;
    }

    private void populateTable(CookieManager manager) {
        tableModel.clearData();
        PropertyIterator iter = manager.getCookies().iterator();
        while (iter.hasNext()) {
            addCookieToTable((Cookie) iter.next().getObjectValue());
        }
    }

    public TestElement createTestElement() {
        CookieManager cookieManager = new CookieManager();
        modifyTestElement(cookieManager);
        return cookieManager;
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);

        CookieManager cookieManager = (CookieManager) el;
        populateTable(cookieManager);
        clearEachIteration.setSelected((cookieManager).getClearEachIteration());
        policy.setText(cookieManager.getPolicy());
    }

    /**
     * Shows the main cookie configuration panel.
     */
    private void init() {
        tableModel = new PowerTableModel(COLUMN_RESOURCE_NAMES, columnClasses);
        clearEachIteration = 
            new JCheckBox(JMeterUtils.getResString("clear_cookies_per_iter"), false); //$NON-NLS-1$
        policy = new JLabeledChoice(
                JMeterUtils.getResString("cookie_manager_policy"), //$NON-NLS-1$
                policies);
        policy.setText(CookieManager.DEFAULT_POLICY);
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        northPanel.add(makeTitlePanel());
        northPanel.add(clearEachIteration);
        northPanel.add(policy);
        add(northPanel, BorderLayout.NORTH);
        add(createCookieTablePanel(), BorderLayout.CENTER);
    }

    public JPanel createCookieTablePanel() {
        // create the JTable that holds one cookie per row
        cookieTable = new JTable(tableModel);
        cookieTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        cookieTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cookieTable.setPreferredScrollableViewportSize(new Dimension(100, 70));

        JPanel buttonPanel = createButtonPanel();

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("cookies_stored"))); //$NON-NLS-1$

        panel.add(new JScrollPane(cookieTable), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
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
        boolean tableEmpty = (tableModel.getRowCount() == 0);

        addButton = createButton("add", 'A', ADD_COMMAND, true); //$NON-NLS-1$
        deleteButton = createButton("delete", 'D', DELETE_COMMAND, !tableEmpty); //$NON-NLS-1$
        loadButton = createButton("load", 'L', LOAD_COMMAND, true); //$NON-NLS-1$
        saveButton = createButton("save", 'S', SAVE_COMMAND, !tableEmpty); //$NON-NLS-1$

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        return buttonPanel;
    }
}
