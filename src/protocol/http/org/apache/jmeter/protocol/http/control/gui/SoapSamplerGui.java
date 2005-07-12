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

package org.apache.jmeter.protocol.http.control.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import org.apache.jmeter.protocol.http.sampler.SoapSampler;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * @version $Revision$ on $Date$
 */
public class SoapSamplerGui extends AbstractSamplerGui {
	private JLabeledTextField urlField;

	private JLabeledTextArea soapXml;

	public SoapSamplerGui() {
		init();
	}

	public String getLabelResource() {
		return "soap_sampler_title";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		SoapSampler sampler = new SoapSampler();
		modifyTestElement(sampler);
		return sampler;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement s) {
		this.configureTestElement(s);
		if (s instanceof SoapSampler) {
			SoapSampler sampler = (SoapSampler) s;
			sampler.setURLData(urlField.getText());
			sampler.setXmlData(soapXml.getText());
		}
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);

		urlField = new JLabeledTextField(JMeterUtils.getResString("url"), 10);
		soapXml = new JLabeledTextArea(JMeterUtils.getResString("soap_data_title"), null);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(urlField, BorderLayout.NORTH);
		mainPanel.add(soapXml, BorderLayout.CENTER);

		add(mainPanel, BorderLayout.CENTER);
	}

	public void configure(TestElement el) {
		super.configure(el);
		SoapSampler sampler = (SoapSampler) el;
		urlField.setText(sampler.getURLData());
		soapXml.setText(sampler.getXmlData());
	}

	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
}
