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
import java.io.*;
import java.util.*;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.samplers.*;
import org.apache.jmeter.util.JMeterUtils;

/****************************************
 * Title: Description: Copyright: Copyright (c) 2001 Company:
 *
 *@author    Michael Stover
 *@created   March 13, 2001
 *@version   1.0
 ***************************************/

public class InterleaveControl extends GenericController implements Serializable
{

	private static final String STYLE = "InterleaveControl.style";
	public static final int DEFAULT_STYLE = 0;
	public static final int NEW_STYLE = 1;
	private boolean interleave;
	private boolean doNotIncrement = false;

	/****************************************
	 * Constructor for the InterleaveControl object
	 ***************************************/
	public InterleaveControl()
	{
		
	}

	public void initialize()
	{
		super.initialize();
		interleave = false;
	}

	public void reInitialize()
	{
		super.initialize();
		interleave = false;
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
		if(controller == null)
		{
			reInitialize();
		}
		if(interleave)
		{
			interleave = false;
			return false;
		}
		return retVal;
	}
	
	protected void removeCurrentController()
	{
		setInterleave(NEW_STYLE);
		super.removeCurrentController();
	}

	protected void incrementCurrent()
	{
		setInterleave(NEW_STYLE);
		super.incrementCurrent();
	}

	protected void setInterleave(int style)
	{
		if(getStyle() == style)
		{
			interleave = true;
		}
	}
	
	public void setStyle(int style)
	{
		setProperty(STYLE,new Integer(style));
	}
	
	public int getStyle()
	{
		return getPropertyAsInt(STYLE);
	}

	public Sampler next()
	{
		setInterleave(DEFAULT_STYLE);
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
				if(getStyle() == DEFAULT_STYLE)
				{
					incrementCurrent();
				}
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

	public static class Test extends junit.framework.TestCase
	{
		public Test(String name)
		{
			super(name);
		}

		public void testProcessing() throws Exception
		{
			GenericController controller = new GenericController();
			InterleaveControl sub_1 = new InterleaveControl();
			sub_1.setStyle(DEFAULT_STYLE);
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
			String[] interleaveOrder = new String[]{"one","two"};
			String[] order = new String[]{"dummy","three","four","five","six","seven",
						"four","five","six","seven","four","five","six","seven"};
			int counter = 14;
			for (int i = 0; i < 4; i++)
			{
				assertEquals(14,counter);
				counter = 0;
				while(controller.hasNext())
				{
					TestElement sampler = controller.next();
					if(counter == 0)
					{
						assertEquals(interleaveOrder[i%2],sampler.getProperty(TestElement.NAME));
					}
					else
					{
						assertEquals(order[counter],sampler.getProperty(TestElement.NAME));
					}
					counter++;
				}
			}
		}
		
		public void testProcessing2() throws Exception
		{
			GenericController controller = new GenericController();
			InterleaveControl sub_1 = new InterleaveControl();
			sub_1.setStyle(DEFAULT_STYLE);
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
			sub_1.addTestElement(sub_2);
			String[] order = new String[]{"one","three","two","three","four","three",
						"one","three","two","three","five","three","one","three",
						"two","three","six","three","one","three"};
			int counter = 0;
			while (counter < order.length)
			{
				while(controller.hasNext())
				{
					TestElement sampler = controller.next();
					assertEquals("failed on "+counter,
							order[counter],sampler.getProperty(TestElement.NAME));
					counter++;
				}
			}
		}
		
		public void testProcessing3() throws Exception
		{
			GenericController controller = new GenericController();
			InterleaveControl sub_1 = new InterleaveControl();
			sub_1.setStyle(NEW_STYLE);
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
			sub_1.addTestElement(sub_2);
			String[] order = new String[]{"one","three","two","three","four","five",
						"six","seven","four","five","six","seven","four","five",
						"six","seven","three","one","three","two","three"};
			int counter = 0;
			while (counter < order.length)
			{
				while(controller.hasNext())
				{
					TestElement sampler = controller.next();
					assertEquals("failed on "+counter,order[counter],sampler.getProperty(TestElement.NAME));
					counter++;
				}
			}
		}
		
		public void testProcessing4() throws Exception
		{
			GenericController controller = new GenericController();
			InterleaveControl sub_1 = new InterleaveControl();
			sub_1.setStyle(DEFAULT_STYLE);
			controller.addTestElement(sub_1);
			GenericController sub_2 = new GenericController();
			sub_2.addTestElement(makeSampler("one"));
			sub_2.addTestElement(makeSampler("two"));
			sub_1.addTestElement(sub_2);
			GenericController sub_3 = new GenericController();
			sub_3.addTestElement(makeSampler("three"));
			sub_3.addTestElement(makeSampler("four"));
			sub_1.addTestElement(sub_3);
			String[] order = new String[]{"one","three","two","four"};
			int counter = 0;
			while (counter < order.length)
			{
				while(controller.hasNext())
				{
					TestElement sampler = controller.next();
					assertEquals("failed on "+counter,order[counter],sampler.getProperty(TestElement.NAME));
					counter++;
				}
			}
		}
		
		public void testProcessing5() throws Exception
		{
			GenericController controller = new GenericController();
			InterleaveControl sub_1 = new InterleaveControl();
			sub_1.setStyle(NEW_STYLE);
			controller.addTestElement(sub_1);
			GenericController sub_2 = new GenericController();
			sub_2.addTestElement(makeSampler("one"));
			sub_2.addTestElement(makeSampler("two"));
			sub_1.addTestElement(sub_2);
			GenericController sub_3 = new GenericController();
			sub_3.addTestElement(makeSampler("three"));
			sub_3.addTestElement(makeSampler("four"));
			sub_1.addTestElement(sub_3);
			String[] order = new String[]{"one","two","three","four"};
			int counter = 0;
			while (counter < order.length)
			{
				while(controller.hasNext())
				{
					TestElement sampler = controller.next();
					assertEquals("failed on "+counter,order[counter],sampler.getProperty(TestElement.NAME));
					counter++;
				}
			}
		}

		private TestElement makeSampler(String name)
		{
		  	TestSampler s= new TestSampler();
			s.setName(name);
			return s;
		}
		public class TestSampler extends AbstractSampler {
		  public void addCustomTestElement(TestElement t) { }
		  public org.apache.jmeter.samplers.SampleResult sample(org.apache.jmeter.samplers.Entry e) { return null; }
		}
	}
}
