/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001,2003 The Apache Software Foundation.  All rights
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

package org.apache.jmeter.control;

import java.io.Serializable;

import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.threads.JMeterContext;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.testelement.property.StringProperty;

/**
 * @author    Dolf Smits
 * @author    Michael Stover
 * @author    Thad Smith
 * @version   $Revision$
 */
public class ForeachController extends GenericController implements Serializable
{

    private final static String INPUTVAL = "ForeachController.inputVal";
    private final static String RETURNVAL ="ForeachController.returnVal";
    private int loopCount = 0;

    public ForeachController()
    {
    }

    public void initialize()
    {
        log.debug("Initilizing ForEach");
    }
    
    
    public void setInputVal(String inputValue)
    {
        setProperty(new StringProperty(INPUTVAL, inputValue));
    }

    public String getInputValString()
    {
        return getPropertyAsString(INPUTVAL);
    }

    public void setReturnVal(String inputValue)
    {
        setProperty(new StringProperty(RETURNVAL, inputValue));
    }

    public String getReturnValString()
    {
        return getPropertyAsString(RETURNVAL);
    }

   /* (non-Javadoc)
     * @see org.apache.jmeter.control.Controller#isDone()
     */
    public boolean isDone()
    {
        JMeterContext context = JMeterContextService.getContext();
    	String inputVariable=getInputValString()+"_"+(loopCount+1);
    	if (context.getVariables().get(inputVariable) != null) 
    	{
    	   context.getVariables().put(getReturnValString(), context.getVariables().get(inputVariable));
                   log.debug("ForEach resultstring isDone="+context.getVariables().get(getReturnValString()));
    	   return false;
    	} 
        return super.isDone();
    }

    private boolean endOfArguments()
    {
        JMeterContext context = JMeterContextService.getContext();
    	String inputVariable=getInputValString()+"_"+(loopCount+1);
    	if (context.getVariables().get(inputVariable) != null) 
    	{
           log.debug("ForEach resultstring eofArgs= false");
    	   return false;
    	} else {
           log.debug("ForEach resultstring eofArgs= true");
    	   return true;
    	}
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#nextIsNull()
     */
    protected Sampler nextIsNull() throws NextIsNullException
    {
        reInitialize();
        if (endOfArguments())
        {
//           setDone(true);
           resetLoopCount();
           return null;
        }
        else
        {
            return next();
        }
    }

    protected void incrementLoopCount()
    {
        loopCount++;
    }

    protected void resetLoopCount()
    {
        loopCount = 0;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#getIterCount()
     */
    protected int getIterCount()
    {
        return loopCount + 1;
    }

	/* (non-Javadoc)
	 * @see org.apache.jmeter.control.GenericController#reInitialize()
	 */
	protected void reInitialize()
	{
		setFirst(true);
		resetCurrent();
		incrementLoopCount();
	}
}