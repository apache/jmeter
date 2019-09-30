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

package org.apache.jmeter.assertions.jmespath.gui;

import javax.swing.JCheckBox;

import org.apache.jmeter.assertions.gui.JSONPathAssertionGui;
import org.apache.jmeter.assertions.jmespath.JMESPathAssertion;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.JLabeledTextArea;
import org.apache.jorphan.gui.JLabeledTextField;

/**
 * Java class representing GUI for the {@link JMESPathAssertion} component in
 * JMeter.
 * <p>This class extends {@link JSONPathAssertionGui} to avoid code duplication
 * because they work the same way, except that field names are different and
 * some method that we must {@link Override}.</p>
 *
 * @since 5.2
 */
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
    protected final void initFields() {
        // get the superclass fields and set their name to current component fields.
        super.jsonPath = new JLabeledTextField(JMeterUtils.getResString(JMES_ASSERTION_PATH));
        super.jsonValue = new JLabeledTextArea(JMeterUtils.getResString(JMES_ASSERTION_EXPECTED_VALUE));
        super.jsonValidation = new JCheckBox(JMeterUtils.getResString(JMES_ASSERTION_VALIDATION));
        super.expectNull = new JCheckBox(JMeterUtils.getResString(JMES_ASSERTION_NULL));
        super.invert = new JCheckBox(JMeterUtils.getResString(JMES_ASSERTION_INVERT));
        super.isRegex = new JCheckBox(JMeterUtils.getResString(JMES_ASSERTION_REGEX));
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
