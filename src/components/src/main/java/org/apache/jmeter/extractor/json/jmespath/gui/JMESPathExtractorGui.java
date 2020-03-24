/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jmeter.extractor.json.jmespath.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.jmeter.extractor.json.jmespath.JMESPathExtractor;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * GUI for {@link JMESPathExtractor}
 *
 * @since 5.2
 */
@GUIMenuSortOrder(2)
@TestElementMetadata(labelResource = "jmes_extractor_title")
public class JMESPathExtractorGui extends AbstractPostProcessorGui {

    private static final long serialVersionUID = -4825532539405119033L;
    private JLabeledTextField defaultValueField;
    private JLabeledTextField jmesPathExpressionField;
    private JLabeledTextField refNameField;
    private JLabeledTextField matchNumberField;

    public JMESPathExtractorGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "jmes_extractor_title";//$NON-NLS-1$
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        JMESPathExtractor config = (JMESPathExtractor) element;
        showScopeSettings(config, true);
        refNameField.setText(config.getRefName());
        jmesPathExpressionField.setText(config.getJmesPathExpression());
        matchNumberField.setText(config.getMatchNumber());
        defaultValueField.setText(config.getDefaultValue());
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        JMESPathExtractor config = new JMESPathExtractor();
        modifyTestElement(config);
        return config;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement c) {
        super.configureTestElement(c);
        if (c instanceof JMESPathExtractor) {
            JMESPathExtractor config = (JMESPathExtractor) c;
            saveScopeSettings(config);
            config.setRefName(refNameField.getText());
            config.setJmesPathExpression(jmesPathExpressionField.getText());
            config.setDefaultValue(defaultValueField.getText());
            config.setMatchNumber(matchNumberField.getText());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        refNameField.setText(""); //$NON-NLS-1$
        jmesPathExpressionField.setText(""); //$NON-NLS-1$
        matchNumberField.setText(""); //$NON-NLS-1$
        defaultValueField.setText(""); //$NON-NLS-1$
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or
                          // final)

        setLayout(new BorderLayout());
        setBorder(makeBorder());

        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createScopePanel(true));
        add(box, BorderLayout.NORTH);
        add(makeParameterPanel(), BorderLayout.CENTER);

    }

    private JPanel makeParameterPanel() {
        refNameField = new JLabeledTextField(JMeterUtils.getResString("jsonpp_variable_names"));//$NON-NLS-1$
        jmesPathExpressionField = new JLabeledTextField(JMeterUtils.getResString("jmes_path_expressions"));//$NON-NLS-1$
        matchNumberField = new JLabeledTextField(JMeterUtils.getResString("jsonpp_match_numbers"));//$NON-NLS-1$
        defaultValueField = new JLabeledTextField(JMeterUtils.getResString("jsonpp_default_values"));//$NON-NLS-1$
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);
        addField(panel, refNameField, gbc);
        nextLine(gbc);
        addField(panel, jmesPathExpressionField, gbc);
        nextLine(gbc);
        addField(panel, matchNumberField, gbc);
        nextLine(gbc);
        nextLine(gbc);
        gbc.weighty = 1;
        addField(panel, defaultValueField, gbc);
        return panel;
    }

    private void addField(JPanel panel, JLabeledTextField field, GridBagConstraints gbc) {
        List<JComponent> item = field.getComponentList();
        panel.add(item.get(0), gbc.clone());
        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(item.get(1), gbc.clone());
    }

    private void nextLine(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
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
