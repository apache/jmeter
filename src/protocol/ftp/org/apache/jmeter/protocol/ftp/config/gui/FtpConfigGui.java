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

package org.apache.jmeter.protocol.ftp.config.gui;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.config.ConfigTestElement;
import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.protocol.ftp.sampler.FTPSampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @version $Revision$ last updated $Date$
 */
public class FtpConfigGui extends AbstractConfigGui {
	private final static String SERVER = "server";

	private final static String FILENAME = "filename";

	private JTextField server;

	private JTextField filename;

	private boolean displayName = true;

	public FtpConfigGui() {
		this(true);
	}

	public FtpConfigGui(boolean displayName) {
		this.displayName = displayName;
		init();
	}

	public String getLabelResource() {
		return "ftp_sample_title";
	}

	public void configure(TestElement element) {
		super.configure(element);
		server.setText(element.getPropertyAsString(FTPSampler.SERVER));
		filename.setText(element.getPropertyAsString(FTPSampler.FILENAME));
	}

	public TestElement createTestElement() {
		ConfigTestElement element = new ConfigTestElement();
		modifyTestElement(element);
		return element;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement element) {
		configureTestElement(element);
		element.setProperty(FTPSampler.SERVER, server.getText());
		element.setProperty(FTPSampler.FILENAME, filename.getText());
	}

	private JPanel createServerPanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("server"));

		server = new JTextField(10);
		server.setName(SERVER);
		label.setLabelFor(server);

		JPanel serverPanel = new JPanel(new BorderLayout(5, 0));
		serverPanel.add(label, BorderLayout.WEST);
		serverPanel.add(server, BorderLayout.CENTER);
		return serverPanel;
	}

	private JPanel createFilenamePanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("file_to_retrieve"));

		filename = new JTextField(10);
		filename.setName(FILENAME);
		label.setLabelFor(filename);

		JPanel filenamePanel = new JPanel(new BorderLayout(5, 0));
		filenamePanel.add(label, BorderLayout.WEST);
		filenamePanel.add(filename, BorderLayout.CENTER);
		return filenamePanel;
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));

		if (displayName) {
			setBorder(makeBorder());
			add(makeTitlePanel(), BorderLayout.NORTH);
		}

		// MAIN PANEL
		VerticalPanel mainPanel = new VerticalPanel();

		// LOOP
		mainPanel.add(createServerPanel());
		mainPanel.add(createFilenamePanel());

		add(mainPanel, BorderLayout.CENTER);
	}
}
