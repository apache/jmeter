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

package org.apache.jmeter.gui.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.JMeterFileFilter;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

public class FileListPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private JTable files = null;

    private transient ObjectTableModel tableModel = null;

    private static final String ACTION_BROWSE = "browse"; // $NON-NLS-1$

    private static final String LABEL_LIBRARY = "library"; // $NON-NLS-1$

    private JButton browse = new JButton(JMeterUtils.getResString(ACTION_BROWSE));

    private JButton clear = new JButton(JMeterUtils.getResString("clear")); // $NON-NLS-1$

    private JButton delete = new JButton(JMeterUtils.getResString("delete")); // $NON-NLS-1$

    private List<ChangeListener> listeners = new LinkedList<>();

    private String title;

    private String filetype;

    /**
     * Constructor for the FilePanel object.
     */
    public FileListPanel() {
        title = ""; // $NON-NLS-1$
        init();
    }

    public FileListPanel(String title) {
        this.title = title;
        init();
    }

    public FileListPanel(String title, String filetype) {
        this.title = title;
        this.filetype = filetype;
        init();
    }

    /**
     * Constructor for the FilePanel object.
     * @param l The changelistener for this panel
     * @param title The title of this panel
     */
    public FileListPanel(ChangeListener l, String title) {
        this.title = title;
        init();
        listeners.add(l);
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        this.setLayout(new BorderLayout(0, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
        JLabel jtitle = new JLabel(title);

        HorizontalPanel buttons = new HorizontalPanel();
        buttons.add(jtitle);
        buttons.add(browse);
        buttons.add(delete);
        buttons.add(clear);
        add(buttons,BorderLayout.NORTH);

        this.initializeTableModel();
        files = new JTable(tableModel);
        JMeterUtils.applyHiDPI(files);
        files.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        files.revalidate();

        JScrollPane scrollpane = new JScrollPane(files);
        scrollpane.setPreferredSize(new Dimension(100,80));
        add(scrollpane,BorderLayout.CENTER);

        browse.setActionCommand(ACTION_BROWSE); // $NON-NLS-1$
        browse.addActionListener(this);
        clear.addActionListener(this);
        delete.addActionListener(this);
    }

    /**
     * If the gui needs to enable/disable the FilePanel, call the method.
     *
     * @param enable Flag whether FilePanel should be enabled
     */
    public void enableFile(boolean enable) {
        browse.setEnabled(enable);
        files.setEnabled(false);
    }

    /**
     * Add a single file to the table
     * @param f The name of the file to be added
     */
    public void addFilename(String f) {
        tableModel.addRow(f);
    }

    /**
     * clear the files from the table
     */
    public void clearFiles() {
        tableModel.clearData();
    }

    public void setFiles(String[] files) {
        this.clearFiles();
        for (String file : files) {
            addFilename(file);
        }
    }

    public String[] getFiles() {
        GuiUtils.stopTableEditing(files);
        String[] filesArray = new String[tableModel.getRowCount()];
        for (int idx=0; idx < filesArray.length; idx++) {
            filesArray[idx] = (String)tableModel.getValueAt(idx,0);
        }
        return filesArray;
    }

    protected void deleteFile() {
        // If a table cell is being edited, we must cancel the editing before
        // deleting the row

        int rowSelected = files.getSelectedRow();
        if (rowSelected >= 0) {
            tableModel.removeRow(rowSelected);
            tableModel.fireTableDataChanged();

        }
    }

    private void fireFileChanged() {
        for (ChangeListener cl : listeners) {
            cl.stateChanged(new ChangeEvent(this));
        }
    }

    protected void initializeTableModel() {
        tableModel = new ObjectTableModel(new String[] { JMeterUtils.getResString(LABEL_LIBRARY) },
                new Functor[0] , new Functor[0] , // i.e. bypass the Functors
                new Class[] { String.class });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clear) {
            this.clearFiles();
        } else if (e.getActionCommand().equals(ACTION_BROWSE)) {
            JFileChooser chooser = new JFileChooser();
            String start = System.getProperty("user.dir", ""); // $NON-NLS-1$ // $NON-NLS-2$
            chooser.setCurrentDirectory(new File(start));
            chooser.setFileFilter(new JMeterFileFilter(new String[] { filetype }));
            chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            chooser.setMultiSelectionEnabled(true);
            chooser.showOpenDialog(GuiPackage.getInstance().getMainFrame());
            File[] cfiles = chooser.getSelectedFiles();
            if (cfiles != null) {
                for (File cfile : cfiles) {
                    this.addFilename(cfile.getPath());
                }
                fireFileChanged();
            }
        } else if (e.getSource() == delete) {
            this.deleteFile();
        } else {
            fireFileChanged();
        }
    }
}
