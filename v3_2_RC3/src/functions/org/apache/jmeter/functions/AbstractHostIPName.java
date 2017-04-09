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

abstract class AbstractHostIPName extends AbstractFunction {

    private static final List<String> desc = new LinkedList<>();

    static {
        // desc.add("Use fully qualified host name: TRUE/FALSE (Default FALSE)");
        desc.add(JMeterUtils.getResString("function_name_paropt")); //$NON-NLS-1$
    }

    private Object[] values;

    public AbstractHostIPName() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        /*
         * boolean fullHostName = false; if (((CompoundFunction) values[0])
         * .execute() .toLowerCase() .equals("true")) { fullHostName = true; }
         */

        String value = compute();

        if (values.length >= 1){// we have a variable name
            JMeterVariables vars = getVariables();
            if (vars != null) {// May be null if function is used on TestPlan
                String varName = ((CompoundVariable) values[0]).execute().trim();
                if (varName.length() > 0) {
                    vars.put(varName, value);
                }
            }
        }
        return value;

    }

    abstract protected String compute();

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 0, 1);
        values = parameters.toArray();
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getArgumentDesc() {
        return desc;
    }
}
