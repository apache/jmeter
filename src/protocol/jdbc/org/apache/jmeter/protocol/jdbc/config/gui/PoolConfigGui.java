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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.protocol.jdbc.util.JMeter19ConnectionPool;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @version   $Revision$ on $Date$
 */
public class PoolConfigGui extends AbstractConfigGui implements FocusListener
{
    private static String CONNECTIONS = "connections";
    private static String MAXUSE = "maxuse";
    private static String DEFAULT_MAX_USE = "50";
    private static String DEFAULT_NUM_CONNECTIONS = "1";
    private JTextField connField;
    private JTextField maxUseField;

    private boolean displayName;

    public PoolConfigGui()
    {
        this(true);
    }

    public PoolConfigGui(boolean displayName)
    {
        this.displayName = displayName;
        init();
    }

    public void configure(TestElement element)
    {
        super.configure(element);
        connField.setText(
            element.getPropertyAsString(JMeter19ConnectionPool.CONNECTIONS));
        maxUseField.setText(
            element.getPropertyAsString(JMeter19ConnectionPool.MAXUSE));
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
        configureTestElement(element);
        element.setProperty(
            JDBCSampler.CONNECTION_POOL_IMPL,
            JMeter19ConnectionPool.class.getName());
        element.setProperty(
            JMeter19ConnectionPool.CONNECTIONS,
            connField.getText());
        element.setProperty(
            JMeter19ConnectionPool.MAXUSE,
            maxUseField.getText());
    }

    public String getLabelResource()
    {
        return "database_conn_pool_title";
    }

    public void focusGained(FocusEvent e)
    {
    }

    public void focusLost(FocusEvent e)
    {
        String name = e.getComponent().getName();

        if (name.equals(CONNECTIONS))
        {
            try
            {
                Integer.parseInt(connField.getText());
            }
            catch (NumberFormatException nfe)
            {
                if (connField.getText().length() > 0)
                {
                    JOptionPane.showMessageDialog(
                        this,
                        "You must enter a valid number",
                        "Invalid data",
                        JOptionPane.WARNING_MESSAGE);
                }
                connField.setText(DEFAULT_NUM_CONNECTIONS);
            }
        }
        else if (name.equals(MAXUSE))
        {
            try
            {
                Integer.parseInt(maxUseField.getText());
            }
            catch (NumberFormatException nfe)
            {
                if (maxUseField.getText().length() > 0)
                {
                    JOptionPane.showMessageDialog(
                        this,
                        "You must enter a valid number",
                        "Invalid data",
                        JOptionPane.WARNING_MESSAGE);
                }
                maxUseField.setText(DEFAULT_NUM_CONNECTIONS);
            }
        }
    }

    private void init()
    {
        setLayout(new BorderLayout(0, 5));

        if (displayName)
        {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        VerticalPanel poolPanel = new VerticalPanel();
        poolPanel.setBorder(
            BorderFactory.createTitledBorder(
                JMeterUtils.getResString("database_conn_pool_props")));

        poolPanel.add(createConnPanel());
        poolPanel.add(createMaxUsePanel());

        // The Center component will fill all available space.  Since poolPanel
        // has a titled border, this means that the border would extend to the
        // bottom of the frame, which is ugly.  So put the poolPanel in a
        // second panel to fix this.
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.add(poolPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createConnPanel()
    {
        connField = new JTextField(DEFAULT_NUM_CONNECTIONS, 5);
        connField.setName(CONNECTIONS);
        connField.addFocusListener(this);

        JLabel label =
            new JLabel(JMeterUtils.getResString("database_conn_pool_size"));
        label.setLabelFor(connField);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(connField, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMaxUsePanel()
    {
        maxUseField = new JTextField(DEFAULT_MAX_USE, 5);
        maxUseField.setName(MAXUSE);
        maxUseField.addFocusListener(this);

        JLabel label =
            new JLabel(
                JMeterUtils.getResString("database_conn_pool_max_usage"));
        label.setLabelFor(maxUseField);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(maxUseField, BorderLayout.CENTER);
        return panel;
    }
}
