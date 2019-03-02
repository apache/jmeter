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

package org.apache.jmeter.assertions;

import java.io.Serializable;
import java.text.MessageFormat;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.AbstractScopedAssertion;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.util.JMeterUtils;

//@see org.apache.jmeter.assertions.SizeAssertionTest for unit tests

/**
 * Checks if the results of a Sample matches a particular size.
 *
 */
public class SizeAssertion extends AbstractScopedAssertion implements Serializable, Assertion {

    private static final long serialVersionUID = 241L;

    // Static int to signify the type of logical comparator to assert
    public static final int EQUAL = 1;

    public static final int NOTEQUAL = 2;

    public static final int GREATERTHAN = 3;

    public static final int LESSTHAN = 4;

    public static final int GREATERTHANEQUAL = 5;

    public static final int LESSTHANEQUAL = 6;

    /** Key for storing assertion-information in the jmx-file. */
    private static final String SIZE_KEY = "SizeAssertion.size"; // $NON-NLS-1$

    private static final String OPERATOR_KEY = "SizeAssertion.operator"; // $NON-NLS-1$

    private static final String TEST_FIELD = "Assertion.test_field";  // $NON-NLS-1$

    private static final String RESPONSE_NETWORK_SIZE = "SizeAssertion.response_network_size"; // $NON-NLS-1$

    private static final String RESPONSE_HEADERS = "SizeAssertion.response_headers"; // $NON-NLS-1$

    private static final String RESPONSE_BODY = "SizeAssertion.response_data"; // $NON-NLS-1$

    private static final String RESPONSE_CODE = "SizeAssertion.response_code"; // $NON-NLS-1$

    private static final String RESPONSE_MESSAGE = "SizeAssertion.response_message"; // $NON-NLS-1$

    /**
     * Returns the result of the Assertion.
     * Here it checks the Sample responseData length.
     */
    @Override
    public AssertionResult getResult(SampleResult response) {
        AssertionResult result = new AssertionResult(getName());
        result.setFailure(false);
        long resultSize;
        if (isScopeVariable()){
            String variableName = getVariableName();
            String value = getThreadContext().getVariables().get(variableName);
            try {
                resultSize = Long.parseLong(value);
            } catch (NumberFormatException e) {
                result.setFailure(true);
                result.setFailureMessage("Error parsing variable name: "+variableName+" value: "+value);
                return result;
            }
        } else if (isTestFieldResponseHeaders()) {
            resultSize = response.getHeadersSize();
        }  else if (isTestFieldResponseBody()) {
            resultSize = response.getBodySizeAsLong();
        } else if (isTestFieldResponseCode()) {
            resultSize = response.getResponseCode().length();
        } else if (isTestFieldResponseMessage()) {
            resultSize = response.getResponseMessage().length();
        } else {
            resultSize = response.getBytesAsLong();
        }
        // is the Sample the correct size?
        final String msg = compareSize(resultSize);
        if (msg.length() > 0) {
            result.setFailure(true);
            Object[] arguments = { Long.valueOf(resultSize), msg, Long.valueOf(getAllowedSize()) };
            String message = MessageFormat.format(
                    JMeterUtils.getResString("size_assertion_failure"), arguments); //$NON-NLS-1$
            result.setFailureMessage(message);
        }
        return result;
    }

    /**
     * Returns the size in bytes to be asserted.
     * @return The allowed size
     */
    public String getAllowedSize() {
        return getPropertyAsString(SIZE_KEY);
    }

    /**
     Set the operator used for the assertion. Has to be one of
     <dl>
     * <dt>EQUAL</dt><dd>1</dd>
     * <dt>NOTEQUAL</dt><dd>2</dd>
     * <dt>GREATERTHAN</dt><dd>3</dd>
     * <dt>LESSTHAN</dt><dd>4</dd>
     * <dt>GREATERTHANEQUAL</dt><dd>5</dd>
     * <dt>LESSTHANEQUAL</dt><dd>6</dd>
     * </dl>
     * @param operator The operator to be used in the assertion
     */
    public void setCompOper(int operator) {
        setProperty(new IntegerProperty(OPERATOR_KEY, operator));

    }

    /**
     * Returns the operator to be asserted.
     * <dl>
     * <dt>EQUAL</dt><dd>1</dd>
     * <dt>NOTEQUAL</dt><dd>2</dd>
     * <dt>GREATERTHAN</dt><dd>3</dd>
     * <dt>LESSTHAN</dt><dd>4</dd>
     * <dt>GREATERTHANEQUAL</dt><dd>5</dd>
     * <dt>LESSTHANEQUAL</dt><dd>6</dd>
     * </dl>
     * @return The operator used for the assertion
     */

    public int getCompOper() {
        return getPropertyAsInt(OPERATOR_KEY);
    }

    /**
     * Set the size that shall be asserted.
     *
     * @param size a number of bytes.
     */
    public void setAllowedSize(String size) {
            setProperty(SIZE_KEY, size);
    }

    /**
     * Set the size that should be used in the assertion
     * @param size The number of bytes
     */
    public void setAllowedSize(long size) {
        setProperty(SIZE_KEY, Long.toString(size));
    }

    /**
     * Compares the size of a return result to the set allowed size using a
     * logical comparator set in setLogicalComparator().
     *
     * Possible values are: equal, not equal, greater than, less than, greater
     * than equal, less than equal.
     *
     */
    private String compareSize(long resultSize) {
        String comparatorErrorMessage;
        long allowedSize = Long.parseLong(getAllowedSize());
        boolean result;
        int comp = getCompOper();
        switch (comp) {
        case EQUAL:
            result = resultSize == allowedSize;
            comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_equal"); //$NON-NLS-1$
            break;
        case NOTEQUAL:
            result = resultSize != allowedSize;
            comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_notequal"); //$NON-NLS-1$
            break;
        case GREATERTHAN:
            result = resultSize > allowedSize;
            comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_greater"); //$NON-NLS-1$
            break;
        case LESSTHAN:
            result = resultSize < allowedSize;
            comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_less"); //$NON-NLS-1$
            break;
        case GREATERTHANEQUAL:
            result = resultSize >= allowedSize;
            comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_greaterequal"); //$NON-NLS-1$
            break;
        case LESSTHANEQUAL:
            result = resultSize <= allowedSize;
            comparatorErrorMessage = JMeterUtils.getResString("size_assertion_comparator_error_lessequal"); //$NON-NLS-1$
            break;
        default:
            result = false;
            comparatorErrorMessage = "ERROR - invalid condition";
            break;
        }
        return result ? "" : comparatorErrorMessage;
    }

    private void setTestField(String testField) {
        setProperty(TEST_FIELD, testField);
    }

    public void setTestFieldNetworkSize(){
        setTestField(RESPONSE_NETWORK_SIZE);
    }

    public void setTestFieldResponseHeaders(){
        setTestField(RESPONSE_HEADERS);
    }

    public void setTestFieldResponseBody(){
        setTestField(RESPONSE_BODY);
    }

    public void setTestFieldResponseCode(){
        setTestField(RESPONSE_CODE);
    }

    public void setTestFieldResponseMessage(){
        setTestField(RESPONSE_MESSAGE);
    }

    public String getTestField() {
        return getPropertyAsString(TEST_FIELD);
    }

    public boolean isTestFieldNetworkSize(){
        return RESPONSE_NETWORK_SIZE.equals(getTestField());
    }

    public boolean isTestFieldResponseHeaders(){
        return RESPONSE_HEADERS.equals(getTestField());
    }

    public boolean isTestFieldResponseBody(){
        return RESPONSE_BODY.equals(getTestField());
    }

    public boolean isTestFieldResponseCode(){
        return RESPONSE_CODE.equals(getTestField());
    }

    public boolean isTestFieldResponseMessage(){
        return RESPONSE_MESSAGE.equals(getTestField());
    }

}
