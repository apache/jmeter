// $Header$
/*
 * Copyright 2005 The Apache Software Foundation.
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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * The function represented by this class allows data to be read from XML
 * files.  Syntax is similar to the CVSRead function.  The function allows
 * the test to line-thru the nodes in the XML file - one node per each test.
 * E.g. inserting the following in the test scripts :
 * 
 *   ${_XPath(c:/BOF/abcd.xml,/xpath/)}       // match the (first) node of 'c:/BOF/abcd.xml' , return the 1st column ( represented by the '0'),
 *   ${_XPath(c:/BOF/abcd.xml,/xpath/)}       // read (first) match of '/xpath/' expressions
 *   ${_XPath(c:/BOF/abcd.xml,/xpath/)}  // Go to next match of '/xpath/' expressions
 *
 * NOTE: A single instance of each different file is opened and used for all threads.
 *
 * 
 *
 */
public class XPath extends AbstractFunction implements Serializable
{
    transient private static final Logger log = LoggingManager.getLoggerForClass();
 //   static {
 //   LoggingManager.setPriority("DEBUG","jmeter");
//	LoggingManager.setTarget(new java.io.PrintWriter(System.out));
//    }
	private static final String KEY = "__XPath"; // Function name

    private static final List desc = new LinkedList();

    
    private Object[] values; // Parameter list
    
    static {
        desc.add(JMeterUtils.getResString("xpath_file_file_name"));
        desc.add(JMeterUtils.getResString("xpath_expression"));
    }

    public XPath()
    {
    }

    public Object clone()
    {
        XPath newReader = new XPath();
        return newReader;
    }

    /**
     * @see org.apache.jmeter.functions.Function#execute(SampleResult, Sampler)
     */
    public synchronized String execute(
        SampleResult previousResult,
        Sampler currentSampler)
        throws InvalidVariableException
    {
    	String myValue = "";

        String fileName =
                ((org.apache.jmeter.engine.util.CompoundVariable) values[0])
                    .execute();
        String xpathString =
            ((org.apache.jmeter.engine.util.CompoundVariable) values[1])
                .execute();
    

		log.debug("execute (" + fileName + " "+xpathString+")   ");


        try
        {
            myValue = XPathWrapper.getXPathString(fileName, xpathString);
        }
        catch (NumberFormatException e)
        {
            log.warn(Thread.currentThread().getName()+" - can't parse column number: " 
                      + " "+ e.toString());
        }
		catch (IndexOutOfBoundsException e)
		{
			log.warn(Thread.currentThread().getName()+" - invalid column number:  at row " +XPathWrapper.getCurrentRow(fileName) + " "
			          + e.toString());
		}

        log.debug("execute value: "+myValue);
        
        return myValue;
    }

    /**
     * @see org.apache.jmeter.functions.Function#getArgumentDesc()
     */
    public List getArgumentDesc()
    {
        return desc;
    }

    /**
     * @see org.apache.jmeter.functions.Function#getReferenceKey()
     */
    public String getReferenceKey()
    {
        return KEY;
    }
    
    /**
     * @see org.apache.jmeter.functions.Function#setParameters(Collection)
     */
    public void setParameters(Collection parameters)
        throws InvalidVariableException
    {
   		log.debug("setParameter - Collection.size=" + parameters.size());

        values = parameters.toArray();

		if (log.isDebugEnabled()){
			for (int i=0;i <parameters.size();i++){
				log.debug("i:"+((CompoundVariable)values[i]).execute());
			}
		}

        if (values.length != 2)
        {
            throw new InvalidVariableException("Wrong number of parameters; 2 != "+values.length);
        }
        
        /*
         * Need to reset the containers for repeated runs; about the only way for 
         * functions to detect that a run is starting seems to be the setParameters()
         * call.
        */
		XPathWrapper.clearAll();//TODO only clear the relevant entry - if possible...

    }
}