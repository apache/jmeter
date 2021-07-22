/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.protocol.http.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.gui.RowDetailDialog;
import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.protocol.http.util.HTTPFileArg;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

/*
 * Note: this class is currently only suitable for use with HTTSamplerBase.
 * If it is required for other classes, then the appropriate configure() and modifyTestElement()
 * method code needs to be written.
 */
/**
 * A GUI panel allowing the user to enter file information for http upload.
 * Used by UrlConfigGui for use in HTTP Samplers.
 */
public class HTTPFileArgsPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 240L;

    /** The table containing the list of files. */
    private transient JTable table;

    /** The model for the files table. */
    private transient ObjectTableModel tableModel; // only contains HTTPFileArg elements

    /** A button for adding new files to the table. */
    private JButton add;

    /** A button for browsing file system to set path of selected row in table. */
    private JButton browse;

    /** A button for removing files from the table. */
    private JButton delete;

    private AbstractButton showDetail;

    /** Command for adding a row to the table. */
    private static final String ADD = "add"; // $NON-NLS-1$

    /** Command for browsing filesystem to set path of selected row in table. */
    private static final String BROWSE = "browse"; // $NON-NLS-1$

    /** Command for adding rows from the clipboard */
    private static final String ADD_FROM_CLIPBOARD = "addFromClipboard"; // $NON-NLS-1$

    /** Command for removing a row from the table. */
    private static final String DELETE = "delete"; // $NON-NLS-1$

    /** Command for moving a row up in the table. */
    private static final String UP = "up"; // $NON-NLS-1$

    /** Command for moving a row down in the table. */
    private static final String DOWN = "down"; // $NON-NLS-1$

    private static final String FILEPATH = "send_file_filename_label"; // $NON-NLS-1$

    /** The parameter name column title of file table. */
    private static final String PARAMNAME = "send_file_param_name_label"; //$NON-NLS-1$

    /** The mime type column title of file table. */
    private static final String MIMETYPE = "send_file_mime_label"; //$NON-NLS-1$

    /** When pasting from the clipboard, split lines on linebreak */
    private static final String CLIPBOARD_LINE_DELIMITERS = "\n"; //$NON-NLS-1$

    /** When pasting from the clipboard, split parameters on tab */
    private static final String CLIPBOARD_ARG_DELIMITERS = "\t"; //$NON-NLS-1$

    /** Command for showing detail. */
    private static final String DETAIL = "detail"; // $NON-NLS-1$

    /**
     * Create a new HTTPFileArgsPanel as an embedded component
     */
    public HTTPFileArgsPanel() {
        init();
    }

    /**
     * Initialize the table model used for the http files table.
     */
    private void initializeTableModel() {
        tableModel = new ObjectTableModel(new String[] {
                FILEPATH, PARAMNAME, MIMETYPE},
            HTTPFileArg.class,
            new Functor[] {
                new Functor("getPath"), //$NON-NLS-1$
                new Functor("getParamName"), //$NON-NLS-1$
                new Functor("getMimeType")}, //$NON-NLS-1$
            new Functor[] {
                new Functor("setPath"), //$NON-NLS-1$
                new Functor("setParamName"), //$NON-NLS-1$
                new Functor("setMimeType")}, //$NON-NLS-1$
            new Class[] {String.class, String.class, String.class});
    }

    public static boolean testFunctors(){
        HTTPFileArgsPanel instance = new HTTPFileArgsPanel();
        instance.initializeTableModel();
        return instance.tableModel.checkFunctors(null,instance.getClass());
    }

    /**
     * Save the GUI data in the HTTPSamplerBase element.
     *
     * @param testElement {@link TestElement} to modify
     */
    public void modifyTestElement(TestElement testElement) {
        GuiUtils.stopTableEditing(table);
        if (testElement instanceof HTTPSamplerBase) {
            HTTPSamplerBase base = (HTTPSamplerBase) testElement;
            int rows = tableModel.getRowCount();
            @SuppressWarnings("unchecked") // we only put HTTPFileArgs in it
            Iterator<HTTPFileArg> modelData = (Iterator<HTTPFileArg>) tableModel.iterator();
            HTTPFileArg[] files = new HTTPFileArg[rows];
            int row = 0;
            while (modelData.hasNext()) {
                HTTPFileArg file = modelData.next();
                files[row++] = file;
            }
            base.setHTTPFiles(files);
        }
    }

    public boolean hasData() {
        return tableModel.iterator().hasNext();
    }

    /**
     * A newly created component can be initialized with the contents of a
     * HTTPSamplerBase object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param testElement the HTTPSamplerBase to be used to configure the GUI
     */
    public void configure(TestElement testElement) {
        if (testElement instanceof HTTPSamplerBase) {
            HTTPSamplerBase base = (HTTPSamplerBase) testElement;
            tableModel.clearData();
            for(HTTPFileArg file : base.getHTTPFiles()){
                tableModel.addRow(file);
            }
            checkButtonsStatus();
        }
    }


    /**
     * Enable or disable the delete button depending on whether or not there is
     * a row to be deleted.
     */
    private void checkButtonsStatus() {
        // Disable DETAILS, DELETE and BROWSE buttons if there are no rows in
        // the table to delete.
        final boolean hasRows = tableModel.getRowCount() > 0;
        browse.setEnabled(hasRows);
        delete.setEnabled(hasRows);
        showDetail.setEnabled(hasRows);
    }

    /**
     * Clear all rows from the table.
     */
    public void clear() {
        GuiUtils.stopTableEditing(table);
        tableModel.clearData();
    }

    /**
     * Invoked when an action occurs. This implementation supports the add and
     * delete buttons.
     *
     * @param e
     *  the event that has occurred
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(ADD)) {
            addFile(""); //$NON-NLS-1$
        } else if (action.equals(ADD_FROM_CLIPBOARD)) {
            addFromClipboard();
        } else if (action.equals(UP)) {
            moveUp();
        } else if (action.equals(DOWN)) {
            moveDown();
        } else if (action.equals(DETAIL)) {
            showDetail();
        }
        runCommandOnSelectedFile(action);
    }

    /**
     * Runs specified command on currently selected file.
     *
     * @param command specifies which process will be done on selected
     * file. it's coming from action command currently caught by
     * action listener.
     */
    private void runCommandOnSelectedFile(String command) {
        // If a table cell is being edited, we must cancel the editing before
        // deleting the row
        GuiUtils.cancelEditing(table);

        int rowSelected = table.getSelectedRow();
        if (rowSelected >= 0) {
            runCommandOnRow(command, rowSelected);
            tableModel.fireTableDataChanged();
            // Disable DELETE and BROWSE if there are no rows in the table to delete.
            checkButtonsStatus();
            // Table still contains one or more rows, so highlight (select)
            // the appropriate one.
            if (tableModel.getRowCount() != 0) {
                int rowToSelect = rowSelected;
                if (rowSelected >= tableModel.getRowCount()) {
                    rowToSelect = rowSelected - 1;
                }
                table.setRowSelectionInterval(rowToSelect, rowToSelect);
            }
        }
    }

    /**
     * runs specified command on currently selected table row.
     *
     * @param command specifies which process will be done on selected
     * file. it's coming from action command currently caught by
     * action listener.
     *
     * @param rowSelected index of selected row.
     */
    private void runCommandOnRow(String command, int rowSelected) {
        if (DELETE.equals(command)) {
            tableModel.removeRow(rowSelected);
        } else if (BROWSE.equals(command)) {
            String path = browseAndGetFilePath();
            if(StringUtils.isNotBlank(path)) {
                tableModel.setValueAt(path, rowSelected, 0);
            }
        }
    }

    /**
     * Add a new file row to the table.
     */
    private void addFile(String path) {
        // If a table cell is being edited, we should accept the current value
        // and stop the editing before adding a new row.
        GuiUtils.stopTableEditing(table);

        tableModel.addRow(new HTTPFileArg(path));

        checkButtonsStatus();

        // Highlight (select) the appropriate row.
        int rowToSelect = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(rowToSelect, rowToSelect);
    }

    /**
     * opens a dialog box to choose a file and returns selected file's
     * path.
     *
     * @return a new File object
     */
    private String browseAndGetFilePath() {
        String path = ""; //$NON-NLS-1$
        JFileChooser chooser = FileDialoger.promptToOpenFile();
        if (chooser != null) {
            File file = chooser.getSelectedFile();
            if (file != null) {
                path = file.getPath();
            }
        }
        return path;
    }

    /**
     * Stop any editing that is currently being done on the table. This will
     * save any changes that have already been made.
     */
    protected void stopTableEditing() {
        GuiUtils.stopTableEditing(table);
    }

    /**
     * Create the main GUI panel which contains the file table.
     *
     * @return the main GUI panel
     */
    private Component makeMainPanel() {
        initializeTableModel();
        table = new JTable(tableModel);
        JMeterUtils.applyHiDPI(table);
        table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return makeScrollPane(table);
    }

    /**
     * Create a panel containing the add and delete buttons.
     *
     * @return a GUI panel containing the buttons
     */
    private JPanel makeButtonPanel() {
        add = new JButton(JMeterUtils.getResString("add")); // $NON-NLS-1$
        add.setActionCommand(ADD);
        add.setEnabled(true);

        browse = new JButton(JMeterUtils.getResString("browse")); // $NON-NLS-1$
        browse.setActionCommand(BROWSE);

        delete = new JButton(JMeterUtils.getResString("delete")); // $NON-NLS-1$
        delete.setActionCommand(DELETE);

        showDetail = new JButton(JMeterUtils.getResString("detail")); // $NON-NLS-1$
        showDetail.setActionCommand(DETAIL);
        showDetail.setEnabled(true);
        showDetail.addActionListener(this);

        // A button for adding new arguments to the table from the clipboard
        JButton addFromClipboard = new JButton(JMeterUtils.getResString("add_from_clipboard")); // $NON-NLS-1$
        addFromClipboard.setActionCommand(ADD_FROM_CLIPBOARD);

        JButton up = new JButton(JMeterUtils.getResString("up")); // $NON-NLS-1$
        up.setActionCommand(UP);

        JButton down = new JButton(JMeterUtils.getResString("down")); // $NON-NLS-1$
        down.setActionCommand(DOWN);

        checkButtonsStatus();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add.addActionListener(this);
        browse.addActionListener(this);
        addFromClipboard.addActionListener(this);
        delete.addActionListener(this);
        up.addActionListener(this);
        down.addActionListener(this);
        buttonPanel.add(showDetail);
        buttonPanel.add(add);
        buttonPanel.add(browse);
        buttonPanel.add(addFromClipboard);
        buttonPanel.add(delete);
        buttonPanel.add(up);
        buttonPanel.add(down);
        return buttonPanel;
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        JPanel p = this;

        p.setLayout(new BorderLayout());

        p.add(makeMainPanel(), BorderLayout.CENTER);
        // Force a minimum table height of 70 pixels
        p.add(Box.createVerticalStrut(70), BorderLayout.WEST);
        p.add(makeButtonPanel(), BorderLayout.SOUTH);

        table.revalidate();
    }

    private JScrollPane makeScrollPane(Component comp) {
        JScrollPane pane = new JScrollPane(comp);
        pane.setPreferredSize(pane.getMinimumSize());
        return GuiUtils.emptyBorder(pane);
    }


    /**
     * Move a row down
     */
    private void moveDown() {
        //get the selected rows before stopping editing
        // or the selected rows will be unselected
        int[] rowsSelected = table.getSelectedRows();
        GuiUtils.stopTableEditing(table);

        if (rowsSelected.length > 0 && rowsSelected[rowsSelected.length - 1] < table.getRowCount() - 1) {
            table.clearSelection();
            for (int i = rowsSelected.length - 1; i >= 0; i--) {
                int rowSelected = rowsSelected[i];
                tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected + 1);
            }
            for (int rowSelected : rowsSelected) {
                table.addRowSelectionInterval(rowSelected + 1, rowSelected + 1);
            }

            scrollToRowIfNotVisible(rowsSelected[0]+1);
        }
    }

    /**
     * ensure that a row is visible in the viewport
     * @param rowIndex row index
     */
    private void scrollToRowIfNotVisible(int rowIndex) {
        if(table.getParent() instanceof JViewport) {
            Rectangle visibleRect = table.getVisibleRect();
            final int cellIndex = 0;
            Rectangle cellRect = table.getCellRect(rowIndex, cellIndex, false);
            if (visibleRect.y > cellRect.y) {
                table.scrollRectToVisible(cellRect);
            } else {
                Rectangle rect2 = table.getCellRect(rowIndex + getNumberOfVisibleRows(table), cellIndex, true);
                int width = rect2.y - cellRect.y;
                table.scrollRectToVisible(new Rectangle(cellRect.x, cellRect.y, cellRect.width, cellRect.height + width));
            }
        }
    }

    /**
     * @param table {@link JTable}
     * @return number of visible rows
     */
    private static int getNumberOfVisibleRows(JTable table) {
        Rectangle vr = table.getVisibleRect();
        int first = table.rowAtPoint(vr.getLocation());
        vr.translate(0, vr.height);
        return table.rowAtPoint(vr.getLocation()) - first;
    }

    /**
     *  Move a row down
     */
    private void moveUp() {
        //get the selected rows before stopping editing
        // or the selected rows will be unselected
        int[] rowsSelected = table.getSelectedRows();
        GuiUtils.stopTableEditing(table);

        if (rowsSelected.length > 0 && rowsSelected[0] > 0) {
            table.clearSelection();
            for (int rowSelected : rowsSelected) {
                tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected - 1);
            }

            for (int rowSelected : rowsSelected) {
                table.addRowSelectionInterval(rowSelected - 1, rowSelected - 1);
            }

            scrollToRowIfNotVisible(rowsSelected[0]-1);
        }
    }

    private void addFromClipboard() {
        addFromClipboard(CLIPBOARD_LINE_DELIMITERS, CLIPBOARD_ARG_DELIMITERS);
    }

    /**
     * Add values from the clipboard
     * @param lineDelimiter Delimiter string to split clipboard into lines
     * @param argDelimiter Delimiter string to split line into key-value pair
     */
    private void addFromClipboard(String lineDelimiter, String argDelimiter) {
        GuiUtils.stopTableEditing(table);
        int rowCount = table.getRowCount();
        try {
            String clipboardContent = GuiUtils.getPastedText();
            if(clipboardContent == null) {
                return;
            }
            String[] clipboardLines = clipboardContent.split(lineDelimiter);
            for (String clipboardLine : clipboardLines) {
                String[] clipboardCols = clipboardLine.split(argDelimiter);
                if (clipboardCols.length > 0) {
                    HTTPFileArg argument = createHTTPFileArgFromClipboard(clipboardCols);
                    if (argument != null) {
                        tableModel.addRow(argument);
                    }
                }
            }
            if (table.getRowCount() > rowCount) {
                checkButtonsStatus();

                // Highlight (select) and scroll to the appropriate rows.
                int rowToSelect = tableModel.getRowCount() - 1;
                table.setRowSelectionInterval(rowCount, rowToSelect);
                table.scrollRectToVisible(table.getCellRect(rowCount, 0, true));
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this,
                    "Could not add read file arguments from clipboard:\n" + ioe.getLocalizedMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedFlavorException ufe) {
            JOptionPane.showMessageDialog(this,
                    "Could not add retrieve " + DataFlavor.stringFlavor.getHumanPresentableName()
                            + " from clipboard" + ufe.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private HTTPFileArg createHTTPFileArgFromClipboard(String[] clipboardCols) {
        if (clipboardCols.length == 1) {
            return new HTTPFileArg(clipboardCols[0]);
        } else if (clipboardCols.length == 2) {
            return new HTTPFileArg(clipboardCols[0], clipboardCols[1], "");
        } else if (clipboardCols.length == 3) {
            return new HTTPFileArg(clipboardCols[0], clipboardCols[1], clipboardCols[2]);
        }
        return null;
    }

    /**
     * Show Row Detail
     */
    private void showDetail() {
        //get the selected rows before stopping editing
        // or the selected will be unselected
        int[] rowsSelected = table.getSelectedRows();
        GuiUtils.stopTableEditing(table);

        if (rowsSelected.length == 1) {
            table.clearSelection();
            RowDetailDialog detailDialog = new RowDetailDialog(tableModel, rowsSelected[0]);
            detailDialog.setVisible(true);
        }
    }

}
