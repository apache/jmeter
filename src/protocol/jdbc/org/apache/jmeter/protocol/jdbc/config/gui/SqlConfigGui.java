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
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @author    Michael Stover
 * @version   $Revision$
 */
public class SqlConfigGui extends AbstractConfigGui
{
    private JTextArea sqlField;
    private boolean displayName;

    public SqlConfigGui()
    {
        this(true);
    }

    public SqlConfigGui(boolean displayName)
    {
        this.displayName = displayName;
        init();
    }

    public String getStaticLabel()
    {
        return JMeterUtils.getResString("database_sql_query_title");
    }

    public void configure(TestElement element)
    {
        sqlField.setText(element.getProperty(JDBCSampler.QUERY).toString());
        super.configure(element);
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

        String text = sqlField.getText();
        // Remove any line feeds from the text
        text = text.replace('\n', ' ');
        element.setProperty(JDBCSampler.QUERY, text);
    }

    private void init()
    {
        setLayout(new BorderLayout(0, 5));

        if (displayName)
        {
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
        }

        JPanel panel = createSqlPanel();
        add(panel, BorderLayout.CENTER);
        // Don't let the SQL field shrink too much
        add(
            Box.createVerticalStrut(panel.getPreferredSize().height),
            BorderLayout.WEST);
    }

    private JPanel createSqlPanel()
    {
        sqlField = new JTextArea();
        sqlField.setRows(4);
        sqlField.setLineWrap(true);
        sqlField.setWrapStyleWord(true);

        JLabel label =
            new JLabel(JMeterUtils.getResString("database_sql_query_string"));
        label.setLabelFor(sqlField);

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(sqlField), BorderLayout.CENTER);
        return panel;
    }

    public Dimension getPreferredSize()
    {
        return getMinimumSize();
    }
}
