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

package org.apache.jmeter.functions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Function to get a JMeter property, and optionally store it
 *
 * Parameters:
 *  - property name
 *  - variable name (optional)
 *  - default value (optional)
 *
 * Returns:
 * - the property value, but if not found:
 * - the default value, but if not defined:
 * - the property name itself
 * @since 2.0
 */
public class Property extends AbstractFunction {

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__property"; //$NON-NLS-1$

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 1;
    private static final int MAX_PARAMETER_COUNT = 3;

    static {
        desc.add(JMeterUtils.getResString("property_name_param")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("property_default_param")); //$NON-NLS-1$
    }

    private Object[] values;

    public Property() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        String propertyName = ((CompoundVariable) values[0]).execute();
        String propertyDefault = propertyName;
        if (values.length > 2) { // We have a 3rd parameter
            propertyDefault = ((CompoundVariable) values[2]).execute();
        }
        String propertyValue = JMeterUtils.getPropDefault(propertyName, propertyDefault);
        if (values.length > 1) {
            String variableName = ((CompoundVariable) values[1]).execute();
            if (variableName.length() > 0) {// Allow for empty name
                final JMeterVariables variables = getVariables();
                if (variables != null) {
                    variables.put(variableName, propertyValue);
                }
            }
        }
        return propertyValue;

    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAMETER_COUNT, MAX_PARAMETER_COUNT);
        values = parameters.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }

}
