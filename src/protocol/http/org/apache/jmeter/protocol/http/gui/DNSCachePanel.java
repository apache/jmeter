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

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.protocol.http.control.StaticHost;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(DNSCacheManager.class);

    private static final long serialVersionUID = 2120L;

    public static final String OPTIONS = JMeterUtils.getResString("option");

    private static final String ADD_COMMAND = JMeterUtils.getResString("add"); // $NON-NLS-1$

    private static final String ADD_HOST_COMMAND = JMeterUtils.getResString("add_host"); // $NON-NLS-1$

    private static final String DELETE_COMMAND = JMeterUtils.getResString("delete"); // $NON-NLS-1$

    private static final String DELETE_HOST_COMMAND = JMeterUtils.getResString("delete_host"); // $NON-NLS-1$

    private static final String SYS_RES_COMMAND = JMeterUtils.getResString("use_system_dns_resolver"); // $NON-NLS-1$

    private static final String CUST_RES_COMMAND = JMeterUtils.getResString("use_custom_dns_resolver"); // $NON-NLS-1$

    private JTable dnsHostsTable;

    private JPanel dnsHostsPanel;
    private JPanel dnsHostsButPanel;

    private JTable dnsServersTable;

    private JPanel dnsServersPanel;

    private JPanel dnsServButPanel;

    private PowerTableModel dnsServersTableModel;
    private PowerTableModel dnsHostsTableModel;

    private JRadioButton sysResButton;

    private JRadioButton custResButton;

    private JButton deleteButton;

    private JButton addButton;

    private JButton addHostButton;
    private JButton deleteHostButton;

    private ButtonGroup providerDNSradioGroup = new ButtonGroup();

    private static final String[] COLUMN_RESOURCE_NAMES = {
        JMeterUtils.getResString("dns_hostname_or_ip"), //$NON-NLS-1$
    };
    private static final Class<?>[] columnClasses = {
        String.class };

    private static final String[] HOSTS_COLUMN_RESOURCE_NAMES = { JMeterUtils.getResString("dns_host"), JMeterUtils.getResString("dns_hostname_or_ip") };
    private static final Class<?>[] HOSTS_COLUMN_CLASSES = { String.class, String.class };

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
            for (int i = 0; i < dnsHostsTableModel.getRowCount(); i++) {
                String host = (String) dnsHostsTableModel.getRowData(i)[0];
                String addresses = (String) dnsHostsTableModel.getRowData(i)[1];
                dnsCacheManager.addHost(host, addresses);
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
        dnsHostsTableModel.clearData();
        deleteHostButton.setEnabled(false);

    }

    private void populateTable(DNSCacheManager resolver) {
        dnsServersTableModel.clearData();
        for (JMeterProperty jMeterProperty : resolver.getServers()) {
            addServerToTable((String) jMeterProperty.getObjectValue());
        }
    }

    private void populateHostsTable(DNSCacheManager resolver) {
        dnsHostsTableModel.clearData();
        for (JMeterProperty hostEntry : resolver.getHosts()) {
            addHostToTable((StaticHost) hostEntry.getObjectValue());
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
        populateHostsTable(dnsCacheManager);
        clearEachIteration.setSelected(dnsCacheManager.isClearEachIteration());
        if (dnsCacheManager.isCustomResolver()) {
            providerDNSradioGroup.setSelected(custResButton.getModel(), true);
            deleteButton.setEnabled(dnsServersTable.getColumnCount() > 0);
            deleteHostButton.setEnabled(dnsHostsTable.getColumnCount() > 0);
            addButton.setEnabled(true);
            addHostButton.setEnabled(true);
        } else {
            providerDNSradioGroup.setSelected(sysResButton.getModel(), true);
        }
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        dnsServersTableModel = new PowerTableModel(COLUMN_RESOURCE_NAMES, columnClasses);
        dnsHostsTableModel = new PowerTableModel(HOSTS_COLUMN_RESOURCE_NAMES, HOSTS_COLUMN_CLASSES);

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

        JPanel tables = new JPanel();
        tables.setLayout(new VerticalLayout(2, VerticalLayout.BOTH));
        dnsServersPanel = createDnsServersTablePanel();
        dnsHostsPanel = createDnsHostsTablePanel();
        tables.add(dnsServersPanel);
        tables.add(dnsHostsPanel);
        add(tables, BorderLayout.CENTER);


    }

    public JPanel createDnsServersTablePanel() {
        // create the JTable that holds header per row
        dnsServersTable = new JTable(dnsServersTableModel);
        JMeterUtils.applyHiDPI(dnsServersTable);
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

    public JPanel createDnsHostsTablePanel() {
        // create the JTable that holds header per row
        dnsHostsTable = new JTable(dnsHostsTableModel);
        JMeterUtils.applyHiDPI(dnsHostsTable);
        dnsHostsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dnsHostsTable.setPreferredScrollableViewportSize(new Dimension(400, 100));

        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("dns_hosts"))); // $NON-NLS-1$
        JScrollPane dnsHostsScrollPane = new JScrollPane(dnsHostsTable);
        panel.add(dnsHostsScrollPane, BorderLayout.CENTER);
        dnsHostsButPanel = createHostsButtonPanel();
        panel.add(dnsHostsButPanel, BorderLayout.SOUTH);
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
        boolean tableEmpty = dnsServersTableModel.getRowCount() == 0;

        addButton = createButton("add", 'A', ADD_COMMAND, custResButton.isSelected()); // $NON-NLS-1$
        deleteButton = createButton("delete", 'D', DELETE_COMMAND, !tableEmpty); // $NON-NLS-1$

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton, BorderLayout.WEST);
        buttonPanel.add(deleteButton, BorderLayout.LINE_END);
        return buttonPanel;
    }

    private JPanel createHostsButtonPanel() {
        boolean tableEmpty = dnsHostsTableModel.getRowCount() == 0;

        addHostButton = createButton("add_host", 'H', ADD_HOST_COMMAND, custResButton.isSelected()); // $NON-NLS-1$
        deleteHostButton = createButton("delete_host", 'X', DELETE_HOST_COMMAND, !tableEmpty); // $NON-NLS-1$

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addHostButton, BorderLayout.WEST);
        buttonPanel.add(deleteHostButton, BorderLayout.LINE_END);
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

    private void addHostToTable(StaticHost hostEntry) {
        LOGGER.debug("Adding entry {}", hostEntry);
        dnsHostsTableModel.addRow(new Object[] {
            hostEntry.getName(), hostEntry.getAddress() });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        enableTable(custResButton.isSelected(), sysResButton.isSelected(), dnsServersTable, dnsServersTableModel,
                addButton, deleteButton);
        enableTable(custResButton.isSelected(), sysResButton.isSelected(), dnsHostsTable, dnsHostsTableModel,
                addHostButton, deleteHostButton);

        if (action.equals(DELETE_COMMAND)) {
            deleteTableRow(dnsServersTable, dnsServersTableModel, deleteButton);
        } else if (action.equals(ADD_COMMAND)) {
            addTableRow(dnsServersTable, dnsServersTableModel, deleteButton);
        } else if (DELETE_HOST_COMMAND.equals(action)) {
            deleteTableRow(dnsHostsTable, dnsHostsTableModel, deleteHostButton);
        } else if (ADD_HOST_COMMAND.equals(action)) {
            addTableRow(dnsHostsTable, dnsHostsTableModel, deleteHostButton);
        }
    }

    private void enableTable(boolean custEnabled, boolean sysEnabled, JTable table, PowerTableModel model,
            JButton addButton, JButton deleteButton) {
        table.setEnabled(custEnabled);
        Color greyColor = new Color(240, 240, 240);
        Color blueColor = new Color(184, 207, 229);
        table.setBackground(sysEnabled ? greyColor : Color.WHITE);
        table.setSelectionBackground(sysEnabled ? greyColor : blueColor);
        addButton.setEnabled(custEnabled);
        deleteButton.setEnabled(custEnabled);
        if (custEnabled && (model.getRowCount() > 0)) {
            deleteButton.setEnabled(true);
            addButton.setEnabled(true);
        }
    }

    private void addTableRow(JTable table, PowerTableModel model, JButton button) {
        // If a table cell is being edited, we should accept the current
        // value and stop the editing before adding a new row.
        GuiUtils.stopTableEditing(table);

        model.addNewRow();
        model.fireTableDataChanged();

        if (!button.isEnabled()) {
            button.setEnabled(true);
        }

        // Highlight (select) the appropriate row.
        int rowToSelect = model.getRowCount() - 1;
        table.setRowSelectionInterval(rowToSelect, rowToSelect);
    }

    private void deleteTableRow(JTable table, PowerTableModel model, JButton button) {
        if (model.getRowCount() > 0) {
            // If a table cell is being edited, we must cancel the editing
            // before deleting the row.
            GuiUtils.cancelEditing(table);

            int rowSelected = table.getSelectedRow();

            if (rowSelected != -1) {
                model.removeRow(rowSelected);
                model.fireTableDataChanged();

                if (model.getRowCount() == 0) {
                    button.setEnabled(false);
                }

                else {
                    int rowToSelect = Math.min(rowSelected, model.getRowCount() - 1);
                    table.setRowSelectionInterval(rowToSelect, rowToSelect);
                }
            }
        }
    }
}
