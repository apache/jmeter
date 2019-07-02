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
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.jmeter.reporters.ResultSaver;
import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractListenerGui;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * Create a ResultSaver test element, which saves the sample information in set
 * of files
 *
 */
public class ResultSaverGui extends AbstractListenerGui implements Clearable { // NOSONAR Ignore inheritance rule

    private static final long serialVersionUID = 241L;

    private JLabeledTextField filename;

    private JLabeledTextField variableName;

    private JLabeledTextField numberPadLength;

    private JCheckBox errorsOnly;

    private JCheckBox successOnly;

    private JCheckBox ignoreTC;

    private JCheckBox skipAutoNumber;

    private JCheckBox skipSuffix;

    private JCheckBox addTimestamp;

    public ResultSaverGui() {
        super();
        init();
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#getStaticLabel()
     */
    @Override
    public String getLabelResource() {
        return "resultsaver_title"; // $NON-NLS-1$
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#configure(TestElement)
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        ResultSaver resultSaver = (ResultSaver) el;
        filename.setText(resultSaver.getFilename());
        errorsOnly.setSelected(resultSaver.getErrorsOnly());
        successOnly.setSelected(resultSaver.getSuccessOnly());
        ignoreTC.setSelected(resultSaver.getIgnoreTC());
        skipAutoNumber.setSelected(resultSaver.getSkipAutoNumber());
        skipSuffix.setSelected(resultSaver.getSkipSuffix());
        variableName.setText(resultSaver.getVariableName());
        addTimestamp.setSelected(resultSaver.getAddTimeStamp());
        numberPadLength.setText(resultSaver.getNumberPadLen() == 0 ?
                "" : Integer.toString(resultSaver.getNumberPadLen()));
    }

    /**
     * @see org.apache.jmeter.gui.JMeterGUIComponent#createTestElement()
     */
    @Override
    public TestElement createTestElement() {
        ResultSaver resultSaver = new ResultSaver();
        modifyTestElement(resultSaver);
        return resultSaver;
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(TestElement)
     */
    @Override
    public void modifyTestElement(TestElement te) {
        super.configureTestElement(te);
        ResultSaver resultSaver = (ResultSaver) te;
        resultSaver.setFilename(filename.getText());
        resultSaver.setErrorsOnly(errorsOnly.isSelected());
        resultSaver.setSuccessOnly(successOnly.isSelected());
        resultSaver.setSkipSuffix(skipSuffix.isSelected());
        resultSaver.setSkipAutoNumber(skipAutoNumber.isSelected());
        resultSaver.setIgnoreTC(ignoreTC.isSelected());
        resultSaver.setAddTimestamp(addTimestamp.isSelected());
        resultSaver.setVariableName(variableName.getText());
        resultSaver.setNumberPadLength(numberPadLength.getText());
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();

        skipAutoNumber.setSelected(false);
        skipSuffix.setSelected(false);
        filename.setText(""); //$NON-NLS-1$
        errorsOnly.setSelected(false);
        successOnly.setSelected(false);
        ignoreTC.setSelected(true);
        addTimestamp.setSelected(false);
        variableName.setText(""); //$NON-NLS-1$
        numberPadLength.setText(""); //$NON-NLS-1$
    }

    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        Box box = Box.createVerticalBox();
        box.add(makeTitlePanel());
        box.add(createSaveConditionsPanel());
        box.add(createSaveFormatPanel());
        add(box, BorderLayout.NORTH);
    }

    private Component createSaveFormatPanel() {
        filename = new JLabeledTextField(JMeterUtils.getResString("resultsaver_prefix"));
        filename.setName(ResultSaver.FILENAME);

        numberPadLength = new JLabeledTextField(JMeterUtils.getResString("resultsaver_numberpadlen"));// $NON-NLS-1$
        numberPadLength.setName(ResultSaver.NUMBER_PAD_LENGTH);

        skipAutoNumber = new JCheckBox(JMeterUtils.getResString("resultsaver_skipautonumber")); // $NON-NLS-1$
        skipSuffix = new JCheckBox(JMeterUtils.getResString("resultsaver_skipsuffix")); // $NON-NLS-1$
        addTimestamp = new JCheckBox(JMeterUtils.getResString("resultsaver_addtimestamp")); // $NON-NLS-1$

        variableName = new JLabeledTextField(JMeterUtils.getResString("resultsaver_variable"));
        variableName.setName(ResultSaver.VARIABLE_NAME);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("resultsaver_save_format"))); //$NON-NLS-1$
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);

        addField(panel, variableName, gbc);
        resetContraints(gbc);
        addField(panel, filename, gbc);
        resetContraints(gbc);
        addField(panel, skipAutoNumber, gbc);
        resetContraints(gbc);
        addField(panel, skipSuffix, gbc);
        resetContraints(gbc);
        addField(panel, addTimestamp, gbc);
        resetContraints(gbc);
        addField(panel, numberPadLength, gbc);
        resetContraints(gbc);

        return panel;
    }

    private Component createSaveConditionsPanel() {
        successOnly = new JCheckBox(JMeterUtils.getResString("resultsaver_success")); // $NON-NLS-1$
        errorsOnly = new JCheckBox(JMeterUtils.getResString("resultsaver_errors")); // $NON-NLS-1$
        ignoreTC = new JCheckBox(JMeterUtils.getResString("resultsaver_ignore_tc")); // $NON-NLS-1$

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(JMeterUtils.getResString("resultsaver_save_conditions"))); //$NON-NLS-1$
        GridBagConstraints gbc = new GridBagConstraints();
        initConstraints(gbc);

        addField(panel, successOnly, gbc);
        resetContraints(gbc);
        addField(panel, errorsOnly, gbc);
        resetContraints(gbc);
        addField(panel, ignoreTC, gbc);
        resetContraints(gbc);

        return panel;
    }

    // Needed to avoid Class cast error in Clear.java
    @Override
    public void clearData() {
        // NOOP
    }

    private void addField(JPanel panel, JCheckBox field, GridBagConstraints gbc) {
        gbc.weightx = 2;
        gbc.fill=GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc.clone());
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
