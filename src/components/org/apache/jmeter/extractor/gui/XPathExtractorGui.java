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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.jmeter.assertions.gui.XMLConfPanel;
import org.apache.jmeter.extractor.XPathExtractor;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;
/**
 * GUI for XPathExtractor class.
 */
public class XPathExtractorGui extends AbstractPostProcessorGui {

    private static final long serialVersionUID = 240L;

    private final JLabeledTextField defaultField =
        new JLabeledTextField(JMeterUtils.getResString("default_value_field"));//$NON-NLS-1$

    private final JLabeledTextField xpathQueryField =
        new JLabeledTextField(JMeterUtils.getResString("xpath_extractor_query"));//$NON-NLS-1$

    private final JLabeledTextField matchNumberField =
            new JLabeledTextField(JMeterUtils.getResString("match_num_field"));//$NON-NLS-1$

    private final JLabeledTextField refNameField =
        new JLabeledTextField(JMeterUtils.getResString("ref_name_field"));//$NON-NLS-1$

    // Should we return fragment as text, rather than text of fragment?
    private final JCheckBox getFragment =
        new JCheckBox(JMeterUtils.getResString("xpath_extractor_fragment"));//$NON-NLS-1$

    private final XMLConfPanel xml = new XMLConfPanel();

    @Override
    public String getLabelResource() {
        return "xpath_extractor_title"; //$NON-NLS-1$
    }

    public XPathExtractorGui(){
        super();
        init();
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        XPathExtractor xpe = (XPathExtractor) el;
        showScopeSettings(xpe,true);
        xpathQueryField.setText(xpe.getXPathQuery());
        defaultField.setText(xpe.getDefaultValue());
        refNameField.setText(xpe.getRefName());
        matchNumberField.setText(xpe.getMatchNumberAsString());
        getFragment.setSelected(xpe.getFragment());
        xml.configure(xpe);
    }


    @Override
    public TestElement createTestElement() {
        XPathExtractor extractor = new XPathExtractor();
        modifyTestElement(extractor);
        return extractor;
    }

    @Override
    public void modifyTestElement(TestElement extractor) {
        super.configureTestElement(extractor);
        if ( extractor instanceof XPathExtractor){
            XPathExtractor xpath = (XPathExtractor)extractor;
            saveScopeSettings(xpath);
            xpath.setDefaultValue(defaultField.getText());
            xpath.setRefName(refNameField.getText());
            xpath.setMatchNumber(matchNumberField.getText());
            xpath.setXPathQuery(xpathQueryField.getText());
            xpath.setFragment(getFragment.isSelected());
            xml.modifyTestElement(xpath);
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        xpathQueryField.setText(""); // $NON-NLS-1$
        defaultField.setText(""); // $NON-NLS-1$
        refNameField.setText(""); // $NON-NLS-1$
        matchNumberField.setText(XPathExtractor.DEFAULT_VALUE_AS_STRING); // $NON-NLS-1$
        xml.setDefaultValues();
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createScopePanel(true, true, true));
        xml.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), JMeterUtils
                .getResString("xpath_assertion_option"))); //$NON-NLS-1$
        box.add(xml);
        box.add(getFragment);
        box.add(makeParameterPanel());
        add(box, BorderLayout.NORTH);
    }


    private JPanel makeParameterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);
        addField(panel, refNameField, gbc);
        resetContraints(gbc);
        addField(panel, xpathQueryField, gbc);
        resetContraints(gbc);
        addField(panel, matchNumberField, gbc);
        resetContraints(gbc);
        gbc.weighty = 1;
        addField(panel, defaultField, gbc);
        return panel;
    }

    private void addField(JPanel panel, JLabeledTextField field, GridBagConstraints gbc) {
        List<JComponent> item = field.getComponentList();
        panel.add(item.get(0), gbc.clone());
        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add(item.get(1), gbc.clone());
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
