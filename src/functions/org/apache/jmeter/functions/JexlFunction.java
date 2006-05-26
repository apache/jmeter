/*
 * Copyright 2004-2005 The Apache Software Foundation or its licensors,
 *                     as applicable.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

    private static final String KEY = "__jexl";

    private static final List desc = new LinkedList();

    static
    {
        desc.add("expression");
    }

    private Object[] values;

    public String execute(SampleResult result, Sampler sampler)
            throws InvalidVariableException
    {
        String str = "";

        CompoundVariable var = (CompoundVariable) values[0];
        String exp = var.getRawParameters();

        try
        {
            Expression e = ExpressionFactory.createExpression(exp);
            JexlContext jc = JexlHelper.createContext();
            jc.getVars().put("ctx", sampler.getThreadContext());
            jc.getVars().put("vars", getVariables());
            jc.getVars().put("theadName", sampler.getThreadName());
            jc.getVars().put("sampler", sampler);
            jc.getVars().put("sampleResult", result);

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

    public void setParameters(Collection parameters)
            throws InvalidVariableException
    {
        values = parameters.toArray();
        if (values.length != 1)
        {
            throw new InvalidVariableException("it only accepts one parameter");
        }
    }

}