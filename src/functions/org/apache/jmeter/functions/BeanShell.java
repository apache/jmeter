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
 * @version    $Revision$ Updated on: $Date$
 */

public class BeanShell extends AbstractFunction implements Serializable
{

	protected static Logger log = LoggingManager.getLoggerForClass();

    private static final List desc = new LinkedList();
    private static final String KEY = "__BeanShell";

	
    static {
        desc.add(JMeterUtils.getResString("bsh_function_expression"));//$NON-NLS1$
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
