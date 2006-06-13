/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.protocol.http.config.gui.MultipartUrlConfigGui;
import org.apache.jmeter.protocol.http.config.gui.UrlConfigGui;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerFactory;
import org.apache.jmeter.protocol.http.sampler.HTTPSamplerBase;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

//For unit tests, @see TestHttpTestSampleGui

/**
 * HTTP Sampler GUI
 * 
 */
public class HttpTestSampleGui extends AbstractSamplerGui {
	private UrlConfigGui urlConfigGui;

	private JCheckBox getImages;

	private JCheckBox isMon;

	public HttpTestSampleGui() {
		init();
	}

	public void configure(TestElement element) {
		super.configure(element);
		urlConfigGui.configure(element);
		getImages.setSelected(((HTTPSamplerBase) element).isImageParser());
		isMon.setSelected(((HTTPSamplerBase) element).isMonitor());
	}

	public TestElement createTestElement() {
		HTTPSamplerBase sampler = HTTPSamplerFactory.newInstance();// create default sampler
		modifyTestElement(sampler);
		return sampler;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement sampler) {
		TestElement el = urlConfigGui.createTestElement();
		sampler.clear();
		sampler.addTestElement(el);
		if (getImages.isSelected()) {
			((HTTPSamplerBase) sampler).setImageParser(true);
		} else {
			sampler.removeProperty(HTTPSamplerBase.IMAGE_PARSER);// TODO - why?
		}
        ((HTTPSamplerBase) sampler).setMonitor(isMon.isSelected());
		this.configureTestElement(sampler);
	}

	public String getLabelResource() {
		return "web_testing_title"; // $NON-NLS-1$
	}

	protected void init() {
		setLayout(new BorderLayout(0, 5));
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);

		// URL CONFIG
		urlConfigGui = new MultipartUrlConfigGui();
		add(urlConfigGui, BorderLayout.CENTER);

		// OPTIONAL TASKS
		add(createOptionalTasksPanel(), BorderLayout.SOUTH);
	}

	private JPanel createOptionalTasksPanel() {
		// OPTIONAL TASKS
		HorizontalPanel optionalTasksPanel = new HorizontalPanel();
		optionalTasksPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
				.getResString("optional_tasks"))); // $NON-NLS-1$

		// RETRIEVE IMAGES
		JPanel retrieveImagesPanel = new JPanel();
		getImages = new JCheckBox(JMeterUtils.getResString("web_testing_retrieve_images")); // $NON-NLS-1$
		retrieveImagesPanel.add(getImages);
		JPanel isMonitorPanel = new JPanel();
		isMon = new JCheckBox(JMeterUtils.getResString("monitor_is_title")); // $NON-NLS-1$
		isMonitorPanel.add(isMon);
		optionalTasksPanel.add(retrieveImagesPanel);
		optionalTasksPanel.add(isMonitorPanel);
		return optionalTasksPanel;
	}

	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#clear()
	 */
	public void clear() {
		super.clear();
		getImages.setSelected(false);
		urlConfigGui.clear();
	}
}
