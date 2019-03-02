/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.jmeter.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Common server panel implementation for use with HTTP, TCP etc samplers
 */
public class ServerPanel extends JPanel {

    private static final long serialVersionUID = -2749091243070619669L;

    private JTextField domain;

    private JTextField port;

    private JTextField connectTimeOut;

    private JTextField responseTimeOut;

    /**
     * create the target server panel.
     * <ul>
     * <li>Server IP</li>
     * <li>Server Port</li>
     * <li>Connect Timeout</li>
     * <li>Response Timeout</li>
     * </ul>
     */
    public ServerPanel() {
        init();
    }

    /**
     * clear all the fields
     */
    public void clear() {
        domain.setText("");
        port.setText("");
        connectTimeOut.setText("");
        responseTimeOut.setText("");
    }

    public String getServer(){
        return domain.getText();
    }

    public void setServer(String value){
        domain.setText(value);
    }

    public String getPort(){
        return port.getText();
    }

    public void setPort(String value){
        port.setText(value);
    }

    public String getConnectTimeout(){
        return connectTimeOut.getText();
    }

    public void setConnectTimeout(String value){
        connectTimeOut.setText(value);
    }

    public String getResponseTimeout(){
        return responseTimeOut.getText();
    }

    public void setResponseTimeout(String value){
        responseTimeOut.setText(value);
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout(5, 0));
        // Target server panel
        JPanel webServerPanel = new HorizontalPanel();
        webServerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("target_server"))); // $NON-NLS-1$
        final JPanel domainPanel = getDomainPanel();
        final JPanel portPanel = getPortPanel();
        webServerPanel.add(domainPanel, BorderLayout.CENTER);
        webServerPanel.add(portPanel, BorderLayout.EAST);

        JPanel timeOut = new HorizontalPanel();
        timeOut.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                JMeterUtils.getResString("web_server_timeout_title"))); // $NON-NLS-1$
        final JPanel connPanel = getConnectTimeOutPanel();
        final JPanel reqPanel = getResponseTimeOutPanel();
        timeOut.add(connPanel);
        timeOut.add(reqPanel);

        JPanel webServerTimeoutPanel = new VerticalPanel();
        webServerTimeoutPanel.add(webServerPanel, BorderLayout.CENTER);
        webServerTimeoutPanel.add(timeOut, BorderLayout.EAST);

        JPanel bigPanel = new VerticalPanel();
        bigPanel.add(webServerTimeoutPanel);

        add(bigPanel);
    }

    private JPanel getDomainPanel() {
        domain = new JTextField(20);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_domain")); // $NON-NLS-1$
        label.setLabelFor(domain);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(domain, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getPortPanel() {
        port = new JTextField(4);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_port")); // $NON-NLS-1$
        label.setLabelFor(port);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(port, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getConnectTimeOutPanel() {
        connectTimeOut = new JTextField(4);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_timeout_connect")); // $NON-NLS-1$
        label.setLabelFor(connectTimeOut);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(connectTimeOut, BorderLayout.CENTER);

        return panel;
    }

    private JPanel getResponseTimeOutPanel() {
        responseTimeOut = new JTextField(4);

        JLabel label = new JLabel(JMeterUtils.getResString("web_server_timeout_response")); // $NON-NLS-1$
        label.setLabelFor(responseTimeOut);

        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(label, BorderLayout.WEST);
        panel.add(responseTimeOut, BorderLayout.CENTER);

        return panel;
    }

}
