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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.jmeter.engine.event.IterationEvent;
import org.apache.jmeter.engine.event.IterationListener;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.PerSampleClonable;
import org.apache.jmeter.testelement.PerThreadClonable;
import org.apache.jmeter.testelement.TestElement;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class GenericController extends AbstractTestElement implements Controller,
		Serializable,PerThreadClonable
{
    protected List iterationListeners = new LinkedList();
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	protected List subControllersAndSamplers = new ArrayList();
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	protected int current = 0;
	/****************************************
	 * !ToDo (Field description)
	 ***************************************/
	protected Iterator controlIt;
	private List configs = new LinkedList();
	private boolean returnedNull = false;
	private boolean done = false, timeForNext = false;
	private List assertions = new LinkedList();
    private boolean first = true;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public GenericController()
	{
		
	}
    
    public void addIterationListener(IterationListener lis)
    {
        iterationListeners.add(lis);
    }
	
	public boolean isNextFirst()
	{
		return first;
	}

	/****************************************
	 * Gets the ConfigElements attribute of the GenericController object
	 *
	 *@return   The ConfigElements value
	 ***************************************/
	protected List getConfigElements()
	{
		return configs;
	}

	private void addConfigElement(TestElement el)
	{
		configs.add(el);
	}

	public void initialize()
	{
        first = true;
		resetCurrent();
	}

	public void reInitialize()
	{
		resetCurrent();
	}

	protected void removeCurrentController()
	{
		subControllersAndSamplers.remove(current);
	}

	protected void resetCurrent()
	{
        first = true;
		current = 0;
	}

	protected void incrementCurrent()
	{
		current++;
	}

	/**
	 * Answers the question: when the end of subcontrollers and samplers is reached,
	 * how does this Controller answert the question: hasNext()?  For most controllers,
	 * the answer is to return false.  For some, it depends.  The LoopController, for
	 * instance will repeat the list of subcontrollers a given number of times
	 * before signalling false to 'hasNext()'.
	 */
	protected boolean hasNextAtEnd()
	{
		return false;
	}

	protected void nextAtEnd()
	{
		resetCurrent();
	}

	public boolean hasNext()
	{
		boolean retVal;
		Object controller = getCurrentController();
		if(controller == null)
		{
			retVal = hasNextAtEnd();
		}
		else if(controller instanceof Controller)
		{
			if(((Controller)controller).hasNext())
			{
				retVal = true;
			}
			else
			{
				currentHasNextIsFalse();
				retVal = hasNext();
			}
		}
		else
		{
			retVal = true;
		}
		if(!retVal)
		{
			reInitialize();
		}
		return retVal;
	}

	protected void currentHasNextIsFalse()
	{
		if(((Controller)getCurrentController()).isDone())
		{
			removeCurrentController();
		}
		else
		{
			incrementCurrent();
		}
	}

	protected boolean shortCircuitIsDone()
	{
		return done;
	}

	protected void setShortCircuit(boolean done)
	{
		this.done = done;
	}

	public boolean isDone()
	{
		if(shortCircuitIsDone())
		{
			return true;
		}
		boolean isdone = true;
		Iterator iter = subControllersAndSamplers.iterator();
		while (iter.hasNext())
		{
			Object item = iter.next();
			if(item instanceof Sampler)
			{
				return false;
			}
			else
			{
				isdone = isdone && ((Controller)item).isDone();
			}
		}
		setShortCircuit(isdone);
		return isdone;
	}

	protected TestElement getCurrentController()
	{
		if(current < subControllersAndSamplers.size())
		{
			return (TestElement)subControllersAndSamplers.get(current);
		}
		else return null;
	}

	/****************************************
	 * Gets the SubControllers attribute of the GenericController object
	 *
	 *@return   The SubControllers value
	 ***************************************/
	protected List getSubControllers()
	{
		return subControllersAndSamplers;
	}


	/****************************************
	 * !ToDo
	 *
	 *@param child  !ToDo
	 ***************************************/
	public void addTestElement(TestElement child)
	{
		if(child instanceof Controller || child instanceof Sampler)
		{
			addController(child);
		}
	}

	private void addController(TestElement child)
	{
		subControllersAndSamplers.add(child);
	}

	/****************************************
	 * Retrieves the next Entry to be sampled.
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public Sampler next()
	{
        fireIterEvents();     
		TestElement controller = getCurrentController();
		if(controller == null)
		{
			nextAtEnd();
			return next();
		}
		if(controller instanceof Sampler)
		{
			incrementCurrent();
			return (Sampler)controller;
		}
		else
		{
			Controller c = (Controller)controller;
			if(c.hasNext())
			{
				Sampler s = c.next();
				return s;
			}
			else if(c.isDone())
			{
				removeCurrentController();
				return next();
			}
			else
			{
				incrementCurrent();
				return next();
			}
		}
	}

    protected void fireIterEvents()
    {
        if(isNextFirst())
        {
            fireIterationStart();
            first = false;
        }  
        fireIteration();
    }
    
    protected int getIterCount()
    {
        return 1;
    }
    
    protected void fireIterationStart()
    {
        Iterator iter = iterationListeners.iterator();
        while (iter.hasNext())
        {
            IterationListener item = (IterationListener)iter.next();
            item.iterationStart(new IterationEvent(this,getIterCount()));            
        }
    }
    
    protected void fireIteration() {
    	Iterator iter = iterationListeners.iterator();
    	while (iter.hasNext())
    	{
    		IterationListener item = (IterationListener)iter.next();
    		item.iteration(new IterationEvent(this,getIterCount()));
    	}
    }

	public static class Test extends junit.framework.TestCase
	{
		public Test(String name)
		{
			super(name);
		}

		public void testProcessing() throws Exception
		{
			GenericController controller = new GenericController();
			GenericController sub_1 = new GenericController();
			sub_1.addTestElement(makeSampler("one"));
			sub_1.addTestElement(makeSampler("two"));
			controller.addTestElement(sub_1);
			controller.addTestElement(makeSampler("three"));
			GenericController sub_2 = new GenericController();
			GenericController sub_3 = new GenericController();
			sub_2.addTestElement(makeSampler("four"));
			sub_3.addTestElement(makeSampler("five"));
			sub_3.addTestElement(makeSampler("six"));
			sub_2.addTestElement(sub_3);
			sub_2.addTestElement(makeSampler("seven"));
			controller.addTestElement(sub_2);
			String[] order = new String[]{"one","two","three","four","five","six","seven"};
			int counter = 7;
			for (int i = 0; i < 2; i++)
			{
				assertEquals(7,counter);
				counter = 0;
				while(controller.hasNext())
				{
					TestElement sampler = controller.next();
					assertEquals(order[counter++],sampler.getPropertyAsString(TestElement.NAME));
				}
			}
		}

		private TestElement makeSampler(String name)
		{
			TestSampler s = new TestSampler();
			s.setName(name);
			return s;
		}
		class TestSampler extends AbstractSampler implements PerSampleClonable {
		  public void addCustomTestElement(TestElement t) { }
		  public org.apache.jmeter.samplers.SampleResult sample(org.apache.jmeter.samplers.Entry e) { return null; }
		}
	}
}
