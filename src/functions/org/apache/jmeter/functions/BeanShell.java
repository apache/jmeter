/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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

package org.apache.jmeter.functions;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * A function which understands BeanShell
 *
 * ALPHA CODE - liable to change without notice!
 * =============================================
 * 
 * @author sebb AT apache DOT org
 * @version    $Revision$ Updated on: $Date$
 */

public class BeanShell extends AbstractFunction implements Serializable
{

	protected static Logger log = LoggingManager.getLoggerForClass();

    private static final List desc = new LinkedList();
    private static final String KEY = "__BeanShell";

	
    static {
        desc.add("Expression to evaluate");//TODO make property
        desc.add(JMeterUtils.getResString("function_name_param"));//$NON-NLS1$
    }

    private Object[] values;

    public BeanShell()
    {
    }

    public Object clone()
    {
        return new BeanShell();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
     */
    public synchronized String execute(
        SampleResult previousResult,
        Sampler currentSampler)
        throws InvalidVariableException
    {

        JMeterVariables vars = getVariables();

        String script  = ((CompoundVariable) values[0]).execute();
        String varName = "";
        if (values.length > 1){
			varName = ((CompoundVariable) values[1]).execute();
        }
        
        String resultStr = "";
        
		log.debug("Script="+script);

        try
        {

			// Pass in some variables
        	if (currentSampler != null) {
				setObj.invoke(instance, new Object[] {"Sampler",currentSampler});
        	}
			
			if (previousResult != null)
			{
				setObj.invoke(instance, new Object[] {"SampleResult",previousResult});
			}
				
			setObj.invoke(instance, new Object[] {"log",log});
			setObj.invoke(instance, new Object[] {"t",this});
			
            // Execute the script
            Object bshOut = eval.invoke(instance, new Object[]{script});
			if (bshOut != null) {
				resultStr = bshOut.toString();
			}
			if (varName.length() > 0)
			{
				vars.put(varName, resultStr);
			}
        }
		catch (Exception ex) // Mainly for bsh.EvalError
		{
			log.warn("",ex);
		}

        log.debug("Output="+resultStr);
        return resultStr;

    }

    /*
     * Helper method for use by scripts 
     *
     */
    public void log_info(String s){
    	log.info(s);
    }

	transient private Object instance;
	transient private Method setObj;
	transient private Method eval;

//TODO move to common class (in jorphan?) so can be shared with other BSH modules

    private void setupBeanShell()
    {    
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

	try
	{
		Class Interpreter = loader.loadClass("bsh.Interpreter");
		instance = Interpreter.newInstance();
		Class string = String.class;
		Class object = Object.class;
			
		eval = Interpreter.getMethod(
				"eval",
				new Class[] {string});
		setObj = Interpreter.getMethod(
				"set",
				new Class[] {string,object});
	}
	catch(ClassNotFoundException e ){
		log.error("Beanshell Interpreter not found");
	}
	catch (Exception e)
	{
		log.error("Problem starting BeanShell server ",e);
	}

	}
	
    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#setParameters(Collection)
     */
    public void setParameters(Collection parameters)
        throws InvalidVariableException
    {

        values = parameters.toArray();

        if (values.length < 1 || values.length > 2)
        {
            throw new InvalidVariableException("Wrong number of variables");
        }
        
        setupBeanShell();

    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#getReferenceKey()
     */
    public String getReferenceKey()
    {
        return KEY;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.functions.Function#getArgumentDesc()
     */
    public List getArgumentDesc()
    {
        return desc;
    }

}
