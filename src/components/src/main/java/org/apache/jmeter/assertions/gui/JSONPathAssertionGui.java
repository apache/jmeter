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
package org.apache.jmeter.assertions.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.assertions.JSONPathAssertion;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * Java class representing GUI for the {@link JSONPathAssertion} component in JMeter
 * @since 4.0
 */
@GUIMenuSortOrder(2)
public class JSONPathAssertionGui extends AbstractAssertionGui implements ChangeListener {

    private static final long serialVersionUID = -6008018002423594040L;
    private JLabeledTextField jsonPath = null;
    private JLabeledTextArea jsonValue = null;
    private JCheckBox jsonValidation = null;
    private JCheckBox expectNull = null;
    private JCheckBox invert = null;
    private JCheckBox isRegex;

    public JSONPathAssertionGui() {
        init();
    }

    public void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        VerticalPanel panel = new VerticalPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        jsonPath = new JLabeledTextField(JMeterUtils.getResString("json_assertion_path"));
        jsonValidation = new JCheckBox(JMeterUtils.getResString("json_assertion_validation"));
        isRegex = new JCheckBox(JMeterUtils.getResString("json_assertion_regex"));
        jsonValue = new JLabeledTextArea(JMeterUtils.getResString("json_assertion_expected_value"));
        expectNull = new JCheckBox(JMeterUtils.getResString("json_assertion_null"));
        invert = new JCheckBox(JMeterUtils.getResString("json_assertion_invert"));

        jsonValidation.addChangeListener(this);
        expectNull.addChangeListener(this);

        panel.add(jsonPath);
        panel.add(jsonValidation);
        panel.add(isRegex);
        panel.add(jsonValue);
        panel.add(expectNull);
        panel.add(invert);

        add(panel, BorderLayout.CENTER);
    }

    @Override
    public void clearGui() {
        super.clearGui();
        jsonPath.setText("$.");
        jsonValue.setText("");
        jsonValidation.setSelected(false);
        expectNull.setSelected(false);
        invert.setSelected(false);
        isRegex.setSelected(true);
    }

    @Override
    public TestElement createTestElement() {
        JSONPathAssertion jpAssertion = new JSONPathAssertion();
        modifyTestElement(jpAssertion);
        return jpAssertion;
    }

    @Override
    public String getLabelResource() {
        return "json_assertion_title";
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        if (element instanceof JSONPathAssertion) {
            JSONPathAssertion jpAssertion = (JSONPathAssertion) element;
            jpAssertion.setJsonPath(jsonPath.getText());
            jpAssertion.setExpectedValue(jsonValue.getText());
            jpAssertion.setJsonValidationBool(jsonValidation.isSelected());
            jpAssertion.setExpectNull(expectNull.isSelected());
            jpAssertion.setInvert(invert.isSelected());
            jpAssertion.setIsRegex(isRegex.isSelected());
        }
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        JSONPathAssertion jpAssertion = (JSONPathAssertion) element;
        jsonPath.setText(jpAssertion.getJsonPath());
        jsonValue.setText(jpAssertion.getExpectedValue());
        jsonValidation.setSelected(jpAssertion.isJsonValidationBool());
        expectNull.setSelected(jpAssertion.isExpectNull());
        invert.setSelected(jpAssertion.isInvert());
        isRegex.setSelected(jpAssertion.isUseRegex());
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        jsonValue.setEnabled(jsonValidation.isSelected() && !expectNull.isSelected());
        isRegex.setEnabled(jsonValidation.isSelected() && !expectNull.isSelected());
    }
}
