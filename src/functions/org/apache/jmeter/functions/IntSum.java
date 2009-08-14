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
 * Provides an intSum function that adds two or more integer values.
 *
 * @see LongSum
 */
public class IntSum extends AbstractFunction {

    private static final List desc = new LinkedList();

    private static final String KEY = "__intSum"; //$NON-NLS-1$

    static {
        desc.add(JMeterUtils.getResString("intsum_param_1")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("intsum_param_2")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt")); //$NON-NLS-1$
    }

    private Object[] values;

    /**
     * No-arg constructor.
     */
    public IntSum() {
    }

    /**
     * Execute the function.
     *
     * @see Function#execute(SampleResult, Sampler)
     */
    @Override
    public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        JMeterVariables vars = getVariables();

        int sum = 0;
        String varName = ((CompoundVariable) values[values.length - 1]).execute();

        for (int i = 0; i < values.length - 1; i++) {
            sum += Integer.parseInt(((CompoundVariable) values[i]).execute());
        }

        try {
            sum += Integer.parseInt(varName);
            varName = null; // there is no variable name
        } catch (NumberFormatException ignored) {
        }

        String totalString = Integer.toString(sum);
        if (vars != null && varName != null){// vars will be null on TestPlan
            vars.put(varName.trim(), totalString);
        }

        return totalString;

    }

    /**
     * Set the parameters for the function.
     *
     * @see Function#setParameters(Collection)
     */
    @Override
    public synchronized void setParameters(Collection parameters) throws InvalidVariableException {
        checkMinParameterCount(parameters, 2);
        values = parameters.toArray();
    }

    /**
     * Get the invocation key for this function.
     *
     * @see Function#getReferenceKey()
     */
    @Override
    public String getReferenceKey() {
        return KEY;
    }

    /**
     * Get the description of this function.
     *
     * @see Function#getArgumentDesc()
     */
    public List getArgumentDesc() {
        return desc;
    }
}
