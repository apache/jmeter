/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    T.Elanjchezhiyan(chezhiyan@siptech.co.in)
 *@created   Apr 29 2003 11:45 AM
 *@company   Sip Technologies and Exports Ltd.
 *@version   1.0
 ***************************************/

/****************************************
 * This class LdapConfigGui is user interface gui 
 * for getting all the configuration value from the
 * user
 ***************************************/

public class LdapConfigGui extends AbstractConfigGui implements ItemListener
{
    private final static String ROOTDN = "rootdn";
    private final static String TEST = "test";
    private static String testValue="NNNN";

    private JTextField rootdn = new JTextField(20);
    private JTextField searchbase = new JTextField(20);
    private JTextField searchfilter = new JTextField(20);
    private JTextField delete = new JTextField(20);
    private JTextField add = new JTextField(20);
    private JTextField modify = new JTextField(20);
    private JTextField servername = new JTextField(20);
    private JTextField port = new JTextField(20);

    private JCheckBox user_Defined = new JCheckBox(JMeterUtils.getResString("user_defined_test"));
    private JRadioButton addTest = new JRadioButton(JMeterUtils.getResString("add_test"));
    private JRadioButton modifyTest = new JRadioButton(JMeterUtils.getResString("modify_test"));
    private JRadioButton deleteTest = new JRadioButton(JMeterUtils.getResString("delete_test"));
    private JRadioButton searchTest = new JRadioButton(JMeterUtils.getResString("search_test"));
    private ButtonGroup bGroup = new ButtonGroup();
    private boolean displayName = true;
    ArgumentsPanel tableAddPanel = new ArgumentsPanel(JMeterUtils.getResString("add_test"));
    ArgumentsPanel tableModifyPanel = new ArgumentsPanel(JMeterUtils.getResString("modify_test"));
    private JPanel cards;
    /****************************************
     *  Default constructor for LdapConfigGui
     ***************************************/
    public LdapConfigGui()
    {
        this(true);
    }

    /* Implements JMeterGUIComponent.getStaticLabel() */
    public String getStaticLabel()
    {
        return  JMeterUtils.getResString("ldap_sample_title");
    }

    /**
     * A newly created component can be initialized with the contents of
     * a Test Element object by calling this method.  The component is
     * responsible for querying the Test Element object for the
     * relevant information to display in its GUI.
     *
     * @param element the TestElement to configure 
     */
    public void configure(TestElement element)
    {
        super.configure(element);
        servername.setText(element.getPropertyAsString(LDAPSampler.SERVERNAME));
        port.setText(element.getPropertyAsString(LDAPSampler.PORT));
        rootdn.setText(element.getPropertyAsString(LDAPSampler.ROOTDN));
        CardLayout cl = (CardLayout)(cards.getLayout());
        if (element.getPropertyAsString(LDAPSampler.TEST).equals("add")) {
            addTest.setSelected(true);
            add.setText(element.getPropertyAsString(LDAPSampler.BASE_ENTRY_DN));
            tableAddPanel.configure((TestElement)element.getProperty(LDAPSampler.ARGUMENTS).getObjectValue());
            cl.show(cards,"Add");
        }else if (element.getPropertyAsString(LDAPSampler.TEST).equals("modify")) {
            modifyTest.setSelected(true);
            modify.setText(element.getPropertyAsString(LDAPSampler.BASE_ENTRY_DN));
            tableModifyPanel.configure((TestElement)element.getProperty(LDAPSampler.ARGUMENTS).getObjectValue());
            cl.show(cards,"Modify");
        }else if (element.getPropertyAsString(LDAPSampler.TEST).equals("delete")) {
            deleteTest.setSelected(true);
            delete.setText(element.getPropertyAsString(LDAPSampler.DELETE));
            cl.show(cards,"Delete");
        }else if (element.getPropertyAsString(LDAPSampler.TEST).equals("search")) {
            searchTest.setSelected(true);
            searchbase.setText(element.getPropertyAsString(LDAPSampler.SEARCHBASE));
            searchfilter.setText(element.getPropertyAsString(LDAPSampler.SEARCHFILTER));
            cl.show(cards,"Search");
        }
        if (element.getPropertyAsBoolean(LDAPSampler.USER_DEFINED)) {
            user_Defined.setSelected(true);
        }else {
            user_Defined.setSelected(false);
            cl.show(cards,"");
        }
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    public TestElement createTestElement()
    {
        ConfigTestElement element = new ConfigTestElement();
        modifyTestElement(element);
        return element;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement element)
    {
        element.clear();
        configureTestElement(element);
        element.setProperty(LDAPSampler.SERVERNAME,servername.getText());
        element.setProperty(LDAPSampler.PORT,port.getText());
        element.setProperty(LDAPSampler.ROOTDN,rootdn.getText());
        element.setProperty(new BooleanProperty(LDAPSampler.USER_DEFINED,user_Defined.isSelected()));
        if (addTest.isSelected()) {
            element.setProperty(new StringProperty(LDAPSampler.TEST,LDAPSampler.ADD));
            element.setProperty(new StringProperty(LDAPSampler.BASE_ENTRY_DN,add.getText()));
            element.setProperty(new TestElementProperty(LDAPSampler.ARGUMENTS,tableAddPanel.createTestElement()));
        }
        if (modifyTest.isSelected()) {
            element.setProperty(new StringProperty(LDAPSampler.TEST,LDAPSampler.MODIFY));
            element.setProperty(new StringProperty(LDAPSampler.BASE_ENTRY_DN,modify.getText()));
            element.setProperty(new TestElementProperty(LDAPSampler.ARGUMENTS,tableModifyPanel.createTestElement()));
        }
        if (deleteTest.isSelected()) {
            element.setProperty(new StringProperty(LDAPSampler.TEST,LDAPSampler.DELETE));
            element.setProperty(new StringProperty(LDAPSampler.DELETE,delete.getText()));
        }
        if (searchTest.isSelected()) {
            element.setProperty(new StringProperty(LDAPSampler.TEST,LDAPSampler.SEARCHBASE));
            element.setProperty(new StringProperty(LDAPSampler.SEARCHBASE,searchbase.getText()));
            element.setProperty(new StringProperty(LDAPSampler.SEARCHFILTER,searchfilter.getText()));
        }
}


/**********************************************
 * This itemStateChanged listener for changing  the
 * card layout for based on the test selected
 * in the User defined test case.
 ***********************************************/
    public void itemStateChanged(ItemEvent ie) {
        CardLayout cl = (CardLayout)(cards.getLayout());
        if (user_Defined.isSelected()) {
            if (addTest.isSelected()){
                cl.show(cards,"Add");
                tableModifyPanel.clear();
                searchbase.setText("");
                searchfilter.setText("");
                delete.setText("");
            } else if(deleteTest.isSelected()){
                cl.show(cards,"Delete");
                tableModifyPanel.clear();
                tableAddPanel.clear();
                searchbase.setText("");
                searchfilter.setText("");
            } else if(searchTest.isSelected()){
                cl.show(cards,"Search");
                delete.setText("");
                tableModifyPanel.clear();
                tableAddPanel.clear();
            } else if(modifyTest.isSelected()){
                cl.show(cards,"Modify");
                tableAddPanel.clear();
                searchbase.setText("");
                searchfilter.setText("");
                delete.setText("");
            }else {
                cl.show(cards,"");
                tableAddPanel.clear();
                tableModifyPanel.clear();
                searchbase.setText("");
                searchfilter.setText("");
                delete.setText("");
            }
        }else {
            cl.show(cards,"");
            tableAddPanel.clear();
            tableModifyPanel.clear();
            searchbase.setText("");
            searchfilter.setText("");
            delete.setText("");
        }
    }
    
    /****************************************
     * !ToDo (Constructor description)
     *
     *@param displayName  !ToDo (Parameter description)
     ***************************************/
    public LdapConfigGui(boolean displayName) {
        this.displayName = displayName;
        init();
    }


    /****************************************
     * This will create the servername
     *panel in the LdapConfigGui
     ***************************************/
    private JPanel createServernamePanel() {
        JPanel serverPanel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString("servername"));
        label.setLabelFor(servername);
        serverPanel.add(label, BorderLayout.WEST);
        serverPanel.add(servername, BorderLayout.CENTER);
        return serverPanel;
    }

    /****************************************
     * This will create the port
     *panel in the LdapConfigGui
     ***************************************/
    private JPanel createPortPanel() {
        JPanel portPanel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString("port"));
        label.setLabelFor(port);
        portPanel.add(label, BorderLayout.WEST);
        portPanel.add(port, BorderLayout.CENTER);
        return portPanel;
    }
    
    /****************************************
     * This will create the Root distinguised name
     *panel in the LdapConfigGui
     ***************************************/
    private JPanel createRootdnPanel() {
        JPanel rootdnPanel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString("dn"));
        label.setLabelFor(rootdn);
        rootdnPanel.add(label, BorderLayout.WEST);
        rootdnPanel.add(rootdn, BorderLayout.CENTER);
        return rootdnPanel;
    }

    /****************************************
     * This will create the Search 
     *panel in the LdapConfigGui
     ***************************************/
    
    private JPanel createSearchPanel() {
        VerticalPanel searchPanel = new VerticalPanel();
        JPanel searchBPanel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString("search_base"));
        label.setLabelFor(searchbase);
        searchBPanel.add(label, BorderLayout.WEST);
        searchBPanel.add(searchbase, BorderLayout.CENTER);
        JPanel searchFPanel  = new JPanel(new BorderLayout(5, 0));
        JLabel label2 = new JLabel(JMeterUtils.getResString("search_filter"));
        label2.setLabelFor(searchfilter);
        searchFPanel.add(label2, BorderLayout.WEST);
        searchFPanel.add(searchfilter, BorderLayout.CENTER);
        searchPanel.add(searchBPanel);
        searchPanel.add(searchFPanel);
        return searchPanel;
    }

    /****************************************
     * This will create the Delete
     *panel in the LdapConfigGui
     ***************************************/
    private JPanel createDeletePanel() {
        VerticalPanel panel = new VerticalPanel();
        JPanel deletePanel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString("delete"));
        label.setLabelFor(delete);
        deletePanel.add(label, BorderLayout.WEST);
        deletePanel.add(delete, BorderLayout.CENTER);
        panel.add(deletePanel);
        return panel;
    }

    /****************************************
     * This will create the Add test
     *panel in the LdapConfigGui
     ***************************************/
    private JPanel createAddPanel() {
        JPanel addPanel = new JPanel(new BorderLayout(5,0));
        JPanel addInnerPanel  = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString("entry_dn"));
        label.setLabelFor(add);
        addInnerPanel.add(label, BorderLayout.WEST);
        addInnerPanel.add(add, BorderLayout.CENTER);
        addPanel.add(addInnerPanel,BorderLayout.NORTH);
        addPanel.add(tableAddPanel,BorderLayout.CENTER);
        return addPanel;
    }

    /****************************************
     * This will create the Modify
     *panel in the LdapConfigGui
     ***************************************/
    private JPanel createModifyPanel() {
        JPanel modifyPanel = new JPanel(new BorderLayout(5,0));
        JPanel modifyInnerPanel  = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString("entry_dn"));
        label.setLabelFor(modify);
        modifyInnerPanel.add(label, BorderLayout.WEST);
        modifyInnerPanel.add(modify, BorderLayout.CENTER);
        modifyPanel.add(modifyInnerPanel,BorderLayout.NORTH);
        modifyPanel.add(tableModifyPanel,BorderLayout.CENTER);
        return modifyPanel;
    }

    /****************************************
     * This will create the user defined test
     * panel for create or modify or delete or search
     * based on the panel selected in the itemevent
     * in the LdapConfigGui
     ***************************************/
    private JPanel testPanel() {
        cards = new JPanel(new CardLayout());
        cards.add(new JPanel(),"");
        cards.add(createAddPanel(),"Add");
        cards.add(createModifyPanel(),"Modify");
        cards.add(createDeletePanel(),"Delete");
        cards.add(createSearchPanel(),"Search");
        return cards;
    }


    /****************************************
     * This will create the test panel
     *in the LdapConfigGui
     ***************************************/
    private JPanel createTestPanel() {
        JPanel testPanel = new JPanel(new BorderLayout());
        testPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("test_configuration")));

        testPanel.add(new JLabel(JMeterUtils.getResString("test")));
        JPanel rowPanel = new JPanel();

        rowPanel.add(addTest);
        bGroup.add(addTest);
        rowPanel.add(deleteTest);                
        bGroup.add(deleteTest);
        rowPanel.add(searchTest);
        bGroup.add(searchTest);
        rowPanel.add(modifyTest);
        bGroup.add(modifyTest);
        testPanel.add(rowPanel,BorderLayout.NORTH);
        testPanel.add(user_Defined, BorderLayout.CENTER);
        return testPanel;
    }
      
    /****************************************
     * This will initalise
     *all the panel in the LdapConfigGui
     ***************************************/
    private void init() {
        user_Defined.addItemListener(this);
        addTest.addItemListener(this);
        modifyTest.addItemListener(this);
        deleteTest.addItemListener(this);
        searchTest.addItemListener(this);
        setLayout(new BorderLayout(0, 5));
        if(displayName) {
            VerticalPanel mainPanel = new VerticalPanel();
            mainPanel.setBorder(makeBorder());
            add(makeTitlePanel(),BorderLayout.NORTH);
            mainPanel.setBorder(makeBorder());
            mainPanel.add(createServernamePanel());
            mainPanel.add(createPortPanel());
            mainPanel.add(createRootdnPanel());
            mainPanel.add(createTestPanel());
            mainPanel.add(testPanel());
            add(mainPanel,BorderLayout.CENTER);
        }
        else {
            VerticalPanel mainPanel = new VerticalPanel();
            mainPanel.add(createServernamePanel());
            mainPanel.add(createPortPanel());
            mainPanel.add(createRootdnPanel());
            mainPanel.add(createTestPanel());
            mainPanel.add(testPanel());
            add(mainPanel,BorderLayout.CENTER);
        }
    }
}
