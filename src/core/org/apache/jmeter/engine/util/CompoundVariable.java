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

package org.apache.jmeter.engine.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.jmeter.functions.Function;
import org.apache.jmeter.functions.InvalidVariableException;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.log.Logger;
import org.apache.oro.text.regex.Perl5Compiler;


/**
 * CompoundFunction.
 *
 * @author mstover
 * @version $Id$
 */
public class CompoundVariable implements Function
{
    transient private static Logger log =
        LoggingManager.getLoggerForClass();
        
    private String rawParameters;
    
    //private JMeterVariables threadVars;
    //private Map varMap = new HashMap();
    
    static FunctionParser functionParser = new FunctionParser();

    static Map functions = new HashMap();
    private boolean hasFunction, isDynamic;
    private String staticSubstitution;
    //private Perl5Util util = new Perl5Util();
    private Perl5Compiler compiler = new Perl5Compiler();
    private static final String unescapePattern = "[\\\\]([${}\\\\,])";
    private String permanentResults = "";
    
    LinkedList compiledComponents = new LinkedList();

    static {
        try
        {
            List classes =
                ClassFinder.findClassesThatExtend(
                    JMeterUtils.getSearchPaths(),
                    new Class[] { Function.class },
                    true);
            Iterator iter = classes.iterator();
            while (iter.hasNext())
            {
                Function tempFunc =
                    (Function) Class
                        .forName((String) iter.next())
                        .newInstance();
                functions.put(tempFunc.getReferenceKey(), tempFunc.getClass());
            }
        }
        catch (Exception err)
        {
            log.error("", err);
        }
    }


    public CompoundVariable()
    {
        super();
        isDynamic = true;
        hasFunction = false;
        staticSubstitution = "";
    }
    
    public CompoundVariable(String parameters)
    {
        this();
        try
        {
            setParameters(parameters);
        }
        catch (InvalidVariableException e)
        {
        }
    }

    public String execute()
    {
        if (isDynamic)
        {
            JMeterContext context = JMeterContextService.getContext();
            SampleResult previousResult = context.getPreviousResult();
            Sampler currentSampler = context.getCurrentSampler();
            return execute(previousResult, currentSampler);
        }
        else
        {
            return permanentResults;
        }
    }
    
    /**
     * Allows the retrieval of the original String prior to it being compiled.
     * @return String
     */
    public String getRawParameters()
    {
        return rawParameters;
    }
 
    /* (non-Javadoc)
     * @see Function#execute(SampleResult, Sampler)
     */
    public String execute(SampleResult previousResult, Sampler currentSampler)
    {
        if (compiledComponents == null || compiledComponents.size() == 0)
        {
            return "";
        }
        boolean testDynamic = false;
        StringBuffer results = new StringBuffer();
        Iterator iter = compiledComponents.iterator();
        while (iter.hasNext())
        {
            Object item = iter.next();
            log.debug("executing object: " + item);
            if (item instanceof Function)
            {
                testDynamic = true;
                try
                {
                    results.append(
                        ((Function) item).execute(
                            previousResult,
                            currentSampler));
                }
                catch (InvalidVariableException e)
                {
                }
            }
            else if (item instanceof SimpleVariable)
            {
                testDynamic = true;
                results.append(((SimpleVariable) item).toString());
            }
            else
            {
                results.append(item);
            }
        }
        if(!testDynamic)
        {
            isDynamic = false;
            permanentResults = results.toString();
        }
        return results.toString();
    }

    public CompoundVariable getFunction()
    {
        CompoundVariable func = new CompoundVariable();
        func.compiledComponents = (LinkedList) compiledComponents.clone();
        func.rawParameters = rawParameters;
        return func;
    }

    public List getArgumentDesc()
    {
        return new LinkedList();
    }

    public void clear()
    {
        hasFunction = false;
        compiledComponents.clear();
        staticSubstitution = "";
    }

    public void setParameters(String parameters)
        throws InvalidVariableException
    {
        this.rawParameters = parameters;
        if (parameters == null || parameters.length() == 0)
            return;

        compiledComponents = functionParser.compileString(parameters);
        if(compiledComponents.size() > 1 || !(compiledComponents.get(0) instanceof String))
        {
            hasFunction = true;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#setParameters(Collection)
     */
    public void setParameters(Collection parameters)
        throws InvalidVariableException
    {
    }
    
    static Object getNamedFunction(String functionName) throws InvalidVariableException
    {
        if(functions.containsKey(functionName))
        {
            try
            {
                return (Function) ((Class) functions.get(functionName)).newInstance();
            }
            catch (Exception e)
            {
                log.error("", e);
                 throw new InvalidVariableException();
            }
        }
        else
        {
            return new SimpleVariable(functionName);
        }
    }

    public boolean hasFunction()
    {
        return hasFunction;
    }

    /**
     * @see Function#getReferenceKey()
     */
    public String getReferenceKey()
    {
        return "";
    }
       
    private JMeterVariables getVariables() //TODO: not used
    {
        return JMeterContextService.getContext().getVariables();
    }

}
