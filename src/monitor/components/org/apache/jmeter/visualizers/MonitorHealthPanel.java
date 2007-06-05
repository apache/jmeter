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
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.util.HashMap;

import javax.swing.JScrollPane;

import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.samplers.Clearable;

/**
 * The health panel is responsible for showing the health of the servers. It
 * only uses the most current information to show the status.
 */
public class MonitorHealthPanel extends JPanel implements MonitorListener, Clearable {
	private HashMap SERVERMAP = new HashMap();

	private JPanel SERVERS = null;

	private MonitorAccumModel MODEL;

	private JScrollPane SCROLL = null;

	// NOTUSED Font plainText = new Font("plain", Font.PLAIN, 9);
	public static final String INFO_H = JMeterUtils.getResString("monitor_equation_healthy"); //$NON-NLS-1$

	public static final String INFO_A = JMeterUtils.getResString("monitor_equation_active"); //$NON-NLS-1$

	public static final String INFO_W = JMeterUtils.getResString("monitor_equation_warning"); //$NON-NLS-1$

	public static final String INFO_D = JMeterUtils.getResString("monitor_equation_dead"); //$NON-NLS-1$

	public static final String INFO_LOAD = JMeterUtils.getResString("monitor_equation_load"); //$NON-NLS-1$

	/**
	 * 
	 * @deprecated Only for use in unit testing
	 */
	public MonitorHealthPanel() {
		// log.warn("Only for use in unit testing");
	}

	/**
	 * 
	 */
	public MonitorHealthPanel(MonitorAccumModel model) {
		this.MODEL = model;
		this.MODEL.addListener(this);
		init();
	}

	/**
	 * init is responsible for creating the necessary legends and information
	 * for the health panel.
	 */
	protected void init() {
		this.setLayout(new BorderLayout());
		ImageIcon legend = JMeterUtils.getImage("monitor-legend.gif"); //$NON-NLS-1$
		JLabel label = new JLabel(legend);
		label.setPreferredSize(new Dimension(550, 25));
		this.add(label, BorderLayout.NORTH);

		this.SERVERS = new JPanel();
		this.SERVERS.setLayout(new BoxLayout(SERVERS, BoxLayout.Y_AXIS));
		this.SERVERS.setAlignmentX(Component.LEFT_ALIGNMENT);

		SCROLL = new JScrollPane(this.SERVERS);
		SCROLL.setPreferredSize(new Dimension(300, 300));
		this.add(SCROLL, BorderLayout.CENTER);

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
	 * @param model
	 */
	public void addSample(MonitorModel model) {
		if (SERVERMAP.containsKey(model.getURL())) {
			ServerPanel pane = null;
			if (SERVERMAP.get(model.getURL()) != null) {
				pane = (ServerPanel) SERVERMAP.get((model.getURL()));
			} else {
				pane = new ServerPanel(model);
				SERVERMAP.put(model.getURL(), pane);
			}
			pane.updateGui(model);
		} else {
			ServerPanel newpane = new ServerPanel(model);
			SERVERMAP.put(model.getURL(), newpane);
			this.SERVERS.add(newpane);
			newpane.updateGui(model);
		}
		this.SERVERS.updateUI();
	}

	/**
	 * clear will clear the hashmap, remove all ServerPanels from the servers
	 * pane, and update the ui.
	 */
	public void clearData() {
		this.SERVERMAP.clear();
		this.SERVERS.removeAll();
		this.SERVERS.updateUI();
	}
}
