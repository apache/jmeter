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
import org.apache.jorphan.util.JOrphanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @see org.apache.jmeter.functions.PackageTest for unit tests

/**
 * Function to split a string into variables
 * <p>
 * Parameters:
 * <ul>
 * <li>String to split</li>
 * <li>Variable name prefix</li>
 * <li>String to split on (optional, default is comma)</li>
 * </ul>
 * <p>
 * Returns: the input string
 * </p>
 * Also sets the variables:
 * <ul>
 * <li>VARNAME - the input string</li>
 * <li>VARNAME_n - number of fields found</li>
 * <li>VARNAME_1..n - fields</li>
 * </ul>
 * @since 2.0.2
 */
public class SplitFunction extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(SplitFunction.class);

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__split";// $NON-NLS-1$

    // Number of parameters expected - used to reject invalid calls
    private static final int MIN_PARAMETER_COUNT = 2;

    private static final int MAX_PARAMETER_COUNT = 3;
    static {
        desc.add(JMeterUtils.getResString("split_function_string"));   //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_param"));     //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("split_function_separator"));//$NON-NLS-1$
    }

    private Object[] values;

    public SplitFunction() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {
        JMeterVariables vars = getVariables();

        String stringToSplit = ((CompoundVariable) values[0]).execute();
        String varNamePrefix = ((CompoundVariable) values[1]).execute().trim();
        String splitString = ",";

        if (values.length > 2) { // Split string provided
            String newSplitString = ((CompoundVariable) values[2]).execute();
            splitString = newSplitString.length() > 0 ? newSplitString : splitString;
        }
        log.debug("Split {} using {} into {}", stringToSplit, splitString, varNamePrefix);
        String[] parts = JOrphanUtils.split(stringToSplit, splitString, "?");// $NON-NLS-1$

        vars.put(varNamePrefix, stringToSplit);
        vars.put(varNamePrefix + "_n", Integer.toString(parts.length));// $NON-NLS-1$
        for (int i = 1; i <= parts.length; i++) {
            if (log.isDebugEnabled()){
                log.debug(parts[i-1]);
            }
            vars.put(varNamePrefix + "_" + i, parts[i - 1]);// $NON-NLS-1$
        }
        vars.remove(varNamePrefix + "_" + (parts.length+1));
        return stringToSplit;

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
