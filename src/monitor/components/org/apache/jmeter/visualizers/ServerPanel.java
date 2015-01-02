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

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JLabel;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.monitor.util.Stats;

/**
 * The purpose of ServerPanel is to display an unique server and its current
 * status. The server label consist of the protocol, host and port. For example,
 * a system with multiple Tomcat's running on different ports would be different
 * ServerPanel.
 */
public class ServerPanel extends JPanel implements MonitorGuiListener {

    private static final long serialVersionUID = 240L;

    private JLabel serverField;

    private JLabel timestampField;

    /**
     * Preference size for the health icon
     */
    private final Dimension prefsize = new Dimension(25, 75);

    private JLabel healthIcon;

    private JLabel loadIcon;

    /**
     * Health Icons
     */
    private static final ImageIcon HEALTHY = JMeterUtils.getImage("monitor-healthy.gif");

    private static final ImageIcon ACTIVE = JMeterUtils.getImage("monitor-active.gif");

    private static final ImageIcon WARNING = JMeterUtils.getImage("monitor-warning.gif");

    private static final ImageIcon DEAD = JMeterUtils.getImage("monitor-dead.gif");

    /**
     * Load Icons
     */
    private static final ImageIcon LOAD_0 = JMeterUtils.getImage("monitor-load-0.gif");

    private static final ImageIcon LOAD_1 = JMeterUtils.getImage("monitor-load-1.gif");

    private static final ImageIcon LOAD_2 = JMeterUtils.getImage("monitor-load-2.gif");

    private static final ImageIcon LOAD_3 = JMeterUtils.getImage("monitor-load-3.gif");

    private static final ImageIcon LOAD_4 = JMeterUtils.getImage("monitor-load-4.gif");

    private static final ImageIcon LOAD_5 = JMeterUtils.getImage("monitor-load-5.gif");

    private static final ImageIcon LOAD_6 = JMeterUtils.getImage("monitor-load-6.gif");

    private static final ImageIcon LOAD_7 = JMeterUtils.getImage("monitor-load-7.gif");

    private static final ImageIcon LOAD_8 = JMeterUtils.getImage("monitor-load-8.gif");

    private static final ImageIcon LOAD_9 = JMeterUtils.getImage("monitor-load-9.gif");

    private static final ImageIcon LOAD_10 = JMeterUtils.getImage("monitor-load-10.gif");

    // private MonitorModel DATA;

    /**
     * Creates a new server panel for a monitored server
     *
     * @param model
     *            information about the monitored server
     */
    public ServerPanel(MonitorModel model) {
        super();
        // DATA = model;
        init(model);
    }

    /**
     *
     * @deprecated Only for use in unit testing
     */
    @Deprecated
    public ServerPanel() {
        // log.warn("Only for use in unit testing");
    }

    /**
     * Init will create the JLabel widgets for the host, health, load and
     * timestamp.
     *
     * @param model information about the monitored server
     */
    private void init(MonitorModel model) {
        this.setLayout(new FlowLayout());
        serverField = new JLabel(model.getURL());
        this.add(serverField);
        healthIcon = new JLabel(getHealthyImageIcon(model.getHealth()));
        healthIcon.setPreferredSize(prefsize);
        this.add(healthIcon);
        loadIcon = new JLabel(getLoadImageIcon(model.getLoad()));
        this.add(loadIcon);
        timestampField = new JLabel(model.getTimestampString());
        this.add(timestampField);
    }

    /**
     * Static method for getting the right ImageIcon for the health.
     *
     * @param health
     * @return image for the status
     */
    private static ImageIcon getHealthyImageIcon(int health) {
        ImageIcon i = null;
        switch (health) {
        case Stats.HEALTHY:
            i = HEALTHY;
            break;
        case Stats.ACTIVE:
            i = ACTIVE;
            break;
        case Stats.WARNING:
            i = WARNING;
            break;
        case Stats.DEAD:
            i = DEAD;
            break;
        }
        return i;
    }

    /**
     * Static method looks up the right ImageIcon from the load value.
     *
     * @param load
     * @return image for the load
     */
    private static ImageIcon getLoadImageIcon(int load) {
        if (load == 0) {
            return LOAD_0;
        } else if (load > 0 && load <= 10) {
            return LOAD_1;
        } else if (load > 10 && load <= 20) {
            return LOAD_2;
        } else if (load > 20 && load <= 30) {
            return LOAD_3;
        } else if (load > 30 && load <= 40) {
            return LOAD_4;
        } else if (load > 40 && load <= 50) {
            return LOAD_5;
        } else if (load > 50 && load <= 60) {
            return LOAD_6;
        } else if (load > 60 && load <= 70) {
            return LOAD_7;
        } else if (load > 70 && load <= 80) {
            return LOAD_8;
        } else if (load > 80 && load <= 90) {
            return LOAD_9;
        } else {
            return LOAD_10;
        }
    }

    /**
     * Method will update the ServerPanel's health, load, and timestamp. For
     * efficiency, it uses the static method to lookup the images.
     */
    @Override
    public void updateGui(MonitorModel stat) {
        // this.DATA = null;
        // this.DATA = stat;
        loadIcon.setIcon(getLoadImageIcon(stat.getLoad()));
        healthIcon.setIcon(getHealthyImageIcon(stat.getHealth()));
        timestampField.setText(stat.getTimestampString());
        this.updateGui();
    }

    /**
     * update the gui
     */
    @Override
    public void updateGui() {
        this.repaint();
    }
}
