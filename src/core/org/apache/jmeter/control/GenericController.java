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

import junit.framework.TestSuite;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.AbstractTestElement;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/****************************************
 * Title: JMeter 
 * Description: Copyright: Copyright (c) 2000 
 * Company: Apache
 *
 *@author	Michael Stover
 *@author	Thad Smith
 *@created	$Date$
 *@version	1.0
 ***************************************/

public class GenericController extends AbstractTestElement implements Controller, Serializable
{
    protected static Logger log = LoggingManager.getLoggerFor(JMeterUtils.ELEMENTS);
    protected List iterationListeners = new LinkedList();
    protected List subControllersAndSamplers = new ArrayList();

    protected int current;
    private int iterCount;
    private boolean done, first;

    /**
     * Creates a Generic Controller
     */
    public GenericController()
    {
        initialize();
    }

    public void initialize()
    {
        resetCurrent();
        resetIterCount();
        done = false;
        first = true;
        TestElement elem;
        for (int i = 0; i < subControllersAndSamplers.size(); i++)
        {
            elem = (TestElement) subControllersAndSamplers.get(i);
            if (elem instanceof Controller)
            {
                ((Controller) elem).initialize();
            }
        }
    }

    protected void reInitialize()
    {
        resetCurrent();
        incrementIterCount();
        setFirst(true);
    }

    /**
     * @see org.apache.jmeter.control.Controller#next()
     */
    public Sampler next()
    {
        fireIterEvents();
        log.debug("Calling next on: " + this.getClass().getName());
        Sampler returnValue = null;
        TestElement currentElement = null;
        try
        {
            currentElement = getCurrentElement();
            setCurrentElement(currentElement);
            if (currentElement == null)
            {
                //incrementCurrent();  
                returnValue = nextIsNull();
            }
            else
            {
                if (currentElement instanceof Sampler)
                {
                    returnValue = nextIsASampler((Sampler) currentElement);
                }
                else
                {
                    returnValue = nextIsAController((Controller) currentElement);
                }
            }
        }
        catch (NextIsNullException e)
        {
            returnValue = null;
        }
        return returnValue;
    }

    /**
     * @see org.apache.jmeter.control.Controller#isDone()
     */
    public boolean isDone()
    {
        return done;
    }

    protected void setDone(boolean done)
    {
        this.done = done;
    }

    protected boolean isFirst()
    {
        return first;
    }

    public void setFirst(boolean b)
    {
        first = b;
    }

    protected Sampler nextIsAController(Controller controller) throws NextIsNullException
    {
        Sampler returnValue;
        Sampler sampler = controller.next();
        if (sampler == null)
        {
            currentReturnedNull(controller);
            returnValue = next();
        }
        else
        {
            returnValue = sampler;
        }
        return returnValue;
    }

    protected Sampler nextIsASampler(Sampler element) throws NextIsNullException
    {
        incrementCurrent();
        return element;
    }

    protected Sampler nextIsNull() throws NextIsNullException
    {
        reInitialize();
        return null;
    }

    protected void currentReturnedNull(Controller c)
    {
        if (c.isDone())
        {
            removeCurrentElement();
        }
        else
        {
            incrementCurrent();
        }
    }

    /**
     * Gets the SubControllers attribute of the 
     * GenericController object
     *
     *	@return	The SubControllers value
     */
    protected List getSubControllers()
    {
        return subControllersAndSamplers;
    }

    private void addElement(TestElement child)
    {
        subControllersAndSamplers.add(child);
    }

    protected void setCurrentElement(TestElement currentElement) throws NextIsNullException
    {}

    protected TestElement getCurrentElement() throws NextIsNullException
    {
        if (current < subControllersAndSamplers.size())
        {
            return (TestElement) subControllersAndSamplers.get(current);
        }
        else
        {
            if (subControllersAndSamplers.size() == 0)
            {
                setDone(true);
                throw new NextIsNullException();
            }
            return null;
        }
    }

    protected void removeCurrentElement()
    {
        subControllersAndSamplers.remove(current);
    }

    protected void incrementCurrent()
    {
        current++;
    }

    protected void resetCurrent()
    {
        current = 0;
    }

    /**
     * @see org.apache.jmeter.testelement.TestElement#addTestElement(org.apache.jmeter.testelement.TestElement)
     */
    public void addTestElement(TestElement child)
    {
        if (child instanceof Controller || child instanceof Sampler)
        {
            addElement(child);
        }
    }

    public void addIterationListener(LoopIterationListener lis)
    {
        iterationListeners.add(lis);
    }

    protected void fireIterEvents()
    {
        if (isFirst())
        {
            fireIterationStart();
            first = false;
        }
    }

    protected void fireIterationStart()
    {
        Iterator iter = iterationListeners.iterator();
        LoopIterationEvent event = new LoopIterationEvent(this, getIterCount());
        while (iter.hasNext())
        {
            LoopIterationListener item = (LoopIterationListener) iter.next();
            item.iterationStart(event);
        }
    }

    protected int getIterCount()
    {
        return iterCount;
    }

    protected void incrementIterCount()
    {
        iterCount++;
    }

    protected void resetIterCount()
    {
        iterCount = 0;
    }

    public static class Test extends JMeterTestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void testProcessing() throws Exception
        {
            testLog.debug("Testing Generic Controller");
            GenericController controller = new GenericController();
            GenericController sub_1 = new GenericController();
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));
            controller.addTestElement(sub_1);
            controller.addTestElement(new TestSampler("three"));
            GenericController sub_2 = new GenericController();
            GenericController sub_3 = new GenericController();
            sub_2.addTestElement(new TestSampler("four"));
            sub_3.addTestElement(new TestSampler("five"));
            sub_3.addTestElement(new TestSampler("six"));
            sub_2.addTestElement(sub_3);
            sub_2.addTestElement(new TestSampler("seven"));
            controller.addTestElement(sub_2);
            String[] order = new String[] { "one", "two", "three", "four", "five", "six", "seven" };
            int counter = 7;
            controller.initialize();
            for (int i = 0; i < 2; i++)
            {
                assertEquals(7, counter);
                counter = 0;
                TestElement sampler = null;
                while ((sampler = controller.next()) != null)
                {
                    assertEquals(order[counter++], sampler.getPropertyAsString(TestElement.NAME));
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
        return suite;
    }
}
