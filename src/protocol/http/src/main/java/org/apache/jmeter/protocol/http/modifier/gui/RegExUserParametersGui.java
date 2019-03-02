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

package org.apache.jmeter.protocol.http.modifier.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.jmeter.processor.gui.AbstractPreProcessorGui;
import org.apache.jmeter.protocol.http.modifier.RegExUserParameters;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * GUI for {@link RegExUserParameters}
 */
public class RegExUserParametersGui extends AbstractPreProcessorGui {

    /**
     *
     */
    private static final long serialVersionUID = 3080808672311046276L;

    private JLabeledTextField refRegExRefNameField;

    private JLabeledTextField paramNamesGrNrField;

    private JLabeledTextField paramValuesGrNrField;

    public RegExUserParametersGui() {
        super();
        init();
    }

    @Override
    public String getLabelResource() {
        return "regex_params_title"; //$NON-NLS-1$
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof RegExUserParameters){
            RegExUserParameters re = (RegExUserParameters) el;
            paramNamesGrNrField.setText(re.getRegParamNamesGrNr());
            paramValuesGrNrField.setText(re.getRegExParamValuesGrNr());
            refRegExRefNameField.setText(re.getRegExRefName());
        }
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        RegExUserParameters regExUserParams = new RegExUserParameters();
        modifyTestElement(regExUserParams);
        return regExUserParams;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement extractor) {
        super.configureTestElement(extractor);
        if (extractor instanceof RegExUserParameters) {
            RegExUserParameters regExUserParams = (RegExUserParameters) extractor;
            regExUserParams.setRegExRefName(refRegExRefNameField.getText());
            regExUserParams.setRegExParamNamesGrNr(paramNamesGrNrField.getText());
            regExUserParams.setRegExParamValuesGrNr(paramValuesGrNrField.getText());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        paramNamesGrNrField.setText(""); //$NON-NLS-1$
        paramValuesGrNrField.setText(""); //$NON-NLS-1$
        refRegExRefNameField.setText(""); //$NON-NLS-1$
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        add(box, BorderLayout.NORTH);
        add(makeParameterPanel(), BorderLayout.CENTER);
    }

    private JPanel makeParameterPanel() {
        refRegExRefNameField = new JLabeledTextField(JMeterUtils.getResString("regex_params_ref_name_field")); //$NON-NLS-1$
        paramNamesGrNrField = new JLabeledTextField(JMeterUtils.getResString("regex_params_names_field")); //$NON-NLS-1$
        paramValuesGrNrField = new JLabeledTextField(JMeterUtils.getResString("regex_params_values_field")); //$NON-NLS-1$

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);
        addField(panel, refRegExRefNameField, gbc);
        resetContraints(gbc);
        addField(panel, paramNamesGrNrField, gbc);
        resetContraints(gbc);
        gbc.weighty = 1;
        addField(panel, paramValuesGrNrField, gbc);
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
