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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.jmeter.extractor.HtmlExtractor;
import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * CSS/JQuery Expression Extractor Post-Processor GUI
 * @since 2.9
 */
public class HtmlExtractorGui extends AbstractPostProcessorGui {
    private static final long serialVersionUID = 240L;

    /**
     * This choice means don't explicitly set Implementation and rely on default
     */
    private static final String USE_DEFAULT_EXTRACTOR_IMPL = ""; // $NON-NLS-1$

    private JLabeledTextField expressionField;

    private JLabeledTextField attributeField;
    
    private JLabeledTextField defaultField;

    private JLabeledTextField matchNumberField;

    private JLabeledTextField refNameField;

    private JComboBox<String> extractorImplName;

    private JCheckBox emptyDefaultValue;
    
    public HtmlExtractorGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "html_extractor_title"; //$NON-NLS-1$
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof HtmlExtractor){
            HtmlExtractor htmlExtractor = (HtmlExtractor) el;
            showScopeSettings(htmlExtractor, true);
            expressionField.setText(htmlExtractor.getExpression());
            attributeField.setText(htmlExtractor.getAttribute());
            defaultField.setText(htmlExtractor.getDefaultValue());
            emptyDefaultValue.setSelected(htmlExtractor.isEmptyDefaultValue());
            matchNumberField.setText(htmlExtractor.getMatchNumberAsString());
            refNameField.setText(htmlExtractor.getRefName());
            extractorImplName.setSelectedItem(htmlExtractor.getExtractor());
        }
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        AbstractScopedTestElement extractor = new HtmlExtractor();
        modifyTestElement(extractor);
        return extractor;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement extractor) {
        super.configureTestElement(extractor);
        if (extractor instanceof HtmlExtractor) {
            HtmlExtractor htmlExtractor = (HtmlExtractor) extractor;
            saveScopeSettings(htmlExtractor);
            htmlExtractor.setRefName(refNameField.getText());
            htmlExtractor.setExpression(expressionField.getText());
            htmlExtractor.setAttribute(attributeField.getText());
            htmlExtractor.setDefaultValue(defaultField.getText());
            htmlExtractor.setDefaultEmptyValue(emptyDefaultValue.isSelected());
            htmlExtractor.setMatchNumber(matchNumberField.getText());
            if(extractorImplName.getSelectedIndex()< HtmlExtractor.getImplementations().length) {
                htmlExtractor.setExtractor(HtmlExtractor.getImplementations()[extractorImplName.getSelectedIndex()]);
            } else {
                htmlExtractor.setExtractor(USE_DEFAULT_EXTRACTOR_IMPL);               
            }

        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        extractorImplName.setSelectedItem(HtmlExtractor.DEFAULT_EXTRACTOR);
        expressionField.setText(""); //$NON-NLS-1$        
        attributeField.setText(""); //$NON-NLS-1$
        defaultField.setText(""); //$NON-NLS-1$
        refNameField.setText(""); //$NON-NLS-1$
        emptyDefaultValue.setSelected(false);
        matchNumberField.setText(""); //$NON-NLS-1$
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createScopePanel(true));
        box.add(makeExtractorPanel());
        add(box, BorderLayout.NORTH);
        add(makeParameterPanel(), BorderLayout.CENTER);
    }

    

    private Component makeExtractorPanel() {
        JPanel panel = new HorizontalPanel();
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("html_extractor_type"))); //$NON-NLS-1$
        
        DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
        for (String s : HtmlExtractor.getImplementations()){
            m.addElement(s);
        }
        m.addElement(USE_DEFAULT_EXTRACTOR_IMPL);
        extractorImplName = new JComboBox<>(m);
        extractorImplName.setSelectedItem(HtmlExtractor.DEFAULT_EXTRACTOR);
        JLabel label2 = new JLabel(JMeterUtils.getResString("html_extractor_type")); // $NON-NLS-1$
        label2.setLabelFor(extractorImplName);
        panel.add(label2);
        panel.add(extractorImplName);
        return panel;
    }

    private JPanel makeParameterPanel() {        
        expressionField = new JLabeledTextField(JMeterUtils.getResString("expression_field")); //$NON-NLS-1$
        attributeField = new JLabeledTextField(JMeterUtils.getResString("attribute_field")); //$NON-NLS-1$
        refNameField = new JLabeledTextField(JMeterUtils.getResString("ref_name_field")); //$NON-NLS-1$
        matchNumberField = new JLabeledTextField(JMeterUtils.getResString("match_num_field")); //$NON-NLS-1$

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);
        addField(panel, refNameField, gbc);
        resetContraints(gbc);
        addField(panel, expressionField, gbc);
        resetContraints(gbc);
        addField(panel, attributeField, gbc);
        resetContraints(gbc);
        addField(panel, matchNumberField, gbc);
        resetContraints(gbc);
        gbc.weighty = 1;
        
        defaultField = new JLabeledTextField(JMeterUtils.getResString("default_value_field")); //$NON-NLS-1$
        List<JComponent> item = defaultField.getComponentList();
        panel.add(item.get(0), gbc.clone());
        JPanel p = new JPanel(new BorderLayout());
        p.add(item.get(1), BorderLayout.WEST);
        emptyDefaultValue = new JCheckBox(JMeterUtils.getResString("cssjquery_empty_default_value"));
        emptyDefaultValue.addItemListener(evt -> {
            if(emptyDefaultValue.isSelected()) {
                defaultField.setText("");
            }
            defaultField.setEnabled(!emptyDefaultValue.isSelected());
        });
        p.add(emptyDefaultValue, BorderLayout.CENTER);
        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(p, gbc.clone());
        
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

    // Next line
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
