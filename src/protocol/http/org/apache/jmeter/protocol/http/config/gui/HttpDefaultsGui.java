/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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

import javax.swing.Box;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;


/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class HttpDefaultsGui extends AbstractConfigGui
{
	JLabeledTextField protocol;
	JLabeledTextField domain;
	JLabeledTextField path;
	JLabeledTextField port;
	HTTPArgumentsPanel argPanel;
	
	public HttpDefaultsGui()
	{
		super();
		init();
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
	
	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement()
	{
		ConfigTestElement config = new ConfigTestElement();
		modifyTestElement(config);
		return config;
	}

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement config)
    {
        super.configureTestElement(config);
        config.setProperty(HTTPSampler.PROTOCOL,protocol.getText());
        config.setProperty(HTTPSampler.DOMAIN,domain.getText());
        config.setProperty(HTTPSampler.PATH,path.getText());
        config.setProperty(new TestElementProperty(HTTPSampler.ARGUMENTS,argPanel.createTestElement()));
        config.setProperty(HTTPSampler.PORT,port.getText());
    }
	
	public void configure(TestElement el)
	{
		super.configure(el);
		protocol.setText(el.getPropertyAsString(HTTPSampler.PROTOCOL));
		domain.setText(el.getPropertyAsString(HTTPSampler.DOMAIN));
		path.setText(el.getPropertyAsString(HTTPSampler.PATH));
		port.setText(el.getPropertyAsString(HTTPSampler.PORT));
		argPanel.configure((TestElement)el.getProperty(HTTPSampler.ARGUMENTS).getObjectValue());
	}
	
	private void init()
	{
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);

        Box mainPanel = Box.createVerticalBox();
        
        VerticalPanel urlPanel = new VerticalPanel();
		protocol = new JLabeledTextField(JMeterUtils.getResString("url_config_protocol"));
        urlPanel.add(protocol);
        
		domain = new JLabeledTextField(JMeterUtils.getResString("web_server_domain"));
        urlPanel.add(domain);
        
		path = new JLabeledTextField(JMeterUtils.getResString("path"));
        urlPanel.add(path);
        
		port = new JLabeledTextField(JMeterUtils.getResString("web_server_port"));
        urlPanel.add(port);
        
        mainPanel.add(urlPanel);

        argPanel = new HTTPArgumentsPanel();
        mainPanel.add(argPanel);
        
		add(mainPanel, BorderLayout.CENTER);
	}
    
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
}
