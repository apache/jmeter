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

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.jmeter.extractor.XPath2Extractor;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.processor.gui.AbstractPostProcessorGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * GUI for XPath2Extractor class.
 * @since 5.0
 */
public class XPath2ExtractorGui extends AbstractPostProcessorGui{ // NOSONAR Ignore parents warning

    private static final long serialVersionUID = 1L;

    private final JLabeledTextField defaultField = new JLabeledTextField(
            JMeterUtils.getResString("default_value_field"));//$NON-NLS-1$

    private final JLabeledTextField xpathQueryField = new JLabeledTextField(
            JMeterUtils.getResString("xpath_extractor_query"));//$NON-NLS-1$

    private final JLabeledTextField matchNumberField = new JLabeledTextField(
            JMeterUtils.getResString("match_num_field"));//$NON-NLS-1$

    private final JLabeledTextField refNameField = new JLabeledTextField(JMeterUtils.getResString("ref_name_field"));//$NON-NLS-1$

    // Should we return fragment as text, rather than text of fragment?
    private JCheckBox getFragment;
    
    private JSyntaxTextArea namespacesTA;
    
    @Override
    public String getLabelResource() {
        return "xpath2_extractor_title"; //$NON-NLS-1$
    }

    public XPath2ExtractorGui() {
        super();
        init();
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        XPath2Extractor xpe = (XPath2Extractor) el;
        showScopeSettings(xpe, true);
        xpathQueryField.setText(xpe.getXPathQuery());
        defaultField.setText(xpe.getDefaultValue());
        refNameField.setText(xpe.getRefName());
        matchNumberField.setText(xpe.getMatchNumberAsString());
        namespacesTA.setText(xpe.getNamespaces());
        getFragment.setSelected(xpe.getFragment());
    }

    @Override
    public TestElement createTestElement() {
        XPath2Extractor extractor = new XPath2Extractor();
        modifyTestElement(extractor);
        return extractor;
    }

    @Override
    public void modifyTestElement(TestElement extractor) {
        super.configureTestElement(extractor);
        if (extractor instanceof XPath2Extractor) {
            XPath2Extractor xpath = (XPath2Extractor) extractor;
            saveScopeSettings(xpath);
            xpath.setDefaultValue(defaultField.getText());
            xpath.setRefName(refNameField.getText());
            xpath.setMatchNumber(matchNumberField.getText());
            xpath.setXPathQuery(xpathQueryField.getText());
            xpath.setFragment(getFragment.isSelected());
            xpath.setNamespaces(namespacesTA.getText());
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
        matchNumberField.setText(XPath2Extractor.DEFAULT_VALUE_AS_STRING); // $NON-NLS-1$
        namespacesTA.setText("");
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createScopePanel(true, true, true));
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
        addField(panel, defaultField, gbc);
        resetContraints(gbc);
        panel.add(new JLabel(JMeterUtils.getResString("xpath_extractor_user_namespaces")), gbc.clone());
        gbc.gridx++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        namespacesTA = JSyntaxTextArea.getInstance(5, 80);
        panel.add(JTextScrollPane.getInstance(namespacesTA, true), gbc.clone());

        resetContraints(gbc);
        gbc.gridwidth = 2;
        getFragment = new JCheckBox(JMeterUtils.getResString("xpath_extractor_fragment"));//$NON-NLS-1$
        panel.add(getFragment, gbc.clone());
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

    private void resetContraints(GridBagConstraints gbc) {
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
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
