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

package org.apache.jmeter.protocol.http.config.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

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
import org.apache.jmeter.protocol.http.gui.HTTPArgumentsPanel;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.TestElementProperty;
import org.apache.jmeter.util.JMeterUtils;


/**
 * @author    Michael Stover
 * @version   $Revision$
 */
public class UrlConfigGui extends JPanel
{
    protected HTTPArgumentsPanel argsPanel;
    private static String DOMAIN = "domain";
    private static String PORT = "port";
    private static String PROTOCOL = "protocol";
    private static String PATH = "path";
    private static String FOLLOW_REDIRECTS = "follow_redirects";
    private static String AUTO_REDIRECTS = "auto_redirects";
    private static String USE_KEEPALIVE = "use_keepalive";

    private JTextField domain;
    private JTextField port;
    private JTextField protocol;
    private JTextField path;
    private JCheckBox followRedirects;
    private JCheckBox autoRedirects;
    private JCheckBox useKeepAlive;
    private JRadioButton post;
    private JRadioButton get;

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
    
    public void clear()
    {
        domain.setText("");
        followRedirects.setSelected(true);
        autoRedirects.setSelected(false);
        get.setSelected(true);
        path.setText("");
        port.setText("");
        protocol.setText("");
        useKeepAlive.setSelected(true);
        argsPanel.clear();
       
    }

    public TestElement createTestElement()
    {
        ConfigTestElement element = new ConfigTestElement();

        this.configureTestElement(element);
        Arguments args = (Arguments) argsPanel.createTestElement();

        HTTPArgument.convertArgumentsToHTTP(args);
        element.setProperty(
            new TestElementProperty(HTTPSampler.ARGUMENTS, args));
        element.setProperty(HTTPSampler.DOMAIN, domain.getText());
        element.setProperty(HTTPSampler.PORT, port.getText());
        element.setProperty(HTTPSampler.PROTOCOL, protocol.getText());
        element.setProperty(HTTPSampler.METHOD,
                (post.isSelected() ? "POST" : "GET"));
        element.setProperty(HTTPSampler.PATH, path.getText());
        element.setProperty(HTTPSampler.ENCODED_PATH,path.getText());
        element.setProperty(new BooleanProperty(HTTPSampler.FOLLOW_REDIRECTS,
                followRedirects.isSelected()));
        element.setProperty(new BooleanProperty(HTTPSampler.AUTO_REDIRECTS,
                autoRedirects.isSelected()));
        element.setProperty(new BooleanProperty(HTTPSampler.USE_KEEPALIVE,
                useKeepAlive.isSelected()));
        return element;
    }

// Does not appear to be used
//    public void configureSampler(HTTPSampler sampler)
//    {
//        sampler.setArguments((Arguments) argsPanel.createTestElement());
//        sampler.setDomain(domain.getText());
//        sampler.setProtocol(protocol.getText());
//        sampler.setPath(path.getText());
//        sampler.setFollowRedirects(followRedirects.isSelected());
//        sampler.setDelegateRedirects(autoRedirects.isSelected());
//        sampler.setUseKeepAlive(useKeepAlive.isSelected());
//        if (port.getText().length() > 0)
//        {
//            sampler.setPort(Integer.parseInt(port.getText()));
//        }
//        sampler.setMethod((post.isSelected() ? "POST" : "GET"));
//    }
    
    /**
     * Set the text, etc. in the UI.
     *
     * @param el contains the data to be displayed
     */
    public void configure(TestElement el)
    {
        setName(el.getPropertyAsString(TestElement.NAME));
        argsPanel.configure(
            (TestElement) el
                .getProperty(HTTPSampler.ARGUMENTS)
                .getObjectValue());
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
        followRedirects.setSelected(
            ((AbstractTestElement) el).getPropertyAsBoolean(
                HTTPSampler.FOLLOW_REDIRECTS));

        autoRedirects.setSelected(
                ((AbstractTestElement) el).getPropertyAsBoolean(
                    HTTPSampler.AUTO_REDIRECTS));
        useKeepAlive.setSelected(
            ((AbstractTestElement) el).getPropertyAsBoolean(
                HTTPSampler.USE_KEEPALIVE));
    }

    protected void init()
    {
        this.setLayout(new BorderLayout());

        JPanel webServerPanel = new JPanel();

        webServerPanel.setLayout(new BorderLayout());
        webServerPanel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_server")));
        webServerPanel.add(getDomainPanel(), BorderLayout.NORTH);
        webServerPanel.add(getPortPanel(), BorderLayout.WEST);

        JPanel webRequestPanel = new JPanel();

        webRequestPanel.setLayout(new BorderLayout());
        webRequestPanel.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_request")));
        JPanel northPanel = new JPanel(new BorderLayout());

        northPanel.add(getProtocolAndMethodPanel(), BorderLayout.NORTH);
        northPanel.add(getPathPanel(), BorderLayout.SOUTH);
        webServerPanel.add(northPanel, BorderLayout.SOUTH);
        webRequestPanel.add(getParameterPanel(), BorderLayout.CENTER);

        this.add(webServerPanel, BorderLayout.NORTH);
        this.add(webRequestPanel, BorderLayout.CENTER);
    }

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

    protected JPanel getDomainPanel()
    {
        domain = new JTextField(20);
        domain.setName(DOMAIN);

        JLabel label =
            new JLabel(JMeterUtils.getResString("web_server_domain"));
        label.setLabelFor(domain);
        
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(domain, BorderLayout.CENTER);
        return panel;
    }

    /**
     * This method defines the Panel for the HTTP path, 'Follow Redirects' and
     * 'Use KeepAlive' elements.
     *
     * @return JPanel The Panel for the path, 'Follow Redirects' and
     *         'Use KeepAlive' elements.
     */
    protected Component getPathPanel()
    {
        path = new JTextField(15);
        path.setName(PATH);

        JLabel label = new JLabel(JMeterUtils.getResString("path"));
        label.setLabelFor(path);

        autoRedirects =
            new JCheckBox(JMeterUtils.getResString("follow_redirects_auto"));
        autoRedirects.setName(AUTO_REDIRECTS);
        autoRedirects.setSelected(false);// will be reset by configure(TestElement)

        followRedirects =
            new JCheckBox(JMeterUtils.getResString("follow_redirects"));
        followRedirects.setName(FOLLOW_REDIRECTS);
        followRedirects.setSelected(true);

        useKeepAlive = new JCheckBox(JMeterUtils.getResString("use_keepalive"));
        useKeepAlive.setName(USE_KEEPALIVE);
        useKeepAlive.setSelected(true);

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(label);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(path);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(autoRedirects);
        panel.add(followRedirects);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(useKeepAlive);
        panel.setMinimumSize(panel.getPreferredSize());
        return panel;
    }

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


        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        panel.add(protocolLabel);
        panel.add(protocol);
        panel.add(Box.createHorizontalStrut(5));

        panel.add(methodLabel);
        panel.add(get);
        panel.add(post);
        panel.setMinimumSize(panel.getPreferredSize());
        return panel;
    }

    protected JPanel getParameterPanel()
    {
        argsPanel = new HTTPArgumentsPanel();

        return argsPanel;
    }
}
