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
import org.apache.jmeter.util.JMeterUtils;

/**
 * Function to set a JMeter property
 *
 * Parameters: - property name - value
 *
 * Usage:
 *
 * Set the property value in the appropriate GUI by using the string:
 * ${__setProperty(propname,propvalue[,returnvalue?])}
 *
 * Returns: nothing or original value if the 3rd parameter is true
 * @since 2.1
 */
public class SetProperty extends AbstractFunction {

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__setProperty"; //$NON-NLS-1$

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 2;

    private static final int MAX_PARAMETER_COUNT = 3;
    static {
        desc.add(JMeterUtils.getResString("property_name_param")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("property_value_param")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("property_returnvalue_param")); //$NON-NLS-1$
    }

    private Object[] values;

    public SetProperty() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        String propertyName = ((CompoundVariable) values[0]).execute();

        String propertyValue = ((CompoundVariable) values[1]).execute();

        boolean returnValue = false;// should we return original value?
        if (values.length > 2) {
            returnValue = ((CompoundVariable) values[2]).execute().equalsIgnoreCase("true"); //$NON-NLS-1$
        }

        if (returnValue) { // Only obtain and cast the return if needed
            return (String) JMeterUtils.setProperty(propertyName, propertyValue);
        } else {
            JMeterUtils.setProperty(propertyName, propertyValue);
            return "";
        }
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
