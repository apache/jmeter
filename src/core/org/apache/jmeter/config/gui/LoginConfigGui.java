/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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
package org.apache.jmeter.config.gui;
import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.util.JMeterUtils;

/**
 * A GUI component allowing the user to enter a username and password for a
 * login.
 *
 * @author    Michael Stover
 * @version   $Revision$
 */
public class LoginConfigGui extends AbstractConfigGui
{
    /** Field allowing the user to enter a username. */
    private JTextField username = new JTextField(15);
    
    /** Field allowing the user to enter a password. */
    private JPasswordField password = new JPasswordField(15);
    
    /**
     * Boolean indicating whether or not this component should display its
     * name. If true, this is a standalone component. If false, this component
     * is intended to be used as a subpanel for another component.
     */
    private boolean displayName = true;

    /**
     * Create a new LoginConfigGui as a standalone component.
     */
    public LoginConfigGui()
    {
        this(true);
    }

    /**
     * Create a new LoginConfigGui as either a standalone or an embedded
     * component.
     *
     * @param displayName  indicates whether or not this component should
     *                     display its name.  If true, this is a standalone
     *                     component.  If false, this component is intended
     *                     to be used as a subpanel for another component.
     */
    public LoginConfigGui(boolean displayName)
    {
        this.displayName = displayName;
        init();
    }

    /* Implements JMeterGUIComponent.getStaticLabel() */
    public String getStaticLabel()
    {
        return JMeterUtils.getResString("Login Config Element");
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
        username.setText(
            element.getPropertyAsString(ConfigTestElement.USERNAME));
        password.setText(
            element.getPropertyAsString(ConfigTestElement.PASSWORD));
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    public TestElement createTestElement()
    {
        ConfigTestElement element = new ConfigTestElement();
        modifyTestElement(element);
        return element;
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    public void modifyTestElement(TestElement element)
    {
        configureTestElement(element);
        element.setProperty(
            new StringProperty(ConfigTestElement.USERNAME, username.getText()));
        
        String passwordString = new String(password.getPassword());
        element.setProperty(
            new StringProperty(ConfigTestElement.PASSWORD, passwordString));
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init()
    {
        setLayout(new BorderLayout(0, 5));

        if (displayName)
        {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(createUsernamePanel());
        mainPanel.add(createPasswordPanel());
        add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Create a panel containing the username field and corresponding label.
     * 
     * @return a GUI panel containing the username field
     */
    private JPanel createUsernamePanel()
    {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString("username"));
        label.setLabelFor(username);
        panel.add(label, BorderLayout.WEST);
        panel.add(username, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Create a panel containing the password field and corresponding label.
     * 
     * @return a GUI panel containing the password field
     */
    private JPanel createPasswordPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString("password"));
        label.setLabelFor(password);
        panel.add(label, BorderLayout.WEST);
        panel.add(password, BorderLayout.CENTER);
        return panel;
    }
}
