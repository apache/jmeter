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
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Scriptable;

/**
 * javaScript function implementation that executes a piece of JavaScript (not Java!) code and returns its value
 * @since 1.9
 */
public class JavaScript extends AbstractFunction {

    private static final List<String> desc = new LinkedList<String>();

    private static final String KEY = "__javaScript"; //$NON-NLS-1$

    private static final Logger log = LoggingManager.getLoggerForClass();

    static {
        desc.add(JMeterUtils.getResString("javascript_expression"));//$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt")); //$NON-NLS-1$
    }

    private Object[] values;

    public JavaScript() {
    }

    /** {@inheritDoc} */
    @Override
    public String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException {

        JMeterContext jmctx = JMeterContextService.getContext();
        JMeterVariables vars = jmctx.getVariables();

        String script = ((CompoundVariable) values[0]).execute();
        // Allow variable to be omitted
        String varName = values.length < 2 ? null : ((CompoundVariable) values[1]).execute().trim();
        String resultStr = "";

        Context cx = Context.enter();
        try {

            Scriptable scope = cx.initStandardObjects(null);

            // Set up some objects for the script to play with
            scope.put("log", scope, log); //$NON-NLS-1$
            scope.put("ctx", scope, jmctx); //$NON-NLS-1$
            scope.put("vars", scope, vars); //$NON-NLS-1$
            scope.put("props", scope, JMeterUtils.getJMeterProperties()); //$NON-NLS-1$
            // Previously mis-spelt as theadName
            scope.put("threadName", scope, Thread.currentThread().getName()); //$NON-NLS-1$
            scope.put("sampler", scope, currentSampler); //$NON-NLS-1$
            scope.put("sampleResult", scope, previousResult); //$NON-NLS-1$

            Object result = cx.evaluateString(scope, script, "<cmd>", 1, null); //$NON-NLS-1$

            resultStr = Context.toString(result);
            if (varName != null && vars != null) {// vars can be null if run from TestPlan
                vars.put(varName, resultStr);
            }

        } catch (RhinoException e) {
            log.error("Error processing Javascript: [" + script + "]\n", e);
            throw new InvalidVariableException("Error processing Javascript: [" + script + "]", e);
        } finally {
            Context.exit();
        }

        return resultStr;

    }

    /** {@inheritDoc} */
    @Override
    public void setParameters(Collection<CompoundVariable> parameters) throws InvalidVariableException {
        checkParameterCount(parameters, 1, 2);
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
