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
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.BeanShellInterpreter;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A function which understands BeanShell
 * @since 1.X
 */
public class BeanShell extends AbstractFunction {

    private static final Logger log = LoggerFactory.getLogger(BeanShell.class);

    private static final List<String> desc = new LinkedList<>();

    private static final String KEY = "__BeanShell"; //$NON-NLS-1$

    public static final String INIT_FILE = "beanshell.function.init"; //$NON-NLS-1$

    static {
        desc.add(JMeterUtils.getResString("bsh_function_expression"));// $NON-NLS1$
        desc.add(JMeterUtils.getResString("function_name_paropt"));// $NON-NLS1$
    }

    private Object[] values;

    private BeanShellInterpreter bshInterpreter = null;

    public BeanShell() {
    }

    /** {@inheritDoc} */
    @Override
    public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        if (bshInterpreter == null) // did we find BeanShell?
        {
            throw new InvalidVariableException("BeanShell not found");
        }

        JMeterContext jmctx = JMeterContextService.getContext();
        JMeterVariables vars = jmctx.getVariables();

        String script = ((CompoundVariable) values[0]).execute();
        String varName = ""; //$NON-NLS-1$
        if (values.length > 1) {
            varName = ((CompoundVariable) values[1]).execute().trim();
        }

        String resultStr = ""; //$NON-NLS-1$
        try {

            // Pass in some variables
            if (currentSampler != null) {
                bshInterpreter.set("Sampler", currentSampler); //$NON-NLS-1$
            }

            if (previousResult != null) {
                bshInterpreter.set("SampleResult", previousResult); //$NON-NLS-1$
            }

            // Allow access to context and variables directly
            bshInterpreter.set("ctx", jmctx); //$NON-NLS-1$
            bshInterpreter.set("vars", vars); //$NON-NLS-1$
            bshInterpreter.set("props", JMeterUtils.getJMeterProperties()); //$NON-NLS-1$
            bshInterpreter.set("threadName", Thread.currentThread().getName()); //$NON-NLS-1$

            // Execute the script
            Object bshOut = bshInterpreter.eval(script);
            if (bshOut != null) {
                resultStr = bshOut.toString();
            }
            if (vars != null && varName.length() > 0) {// vars will be null on TestPlan
                vars.put(varName, resultStr);
            }
        } catch (Exception ex) // Mainly for bsh.EvalError
        {
            log.warn("Error running BSH script", ex);
        }
        log.debug("__Beanshell({},{})={}", script, varName, resultStr);
        return resultStr;

    }

    /*
     * Helper method for use by scripts
     *
     */
    public void log_info(String s) {
        log.info(s);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {

        checkParameterCount(parameters, 1, 2);

        values = parameters.toArray();

        try {
            bshInterpreter = new BeanShellInterpreter(JMeterUtils.getProperty(INIT_FILE), log);
        } catch (ClassNotFoundException e) {
            throw new InvalidVariableException("BeanShell not found", e);
        }
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
