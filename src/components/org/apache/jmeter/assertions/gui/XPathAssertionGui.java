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
import javax.swing.Box;
import javax.swing.JPanel;

import org.apache.jmeter.assertions.XPathAssertion;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

@GUIMenuSortOrder(50)
public class XPathAssertionGui extends AbstractAssertionGui {

    private static final long serialVersionUID = 240L;

    private XPathPanel xpath;

    private XMLConfPanel xml;

    public XPathAssertionGui() {
        super();
        init();
    }

    /**
     * Returns the label to be shown within the JTree-Component.
     */
    @Override
    public String getLabelResource() {
        return "xpath_assertion_title"; //$NON-NLS-1$
    }

    /**
     * Create test element
     */
    @Override
    public TestElement createTestElement() {
        XPathAssertion el = new XPathAssertion();
        modifyTestElement(el);
        return el;
    }

    public String getXPathAttributesTitle() {
        return JMeterUtils.getResString("xpath_assertion_test"); //$NON-NLS-1$
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        XPathAssertion assertion = (XPathAssertion) el;
        showScopeSettings(assertion, true);
        xpath.setXPath(assertion.getXPathString());
        xpath.setNegated(assertion.isNegated());

        xml.configure(assertion);
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        Box topBox = Box.createVerticalBox();

        topBox.add(makeTitlePanel());

        topBox.add(createScopePanel(true));

        xml = new XMLConfPanel();
        xml.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("xpath_assertion_option"))); //$NON-NLS-1$
        topBox.add(xml);

        add(topBox, BorderLayout.NORTH);

        // USER_INPUT
        JPanel sizePanel = new JPanel(new BorderLayout());
        sizePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        sizePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                getXPathAttributesTitle()));
        xpath = new XPathPanel();
        sizePanel.add(xpath);
        add(sizePanel, BorderLayout.CENTER);
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement el) {
        super.configureTestElement(el);
        if (el instanceof XPathAssertion) {
            XPathAssertion assertion = (XPathAssertion) el;
            saveScopeSettings(assertion);
            assertion.setNegated(xpath.isNegated());
            assertion.setXPathString(xpath.getXPath());
            xml.modifyTestElement(assertion);
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        xpath.setXPath("/"); //$NON-NLS-1$
        xpath.setNegated(false);

        xml.setDefaultValues();

    }
}
