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

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.IntegerProperty;

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
    public static final int IGNORE_SUB_CONTROLLERS = 0;
    public static final int USE_SUB_CONTROLLERS = 1;
    private boolean skipNext;
    private boolean doNotIncrement = false;
    private TestElement searchStart = null;
    private boolean currentReturnedAtLeastOne;
    private boolean stillSame = true;

    /****************************************
     * Constructor for the InterleaveControl object
     ***************************************/
    public InterleaveControl()
    {}

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#reInitialize()
     */
    public void reInitialize()
    {
        setFirst(true);
        currentReturnedAtLeastOne = false;
        searchStart = null;
        stillSame = true;
        skipNext = false;
        incrementIterCount();
    }

    public void setStyle(int style)
    {
        setProperty(new IntegerProperty(STYLE, style));
    }

    public int getStyle()
    {
        return getPropertyAsInt(STYLE);
    }
    
    /* (non-Javadoc)
	 * @see org.apache.jmeter.control.Controller#next()
	 */
	public Sampler next()
	{
		if(isSkipNext())
		{
			reInitialize();
			return null;
		}
		return super.next();
	}

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#nextIsAController(Controller)
     */
    protected Sampler nextIsAController(Controller controller) throws NextIsNullException
    {
        Sampler sampler = controller.next();
        if (sampler == null)
        {
            currentReturnedNull(controller);
            return next();
        }
        else
        {
            currentReturnedAtLeastOne = true;
            if (getStyle() == IGNORE_SUB_CONTROLLERS)
            {
                incrementCurrent();
                skipNext = true;
            }
            else
            {
                searchStart = null;
            }
            return sampler;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#nextIsASampler(Sampler)
     */
    protected Sampler nextIsASampler(Sampler element) throws NextIsNullException
    {
        skipNext = true;
        incrementCurrent();
        return element;
    }

    /**
     * If the current is null, reset and continue searching.  The 
     * searchStart attribute will break us off when we start a repeat.
     * 
     * @see org.apache.jmeter.testelement.AbstractTestElement#nextIsNull()
     */
    protected Sampler nextIsNull()
    {
        resetCurrent();
        return next();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#setCurrentElement(TestElement)
     */
    protected void setCurrentElement(TestElement currentElement) throws NextIsNullException
    {
        if (searchStart == null) // set the position when next is first called, and don't overwrite until reInitialize is called
        {
            searchStart = currentElement;
        }
        else if (searchStart == currentElement && !stillSame) // we've gone through the whole list and are now back at the start point of our search.
        {
            reInitialize();
            throw new NextIsNullException();
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#currentReturnedNull(org.apache.jmeter.control.Controller)
     */
    protected void currentReturnedNull(Controller c)
    {
        if (c.isDone())
        {
            removeCurrentElement();
        }
        else if(getStyle() == USE_SUB_CONTROLLERS)
        {
            incrementCurrent();
        }
    }

    /**
     * @return skipNext
     */
    protected boolean isSkipNext()
    {
        return skipNext;
    }

    /**
     * @param skipNext
     */
    protected void setSkipNext(boolean skipNext)
    {
        this.skipNext = skipNext;
    }

    public static class Test extends JMeterTestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void testProcessing() throws Exception
        {
            testLog.debug("Testing Interleave Controller 1");
            GenericController controller = new GenericController();
            InterleaveControl sub_1 = new InterleaveControl();
            sub_1.setStyle(IGNORE_SUB_CONTROLLERS);
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
            String[] interleaveOrder = new String[] { "one", "two" };
            String[] order = new String[] { "dummy", "three", "four", "five", "six", "seven", "four", "five", "six", "seven", "four", "five", "six", "seven" };
            int counter = 14;
            controller.initialize();
            for (int i = 0; i < 4; i++)
            {
                assertEquals(14, counter);
                counter = 0;
                TestElement sampler = null;
                while ((sampler = controller.next()) != null)
                {
                    if (counter == 0)
                    {
                        assertEquals(interleaveOrder[i % 2], sampler.getPropertyAsString(TestElement.NAME));
                    }
                    else
                    {
                        assertEquals(order[counter], sampler.getPropertyAsString(TestElement.NAME));
                    }
                    counter++;
                }
            }
        }
        
        public void testProcessing6() throws Exception
               {
                   testLog.debug("Testing Interleave Controller 6");
                   GenericController controller = new GenericController();
                   InterleaveControl sub_1 = new InterleaveControl();
                   controller.addTestElement(new TestSampler("one"));
                   sub_1.setStyle(IGNORE_SUB_CONTROLLERS);
                   controller.addTestElement(sub_1);
                   LoopController sub_2 = new LoopController();
                   sub_1.addTestElement(sub_2);
                   sub_2.setLoops(3);
                   int counter = 1;
                   controller.initialize();
                   for (int i = 0; i < 4; i++)
                   {
                       assertEquals(1, counter);
                       counter = 0;
                       TestElement sampler = null;
                       while ((sampler = controller.next()) != null)
                       {
                               assertEquals("one", sampler.getPropertyAsString(TestElement.NAME));
                           counter++;
                       }
                   }
               }

        public void testProcessing2() throws Exception
        {
            testLog.debug("Testing Interleave Controller 2");
            GenericController controller = new GenericController();
            InterleaveControl sub_1 = new InterleaveControl();
            sub_1.setStyle(IGNORE_SUB_CONTROLLERS);
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
            sub_1.addTestElement(sub_2);
            String[] order =
                new String[] {
                    "one",
                    "three",
                    "two",
                    "three",
                    "four",
                    "three",
                    "one",
                    "three",
                    "two",
                    "three",
                    "five",
                    "three",
                    "one",
                    "three",
                    "two",
                    "three",
                    "six",
                    "three",
                    "one",
                    "three" };
            int counter = 0;
            int loops = 1;
            controller.initialize();
            while (counter < order.length)
            {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null)
                {
                    assertEquals("failed on " + counter, order[counter], sampler.getPropertyAsString(TestElement.NAME));
                    counter++;
                }
            }
        }

        public void testProcessing3() throws Exception
        {
            testLog.debug("Testing Interleave Controller 3");
            GenericController controller = new GenericController();
            InterleaveControl sub_1 = new InterleaveControl();
            sub_1.setStyle(USE_SUB_CONTROLLERS);
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
            sub_1.addTestElement(sub_2);
            String[] order =
                new String[] {
                    "one",
                    "three",
                    "two",
                    "three",
                    "four",
                    "five",
                    "six",
                    "seven",
                    "four",
                    "five",
                    "six",
                    "seven",
                    "four",
                    "five",
                    "six",
                    "seven",
                    "three",
                    "one",
                    "three",
                    "two",
                    "three" };
            int counter = 0;
            int loops = 1;
            controller.initialize();
            while (counter < order.length)
            {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null)
                {
                    assertEquals("failed on" + counter, order[counter], sampler.getPropertyAsString(TestElement.NAME));
                    counter++;
                }
            }
        }

        public void testProcessing4() throws Exception
        {
            testLog.debug("Testing Interleave Controller 4");
            GenericController controller = new GenericController();
            InterleaveControl sub_1 = new InterleaveControl();
            sub_1.setStyle(IGNORE_SUB_CONTROLLERS);
            controller.addTestElement(sub_1);
            GenericController sub_2 = new GenericController();
            sub_2.addTestElement(new TestSampler("one"));
            sub_2.addTestElement(new TestSampler("two"));
            sub_1.addTestElement(sub_2);
            GenericController sub_3 = new GenericController();
            sub_3.addTestElement(new TestSampler("three"));
            sub_3.addTestElement(new TestSampler("four"));
            sub_1.addTestElement(sub_3);
            String[] order = new String[] { "one", "three", "two", "four" };
            int counter = 0;
            int loops = 1;
            controller.initialize();
            while (counter < order.length)
            {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null)
                {
                    assertEquals("failed on" + counter, order[counter], sampler.getPropertyAsString(TestElement.NAME));
                    counter++;
                }
            }
        }

        public void testProcessing5() throws Exception
        {
            testLog.debug("Testing Interleave Controller 5");
            GenericController controller = new GenericController();
            InterleaveControl sub_1 = new InterleaveControl();
            sub_1.setStyle(USE_SUB_CONTROLLERS);
            controller.addTestElement(sub_1);
            GenericController sub_2 = new GenericController();
            sub_2.addTestElement(new TestSampler("one"));
            sub_2.addTestElement(new TestSampler("two"));
            sub_1.addTestElement(sub_2);
            GenericController sub_3 = new GenericController();
            sub_3.addTestElement(new TestSampler("three"));
            sub_3.addTestElement(new TestSampler("four"));
            sub_1.addTestElement(sub_3);
            String[] order = new String[] { "one", "two", "three", "four" };
            int counter = 0;
            int loops = 1;
            controller.initialize();
            while (counter < order.length)
            {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null)
                {
                    assertEquals("failed on" + counter, order[counter], sampler.getPropertyAsString(TestElement.NAME));
                    counter++;
                }
            }
        }
    }

    public static void main(String args[])
    {
        junit.textui.TestRunner.run(suite());
    }

    public static TestSuite suite()
    {
        TestSuite suite = new TestSuite();
        suite.addTest(new Test("testProcessing"));
        suite.addTest(new Test("testProcessing2"));
        suite.addTest(new Test("testProcessing3"));
        suite.addTest(new Test("testProcessing4"));
        suite.addTest(new Test("testProcessing5"));
        //suite.addTestSuite(Test.class);
        return suite;
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#incrementCurrent()
     */
    protected void incrementCurrent()
    {
        if (currentReturnedAtLeastOne)
        {
            skipNext = true;
        }
        stillSame = false;
        super.incrementCurrent();
    }

}
