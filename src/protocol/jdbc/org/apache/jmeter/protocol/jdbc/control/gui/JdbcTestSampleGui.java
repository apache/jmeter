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

package org.apache.jmeter.protocol.jdbc.control.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.jdbc.config.gui.DbConfigGui;
import org.apache.jmeter.protocol.jdbc.config.gui.PoolConfigGui;
import org.apache.jmeter.protocol.jdbc.config.gui.SqlConfigGui;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;

/**
 * @version   $Revision$ on $Date$
 */
public class JdbcTestSampleGui extends AbstractSamplerGui
{

    private PoolConfigGui poolGui;
    private DbConfigGui dbGui;
    private SqlConfigGui sqlGui;

    public JdbcTestSampleGui()
    {
        init();
    }

    public void configure(TestElement element)
    {
        super.configure(element);
        dbGui.configure(element);
        poolGui.configure(element);
        sqlGui.configure(element);
    }

    public String getLabelResource()
    {
        return "database_testing_title";
    }

    public TestElement createTestElement()
    {
        JDBCSampler sampler = new JDBCSampler();
        modifyTestElement(sampler);
        return sampler;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement sampler)
    {
        sampler.clear();
        sampler.addTestElement(dbGui.createTestElement());
        sampler.addTestElement(poolGui.createTestElement());
        sampler.addTestElement(sqlGui.createTestElement());
        configureTestElement(sampler);
    }

    private void init()
    {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 5));

        VerticalPanel connPanel = new VerticalPanel();
        dbGui = new DbConfigGui(false);
        connPanel.add(dbGui);

        poolGui = new PoolConfigGui(false);
        connPanel.add(poolGui);

        mainPanel.add(connPanel, BorderLayout.NORTH);

        sqlGui = new SqlConfigGui(false);
        mainPanel.add(sqlGui, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    public Dimension getPreferredSize()
    {
        return getMinimumSize();
    }
}
