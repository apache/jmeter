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

import org.apache.commons.text.StringEscapeUtils;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;

/**
 * Function to unescape any Java literals found in the String.
 * For example, it will turn a sequence of '\' and 'n' into a newline character,
 * unless the '\' is preceded by another '\'.
 *
 * @see StringEscapeUtils#unescapeJava(String)
 * @since 2.3.3
 */
public class UnEscape extends AbstractFunction {

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__unescape"; //$NON-NLS-1$

    static {
        desc.add(JMeterUtils.getResString("unescape_string")); //$NON-NLS-1$
    }

    private Object[] values;

    public UnEscape() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        String rawString = ((CompoundVariable) values[0]).execute();
        return StringEscapeUtils.unescapeJava(rawString);

    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1);
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
