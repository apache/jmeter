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
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.jmeter.extractor.BoundaryExtractor;
import org.apache.jmeter.extractor.RegexExtractor;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.AbstractScopedTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * Boundary Extractor Post-Processor GUI
 * @since 4.0
 */
@GUIMenuSortOrder(4)
public class BoundaryExtractorGui extends AbstractPostProcessorGui {
    private static final long serialVersionUID = 240L;

    private JLabeledTextField leftBoundaryField;

    private JLabeledTextField rightBoundaryField;
    
    private JLabeledTextField defaultField;

    private JLabeledTextField matchNumberField;

    private JLabeledTextField refNameField;

    private JCheckBox emptyDefaultValue;
    
    private JRadioButton useBody;

    private JRadioButton useUnescapedBody;

    private JRadioButton useBodyAsDocument;

    private JRadioButton useHeaders;

    private JRadioButton useRequestHeaders;

    private JRadioButton useURL;

    private JRadioButton useCode;

    private JRadioButton useMessage;

    private ButtonGroup group;
    
    public BoundaryExtractorGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "boundaryextractor_title"; //$NON-NLS-1$
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof BoundaryExtractor){
            BoundaryExtractor boundary = (BoundaryExtractor) el;
            showScopeSettings(boundary, true);
            useHeaders.setSelected(boundary.useHeaders());
            useRequestHeaders.setSelected(boundary.useRequestHeaders());
            useBody.setSelected(boundary.useBody());
            useUnescapedBody.setSelected(boundary.useUnescapedBody());
            useBodyAsDocument.setSelected(boundary.useBodyAsDocument());
            useURL.setSelected(boundary.useUrl());
            useCode.setSelected(boundary.useCode());
            useMessage.setSelected(boundary.useMessage());
            leftBoundaryField.setText(boundary.getLeftBoundary());
            rightBoundaryField.setText(boundary.getRightBoundary());
            defaultField.setText(boundary.getDefaultValue());
            emptyDefaultValue.setSelected(boundary.isEmptyDefaultValue());
            matchNumberField.setText(boundary.getMatchNumberAsString());
            refNameField.setText(boundary.getRefName());
        }
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        AbstractScopedTestElement extractor = new BoundaryExtractor();
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
        if (extractor instanceof BoundaryExtractor) {
            BoundaryExtractor boundary = (BoundaryExtractor) extractor;
            saveScopeSettings(boundary);
            boundary.setUseField(group.getSelection().getActionCommand());
            boundary.setRefName(refNameField.getText());
            boundary.setLeftBoundary(leftBoundaryField.getText());
            boundary.setRightBoundary(rightBoundaryField.getText());
            boundary.setDefaultValue(defaultField.getText());
            boundary.setDefaultEmptyValue(emptyDefaultValue.isSelected());
            boundary.setMatchNumber(matchNumberField.getText());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        
        useBody.setSelected(true);
        
        leftBoundaryField.setText(""); //$NON-NLS-1$        
        rightBoundaryField.setText(""); //$NON-NLS-1$
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
        box.add(makeSourcePanel());
        add(box, BorderLayout.NORTH);
        add(makeParameterPanel(), BorderLayout.CENTER);
    }

    private JPanel makeSourcePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("regex_source"))); //$NON-NLS-1$

        useBody = new JRadioButton(JMeterUtils.getResString("regex_src_body")); //$NON-NLS-1$
        useUnescapedBody = new JRadioButton(JMeterUtils.getResString("regex_src_body_unescaped")); //$NON-NLS-1$
        useBodyAsDocument = new JRadioButton(JMeterUtils.getResString("regex_src_body_as_document")); //$NON-NLS-1$
        useHeaders = new JRadioButton(JMeterUtils.getResString("regex_src_hdrs")); //$NON-NLS-1$
        useRequestHeaders = new JRadioButton(JMeterUtils.getResString("regex_src_hdrs_req")); //$NON-NLS-1$
        useURL = new JRadioButton(JMeterUtils.getResString("regex_src_url")); //$NON-NLS-1$
        useCode = new JRadioButton(JMeterUtils.getResString("assertion_code_resp")); //$NON-NLS-1$
        useMessage = new JRadioButton(JMeterUtils.getResString("assertion_message_resp")); //$NON-NLS-1$

        group = new ButtonGroup();
        group.add(useBody);
        group.add(useUnescapedBody);
        group.add(useBodyAsDocument);
        group.add(useHeaders);
        group.add(useRequestHeaders);
        group.add(useURL);
        group.add(useCode);
        group.add(useMessage);

        panel.add(useBody);
        panel.add(useUnescapedBody);
        panel.add(useBodyAsDocument);
        panel.add(useHeaders);
        panel.add(useRequestHeaders);
        panel.add(useURL);
        panel.add(useCode);
        panel.add(useMessage);

        useBody.setSelected(true);

        // So we know which button is selected
        useBody.setActionCommand(RegexExtractor.USE_BODY);
        useUnescapedBody.setActionCommand(RegexExtractor.USE_BODY_UNESCAPED);
        useBodyAsDocument.setActionCommand(RegexExtractor.USE_BODY_AS_DOCUMENT);
        useHeaders.setActionCommand(RegexExtractor.USE_HDRS);
        useRequestHeaders.setActionCommand(RegexExtractor.USE_REQUEST_HDRS);
        useURL.setActionCommand(RegexExtractor.USE_URL);
        useCode.setActionCommand(RegexExtractor.USE_CODE);
        useMessage.setActionCommand(RegexExtractor.USE_MESSAGE);

        return panel;
    }
    
    private JPanel makeParameterPanel() {        
        leftBoundaryField = new JLabeledTextField(JMeterUtils.getResString("boundaryextractor_leftboundary_field")); //$NON-NLS-1$
        rightBoundaryField = new JLabeledTextField(JMeterUtils.getResString("boundaryextractor_rightboundary_field")); //$NON-NLS-1$
        refNameField = new JLabeledTextField(JMeterUtils.getResString("ref_name_field")); //$NON-NLS-1$
        matchNumberField = new JLabeledTextField(JMeterUtils.getResString("match_num_field")); //$NON-NLS-1$

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);
        addField(panel, refNameField, gbc);
        resetContraints(gbc);
        addField(panel, leftBoundaryField, gbc);
        resetContraints(gbc);
        addField(panel, rightBoundaryField, gbc);
        resetContraints(gbc);
        addField(panel, matchNumberField, gbc);
        resetContraints(gbc);
        gbc.weighty = 1;
        
        defaultField = new JLabeledTextField(JMeterUtils.getResString("default_value_field")); //$NON-NLS-1$
        List<JComponent> item = defaultField.getComponentList();
        panel.add(item.get(0), gbc.clone());
        JPanel p = new JPanel(new BorderLayout());
        p.add(item.get(1), BorderLayout.WEST);
        emptyDefaultValue = new JCheckBox(JMeterUtils.getResString("boundaryextractor_empty_default_value"));
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
