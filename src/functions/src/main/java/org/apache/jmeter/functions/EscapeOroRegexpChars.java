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
import org.apache.oro.text.regex.Perl5Compiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Escape ORO meta characters
 * @since 2.9
 */
public class EscapeOroRegexpChars extends AbstractFunction {
    private static final Logger log = LoggerFactory.getLogger(EscapeOroRegexpChars.class);

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__escapeOroRegexpChars"; //$NON-NLS-1$

    static {
        desc.add(JMeterUtils.getResString("value_to_quote_meta")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt")); //$NON-NLS-1$
    }

    private CompoundVariable[] values;

    private static final int MAX_PARAM_COUNT = 2;

    private static final int MIN_PARAM_COUNT = 1;

    private static final int PARAM_NAME = 2;

    /**
     * No-arg constructor.
     */
    public EscapeOroRegexpChars() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        String valueToEscape = values[0].execute();

        String varName = "";//$NON-NLS-1$
        if (values.length >= PARAM_NAME) {
            varName = values[PARAM_NAME - 1].execute().trim();
        }

        String escapedValue = Perl5Compiler.quotemeta(valueToEscape);

        if (varName.length() > 0) {
            JMeterVariables vars = getVariables();
            if (vars != null) {// Can be null if called from Config item testEnded() method
                vars.put(varName, escapedValue);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("{} name:{} value:{}", Thread.currentThread().getName(), varName, escapedValue); //$NON-NLS-1$
        }

        return escapedValue;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, MIN_PARAM_COUNT, MAX_PARAM_COUNT);
        values = parameters.toArray(new CompoundVariable[parameters.size()]);
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
