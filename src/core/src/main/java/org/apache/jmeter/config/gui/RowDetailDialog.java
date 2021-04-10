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

package org.apache.jmeter.config.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import org.apache.jmeter.gui.action.KeyStrokes;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.apache.jorphan.gui.ObjectTableModel;

import net.miginfocom.swing.MigLayout;

/**
 * Show detail of a Row
 */
public class RowDetailDialog extends JDialog implements ActionListener, DocumentListener, ItemListener {

    private static final long serialVersionUID = 6578889215615435475L;

    /** Command for moving a row up in the table. */
    private static final String NEXT = "next"; // $NON-NLS-1$

    /** Command for moving a row down in the table. */
    private static final String PREVIOUS = "previous"; // $NON-NLS-1$

    /** Command for CANCEL. */
    private static final String CLOSE = "close"; // $NON-NLS-1$

    private static final String UPDATE = "update"; // $NON-NLS-1$

    private List<JComponent> dataComponents;

    private JButton nextButton;

    private JButton previousButton;

    private JButton closeButton;

    private ObjectTableModel tableModel;

    private int selectedRow;

    private boolean textChanged = true; // change to false after the first insert


    public RowDetailDialog() {
        super();
    }

    public RowDetailDialog(ObjectTableModel tableModel, int selectedRow) {
        super((JFrame) null, JMeterUtils.getResString("detail"), true); //$NON-NLS-1$
        this.tableModel = tableModel;
        this.selectedRow = selectedRow;
        init();
    }

    @Override
    protected JRootPane createRootPane() {
        JRootPane rootPane = new JRootPane();
        // Hide Window on ESC
        Action escapeAction = new AbstractAction("ESCAPE") {

            private static final long serialVersionUID = -8699034338969407625L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setVisible(false);
            }
        };
        // Do update on Enter
        Action enterAction = new AbstractAction("ENTER") {

            private static final long serialVersionUID = -1529005452976176873L;

            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                doUpdate(actionEvent);
                setVisible(false);
            }
        };
        ActionMap actionMap = rootPane.getActionMap();
        actionMap.put(escapeAction.getValue(Action.NAME), escapeAction);
        actionMap.put(enterAction.getValue(Action.NAME), enterAction);
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStrokes.ESC, escapeAction.getValue(Action.NAME));
        inputMap.put(KeyStrokes.ENTER, enterAction.getValue(Action.NAME));
        return rootPane;
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        this.getContentPane().setLayout(new BorderLayout(10,10));

        JPanel dataPanel = new JPanel(new MigLayout("fillx, wrap 2", "[][fill, grow]"));
        dataComponents = new ArrayList<>();

        final int columnCount = tableModel.getColumnCount();
        final int linesPerTextArea = Math.max(5, 30 / Math.max(columnCount - 1, 1));
        for (int column = 0; column < columnCount; column++) {
            JLabel dataLabel = new JLabel(JMeterUtils.getResString(tableModel.getColumnName(column)));
            dataPanel.add(dataLabel);
            JComponent component = createComponentForRow(columnCount, linesPerTextArea, column,
                    tableModel.getValueAt(selectedRow, column));
            dataPanel.add(component);
            dataLabel.setLabelFor(component);
        }

        setValues(selectedRow);

        JPanel mainPanel = new JPanel(new MigLayout());
        mainPanel.add(dataPanel, "wrap");

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton updateButton = new JButton(JMeterUtils.getResString("update")); //$NON-NLS-1$
        updateButton.setActionCommand(UPDATE);
        updateButton.addActionListener(this);
        closeButton = new JButton(JMeterUtils.getResString("close")); //$NON-NLS-1$
        closeButton.setActionCommand(CLOSE);
        closeButton.addActionListener(this);
        nextButton = new JButton(JMeterUtils.getResString("next")); //$NON-NLS-1$
        nextButton.setActionCommand(NEXT);
        nextButton.addActionListener(this);
        nextButton.setEnabled(selectedRow < tableModel.getRowCount()-1);
        previousButton = new JButton(JMeterUtils.getResString("previous")); //$NON-NLS-1$
        previousButton.setActionCommand(PREVIOUS);
        previousButton.addActionListener(this);
        previousButton.setEnabled(selectedRow > 0);

        buttonsPanel.add(updateButton);
        buttonsPanel.add(previousButton);
        buttonsPanel.add(nextButton);
        buttonsPanel.add(closeButton);
        mainPanel.add(buttonsPanel, "center");
        this.getContentPane().add(mainPanel);
        dataComponents.get(0).requestFocusInWindow();

        this.pack();
        ComponentUtil.centerComponentInWindow(this);
    }

    private JComponent createComponentForRow(final int columnCount, final int linesPerTextArea, int column, Object value) {
        if (column > 0 || columnCount == 1) {
            if (tableModel.getColumnClass(column).equals(Boolean.class)) {
                JCheckBox checkBox = new JCheckBox();
                if (value instanceof Boolean) {
                    checkBox.setSelected((Boolean) value);
                }
                checkBox.addItemListener(this);
                dataComponents.add(checkBox);
                return checkBox;
            } else {
                JSyntaxTextArea dataArea = JSyntaxTextArea
                        .getInstance(linesPerTextArea, 80);
                if (value instanceof String) {
                    dataArea.setInitialText((String) value);
                }
                dataArea.getDocument().addDocumentListener(this);
                dataComponents.add(dataArea);
                return JTextScrollPane.getInstance(dataArea);
            }
        } else {
            final JTextField nameTF = new JTextField("");
            if (value instanceof String) {
                nameTF.setText((String) value);
            }
            nameTF.getDocument().addDocumentListener(this);
            dataComponents.add(nameTF);
            return nameTF;
        }
    }

    /**
     * Do search
     * @param e {@link ActionEvent}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        if (action.equals(CLOSE)) {
            this.setVisible(false);
        }
        else if (action.equals(NEXT)) {
            selectedRow++;
            previousButton.setEnabled(true);
            nextButton.setEnabled(selectedRow < tableModel.getRowCount()-1);
            setValues(selectedRow);
        }
        else if (action.equals(PREVIOUS)) {
            selectedRow--;
            nextButton.setEnabled(true);
            previousButton.setEnabled(selectedRow > 0);
            setValues(selectedRow);
        }
        else if (action.equals(UPDATE)) {
            doUpdate(e);
        }
    }

    /**
     * Set dataComponents fields based on the value of the tableModel
     * @param selectedRow Selected row
     */
    private void setValues(int selectedRow) {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            final JComponent component = dataComponents.get(i);
            if (component instanceof JTextComponent) {
                JTextComponent dataArea = (JTextComponent) component;
                dataArea.setText((String) tableModel.getValueAt(selectedRow, i));
                if (dataArea instanceof JSyntaxTextArea) {
                    dataArea.setCaretPosition(0);
                }
            } else if (component instanceof JCheckBox) {
                ((JCheckBox) component).setSelected((Boolean) tableModel.getValueAt(selectedRow, i));
            }
        }
        textChanged = false;
    }

    /**
     * Update model values
     * @param actionEvent the event that led to this call
     */
    protected void doUpdate(ActionEvent actionEvent) {
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            final JComponent component = dataComponents.get(i);
            if (component instanceof JTextComponent) {
                tableModel.setValueAt(((JTextComponent) component).getText(), selectedRow, i);
            } else if (component instanceof JCheckBox) {
                tableModel.setValueAt(((JCheckBox) component).isSelected(), selectedRow, i);
            }
        }
        // Change Cancel label to Close
        closeButton.setText(JMeterUtils.getResString("close")); //$NON-NLS-1$
        textChanged = false;
    }

    /**
     * Change the label of Close button to Cancel (after the first text changes)
     */
    private void changeLabelButton() {
        if (!textChanged) {
            closeButton.setText(JMeterUtils.getResString("cancel")); //$NON-NLS-1$
            textChanged = true;
        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        changeLabelButton();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        changeLabelButton();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        changeLabelButton();
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        changeLabelButton();
    }

}
