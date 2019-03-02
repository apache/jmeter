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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.apache.commons.lang3.StringUtils;
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

    /** Command for adding a row to the table. */
    private static final String ADD = "add"; // $NON-NLS-1$

    /** Command for browsing filesystem to set path of selected row in table. */
    private static final String BROWSE = "browse"; // $NON-NLS-1$

    /** Command for removing a row from the table. */
    private static final String DELETE = "delete"; // $NON-NLS-1$

    private static final String FILEPATH = "send_file_filename_label"; // $NON-NLS-1$

    /** The parameter name column title of file table. */
    private static final String PARAMNAME = "send_file_param_name_label"; //$NON-NLS-1$

    /** The mime type column title of file table. */
    private static final String MIMETYPE = "send_file_mime_label"; //$NON-NLS-1$


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
            checkDeleteAndBrowseStatus();
        }
    }


    /**
     * Enable or disable the delete button depending on whether or not there is
     * a row to be deleted.
     */
    private void checkDeleteAndBrowseStatus() {
        // Disable DELETE and BROWSE buttons if there are no rows in
        // the table to delete.
        if (tableModel.getRowCount() == 0) {
            browse.setEnabled(false);
            delete.setEnabled(false);
        } else {
            browse.setEnabled(true);
            delete.setEnabled(true);
        }
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
            checkDeleteAndBrowseStatus();
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

        checkDeleteAndBrowseStatus();

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

        checkDeleteAndBrowseStatus();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        add.addActionListener(this);
        browse.addActionListener(this);
        delete.addActionListener(this);
        buttonPanel.add(add);
        buttonPanel.add(browse);
        buttonPanel.add(delete);
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
        return pane;
    }
}
