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

package org.apache.jmeter.assertions.jmespath.gui;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.jmeter.assertions.gui.JSONPathAssertionGui;
import org.apache.jmeter.assertions.jmespath.JMESPathAssertion;
import org.apache.jmeter.gui.TestElementMetadata;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;

import net.miginfocom.swing.MigLayout;

/**
 * Java class representing GUI for the {@link JMESPathAssertion} component in
 * JMeter.
 * <p>This class extends {@link JSONPathAssertionGui} to avoid code duplication
 * because they work the same way, except that field names are different and
 * some method that we must {@link Override}.</p>
 *
 * @since 5.2
 */
@TestElementMetadata(labelResource = "jmespath_assertion_title")
public class JMESPathAssertionGui extends JSONPathAssertionGui {
    private static final long serialVersionUID = 3719848809836264945L;

    private static final String JMES_ASSERTION_PATH = "jmespath_assertion_path";
    private static final String JMES_ASSERTION_VALIDATION = "jmespath_assertion_validation";
    private static final String JMES_ASSERTION_REGEX = "jmespath_assertion_regex";
    private static final String JMES_ASSERTION_EXPECTED_VALUE = "jmespath_assertion_expected_value";
    private static final String JMES_ASSERTION_NULL = "jmespath_assertion_null";
    private static final String JMES_ASSERTION_INVERT = "jmespath_assertion_invert";
    private static final String JMES_ASSERTION_TITLE = "jmespath_assertion_title";

    /**
     * constructor
     */
    public JMESPathAssertionGui() {
        super();
    }

    @Override
    protected JPanel buildPanel() {
        JPanel panel = new JPanel(new MigLayout("fillx, wrap 2, insets 0", "[][fill,grow]"));

        jsonPath =  new JTextField();
        panel.add(JMeterUtils.labelFor(jsonPath, JMES_ASSERTION_PATH));
        panel.add(jsonPath, "span, growx");

        jsonValidation = new JCheckBox();
        panel.add(JMeterUtils.labelFor(jsonValidation, JMES_ASSERTION_VALIDATION));
        panel.add(jsonValidation, "span");

        isRegex = new JCheckBox();
        panel.add(JMeterUtils.labelFor(isRegex, JMES_ASSERTION_REGEX));
        panel.add(isRegex, "span");

        jsonValue =  JSyntaxTextArea.getInstance(5, 60);
        panel.add(JMeterUtils.labelFor(jsonValue, JMES_ASSERTION_EXPECTED_VALUE));
        panel.add(JTextScrollPane.getInstance(jsonValue));

        expectNull = new JCheckBox();
        panel.add(JMeterUtils.labelFor(expectNull, JMES_ASSERTION_NULL));
        panel.add(expectNull, "span");

        invert = new JCheckBox();
        panel.add(JMeterUtils.labelFor(invert, JMES_ASSERTION_INVERT));
        panel.add(invert, "span");
        return panel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TestElement createTestElement() {
        JMESPathAssertion jmesAssertion = new JMESPathAssertion();
        modifyTestElement(jmesAssertion);
        return jmesAssertion;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLabelResource() {
        return JMES_ASSERTION_TITLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyTestElement(TestElement element) {
        super.configureTestElement(element);
        if (element instanceof JMESPathAssertion) {
            JMESPathAssertion jmesAssertion = (JMESPathAssertion) element;
            jmesAssertion.setJmesPath(jsonPath.getText());
            jmesAssertion.setExpectedValue(jsonValue.getText());
            jmesAssertion.setJsonValidationBool(jsonValidation.isSelected());
            jmesAssertion.setExpectNull(expectNull.isSelected());
            jmesAssertion.setInvert(invert.isSelected());
            jmesAssertion.setIsRegex(isRegex.isSelected());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof JMESPathAssertion) {
            JMESPathAssertion jmesAssertion = (JMESPathAssertion) element;
            jsonPath.setText(jmesAssertion.getJmesPath());
            jsonValue.setText(jmesAssertion.getExpectedValue());
            jsonValidation.setSelected(jmesAssertion.isJsonValidationBool());
            expectNull.setSelected(jmesAssertion.isExpectNull());
            invert.setSelected(jmesAssertion.isInvert());
            isRegex.setSelected(jmesAssertion.isUseRegex());
        }
    }

}
