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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.assertions.XMLSchemaAssertion;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XMLSchemaAssertionGUI.java
 *
 */
public class XMLSchemaAssertionGUI extends AbstractAssertionGui {
    // class attributes
     private static final Logger log = LoggerFactory.getLogger(XMLSchemaAssertionGUI.class);

    private static final long serialVersionUID = 241L;

    private JTextField xmlSchema;

    /**
     * The constructor.
     */
    public XMLSchemaAssertionGUI() {
        init();
    }

    /**
     * Returns the label to be shown within the JTree-Component.
     */
    @Override
    public String getLabelResource() {
        return "xmlschema_assertion_title"; //$NON-NLS-1$
    }

    /**
     * create Test Element
     */
    @Override
    public TestElement createTestElement() {
        log.debug("XMLSchemaAssertionGui.createTestElement() called");
        XMLSchemaAssertion el = new XMLSchemaAssertion();
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

        log.debug("XMLSchemaAssertionGui.modifyTestElement() called");
        configureTestElement(inElement);
        ((XMLSchemaAssertion) inElement).setXsdFileName(xmlSchema.getText());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        xmlSchema.setText(""); //$NON-NLS-1$
    }

    /**
     * Configures the GUI from the associated test element.
     *
     * @param el -
     *            the test element (should be XMLSchemaAssertion)
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        XMLSchemaAssertion assertion = (XMLSchemaAssertion) el;
        xmlSchema.setText(assertion.getXsdFileName());
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
        assertionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "XML Schema"));

        // doctype
        HorizontalPanel xmlSchemaPanel = new HorizontalPanel();

        xmlSchemaPanel.add(new JLabel(JMeterUtils.getResString("xmlschema_assertion_label"))); //$NON-NLS-1$

        xmlSchema = new JTextField(26);
        xmlSchemaPanel.add(xmlSchema);

        assertionPanel.add(xmlSchemaPanel);

        mainPanel.add(assertionPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }
}
