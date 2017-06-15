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

package org.apache.jmeter.protocol.ldap.config.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JPasswordField;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.ldap.sampler.LDAPExtSampler;
import org.apache.jmeter.protocol.ldap.config.gui.LDAPArgumentsPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledChoice;

/*******************************************************************************
 * This class LdapConfigGui is user interface gui for getting all the
 * configuration value from the user
 *
 * author Dolf Smits(Dolf.Smits@Siemens.com) created Aug 09 2003 11:00 AM
 * company Siemens Netherlands N.V..
 *
 * Based on the work of: author T.Elanjchezhiyan(chezhiyan@siptech.co.in)
 * created Apr 29 2003 11:00 AM company Sip Technologies and Exports Ltd.
 ******************************************************************************/
public class LdapExtConfigGui extends AbstractConfigGui implements ItemListener {

    private static final long serialVersionUID = 240L;

    private JTextField rootdn = new JTextField(20);

    private JTextField searchbase = new JTextField(20);

    private JTextField searchfilter = new JTextField(20);

    private JTextField delete = new JTextField(20);

    private JTextField add = new JTextField(20);

    private JTextField modify = new JTextField(20);

    private JTextField servername = new JTextField(20);

    private JTextField port = new JTextField(20);

    /*
     * N.B. These entry indexes MUST agree with the SearchControls SCOPE_LEVELS, i.e.
     * 
     * javax.naming.directory.SearchControls.OBJECT_SCOPE, ONELEVEL_SCOPE, SUBTREE_SCOPE
     * 
     * These have the values 0,1,2 so can be used as indexes in the array
     * as well as the value for the search itself.
     * 
     * N.B. Although the strings are used to set and get the options, language change
     * does not currently cause a problem, because that always saves the current settings first,
     * and then recreates all the GUI classes.
     */
    private final String[] SCOPE_STRINGS = new String[]{
        JMeterUtils.getResString("ldap_search_baseobject"),// $NON-NLS-1$
        JMeterUtils.getResString("ldap_search_onelevel"),// $NON-NLS-1$
        JMeterUtils.getResString("ldap_search_subtree"),// $NON-NLS-1$
        };

    // Names for the cards
    private static final String CARDS_DEFAULT = ""; // $NON-NLS-1$
    private static final String CARDS_ADD = "Add"; // $NON-NLS-1$
    private static final String CARDS_DELETE = "Delete"; // $NON-NLS-1$
    private static final String CARDS_BIND = "Bind"; // $NON-NLS-1$
    private static final String CARDS_RENAME = "Rename"; // $NON-NLS-1$
    private static final String CARDS_COMPARE = "Compare"; // $NON-NLS-1$
    private static final String CARDS_SEARCH = "Search"; // $NON-NLS-1$
    private static final String CARDS_MODIFY = "Modify"; // $NON-NLS-1$

    private JLabeledChoice scope =
        new JLabeledChoice(JMeterUtils.getResString("scope"), // $NON-NLS-1$
        SCOPE_STRINGS);

    private JTextField countlim = new JTextField(20);

    private JTextField timelim = new JTextField(20);

    private JTextField attribs = new JTextField(20);

    private JCheckBox retobj = new JCheckBox(JMeterUtils.getResString("retobj")); // $NON-NLS-1$

    private JCheckBox deref = new JCheckBox(JMeterUtils.getResString("deref")); // $NON-NLS-1$

    private JTextField userdn = new JTextField(20);

    private JTextField userpw = new JPasswordField(20);

    private JTextField comparedn = new JTextField(20);

    private JTextField comparefilt = new JTextField(20);

    private JTextField modddn = new JTextField(20);

    private JTextField newdn = new JTextField(20);

    private JTextField connto = new JTextField(20);

    private JCheckBox parseflag = new JCheckBox(JMeterUtils.getResString("ldap_parse_results")); // $NON-NLS-1$

    private JCheckBox secure = new JCheckBox(JMeterUtils.getResString("ldap_secure")); // $NON-NLS-1$

    private JRadioButton addTest = new JRadioButton(JMeterUtils.getResString("addtest")); // $NON-NLS-1$

    private JRadioButton modifyTest = new JRadioButton(JMeterUtils.getResString("modtest")); // $NON-NLS-1$

    private JRadioButton deleteTest = new JRadioButton(JMeterUtils.getResString("deltest")); // $NON-NLS-1$

    private JRadioButton searchTest = new JRadioButton(JMeterUtils.getResString("searchtest")); // $NON-NLS-1$

    private JRadioButton bind = new JRadioButton(JMeterUtils.getResString("bind")); // $NON-NLS-1$

    private JRadioButton rename = new JRadioButton(JMeterUtils.getResString("rename")); // $NON-NLS-1$

    private JRadioButton unbind = new JRadioButton(JMeterUtils.getResString("unbind")); // $NON-NLS-1$

    private JRadioButton sbind = new JRadioButton(JMeterUtils.getResString("sbind")); // $NON-NLS-1$

    private JRadioButton compare = new JRadioButton(JMeterUtils.getResString("compare")); // $NON-NLS-1$

    private ButtonGroup bGroup = new ButtonGroup();

    private boolean displayName = true;

    private ArgumentsPanel tableAddPanel = new ArgumentsPanel(JMeterUtils.getResString("addtest")); // $NON-NLS-1$

    private LDAPArgumentsPanel tableModifyPanel = new LDAPArgumentsPanel(JMeterUtils.getResString("modtest")); // $NON-NLS-1$

    private JPanel cards;

    /***************************************************************************
     * Default constructor for LdapConfigGui
     **************************************************************************/
    public LdapExtConfigGui() {
        this(true);
    }

    /***************************************************************************
     * Constructor for LdapExtConfigGui
     * 
     * @param displayName
     *            flag, whether to show the display name of the component.
     **************************************************************************/
    public LdapExtConfigGui(boolean displayName) {
        this.displayName = displayName;
        init();
    }

    @Override
    public String getLabelResource() {
        return "ldapext_sample_title"; // $NON-NLS-1$
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
        servername.setText(element.getPropertyAsString(LDAPExtSampler.SERVERNAME));
        port.setText(element.getPropertyAsString(LDAPExtSampler.PORT));
        rootdn.setText(element.getPropertyAsString(LDAPExtSampler.ROOTDN));
        scope.setSelectedIndex(element.getPropertyAsInt(LDAPExtSampler.SCOPE));
        countlim.setText(element.getPropertyAsString(LDAPExtSampler.COUNTLIM));
        timelim.setText(element.getPropertyAsString(LDAPExtSampler.TIMELIM));
        attribs.setText(element.getPropertyAsString(LDAPExtSampler.ATTRIBS));
        retobj.setSelected(element.getPropertyAsBoolean(LDAPExtSampler.RETOBJ));
        deref.setSelected(element.getPropertyAsBoolean(LDAPExtSampler.DEREF));
        connto.setText(element.getPropertyAsString(LDAPExtSampler.CONNTO));
        parseflag.setSelected(element.getPropertyAsBoolean(LDAPExtSampler.PARSEFLAG));
        secure.setSelected(element.getPropertyAsBoolean(LDAPExtSampler.SECURE));
        userpw.setText(element.getPropertyAsString(LDAPExtSampler.USERPW));
        userdn.setText(element.getPropertyAsString(LDAPExtSampler.USERDN));
        comparedn.setText(element.getPropertyAsString(LDAPExtSampler.COMPAREDN));
        comparefilt.setText(element.getPropertyAsString(LDAPExtSampler.COMPAREFILT));
        modddn.setText(element.getPropertyAsString(LDAPExtSampler.MODDDN));
        newdn.setText(element.getPropertyAsString(LDAPExtSampler.NEWDN));
        CardLayout cl = (CardLayout) (cards.getLayout());
        final String testType = element.getPropertyAsString(LDAPExtSampler.TEST);
        if (testType.equals(LDAPExtSampler.UNBIND)) {
            unbind.setSelected(true);
            cl.show(cards, CARDS_DEFAULT);
        } else if (testType.equals(LDAPExtSampler.BIND)) {
            bind.setSelected(true);
            cl.show(cards, CARDS_BIND);
        } else if (testType.equals(LDAPExtSampler.SBIND)) {
            sbind.setSelected(true);
            cl.show(cards, CARDS_BIND);
        } else if (testType.equals(LDAPExtSampler.COMPARE)) {
            compare.setSelected(true);
            cl.show(cards, CARDS_COMPARE);
        } else if (testType.equals(LDAPExtSampler.ADD)) {
            addTest.setSelected(true);
            add.setText(element.getPropertyAsString(LDAPExtSampler.BASE_ENTRY_DN));
            tableAddPanel.configure((TestElement) element.getProperty(LDAPExtSampler.ARGUMENTS).getObjectValue());
            cl.show(cards, CARDS_ADD);
        } else if (testType.equals(LDAPExtSampler.MODIFY)) {
            modifyTest.setSelected(true);
            modify.setText(element.getPropertyAsString(LDAPExtSampler.BASE_ENTRY_DN));
            tableModifyPanel
                    .configure((TestElement) element.getProperty(LDAPExtSampler.LDAPARGUMENTS).getObjectValue());
            cl.show(cards, CARDS_MODIFY);
        } else if (testType.equals(LDAPExtSampler.DELETE)) {
            deleteTest.setSelected(true);
            delete.setText(element.getPropertyAsString(LDAPExtSampler.DELETE));
            cl.show(cards, CARDS_DELETE);
        } else if (testType.equals(LDAPExtSampler.RENAME)) {
            rename.setSelected(true);
            cl.show(cards, CARDS_RENAME);
        } else if (testType.equals(LDAPExtSampler.SEARCH)) {
            searchTest.setSelected(true);
            searchbase.setText(element.getPropertyAsString(LDAPExtSampler.SEARCHBASE));
            searchfilter.setText(element.getPropertyAsString(LDAPExtSampler.SEARCHFILTER));
            cl.show(cards, CARDS_SEARCH);
        }
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        ConfigTestElement element = new ConfigTestElement();
        modifyTestElement(element);
        return element;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement element) {
        element.clear();
        configureTestElement(element);
        element.setProperty(LDAPExtSampler.SERVERNAME, servername.getText());
        element.setProperty(LDAPExtSampler.PORT, port.getText());
        element.setProperty(LDAPExtSampler.ROOTDN, rootdn.getText());
        element.setProperty(LDAPExtSampler.SCOPE,String.valueOf(scope.getSelectedIndex()));
        element.setProperty(LDAPExtSampler.COUNTLIM, countlim.getText());
        element.setProperty(LDAPExtSampler.TIMELIM, timelim.getText());
        element.setProperty(LDAPExtSampler.ATTRIBS, attribs.getText());
        element.setProperty(LDAPExtSampler.RETOBJ,Boolean.toString(retobj.isSelected()));
        element.setProperty(LDAPExtSampler.DEREF,Boolean.toString(deref.isSelected()));
        element.setProperty(LDAPExtSampler.CONNTO, connto.getText());
        element.setProperty(LDAPExtSampler.PARSEFLAG,Boolean.toString(parseflag.isSelected()));
        element.setProperty(LDAPExtSampler.SECURE,Boolean.toString(secure.isSelected()));
        element.setProperty(LDAPExtSampler.USERDN, userdn.getText());
        element.setProperty(LDAPExtSampler.USERPW, userpw.getText());
        element.setProperty(LDAPExtSampler.COMPAREDN, comparedn.getText());
        element.setProperty(LDAPExtSampler.COMPAREFILT, comparefilt.getText());
        element.setProperty(LDAPExtSampler.MODDDN, modddn.getText());
        element.setProperty(LDAPExtSampler.NEWDN, newdn.getText());
        if (addTest.isSelected()) {
            element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.ADD));
            element.setProperty(new StringProperty(LDAPExtSampler.BASE_ENTRY_DN, add.getText()));
            element.setProperty(new TestElementProperty(LDAPExtSampler.ARGUMENTS, tableAddPanel.createTestElement()));
        }
        if (modifyTest.isSelected()) {
            element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.MODIFY));
            element.setProperty(new StringProperty(LDAPExtSampler.BASE_ENTRY_DN, modify.getText()));
            element.setProperty(new TestElementProperty(LDAPExtSampler.LDAPARGUMENTS, tableModifyPanel
                    .createTestElement()));
        }
        if (deleteTest.isSelected()) {
            element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.DELETE));
            element.setProperty(new StringProperty(LDAPExtSampler.DELETE, delete.getText()));
        }
        if (searchTest.isSelected()) {
            element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.SEARCH));
            element.setProperty(new StringProperty(LDAPExtSampler.SEARCHBASE, searchbase.getText()));
            element.setProperty(new StringProperty(LDAPExtSampler.SEARCHFILTER, searchfilter.getText()));
        }
        if (bind.isSelected()) {
            element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.BIND));
        }
        if (sbind.isSelected()) {
            element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.SBIND));
        }
        if (compare.isSelected()) {
            element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.COMPARE));
        }
        if (rename.isSelected()) {
            element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.RENAME));
        }
        if (unbind.isSelected()) {
            element.setProperty(new StringProperty(LDAPExtSampler.TEST, LDAPExtSampler.UNBIND));
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        rootdn.setText(""); //$NON-NLS-1$
        searchbase.setText(""); //$NON-NLS-1$
        searchfilter.setText(""); //$NON-NLS-1$
        delete.setText(""); //$NON-NLS-1$
        add.setText(""); //$NON-NLS-1$
        modify.setText(""); //$NON-NLS-1$
        servername.setText(""); //$NON-NLS-1$
        port.setText(""); //$NON-NLS-1$
        add.setText(""); //$NON-NLS-1$
        scope.setSelectedIndex(SCOPE_STRINGS.length - 1);
        countlim.setText(""); //$NON-NLS-1$
        timelim.setText(""); //$NON-NLS-1$
        attribs.setText(""); //$NON-NLS-1$
        userdn.setText(""); //$NON-NLS-1$
        userpw.setText(""); //$NON-NLS-1$
        comparedn.setText(""); //$NON-NLS-1$
        comparefilt.setText(""); //$NON-NLS-1$
        modddn.setText(""); //$NON-NLS-1$
        newdn.setText(""); //$NON-NLS-1$
        connto.setText(""); //$NON-NLS-1$
        retobj.setSelected(false);
        deref.setSelected(false);
        parseflag.setSelected(false);
        secure.setSelected(false);
        addTest.setSelected(false);
        modifyTest.setSelected(false);
        deleteTest.setSelected(false);
        searchTest.setSelected(false);
        bind.setSelected(false);
        rename.setSelected(false);
        unbind.setSelected(false);
        sbind.setSelected(false);
        compare.setSelected(false);

        tableAddPanel.clear();
        tableModifyPanel.clear();
    }

    /***************************************************************************
     * This itemStateChanged listener for changing the card layout for based on
     * the test selected in the User defined test case.
     **************************************************************************/
    @Override
    public void itemStateChanged(ItemEvent ie) {
        CardLayout cl = (CardLayout) (cards.getLayout());
        if (addTest.isSelected()) {
            cl.show(cards, CARDS_ADD);
        } else if (deleteTest.isSelected()) {
            cl.show(cards, CARDS_DELETE);
        } else if (bind.isSelected()) {
            cl.show(cards, CARDS_BIND);
        } else if (sbind.isSelected()) {
            cl.show(cards, CARDS_BIND);
        } else if (rename.isSelected()) {
            cl.show(cards, CARDS_RENAME);
        } else if (compare.isSelected()) {
            cl.show(cards, CARDS_COMPARE);
        } else if (searchTest.isSelected()) {
            cl.show(cards, CARDS_SEARCH);
        } else if (modifyTest.isSelected()) {
            cl.show(cards, CARDS_MODIFY);
        } else { // e.g unbind
            cl.show(cards, CARDS_DEFAULT);
        }
    }

    /***************************************************************************
     * This will create the bind/sbind panel in the LdapConfigGui
     **************************************************************************/
    private JPanel createBindPanel() {
        VerticalPanel bindPanel = new VerticalPanel();

        bindPanel.add(createLabelPanel("servername", servername));
        bindPanel.add(createLabelPanel("port", port));
        bindPanel.add(createLabelPanel("ddn", rootdn));
        bindPanel.add(createLabelPanel("userdn", userdn));
        bindPanel.add(createLabelPanel("userpw", userpw));
        bindPanel.add(createLabelPanel("ldap_connto", connto));

        bindPanel.add(secure);
        return bindPanel;
    }

    /***************************************************************************
     * This will create the bind panel in the LdapConfigGui
     **************************************************************************/
    private JPanel createComparePanel() {
        VerticalPanel cbindPanel = new VerticalPanel();

        cbindPanel.add(createLabelPanel("entrydn", comparedn));
        cbindPanel.add(createLabelPanel("comparefilt", comparefilt));

        return cbindPanel;
    }

    /***************************************************************************
     * This will create the Search controls panel in the LdapConfigGui
     **************************************************************************/
    private JPanel createSCPanel() {
        VerticalPanel scPanel = new VerticalPanel();

        scPanel.add(scope);

        scPanel.add(createLabelPanel("countlim", countlim));
        scPanel.add(createLabelPanel("timelim", timelim));
        scPanel.add(createLabelPanel("attrs", attribs));

        scPanel.add(retobj);
        scPanel.add(deref);
        scPanel.add(parseflag);

        return scPanel;
    }

    /***************************************************************************
     * This will create the Search panel in the LdapConfigGui
     **************************************************************************/
    private JPanel createSearchPanel() {
        VerticalPanel searchPanel = new VerticalPanel();

        searchPanel.add(createLabelPanel("searchbase", searchbase));
        searchPanel.add(createLabelPanel("searchfilter", searchfilter));
        searchPanel.add(createSCPanel());

        return searchPanel;
    }

    /***************************************************************************
     * This will create the Moddn panel in the LdapConfigGui
     **************************************************************************/
    private JPanel createModdnPanel() {
        VerticalPanel modPanel = new VerticalPanel();
        modPanel.add(createLabelPanel("modddn", modddn));
        modPanel.add(createLabelPanel("newdn", newdn));
        return modPanel;
    }

    /**
     * Create a panel with a label and a text field
     * 
     * @param key
     *            to look up the label with
     *            {@link JMeterUtils#getResString(String)}
     * @param field
     *            field to show and attach the label
     * @return newly constructed panel
     */
    private JPanel createLabelPanel(String key, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString(key)); // $NON-NLS-1$
        label.setLabelFor(field);
        panel.add(label, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    /***************************************************************************
     * This will create the Delete panel in the LdapConfigGui
     **************************************************************************/
    private JPanel createDeletePanel() {
        VerticalPanel panel = new VerticalPanel();
        panel.add(createLabelPanel("delete", delete));
        return panel;
    }

    /***************************************************************************
     * This will create the Add test panel in the LdapConfigGui
     **************************************************************************/
    private JPanel createAddPanel() {
        JPanel addPanel = new JPanel(new BorderLayout(5, 0));
        addPanel.add(createLabelPanel("entrydn", add), BorderLayout.NORTH);
        addPanel.add(tableAddPanel, BorderLayout.CENTER);
        return addPanel;
    }

    /***************************************************************************
     * This will create the Modify panel in the LdapConfigGui
     **************************************************************************/
    private JPanel createModifyPanel() {
        JPanel modifyPanel = new JPanel(new BorderLayout(5, 0));
        modifyPanel.add(createLabelPanel("entrydn", modify), BorderLayout.NORTH);
        modifyPanel.add(tableModifyPanel, BorderLayout.CENTER);
        return modifyPanel;
    }

    /***************************************************************************
     * This will create the user defined test panel for create or modify or
     * delete or search based on the panel selected in the itemevent in the
     * LdapConfigGui
     **************************************************************************/
    private JPanel testPanel() {
        cards = new JPanel(new CardLayout());
        cards.add(new JPanel(),         CARDS_DEFAULT);
        cards.add(createAddPanel(),     CARDS_ADD);
        cards.add(createModifyPanel(),  CARDS_MODIFY);
        cards.add(createModdnPanel(),   CARDS_RENAME);
        cards.add(createDeletePanel(),  CARDS_DELETE);
        cards.add(createSearchPanel(),  CARDS_SEARCH);
        cards.add(createBindPanel(),    CARDS_BIND);
        cards.add(createComparePanel(), CARDS_COMPARE);
        return cards;
    }

    /***************************************************************************
     * This will create the test panel in the LdapConfigGui
     **************************************************************************/
    private JPanel createTestPanel() {
        JPanel testPanel = new JPanel(new BorderLayout());
        testPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("test_configuration"))); // $NON-NLS-1$

        testPanel.add(new JLabel(JMeterUtils.getResString("testt"))); // $NON-NLS-1$
        JPanel rowPanel = new JPanel();
        JPanel row2Panel = new JPanel();

        rowPanel.add(bind);
        bGroup.add(bind);
        rowPanel.add(unbind);
        bGroup.add(unbind);
        rowPanel.add(sbind);
        bGroup.add(sbind);
        rowPanel.add(rename);
        bGroup.add(rename);
        row2Panel.add(addTest);
        bGroup.add(addTest);
        row2Panel.add(deleteTest);
        bGroup.add(deleteTest);
        row2Panel.add(searchTest);
        bGroup.add(searchTest);
        row2Panel.add(compare);
        bGroup.add(compare);
        row2Panel.add(modifyTest);
        bGroup.add(modifyTest);
        testPanel.add(rowPanel, BorderLayout.NORTH);
        testPanel.add(row2Panel, BorderLayout.SOUTH);
        return testPanel;
    }

    /***************************************************************************
     * This will initialise all the panels in the LdapConfigGui
     **************************************************************************/
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));
        if (displayName) {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(createTestPanel());
        mainPanel.add(testPanel());
        add(mainPanel, BorderLayout.CENTER);
        // Take note of when buttons are changed so we can change panel
        bind.addItemListener(this);
        sbind.addItemListener(this);
        unbind.addItemListener(this);
        compare.addItemListener(this);
        addTest.addItemListener(this);
        modifyTest.addItemListener(this);
        rename.addItemListener(this);
        deleteTest.addItemListener(this);
        searchTest.addItemListener(this);
    }
}
