// $Header$
/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

package org.apache.jmeter.control;

import java.io.Serializable;

import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.StringProperty;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterThread;
import org.apache.jmeter.threads.JMeterVariables;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @version   $Revision$
 */
public class WhileController extends GenericController implements Serializable
{
    private static Logger log = LoggingManager.getLoggerForClass();
	private final static String CONDITION = "WhileController.condition"; // $NON-NLS-1$

    public WhileController()
    {
    }


    /* (non-Javadoc)
     * @see org.apache.jmeter.control.Controller#isDone()
     */
    public boolean isDone()
    {
        return false;// Never want to remove the controller from the tree
    }

    /*
     * Evaluate the condition, which can be:
     * blank or LAST = was the last sampler OK?
     * otherwise, evaluate the condition
     * @param inLoop - called by nextIsNull (within loop)
     */
    private boolean conditionTrue(boolean inLoop)
    {
    	String cnd = getCondition();
    	log.debug("Condition string:"+cnd);
    	boolean res;
    	// If blank, only check previous sample when in loop
    	if ((inLoop && cnd.length() == 0) 
    			|| "LAST".equalsIgnoreCase(cnd)) {// $NON-NLS-1$
        	JMeterVariables threadVars = 
        		JMeterContextService.getContext().getVariables();
        	// Use !false rather than true, so that null is treated as true 
       	    res = !"false".equalsIgnoreCase(threadVars.get(JMeterThread.LAST_SAMPLE_OK));// $NON-NLS-1$
    	} else {
    		// cnd may be blank if next() called us
    		res = !"false".equalsIgnoreCase(cnd);// $NON-NLS-1$
    	}
    	log.debug("Condition value: "+res);
        return res;
    }

	/* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#nextIsNull()
     */
    protected Sampler nextIsNull() throws NextIsNullException
    {
        reInitialize();
        if (conditionTrue(true))
        {
            return next();
        }
        else
        {
            setDone(true);
            return null;
        }
    }

    /*
     * This skips controller entirely if the condition is false
     * 
     * TODO consider checking for previous sampler failure here -
     * would need to distinguish this from previous failure *inside* loop 
     * 
     */
    public Sampler next()
    {
        if(conditionTrue(false))// $NON-NLS-1$
        {
            return super.next(); // OK to continue
        }
        else
        {
            reInitialize(); // Don't even start the loop
            return null;
        }
    }

	/**
	 * @param string the condition to save
	 */
	public void setCondition(String string) {
		log.debug("setCondition("+ string+")");
		setProperty(new StringProperty(CONDITION, string));
	}

	/**
	 * @return the condition
	 */
	public String getCondition() {
		String cnd;
		cnd=getPropertyAsString(CONDITION);
		log.debug("getCondition() => "+cnd);
		return cnd;
	}
}