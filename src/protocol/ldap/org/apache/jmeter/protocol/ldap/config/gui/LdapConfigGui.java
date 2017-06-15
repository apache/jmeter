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

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.ldap.sampler.LDAPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;

/**
 * This class LdapConfigGui is user interface gui for getting all the
 * configuration values from the user.
 *
 * Created Apr 29 2003 11:45 AM
 *
 */
public class LdapConfigGui extends AbstractConfigGui implements ItemListener {

    private static final long serialVersionUID = 241L;

    private JTextField rootdn = new JTextField(20);

    private JTextField searchbase = new JTextField(20);

    private JTextField searchfilter = new JTextField(20);

    private JTextField delete = new JTextField(20);

    private JTextField add = new JTextField(20);

    private JTextField modify = new JTextField(20);

    private JTextField servername = new JTextField(20);

    private JTextField port = new JTextField(20);

    private JCheckBox userDefined = new JCheckBox(JMeterUtils.getResString("user_defined_test")); // $NON-NLS-1$

    private JRadioButton addTest = new JRadioButton(JMeterUtils.getResString("add_test")); // $NON-NLS-1$

    private JRadioButton modifyTest = new JRadioButton(JMeterUtils.getResString("modify_test")); // $NON-NLS-1$

    private JRadioButton deleteTest = new JRadioButton(JMeterUtils.getResString("delete_test")); // $NON-NLS-1$

    private JRadioButton searchTest = new JRadioButton(JMeterUtils.getResString("search_test")); // $NON-NLS-1$

    private ButtonGroup bGroup = new ButtonGroup();

    private boolean displayName = true;

    private ArgumentsPanel tableAddPanel = new ArgumentsPanel(JMeterUtils.getResString("add_test")); // $NON-NLS-1$

    private ArgumentsPanel tableModifyPanel = new ArgumentsPanel(JMeterUtils.getResString("modify_test")); // $NON-NLS-1$

    private JPanel cards;

    /**
     * Default constructor for LdapConfigGui.
     */
    public LdapConfigGui() {
        this(true);
    }

    /**
     * Constructor which sets the displayName.
     *
     * @param displayName flag, whether to display the name of the component
     */
    public LdapConfigGui(boolean displayName) {
        this.displayName = displayName;
        init();
    }

    @Override
    public String getLabelResource() {
        return "ldap_sample_title"; // $NON-NLS-1$
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
        servername.setText(element.getPropertyAsString(LDAPSampler.SERVERNAME));
        port.setText(element.getPropertyAsString(LDAPSampler.PORT));
        rootdn.setText(element.getPropertyAsString(LDAPSampler.ROOTDN));
        CardLayout cl = (CardLayout) (cards.getLayout());
        final String testType = element.getPropertyAsString(LDAPSampler.TEST);
        if (testType.equals(LDAPSampler.ADD)) {
            addTest.setSelected(true);
            add.setText(element.getPropertyAsString(LDAPSampler.BASE_ENTRY_DN));
            tableAddPanel.configure((TestElement) element.getProperty(LDAPSampler.ARGUMENTS).getObjectValue());
            cl.show(cards, "Add");
        } else if (testType.equals(LDAPSampler.MODIFY)) {
            modifyTest.setSelected(true);
            modify.setText(element.getPropertyAsString(LDAPSampler.BASE_ENTRY_DN));
            tableModifyPanel.configure((TestElement) element.getProperty(LDAPSampler.ARGUMENTS).getObjectValue());
            cl.show(cards, "Modify");
        } else if (testType.equals(LDAPSampler.DELETE)) {
            deleteTest.setSelected(true);
            delete.setText(element.getPropertyAsString(LDAPSampler.DELETE));
            cl.show(cards, "Delete");
        } else if (testType.equals(LDAPSampler.SEARCHBASE)) {
            searchTest.setSelected(true);
            searchbase.setText(element.getPropertyAsString(LDAPSampler.SEARCHBASE));
            searchfilter.setText(element.getPropertyAsString(LDAPSampler.SEARCHFILTER));
            cl.show(cards, "Search");
        }

        if (element.getPropertyAsBoolean(LDAPSampler.USER_DEFINED)) {
            userDefined.setSelected(true);
        } else {
            userDefined.setSelected(false);
            cl.show(cards, ""); // $NON-NLS-1$
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
        element.setProperty(LDAPSampler.SERVERNAME, servername.getText());
        element.setProperty(LDAPSampler.PORT, port.getText());
        element.setProperty(LDAPSampler.ROOTDN, rootdn.getText());
        element.setProperty(new BooleanProperty(LDAPSampler.USER_DEFINED, userDefined.isSelected()));

        if (addTest.isSelected()) {
            element.setProperty(new StringProperty(LDAPSampler.TEST, LDAPSampler.ADD));
            element.setProperty(new StringProperty(LDAPSampler.BASE_ENTRY_DN, add.getText()));
            element.setProperty(new TestElementProperty(LDAPSampler.ARGUMENTS, tableAddPanel.createTestElement()));
        }

        if (modifyTest.isSelected()) {
            element.setProperty(new StringProperty(LDAPSampler.TEST, LDAPSampler.MODIFY));
            element.setProperty(new StringProperty(LDAPSampler.BASE_ENTRY_DN, modify.getText()));
            element.setProperty(new TestElementProperty(LDAPSampler.ARGUMENTS, tableModifyPanel.createTestElement()));
        }

        if (deleteTest.isSelected()) {
            element.setProperty(new StringProperty(LDAPSampler.TEST, LDAPSampler.DELETE));
            element.setProperty(new StringProperty(LDAPSampler.DELETE, delete.getText()));
        }

        if (searchTest.isSelected()) {
            element.setProperty(new StringProperty(LDAPSampler.TEST, LDAPSampler.SEARCHBASE));
            element.setProperty(new StringProperty(LDAPSampler.SEARCHBASE, searchbase.getText()));
            element.setProperty(new StringProperty(LDAPSampler.SEARCHFILTER, searchfilter.getText()));
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
        userDefined.setSelected(false);
        addTest.setSelected(true);
        modifyTest.setSelected(false);
        deleteTest.setSelected(false);
        searchTest.setSelected(false);
    }

    /**
     * This itemStateChanged listener for changing the card layout for based on
     * the test selected in the User defined test case.
     */
    @Override
    public void itemStateChanged(ItemEvent ie) {
        CardLayout cl = (CardLayout) (cards.getLayout());
        if (userDefined.isSelected()) {
            if (addTest.isSelected()) {
                cl.show(cards, "Add");
                tableModifyPanel.clear();
                modify.setText(""); // $NON-NLS-1$
                searchbase.setText(""); // $NON-NLS-1$
                searchfilter.setText(""); // $NON-NLS-1$
                delete.setText("");
            } else if (deleteTest.isSelected()) {
                cl.show(cards, "Delete");
                tableModifyPanel.clear();
                modify.setText(""); // $NON-NLS-1$
                tableAddPanel.clear();
                add.setText(""); // $NON-NLS-1$
                searchbase.setText(""); // $NON-NLS-1$
                searchfilter.setText(""); // $NON-NLS-1$
            } else if (searchTest.isSelected()) {
                cl.show(cards, "Search");
                delete.setText(""); // $NON-NLS-1$
                tableModifyPanel.clear();
                modify.setText(""); // $NON-NLS-1$
                tableAddPanel.clear();
                add.setText(""); // $NON-NLS-1$
            } else if (modifyTest.isSelected()) {
                cl.show(cards, "Modify");
                tableAddPanel.clear();
                add.setText(""); // $NON-NLS-1$
                searchbase.setText(""); // $NON-NLS-1$
                searchfilter.setText(""); // $NON-NLS-1$
                delete.setText("");
            } else {
                resetCardLayout(cl);
            }
        } else {
            resetCardLayout(cl);
        }
    }

    private void resetCardLayout(CardLayout cl) {
        cl.show(cards, ""); // $NON-NLS-1$
        tableAddPanel.clear();
        add.setText(""); // $NON-NLS-1$
        tableModifyPanel.clear();
        modify.setText(""); // $NON-NLS-1$
        searchbase.setText(""); // $NON-NLS-1$
        searchfilter.setText(""); // $NON-NLS-1$
        delete.setText(""); // $NON-NLS-1$
    }

    /**
     * This will create the servername panel in the LdapConfigGui.
     */
    private JPanel createServernamePanel() {
        return createLabelPanel("servername", servername);
    }

    /**
     * This will create the port panel in the LdapConfigGui.
     */
    private JPanel createPortPanel() {
        return createLabelPanel("port", port);
    }

    /**
     * This will create the Root distinguished name panel in the LdapConfigGui.
     */
    private JPanel createRootdnPanel() {
        return createLabelPanel("dn", rootdn);
    }

    /**
     * This will create the Search panel in the LdapConfigGui.
     */
    private JPanel createSearchPanel() {
        VerticalPanel searchPanel = new VerticalPanel();
        searchPanel.add(createLabelPanel("search_base", searchbase));
        searchPanel.add(createLabelPanel("search_filter", searchfilter));
        return searchPanel;
    }

    /**
     * This will create the Delete panel in the LdapConfigGui.
     */
    private JPanel createDeletePanel() {
        VerticalPanel panel = new VerticalPanel();
        panel.add(createLabelPanel("delete", delete));
        return panel;
    }

    /**
     * This will create the Add test panel in the LdapConfigGui.
     */
    private JPanel createAddPanel() {
        JPanel addPanel = new JPanel(new BorderLayout(5, 0));
        addPanel.add(createLabelPanel("entry_dn", add), BorderLayout.NORTH);
        addPanel.add(tableAddPanel, BorderLayout.CENTER);
        return addPanel;
    }

    /**
     * Create a panel with the text field and a label
     * 
     * @param key
     *            to look up the label by using
     *            {@link JMeterUtils#getResString(String)}
     * @param field
     *            text field to display
     * @return newly created panel
     */
    private JPanel createLabelPanel(String key, JTextField field) {
        JPanel addInnerPanel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString(key)); // $NON-NLS-1$
        label.setLabelFor(field);
        addInnerPanel.add(label, BorderLayout.WEST);
        addInnerPanel.add(field, BorderLayout.CENTER);
        return addInnerPanel;
    }

    /**
     * This will create the Modify panel in the LdapConfigGui.
     */
    private JPanel createModifyPanel() {
        JPanel modifyPanel = new JPanel(new BorderLayout(5, 0));
        modifyPanel.add(createLabelPanel("entry_dn", modify), BorderLayout.NORTH);
        modifyPanel.add(tableModifyPanel, BorderLayout.CENTER);
        return modifyPanel;
    }

    /**
     * This will create the user defined test panel for create or modify or
     * delete or search based on the panel selected in the itemevent in the
     * LdapConfigGui.
     */
    private JPanel testPanel() {
        cards = new JPanel(new CardLayout());
        cards.add(new JPanel(), "");
        cards.add(createAddPanel(), "Add");
        cards.add(createModifyPanel(), "Modify");
        cards.add(createDeletePanel(), "Delete");
        cards.add(createSearchPanel(), "Search");
        return cards;
    }

    /**
     * This will create the test panel in the LdapConfigGui.
     */
    private JPanel createTestPanel() {
        JPanel testPanel = new JPanel(new BorderLayout());
        testPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("test_configuration"))); // $NON-NLS-1$

        testPanel.add(new JLabel(JMeterUtils.getResString("test"))); // $NON-NLS-1$
        JPanel rowPanel = new JPanel();

        rowPanel.add(addTest);
        bGroup.add(addTest);
        rowPanel.add(deleteTest);
        bGroup.add(deleteTest);
        rowPanel.add(searchTest);
        bGroup.add(searchTest);
        rowPanel.add(modifyTest);
        bGroup.add(modifyTest);
        testPanel.add(rowPanel, BorderLayout.NORTH);
        testPanel.add(userDefined, BorderLayout.CENTER);
        return testPanel;
    }

    /**
     * This will initialise all the panel in the LdapConfigGui.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));

        if (displayName) {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(createServernamePanel());
        mainPanel.add(createPortPanel());
        mainPanel.add(createRootdnPanel());
        mainPanel.add(createTestPanel());
        mainPanel.add(testPanel());
        add(mainPanel, BorderLayout.CENTER);

        userDefined.addItemListener(this);
        addTest.addItemListener(this);
        modifyTest.addItemListener(this);
        deleteTest.addItemListener(this);
        searchTest.addItemListener(this);
    }
}
