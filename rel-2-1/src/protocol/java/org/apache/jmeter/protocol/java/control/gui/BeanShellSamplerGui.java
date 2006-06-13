// $Header$
/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

package org.apache.jmeter.protocol.java.control.gui;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.jmeter.protocol.java.sampler.BeanShellSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

/**
 * @version $Revision$ $Date$
 */
public class BeanShellSamplerGui extends AbstractSamplerGui {

	private JTextField filename;// script file name (if present)

	private JTextField parameters;// parameters to pass to script file (or
									// script)

	private JTextArea scriptField;// script area

	public BeanShellSamplerGui() {
		init();
	}

	public void configure(TestElement element) {
		scriptField.setText(element.getPropertyAsString(BeanShellSampler.SCRIPT));
		filename.setText(element.getPropertyAsString(BeanShellSampler.FILENAME));
		parameters.setText(element.getPropertyAsString(BeanShellSampler.PARAMETERS));
		super.configure(element);
	}

	public TestElement createTestElement() {
		BeanShellSampler sampler = new BeanShellSampler();
		modifyTestElement(sampler);
		return sampler;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement te) {
		te.clear();
		this.configureTestElement(te);
		te.setProperty(BeanShellSampler.SCRIPT, scriptField.getText());
		te.setProperty(BeanShellSampler.FILENAME, filename.getText());
		te.setProperty(BeanShellSampler.PARAMETERS, parameters.getText());
	}

	public String getLabelResource() {
		return "bsh_sampler_title";
	}

	private JPanel createFilenamePanel()// TODO ought to be a FileChooser ...
	{
		JLabel label = new JLabel(JMeterUtils.getResString("bsh_script_file"));

		filename = new JTextField(10);
		filename.setName(BeanShellSampler.FILENAME);
		label.setLabelFor(filename);

		JPanel filenamePanel = new JPanel(new BorderLayout(5, 0));
		filenamePanel.add(label, BorderLayout.WEST);
		filenamePanel.add(filename, BorderLayout.CENTER);
		return filenamePanel;
	}

	private JPanel createParameterPanel() {
		JLabel label = new JLabel(JMeterUtils.getResString("bsh_script_parameters"));

		parameters = new JTextField(10);
		parameters.setName(BeanShellSampler.PARAMETERS);
		label.setLabelFor(parameters);

		JPanel parameterPanel = new JPanel(new BorderLayout(5, 0));
		parameterPanel.add(label, BorderLayout.WEST);
		parameterPanel.add(parameters, BorderLayout.CENTER);
		return parameterPanel;
	}

	private void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		Box box = Box.createVerticalBox();
		box.add(makeTitlePanel());
		box.add(createParameterPanel());
		box.add(createFilenamePanel());
		add(box, BorderLayout.NORTH);

		JPanel panel = createScriptPanel();
		add(panel, BorderLayout.CENTER);
		// Don't let the input field shrink too much
		add(Box.createVerticalStrut(panel.getPreferredSize().height), BorderLayout.WEST);
	}

	private JPanel createScriptPanel() {
		scriptField = new JTextArea();
		scriptField.setRows(4);
		scriptField.setLineWrap(true);
		scriptField.setWrapStyleWord(true);

		JLabel label = new JLabel(JMeterUtils.getResString("bsh_script"));
		label.setLabelFor(scriptField);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);
		panel.add(new JScrollPane(scriptField), BorderLayout.CENTER);
		return panel;
	}
}
