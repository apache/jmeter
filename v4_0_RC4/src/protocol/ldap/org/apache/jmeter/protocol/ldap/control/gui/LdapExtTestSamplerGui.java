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

package org.apache.jmeter.protocol.ldap.control.gui;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.apache.jmeter.protocol.ldap.config.gui.LdapExtConfigGui;
import org.apache.jmeter.protocol.ldap.sampler.LDAPExtSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

/*******************************************************************************
 *
 * author Dolf Smits(Dolf.Smits@Siemens.com) created Aug 09 2003 11:00 AM
 * company Siemens Netherlands N.V..
 *
 * Based on the work of: author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
 * created Apr 29 2003 11:00 AM company Sip Technologies and Exports Ltd.
 *
 ******************************************************************************/
public class LdapExtTestSamplerGui extends AbstractSamplerGui {
    private static final long serialVersionUID = 240L;

    private LdapExtConfigGui ldapDefaultPanel;

    /***************************************************************************
     * Constructor that initialises the GUI components
     **************************************************************************/
    public LdapExtTestSamplerGui() {
        init();
    }

    /**
     * A newly created component can be initialized with the contents of a Test
     * Element object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param element
     *            the TestElement to configure
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        ldapDefaultPanel.configure(element);
    }

    @Override
    public TestElement createTestElement() {
        LDAPExtSampler sampler = new LDAPExtSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement sampler) {
        sampler.clear();
        sampler.addTestElement(ldapDefaultPanel.createTestElement());
        super.configureTestElement(sampler);
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        ldapDefaultPanel.clearGui();
    }

    @Override
    public String getLabelResource() {
        return "ldapext_testing_title"; // $NON-NLS-1$
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);
        // MAIN PANEL
        JPanel mainPanel = new JPanel(new BorderLayout(0, 5));
        ldapDefaultPanel = new LdapExtConfigGui(false);
        mainPanel.add(ldapDefaultPanel);
        add(mainPanel, BorderLayout.CENTER);
    }
}
