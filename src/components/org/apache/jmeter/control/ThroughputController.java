/*
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
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

import java.io.IOException;
import java.io.Serializable;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.FloatProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;

/**
 * @author Thad Smith
 *
 * This class represents a controller that can controll the 
 * number of times that it is executed, either by the total number
 * of times the user wants the controller executed (BYNUMBER)
 * or by the percentage of time it is called (BYPERCENT)
 */
public class ThroughputController
	extends GenericController
	implements Serializable,LoopIterationListener {

	public static final int BYNUMBER = 0;
	public static final int BYPERCENT = 1;

	private static final String STYLE = "ThroughputController.style";
	private static final String PERTHREAD = "ThroughputController.perThread";
	private static final String MAXTHROUGHPUT = "ThroughputController.maxThroughput";
	private static final String PERCENTTHROUGHPUT = "ThroughputController.percentThroughput";

	private IntegerWrapper globalNumExecutions;
	private IntegerWrapper globalIteration;
	private transient Object numExecutionsLock;
	private transient Object iterationLock;
	
	private int numExecutions, iteration;
	private boolean returnTrue;

	public ThroughputController() 
	{
		globalNumExecutions = new IntegerWrapper();
		globalIteration = new IntegerWrapper();
		numExecutionsLock = new Object();
		iterationLock = new Object();
		setStyle(BYNUMBER);
		setPerThread(true);
		setMaxThroughput(1);
		setPercentThroughput(100);
		returnTrue = false;
	}

	public void initialize() 
	{
		setExecutions(0);
		setIteration(-1);
		super.initialize();
	}
	
	public void reInitialize()
	{
		returnTrue = false;
		super.reInitialize();
	}
	
	public void setStyle(int style) 
	{
		setProperty(new IntegerProperty(STYLE,style));
	}

	public int getStyle() 
	{
		return getPropertyAsInt(STYLE);
	}
	
	public void setPerThread(boolean perThread)
	{
		setProperty(new BooleanProperty(PERTHREAD,perThread));
	}
	
	public boolean isPerThread()
	{
		return getPropertyAsBoolean(PERTHREAD);
	}
	
	public void setMaxThroughput(int maxThroughput)
	{
		setProperty(new IntegerProperty(MAXTHROUGHPUT,maxThroughput));
	}
	
	public int getMaxThroughput() 
	{
		return getPropertyAsInt(MAXTHROUGHPUT);
	}
	
	public void setPercentThroughput(float percentThroughput)
	{
		setProperty(new FloatProperty(PERCENTTHROUGHPUT,percentThroughput));
	}
	
	public float getPercentThroughput()
	{
		return getPropertyAsFloat(PERCENTTHROUGHPUT);
	}
	
	protected void setExecutions(int executions)
	{
		if (!isPerThread()) 
		{
			globalNumExecutions.setInteger(new Integer(executions));
		}
		this.numExecutions = executions;
	}
	
	protected int getExecutions()
	{
		if (!isPerThread())
			return globalNumExecutions.getInteger().intValue();
		else
			return numExecutions;
	}
	
	
	private void increaseExecutions() 
	{
		setExecutions(getExecutions()+1);
	}
	
	protected void setIteration(int iteration)
	{
		if (!isPerThread())
			globalIteration.setInteger(new Integer(iteration));
		this.iteration = iteration;
	}
	
	protected int getIteration()
	{
		if (!isPerThread())
			return globalIteration.getInteger().intValue();
		else
			return iteration;
	}
	
	private void increaseIteration() 
	{
		setIteration(getIteration()+1); 
	}

	
	/**
	 * @see org.apache.jmeter.control.Controller#next()
	 */
	public Sampler next()
	{
		Sampler retVal = null;
		if ( getSubControllers().size()>0 && current<getSubControllers().size() )
		{
			if (!isPerThread())
			{
				synchronized(numExecutionsLock)
				{
					if ( canExecute() ) 
					{
						retVal = super.next();
					}
				}
			}
			else
			{
				if ( canExecute() ) 
				{
					retVal = super.next();
				}
			}
		}
		return retVal;
	}

	public boolean canExecute() 
	{
		if (returnTrue)
			return true;

		int executions, iterations;
		boolean retval = false;

		executions = getExecutions();
		iterations = getIteration();
		if ( getStyle() == BYNUMBER )
		{ 
			if ( executions < getMaxThroughput() )
			{
				retval = true;
			}
		}
		else 
		{
			if (iteration==0 && getPercentThroughput()>0)
				retval = true;
			else if ( (executions/iteration)*100 <= getPercentThroughput() )
				retval = true;
		}
		if (retval)
		{
			returnTrue = true;
			increaseExecutions();
		}
		return retval;
	}

	/**
	 * @see org.apache.jmeter.control.Controller#isDone()
	 */
	public boolean isDone() 
	{
		if ( subControllersAndSamplers.size()==0 )
			return true;
		else if ( getStyle()==BYNUMBER && getExecutions()>=getMaxThroughput() ) 
			return true;
		else
			return false;
	}
	
	public Object clone() 
	{
		ThroughputController clone = (ThroughputController) super.clone();
		clone.numExecutions = numExecutions;
		clone.iteration = iteration;	
		clone.globalNumExecutions = globalNumExecutions;
		clone.globalIteration = globalIteration;
		clone.numExecutionsLock = numExecutionsLock;
		clone.iterationLock = iterationLock;
		clone.returnTrue = false;
		return clone;
	}
	
	private void readObject(java.io.ObjectInputStream in)
		throws IOException, ClassNotFoundException 	
	{
		in.defaultReadObject();
		numExecutionsLock = new Object();
		iterationLock = new Object();
	}


    /* (non-Javadoc)
     * @see org.apache.jmeter.engine.event.LoopIterationListener#iterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
     */
    public void iterationStart(LoopIterationEvent iterEvent)
    {
    	if (!isPerThread()) 
    	{
    		synchronized (iterationLock)
    		{
    			increaseIteration();
    		}
    	}
    	else
    	{
	        increaseIteration();
    	}
        reInitialize();
    }
    
	protected class IntegerWrapper
		implements Serializable
	{
		Integer i;

		public IntegerWrapper() {}
	
		public IntegerWrapper(Integer i)
		{
			this.i = i;
		}
	
		public void setInteger(Integer i)
		{
			System.out.println(((Object)this).toString());
			this.i = i;
		}
	
		public Integer getInteger()
		{
			return i;
		}
	}

}

