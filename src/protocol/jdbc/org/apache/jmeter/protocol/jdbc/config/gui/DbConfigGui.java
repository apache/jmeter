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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.LoginConfigGui;
import org.apache.jmeter.protocol.jdbc.sampler.JDBCSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/****************************************
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class DbConfigGui extends AbstractConfigGui
{
	private final static String LOGIN_ELEMENT = "DbConfigGui.login_element";
	private static String PROTOCOL = "protocol";
	private static String URL = "url";
	private static String DRIVER = "driver";
	private JTextField urlField = new JTextField(20);
	private JTextField driverField = new JTextField(20);

	private boolean displayName;
	private LoginConfigGui loginGui;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public DbConfigGui()
	{
		this(true);
	}

	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param displayName  !ToDo (Parameter description)
	 ***************************************/
	public DbConfigGui(boolean displayName)
	{
		this.displayName = displayName;
		init();
	}

	public String getStaticLabel()
	{
		return JMeterUtils.getResString("database_login_title");
	}

	public TestElement createTestElement()
	{
		ConfigTestElement element = new ConfigTestElement();
		configureTestElement(element);
		element.setProperty(JDBCSampler.URL,urlField.getText());
		element.setProperty(JDBCSampler.DRIVER,driverField.getText());
		element.addTestElement(loginGui.createTestElement());
		return element;
	}

	public void configure(TestElement element)
	{
		super.configure(element);
		urlField.setText((String)element.getProperty(JDBCSampler.URL));
		driverField.setText((String)element.getProperty(JDBCSampler.DRIVER));
		loginGui.configure(element);
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
			JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("database_login_title"));
			Font curFont = panelTitleLabel.getFont();
			int curFontSize = curFont.getSize();
			curFontSize += 4;
			panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
			mainPanel.add(panelTitleLabel);

			// NAME
			mainPanel.add(getNamePanel());

			// URL and JDBC PROPS
			JPanel urlJDBCPanel = new JPanel();
			urlJDBCPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
			urlJDBCPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("database_url_jdbc_props")));

			// URL
			urlJDBCPanel.add(getUrlPanel());

			// DRIVER
			urlJDBCPanel.add(getDriverPanel());

			mainPanel.add(urlJDBCPanel);

			// LOGIN
			loginGui = new LoginConfigGui(false);
			JPanel loginPanel = new JPanel();
			loginPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("login_config")));
			loginPanel.add(loginGui);
			mainPanel.add(loginPanel);

			this.add(mainPanel);
		}
		else
		{
			this.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));

			// URL and JDBC PROPS
			JPanel urlJDBCPanel = new JPanel();
			urlJDBCPanel.setLayout(new VerticalLayout(5, VerticalLayout.LEFT));
			urlJDBCPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("database_url_jdbc_props")));

			// URL
			urlJDBCPanel.add(getUrlPanel());

			// DRIVER
			urlJDBCPanel.add(getDriverPanel());

			this.add(urlJDBCPanel);

			// LOGIN
			loginGui = new LoginConfigGui(false);
			JPanel loginPanel = new JPanel();
			loginPanel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("login_config")));
			loginPanel.add(loginGui);
			this.add(loginPanel);
		}
	}

	private JPanel getDriverPanel()
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel(JMeterUtils.getResString("database_driver_class")));
		driverField.setName(DRIVER);
		panel.add(driverField);
		return panel;
	}

	private JPanel getUrlPanel()
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel(JMeterUtils.getResString("database_url")));
		urlField.setName(URL);
		panel.add(urlField);
		return panel;
	}
}
