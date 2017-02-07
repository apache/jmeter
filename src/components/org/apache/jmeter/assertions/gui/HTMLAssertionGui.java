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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.jmeter.assertions.HTMLAssertion;
import org.apache.jmeter.gui.util.FilePanel;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GUI for HTMLAssertion
 */
public class HTMLAssertionGui extends AbstractAssertionGui implements KeyListener, ActionListener {

    private static final Logger log = LoggerFactory.getLogger(HTMLAssertionGui.class);

    private static final long serialVersionUID = 2L;

    // Names for the fields
    private static final String WARNING_THRESHOLD_FIELD = "warningThresholdField"; // $NON-NLS-1$

    private static final String ERROR_THRESHOLD_FIELD = "errorThresholdField"; // $NON-NLS-1$

    // instance attributes
    private JTextField errorThresholdField = null;

    private JTextField warningThresholdField = null;

    private JCheckBox errorsOnly = null;

    private JComboBox<String> docTypeBox = null;

    private JRadioButton htmlRadioButton = null;

    private JRadioButton xhtmlRadioButton = null;

    private JRadioButton xmlRadioButton = null;

    private FilePanel filePanel = null;

    /**
     * The constructor.
     */
    public HTMLAssertionGui() {
        init();
    }

    /**
     * Returns the label to be shown within the JTree-Component.
     */
    @Override
    public String getLabelResource() {
        return "html_assertion_title"; // $NON-NLS-1$
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        HTMLAssertion el = new HTMLAssertion();
        modifyTestElement(el);
        return el;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement inElement) {

        log.debug("HTMLAssertionGui.modifyTestElement() called");

        configureTestElement(inElement);

        String errorThresholdString = errorThresholdField.getText();
        long errorThreshold = 0;

        try {
            errorThreshold = Long.parseLong(errorThresholdString);
        } catch (NumberFormatException e) {
            errorThreshold = 0;
        }
        ((HTMLAssertion) inElement).setErrorThreshold(errorThreshold);

        String warningThresholdString = warningThresholdField.getText();
        long warningThreshold = 0;
        try {
            warningThreshold = Long.parseLong(warningThresholdString);
        } catch (NumberFormatException e) {
            warningThreshold = 0;
        }
        ((HTMLAssertion) inElement).setWarningThreshold(warningThreshold);

        String docTypeString = docTypeBox.getSelectedItem().toString();
        ((HTMLAssertion) inElement).setDoctype(docTypeString);

        boolean trackErrorsOnly = errorsOnly.isSelected();
        ((HTMLAssertion) inElement).setErrorsOnly(trackErrorsOnly);

        if (htmlRadioButton.isSelected()) {
            ((HTMLAssertion) inElement).setHTML();
        } else if (xhtmlRadioButton.isSelected()) {
            ((HTMLAssertion) inElement).setXHTML();
        } else {
            ((HTMLAssertion) inElement).setXML();
        }
        ((HTMLAssertion) inElement).setFilename(filePanel.getFilename());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     * {@inheritDoc}
     */
    @Override
    public void clearGui() {
        super.clearGui();

        docTypeBox.setSelectedIndex(0);
        htmlRadioButton.setSelected(true);
        xhtmlRadioButton.setSelected(false);
        xmlRadioButton.setSelected(false);
        errorThresholdField.setText("0"); //$NON-NLS-1$
        warningThresholdField.setText("0"); //$NON-NLS-1$
        filePanel.setFilename(""); //$NON-NLS-1$
        errorsOnly.setSelected(false);
    }

    /**
     * Configures the associated test element.
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement inElement) {
        super.configure(inElement);
        HTMLAssertion lAssertion = (HTMLAssertion) inElement;
        errorThresholdField.setText(String.valueOf(lAssertion.getErrorThreshold()));
        warningThresholdField.setText(String.valueOf(lAssertion.getWarningThreshold()));
        errorsOnly.setSelected(lAssertion.isErrorsOnly());
        docTypeBox.setSelectedItem(lAssertion.getDoctype());
        if (lAssertion.isHTML()) {
            htmlRadioButton.setSelected(true);
        } else if (lAssertion.isXHTML()) {
            xhtmlRadioButton.setSelected(true);
        } else {
            xmlRadioButton.setSelected(true);
        }
        if (lAssertion.isErrorsOnly()) {
            warningThresholdField.setEnabled(false);
            warningThresholdField.setEditable(false);
        }
        else {
            warningThresholdField.setEnabled(true);
            warningThresholdField.setEditable(true);
        }
        filePanel.setFilename(lAssertion.getFilename());
    }

    /**
     * Inits the GUI.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)

        setLayout(new BorderLayout(0, 10));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // USER_INPUT
        VerticalPanel assertionPanel = new VerticalPanel();
        assertionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Tidy Settings"));

        // doctype
        HorizontalPanel docTypePanel = new HorizontalPanel();
        docTypeBox = new JComboBox<>(new String[] { "omit", "auto", "strict", "loose" });
        // docTypePanel.add(new
        // JLabel(JMeterUtils.getResString("duration_assertion_label"))); //$NON-NLS-1$
        docTypePanel.add(new JLabel("Doctype:"));
        docTypePanel.add(docTypeBox);
        assertionPanel.add(docTypePanel);

        // format (HTML, XHTML, XML)
        VerticalPanel formatPanel = new VerticalPanel();
        formatPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Format"));
        htmlRadioButton = new JRadioButton("HTML", true); //$NON-NLS-1$
        xhtmlRadioButton = new JRadioButton("XHTML", false); //$NON-NLS-1$
        xmlRadioButton = new JRadioButton("XML", false); //$NON-NLS-1$
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(htmlRadioButton);
        buttonGroup.add(xhtmlRadioButton);
        buttonGroup.add(xmlRadioButton);
        formatPanel.add(htmlRadioButton);
        formatPanel.add(xhtmlRadioButton);
        formatPanel.add(xmlRadioButton);
        assertionPanel.add(formatPanel);

        // errors only
        errorsOnly = new JCheckBox("Errors only", false);
        errorsOnly.addActionListener(this);
        assertionPanel.add(errorsOnly);

        // thresholds
        HorizontalPanel thresholdPanel = new HorizontalPanel();
        thresholdPanel.add(new JLabel("Error threshold:"));
        errorThresholdField = new JTextField("0", 5); // $NON-NLS-1$
        errorThresholdField.setName(ERROR_THRESHOLD_FIELD);
        errorThresholdField.addKeyListener(this);
        thresholdPanel.add(errorThresholdField);
        thresholdPanel.add(new JLabel("Warning threshold:"));
        warningThresholdField = new JTextField("0", 5); // $NON-NLS-1$
        warningThresholdField.setName(WARNING_THRESHOLD_FIELD);
        warningThresholdField.addKeyListener(this);
        thresholdPanel.add(warningThresholdField);
        assertionPanel.add(thresholdPanel);

        // file panel
        filePanel = new FilePanel(JMeterUtils.getResString("html_assertion_file"), ".txt"); //$NON-NLS-1$ //$NON-NLS-2$
        assertionPanel.add(filePanel);

        mainPanel.add(assertionPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * This method is called from errors-only checkbox
     *
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (errorsOnly.isSelected()) {
            warningThresholdField.setEnabled(false);
            warningThresholdField.setEditable(false);
        } else {
            warningThresholdField.setEnabled(true);
            warningThresholdField.setEditable(true);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // NOOP
    }

    @Override
    public void keyReleased(KeyEvent e) {
        String fieldName = e.getComponent().getName();

        if (fieldName.equals(WARNING_THRESHOLD_FIELD)) {
            validateInteger(warningThresholdField);
        }

        if (fieldName.equals(ERROR_THRESHOLD_FIELD)) {
            validateInteger(errorThresholdField);
        }
    }

    private void validateInteger(JTextField field){
        try {
            Integer.parseInt(field.getText());
        } catch (NumberFormatException nfe) {
            int length = field.getText().length();
            if (length > 0) {
                JOptionPane.showMessageDialog(this, "Only digits allowed", "Invalid data",
                        JOptionPane.WARNING_MESSAGE);
                // Drop the last character:
                field.setText(field.getText().substring(0, length-1));
            }
        }

    }
    @Override
    public void keyTyped(KeyEvent e) {
        // NOOP
    }

}
