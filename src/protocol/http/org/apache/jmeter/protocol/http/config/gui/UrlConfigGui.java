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
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;


/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class UrlConfigGui extends JPanel
{

    /****************************************
     * !ToDo (Field description)
     ***************************************/
    protected HTTPArgumentsPanel argsPanel;
    private static String DOMAIN = "domain";
    private static String PORT = "port";
    private static String PROTOCOL = "protocol";
    private static String PATH = "path";
    private static String FOLLOW_REDIRECTS = "follow_redirects";
    private static String USE_KEEPALIVE = "use_keepalive";
    private static String POST = "post";
    private static String GET = "get";
    private static String SEND_PARAM = "sendparam";

    private JTextField domain;
    private JTextField port;
    private JTextField protocol;
    private JTextField path;
    private JCheckBox followRedirects;
    private JCheckBox useKeepAlive;
    private JRadioButton post;
    private JRadioButton get;

    /****************************************
     * !ToDo (Constructor description)
     ***************************************/
    public UrlConfigGui()
    {
        init();
    }

    protected void configureTestElement(TestElement mc)
    {
        mc.setProperty(TestElement.NAME, getName());
        mc.setProperty(TestElement.GUI_CLASS, this.getClass().getName());
        mc.setProperty(TestElement.TEST_CLASS, mc.getClass().getName());
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
        Arguments args = (Arguments) argsPanel.createTestElement();

        HTTPArgument.convertArgumentsToHTTP(args);
        element.setProperty(new TestElementProperty(HTTPSampler.ARGUMENTS, args));
        element.setProperty(HTTPSampler.DOMAIN, domain.getText());
        element.setProperty(HTTPSampler.PORT, port.getText());
        element.setProperty(HTTPSampler.PROTOCOL, protocol.getText());
        element.setProperty(HTTPSampler.METHOD,
                (post.isSelected() ? "POST" : "GET"));
        element.setProperty(HTTPSampler.PATH, path.getText());
        element.setProperty(new BooleanProperty(HTTPSampler.FOLLOW_REDIRECTS,
                followRedirects.isSelected()));
        element.setProperty(new BooleanProperty(HTTPSampler.USE_KEEPALIVE,
                useKeepAlive.isSelected()));
        return element;
    }

    public void configureSampler(HTTPSampler sampler)
    {
        sampler.setArguments((Arguments) argsPanel.createTestElement());
        sampler.setDomain(domain.getText());
        sampler.setProtocol(protocol.getText());
        sampler.setPath(path.getText());
        sampler.setFollowRedirects(followRedirects.isSelected());
        sampler.setUseKeepAlive(useKeepAlive.isSelected());
        if (port.getText().length() > 0)
        {
            sampler.setPort(Integer.parseInt(port.getText()));
        }
        sampler.setMethod((post.isSelected() ? "POST" : "GET"));
    }

    /****************************************
     * Set the text, etc. in the UI.
     *
     *@param el contains the data to be displayed
     ***************************************/
    public void configure(TestElement el)
    {
        setName(el.getPropertyAsString(TestElement.NAME));
        argsPanel.configure((TestElement) el.getProperty(HTTPSampler.ARGUMENTS).getObjectValue());
        domain.setText(el.getPropertyAsString(HTTPSampler.DOMAIN));
        
        String portString = (String) el.getPropertyAsString(HTTPSampler.PORT);

        // Only display the port number if it is meaningfully specified
        if (portString.equals("" + HTTPSampler.UNSPECIFIED_PORT))
        {
                port.setText("");
        }
        else
        {
            port.setText(portString);
        }
        protocol.setText(el.getPropertyAsString(HTTPSampler.PROTOCOL));
        if ("POST".equals(el.getPropertyAsString(HTTPSampler.METHOD)))
        {
            post.setSelected(true);
            get.setSelected(false);
        }
        else
        {
            get.setSelected(true);
            post.setSelected(false);
        }
        path.setText(el.getPropertyAsString(HTTPSampler.PATH));
        followRedirects.setSelected(((AbstractTestElement) el).getPropertyAsBoolean(HTTPSampler.FOLLOW_REDIRECTS));
        useKeepAlive.setSelected(((AbstractTestElement) el).getPropertyAsBoolean(HTTPSampler.USE_KEEPALIVE));
    }

    /****************************************
     * !ToDo (Method description)
     ***************************************/
    protected void init()
    {
        this.setLayout(new BorderLayout());

        JPanel webServerPanel = new JPanel();

        webServerPanel.setLayout(new BorderLayout());
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_server")));
        webServerPanel.add(getDomainPanel(), BorderLayout.NORTH);
        webServerPanel.add(getPortPanel(), BorderLayout.SOUTH);

        JPanel webRequestPanel = new JPanel();

        webRequestPanel.setLayout(new BorderLayout());
        webRequestPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_request")));
        JPanel northPanel = new JPanel(new BorderLayout());

        northPanel.add(getProtocolAndMethodPanel(), BorderLayout.NORTH);
        northPanel.add(getPathPanel(), BorderLayout.SOUTH);
        webRequestPanel.add(northPanel, BorderLayout.NORTH);
        webRequestPanel.add(getParameterPanel(), BorderLayout.CENTER);

        this.add(webServerPanel, BorderLayout.NORTH);
        this.add(webRequestPanel, BorderLayout.CENTER);
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    protected JPanel getPortPanel()
    {
        port = new JTextField(6);
        port.setName(PORT);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_port"));
        label.setLabelFor(port);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(port, BorderLayout.CENTER);

        return panel;
    }

    /****************************************
     * !ToDoo (Method description)
     *
     *@return   !ToDo (Return description)
     ***************************************/
    protected JPanel getDomainPanel()
    {
        domain = new JTextField(20);
        domain.setName(DOMAIN);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_domain"));
        label.setLabelFor(domain);
        
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(domain, BorderLayout.CENTER);
        return panel;
    }

    /****************************************
     * This method defines the Panel for the 
     * HTTP path, 'Follow Redirects' and
     * 'Use KeepAlive' elements.
     *
     *@return JPanel The Panel for the path,
     * 'Follow Redirects' and 'Use KeepAlive' elements.
     ***************************************/
    protected Component getPathPanel()
    {
        path = new JTextField(15);
        path.setName(PATH);

        JLabel label = new JLabel(JMeterUtils.getResString("path"));
        label.setLabelFor(path);

        followRedirects = new JCheckBox(JMeterUtils.getResString("follow_redirects"));
        followRedirects.setName(FOLLOW_REDIRECTS);
        followRedirects.setSelected(true);

        useKeepAlive = new JCheckBox(JMeterUtils.getResString("use_keepalive"));
        useKeepAlive.setName(USE_KEEPALIVE);
        useKeepAlive.setSelected(true);

        Box panel = Box.createHorizontalBox();
        panel.add(label);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(path);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(followRedirects);
        panel.add(Box.createHorizontalStrut(5));
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
        // PROTOCOL
        protocol = new JTextField(4);
        protocol.setName(PROTOCOL);

        JLabel protocolLabel = new JLabel(JMeterUtils.getResString("protocol"));
        protocolLabel.setLabelFor(protocol);

        // METHOD
        ButtonGroup methodButtonGroup = new ButtonGroup();

        get = new JRadioButton(JMeterUtils.getResString("url_config_get"));
        methodButtonGroup.add(get);

        post = new JRadioButton(JMeterUtils.getResString("url_config_post"));
        methodButtonGroup.add(post);
        post.setSelected(true);

        JLabel methodLabel = new JLabel(JMeterUtils.getResString("method"));


        JPanel panel = new HorizontalPanel();

        panel.add(protocolLabel);
        panel.add(protocol);
        panel.add(Box.createHorizontalStrut(5));

        panel.add(methodLabel);
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
