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
 */
package org.apache.jmeter.visualizers;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.util.JMeterUtils;

/**
 * The health panel is responsible for showing the health of the servers. It
 * only uses the most current information to show the status.
 */
public class MonitorHealthPanel extends JPanel implements MonitorListener, Clearable {
    private static final long serialVersionUID = 240L;

    private final Map<String, ServerPanel> serverPanelMap = new HashMap<String, ServerPanel>();

    private JPanel servers = null;

    private final MonitorAccumModel model;

    private JScrollPane serverScrollPane = null;

    // NOTUSED Font plainText = new Font("plain", Font.PLAIN, 9);
    // These must not be static, otherwise Language change does not work
    private final String INFO_H = JMeterUtils.getResString("monitor_equation_healthy"); //$NON-NLS-1$

    private final String INFO_A = JMeterUtils.getResString("monitor_equation_active"); //$NON-NLS-1$

    private final String INFO_W = JMeterUtils.getResString("monitor_equation_warning"); //$NON-NLS-1$

    private final String INFO_D = JMeterUtils.getResString("monitor_equation_dead"); //$NON-NLS-1$

    private final String INFO_LOAD = JMeterUtils.getResString("monitor_equation_load"); //$NON-NLS-1$

    /**
     *
     * @deprecated Only for use in unit testing
     */
    @Deprecated
    public MonitorHealthPanel() {
        // log.warn("Only for use in unit testing");
        model = null;
    }

    /**
     * @param model model to use
     *
     */
    public MonitorHealthPanel(MonitorAccumModel model) {
        this.model = model;
        this.model.addListener(this);
        init();
    }

    /**
     * init is responsible for creating the necessary legends and information
     * for the health panel.
     */
    private void init() {// called from ctor, so must not be overridable
        this.setLayout(new BorderLayout());
        ImageIcon legend = JMeterUtils.getImage("monitor-legend.gif"); // I18N: Contains fixed English text ...
        JLabel label = new JLabel(legend);
        label.setPreferredSize(new Dimension(550, 25));
        this.add(label, BorderLayout.NORTH);

        this.servers = new JPanel();
        this.servers.setLayout(new BoxLayout(servers, BoxLayout.Y_AXIS));
        this.servers.setAlignmentX(Component.LEFT_ALIGNMENT);

        serverScrollPane = new JScrollPane(this.servers);
        serverScrollPane.setPreferredSize(new Dimension(300, 300));
        this.add(serverScrollPane, BorderLayout.CENTER);

        // the equations
        String eqstring1 = " " + INFO_H + "   |   " + INFO_A;
        String eqstring2 = " " + INFO_W + "   |   " + INFO_D;
        String eqstring3 = " " + INFO_LOAD;
        JLabel eqs = new JLabel();
        eqs.setLayout(new BorderLayout());
        eqs.setPreferredSize(new Dimension(500, 60));
        eqs.add(new JLabel(eqstring1), BorderLayout.NORTH);
        eqs.add(new JLabel(eqstring2), BorderLayout.CENTER);
        eqs.add(new JLabel(eqstring3), BorderLayout.SOUTH);
        this.add(eqs, BorderLayout.SOUTH);
    }

    /**
     *
     * @param model information about monitored server
     */
    @Override
    public void addSample(MonitorModel model) {
        if (serverPanelMap.containsKey(model.getURL())) {
            ServerPanel pane = null;
            if (serverPanelMap.get(model.getURL()) != null) {
                pane = serverPanelMap.get((model.getURL()));
            } else {
                pane = new ServerPanel(model);
                serverPanelMap.put(model.getURL(), pane);
            }
            pane.updateGui(model);
        } else {
            ServerPanel newpane = new ServerPanel(model);
            serverPanelMap.put(model.getURL(), newpane);
            this.servers.add(newpane);
            newpane.updateGui(model);
        }
        this.servers.updateUI();
    }

    /**
     * clear will clear the hashmap, remove all ServerPanels from the servers
     * pane, and update the ui.
     */
    @Override
    public void clearData() {
        this.serverPanelMap.clear();
        this.servers.removeAll();
        this.servers.updateUI();
    }
}
