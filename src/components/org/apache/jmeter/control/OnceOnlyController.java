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

import junit.framework.TestSuite;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;

/************************************************************
 *  Title: Apache JMeter Description: 
 * Copyright: Copyright (c) 2000 
 * Company: Apache Foundation
 *
 *@author     Michael Stover
 *@created    March 13, 2001
 *@version    1.0
 ***********************************************************/

public class OnceOnlyController 
	extends GenericController 
	implements Serializable,LoopIterationListener
{

	/************************************************************
	 *  Constructor for the OnceOnlyController object
	 ***********************************************************/
	public OnceOnlyController() {}

	/**
	 * @see org.apache.jmeter.engine.event.LoopIterationListener#iterationStart(org.apache.jmeter.engine.event.LoopIterationEvent)
	 */
    public void iterationStart(LoopIterationEvent event)
    {
        if (event.getIteration() == 1)
        {
            reInitialize();
        }
    }
    
    protected Sampler nextIsNull() throws NextIsNullException
        {
            return null;
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
			GenericController sub_1 = new OnceOnlyController();
			sub_1.addTestElement(new TestSampler("one"));
			sub_1.addTestElement(new TestSampler("two"));
			controller.addTestElement(sub_1);
			controller.addTestElement(new TestSampler("three"));
			LoopController sub_2 = new LoopController();
			sub_2.setLoops(3);
			GenericController sub_3 = new GenericController();
			sub_2.addTestElement(new TestSampler("four"));
			sub_3.addTestElement(new TestSampler("five"));
			sub_3.addTestElement(new TestSampler("six"));
			sub_2.addTestElement(sub_3);
			sub_2.addTestElement(new TestSampler("seven"));
			controller.addTestElement(sub_2);
			String[] interleaveOrder = new String[]{"one","two"};
			String[] order = new String[]{"","","three","four","five","six","seven",
						"four","five","six","seven","four","five","six","seven"};
			int counter = 15;
            controller.initialize();
			for (int i = 0; i < 4; i++)
			{
				assertEquals(15,counter);
				counter = 0;
				if(i > 0)
				{
					counter = 2;
				}
                TestElement sampler = null;
				while((sampler = controller.next()) != null)
				{
					if(i == 0 && counter < 2)
					{
						assertEquals(interleaveOrder[counter],sampler.getPropertyAsString(TestElement.NAME));
					}
					else
					{
						assertEquals(order[counter],sampler.getPropertyAsString(TestElement.NAME));
					}
					counter++;
				}
			}
		}
        
        public void testProcessing2() throws Exception
                {
                    GenericController controller = new GenericController();
                    GenericController sub_1 = new OnceOnlyController();
                    sub_1.addTestElement(new TestSampler("one"));
                    sub_1.addTestElement(new TestSampler("two"));
                    controller.addTestElement(sub_1);
                    controller.addTestElement(new TestSampler("three"));
                    LoopController sub_2 = new LoopController();
                    sub_2.setLoops(3);
                    OnceOnlyController sub_3 = new OnceOnlyController();
                    sub_2.addTestElement(new TestSampler("four"));
                    sub_3.addTestElement(new TestSampler("five"));
                    sub_3.addTestElement(new TestSampler("six"));
                    sub_2.addTestElement(sub_3);
                    sub_2.addIterationListener(sub_3);
                    sub_2.addTestElement(new TestSampler("seven"));
                    controller.addTestElement(sub_2);
                    String[] interleaveOrder = new String[]{"one","two"};
                    String[] order = new String[]{"","","three","four","five","six","seven",
                                "four","seven","four","seven"};
                    int counter = 11;
                    controller.initialize();
                    for (int i = 0; i < 4; i++)
                    {
                        assertEquals(11,counter);
                        counter = 0;
                        if(i > 0)
                        {
                            counter = 2;
                        }
                        TestElement sampler = null;
                        while((sampler = controller.next()) != null)
                        {
                            if(i == 0 && counter < 2)
                            {
                                assertEquals(interleaveOrder[counter],sampler.getPropertyAsString(TestElement.NAME));
                            }
                            else
                            {
                                assertEquals(order[counter],sampler.getPropertyAsString(TestElement.NAME));
                            }
                            counter++;
                        }
                    }
                }
	}
	
	public static void main(String args[]) { 
		junit.textui.TestRunner.run(suite());
	}
	
	public static TestSuite suite()
	{
		TestSuite suite = new TestSuite();
		suite.addTest(new Test("testProcessing"));
        suite.addTest(new Test("testProcessing2"));
		return suite;
	}

}

