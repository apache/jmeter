// $Header$
/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

/**
 * A function which understands BeanShell
 *
 * ALPHA CODE - liable to change without notice!
 * =============================================
 * 
 * @version    $Revision$ Updated on: $Date$
 */

public class BeanShell extends AbstractFunction implements Serializable
{

	private static Logger log = LoggingManager.getLoggerForClass();

    private static final List desc = new LinkedList();
    private static final String KEY = "__BeanShell";  //$NON-NLS-1$
    public static final String INIT_FILE = "beanshell.function.init";  //$NON-NLS-1$

	
    static {
        desc.add(JMeterUtils.getResString("bsh_function_expression"));//$NON-NLS1$
        desc.add(JMeterUtils.getResString("function_name_param"));//$NON-NLS1$
    }

    transient private Object[] values;

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

    	if (bshSet == null) // did we find BeanShell?
    	{
    		throw new InvalidVariableException("BeanShell not found");
    	}
    	
    	JMeterContext jmctx = JMeterContextService.getContext();
        JMeterVariables vars = jmctx.getVariables();

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
        	if (currentSampler != null)
        	{
				bshInvoke(bshSet,"Sampler",currentSampler);  //$NON-NLS-1$
        	}
			
			if (previousResult != null)
			{
				bshInvoke(bshSet,"SampleResult",previousResult);  //$NON-NLS-1$
			}
			
			// Allow access to context and variables directly
			bshInvoke(bshSet,"ctx",jmctx);  //$NON-NLS-1$
			bshInvoke(bshSet,"vars",vars); //$NON-NLS-1$
			bshInvoke(bshSet,"threadName",Thread.currentThread().getName());  //$NON-NLS-1$
			
            // Execute the script
            Object bshOut = bshInvoke(bshEval,script,null);
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
			log.warn("Error running BSH script",ex);
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
	transient private Method bshSet;
	transient private Method bshEval;
	transient private Method bshSource;

//TODO move to common class (in jorphan?) so can be shared with other BSH modules


	// Helper method for invoking bsh methods
	private Object bshInvoke(Method m, String s, Object o)
	{
		Object r=null;
		try {
			if (o == null)
			{
				r = m.invoke(instance, new Object[] {s});
			}
			else
			{
			    r = m.invoke(instance, new Object[] {s, o});
			}
		} catch (IllegalArgumentException e) {
			log.error("Error invoking bsh method "+m.getName()+"\n",e);
		} catch (IllegalAccessException e) {
			log.error("Error invoking bsh method "+m.getName()+"\n",e);
		} catch (InvocationTargetException e) {
			log.error("Error invoking bsh method "+m.getName()+"\n",e);
		}		
		return r;
	}

	private void setupBeanShell()
    {    
		ClassLoader loader = Thread.currentThread().getContextClassLoader();

	try
	{
		Class Interpreter = loader.loadClass("bsh.Interpreter");
		instance = Interpreter.newInstance();
		Class string = String.class;
		Class object = Object.class;
			
		bshEval = Interpreter.getMethod(
				"eval", //$NON-NLS-1$
				new Class[] {string});
		bshSet = Interpreter.getMethod(
				"set", //$NON-NLS-1$
				new Class[] {string,object});
		
		bshSource = Interpreter.getMethod(
				"source", //$NON-NLS-1$
				new Class[] {string});

	}
	catch(ClassNotFoundException e ){
		log.error("Beanshell Interpreter not found");
	}
	catch (Exception e)
	{
		log.error("Problem starting BeanShell server ",e);
	}

	// These don't vary between executes, so can be done once
	bshInvoke(bshSet,"log",log); //$NON-NLS-1$
    String initFile = JMeterUtils.getPropDefault(INIT_FILE,null);
	if (initFile!=null) bshInvoke(bshSource,initFile,null);
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
