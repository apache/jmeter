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
package org.apache.jmeter.protocol.jdbc.config.gui;
import java.awt.Font;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class PoolConfigGui extends AbstractConfigGui implements FocusListener
{
	private static String CONNECTIONS = "connections";
	private static String MAXUSE = "maxuse";
	private static String DEFAULT_MAX_USE = "50";
	private static String DEFAULT_NUM_CONNECTIONS = "1";
	private JTextField connField = new JTextField(5);
	private JTextField maxUseField = new JTextField(5);

	private boolean displayName;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public PoolConfigGui()
	{
		this(true);
	}

	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param displayName  !ToDo (Parameter description)
	 ***************************************/
	public PoolConfigGui(boolean displayName)
	{
		this.displayName = displayName;
		init();
	}

	public void configure(TestElement element)
	{
		super.configure(element);
		connField.setText(element.getProperty(JDBCSampler.CONNECTIONS).toString());
		maxUseField.setText(element.getProperty(JDBCSampler.MAXUSE).toString());
	}

	public TestElement createTestElement()
	{
		ConfigTestElement element = new ConfigTestElement();
		configureTestElement(element);
		element.setProperty(JDBCSampler.CONNECTIONS,connField.getText());
		element.setProperty(JDBCSampler.MAXUSE,maxUseField.getText());
		return element;
	}

	public String getStaticLabel()
	{
		return JMeterUtils.getResString("database_conn_pool_title");
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void focusGained(FocusEvent e) { }

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param e  !ToDo (Parameter description)
	 ***************************************/
	public void focusLost(FocusEvent e)
	{
		String name = e.getComponent().getName();

		if(name.equals(CONNECTIONS))
		{
			try
			{
				Integer.parseInt(connField.getText());
			}
			catch(NumberFormatException nfe)
			{
				if(connField.getText().length() > 0)
				{
					JOptionPane.showMessageDialog(this, "You must enter a valid number",
							"Invalid data", JOptionPane.WARNING_MESSAGE);
				}
				connField.setText(DEFAULT_NUM_CONNECTIONS);
			}
		}
		else if(name.equals(MAXUSE))
		{
			try
			{
				Integer.parseInt(maxUseField.getText());
			}
			catch(NumberFormatException nfe)
			{
				if(maxUseField.getText().length() > 0)
				{
					JOptionPane.showMessageDialog(this, "You must enter a valid number",
							"Invalid data", JOptionPane.WARNING_MESSAGE);
				}
				maxUseField.setText(DEFAULT_NUM_CONNECTIONS);
			}
		}
	}

	private void init()
	{
		if(displayName)
		{
			this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT, VerticalLayout.TOP));

			// MAIN PANEL
			JPanel mainPanel = new JPanel();
			Border margin = new EmptyBorder(10, 10, 5, 10);
			mainPanel.setBorder(margin);
			mainPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

			// TITLE
			JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("database_conn_pool_title"));
			Font curFont = panelTitleLabel.getFont();
			int curFontSize = curFont.getSize();
			curFontSize += 4;
			panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
			mainPanel.add(panelTitleLabel);

			// NAME
			mainPanel.add(getNamePanel());

			// CONNECTION POOL
			JPanel connPoolPanel = new JPanel();
			connPoolPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
			connPoolPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("database_conn_pool_props")));

			// NUMBER OF CONNECTIONS
			connPoolPanel.add(createConnPanel());

			// MAX USAGE
			connPoolPanel.add(createMaxUsePanel());

			mainPanel.add(connPoolPanel);

			this.add(mainPanel);
		}
		else
		{
			this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

			// CONNECTION POOL
			JPanel connPoolPanel = new JPanel();
			connPoolPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
			connPoolPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("database_conn_pool_props")));

			// NUMBER OF CONNECTIONS
			connPoolPanel.add(createConnPanel());

			// MAX USAGE
			connPoolPanel.add(createMaxUsePanel());

			this.add(connPoolPanel);
		}
	}

	private JPanel createConnPanel()
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel(JMeterUtils.getResString("database_conn_pool_size")));
		connField.setText(DEFAULT_NUM_CONNECTIONS);
		connField.setName(CONNECTIONS);
		connField.addFocusListener(this);
		panel.add(connField);
		return panel;
	}

	private JPanel createMaxUsePanel()
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel(JMeterUtils.getResString("database_conn_pool_max_usage")));
		maxUseField.setText(DEFAULT_MAX_USE);
		maxUseField.setName(MAXUSE);
		maxUseField.addFocusListener(this);
		panel.add(maxUseField);
		return panel;
	}
}
