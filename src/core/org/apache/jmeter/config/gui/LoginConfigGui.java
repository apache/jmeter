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
 */
public class LoginConfigGui extends AbstractConfigGui {
    private static final long serialVersionUID = 240L;

    /** Field allowing the user to enter a username. */
    private final JTextField username = new JTextField(15);

    /** Field allowing the user to enter a password. */
    private final JPasswordField password = new JPasswordField(15);

    /**
     * Boolean indicating whether or not this component should display its name.
     * If true, this is a standalone component. If false, this component is
     * intended to be used as a subpanel for another component.
     */
    private boolean displayName = true;

    /**
     * Create a new LoginConfigGui as a standalone component.
     */
    public LoginConfigGui() {
        this(true);
    }

    /**
     * Create a new LoginConfigGui as either a standalone or an embedded
     * component.
     *
     * @param displayName
     *            indicates whether or not this component should display its
     *            name. If true, this is a standalone component. If false, this
     *            component is intended to be used as a subpanel for another
     *            component.
     */
    public LoginConfigGui(boolean displayName) {
        this.displayName = displayName;
        init();
    }

    @Override
    public String getLabelResource() {
        return "login_config_element"; // $NON-NLS-1$
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
        username.setText(element.getPropertyAsString(ConfigTestElement.USERNAME));
        password.setText(element.getPropertyAsString(ConfigTestElement.PASSWORD));
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        ConfigTestElement element = new ConfigTestElement();
        modifyTestElement(element);
        return element;
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement element) {
        configureTestElement(element);
        element.setProperty(new StringProperty(ConfigTestElement.USERNAME, username.getText()));

        String passwordString = new String(password.getPassword());
        element.setProperty(new StringProperty(ConfigTestElement.PASSWORD, passwordString));
    }
    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        username.setText(""); //$NON-NLS-1$
        password.setText(""); //$NON-NLS-1$
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(0, 5));

        if (displayName) {
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
    private JPanel createUsernamePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString("username")); // $NON-NLS-1$
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
    private JPanel createPasswordPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString("password")); // $NON-NLS-1$
        label.setLabelFor(password);
        panel.add(label, BorderLayout.WEST);
        panel.add(password, BorderLayout.CENTER);
        return panel;
    }
}
