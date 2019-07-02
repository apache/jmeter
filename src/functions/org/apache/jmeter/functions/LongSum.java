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
 * Provides a longSum function that adds two or more long values.
 * @see IntSum
 * @since 2.3.2
 */
public class LongSum extends AbstractFunction {

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__longSum"; //$NON-NLS-1$

    static {
        desc.add(JMeterUtils.getResString("longsum_param_1")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("longsum_param_2")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt")); //$NON-NLS-1$
    }

    private Object[] values;

    /**
     * No-arg constructor.
     */
    public LongSum() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        JMeterVariables vars = getVariables();

        long sum = 0;
        String varName = ((CompoundVariable) values[values.length - 1]).execute().trim();

        for (int i = 0; i < values.length - 1; i++) {
            sum += Long.parseLong(((CompoundVariable) values[i]).execute());
        }

        try {
            // Has chances to be a var
            sum += Long.parseLong(varName);
            varName = null; // there is no variable name
        } catch(NumberFormatException ignored) {
            // varName keeps its value and sum has not taken
            // into account non numeric or overflowing number
        }

        String totalString = Long.toString(sum);
        if (vars != null && varName != null && varName.length() > 0){// vars will be null on TestPlan
            vars.put(varName, totalString);
        }

        return totalString;

    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkMinParameterCount(parameters, 2);
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
