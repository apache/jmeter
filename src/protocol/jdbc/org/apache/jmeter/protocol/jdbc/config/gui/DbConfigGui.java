// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

package org.apache.jmeter.protocol.jdbc.config.gui;
import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.LoginConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @version   $Revision$ on $Date$
 */
public class DbConfigGui extends AbstractConfigGui
{
    private static String URL = "url";
    private static String DRIVER = "driver";
    private JTextField urlField = new JTextField(20);
    private JTextField driverField = new JTextField(20);

    private boolean displayName;
    private LoginConfigGui loginGui;

    public DbConfigGui()
    {
        this(true);
    }

    public DbConfigGui(boolean displayName)
    {
        this.displayName = displayName;
        init();
    }

    public String getLabelResource()
    {
        return "database_login_title";
    }

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
        element.setProperty(JDBCSampler.URL, urlField.getText());
        element.setProperty(JDBCSampler.DRIVER, driverField.getText());
        element.addTestElement(loginGui.createTestElement());
    }

    public void configure(TestElement element)
    {
        super.configure(element);
        urlField.setText(element.getPropertyAsString(JDBCSampler.URL));
        driverField.setText(element.getPropertyAsString(JDBCSampler.DRIVER));
        loginGui.configure(element);
    }

    private void init()
    {
        setLayout(new BorderLayout(0, 5));

        if (displayName)
        {
            setBorder(makeBorder());

            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        VerticalPanel mainPanel = new VerticalPanel();

        // URL and JDBC PROPS
        VerticalPanel urlJDBCPanel = new VerticalPanel();
        urlJDBCPanel.setBorder(
            BorderFactory.createTitledBorder(
                JMeterUtils.getResString("database_url_jdbc_props")));

        urlJDBCPanel.add(getUrlPanel());
        urlJDBCPanel.add(getDriverPanel());

        mainPanel.add(urlJDBCPanel);

        // LOGIN
        JPanel loginPanel = new JPanel(new BorderLayout());
        loginPanel.setBorder(
            BorderFactory.createTitledBorder(
                JMeterUtils.getResString("login_config")));

        loginGui = new LoginConfigGui(false);
        loginPanel.add(loginGui, BorderLayout.NORTH);

        mainPanel.add(loginPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel getDriverPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        JLabel label =
            new JLabel(JMeterUtils.getResString("database_driver_class"));
        label.setLabelFor(driverField);
        driverField.setName(DRIVER);
        panel.add(label, BorderLayout.WEST);
        panel.add(driverField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getUrlPanel()
    {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        JLabel label = new JLabel(JMeterUtils.getResString("database_url"));
        label.setLabelFor(urlField);
        urlField.setName(URL);
        panel.add(label, BorderLayout.WEST);
        panel.add(urlField, BorderLayout.CENTER);
        return panel;
    }
}
