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

package org.apache.jmeter.reporters.gui;

import java.awt.BorderLayout;

import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;

/**
 * Create a summariser test element.
 * 
 * Note: This is not really a PostProcessor, but that seems to be the closest of
 * the existing types.
 * 
 */
public class SummariserGui extends AbstractPostProcessorGui implements Clearable {

	public SummariserGui() {
		super();
		init();
	}

	public String getLabelResource() {
		return "summariser_title";
	}

	public void configure(TestElement el) {
		super.configure(el);
	}

	/**
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
	 */
	public TestElement createTestElement() {
		Summariser summariser = new Summariser();
		modifyTestElement(summariser);
		return summariser;
	}

	/**
	 * Modifies a given TestElement to mirror the data in the gui components.
	 * 
	 * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
	 */
	public void modifyTestElement(TestElement summariser) {
		super.configureTestElement(summariser);
	}

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		add(makeTitlePanel(), BorderLayout.NORTH);
	}
}
