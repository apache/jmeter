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
package org.apache.jmeter.extractor.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.apache.jmeter.extractor.XPathExtractor;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
/**
 * GUI for XPathExtractor class.
 */
 /* This file is inspired by RegexExtractor.
 * author <a href="mailto:hpaluch@gitus.cz">Henryk Paluch</a>
 *            of <a href="http://www.gitus.com">Gitus a.s.</a>
 * See Bugzilla: 37183
 */
public class XPathExtractorGui extends AbstractPostProcessorGui {

	private JLabeledTextField defaultField;

	private JLabeledTextField xpathQueryField;

	private JLabeledTextField refNameField;
	
	private JCheckBox tolerant;
    
    public String getLabelResource() {
        return "xpath_extractor_title";
    }

    public XPathExtractorGui(){
		super();
		init();        
    }    
    
    public void configure(TestElement el) {
		super.configure(el);
		xpathQueryField.setText(el.getPropertyAsString(XPathExtractor.XPATH_QUERY));
		defaultField.setText(el.getPropertyAsString(XPathExtractor.DEFAULT));
		refNameField.setText(el.getPropertyAsString(XPathExtractor.REFNAME));
		tolerant.setSelected(el.getPropertyAsBoolean(XPathExtractor.TOLERANT));
	}

    
    public TestElement createTestElement() {
		XPathExtractor extractor = new XPathExtractor();
		modifyTestElement(extractor);
		return extractor;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
     */
    public void modifyTestElement(TestElement extractor) {
        super.configureTestElement(extractor);
        if ( extractor instanceof XPathExtractor){
            XPathExtractor xpath = (XPathExtractor)extractor;
            xpath.setDefaultValue(defaultField.getText());
            xpath.setRefName(refNameField.getText());
            xpath.setXPathQuery(xpathQueryField.getText());
            xpath.setTolerant(tolerant.isSelected());
        }
    }

    /**
     * Implements JMeterGUIComponent.clear
     */
    public void clear() {
        super.clear();
        
        xpathQueryField.setText(""); // $NON-NLS-1$
        defaultField.setText(""); // $NON-NLS-1$
        refNameField.setText(""); // $NON-NLS-1$
        tolerant.setSelected(false);
    }

	private void init() {
		setLayout(new BorderLayout());
		setBorder(makeBorder());

		Box box = Box.createVerticalBox();
		box.add(makeTitlePanel());
		tolerant = new JCheckBox(JMeterUtils.getResString("xpath_extractor_tolerant"));
		box.add(tolerant);
		add(box, BorderLayout.NORTH);
		add(makeParameterPanel(), BorderLayout.CENTER);
	}


	private JPanel makeParameterPanel() {
		xpathQueryField = new JLabeledTextField(JMeterUtils.getResString("xpath_extractor_query"));
		defaultField = new JLabeledTextField(JMeterUtils.getResString("default_value_field"));
		refNameField = new JLabeledTextField(JMeterUtils.getResString("ref_name_field"));

		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		initConstraints(gbc);
		addField(panel, refNameField, gbc);
		resetContraints(gbc);
		addField(panel, xpathQueryField, gbc);
		resetContraints(gbc);
		gbc.weighty = 1;
		addField(panel, defaultField, gbc);
		return panel;
	}

	private void addField(JPanel panel, JLabeledTextField field, GridBagConstraints gbc) {
		List item = field.getComponentList();
		panel.add((Component) item.get(0), gbc.clone());
		gbc.gridx++;
		gbc.weightx = 1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add((Component) item.get(1), gbc.clone());
	}

	private void resetContraints(GridBagConstraints gbc) {
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.weightx = 0;
        gbc.fill=GridBagConstraints.NONE;
	}

	private void initConstraints(GridBagConstraints gbc) {
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
	}        
}