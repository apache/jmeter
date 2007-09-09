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

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A function which understands Commons JEXL
 */
public class JexlFunction extends AbstractFunction implements Serializable
{
    /**
     * <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 3546359539474968625L;

    private static Logger log = LoggingManager.getLoggerForClass();

    private static final String KEY = "__jexl"; //$NON-NLS-1$

    private static final List desc = new LinkedList();

    static
    {
        desc.add(JMeterUtils.getResString("jexl_expression")); //$NON-NLS-1$
    }

    private Object[] values;

    public synchronized String execute(SampleResult result, Sampler sampler)
            throws InvalidVariableException
    {
        String str = ""; //$NON-NLS-1$

        CompoundVariable var = (CompoundVariable) values[0];
        String exp = var.execute();

        try
        {
            Expression e = ExpressionFactory.createExpression(exp);
            JexlContext jc = JexlHelper.createContext();
            jc.getVars().put("ctx", sampler.getThreadContext()); //$NON-NLS-1$
            jc.getVars().put("vars", getVariables()); //$NON-NLS-1$
            jc.getVars().put("theadName", sampler.getThreadName()); //$NON-NLS-1$
            jc.getVars().put("sampler", sampler); //$NON-NLS-1$
            jc.getVars().put("sampleResult", result); //$NON-NLS-1$

            // Now evaluate the expression, getting the result
            Object o = e.evaluate(jc);
            if (o != null)
            {
                str = o.toString();
            }
        } catch (Exception e)
        {
            log.error("An error occurred while evaluating the expression \""
                    + exp + "\"");
        }
        return str;
    }

    public List getArgumentDesc()
    {
        return desc;
    }

    public String getReferenceKey()
    {
        return KEY;
    }

    public synchronized void setParameters(Collection parameters)
            throws InvalidVariableException
    {
        values = parameters.toArray();
        if (values.length != 1)
        {
            throw new InvalidVariableException("it only accepts one parameter");
        }
    }

}