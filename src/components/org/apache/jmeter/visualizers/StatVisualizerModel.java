// $Header$
/*
 * Copyright 2002-2004 The Apache Software Foundation.
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

package org.apache.jmeter.visualizers; // java


import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.jmeter.samplers.Clearable;
import org.apache.jmeter.samplers.SampleResult;


/**
 * Aggregrate Table-Based Reporting Model for JMeter.  Props to the people
 * who've done the other visualizers ahead of me (Stefano Mazzocchi), who I
 * borrowed code from to start me off (and much code may still exist). Thank
 * you!
 * 
 * @author    James Boutcher
 * Created     March 21, 2002
 * @version   $Revision$ Last updated: $Date$
 */
public class StatVisualizerModel implements Clearable
{
    private String name;
    private List listeners;
    private Vector runningSamples;
    private Map labelMap;
    private RunningSample total;

    /**
     * Default Constuctor.
     */
    public StatVisualizerModel()
    {
        listeners = new LinkedList();
        runningSamples = new Vector(0, 10);
        labelMap = Collections.synchronizedMap(new HashMap(10));
        total = new RunningSample("__TOTAL__", -1);
    }

    /**
     * Sets the Name attribute of the StatVisualizerModel object.
     *
     * @param name  the new Name value
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * Gets the GuiClass attribute of the StatVisualizerModel object.
     *
     * @return  the GuiClass value
     */
    public Class getGuiClass()
    {
        return StatVisualizer.class;
    }

    /**
     * Gets the Name attribute of the StatVisualizerModel object.
     *
     * @return  the Name value
     */
    public String getName()
    {
        return name;
    }

    /**
     * Registers a listener (a visualizer, graph, etc) to this model. This will
     * allow the model to fire GUI updates to anyone when data changes, etc.
     */
    public void addGraphListener(GraphListener listener)
    {
        listeners.add(listener);
    }

    public void addAccumListener(AccumListener listener)
    {
        listeners.add(listener);
    }

    public int getRunningSampleCount()
    {
        return runningSamples.size();
    }

    public RunningSample getRunningSample(int index)
    {
        return (RunningSample) runningSamples.get(index);
    }

    public RunningSample getRunningSample(String label)
    {
        return (RunningSample) labelMap.get(label);
    }

    public RunningSample getRunningSampleTotal()
    {
        return total;
    }

    public void addNewSample(SampleResult res)
    {
        String aLabel = res.getSampleLabel();
        RunningSample s;

        synchronized (labelMap)
        {
            s = (RunningSample) labelMap.get(aLabel);
            if (s == null)
            {
                s = new RunningSample(aLabel, runningSamples.size());
                runningSamples.add(s);
                labelMap.put(aLabel, s);
            }
        }
        s.addSample(res);
        total.addSample(res);
        this.fireDataChanged(s);
    }

    /**
     * Reset everything we can in the model.
     */
    public void clear()
    {
        // clear the data structures
        runningSamples.clear();
        labelMap.clear();
        total = new RunningSample("__TOTAL__", -1);
        this.fireDataChanged();
    }

    /**
     * Called when the model changes - then we call out to all registered
     * listeners and tell them to update themselves.
     */
    protected void fireDataChanged()
    {
        Iterator iter = listeners.iterator();

        while (iter.hasNext())
        {
            Object myObj = iter.next();

            if (!(myObj instanceof GraphListener))
            {
                continue;
            }
            ((GraphListener) myObj).updateGui();
        }
    }

    protected void fireDataChanged(RunningSample s)
    {
        Iterator iter = listeners.iterator();

        while (iter.hasNext())
        {
            Object myObj = iter.next();

            if (!(myObj instanceof AccumListener))
            {
                continue;
            }
            ((AccumListener) myObj).updateGui(s);
        }
    }

    public static class Test extends junit.framework.TestCase
    {
        public Test(String name)
        {
            super(name);
        }

        private SampleResult sample(String label, long start,
                long elapsed, boolean ok)
        {
            SampleResult res = SampleResult.createTestSample(start, start+elapsed);

            res.setSampleLabel(label);
            res.setSuccessful(ok);
            assertEquals(elapsed,res.getTime());
            return res;
        }

        public void testStatisticsCalculation()
        {
            StatVisualizerModel m = new StatVisualizerModel();
            long t0 = System.currentTimeMillis();


			//m.addNewSample(sample("1", t0 + 100, 100, true));
			//m.addNewSample(sample("2", t0 + 350, 200, true));
			//m.addNewSample(sample("1", t0 + 600, 300, true));

            /*
             * Create 3 samples lasting a total of 600 ms
             */
            m.addNewSample(sample("1", t0 + 000, 100, true));
            m.addNewSample(sample("2", t0 + 150, 200, true));
            m.addNewSample(sample("1", t0 + 300, 300, true));
            
            assertEquals(2, m.getRunningSampleCount());
            assertEquals(2, m.labelMap.size());

            {
                RunningSample s = m.getRunningSample("1");

                assertEquals("1", s.getLabel());
                assertEquals(2, s.getNumSamples());
                assertEquals(100, s.getMin());
                assertEquals(300, s.getMax());
                assertEquals(200, s.getAverage());
				assertEquals(600, s.getElapsed());
            }

            {
                RunningSample s = m.getRunningSample("2");

                assertEquals("2", s.getLabel());
                assertEquals(1, s.getNumSamples());
                assertEquals(200, s.getMin());
                assertEquals(200, s.getMax());
                assertEquals(200, s.getAverage());
				assertEquals(200, s.getElapsed());
            }

            {
                RunningSample s = m.getRunningSampleTotal();

                assertEquals(3, s.getNumSamples());
                assertEquals(100, s.getMin());
                assertEquals(300, s.getMax());
                assertEquals(200, s.getAverage());
                assertEquals(600, s.getElapsed());
                assertEquals(5.0, s.getRate(), 1e-6);
            }
        }
    }
}
