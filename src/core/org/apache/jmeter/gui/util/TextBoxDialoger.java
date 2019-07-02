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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.gui.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;

/**
 * Dialog text box to display some text in a box
 *
 */
public class TextBoxDialoger implements ActionListener {

    private static final String CANCEL_COMMAND = "cancel_dialog"; // $NON-NLS-1$

    private static final String SAVE_CLOSE_COMMAND = "save_close_dialog"; // $NON-NLS-1$

    private static final String CLOSE_COMMAND = "close_dialog"; // $NON-NLS-1$

    private JDialog dialog;

    private JEditorPane textBox;

    private String originalText;

    private boolean editable = false;

    /**
     * Dialog text box
     */
    public TextBoxDialoger() {
        // Empty box
        init(""); //$NON-NLS-1$
    }

    /**
     * Dialog text box
     * @param text - text to display in a box
     */
    public TextBoxDialoger(String text) {
        init(text);
    }

    /**
     * Dialog text box
     * @param text - text to display in a box
     * @param editable - allow to modify text
     */
    public TextBoxDialoger(String text, boolean editable) {
        this.editable = editable;
        init(text);
    }

    private void init(String text) {
        createDialogBox();
        setTextBox(text);
        dialog.setVisible(true);
    }

    private void createDialogBox() {
        JFrame mainFrame = GuiPackage.getInstance().getMainFrame();
        String title = editable ? JMeterUtils.getResString("textbox_title_edit") //$NON-NLS-1$
                : JMeterUtils.getResString("textbox_title_view"); //$NON-NLS-1$
        dialog = new JDialog(mainFrame, title, true);  // modal dialog box

        // Close action dialog box when tapping Escape key
        JPanel content = (JPanel) dialog.getContentPane();
        content.registerKeyboardAction(this, KeyStrokes.ESC,
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        textBox = new JEditorPane();
        textBox.setEditable(editable);

        JScrollPane textBoxScrollPane = GuiUtils.makeScrollPane(textBox);

        JPanel btnBar = new JPanel();
        btnBar.setLayout(new FlowLayout(FlowLayout.RIGHT));
        if (editable) {
            JButton cancelBtn = new JButton(JMeterUtils.getResString("textbox_cancel")); //$NON-NLS-1$
            cancelBtn.setActionCommand(CANCEL_COMMAND);
            cancelBtn.addActionListener(this);
            JButton saveBtn = new JButton(JMeterUtils.getResString("textbox_save_close")); //$NON-NLS-1$
            saveBtn.setActionCommand(SAVE_CLOSE_COMMAND);
            saveBtn.addActionListener(this);

            btnBar.add(cancelBtn);
            btnBar.add(saveBtn);
        } else {
            JButton closeBtn = new JButton(JMeterUtils.getResString("textbox_close")); //$NON-NLS-1$
            closeBtn.setActionCommand(CLOSE_COMMAND);
            closeBtn.addActionListener(this);

            btnBar.add(closeBtn);
        }

        // Prepare dialog box
        Container panel = dialog.getContentPane();
        dialog.setMinimumSize(new Dimension(400, 250));
        panel.add(textBoxScrollPane, BorderLayout.CENTER);
        panel.add(btnBar, BorderLayout.SOUTH);

        // determine location on screen
        Point p = mainFrame.getLocationOnScreen();
        Dimension d1 = mainFrame.getSize();
        Dimension d2 = dialog.getSize();
        dialog.setLocation(p.x + (d1.width - d2.width) / 2, p.y + (d1.height - d2.height) / 2);
        dialog.pack();
    }

    private void closeDialog() {
        dialog.setVisible(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (CANCEL_COMMAND.equals(command)) {
            closeDialog();
            setTextBox(originalText);
        } else {
            // must be CLOSE or SAVE_CLOSE COMMANDS
            closeDialog();
        }

    }

    public void setTextBox(String text) {
        originalText = text; // text backup
        textBox.setText(text);
    }

    public String getTextBox() {
        return textBox.getText();
    }

    /**
     * Class to display a dialog box and cell's content
     * when double click on a table's cell
     *
     */
    public static class TextBoxDoubleClick extends MouseAdapter {

        private JTable table = null;

        public TextBoxDoubleClick(JTable table) {
            super();
            this.table = table;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) { // double click
                TableModel tm = table.getModel();
                Object value = tm.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
                new TextBoxDialoger(value.toString(), false); // view only NOSONAR this instantiation opens a popup
            }
        }
    }

    /**
     * Class to edit in a dialog box the cell's content
     * when double (pressed) click on a table's cell which is editable
     *
     */
    public static class TextBoxDoubleClickPressed extends MouseAdapter {

        private JTable table = null;

        public TextBoxDoubleClickPressed(JTable table) {
            super();
            this.table = table;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.getClickCount() == 2) { // double (pressed) click
                TableModel tm = table.getModel();
                Object value = tm.getValueAt(table.getSelectedRow(), table.getSelectedColumn());
                if (value instanceof String) {
                    if (table.getCellEditor() != null) {
                        table.getCellEditor().cancelCellEditing(); // in main table (evt mousePressed because cell is editable)
                    }
                    TextBoxDialoger tbd = new TextBoxDialoger(value.toString(), true);
                    tm.setValueAt(tbd.getTextBox(), table.getSelectedRow(), table.getSelectedColumn());
                } // else do nothing (cell isn't a string to edit)
            }
        }

    }


}
