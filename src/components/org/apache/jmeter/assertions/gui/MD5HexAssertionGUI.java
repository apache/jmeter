/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/**
 * GUI class supporting the MD5Hex assertion functionality.
 * 
 * @version $Revision$ updated on $Date$
 * 
 * @author	<a href="mailto:jh@domek.be">Jorg Heymans</a>
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

    private JTextField md5HexInput;

    public MD5HexAssertionGUI() {
        init();
    }

    private void init() {

        setLayout(new BorderLayout(0, 10));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // USER_INPUT
        HorizontalPanel md5HexPanel = new HorizontalPanel();
        md5HexPanel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("md5hex_assertion_md5hex_test")));

        md5HexPanel.add(
            new JLabel(JMeterUtils.getResString("md5hex_assertion_label")));

        md5HexInput = new JTextField(25);
        //        md5HexInput.addFocusListener(this);
        md5HexPanel.add(md5HexInput);

        mainPanel.add(md5HexPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

    }

    public void configure(TestElement el) {
        super.configure(el);
        MD5HexAssertion assertion = (MD5HexAssertion)el;
        this.md5HexInput.setText(String.valueOf(assertion.getAllowedMD5Hex()));
    }

    /* 
     * @return
     */
    public String getStaticLabel() {
        return JMeterUtils.getResString("md5hex_assertion_title");
    }

    /* 
     * @return
     */
    public TestElement createTestElement() {

        MD5HexAssertion el = new MD5HexAssertion();
        modifyTestElement(el);
        return el;

    }

    /* 
     * @param element
     */
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        String md5HexString = this.md5HexInput.getText();
        //initialize to empty string, this will fail the assertion
        if (md5HexString == null || md5HexString.length() == 0) {
            md5HexString = "";
        }
        ((MD5HexAssertion)element).setAllowedMD5Hex(md5HexString);
    }
}
