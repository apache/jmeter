/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 * if any, must include the following acknowledgment:
 * "This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowledgment may appear in the software itself,
 * if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 * "Apache JMeter" must not be used to endorse or promote products
 * derived from this software without prior written permission. For
 * written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 * "Apache JMeter", nor may "Apache" appear in their name, without
 * prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
 package org.apache.jmeter.testelement.property;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.threads.JMeterContextService;

/**
 * @version $Revision$
 */
public class FunctionProperty extends AbstractProperty
{
    CompoundVariable function;
    int testIteration = -1;
    String cacheValue;

    public FunctionProperty(String name, CompoundVariable func)
    {
        super(name);
        function = func;
    }

    public FunctionProperty()
    {
        super();
    }

    public void setObjectValue(Object v)
    {
        if (v instanceof CompoundVariable && !isRunningVersion())
        {
            function = (CompoundVariable) v;
        }
        else
        {
            cacheValue = v.toString();
        }
    }

    public boolean equals(Object o)
    {
        if (o instanceof FunctionProperty)
        {
            if (function != null)
            {
                return function.equals(((JMeterProperty) o).getObjectValue());
            }
        }
        return false;
    }

    /**
     * Executes the function (and caches the value for the duration of the test
     * iteration) if the property is a running version.  Otherwise, the raw
     * string representation of the function is provided.
     * @see JMeterProperty#getStringValue()
     */
    public String getStringValue()
    {
        log.debug("Calling getStringValue from FunctionProperty");
        log.debug("boogedy boogedy");
        if (!isRunningVersion()
            || !JMeterContextService.getContext().isSamplingStarted())
        {
            log.debug("Not running version, return raw function string");
            return function.getRawParameters();
        }
        else
        {
            log.debug("Running version, executing function");
            int iter =
                JMeterContextService.getContext().getVariables().getIteration();
            if (iter < testIteration)
            {
                testIteration = -1;
            }
            if (iter > testIteration || cacheValue == null)
            {
                testIteration = iter;
                cacheValue = function.execute();
            }
            return cacheValue;
        }
    }

    /**
     * @see JMeterProperty#getObjectValue()
     */
    public Object getObjectValue()
    {
        return function;
    }

    public Object clone()
    {
        FunctionProperty prop = (FunctionProperty) super.clone();
        prop.cacheValue = cacheValue;
        prop.testIteration = testIteration;
        prop.function = function;
        return prop;
    }

    /**
     * @see JMeterProperty#recoverRunningVersion(TestElement)
     */
    public void recoverRunningVersion(TestElement owner)
    {
        cacheValue = null;
    }
}
