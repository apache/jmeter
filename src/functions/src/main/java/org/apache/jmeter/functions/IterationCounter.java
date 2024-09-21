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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.ThreadListener;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;

import com.google.auto.service.AutoService;

/**
 * Counter that can be referenced anywhere in the Thread Group. It can be configured per User (Thread Local)
 * or globally.
 * @since 1.X
 */
@AutoService(Function.class)
public class IterationCounter extends AbstractFunction implements ThreadListener {

    private static final List<String> desc = new ArrayList<>();

    private static final String KEY = "__counter"; //$NON-NLS-1$

    private Object[] variables;

    private final AtomicInteger globalCounter = new AtomicInteger();

    private final ThreadLocal<AtomicInteger> perThreadInt = ThreadLocal.withInitial(AtomicInteger::new);

    static {
        desc.add(JMeterUtils.getResString("iteration_counter_arg_1")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt")); //$NON-NLS-1$
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        JMeterVariables vars = getVariables();

        boolean perThread = Boolean.parseBoolean(((CompoundVariable) variables[0]).execute());

        String varName = ""; //$NON-NLS-1$
        if (variables.length >= 2) {// Ensure variable has been provided
            varName = ((CompoundVariable) variables[1]).execute().trim();
        }

        String counterString = ""; //$NON-NLS-1$

        if (perThread) {
            counterString = String.valueOf(perThreadInt.get().addAndGet(1));
        } else {
            counterString = String.valueOf(globalCounter.addAndGet(1));
        }

        // vars will be null on Test Plan
        if (vars != null && varName.length() > 0) {
            vars.put(varName, counterString);
        }
        return counterString;
    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1, 2);
        variables = parameters.toArray();
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

    @Override
    public void threadStarted() {
        // Nothing to do on thread start
    }

    @Override
    public void threadFinished() {
        perThreadInt.remove();
    }
}
