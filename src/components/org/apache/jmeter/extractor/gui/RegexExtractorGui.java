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

package org.apache.jmeter.extractor.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.JPanel;

import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * @version $Revision$ on $Date$
 */
public class RegexExtractorGui extends AbstractPostProcessorGui
{
    private JLabeledTextField regexField;
    private JLabeledTextField templateField;
    private JLabeledTextField defaultField;
    private JLabeledTextField matchNumberField;
    private JLabeledTextField refNameField;
    
    public RegexExtractorGui()
    {
        super();
        init();
    }

    public String getLabelResource()
    {
        return "regex_extractor_title";
    }
    
    public void configure(TestElement el)
    {
        super.configure(el);
        regexField.setText(el.getPropertyAsString(RegexExtractor.REGEX));
        templateField.setText(el.getPropertyAsString(RegexExtractor.TEMPLATE));
        defaultField.setText(el.getPropertyAsString(RegexExtractor.DEFAULT));
        matchNumberField.setText(
            el.getPropertyAsString(RegexExtractor.MATCH_NUMBER));
        refNameField.setText(el.getPropertyAsString(RegexExtractor.REFNAME));
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    public TestElement createTestElement()
    {
        RegexExtractor extractor = new RegexExtractor();
        modifyTestElement(extractor);
        return extractor;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    public void modifyTestElement(TestElement extractor)
    {
        super.configureTestElement(extractor);
        extractor.setProperty(
            RegexExtractor.MATCH_NUMBER,
            matchNumberField.getText());
        if(extractor instanceof RegexExtractor)
        {
            RegexExtractor regex = (RegexExtractor)extractor;
            regex.setRefName(refNameField.getText());
            regex.setRegex(regexField.getText());
            regex.setTemplate(templateField.getText());
            regex.setDefaultValue(defaultField.getText());
        }
    }
    
    private void init()
    {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        
        add(makeTitlePanel(),BorderLayout.NORTH);
        add(makeParameterPanel(),BorderLayout.CENTER);
    }
    
    private JPanel makeParameterPanel()
    {
        regexField =
            new JLabeledTextField(JMeterUtils.getResString("regex_field"));
        templateField =
            new JLabeledTextField(JMeterUtils.getResString("template_field"));
        defaultField =
            new JLabeledTextField(
                JMeterUtils.getResString("default_value_field"));
        refNameField =
            new JLabeledTextField(JMeterUtils.getResString("ref_name_field"));
        matchNumberField =
            new JLabeledTextField(JMeterUtils.getResString("match_num_field"));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);
        addField(panel,refNameField,gbc);
        resetContraints(gbc);
        addField(panel,regexField,gbc);
        resetContraints(gbc);
        addField(panel,templateField,gbc);
        resetContraints(gbc);
        addField(panel,matchNumberField,gbc);
        resetContraints(gbc);
        gbc.weighty = 1;
        addField(panel,defaultField,gbc);
        return panel;
    }
    
    private void addField(
        JPanel panel,
        JLabeledTextField field,
        GridBagConstraints gbc)
    {
        List item = field.getComponentList();
        panel.add((Component)item.get(0),gbc.clone());
        gbc.gridx++;
        gbc.weightx = 1;;
        panel.add((Component)item.get(1),gbc.clone());
    }
    
    private void resetContraints(GridBagConstraints gbc)
    {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
    }
    
    private void initConstraints(GridBagConstraints gbc)
    {
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
