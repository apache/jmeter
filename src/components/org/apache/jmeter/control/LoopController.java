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
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.testelement.TestElement;
/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class LoopController extends GenericController implements Serializable
{
	private final static String LOOPS = "LoopController.loops";
	private final static String CONTINUE_FOREVER = "LoopController.continue_forever";
	private int loopCount = 0;

	/****************************************
	 * !ToDo (Constructor description)
	 ***************************************/
	public LoopController()
	{
		setContinueForever(true);
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param loops  !ToDo (Parameter description)
	 ***************************************/
	public void setLoops(int loops)
	{
		setProperty(LOOPS,new Integer(loops));
	}

	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public int getLoops()
	{
		return getPropertyAsInt(LOOPS);
	}

	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param forever  !ToDo (Parameter description)
	 ***************************************/
	public void setContinueForever(boolean forever)
	{
		setProperty(CONTINUE_FOREVER,new Boolean(forever));
	}


	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public boolean getContinueForever()
	{
		return getPropertyAsBoolean(CONTINUE_FOREVER);
	}

	public void initialize()
	{
		super.initialize();
		resetLoopCount();
	}

	public void reInitialize()
	{
		super.reInitialize();
		resetLoopCount();
	}

	protected void incrementLoopCount()
	{
		loopCount++;
	}

	protected void resetLoopCount()
	{
		if(!getContinueForever() && getLoops() > -1)
		{
			this.setShortCircuit(true);
		}
		else
		{
			loopCount = 0;
		}
	}

	protected boolean hasNextAtEnd()
	{
		resetCurrent();
		incrementLoopCount();
		if(endOfLoop())
		{
			return false;
		}
		else
		{
			return hasNext();
		}
	}

	protected void nextAtEnd()
	{
		resetCurrent();
		incrementLoopCount();
	}

	private boolean endOfLoop()
	{
		return (!getContinueForever() || getLoops() > -1) && loopCount >= getLoops();
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
			LoopController sub_2 = new LoopController();
			sub_2.setLoops(3);
			GenericController sub_3 = new GenericController();
			sub_2.addTestElement(makeSampler("four"));
			sub_3.addTestElement(makeSampler("five"));
			sub_3.addTestElement(makeSampler("six"));
			sub_2.addTestElement(sub_3);
			sub_2.addTestElement(makeSampler("seven"));
			controller.addTestElement(sub_2);
			String[] order = new String[]{"one","two","three","four","five","six","seven",
						"four","five","six","seven","four","five","six","seven"};
			int counter = 15;
			for (int i = 0; i < 2; i++)
			{
				assertEquals(15,counter);
				counter = 0;
				while(controller.hasNext())
				{
					TestElement sampler = controller.next();
					assertEquals(order[counter++],sampler.getProperty(TestElement.NAME));
				}
			}
		}

		private TestElement makeSampler(String name)
		{
			org.apache.jmeter.protocol.http.sampler.HTTPSampler s = new
					org.apache.jmeter.protocol.http.sampler.HTTPSampler();
			s.setName(name);
			return s;
		}
	}
}
