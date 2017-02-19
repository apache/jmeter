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

package org.apache.jmeter.assertions.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;

import org.apache.jmeter.assertions.ResponseAssertion;
import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.gui.util.TextAreaCellRenderer;
import org.apache.jmeter.gui.util.TextAreaTableCellEditor;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;

/**
 * GUI interface for a {@link ResponseAssertion}.
 *
 */
public class AssertionGui extends AbstractAssertionGui {
    private static final long serialVersionUID = 240L;

    /** The name of the table column in the list of patterns. */
    private static final String COL_RESOURCE_NAME = "assertion_patterns_to_test"; //$NON-NLS-1$

    /** Radio button indicating that the text response should be tested. */
    private JRadioButton responseStringButton;

    /** Radio button indicating that the text of a document should be tested. */
    private JRadioButton responseAsDocumentButton;

    /** Radio button indicating that the URL should be tested. */
    private JRadioButton urlButton;

    /** Radio button indicating that the responseMessage should be tested. */
    private JRadioButton responseMessageButton;

    /** Radio button indicating that the responseCode should be tested. */
    private JRadioButton responseCodeButton;

    /** Radio button indicating that the headers should be tested. */
    private JRadioButton responseHeadersButton;
    
    /** Radio button indicating that the request headers should be tested. */
    private JRadioButton requestHeadersButton;

    /**
     * Checkbox to indicate whether the response should be forced successful
     * before testing. This is intended for use when checking the status code or
     * status message.
     */
    private JCheckBox assumeSuccess;

    /**
     * Radio button indicating to test if the field contains one of the
     * patterns.
     */
    private JRadioButton containsBox;

    /**
     * Radio button indicating to test if the field matches one of the patterns.
     */
    private JRadioButton matchesBox;

    /**
     * Radio button indicating if the field equals the string.
     */
    private JRadioButton equalsBox;

    /**
     * Radio button indicating if the field contains the string.
     */
    private JRadioButton substringBox;

    /**
     * Checkbox indicating to test that the field does NOT contain/match the
     * patterns.
     */
    private JCheckBox notBox;
    
    /**
     * Add new OR checkbox.
     */
    private JCheckBox orBox;

    /** A table of patterns to test against. */
    private JTable stringTable;

    /** Button to delete a pattern. */
    private JButton deletePattern;

    /** Table model for the pattern table. */
    private PowerTableModel tableModel;

    /**
     * Create a new AssertionGui panel.
     */
    public AssertionGui() {
        init();
    }

    @Override
    public String getLabelResource() {
        return "assertion_title"; // $NON-NLS-1$
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        ResponseAssertion el = new ResponseAssertion();
        modifyTestElement(el);
        return el;
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement el) {
        GuiUtils.stopTableEditing(stringTable);
        configureTestElement(el);
        if (el instanceof ResponseAssertion) {
            ResponseAssertion ra = (ResponseAssertion) el;

            saveScopeSettings(ra);

            ra.clearTestStrings();
            String[] testStrings = tableModel.getData().getColumn(COL_RESOURCE_NAME);
            for (String testString : testStrings) {
                ra.addTestString(testString);
            }

            if (responseStringButton.isSelected()) {
                ra.setTestFieldResponseData();
            } else if (responseAsDocumentButton.isSelected()) {
                ra.setTestFieldResponseDataAsDocument();
            } else if (responseCodeButton.isSelected()) {
                ra.setTestFieldResponseCode();
            } else if (responseMessageButton.isSelected()) {
                ra.setTestFieldResponseMessage();
            } else if (requestHeadersButton.isSelected()) {
                ra.setTestFieldRequestHeaders();
            } else if (responseHeadersButton.isSelected()) {
                ra.setTestFieldResponseHeaders();
            } else { // Assume URL
                ra.setTestFieldURL();
            }

            ra.setAssumeSuccess(assumeSuccess.isSelected());

            if (containsBox.isSelected()) {
                ra.setToContainsType();
            } else if (equalsBox.isSelected()) {
                ra.setToEqualsType();
            } else if (substringBox.isSelected()) {
                ra.setToSubstringType();
            } else {
                ra.setToMatchType();
            }

            if (notBox.isSelected()) {
                ra.setToNotType();
            } else {
                ra.unsetNotType();
            }

            if (orBox.isSelected()) {
                ra.setToOrType();
            } else {
                ra.unsetOrType();
            }
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        GuiUtils.stopTableEditing(stringTable);
        tableModel.clearData();

        responseStringButton.setSelected(true);
        urlButton.setSelected(false);
        responseCodeButton.setSelected(false);
        responseMessageButton.setSelected(false);
        requestHeadersButton.setSelected(false);
        responseHeadersButton.setSelected(false);
        assumeSuccess.setSelected(false);

        substringBox.setSelected(true);
        notBox.setSelected(false);
        orBox.setSelected(false);
    }

    /**
     * A newly created component can be initialized with the contents of a Test
     * Element object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param el
     *            the TestElement to configure
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        ResponseAssertion model = (ResponseAssertion) el;

        showScopeSettings(model, true);

        if (model.isContainsType()) {
            containsBox.setSelected(true);
        } else if (model.isEqualsType()) {
            equalsBox.setSelected(true);
        } else if (model.isSubstringType()) {
            substringBox.setSelected(true);
        } else {
            matchesBox.setSelected(true);
        }

        notBox.setSelected(model.isNotType());
        orBox.setSelected(model.isOrType());

        if (model.isTestFieldResponseData()) {
            responseStringButton.setSelected(true);
        } else if (model.isTestFieldResponseDataAsDocument()) {
            responseAsDocumentButton.setSelected(true);
        } else if (model.isTestFieldResponseCode()) {
            responseCodeButton.setSelected(true);
        } else if (model.isTestFieldResponseMessage()) {
            responseMessageButton.setSelected(true);
        } else if (model.isTestFieldRequestHeaders()) {
            requestHeadersButton.setSelected(true);
        } else if (model.isTestFieldResponseHeaders()) {
            responseHeadersButton.setSelected(true);
        } else // Assume it is the URL
        {
            urlButton.setSelected(true);
        }

        assumeSuccess.setSelected(model.getAssumeSuccess());

        tableModel.clearData();
        for (JMeterProperty jMeterProperty : model.getTestStrings()) {
            tableModel.addRow(new Object[] { jMeterProperty.getStringValue() });
        }

        if (model.getTestStrings().size() == 0) {
            deletePattern.setEnabled(false);
        } else {
            deletePattern.setEnabled(true);
        }

        tableModel.fireTableDataChanged();
    }

    /**
     * Initialize the GUI components and layout.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        Box box = Box.createVerticalBox();
        setBorder(makeBorder());

        box.add(makeTitlePanel());
        box.add(createScopePanel(true));
        box.add(createFieldPanel());
        box.add(createTypePanel());
        add(box, BorderLayout.NORTH);
        add(createStringPanel(), BorderLayout.CENTER);
    }

    /**
     * Create a panel allowing the user to choose which response field should be
     * tested.
     *
     * @return a new panel for selecting the response field
     */
    private JPanel createFieldPanel() {
        responseStringButton = new JRadioButton(JMeterUtils.getResString("assertion_text_resp")); //$NON-NLS-1$
        responseAsDocumentButton = new JRadioButton(JMeterUtils.getResString("assertion_text_document")); //$NON-NLS-1$
        urlButton = new JRadioButton(JMeterUtils.getResString("assertion_url_samp")); //$NON-NLS-1$
        responseCodeButton = new JRadioButton(JMeterUtils.getResString("assertion_code_resp")); //$NON-NLS-1$
        responseMessageButton = new JRadioButton(JMeterUtils.getResString("assertion_message_resp")); //$NON-NLS-1$
        responseHeadersButton = new JRadioButton(JMeterUtils.getResString("assertion_headers")); //$NON-NLS-1$
        requestHeadersButton = new JRadioButton(JMeterUtils.getResString("assertion_req_headers")); //$NON-NLS-1$

        ButtonGroup group = new ButtonGroup();
        group.add(responseStringButton);
        group.add(responseAsDocumentButton);
        group.add(urlButton);
        group.add(responseCodeButton);
        group.add(responseMessageButton);
        group.add(requestHeadersButton);
        group.add(responseHeadersButton);
        
        responseStringButton.setSelected(true);

        assumeSuccess = new JCheckBox(JMeterUtils.getResString("assertion_assume_success")); //$NON-NLS-1$

        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);

        JPanel panel = new JPanel(gridBagLayout);
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("assertion_resp_field"))); //$NON-NLS-1$

        addField(panel, responseStringButton, gbc);
        addField(panel, responseCodeButton, gbc);
        addField(panel, responseMessageButton, gbc);
        addField(panel, responseHeadersButton, gbc);

        resetContraints(gbc);
        addField(panel, requestHeadersButton, gbc);
        addField(panel, urlButton, gbc);
        addField(panel, responseAsDocumentButton, gbc);
        addField(panel, assumeSuccess, gbc);
        return panel;
    }
    
    private void addField(JPanel panel, JToggleButton button, GridBagConstraints gbc) {
        panel.add(button, gbc.clone());
        gbc.gridx++;
        gbc.fill=GridBagConstraints.HORIZONTAL;
    }
    
    // Next line
    private void resetContraints(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.fill=GridBagConstraints.NONE;
    }

    private void initConstraints(GridBagConstraints gbc) {
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 1;
    }


    /**
     * Create a panel allowing the user to choose what type of test should be
     * performed.
     *
     * @return a new panel for selecting the type of assertion test
     */
    private JPanel createTypePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("assertion_pattern_match_rules"))); //$NON-NLS-1$

        ButtonGroup group = new ButtonGroup();

        containsBox = new JRadioButton(JMeterUtils.getResString("assertion_contains")); //$NON-NLS-1$
        group.add(containsBox);
        containsBox.setSelected(true);
        panel.add(containsBox);

        matchesBox = new JRadioButton(JMeterUtils.getResString("assertion_matches")); //$NON-NLS-1$
        group.add(matchesBox);
        panel.add(matchesBox);

        equalsBox = new JRadioButton(JMeterUtils.getResString("assertion_equals")); //$NON-NLS-1$
        group.add(equalsBox);
        panel.add(equalsBox);

        substringBox = new JRadioButton(JMeterUtils.getResString("assertion_substring")); //$NON-NLS-1$
        group.add(substringBox);
        panel.add(substringBox);

        notBox = new JCheckBox(JMeterUtils.getResString("assertion_not")); //$NON-NLS-1$
        panel.add(notBox);

        orBox = new JCheckBox(JMeterUtils.getResString("assertion_or")); //$NON-NLS-1$
        panel.add(orBox);

        return panel;
    }

    /**
     * Create a panel allowing the user to supply a list of string patterns to
     * test against.
     *
     * @return a new panel for adding string patterns
     */
    private JPanel createStringPanel() {
        tableModel = new PowerTableModel(new String[] { COL_RESOURCE_NAME }, new Class[] { String.class });
        stringTable = new JTable(tableModel);
        stringTable.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        stringTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JMeterUtils.applyHiDPI(stringTable);


        TextAreaCellRenderer renderer = new TextAreaCellRenderer();
        stringTable.setRowHeight(renderer.getPreferredHeight());
        stringTable.setDefaultRenderer(String.class, renderer);
        stringTable.setDefaultEditor(String.class, new TextAreaTableCellEditor());
        stringTable.setPreferredScrollableViewportSize(new Dimension(100, 70));

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("assertion_patterns_to_test"))); //$NON-NLS-1$

        panel.add(new JScrollPane(stringTable), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create a panel with buttons to add and delete string patterns.
     *
     * @return the new panel with add and delete buttons
     */
    private JPanel createButtonPanel() {
        JButton addPattern = new JButton(JMeterUtils.getResString("add")); //$NON-NLS-1$
        addPattern.addActionListener(new AddPatternListener());
        
        JButton addFromClipboardPattern = new JButton(JMeterUtils.getResString("add_from_clipboard")); //$NON-NLS-1$
        addFromClipboardPattern.addActionListener(new AddFromClipboardListener());

        deletePattern = new JButton(JMeterUtils.getResString("delete")); //$NON-NLS-1$
        deletePattern.addActionListener(new ClearPatternsListener());
        deletePattern.setEnabled(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addPattern);
        buttonPanel.add(addFromClipboardPattern);
        buttonPanel.add(deletePattern);
        return buttonPanel;
    }

    /**
     * An ActionListener for deleting a pattern.
     *
     */
    private class ClearPatternsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            GuiUtils.cancelEditing(stringTable);
            
            int[] rowsSelected = stringTable.getSelectedRows();
            stringTable.clearSelection();
            if (rowsSelected.length > 0) {
                for (int i = rowsSelected.length - 1; i >= 0; i--) {
                    tableModel.removeRow(rowsSelected[i]);
                }
                tableModel.fireTableDataChanged();
            } else {
                if(tableModel.getRowCount()>0) {
                    tableModel.removeRow(0);
                    tableModel.fireTableDataChanged();
                }
            }

            if (stringTable.getModel().getRowCount() == 0) {
                deletePattern.setEnabled(false);
            }
        }
    }

    /**
     * An ActionListener for adding a pattern.
     */
    private class AddPatternListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            GuiUtils.stopTableEditing(stringTable);
            tableModel.addNewRow();
            checkButtonsStatus();
            tableModel.fireTableDataChanged();
        }
    }
    
    /**
     * An ActionListener for pasting from clipboard
     */
    private class AddFromClipboardListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            deletePattern.setEnabled(true);
            GuiUtils.stopTableEditing(stringTable);
            int rowCount = stringTable.getRowCount();
            try {
                String clipboardContent = GuiUtils.getPastedText();
                if(clipboardContent == null) {
                    return;
                }
                String[] clipboardLines = clipboardContent.split("\n");
                for (String clipboardLine : clipboardLines) {
                    tableModel.addRow(new Object[] { clipboardLine.trim() });
                }
                if (stringTable.getRowCount() > rowCount) {
                    checkButtonsStatus();

                    // Highlight (select) and scroll to the appropriate rows.
                    int rowToSelect = tableModel.getRowCount() - 1;
                    stringTable.setRowSelectionInterval(rowCount, rowToSelect);
                    stringTable.scrollRectToVisible(stringTable.getCellRect(rowCount, 0, true));
                }
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(GuiPackage.getInstance().getMainFrame(),
                        "Could not add data from clipboard:\n" + ioe.getLocalizedMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (UnsupportedFlavorException ufe) {
                JOptionPane.showMessageDialog(GuiPackage.getInstance().getMainFrame(),
                        "Could not add retrieve " + DataFlavor.stringFlavor.getHumanPresentableName()
                                + " from clipboard" + ufe.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
            tableModel.fireTableDataChanged();
        }
    }
    
    protected void checkButtonsStatus() {
        // Disable DELETE if there are no rows in the table to delete.
        if (tableModel.getRowCount() == 0) {
            deletePattern.setEnabled(false);
        } else {
            deletePattern.setEnabled(true);
        }
    }
}
