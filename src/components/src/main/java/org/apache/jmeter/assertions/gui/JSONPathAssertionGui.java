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

package org.apache.jmeter.assertions.gui;

import java.awt.BorderLayout;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.assertions.JSONPathAssertion;
import org.apache.jmeter.gui.GUIMenuSortOrder;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import net.miginfocom.swing.MigLayout;

/**
 * Java class representing GUI for the {@link JSONPathAssertion} component in JMeter
 * @since 4.0
 */
@GUIMenuSortOrder(2)
@TestElementMetadata(labelResource = "json_assertion_title")
public class JSONPathAssertionGui extends AbstractAssertionGui implements ChangeListener {

    private static final long serialVersionUID = -6008018002423594040L;
    private static final String JSON_ASSERTION_PATH = "json_assertion_path";
    private static final String JSON_ASSERTION_VALIDATION = "json_assertion_validation";
    private static final String JSON_ASSERTION_REGEX = "json_assertion_regex";
    private static final String JSON_ASSERTION_EXPECTED_VALUE = "json_assertion_expected_value";
    private static final String JSON_ASSERTION_NULL = "json_assertion_null";
    private static final String JSON_ASSERTION_INVERT = "json_assertion_invert";
    private static final String JSON_ASSERTION_TITLE = "json_assertion_title";

    protected JTextField jsonPath = null;
    protected JSyntaxTextArea jsonValue = null;
    protected JCheckBox jsonValidation = null;
    protected JCheckBox expectNull = null;
    protected JCheckBox invert = null;
    protected JCheckBox isRegex;

    public JSONPathAssertionGui() {
        init();
    }

    void init() {
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel panel = buildPanel();
        add(panel, BorderLayout.CENTER);

        jsonValidation.addChangeListener(this);
        expectNull.addChangeListener(this);
    }

    protected JPanel buildPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, wrap 2, insets 0", "[][fill,grow]"));

        jsonPath =  new JTextField();
        panel.add(JMeterUtils.labelFor(jsonPath, JSON_ASSERTION_PATH));
        panel.add(jsonPath, "span, growx");

        jsonValidation = new JCheckBox();
        panel.add(JMeterUtils.labelFor(jsonValidation, JSON_ASSERTION_VALIDATION));
        panel.add(jsonValidation, "span");

        isRegex = new JCheckBox();
        panel.add(JMeterUtils.labelFor(isRegex, JSON_ASSERTION_REGEX));
        panel.add(isRegex, "span");

        jsonValue =  JSyntaxTextArea.getInstance(5, 60);
        panel.add(JMeterUtils.labelFor(jsonValue, JSON_ASSERTION_EXPECTED_VALUE));
        panel.add(JTextScrollPane.getInstance(jsonValue));

        expectNull = new JCheckBox();
        panel.add(JMeterUtils.labelFor(expectNull, JSON_ASSERTION_NULL));
        panel.add(expectNull, "span");

        invert = new JCheckBox();
        panel.add(JMeterUtils.labelFor(invert, JSON_ASSERTION_INVERT));
        panel.add(invert, "span");
        return panel;
    }

    @Override
    public void clearGui() {
        super.clearGui();
        jsonPath.setText("");
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
        return JSON_ASSERTION_TITLE;
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
        if (element instanceof JSONPathAssertion) {
            JSONPathAssertion jpAssertion = (JSONPathAssertion) element;
            jsonPath.setText(jpAssertion.getJsonPath());
            jsonValue.setText(jpAssertion.getExpectedValue());
            jsonValidation.setSelected(jpAssertion.isJsonValidationBool());
            expectNull.setSelected(jpAssertion.isExpectNull());
            invert.setSelected(jpAssertion.isInvert());
            isRegex.setSelected(jpAssertion.isUseRegex());
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        jsonValue.setEnabled(jsonValidation.isSelected() && !expectNull.isSelected());
        isRegex.setEnabled(jsonValidation.isSelected() && !expectNull.isSelected());
    }
}
