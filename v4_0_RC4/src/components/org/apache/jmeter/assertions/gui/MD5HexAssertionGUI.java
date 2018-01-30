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

/**
 * GUI class supporting the MD5Hex assertion functionality.
 *
 */
package org.apache.jmeter.assertions.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.assertions.MD5HexAssertion;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

public class MD5HexAssertionGUI extends AbstractAssertionGui {

    private static final long serialVersionUID = 240L;

    private JTextField md5HexInput;

    public MD5HexAssertionGUI() {
        init();
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)

        setLayout(new BorderLayout(0, 10));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // USER_INPUT
        HorizontalPanel md5HexPanel = new HorizontalPanel();
        md5HexPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("md5hex_assertion_md5hex_test"))); // $NON-NLS-1$

        md5HexPanel.add(new JLabel(JMeterUtils.getResString("md5hex_assertion_label"))); //$NON-NLS-1$

        md5HexInput = new JTextField(25);
        md5HexPanel.add(md5HexInput);

        mainPanel.add(md5HexPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        MD5HexAssertion assertion = (MD5HexAssertion) el;
        this.md5HexInput.setText(String.valueOf(assertion.getAllowedMD5Hex()));
    }

    @Override
    public String getLabelResource() {
        return "md5hex_assertion_title"; // $NON-NLS-1$
    }

    /*
     * @return
     */
    @Override
    public TestElement createTestElement() {

        MD5HexAssertion el = new MD5HexAssertion();
        modifyTestElement(el);
        return el;

    }

    /*
     * @param element
     */
    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        String md5HexString = this.md5HexInput.getText();
        // initialize to empty string, this will fail the assertion
        if (md5HexString == null || md5HexString.length() == 0) {
            md5HexString = "";
        }
        ((MD5HexAssertion) element).setAllowedMD5Hex(md5HexString);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        md5HexInput.setText(""); //$NON-NLS-1$
    }
}
