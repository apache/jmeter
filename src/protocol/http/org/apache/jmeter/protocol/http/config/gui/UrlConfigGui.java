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
package org.apache.jmeter.protocol.http.config.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.gui.util.VerticalLayout;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class UrlConfigGui extends AbstractConfigGui
{
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	protected boolean displayName = true;

	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	protected HTTPArgumentsPanel argsPanel;
	private static String DOMAIN = "domain";
	private static String PORT = "port";
	private static String PATH = "path";
	private static String FOLLOW_REDIRECTS = "follow_redirects";
	private static String USE_KEEPALIVE = "use_keepalive";
	private static String POST = "post";
	private static String GET = "get";
	private static String HTTP = "http";
	private static String HTTPS = "https";
	private static String SEND_PARAM = "sendparam";

	private JTextField domain;
	private JTextField port;
	private JTextField path;
	private JCheckBox followRedirects;
	private JCheckBox useKeepAlive;
	private JRadioButton post;
	private JRadioButton get;
	private JRadioButton http;
	private JRadioButton https;


	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public UrlConfigGui()
	{
		this(true);
	}

	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param display  !ToDo (Parameter description)
	 ***************************************/
	public UrlConfigGui(boolean display)
	{
		displayName = display;
		init();
		setName(getStaticLabel());
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public String getStaticLabel()
	{
		return JMeterUtils.getResString("url_config_title");
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public TestElement createTestElement()
	{
		ConfigTestElement element = new ConfigTestElement();
		this.configureTestElement(element);
		Arguments args = (Arguments)argsPanel.createTestElement();
		HTTPArgument.convertArgumentsToHTTP(args);
		element.setProperty(HTTPSampler.ARGUMENTS, args);
		element.setProperty(HTTPSampler.DOMAIN, domain.getText());
		element.setProperty(HTTPSampler.PORT, port.getText());
		element.setProperty(HTTPSampler.METHOD, (post.isSelected() ? "POST" : "GET"));
		element.setProperty(HTTPSampler.PATH, path.getText());
		element.setProperty(HTTPSampler.FOLLOW_REDIRECTS, new Boolean(followRedirects.isSelected()));
		element.setProperty(HTTPSampler.USE_KEEPALIVE, new Boolean(useKeepAlive.isSelected()));
		element.setProperty(HTTPSampler.PROTOCOL, (http.isSelected() ? "http" : "https"));
		return element;
	}

	public void configureSampler(HTTPSampler sampler)
	{
		sampler.setArguments((Arguments)argsPanel.createTestElement());
		sampler.setDomain(domain.getText());
		sampler.setPath(path.getText());
		sampler.setFollowRedirects(followRedirects.isSelected());
		sampler.setUseKeepAlive(useKeepAlive.isSelected());
		if(port.getText().length() > 0)
		{
			sampler.setPort(Integer.parseInt(port.getText()));
		}
		sampler.setMethod((post.isSelected() ? "POST" : "GET"));
		sampler.setProtocol((http.isSelected() ? "http" : "https"));
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param el  !ToDo (Parameter description)
	 ***************************************/
	public void configure(TestElement el)
	{
		setName((String)el.getProperty(TestElement.NAME));
		argsPanel.configure((TestElement)el.getProperty(HTTPSampler.ARGUMENTS));
		domain.setText((String)el.getProperty(HTTPSampler.DOMAIN));
		port.setText((String)el.getPropertyAsString(HTTPSampler.PORT));
		if("POST".equals(el.getProperty(HTTPSampler.METHOD)))
		{
			post.setSelected(true);
			get.setSelected(false);
		}
		else
		{
			get.setSelected(true);
			post.setSelected(false);
		}
		path.setText((String)el.getProperty(HTTPSampler.PATH));
		followRedirects.setSelected(((AbstractTestElement)el).getPropertyAsBoolean(HTTPSampler.FOLLOW_REDIRECTS));
		useKeepAlive.setSelected(((AbstractTestElement)el).getPropertyAsBoolean(HTTPSampler.USE_KEEPALIVE));
		if("http".equals(el.getProperty(HTTPSampler.PROTOCOL)))
		{
			http.setSelected(true);
			https.setSelected(false);
		}
		else
		{
			https.setSelected(true);
			http.setSelected(false);
		}
	}

	/****************************************
	 * !ToDo (Method description)
	 ***************************************/
	protected void init()
	{
		this.setLayout(new BorderLayout());

		JPanel webServerPanel = new JPanel();
		webServerPanel.setLayout(new BorderLayout());
		webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("web_server")));
		webServerPanel.add(getDomainPanel(),BorderLayout.NORTH);
		webServerPanel.add(getPortPanel(),BorderLayout.SOUTH);

		JPanel webRequestPanel = new JPanel();
		webRequestPanel.setLayout(new BorderLayout());
		webRequestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils.getResString("web_request")));
		JPanel northPanel = new JPanel(new BorderLayout());
		northPanel.add(getProtocolAndMethodPanel(),BorderLayout.NORTH);
		northPanel.add(getPathPanel(),BorderLayout.SOUTH);
		webRequestPanel.add(northPanel,BorderLayout.NORTH);
		webRequestPanel.add(getParameterPanel(),BorderLayout.CENTER);

		if(displayName)
		{
			// MAIN PANEL
			JPanel mainPanel = new JPanel();
			Border margin = new EmptyBorder(10, 10, 5, 10);
			mainPanel.setBorder(margin);
			mainPanel.setLayout(new BorderLayout());
			JPanel normalPanel = new JPanel(new VerticalLayout(5,VerticalLayout.LEFT));

			// TITLE
			JLabel panelTitleLabel = new JLabel(JMeterUtils.getResString("url_config_title"));
			Font curFont = panelTitleLabel.getFont();
			int curFontSize = curFont.getSize();
			curFontSize += 4;
			panelTitleLabel.setFont(new Font(curFont.getFontName(), curFont.getStyle(), curFontSize));
			normalPanel.add(panelTitleLabel);

			// NAME
			if(displayName)
			{
				normalPanel.add(getNamePanel());
			}

			normalPanel.add(webServerPanel);
			mainPanel.add(normalPanel,BorderLayout.NORTH);
			mainPanel.add(webRequestPanel,BorderLayout.CENTER);

			this.add(mainPanel,BorderLayout.CENTER);
		}
		else
		{
			this.add(webServerPanel,BorderLayout.NORTH);
			this.add(webRequestPanel,BorderLayout.CENTER);
		}
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	protected JPanel getPortPanel()
	{
		JPanel portP = new JPanel(new FlowLayout(FlowLayout.LEFT));
		portP.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
		portP.add(new JLabel(JMeterUtils.getResString("web_server_port")));

		port = new JTextField(6);

		port.setName(PORT);
		portP.add(port);

		return portP;
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	protected JPanel getDomainPanel()
	{
		JPanel domainP = new JPanel(new FlowLayout(FlowLayout.LEFT));
		domainP.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
		domainP.add(new JLabel(JMeterUtils.getResString("web_server_domain")));

		domain = new JTextField(20);
		domain.setName(DOMAIN);
		domainP.add(domain);

		return domainP;
	}

	/****************************************
	 * This method defines the Panel for the 
	 * HTTP path, 'Follow Redirects' and
	 * 'Use KeepAlive' elements.
	 *
	 *@return JPanel The Panel for the path,
	 * 'Follow Redirects' and 'Use KeepAlive' elements.
	 ***************************************/
	protected JPanel getPathPanel()
	{
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 10));
		panel.add(new JLabel(JMeterUtils.getResString("path")));

		path = new JTextField(15);
		path.setName(PATH);
		panel.add(path);

		followRedirects= new JCheckBox(JMeterUtils.getResString("follow_redirects"));
		followRedirects.setName(FOLLOW_REDIRECTS);
		// Set this by default so as to stay compliant with the old
		// behaviour:
		followRedirects.setSelected(true);
		panel.add(followRedirects);

		useKeepAlive = new JCheckBox(JMeterUtils.getResString("use_keepalive"));
		useKeepAlive.setName(USE_KEEPALIVE);
		useKeepAlive.setSelected(true);
		panel.add(useKeepAlive);

		return panel;
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	protected JPanel getProtocolAndMethodPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

		panel.add(new JLabel(JMeterUtils.getResString("protocol")));
		panel.add(Box.createRigidArea(new Dimension(5, 0)));

		// PROTOCOL
		http = new JRadioButton(JMeterUtils.getResString("url_config_http"));
		https = new JRadioButton(JMeterUtils.getResString("url_config_https"));
		ButtonGroup protocolButtonGroup = new ButtonGroup();
		protocolButtonGroup.add(http);
		protocolButtonGroup.add(https);

		http.setSelected(true);

		panel.add(http);
		panel.add(https);

		panel.add(Box.createRigidArea(new Dimension(20, 0)));

		// METHOD
		post = new JRadioButton(JMeterUtils.getResString("url_config_post"));
		get = new JRadioButton(JMeterUtils.getResString("url_config_get"));
		ButtonGroup methodButtonGroup = new ButtonGroup();
		methodButtonGroup.add(post);
		methodButtonGroup.add(get);

		panel.add(new JLabel(JMeterUtils.getResString("method")));
		panel.add(Box.createRigidArea(new Dimension(5, 0)));

		post.setSelected(true);

		panel.add(get);
		panel.add(post);

		return panel;
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	protected JPanel getParameterPanel()
	{
		argsPanel = new HTTPArgumentsPanel();

		return argsPanel;
	}
}
