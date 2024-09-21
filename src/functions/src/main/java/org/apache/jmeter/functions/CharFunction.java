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

package org.apache.jmeter.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;

/**
 * Function to generate chars from a list of decimal or hex values
 * @since 2.3.3
 */
@AutoService(Function.class)
public class CharFunction extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(CharFunction.class);

    private static final List<String> desc = new ArrayList<>();

    private static final String KEY = "__char"; //$NON-NLS-1$

    static {
        desc.add(JMeterUtils.getResString("char_value")); //$NON-NLS-1$
    }

    private Object[] values;

    public CharFunction() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        StringBuilder sb = new StringBuilder(values.length);
        for (Object val : values) {
            String numberString = ((CompoundVariable) val).execute().trim();
            try {
                long value = Long.decode(numberString);
                char ch = (char) value;
                sb.append(ch);
            } catch (NumberFormatException e) {
                log.warn("Could not parse {} : {}", numberString, e.toString());
            }
        }
        return sb.toString();

    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkMinParameterCount(parameters, 1);
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
