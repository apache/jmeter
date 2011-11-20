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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.util.XPathUtil;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XPathPanel extends JPanel {
    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    // Lazily constructed. Does not matter if it is constructed more than once.
    private static Document testDoc;

    private JCheckBox negated;

    private JTextField xpath;

    private JButton checkXPath;

    /**
     * 
     */
    public XPathPanel() {
        super();
        init();
    }

    private void init() {
        Box hbox = Box.createHorizontalBox();
        hbox.add(Box.createHorizontalGlue());
        hbox.add(getXPathTextField());
        hbox.add(Box.createHorizontalGlue());
        hbox.add(getCheckXPathButton());

        Box vbox = Box.createVerticalBox();
        vbox.add(hbox);
        vbox.add(Box.createVerticalGlue());
        vbox.add(getNegatedCheckBox());

        add(vbox);

        setDefaultValues();
    }

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
     * @param xpath
     */
    public void setXPath(String xpath) {
        this.xpath.setText(xpath);
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
     * @param negated
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
            negated = new JCheckBox(JMeterUtils.getResString("xpath_assertion_negate"), false); //$NON-NLS-1$
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
            checkXPath = new JButton(JMeterUtils.getResString("xpath_assertion_button")); //$NON-NLS-1$
            checkXPath.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    validXPath(xpath.getText(), true);
                }
            });
        }
        return checkXPath;
    }

    public JTextField getXPathTextField() {
        if (xpath == null) {
            xpath = new JTextField(50);
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
     * @return returns true if valid, valse otherwise.
     */
    public static boolean validXPath(String xpathString, boolean showDialog) {
        String ret = null;
        boolean success = true;
        try {
            if (testDoc == null) {
                testDoc = XPathUtil.makeDocumentBuilder(false, false, false, false).newDocument();
                Element el = testDoc.createElement("root"); //$NON-NLS-1$
                testDoc.appendChild(el);

            }
            XPathUtil.validateXPath(testDoc, xpathString);
        } catch (IllegalArgumentException e) {
        	log.warn(e.getLocalizedMessage());
            success = false;
            ret = e.getLocalizedMessage();
        } catch (ParserConfigurationException e) {
            success = false;
            ret = e.getLocalizedMessage();
        } catch (TransformerException e) {
            success = false;
            ret = e.getLocalizedMessage();
        }

        if (showDialog) {
            JOptionPane.showMessageDialog(null, (success) ? JMeterUtils.getResString("xpath_assertion_valid") : ret, //$NON-NLS-1$
                    (success) ? JMeterUtils.getResString("xpath_assertion_valid") : JMeterUtils //$NON-NLS-1$
                            .getResString("xpath_assertion_failed"), (success) ? JOptionPane.INFORMATION_MESSAGE //$NON-NLS-1$
                            : JOptionPane.ERROR_MESSAGE);
        }
        return success;

    }
}
