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
import org.apache.jmeter.util.JMeterUtils;

/****************************************
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class JdbcTestSampleGui extends AbstractSamplerGui
{

	private PoolConfigGui poolGui;
	private DbConfigGui dbGui;
	private SqlConfigGui sqlGui;


	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
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

	public String getStaticLabel()
	{
		return JMeterUtils.getResString("database_testing_title");
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
    
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
}
