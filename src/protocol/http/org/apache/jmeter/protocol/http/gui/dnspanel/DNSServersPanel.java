package org.apache.jmeter.protocol.http.gui.dnspanel;

import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.protocol.http.control.dnscachemanager.Constants;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by dzmitrykashlach on 7/11/14.
 */
public class DNSServersPanel extends JPanel implements ActionListener {
    //++ Action command names
    private static final String ADD_COMMAND = "Add"; //$NON-NLS-1$

    private static final String DELETE_COMMAND = "Delete"; //$NON-NLS-1$

    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final String[] COLUMN_RESOURCE_NAMES = {
            DNSTableFields.Name.toString(),
            DNSTableFields.IP.toString(),
            DNSTableFields.Priority.toString(),
    };
    private static final Class<?>[] columnClasses = {
            String.class,
            String.class,
            Integer.class};
    private JTable dnsServersTable;
    private PowerTableModel tableModel;
    private JButton addButton;
    private JButton deleteButton;

    public DNSServersPanel() {

        super(new BorderLayout(0, 5));
        tableModel = new PowerTableModel(COLUMN_RESOURCE_NAMES, columnClasses);

        dnsServersTable = new JTable(tableModel);
        dnsServersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Dimension dim = new Dimension(100, 30);
        dnsServersTable.setPreferredScrollableViewportSize(dim);

        JPanel buttonPanel = createButtonPanel();

        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Constants.DNS_SERVERS)); //$NON-NLS-1$

        this.add(new JScrollPane(dnsServersTable), BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals(DELETE_COMMAND)) {
            if (tableModel.getRowCount() > 0) {
                // If a table cell is being edited, we must cancel the editing
                // before deleting the row.
                if (dnsServersTable.isEditing()) {
                    TableCellEditor cellEditor = dnsServersTable.getCellEditor(dnsServersTable.getEditingRow(),
                            dnsServersTable.getEditingColumn());
                    cellEditor.cancelCellEditing();
                }

                int rowSelected = dnsServersTable.getSelectedRow();

                if (rowSelected != -1) {
                    tableModel.removeRow(rowSelected);
                    tableModel.fireTableDataChanged();

                    // Disable the DELETE and SAVE buttons if no rows remaining
                    // after delete.
                    if (tableModel.getRowCount() == 0) {
                        deleteButton.setEnabled(false);
                    }

                    // Table still contains one or more rows, so highlight
                    // (select) the appropriate one.
                    else {
                        int rowToSelect = rowSelected;

                        if (rowSelected >= tableModel.getRowCount()) {
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

            tableModel.addNewRow();
            tableModel.fireTableDataChanged();

            // Enable the DELETE and SAVE buttons if they are currently
            // disabled.
            if (!deleteButton.isEnabled()) {
                deleteButton.setEnabled(true);
            }

            // Highlight (select) the appropriate row.
            int rowToSelect = tableModel.getRowCount() - 1;
            dnsServersTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        }

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

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        return buttonPanel;
    }

    public void resetButtons() {
        deleteButton.setEnabled(tableModel.getRowCount() == 0 ? false : true);

    }

    public JTable getDnsServersTable() {
        return dnsServersTable;
    }

    public PowerTableModel getTableModel() {
        return tableModel;
    }

    private enum DNSTableFields {Name, IP, Priority}
}
