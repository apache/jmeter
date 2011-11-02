/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jmeter.functions;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.Script;
import org.apache.commons.jexl.ScriptFactory;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A function which understands Commons JEXL
 */
// For unit tests, see TestJexlFunction
public class JexlFunction extends AbstractFunction {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private static final String KEY = "__jexl"; //$NON-NLS-1$

    private static final List<String> desc = new LinkedList<String>();

    static
    {
        desc.add(JMeterUtils.getResString("jexl_expression")); //$NON-NLS-1$
        desc.add(JMeterUtils.getResString("function_name_paropt"));// $NON-NLS1$
    }

    private Object[] values;

    /** {@inheritDoc} */
    @Override
    public synchronized String execute(SampleResult previousResult, Sampler currentSampler)
            throws InvalidVariableException
    {
        String str = ""; //$NON-NLS-1$

        CompoundVariable var = (CompoundVariable) values[0];
        String exp = var.execute();

        String varName = ""; //$NON-NLS-1$
        if (values.length > 1) {
            varName = ((CompoundVariable) values[1]).execute().trim();
        }

        JMeterContext jmctx = JMeterContextService.getContext();
        JMeterVariables vars = jmctx.getVariables();

        try
        {
            Script script = ScriptFactory.createScript(exp);
            JexlContext jc = JexlHelper.createContext();
            @SuppressWarnings("unchecked")
            final Map<String, Object> jexlVars = jc.getVars();
            jexlVars.put("log", log); //$NON-NLS-1$
            jexlVars.put("ctx", jmctx); //$NON-NLS-1$
            jexlVars.put("vars", vars); //$NON-NLS-1$
            jexlVars.put("props", JMeterUtils.getJMeterProperties()); //$NON-NLS-1$
            // Previously mis-spelt as theadName
            jexlVars.put("threadName", Thread.currentThread().getName()); //$NON-NLS-1$
            jexlVars.put("sampler", currentSampler); //$NON-NLS-1$ (may be null)
            jexlVars.put("sampleResult", previousResult); //$NON-NLS-1$ (may be null)
            jexlVars.put("OUT", System.out);//$NON-NLS-1$

            // Now evaluate the script, getting the result
            Object o = script.execute(jc);
            if (o != null)
            {
                str = o.toString();
            }
            if (vars != null && varName.length() > 0) {// vars will be null on TestPlan
                vars.put(varName, str);
            }
        } catch (Exception e)
        {
            log.error("An error occurred while evaluating the expression \""
                    + exp + "\"\n",e);
        }
        return str;
    }

    /** {@inheritDoc} */
    public List<String> getArgumentDesc()
    {
        return desc;
    }

    /** {@inheritDoc} */
    @Override
    public String getReferenceKey()
    {
        return KEY;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setParameters(Collection<CompoundVariable> parameters)
            throws InvalidVariableException
    {
        checkParameterCount(parameters, 1, 2);
        values = parameters.toArray();
    }

}