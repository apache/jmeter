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
import org.apache.jmeter.protocol.jdbc.util.JMeter19ConnectionPool;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @author    Michael Stover
 * @version   $Revision$
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
            element.getProperty(JMeter19ConnectionPool.CONNECTIONS).toString());
        maxUseField.setText(
            element.getProperty(JMeter19ConnectionPool.MAXUSE).toString());
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
            JMeter19ConnectionPool.CONNECTIONS,
            connField.getText());
        element.setProperty(
            JMeter19ConnectionPool.MAXUSE,
            maxUseField.getText());
    }

    public String getStaticLabel()
    {
        return JMeterUtils.getResString("database_conn_pool_title");
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
