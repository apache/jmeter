/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.jmeter.protocol.http.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/**
 * This gui part of @see
 * {@link org.apache.jmeter.protocol.http.control.DNSCacheManager}. Using
 * radiobuttons, user can switch between using system DNS resolver and custom
 * resolver. Custom resolver functionality is provided by dnsjava library.
 * "DNS servers" may contain one or more IP/Name of dns server for resolving
 * name DNS servers are chosen via round-robin. If table is empty - system
 * resolver is used.
 *
 * @since 2.12
 */
public class DNSCachePanel extends AbstractConfigGui implements ActionListener {

    private static final long serialVersionUID = 2120L;

    public static final String OPTIONS = JMeterUtils.getResString("option");

    private static final String ADD_COMMAND = JMeterUtils.getResString("add"); // $NON-NLS-1$

    private static final String DELETE_COMMAND = JMeterUtils.getResString("delete"); // $NON-NLS-1$

    private static final String SYS_RES_COMMAND = JMeterUtils.getResString("use_system_dns_resolver"); // $NON-NLS-1$

    private static final String CUST_RES_COMMAND = JMeterUtils.getResString("use_custom_dns_resolver"); // $NON-NLS-1$

    private JTable dnsServersTable;

    private JPanel dnsServersPanel;

    private JPanel dnsServButPanel;

    private PowerTableModel dnsServersTableModel;

    private JRadioButton sysResButton;

    private JRadioButton custResButton;

    private JButton deleteButton;

    private JButton addButton;

    private ButtonGroup providerDNSradioGroup = new ButtonGroup();

    private static final String[] COLUMN_RESOURCE_NAMES = {
        (JMeterUtils.getResString("dns_hostname_or_ip")), //$NON-NLS-1$
    };
    private static final Class<?>[] columnClasses = {
        String.class };

    private JCheckBox clearEachIteration;

    /**
     * Default constructor.
     */
    public DNSCachePanel() {
        init();
    }

    @Override
    public String getLabelResource() {
        return "dns_cache_manager_title";
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
     */
    @Override
    public void modifyTestElement(TestElement dnsRes) {
        GuiUtils.stopTableEditing(dnsServersTable);
        dnsRes.clear();
        configureTestElement(dnsRes);
        if (dnsRes instanceof DNSCacheManager) {
            DNSCacheManager dnsCacheManager = (DNSCacheManager) dnsRes;
            for (int i = 0; i < dnsServersTableModel.getRowCount(); i++) {
                String server = (String) dnsServersTableModel.getRowData(i)[0];
                dnsCacheManager.addServer(server);
            }
            dnsCacheManager.setClearEachIteration(clearEachIteration.isSelected());
            if (providerDNSradioGroup.isSelected(custResButton.getModel())) {
                dnsCacheManager.setCustomResolver(true);
            } else {
                dnsCacheManager.setCustomResolver(false);
            }
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        clearEachIteration.setSelected(DNSCacheManager.DEFAULT_CLEAR_CACHE_EACH_ITER);
        providerDNSradioGroup.setSelected(sysResButton.getModel(), true);
        dnsServersTableModel.clearData();
        deleteButton.setEnabled(false);

    }

    private void populateTable(DNSCacheManager resolver) {
        dnsServersTableModel.clearData();
        PropertyIterator iter = resolver.getServers().iterator();
        while (iter.hasNext()) {
            addServerToTable((String) iter.next().getObjectValue());
        }
    }

    @Override
    public TestElement createTestElement() {
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        modifyTestElement(dnsCacheManager);
        return dnsCacheManager;
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);

        DNSCacheManager dnsCacheManager = (DNSCacheManager) el;
        populateTable(dnsCacheManager);
        clearEachIteration.setSelected(dnsCacheManager.isClearEachIteration());
        if (dnsCacheManager.isCustomResolver()) {
            providerDNSradioGroup.setSelected(custResButton.getModel(), true);
            deleteButton.setEnabled(dnsServersTable.getColumnCount() > 0);
            addButton.setEnabled(true);
        } else {
            providerDNSradioGroup.setSelected(sysResButton.getModel(), true);
        }
    }

    private void init() {
        dnsServersTableModel = new PowerTableModel(COLUMN_RESOURCE_NAMES, columnClasses);

        clearEachIteration = new JCheckBox(JMeterUtils.getResString("clear_cache_each_iteration"), true); //$NON-NLS-1$
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        northPanel.add(makeTitlePanel());
        JPanel optionsPane = new JPanel();
        optionsPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), OPTIONS)); // $NON-NLS-1$
        optionsPane.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        optionsPane.add(clearEachIteration, BorderLayout.WEST);
        optionsPane.add(createChooseResPanel(), BorderLayout.SOUTH);
        northPanel.add(optionsPane);
        add(northPanel, BorderLayout.NORTH);

        dnsServersPanel = createDnsServersTablePanel();
        add(dnsServersPanel, BorderLayout.CENTER);

    }

    public JPanel createDnsServersTablePanel() {
        // create the JTable that holds header per row
        dnsServersTable = new JTable(dnsServersTableModel);
        dnsServersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dnsServersTable.setPreferredScrollableViewportSize(new Dimension(400, 100));

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("dns_servers"))); // $NON-NLS-1$
        JScrollPane dnsServScrollPane = new JScrollPane(dnsServersTable);
        panel.add(dnsServScrollPane, BorderLayout.CENTER);
        dnsServButPanel = createButtonPanel();
        panel.add(dnsServButPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createChooseResPanel() {
        JPanel chooseResPanel = new JPanel(new BorderLayout(0, 5));
        sysResButton = new JRadioButton();
        sysResButton.setSelected(true);
        sysResButton.setText(SYS_RES_COMMAND);
        sysResButton.setToolTipText(SYS_RES_COMMAND);
        sysResButton.setEnabled(true);
        sysResButton.addActionListener(this);

        custResButton = new JRadioButton();
        custResButton.setSelected(false);
        custResButton.setText(CUST_RES_COMMAND);
        custResButton.setToolTipText(CUST_RES_COMMAND);
        custResButton.setEnabled(true);
        custResButton.addActionListener(this);

        providerDNSradioGroup.add(sysResButton);
        providerDNSradioGroup.add(custResButton);

        chooseResPanel.add(sysResButton, BorderLayout.WEST);
        chooseResPanel.add(custResButton, BorderLayout.CENTER);
        return chooseResPanel;
    }

    private JPanel createButtonPanel() {
        boolean tableEmpty = (dnsServersTableModel.getRowCount() == 0);

        addButton = createButton("add", 'A', ADD_COMMAND, custResButton.isSelected()); // $NON-NLS-1$
        deleteButton = createButton("delete", 'D', DELETE_COMMAND, !tableEmpty); // $NON-NLS-1$

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton, BorderLayout.WEST);
        buttonPanel.add(deleteButton, BorderLayout.LINE_END);
        return buttonPanel;
    }

    private JButton createButton(String resName, char mnemonic, String command, boolean enabled) {
        JButton button = new JButton(JMeterUtils.getResString(resName));
        button.setMnemonic(mnemonic);
        button.setActionCommand(command);
        button.setEnabled(enabled);
        button.addActionListener(this);
        return button;
    }

    private void addServerToTable(String dnsServer) {
        dnsServersTableModel.addRow(new Object[] {
            dnsServer });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        dnsServersTable.setEnabled(custResButton.isSelected());
        Color greyColor = new Color(240, 240, 240);
        Color blueColor = new Color(184, 207, 229);
        dnsServersTable.setBackground(sysResButton.isSelected() ? greyColor : Color.WHITE);
        dnsServersTable.setSelectionBackground(sysResButton.isSelected() ? greyColor : blueColor);
        addButton.setEnabled(custResButton.isSelected());
        deleteButton.setEnabled(custResButton.isSelected());
        if (custResButton.isSelected() && (dnsServersTableModel.getRowCount() > 0)) {
            deleteButton.setEnabled(true);
            addButton.setEnabled(true);
        }

        if (action.equals(DELETE_COMMAND)) {
            if (dnsServersTableModel.getRowCount() > 0) {
                // If a table cell is being edited, we must cancel the editing
                // before deleting the row.
                if (dnsServersTable.isEditing()) {
                    TableCellEditor cellEditor = dnsServersTable.getCellEditor(dnsServersTable.getEditingRow(),
                            dnsServersTable.getEditingColumn());
                    cellEditor.cancelCellEditing();
                }

                int rowSelected = dnsServersTable.getSelectedRow();

                if (rowSelected != -1) {
                    dnsServersTableModel.removeRow(rowSelected);
                    dnsServersTableModel.fireTableDataChanged();

                    if (dnsServersTableModel.getRowCount() == 0) {
                        deleteButton.setEnabled(false);
                    }

                    else {
                        int rowToSelect = rowSelected;

                        if (rowSelected >= dnsServersTableModel.getRowCount()) {
                            rowToSelect = rowSelected - 1;
                        }

                        dnsServersTable.setRowSelectionInterval(rowToSelect, rowToSelect);
                    }
                }
            }
        } else if (action.equals(ADD_COMMAND)) {
            // If a table cell is being edited, we should accept the current
            // value and stop the editing before adding a new row.
            GuiUtils.stopTableEditing(dnsServersTable);

            dnsServersTableModel.addNewRow();
            dnsServersTableModel.fireTableDataChanged();

            if (!deleteButton.isEnabled()) {
                deleteButton.setEnabled(true);
            }

            // Highlight (select) the appropriate row.
            int rowToSelect = dnsServersTableModel.getRowCount() - 1;
            dnsServersTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        }
    }
}
