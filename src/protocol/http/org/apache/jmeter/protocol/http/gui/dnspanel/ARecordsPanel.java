package org.apache.jmeter.protocol.http.gui.dnspanel;

import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.protocol.http.control.dnscachemanager.ARecord;
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
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by dzmitrykashlach on 7/11/14.
 */
public class ARecordsPanel extends JPanel implements ActionListener {
    //++ Action command names
    private static final String ADD_COMMAND = "Add"; //$NON-NLS-1$

    private static final String DELETE_COMMAND = "Delete"; //$NON-NLS-1$

    private static final String LOAD_COMMAND = "Load"; //$NON-NLS-1$

    private static final String SAVE_COMMAND = "Save"; //$NON-NLS-1$
    private static final String aRecRegEx = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})(\\s{1,})(.*)$";
    private static final Logger log = LoggingManager.getLoggerForClass();
    private static final String[] COLUMN_RESOURCE_NAMES = {
            ARecordsTableFields.Name.toString(),
            ARecordsTableFields.IP.toString(),
            ARecordsTableFields.Expires.toString(),
    };
    private static final Class<?>[] columnClasses = {
            String.class,
            String.class,
            Long.class};
    private JTable aRecordsTable;
    private PowerTableModel tableModel;
    private JButton addButton;
    private JButton deleteButton;
    private JButton loadButton;
    private JButton saveButton;

    public ARecordsPanel() {

        super(new BorderLayout(0, 5));
        tableModel = new PowerTableModel(COLUMN_RESOURCE_NAMES, columnClasses);

        aRecordsTable = new JTable(tableModel);
        aRecordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Dimension minDim = new Dimension(100, 150);
        aRecordsTable.setPreferredScrollableViewportSize(minDim);

        JPanel buttonPanel = createButtonPanel();

        this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), Constants.A_RECORDS)); //$NON-NLS-1$

        this.add(new JScrollPane(aRecordsTable), BorderLayout.CENTER);
        this.add(buttonPanel, BorderLayout.SOUTH);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals(DELETE_COMMAND)) {
            if (tableModel.getRowCount() > 0) {
                // If a table cell is being edited, we must cancel the editing
                // before deleting the row.
                if (aRecordsTable.isEditing()) {
                    TableCellEditor cellEditor = aRecordsTable.getCellEditor(aRecordsTable.getEditingRow(),
                            aRecordsTable.getEditingColumn());
                    cellEditor.cancelCellEditing();
                }

                int rowSelected = aRecordsTable.getSelectedRow();

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

                        aRecordsTable.setRowSelectionInterval(rowToSelect, rowToSelect);
                    }
                }
            }
        } else if (action.equals(ADD_COMMAND)) {
            // If a table cell is being edited, we should accept the current
            // value and stop the editing before adding a new row.
            GuiUtils.stopTableEditing(aRecordsTable);

            tableModel.addNewRow();
            tableModel.fireTableDataChanged();

            // Enable the DELETE and SAVE buttons if they are currently
            // disabled.
            if (!deleteButton.isEnabled()) {
                deleteButton.setEnabled(true);
            }

            // Highlight (select) the appropriate row.
            int rowToSelect = tableModel.getRowCount() - 1;
            aRecordsTable.setRowSelectionInterval(rowToSelect, rowToSelect);
        } else if (action.equals(LOAD_COMMAND)) {
            try {
                final JFileChooser chooser = FileDialoger.promptToOpenFile();
                int filename = chooser.getApproveButtonMnemonic();
                if (chooser != null && filename == JFileChooser.APPROVE_OPTION) {

                    File f = chooser.getSelectedFile();
                    List<ARecord> aRecords = getARecordsFromFile(f);
                    if (aRecords.size() > 0) {
                        tableModel.clearData();
                        for (ARecord rec : aRecords) {
                            addARecToTable(rec);
                        }
                    }

                    tableModel.fireTableDataChanged();

                    if (tableModel.getRowCount() > 0) {
                        deleteButton.setEnabled(true);
                        saveButton.setEnabled(true);
                    }
                }
            } catch (Exception ex) {
                log.error("", ex);
            }
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
        loadButton = createButton("load", 'L', LOAD_COMMAND, true);
        loadButton.setToolTipText("Load A Records from /etc/hosts");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(loadButton);
        return buttonPanel;
    }

    public void resetButtons() {
        deleteButton.setEnabled(tableModel.getRowCount() == 0 ? false : true);

    }

    public JTable getARecordsTable() {
        return aRecordsTable;
    }

    public PowerTableModel getTableModel() {
        return tableModel;
    }

    private List<ARecord> getARecordsFromFile(java.io.File file) {
        List<String> hostsLs = getFileContentAsList(file.getAbsolutePath());
        List<ARecord> aRecords = new ArrayList<ARecord>();
        long expires = System.currentTimeMillis() + 86400000;
        for (String s : hostsLs) {
            if (s.matches(aRecRegEx)) {
                String[] aRecAr = s.split("(\\s{1,})");
                ARecord aRecord = new ARecord(aRecAr[1], aRecAr[0], expires);
                aRecords.add(aRecord);
            }
        }
        return aRecords;
    }

    public List<String> getFileContentAsList(String inputfile) {
        List<String> strings = new ArrayList<String>();
        Scanner sc = null;
        try {
            sc = new Scanner(new File(inputfile));
        } catch (FileNotFoundException e) {
            log.error("Failed to open " + inputfile);
        }

        while (sc.hasNextLine()) {
            strings.add(sc.nextLine());
        }

        return strings;
    }

    private void addARecToTable(ARecord aRecord) {
        tableModel.addRow(new Object[]{aRecord.getName(), aRecord.getIP(), aRecord.getExpires()});
    }

    private enum ARecordsTableFields {Name, IP, Expires}


}
