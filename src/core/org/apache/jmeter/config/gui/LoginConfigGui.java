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
package org.apache.jmeter.config.gui;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   March 13, 2001
 *@version   1.0
 ***************************************/

public class LoginConfigGui extends AbstractConfigGui
{
	JTextField username = new JTextField(15);
	JTextField password = new JPasswordField(15);
	private boolean displayName = true;

	/****************************************
	 * Constructor for the LoginConfigGui object
	 ***************************************/
	public LoginConfigGui()
	{
		this(true);
	}

	/****************************************
	 * Constructor for the LoginConfigGui object
	 *
	 *@param displayName  Description of Parameter
	 ***************************************/
	public LoginConfigGui(boolean displayName)
	{
		this.displayName = displayName;
		init();
	}

	public String getStaticLabel()
	{
		return JMeterUtils.getResString("Login Config Element");
	}

	public void configure(TestElement element)
	{
		super.configure(element);
		username.setText((String)element.getProperty(ConfigTestElement.USERNAME));
		password.setText((String)element.getProperty(ConfigTestElement.PASSWORD));
	}

	public TestElement createTestElement()
	{
		ConfigTestElement element = new ConfigTestElement();
		configureTestElement(element);
		element.setProperty(ConfigTestElement.USERNAME,username.getText());
		element.setProperty(ConfigTestElement.PASSWORD,password.getText());
		return element;
	}

	private void init()
	{
		this.setLayout(new VerticalLayout(1, VerticalLayout.LEFT));

		if(displayName)
		{
			this.add(getNamePanel());
		}

		this.add(createUsernamePanel());
		this.add(createPasswordPanel());
	}

	private JPanel createUsernamePanel()
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel(JMeterUtils.getResString("username")));
		panel.add(username);
		return panel;
	}

	private JPanel createPasswordPanel()
	{
		JPanel panel = new JPanel();
		panel.add(new JLabel(JMeterUtils.getResString("password")));
		panel.add(password);
		return panel;
	}

}
