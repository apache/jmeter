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

import java.io.IOException;
import java.io.Serializable;

import org.apache.jmeter.engine.event.LoopIterationEvent;
import org.apache.jmeter.engine.event.LoopIterationListener;
import org.apache.jmeter.junit.JMeterTestCase;
import org.apache.jmeter.junit.stubs.TestSampler;
import org.apache.jmeter.samplers.Sampler;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestListener;
import org.apache.jmeter.testelement.property.BooleanProperty;
import org.apache.jmeter.testelement.property.FloatProperty;
import org.apache.jmeter.testelement.property.IntegerProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.StringProperty;

/**
 * This class represents a controller that can controll the 
 * number of times that it is executed, either by the total number
 * of times the user wants the controller executed (BYNUMBER)
 * or by the percentage of time it is called (BYPERCENT)
 *
 * @author Thad Smith
 * @version $Revision$
 */
public class ThroughputController
    extends GenericController
    implements Serializable, LoopIterationListener, TestListener
{

    public static final int BYNUMBER = 0;
    public static final int BYPERCENT = 1;

    private static final String STYLE = "ThroughputController.style";
    private static final String PERTHREAD = "ThroughputController.perThread";
    private static final String MAXTHROUGHPUT =
        "ThroughputController.maxThroughput";
    private static final String PERCENTTHROUGHPUT =
        "ThroughputController.percentThroughput";

    private int globalNumExecutions;
    private int globalIteration;
    private transient Object counterLock;

    /**
     * Number of iterations on which we've chosen to deliver samplers.
     */
    private int numExecutions = 0;
    
    /**
     * Index of the current iteration. 0-based.
     */
    private int iteration = -1;
    
    /**
     * Whether to deliver samplers on this iteration.
     */
    private boolean runThisTime;

    public ThroughputController()
    {
        globalNumExecutions = 0;
        globalIteration = -1;
        counterLock = new Object();
        setStyle(BYNUMBER);
        setPerThread(true);
        setMaxThroughput(1);
        setPercentThroughput(100);
        runThisTime = false;
    }

    public void setStyle(int style)
    {
        setProperty(new IntegerProperty(STYLE, style));
    }

    public int getStyle()
    {
        return getPropertyAsInt(STYLE);
    }

    public void setPerThread(boolean perThread)
    {
        setProperty(new BooleanProperty(PERTHREAD, perThread));
    }

    public boolean isPerThread()
    {
        return getPropertyAsBoolean(PERTHREAD);
    }

    public void setMaxThroughput(int maxThroughput)
    {
        setProperty(new IntegerProperty(MAXTHROUGHPUT, maxThroughput));
    }

    public void setMaxThroughput(String maxThroughput)
    {
        setProperty(new StringProperty(MAXTHROUGHPUT, maxThroughput));
    }

    public String getMaxThroughput()
    {
        return getPropertyAsString(MAXTHROUGHPUT);
    }

    protected int getMaxThroughputAsInt()
    {
        JMeterProperty prop = getProperty(MAXTHROUGHPUT);
        int retVal = 1;
        if (prop instanceof IntegerProperty)
        {
            retVal = (((IntegerProperty) prop).getIntValue());
        }
        else
        {
            try
            {
                retVal = Integer.parseInt(prop.getStringValue());
            }
            catch (NumberFormatException e)
            {
            }
        }
        return retVal;
    }

    public void setPercentThroughput(float percentThroughput)
    {
        setProperty(new FloatProperty(PERCENTTHROUGHPUT, percentThroughput));
    }

    public void setPercentThroughput(String percentThroughput)
    {
        setProperty(new StringProperty(PERCENTTHROUGHPUT, percentThroughput));
    }

    public String getPercentThroughput()
    {
        return getPropertyAsString(PERCENTTHROUGHPUT);
    }

    protected float getPercentThroughputAsFloat()
    {
        JMeterProperty prop = getProperty(PERCENTTHROUGHPUT);
        float retVal = 100;
        if (prop instanceof FloatProperty)
        {
            retVal = (((FloatProperty) prop).getFloatValue());
        }
        else
        {
            try
            {
                retVal = Float.parseFloat(prop.getStringValue());
            }
            catch (NumberFormatException e)
            {
            }
        }
        return retVal;
    }

    protected void setExecutions(int executions)
    {
        if (!isPerThread())
        {
            globalNumExecutions=executions;
        }
        this.numExecutions = executions;
    }

    protected int getExecutions()
    {
        if (!isPerThread())
        {
            return globalNumExecutions;
        }
        else
        {
            return numExecutions;
        }
    }

    private void increaseExecutions()
    {
        setExecutions(getExecutions() + 1);
    }

    protected void setIteration(int iteration)
    {
        if (!isPerThread())
        {
            globalIteration=iteration;
        }
        this.iteration = iteration;
    }

    protected int getIteration()
    {
        if (!isPerThread())
        {
            return globalIteration;
        }
        else
        {
            return iteration;
        }
    }

    private void increaseIteration()
    {
        setIteration(getIteration() + 1);
    }

    /**
     * @see org.apache.jmeter.control.Controller#next()
     */
    public Sampler next()
    {
        if (runThisTime)
        {
            return super.next();
        }
        return null;
    }

    /**
     * Decide whether to return any samplers on this iteration.
     */
    private boolean decide()
    {
        int executions, iterations;

        executions = getExecutions();
        iterations = getIteration();
        if (getStyle() == BYNUMBER)
        {
            return executions < getMaxThroughputAsInt();
        }
        else
        {
            return (100.0*executions+50.0) / (iterations+1)
                    < getPercentThroughputAsFloat();
        }
    }

    /**
     * @see org.apache.jmeter.control.Controller#isDone()
     */
    public boolean isDone()
    {
        if (subControllersAndSamplers.size() == 0)
        {
            return true;
        }
        else if (
            getStyle() == BYNUMBER
                && getExecutions() >= getMaxThroughputAsInt()
                && current >= getSubControllers().size())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public Object clone()
    {
        ThroughputController clone = (ThroughputController) super.clone();
        clone.numExecutions = numExecutions;
        clone.iteration = iteration;
        clone.globalNumExecutions = globalNumExecutions;
        clone.globalIteration = globalIteration;
        clone.counterLock = counterLock;
        clone.runThisTime = false;
        return clone;
    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        counterLock = new Object();
    }

    /* (non-Javadoc)
     * @see LoopIterationListener#iterationStart(LoopIterationEvent)
     */
    public void iterationStart(LoopIterationEvent iterEvent)
    {
        if (!isPerThread())
        {
            synchronized (counterLock)
            {
                increaseIteration();
                runThisTime= decide();
                if (runThisTime) increaseExecutions();
            }
        }
        else
        {
            increaseIteration();
            runThisTime= decide();
            if (runThisTime) increaseExecutions();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testStarted()
     */
    public void testStarted()
    {
        setExecutions(0);
        setIteration(-1);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.jmeter.testelement.TestListener#testEnded()
     */
    public void testEnded()
    {
    }

    /*
     * (non-Javadoc)
     * @see TestListener#testStarted(String)
     */
    public void testStarted(String host)
    {
    }

    /*
     * (non-Javadoc)
     * @see TestListener#testEnded(String)
     */
    public void testEnded(String host)
    {
    }

    /*
     * (non-Javadoc)
     * @see TestListener#testIterationStart(LoopIterationEvent)
     */
    public void testIterationStart(LoopIterationEvent event)
    {
    }

    /////////////////////////// Start of Test Code ///////////////////////////

    public static class Test extends JMeterTestCase
    {
        public Test(String name)
        {
            super(name);
        }

        public void testByNumber() throws Exception
        {
            ThroughputController sub_1 = new ThroughputController();
            sub_1.setStyle(BYNUMBER);
            sub_1.setMaxThroughput(2);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));

            LoopController loop = new LoopController();
            loop.setLoops(5);
            loop.addTestElement(new TestSampler("zero"));
            loop.addTestElement(sub_1);
            loop.addIterationListener(sub_1);
            loop.addTestElement(new TestSampler("three"));

            LoopController test = new LoopController();
            test.setLoops(2);
            test.addTestElement(loop);

            String[] order =
                new String[] {
                    "zero",
                    "one",
                    "two",
                    "three",
                    "zero",
                    "one",
                    "two",
                    "three",
                    "zero",
                    "three",
                    "zero",
                    "three",
                    "zero",
                    "three",
                    "zero",
                    "three",
                    "zero",
                    "three",
                    "zero",
                    "three",
                    "zero",
                    "three",
                    "zero",
                    "three",
                };
            sub_1.testStarted();
            test.initialize();
            for (int counter= 0; counter < order.length; counter++)
            {
                TestElement sampler = test.next();
                assertNotNull(sampler);
                assertEquals("Counter: "+counter
                        +", executions: "+sub_1.getExecutions()
                        +", iteration: "+sub_1.getIteration(),
                    order[counter],
                    sampler.getPropertyAsString(TestElement.NAME)
                    );
            }
            assertNull(test.next());
            sub_1.testEnded();
        }

        public void testByNumberZero() throws Exception
        {
            ThroughputController sub_1 = new ThroughputController();
            sub_1.setStyle(BYNUMBER);
            sub_1.setMaxThroughput(0);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));
        
            LoopController controller = new LoopController();
            controller.setLoops(5);
            controller.addTestElement(new TestSampler("zero"));
            controller.addTestElement(sub_1);
            controller.addIterationListener(sub_1);
            controller.addTestElement(new TestSampler("three"));
        
            String[] order =
                new String[] {
                    "zero",
                    "three",
                    "zero",
                    "three",
                    "zero",
                    "three",
                    "zero",
                    "three",
                    "zero",
                    "three",
                };
            int counter= 0;
            sub_1.testStarted();
            controller.initialize();
            for (int i=0; i<3; i++)
            {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null)
                {
                    assertEquals("Counter: "+counter+", i: "+i,
                        order[counter],
                        sampler.getPropertyAsString(TestElement.NAME)
                        );
                    counter++;
                }
                assertEquals(counter, order.length);
                counter= 0;
            }
            sub_1.testEnded();
        }

        public void testByPercent33() throws Exception
        {
            ThroughputController sub_1 = new ThroughputController();
            sub_1.setStyle(BYPERCENT);
            sub_1.setPercentThroughput(33.33f);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));

            LoopController controller = new LoopController();
            controller.setLoops(6);
            controller.addTestElement(new TestSampler("zero"));
            controller.addTestElement(sub_1);
            controller.addIterationListener(sub_1);
            controller.addTestElement(new TestSampler("three"));
            // Expected results established using the DDA
            // algorithm (see http://www.siggraph.org/education/materials/HyperGraph/scanline/outprims/drawline.htm):
            String[] order =
                new String[] {
                    "zero", // 0/1 vs. 1/1 -> 0 is closer to 33.33
                    "three",
                    "zero",  // 0/2 vs. 1/2 -> 50.0 is closer to 33.33
                    "one",
                    "two",
                    "three",
                    "zero", // 1/3 vs. 2/3 -> 33.33 is closer to 33.33
                    "three",
                    "zero", // 1/4 vs. 2/4 -> 25.0 is closer to 33.33
                    "three",
                    "zero", // 1/5 vs. 2/5 -> 40.0 is closer to 33.33
                    "one",
                    "two",
                    "three",
                    "zero", // 2/6 vs. 3/6 -> 33.33 is closer to 33.33
                    "three",
                    // etc...
                };
            int counter= 0;
            sub_1.testStarted();
            controller.initialize();
            for (int i=0; i<3; i++)
            {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null)
                {
                    assertEquals("Counter: "+counter+", i: "+i,
                        order[counter],
                        sampler.getPropertyAsString(TestElement.NAME)
                        );
                    counter++;
                }
                assertEquals(counter, order.length);
                counter= 0;
            }
            sub_1.testEnded();
        }

        public void testByPercentZero() throws Exception
        {
            ThroughputController sub_1 = new ThroughputController();
            sub_1.setStyle(BYPERCENT);
            sub_1.setPercentThroughput(0.0f);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));
        
            LoopController controller = new LoopController();
            controller.setLoops(150);
            controller.addTestElement(new TestSampler("zero"));
            controller.addTestElement(sub_1);
            controller.addIterationListener(sub_1);
            controller.addTestElement(new TestSampler("three"));
        
            String[] order =
                new String[] {
                    "zero",
                    "three",
                };
            int counter= 0;
            sub_1.testStarted();
            controller.initialize();
            for (int i=0; i<3; i++)
            {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null)
                {
                    assertEquals("Counter: "+counter+", i: "+i,
                        order[counter%order.length],
                        sampler.getPropertyAsString(TestElement.NAME)
                        );
                    counter++;
                }
                assertEquals(counter, 150*order.length);
                counter= 0;
            }
            sub_1.testEnded();
        }

        public void testByPercent100() throws Exception
        {
            ThroughputController sub_1 = new ThroughputController();
            sub_1.setStyle(BYPERCENT);
            sub_1.setPercentThroughput(100.0f);
            sub_1.addTestElement(new TestSampler("one"));
            sub_1.addTestElement(new TestSampler("two"));
        
            LoopController controller = new LoopController();
            controller.setLoops(150);
            controller.addTestElement(new TestSampler("zero"));
            controller.addTestElement(sub_1);
            controller.addIterationListener(sub_1);
            controller.addTestElement(new TestSampler("three"));
        
            String[] order =
                new String[] {
                    "zero",
                    "one",
                    "two",
                    "three",
                };
            int counter= 0;
            sub_1.testStarted();
            controller.initialize();
            for (int i=0; i<3; i++)
            {
                TestElement sampler = null;
                while ((sampler = controller.next()) != null)
                {
                    assertEquals("Counter: "+counter+", i: "+i,
                        order[counter%order.length],
                        sampler.getPropertyAsString(TestElement.NAME)
                        );
                    counter++;
                }
                assertEquals(counter, 150*order.length);
                counter= 0;
            }
            sub_1.testEnded();
        }
    }
}
