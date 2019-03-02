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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.XPathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Gui component for representing a xpath expression
 *
 */
public class XPath2Panel extends JPanel {
    private static final long serialVersionUID = 241L;

    private static final Logger log = LoggerFactory.getLogger(XPath2Panel.class);

    private JCheckBox negated;

    private JSyntaxTextArea xpath;

    private JButton checkXPath;

    private JSyntaxTextArea namespacesTA;
    public String getNamespaces() {
        return this.namespacesTA.getText();
    }

    public void setNamespaces(String namespaces) {
        this.namespacesTA.setText(namespaces);
    }

    /**
     *
     */
    public XPath2Panel() {
        super();
        init();
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or
                          // final)
        setLayout(new BorderLayout());
        Box topBox = Box.createVerticalBox();
        Box box = Box.createHorizontalBox();
        box.add(getNegatedCheckBox());
        box.add(Box.createHorizontalGlue());
        box.add(getCheckXPathButton());
        box.add(Box.createHorizontalGlue());
        box.add(Box.createHorizontalGlue());
        box.add(Box.createHorizontalGlue());
        topBox.add(box);
        topBox.add(makeParameterPanel());
        add(topBox, BorderLayout.NORTH);
        add(JTextScrollPane.getInstance(getXPathField()), BorderLayout.CENTER);
        setDefaultValues();
    }
    private JPanel makeParameterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);
        panel.add(new JLabel(JMeterUtils.getResString("xpath_extractor_user_namespaces")), gbc.clone());
        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        namespacesTA = JSyntaxTextArea.getInstance(5, 80);
        panel.add(JTextScrollPane.getInstance(namespacesTA, true), gbc.clone());
        resetContraints(gbc);

        return panel;
    }
    private void resetContraints(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
    }

    private void initConstraints(GridBagConstraints gbc) {
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.weighty = 0;
    }
    /**
     * Set default values on this component
     */
    public void setDefaultValues() {
        setXPath("/"); //$NON-NLS-1$
        setNegated(false);
    }

    /**
     * Get the XPath String
     *
     * @return String
     */
    public String getXPath() {
        return this.xpath.getText();
    }

    /**
     * Set the string that will be used in the xpath evaluation
     *
     * @param xpath The string representing the xpath expression
     */
    public void setXPath(String xpath) {
        this.xpath.setInitialText(xpath);
    }

    /**
     * Does this negate the xpath results
     *
     * @return boolean
     */
    public boolean isNegated() {
        return this.negated.isSelected();
    }

    /**
     * Set this to true, if you want success when the xpath does not match.
     *
     * @param negated Flag whether xpath match should be negated
     */
    public void setNegated(boolean negated) {
        this.negated.setSelected(negated);
    }

    /**
     * Negated chechbox
     *
     * @return JCheckBox
     */
    public JCheckBox getNegatedCheckBox() {
        if (negated == null) {
            negated = new JCheckBox(JMeterUtils.getResString("xpath2_assertion_negate"), false); //$NON-NLS-1$
        }

        return negated;
    }

    /**
     * Check XPath button
     *
     * @return JButton
     */
    public JButton getCheckXPathButton() {
        if (checkXPath == null) {
            checkXPath = new JButton(JMeterUtils.getResString("xpath2_assertion_button")); //$NON-NLS-1$
            checkXPath.addActionListener(e -> validXPath(xpath.getText(), true, this.getNamespaces()));
        }
        return checkXPath;
    }

    /**
     * Returns the current {@link JSyntaxTextArea} for the xpath expression, or
     * creates a new one, if none is found.
     *
     * @return {@link JSyntaxTextArea} for the xpath expression
     */
    public JSyntaxTextArea getXPathField() {
        if (xpath == null) {
            xpath = JSyntaxTextArea.getInstance(20, 80);
            xpath.setLanguage("xpath"); //$NON-NLS-1$
        }
        return xpath;
    }

    /**
     * @return Returns the showNegate.
     */
    public boolean isShowNegated() {
        return this.getNegatedCheckBox().isVisible();
    }

    /**
     * @param showNegate
     *            The showNegate to set.
     */
    public void setShowNegated(boolean showNegate) {
        getNegatedCheckBox().setVisible(showNegate);
    }

    /**
     * Test whether an XPath is valid. It seems the Xalan has no easy way to
     * check, so this creates a dummy test document, then tries to evaluate the xpath against it.
     *
     * @param xpathString
     *            XPath String to validate
     * @param showDialog
     *            weather to show a dialog
     * @param namespaces Namespaces declaration (set of prefix=value separated by spaces)
     * @return returns true if valid, false otherwise.
     */
    public static boolean validXPath(String xpathString, boolean showDialog,String namespaces) {
        String ret = null;
        boolean success = true;
        Document testDoc = null;
        try {
            testDoc = XPathUtil.makeDocumentBuilder(false, false, false, false).newDocument();
            Element el = testDoc.createElement("root"); //$NON-NLS-1$
            testDoc.appendChild(el);
            XPathUtil.validateXPath2(testDoc, xpathString, namespaces);
        } catch (IllegalArgumentException | ParserConfigurationException | TransformerException e) {
            log.warn("Exception while validating XPath.", e);
            success = false;
            ret = e.getLocalizedMessage();
        }
        if (showDialog) {
            JOptionPane.showMessageDialog(null,
                    success ? JMeterUtils.getResString("xpath_assertion_valid") : ret, //$NON-NLS-1$
                    success ? JMeterUtils.getResString("xpath_assertion_valid") : //$NON-NLS-1$
                        JMeterUtils.getResString("xpath_assertion_failed"), //$NON-NLS-1$
                        success ? JOptionPane.INFORMATION_MESSAGE //$NON-NLS-1$
                                : JOptionPane.ERROR_MESSAGE);
        }
        return success;
    }
}
