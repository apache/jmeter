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

import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;
//NOTUSED import org.apache.jorphan.logging.LoggingManager;
//NOTUSED import org.apache.log.Logger;

/**
 * @author    Michael Stover
 * @author    Thad Smith
 * @version   $Revision$
 */
public class LoopController extends GenericController implements Serializable
{
    //NOTUSED private static Logger log = LoggingManager.getLoggerForClass();

    private final static String LOOPS = "LoopController.loops";
    private final static String CONTINUE_FOREVER =
        "LoopController.continue_forever";
    private transient int loopCount = 0;

    public LoopController()
    {
        setContinueForever(true);
    }

    public void setLoops(int loops)
    {
        setProperty(new IntegerProperty(LOOPS, loops));
    }

    public void setLoops(String loopValue)
    {
        setProperty(new StringProperty(LOOPS, loopValue));
    }

    public int getLoops()
    {
        try
        {
        	JMeterProperty prop = getProperty(LOOPS);
            return Integer.parseInt(prop.getStringValue());
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    public String getLoopString()
    {
        return getPropertyAsString(LOOPS);
    }

    /**
     * Determines whether the loop will return any samples if it is rerun.
     * 
     * @param forever true if the loop must be reset after ending a run
     */
    public void setContinueForever(boolean forever)
    {
        setProperty(new BooleanProperty(CONTINUE_FOREVER, forever));
    }

    public boolean getContinueForever()
    {
        return getPropertyAsBoolean(CONTINUE_FOREVER);
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.Controller#isDone()
     */
    public boolean isDone()
    {
        if (getLoops() != 0)
        {
            return super.isDone();
        }
        else
        {
            return true;
        }
    }

    private boolean endOfLoop()
    {
        return (getLoops() > -1) && loopCount >= getLoops();
    }

    /* (non-Javadoc)
     * @see org.apache.jmeter.control.GenericController#nextIsNull()
     */
    protected Sampler nextIsNull() throws NextIsNullException
    {
        reInitialize();
        if (endOfLoop())
        {
            if (!getContinueForever())
            {
                setDone(true);
            }
            else
            {
                resetLoopCount();
            }
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
        recoverRunningVersion();
	}

///////////////////////// Start of Test code ///////////////////////////////

    public static class Test extends JMeterTestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void testProcessing() throws Exception
        {
            GenericController controller = new GenericController();
            GenericController sub_1 = new GenericController();
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
            String[] order =
                new String[] {
                    "one",
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
                    "seven" };
            int counter = 15;
            controller.setRunningVersion(true);
            sub_1.setRunningVersion(true);
            sub_2.setRunningVersion(true);
            sub_3.setRunningVersion(true);
            controller.initialize();
            for (int i = 0; i < 2; i++)
            {
                assertEquals(15, counter);
                counter = 0;
                TestElement sampler = null;
                while ((sampler = controller.next()) != null)
                {
                    assertEquals(
                        order[counter++],
                        sampler.getPropertyAsString(TestElement.NAME));
                }
            }
        }
        
        public void testLoopZeroTimes() throws Exception
        {
            LoopController loop = new LoopController();
            loop.setLoops(0);
            loop.addTestElement(new TestSampler("never run"));
            loop.initialize();
            assertNull(loop.next());
        }

        public void testInfiniteLoop() throws Exception
        {
            LoopController loop = new LoopController();
            loop.setLoops(-1);
            loop.addTestElement(new TestSampler("never run"));
            loop.setRunningVersion(true);
            loop.initialize();
            for (int i=0; i<42; i++)
            {
                assertNotNull(loop.next());
            }
        }

	}
}